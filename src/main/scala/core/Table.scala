package core

case class TableDTO(name: String,
                    column: Seq[ColumnDTO],
                    uniqueIndexes: Seq[UniqueIndexDTO],
                    foreignKeys: Seq[ForeignKeyDTO],
                    checks: Seq[CheckDTO])

case class ColumnDTO(name: String,
                     primary: Boolean,
                     nullable: Boolean,
                     dbType: String,
                     hasSequences: Boolean,
                     column_default:Option[String],
                     character_maximum_length:Option[Int],
                     character_octet_length:Option[Int],
                     numeric_precision:Option[Int],
                     numeric_precision_radix:Option[Int],
                     numeric_scale:Option[Int],
                     datetime_precision:Option[Int])

case class UniqueIndexDTO(name: String,
                          columns: Seq[ColumnDTO])

case class ForeignKeyDTO(name: String,
                         column: ColumnDTO,
                         references_to_table: TableDTO,
                         references_to_column: ColumnDTO,
                         action_on_update:ForeignKeyAction,
                         action_on_delete:ForeignKeyAction)

case class CheckDTO(name: String)

trait ForeignKeyAction
case object Restrict extends ForeignKeyAction
case object NoAction extends ForeignKeyAction
case object Cascade extends ForeignKeyAction
case object SetNull extends ForeignKeyAction
case object SetDefault extends ForeignKeyAction

object ForeignKeyAction {
  def apply(v: String): ForeignKeyAction = v.toUpperCase match {
    case "RESTRICT" => Restrict
    case "NO ACTION" => NoAction
    case "CASCADE" => Cascade
    case "SET NULL" => SetNull
    case "SET DEFAULT" => SetDefault
  }
}