package core

case class TableDTO(name: String,
                    column: Seq[ColumnDTO],
                    uniqueIndexes: Option[Seq[UniqueIndexDTO]],
                    indexes: Option[Seq[IndexDTO]],
                    foreignKeys: Option[Seq[ForeignKeyDTO]],
                    checks: Option[Seq[CheckDTO]])

case class ColumnDTO(name: String,
                     primary: Boolean,
                     nullable: Boolean,
                     dbType: String,
                     hasSequences: Boolean,
                     column_default: Option[String],
                     character_maximum_length: Option[Int],
                     character_octet_length: Option[Int],
                     numeric_precision: Option[Int],
                     numeric_precision_radix: Option[Int],
                     numeric_scale: Option[Int],
                     datetime_precision: Option[Int])

case class UniqueIndexDTO(name: String,
                          columns: Seq[ColumnDTO])

case class IndexDTO(name: String,
                    columns: Seq[ColumnDTO])

case class ForeignKeyDTO(name: String,
                         column: ColumnDTO,
                         references_to_table: TableDTO,
                         references_to_column: ColumnDTO,
                         action_on_update: CascadeOp,
                         action_on_delete: CascadeOp)

case class CheckDTO(name: String)

sealed trait CascadeOp {
  def action: String
}
case class NO_ACTION(action:String) extends CascadeOp
case class SET_DEFAULT(action:String) extends CascadeOp
case class SET_NULL(action:String) extends CascadeOp
case class RESTRICT(action:String) extends CascadeOp
case class CASCADE(action:String) extends CascadeOp