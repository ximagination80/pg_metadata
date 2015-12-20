CREATE TABLE t_driver (
  id            SERIAL PRIMARY KEY,
  name          VARCHAR(64)                         NOT NULL,
  email         VARCHAR(320)                        NOT NULL CHECK ( email ~*
                                                                     '^[A-Za-z0-9._%-]+@[A-Za-z0-9.-]+[.][A-Za-z]+$' ),

  age           SMALLINT,
  date_of_birth TIMESTAMP                           NOT NULL,
  salary        NUMERIC(10, 2) DEFAULT 1000 :: NUMERIC,
  description   TEXT,
  sex           CHAR(1) NOT NULL CHECK (sex = 'm' OR sex = 'f'),
  created_date  TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
  active        BOOL DEFAULT TRUE,

  CONSTRAINT idx_t_driver_email_unique UNIQUE (email)
);

CREATE TABLE t_bus_type (
  id            BIGSERIAL PRIMARY KEY,
  name          TEXT NOT NULL DEFAULT ''::TEXT
);

CREATE TABLE t_bus (
  id          BIGSERIAL PRIMARY KEY,
  name        TEXT,
  bus_type_id BIGINT NOT NULL,

  CONSTRAINT fk_t_bus_type_id FOREIGN KEY (bus_type_id) REFERENCES t_bus_type (id) ON DELETE RESTRICT ON UPDATE SET NULL
);

CREATE TABLE t_driver_bus (
  t_driver_id BIGINT,
  t_bus_id BIGINT,

  PRIMARY KEY (t_driver_id, t_bus_id),

  CONSTRAINT fk_t_driver_id FOREIGN KEY (t_driver_id) REFERENCES t_driver (id) ON DELETE SET DEFAULT ON UPDATE NO ACTION ,
  CONSTRAINT fk_t_bus_id FOREIGN KEY (t_bus_id) REFERENCES t_bus (id) ON DELETE CASCADE ON UPDATE SET NULL
);