[![Build Status](https://travis-ci.org/ximagination80/pg_metadata.svg?branch=master)](https://travis-ci.org/ximagination80/pg_metadata)
[![codecov.io](https://codecov.io/github/ximagination80/pg_metadata/coverage.svg?branch=master)](https://codecov.io/github/ximagination80/pg_metadata?branch=master)
[![License](http://img.shields.io/:license-Apache%202-red.svg)](http://www.apache.org/licenses/LICENSE-2.0.txt)
[![Join the chat at https://gitter.im/ximagination80/pg_metadata](https://badges.gitter.im/ximagination80/pg_metadata.svg)](https://gitter.im/ximagination80/pg_metadata?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

# Postgres metadata collector
# How to run

```scala

--host localhost
--port 5432
--user postgres 
--password postgres
--database postgres
--schema public
--debug true

```

#Result:

* Tables
* Columns + limitations
* ForeignKeys + cascade operations
* Unique Indexes
* Indexes
* Constraints (check)

#Model
```scala

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
                         action_on_update: String,
                         action_on_delete: String)

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


