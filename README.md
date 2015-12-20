# Postgres metadata collector
# How to run

```scala

--host localhost
--port 5432
--user postgres 
--password postgres 
--schema postgres
--debug true

```

#Result:

* Tables
* Columns + limitations
* ForeignKeys + cascade operations
* Unique Indexes
* Indexes
* Constraints (check)

# Installation

```scala
 Installing required dependencies from GitHub..

 git clone https://github.com/ximagination80/Comparator.git
 cd Comparator
 sbt test
 sbt publishLocal

```


