CREATE TYPE mood AS ENUM ('sad', 'ok', 'happy');

CREATE TABLE t_random_fields (
  field_1 smallint,
  field_2 integer,
  field_3 bigint,
  field_4 smallserial,
  field_5 serial,
  field_6 bigserial,

  field_7 decimal,
  field_8 decimal(10),
  field_9 decimal(10,2),
  field_10 numeric,
  field_11 real,
  field_12 double precision,

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
  field_23 timestamp with time zone,
  field_24 date,
  field_25 time,
  field_26 time without time zone,
  field_27 time with time zone,
  field_28 interval,

  field_29 boolean,
  field_30 mood,

  field_31 point,
  field_32 line,
  field_33 lseg,
  field_34 box,
  field_35 path,
  field_36 polygon,
  field_37 circle,

  field_38 cidr,
  field_39 inet,
  field_40 macaddr,
  field_41 uuid,
  field_42 xml,
  field_43 json,
  field_44 integer[3][3],
  field_45 text[3],
  field_46 int4range,
  field_47 int8range,
  field_48 numrange,
  field_49 tsrange,
  field_50 tstzrange,
  field_51 daterange
);