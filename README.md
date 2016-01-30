[![Build Status](https://travis-ci.org/ximagination80/pg_metadata.svg?branch=master)](https://travis-ci.org/ximagination80/pg_metadata)
[![codecov.io](https://codecov.io/github/ximagination80/pg_metadata/coverage.svg?branch=master)](https://codecov.io/github/ximagination80/pg_metadata?branch=master)
[![License](http://img.shields.io/:license-Apache%202-red.svg)](http://www.apache.org/licenses/LICENSE-2.0.txt)
[![Join the chat at https://gitter.im/ximagination80/pg_metadata](https://badges.gitter.im/ximagination80/pg_metadata.svg)](https://gitter.im/ximagination80/pg_metadata?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

# Postgres metadata collector
# Model

```scala
package core

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

case class IntLike(db_type: String,
                   numeric_precision: Option[Int],
                   numeric_precision_radix: Option[Int],
                   numeric_scale: Option[Int]) extends NumberLike

case class DoubleLike(db_type: String,
                      numeric_precision: Option[Int],
                      numeric_precision_radix: Option[Int],
                      numeric_scale: Option[Int]) extends NumberLike

case class StringLike(db_type: String,
                      character_maximum_length: Option[Int]) extends ColumnType

case class TimeLike(db_type: String,
                    datetime_precision: Option[Int]) extends ColumnType

case class PGUuid(db_type: String) extends ColumnType
case class PGBoolean(db_type: String) extends ColumnType
case class PGByteArray(db_type: String) extends ColumnType
case class PGOther(db_type: String) extends ColumnType


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
```

# Installation
```scala
 Installing required dependencies from GitHub..

 git clone https://github.com/ximagination80/Comparator.git
 cd Comparator
 sbt test
 sbt publishLocal
```


