package com.imagination.pg_metadata

case class TableDTO(name: String,
                    column: Seq[ColumnDTO],
                    unique_indexes: Option[Seq[UniqueIndexDTO]],
                    indexes: Option[Seq[IndexDTO]],
                    foreign_keys: Option[Seq[ForeignKeyDTO]],
                    checks: Option[Seq[CheckDTO]])

case class ColumnDTO(name: String,
                     primary: Boolean,
                     nullable: Boolean,
                     column_type: ColumnType,
                     has_sequences: Boolean,
                     column_default: Option[String])

sealed trait ColumnType {
  def column_type: String
}

sealed trait NumberLike extends ColumnType {
  def numeric_precision: Option[Int]
  def numeric_precision_radix: Option[Int]
  def numeric_scale: Option[Int]
}

case class IntLike(column_type: String,
                   numeric_precision: Option[Int],
                   numeric_precision_radix: Option[Int],
                   numeric_scale: Option[Int]) extends NumberLike

case class DoubleLike(column_type: String,
                      numeric_precision: Option[Int],
                      numeric_precision_radix: Option[Int],
                      numeric_scale: Option[Int]) extends NumberLike

case class StringLike(column_type: String,
                      character_maximum_length: Option[Int]) extends ColumnType

case class TimeLike(column_type: String,
                    datetime_precision: Option[Int]) extends ColumnType

case class PGUuid(column_type: String) extends ColumnType
case class PGBoolean(column_type: String) extends ColumnType
case class PGByteArray(column_type: String) extends ColumnType
case class PGOther(column_type: String) extends ColumnType


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

sealed abstract class CascadeOp {
  def action: String
}
case class NoAction(action:String) extends CascadeOp
case class SetDefault(action:String) extends CascadeOp
case class SetNull(action:String) extends CascadeOp
case class Restrict(action:String) extends CascadeOp
case class Cascade(action:String) extends CascadeOp

case class CheckDTO(name: String)