\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

CREATE TABLE iris._gps
(
  name character varying(24) NOT NULL,
  gps_enable boolean NOT NULL DEFAULT false,
  device_name character varying(20) NOT NULL,
  device_class character varying(20) NOT NULL,
  poll_datetime timestamp with time zone,
  sample_datetime timestamp with time zone,
  sample_lat double precision DEFAULT 0.0,
  sample_lon double precision DEFAULT 0.0,
  comm_status character varying(25),
  error_status character varying(50),
  jitter_tolerance_meters smallint DEFAULT 0,
  CONSTRAINT name PRIMARY KEY (name),
  CONSTRAINT geo_loc_name FOREIGN KEY (device_name)
      REFERENCES iris.geo_loc (name) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE FUNCTION iris.gps_insert()
  RETURNS trigger AS
$gps_insert$
BEGIN
	INSERT INTO iris._gps (name, gps_enable, 
				device_name, device_class,
				sample_datetime,
				sample_lat, sample_lon,
				comm_status, error_status,
				jitter_tolerance_meters)
	     VALUES (NEW.name, NEW.gps_enable,
				NEW.device_name, NEW.device_class,
				NEW.sample_datetime,
				NEW.sample_lat, NEW.sample_lon, 
				NEW.comm_status, NEW.error_status,
				NEW.jitter_tolerance_meters);
	INSERT INTO iris._device_io (name, controller, pin)
	     VALUES (NEW.name, NEW.controller, 0);
	RETURN NEW;
END;
$gps_insert$ LANGUAGE plpgsql;


CREATE FUNCTION iris.gps_update()
  RETURNS trigger AS
$gps_update$
BEGIN
        UPDATE iris._device_io
           SET controller = NEW.controller,
               pin = NEW.pin
         WHERE name = OLD.name;
	UPDATE iris._gps 
	   SET gps_enable = NEW.gps_enable,
	       device_name = NEW.device_name,
	       device_class = NEW.device_class,
	       sample_datetime = NEW.sample_datetime,
	       sample_lat = NEW.sample_lat,
	       sample_lon = NEW.sample_lon,
	       comm_status = NEW.comm_status,
	       error_status = NEW.error_status,
	       jitter_tolerance_meters = NEW.jitter_tolerance_meters
	 WHERE name = OLD.name;
	RETURN NEW;
END;
$gps_update$ LANGUAGE plpgsql;

CREATE VIEW iris.gps AS 
 SELECT g.name,
    d.controller,
    d.pin,
    g.gps_enable,
    g.device_name,
    g.device_class,
    g.poll_datetime,
    g.sample_datetime,
    g.sample_lat,
    g.sample_lon,
    g.comm_status,
    g.error_status,
    g.jitter_tolerance_meters
   FROM iris._gps g
     JOIN iris._device_io d ON g.name = d.name;

CREATE TRIGGER gps_insert_trig
  INSTEAD OF INSERT
  ON iris.gps
  FOR EACH ROW
  EXECUTE PROCEDURE iris.gps_insert();

CREATE TRIGGER gps_update_trig
  INSTEAD OF UPDATE
  ON iris.gps
  FOR EACH ROW
  EXECUTE PROCEDURE iris.gps_update();
  
INSERT INTO iris.system_attribute (name, value) VALUES ('comm_idle_disconnect_gps_sec', '5');

