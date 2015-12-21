CREATE TYPE mood AS ENUM ('sad', 'ok', 'happy');

CREATE TABLE t_random_fields (
  field_1 smallint,
  field_2 integer,
  field_3 bigint,

  field_4 decimal,
  field_5 decimal(10),
  field_6 decimal(10,2),
  field_7 numeric,
  field_8 real,
  field_9 double precision,

  field_10 smallserial,
  field_11 serial,
  field_12 bigserial,

  field_13 character varying(10),
  field_14 varchar(10),

  field_15 character(10),
  field_16 char(10),
  field_17 text,
  field_18 char,

  field_19 money,
  field_20 bytea,

  field_21 timestamp,
  field_22 timestamp without time zone,
  field_23 date,
  field_24 time,
  field_25 time without time zone,
  field_26 interval,

  field_27 boolean,
  field_28 mood,

  field_29 point,
  field_30 line,
  field_31 lseg,
  field_32 box,
  field_33 path,
  field_34 polygon,
  field_35 circle,

  field_36 cidr,
  field_37 inet,
  field_38 macaddr,
  field_39 uuid,
  field_40 xml,
  field_41 json,
  field_42 integer[3][3],
  field_43 text[3],
  field_44 int4range,
  field_45 int8range,
  field_46 numrange,
  field_47 tsrange,
  field_48 tstzrange,
  field_49 daterange
);