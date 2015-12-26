package core

import java.sql.Connection

import anorm.Macro.namedParser
import anorm._
import console.AppSettings

case class PGMetadataCollector(schema: String)(implicit connection: Connection, stg: AppSettings) {

  implicit val columnToYesNoBoolean: Column[YesNoBoolean] = Column.nonNull[YesNoBoolean] { (value, meta) =>
    value match {
      case s: String =>
        Right(YesNoBoolean(s))
      case _ =>
        val MetaDataItem(qualified, _, _) = meta
        val msg =
           s"""Cannot convert $value: ${value.asInstanceOf[AnyRef].getClass}
             to YesNoBoolean for column $qualified
            """
        Left(TypeDoesNotMatch(msg))
    }
  }

  trait Service {
    type Content

    def description: String
    def parser: RowParser[Content]
    def sql: String

    def execute(): Seq[Content] = {
      stg.log(description)
      stg.log(s"SQL: $sql")
      try SQL(sql).as(parser.*) catch {
        case e: Exception =>
          stg.log("ERROR")
          throw e
      }
    }
  }

  case class Table(name: String)
  case class TablesService() extends Service {
    type Content = Table

    val parser = namedParser[Table]
    val description = "list of all tables"

    val sql =
      s"""
      SELECT table_name as name
      FROM information_schema.tables
      WHERE table_type = 'BASE TABLE'
      AND table_name != 'play_evolutions'
      AND table_name != 'schema_version'
      AND table_schema='$schema';
      """
  }

  case class YesNoBoolean(value: String) {
    def toBoolean = "YES".equalsIgnoreCase(value)
  }

  case class TableInfo(table_name: String,
                       column_name: String,
                       is_nullable: YesNoBoolean,
                       data_type: String,
                       column_default:Option[String],
                       character_maximum_length:Option[Int],
                       character_octet_length:Option[Int],
                       numeric_precision:Option[Int],
                       numeric_precision_radix:Option[Int],
                       numeric_scale:Option[Int],
                       datetime_precision:Option[Int])

  case class TablesInfoService(tables:Seq[String]) extends Service {
    type Content = TableInfo

    val parser = namedParser[TableInfo]
    val description = s"""all column names, types etc for tables:
        ${tables.mkString(",")}"""

    def sql = s"""
     SELECT s.table_name,
            s.column_name,
            s.is_nullable,
            s.data_type,
            s.column_default,
            s.character_maximum_length,
            s.character_octet_length,
            s.numeric_precision,
            s.numeric_precision_radix,
            s.numeric_scale,
            s.datetime_precision
     FROM information_schema.columns s
     WHERE s.table_name IN (${tables.map("'"+_+"'").mkString(",")})
     ORDER BY s.table_name,s.ordinal_position;
              """
  }

  case class PKName(pk_name:String)
  case class PrimaryKeyService(tableName:String) extends Service {
    type Content = PKName

    val parser = namedParser[PKName]
    val description = s"primary key for table $tableName"

    val sql =
      s"""
      SELECT a.attname as pk_name
      FROM   pg_index i
      JOIN   pg_attribute a ON a.attrelid = i.indrelid
                           AND a.attnum = ANY(i.indkey)
      WHERE  i.indrelid = '$tableName'::regclass
      AND    i.indisprimary;
      """
  }

  case class Sequence(name:String)
  case class SequenceService() extends Service {
    type Content = Sequence

    val parser = namedParser[Sequence]
    val description = s"list of all sequences"

    val sql =
      """
      SELECT relname as name
      FROM pg_class
      WHERE relkind = 'S'
      AND relnamespace IN (
      SELECT oid
      FROM pg_namespace
      WHERE nspname NOT LIKE 'pg_%'
      AND nspname != 'information_schema'
      );
      """
  }

  case class UniqueIndexInfo(relname: String, indkey: String)
  case class UniqueIndexInfoService(table:String) extends Service {
    type Content = UniqueIndexInfo

    val parser = namedParser[UniqueIndexInfo]
    val description = s"unique indexes in $table"

    val sql =
      s"""
      SELECT relname,cast(indkey as text)
      FROM pg_class, pg_index
      WHERE pg_class.oid = pg_index.indexrelid
      AND pg_class.oid IN (
      SELECT indexrelid
      FROM pg_index, pg_class
      WHERE pg_class.relname='$table'
      AND pg_class.oid=pg_index.indrelid
      AND indisunique = 't' AND indisprimary = 'f'
      );
      """
  }

  case class UniqueIndex(idxName: String, columns: Seq[String])
  case class UniqueIndexColumnNames(name: String)
  case class UniqueIndexService(table:String,names:Seq[Int]) extends Service {
    type Content = UniqueIndexColumnNames

    val parser = namedParser[UniqueIndexColumnNames]
    val description =
      s"unique column names in $table for column ids ${names.mkString(",")}}"

    val sql =
      s"""
      SELECT DISTINCT a.attname as name
      FROM pg_index c
      LEFT JOIN pg_class t ON c.indrelid  = t.oid
      LEFT JOIN pg_attribute a ON a.attrelid = t.oid
      AND a.attnum = ANY(indkey)
      WHERE t.relname = '$table'
      AND a.attnum IN (${names.mkString(",")});
      """
  }

  case class ForeignKeyConstraintInfo(constraint_name: String,
                            table_name: String,
                            column_name: String,
                            references_table: String,
                            references_field: String,
                            update_rule:String,
                            delete_rule:String)
  case class ForeignKeyConstraintService(names:Seq[String]) extends Service{
    type Content = ForeignKeyConstraintInfo

    val parser = namedParser[ForeignKeyConstraintInfo]
    val description = s"""all constraints for tables:
         ${names.mkString(",")}}"""

    val sql =
     s"""
      SELECT
      tc.constraint_name,
      tc.constraint_type,
      tc.table_name,
      kcu.column_name,
      ccu.table_name AS references_table,
      ccu.column_name AS references_field,
      rc.update_rule,
      rc.delete_rule

      FROM information_schema.table_constraints tc
      LEFT JOIN information_schema.key_column_usage kcu
      ON tc.constraint_catalog = kcu.constraint_catalog
      AND tc.constraint_schema = kcu.constraint_schema
      AND tc.constraint_name = kcu.constraint_name
      LEFT JOIN information_schema.referential_constraints rc
      ON tc.constraint_catalog = rc.constraint_catalog
      AND tc.constraint_schema = rc.constraint_schema
      AND tc.constraint_name = rc.constraint_name
      LEFT JOIN information_schema.constraint_column_usage ccu
      ON rc.unique_constraint_catalog = ccu.constraint_catalog
      AND rc.unique_constraint_schema = ccu.constraint_schema
      AND rc.unique_constraint_name = ccu.constraint_name
      WHERE tc.table_name IN (${names.map("'"+_+"'").mkString(",")})
      AND tc.constraint_type='FOREIGN KEY'
      """
  }

  case class CheckConstraintInfo(constraint_name: String, table_name: String)
  case class CheckConstraintService(names:Seq[String]) extends Service{
    type Content = CheckConstraintInfo

    val parser = namedParser[CheckConstraintInfo]
    val description = s"""all check for tables:
         ${names.mkString(",")}}"""

    val sql =
      s"""
      SELECT
      tc.constraint_name,
      tc.table_name

      FROM information_schema.table_constraints tc
      LEFT JOIN information_schema.key_column_usage kcu
      ON tc.constraint_catalog = kcu.constraint_catalog
      AND tc.constraint_schema = kcu.constraint_schema
      AND tc.constraint_name = kcu.constraint_name
      LEFT JOIN information_schema.referential_constraints rc
      ON tc.constraint_catalog = rc.constraint_catalog
      AND tc.constraint_schema = rc.constraint_schema
      AND tc.constraint_name = rc.constraint_name
      LEFT JOIN information_schema.constraint_column_usage ccu
      ON rc.unique_constraint_catalog = ccu.constraint_catalog
      AND rc.unique_constraint_schema = ccu.constraint_schema
      AND rc.unique_constraint_name = ccu.constraint_name
      WHERE tc.table_name IN (${names.map("'"+_+"'").mkString(",")})
      AND tc.constraint_type='CHECK'
      """
  }

  def collect():Seq[TableDTO] = {
    stg.log("Reading schema...")
    
    val tableNames = TablesService().execute()
    if (tableNames.isEmpty) {
      stg.log(s"Tables not found")

      Nil
    } else {
      stg.log(s"Found ${tableNames.length} tables.\n")

      val names = tableNames.map(_.name)
      val tablesInfo = TablesInfoService(names).execute().groupBy(_.table_name)
      val sequences = SequenceService().execute()
      val fkConstraints = ForeignKeyConstraintService(names).execute()
      val checkConstraints = CheckConstraintService(names).execute()

      val tables = mergeAdditionalInfo(tablesInfo.map(e => {
        val (tableName, columns) = e

        val pkNames = PrimaryKeyService(tableName).execute()

        val columnDTOs = columns.map(c => {
          ColumnDTO(c.column_name,
            primary = pkNames.exists(_.pk_name == c.column_name),
            nullable = c.is_nullable.toBoolean,
            c.data_type,
            hasSequences = sequences.map(_.name).contains(s"${tableName}_${c.column_name}_seq"),
            c.column_default,
            c.character_maximum_length,
            c.character_octet_length,
            c.numeric_precision,
            c.numeric_precision_radix,
            c.numeric_scale,
            c.datetime_precision)
        })

        val uniqueIndexDTOs = UniqueIndexInfoService(tableName).execute().map(toIndex(tableName, _)).
          map(associateColumnsWithIndexes(_, columnDTOs))

        TableDTO(tableName, columnDTOs, wrap(uniqueIndexDTOs), None, None)
      }).toSeq, fkConstraints, checkConstraints)

      tables
    }
  }

  def wrap[T](seq:Seq[T]):Option[Seq[T]]=
    if (seq.nonEmpty) Some(seq) else None

  def mergeAdditionalInfo(tables: Seq[TableDTO],
                          fkConstraints: Seq[ForeignKeyConstraintInfo],
                          checkConstraints: Seq[CheckConstraintInfo]) = {
    val fk = fkConstraints.groupBy(_.table_name)
    val checks = checkConstraints.filterNot(_.constraint_name.contains("not_null")).groupBy(_.table_name)

    tables.map(e => {
      val producedFk = fk.getOrElse(e.name, Nil).map(fk => {
        val toTable = tables.find(_.name == fk.references_table).
          getOrElse(throw new RuntimeException(s"Unable to find table by key ${fk.references_table}"))

        ForeignKeyDTO(
          fk.constraint_name,
          e.column.find(_.name == fk.column_name).get,
          toTable,
          toTable.column.find(_.name == fk.references_field).get,
          fk.update_rule,
          fk.delete_rule)
      })

      val producedChecks= checks.getOrElse(e.name, Nil).
        sortBy(_.constraint_name).map(ch => CheckDTO(ch.constraint_name))

      e.copy(foreignKeys = wrap(producedFk), checks = wrap(producedChecks))
    })
  }

  def associateColumnsWithIndexes(idx: UniqueIndex, info: Seq[ColumnDTO]) =
    UniqueIndexDTO(idx.idxName, idx.columns.map({ clm =>
      info.find(_.name == clm).get
    }))

  def toIndex(name: String, e: UniqueIndexInfo) = {
    val seq = UniqueIndexService(name,e.indkey.trim.split(" ").map(_.toInt)).execute()
    UniqueIndex(e.relname, seq.map(_.name))
  }
}
