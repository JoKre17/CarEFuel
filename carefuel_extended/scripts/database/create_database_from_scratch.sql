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

CREATE INDEX index_on_stid
    ON public.gas_station_information_history USING btree
    (stid)
    TABLESPACE pg_default;
	
	
	
	
	
-- Table: public.gas_station_information_prediction

CREATE TABLE public.gas_station_information_prediction
(
    id integer NOT NULL,
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

CREATE INDEX index_on_date_prediction
    ON public.gas_station_information_prediction USING btree
    (date)
    TABLESPACE pg_default;

-- Index: index_on_date_stid

-- DROP INDEX public.index_on_date_stid;

CREATE INDEX index_on_date_stid
    ON public.gas_station_information_prediction USING btree
    (stid)
    TABLESPACE pg_default;
	
	

	
	
	
	
	
-- Table: public.distances

-- DROP TABLE public.distances;

CREATE TABLE public.distances
(
    id_1 uuid NOT NULL,
    id_2 uuid NOT NULL,
    distance double precision,
    CONSTRAINT distances_pkey PRIMARY KEY (id_1, id_2),
    CONSTRAINT distances_id_1_fkey FOREIGN KEY (id_1)
        REFERENCES public.gas_station (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT distances_id_2_fkey FOREIGN KEY (id_2)
        REFERENCES public.gas_station (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
)
WITH (
    OIDS = FALSE
)
TABLESPACE pg_default;

ALTER TABLE public.distances
    OWNER to postgres;

-- Index: index_on_distance

-- DROP INDEX public.index_on_distance;

CREATE INDEX index_on_distance
    ON public.distances USING btree
    (distance)
    TABLESPACE pg_default;

-- Index: index_on_id_1

-- DROP INDEX public.index_on_id_1;

CREATE INDEX index_on_id_1
    ON public.distances USING btree
    (id_1)
    TABLESPACE pg_default;

-- Index: index_on_id_2

-- DROP INDEX public.index_on_id_2;

CREATE INDEX index_on_id_2
    ON public.distances USING btree
    (id_2)
    TABLESPACE pg_default;
	

	
	
	
	
-- FUNCTION: public.calculate_distances()

-- DROP FUNCTION public.calculate_distances();

--CREATE FUNCTION public.calculate_distances()
--    RETURNS trigger
--    LANGUAGE 'plpgsql'
--    COST 100
--    VOLATILE NOT LEAKPROOF
--AS $BODY$
--DECLARE
--  dest record;

--BEGIN
--  FOR dest IN (SELECT * FROM gas_station) LOOP
--    IF NEW.id <> dest.id THEN
--      IF (TG_OP = 'INSERT') THEN
--        INSERT INTO distances SELECT NEW.id, dest.id, 6378.388* acos(sin(NEW.lat) * sin(dest.lat) + cos(NEW.lat)
--						* cos(dest.lat) * cos(dest.lng - NEW.lng));
--      END IF;
--      IF (TG_OP = 'UPDATE') THEN
--        UPDATE distances SET distance = 6378.388* acos(sin(NEW.lat) * sin(dest.lat) + cos(NEW.lat)
--						* cos(dest.lat) * cos(dest.lng - NEW.lng)) WHERE (id_1 = NEW.id AND id_2 = dest.id) OR (id_1 = dest.id AND id_2 = NEW.id);
--      END IF;
--    END IF;
--  END LOOP;
--  RETURN NULL;
--END
--$BODY$;

--ALTER FUNCTION public.calculate_distances()
--    OWNER TO postgres;
	

-- Trigger: update_distances

-- DROP TRIGGER update_distances ON public.gas_station;

--CREATE TRIGGER update_distances
--    AFTER INSERT OR UPDATE 
--    ON public.gas_station
--    FOR EACH ROW
--    EXECUTE PROCEDURE public.calculate_distances();
	
	
	
	
	
	
	
-- FUNCTION: public.set_distances()

-- DROP FUNCTION public.set_distances();

CREATE OR REPLACE FUNCTION public.set_distances(
	)
    RETURNS boolean
    LANGUAGE 'plpgsql'

    COST 100
    VOLATILE
AS $BODY$
DECLARE
	g1 RECORD;
    g2 RECORD;
BEGIN
    FOR g1 IN SELECT * FROM gas_station
    LOOP
    	FOR g2 IN SELECT * FROM gas_station
        LOOP
        	IF g1 <> g2 THEN
            	INSERT INTO distances
                	(id_1, id_2, distance)
                VALUES
                	(g1.id, g2.id, 
                    	6378.388* acos(
                          sin(
                            radians(g1.lat)
                          ) * sin(
                            radians(g2.lat)
                          ) + cos(
                            radians(g1.lat)
                          ) * cos(
                            radians(g2.lat)
                          ) * cos(
                            radians(g2.lng) - radians(g1.lng)
                          ))
                    );
            END IF;
        END LOOP;
    END LOOP;
    RETURN TRUE;
END;

$BODY$;

ALTER FUNCTION public.set_distances()
    OWNER TO postgres;


