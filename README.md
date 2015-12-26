[![Build Status](https://travis-ci.org/ximagination80/pg_metadata.svg?branch=master)](https://travis-ci.org/ximagination80/pg_metadata)
[![codecov.io](https://codecov.io/github/ximagination80/pg_metadata/coverage.svg?branch=master)](https://codecov.io/github/ximagination80/pg_metadata?branch=master)
[![License](http://img.shields.io/:license-Apache%202-red.svg)](http://www.apache.org/licenses/LICENSE-2.0.txt)

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

# Installation

```scala
 Installing required dependencies from GitHub..

 git clone https://github.com/ximagination80/Comparator.git
 cd Comparator
 sbt test
 sbt publishLocal

```


