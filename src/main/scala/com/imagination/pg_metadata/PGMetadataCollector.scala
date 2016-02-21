package com.imagination.pg_metadata

import java.sql.Connection

import anorm.SqlParser._
import anorm._

case class PGMetadataCollector(schema: String)(implicit c: Connection, logger: Logger = EmptyLogger) {

  implicit val columnToYesNoBoolean: Column[YesNoBoolean] = Column.nonNull[YesNoBoolean] { (value, meta) =>
    (value: @unchecked) match {
      case s: String => Right(YesNoBoolean(s))
    }
  }

  def optInt(key:String): RowParser[Option[Int]] = get[Option[Int]](key)
  def optStr(key:String): RowParser[Option[String]] = get[Option[String]](key)

  trait Service {
    type Content

    def description: String
    def parser: RowParser[Content]
    def sql: String

    def execute(): Seq[Content] = {
      logger.log(description)
      logger.log(s"SQL: $sql")
      SQL(sql).as(parser.*)
    }
  }

  import anorm.SqlParser._

  case class Table(name: String)
  case class TablesService() extends Service {
    type Content = Table

    val parser = str("name") map {
      case name => Table(name)
    }
    val description = "List of all tables"

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

    val parser =
      str("table_name") ~
        str("column_name") ~
        get[YesNoBoolean]("is_nullable") ~
        str("data_type") ~
        optStr("column_default") ~
        optInt("character_maximum_length") ~
        optInt("character_octet_length") ~
        optInt("numeric_precision") ~
        optInt("numeric_precision_radix") ~
        optInt("numeric_scale") ~
        optInt("datetime_precision") map {
        case tn ~ cn ~ nullable ~ dt ~ cd ~ cml ~ col ~ np ~ npr ~ ns ~ dp =>
          TableInfo(tn, cn, nullable, dt, cd, cml, col, np, npr, ns, dp)
    }
    val description = s"""All column names, types etc for tables:
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

    val parser = str("pk_name") map {
      case pk_name => PKName(pk_name)
    }
    val description = s"Primary key for table $tableName"

    val sql =
      s"""
      SELECT a.attname as pk_name
      FROM   pg_index i
      JOIN   pg_attribute a ON a.attrelid = i.indrelid
                           AND a.attnum = ANY(i.indkey)
      WHERE  i.indrelid = '$schema.$tableName'::regclass
      AND    i.indisprimary;
      """
  }

  case class Sequence(name:String)
  case class SequenceService() extends Service {
    type Content = Sequence

    val parser = str("name") map {
      case name => Sequence(name)
    }
    val description = s"List of all sequences"

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

  case class IndexInfo(relname: String, indkey: String)
  
  object IndexInfo {
    
    val parser = str("relname") ~ str("indkey") map {
      case relname ~ indkey => IndexInfo(relname, indkey)
    }

    def indexSQL(table: String, unique: Boolean) = {
      val flag = if (unique) "t" else "f"

      s"""
      SELECT relname,cast(indkey as text)
      FROM pg_class, pg_index
      WHERE pg_class.oid = pg_index.indexrelid
      AND pg_class.oid IN (
      SELECT indexrelid
      FROM pg_index, pg_class
      WHERE pg_class.relname='$table'
      AND pg_class.oid=pg_index.indrelid
      AND indisunique = '$flag' AND indisprimary = 'f'
      );
      """
    }
  }

  case class UniqueIndexInfoService(table: String) extends Service {
    type Content = IndexInfo
    val parser = IndexInfo.parser
    val description = s"Unique indexes in $table"
    val sql = IndexInfo.indexSQL(table, unique = true)
  }

  case class IndexInfoService(table: String) extends Service {
    type Content = IndexInfo
    val parser = IndexInfo.parser
    val description = s"Indexes in $table"
    val sql = IndexInfo.indexSQL(table, unique = false)
  }

  case class Index(idxName: String, columns: Seq[String])
  case class TableColumnName(name: String)
  case class TableNamesService(table:String,names:Seq[Int]) extends Service {
    type Content = TableColumnName

    val parser = str("name") map {
      case name => TableColumnName(name)
    }
    val description =
      s"Unique column names in $table for column ids ${names.mkString(",")}}"

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

    val parser =
      str("constraint_name") ~
        str("table_name") ~
        str("column_name") ~
        str("references_table") ~
        str("references_field") ~
        str("update_rule") ~
        str("delete_rule") map {
        case cont_name ~ tn ~ cn ~ rt ~ rf ~ ur ~ dr => ForeignKeyConstraintInfo(cont_name, tn, cn, rt, rf, ur, dr)
      }
    val description = s"""All constraints for tables:
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

    val parser = str("constraint_name") ~ str("table_name") map {
      case constraint_name ~ table_name => CheckConstraintInfo(constraint_name, table_name)
    }
    val description = s"""All check for tables:
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
    logger.log("Reading schema...")
    
    val tableNames = TablesService().execute()
    if (tableNames.isEmpty) {
      logger.log(s"Tables not found")

      Nil
    } else {
      logger.log(s"Found ${tableNames.length} tables.")

      val names = tableNames.map(_.name)
      val tablesInfo = TablesInfoService(names).execute().groupBy(_.table_name)
      val sequences = SequenceService().execute()
      val fkConstraints = ForeignKeyConstraintService(names).execute()
      val checkConstraints = CheckConstraintService(names).execute()

      val tables = mergeAdditionalInfo(tablesInfo.map(e => {
        val (tn, columns) = e

        val pks = PrimaryKeyService(tn).execute()

        val columnDTOs = columns.map({ c =>
          ColumnDTO(c.column_name,
            primary = pks.exists(_.pk_name == c.column_name),
            nullable = c.is_nullable.toBoolean,
            getColumnType(c),
            has_sequences = sequences.map(_.name).contains(s"${tn}_${c.column_name}_seq"),
            c.column_default)
        })

        val uniqueIndexDTOs = UniqueIndexInfoService(tn).execute().
          map(toIndex(tn, _)).
          map(toUniqueIndexes(_, columnDTOs)).
          sortBy(_.name).wrap

        val indexDTOs = IndexInfoService(tn).execute().
          map(toIndex(tn, _)).
          map(toIndexes(_, columnDTOs)).
          sortBy(_.name).wrap

        TableDTO(
          tn,
          columnDTOs,
          uniqueIndexDTOs,
          indexDTOs,
          None,
          None)
      }).toSeq, fkConstraints, checkConstraints)

      tables.sortBy(_.name)
    }
  }

  def getColumnType(c: TablesInfoService#Content): ColumnType = {
    val `type` = c.data_type
    `type` match {
      case "smallint" | "integer" | "bigint" =>
        IntLike(`type`, c.numeric_precision, c.numeric_precision_radix, c.numeric_scale)

      case "numeric" | "real" | "double precision" =>
        DoubleLike(`type`, c.numeric_precision, c.numeric_precision_radix, c.numeric_scale)

      case "character varying" | "character" | "text" =>
        StringLike(`type`, c.character_maximum_length)

      case "timestamp without time zone" | "time without time zone" | "date" | "interval" |
           "timestamp with time zone" | "time with time zone"=>
        TimeLike(`type`, c.datetime_precision)

      case "bytea" => PGByteArray(`type`)
      case "boolean" => PGBoolean(`type`)
      case "uuid" => PGUuid(`type`)
      case _ => PGOther(`type`)
    }
  }

  implicit class SeqWrapper[T](seq: Seq[T]){
    def wrap: Option[Seq[T]] = if (seq.nonEmpty) Some(seq) else None
  }

  def mergeAdditionalInfo(tables: Seq[TableDTO],
                          fkConstraints: Seq[ForeignKeyConstraintInfo],
                          checkConstraints: Seq[CheckConstraintInfo]) = {
    val fk = fkConstraints.groupBy(_.table_name)
    val checks = checkConstraints.filterNot(_.constraint_name.contains("not_null")).groupBy(_.table_name)

    tables.map({ e =>
      val producedFk = fk.getOrElse(e.name, Nil).map({ fk =>
        val toTable = tables.find(_.name == fk.references_table).
          getOrElse(throw new RuntimeException(s"Unable to find table by key ${fk.references_table}"))

        ForeignKeyDTO(
          fk.constraint_name,
          e.column.find(_.name == fk.column_name).get,
          toTable,
          toTable.column.find(_.name == fk.references_field).get,
          toCascadeOp(fk.update_rule),
          toCascadeOp(fk.delete_rule))
      })

      val producedChecks = checks.getOrElse(e.name, Nil).
        map(ch => CheckDTO(ch.constraint_name))

      e.copy(
        foreign_keys = producedFk.sortBy(_.name).wrap,
        checks = producedChecks.sortBy(_.name).wrap
      )
    })
  }

  def toCascadeOp(value: String): CascadeOp = (value: @unchecked) match {
    case "NO ACTION" => NoAction(value)
    case "SET DEFAULT" => SetDefault(value)
    case "SET NULL" => SetNull(value)
    case "CASCADE" => Cascade(value)
    case "RESTRICT" => Restrict(value)
  }

  def toUniqueIndexes(idx: Index, info: Seq[ColumnDTO]) =
    UniqueIndexDTO(idx.idxName, associateColumns(idx, info))

  def toIndexes(idx: Index, info: Seq[ColumnDTO]) =
    IndexDTO(idx.idxName, associateColumns(idx, info))

  def associateColumns(idx: Index, info: Seq[ColumnDTO]) = idx.columns.map({ clm =>
    info.find(_.name == clm).get
  })
  
  def toIndex(name: String, e: IndexInfo) = {
    val seq = TableNamesService(name, e.indkey.trim.split(" ").map(_.toInt)).execute()
    Index(e.relname, seq.map(_.name))
  }
}