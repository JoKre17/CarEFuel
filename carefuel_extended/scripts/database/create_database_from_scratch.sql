-- create database with standard UTF-8
CREATE DATABASE "carefuel"
  WITH OWNER "postgres"
  ENCODING 'UTF8'
  TABLESPACE  pg_default
  CONNECTION LIMIT = -1;


\c "carefuel";



-- Create all tables

CREATE TABLE public.gas_station
(
    id uuid NOT NULL,
    version character varying(10) COLLATE pg_catalog."default" NOT NULL,
    version_time timestamp without time zone NOT NULL,
    name text COLLATE pg_catalog."default" NOT NULL,
    brand text COLLATE pg_catalog."default",
    street text COLLATE pg_catalog."default",
    house_number text COLLATE pg_catalog."default",
    post_code text COLLATE pg_catalog."default",
    place text COLLATE pg_catalog."default",
    public_holiday_identifier text COLLATE pg_catalog."default",
    lat double precision NOT NULL,
    lng double precision NOT NULL,
    price_in_import timestamp(0) with time zone NOT NULL DEFAULT '1970-01-01 00:00:00+01'::timestamp with time zone,
    price_changed timestamp(0) with time zone DEFAULT '1970-01-01 00:00:00+01'::timestamp with time zone,
    open_ts integer NOT NULL DEFAULT 1,
    ot_json text COLLATE pg_catalog."default" NOT NULL DEFAULT '{}'::text,
    station_in_import timestamp(0) with time zone NOT NULL DEFAULT now(),
    first_active timestamp(0) with time zone NOT NULL DEFAULT '1970-01-01 01:00:00+01'::timestamp with time zone,
    city character varying(255) COLLATE pg_catalog."default",
    CONSTRAINT gas_station_pkey PRIMARY KEY (id)
)
WITH (
    OIDS = FALSE
)
TABLESPACE pg_default;

ALTER TABLE public.gas_station
    OWNER to postgres;

-- Index: idx_brand

-- DROP INDEX public.idx_brand;

CREATE INDEX idx_brand
    ON public.gas_station USING btree
    (brand COLLATE pg_catalog."default")
    TABLESPACE pg_default;

-- Index: idx_gas_station_post_code

-- DROP INDEX public.idx_gas_station_post_code;

CREATE INDEX idx_gas_station_post_code
    ON public.gas_station USING btree
    (post_code COLLATE pg_catalog."default")
    TABLESPACE pg_default;

-- Index: idx_lat

-- DROP INDEX public.idx_lat;

CREATE INDEX idx_lat
    ON public.gas_station USING btree
    (lat)
    TABLESPACE pg_default;

-- Index: idx_lng

-- DROP INDEX public.idx_lng;

CREATE INDEX idx_lng
    ON public.gas_station USING btree
    (lng)
    TABLESPACE pg_default;

-- Index: idx_open_ts

-- DROP INDEX public.idx_open_ts;

CREATE INDEX idx_open_ts
    ON public.gas_station USING btree
    (open_ts)
    TABLESPACE pg_default;

-- Index: idx_updated

-- DROP INDEX public.idx_updated;

CREATE INDEX idx_updated
    ON public.gas_station USING btree
    (price_in_import)
    TABLESPACE pg_default;
	
	
CREATE INDEX index_on_gas_station_id
    ON public.gas_station USING btree
    (id)
    TABLESPACE pg_default;
	
	
	
	
-- Table: public.gas_station_information_history


CREATE TABLE public.gas_station_information_history
(
    id integer NOT NULL,
    stid uuid NOT NULL,
    e5 smallint,
    e10 smallint,
    diesel smallint,
    date timestamp(0) with time zone NOT NULL,
    changed smallint,
    gasstation_id uuid,
    CONSTRAINT fkmm24tuqmaf1tmo0bro53awhrm FOREIGN KEY (gasstation_id)
        REFERENCES public.gas_station (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
)
WITH (
    OIDS = FALSE
)
TABLESPACE pg_default;

ALTER TABLE public.gas_station_information_history
    OWNER to postgres;

-- Index: index_on_stid

-- DROP INDEX public.index_on_stid;

CREATE INDEX index_on_history_stid
    ON public.gas_station_information_history USING btree
    (stid)
    TABLESPACE pg_default;
	
CREATE INDEX index_on_history_date
    ON public.gas_station_information_history USING btree
    (date)
    TABLESPACE pg_default;	
	
	
	
-- Table: public.gas_station_information_prediction

CREATE TABLE public.gas_station_information_prediction
(
    id SERIAL PRIMARY KEY,
    stid uuid NOT NULL,
    e5 smallint,
    e10 smallint,
    diesel smallint,
    date timestamp(0) with time zone,
    CONSTRAINT gas_station_has_gas_station_information_predictions FOREIGN KEY (stid)
        REFERENCES public.gas_station (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
)
WITH (
    OIDS = FALSE
)
TABLESPACE pg_default;

ALTER TABLE public.gas_station_information_prediction
    OWNER to postgres;

-- Index: index_on_date_prediction

-- DROP INDEX public.index_on_date_prediction;

CREATE INDEX index_on_prediction_date
    ON public.gas_station_information_prediction USING btree
    (date)
    TABLESPACE pg_default;

-- Index: index_on_date_stid

-- DROP INDEX public.index_on_date_stid;

CREATE INDEX index_on_prediction_stid
    ON public.gas_station_information_prediction USING btree
    (stid)
    TABLESPACE pg_default;
	



CREATE OR REPLACE FUNCTION delete_gas_stations_with_insufficient_price_data() RETURNS void AS $$
	BEGIN
		CREATE TEMP TABLE insufficient_station AS (
			WITH border_date as (SELECT (max(date) - interval '30 days') max_date FROM gas_station_information_history)
			SELECT stid FROM (
				SELECT stid, COUNT(1) FROM gas_station_information_history
				WHERE date > (SELECT max_date FROM border_date)
				GROUP BY stid
				HAVING COUNT(1) < 31
			) as sub
		);
		
		ALTER TABLE gas_station_information_history DISABLE TRIGGER ALL;
        ALTER TABLE gas_station_information_prediction DISABLE TRIGGER ALL;
        ALTER TABLE gas_station DISABLE TRIGGER ALL;
		RAISE NOTICE 'Performing delete on gas_station_information_history at %', current_timestamp;
		DELETE FROM gas_station_information_history gsih WHERE gsih.stid = ANY(SELECT * FROM insufficient_station);
		RAISE NOTICE 'Performing delete on gas_station_information_prediction at %', current_timestamp;
		DELETE FROM gas_station_information_prediction gsip WHERE gsip.stid = ANY(SELECT * FROM insufficient_station);
		RAISE NOTICE 'Performing delete on gas_station at %', current_timestamp;
		DELETE FROM gas_station gs WHERE gs.id = ANY(SELECT * FROM insufficient_station);
		RAISE NOTICE '--------- finished at % ---------', current_timestamp;
        ALTER TABLE gas_station_information_history ENABLE TRIGGER ALL;
        ALTER TABLE gas_station_information_prediction ENABLE TRIGGER ALL;
        ALTER TABLE gas_station ENABLE TRIGGER ALL;
	END;
$$ LANGUAGE plpgsql;

