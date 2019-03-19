\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

BEGIN;

SELECT iris.update_version('4.87.0', '4.88.0');

-- Find duplicate sign_detail records
CREATE TEMP TABLE sign_detail_dups AS
SELECT T1.name AS old_name, T2.name AS new_name
FROM iris.sign_detail T1
JOIN iris.sign_detail T2
  ON T1.ctid < T2.ctid
 AND T1.dms_type = T2.dms_type
 AND T1.portable = T2.portable
 AND T1.technology = T2.technology
 AND T1.sign_access = T2.sign_access
 AND T1.legend = T2.legend
 AND T1.beacon_type = T2.beacon_type
 AND T1.hardware_make = T2.hardware_make
 AND T1.hardware_model = T2.hardware_model
 AND T1.software_make = T2.software_make
 AND T1.software_model = T2.software_model;

-- Remove double duplicate records
DELETE FROM sign_detail_dups T1
      USING sign_detail_dups T2
      WHERE T1.new_name = T2.old_name;

-- Replace duplicate sign_detail on DMS records
UPDATE iris._dms d
   SET sign_detail = n.new_name
  FROM sign_detail_dups n
 WHERE d.sign_detail = n.old_name;

-- Delete duplicate sign_detail records
DELETE FROM iris.sign_detail sd
      USING sign_detail_dups d
      WHERE sd.name = d.old_name;

-- Update notify stuff
DROP TRIGGER geo_loc_notify_trig ON iris.geo_loc;
DROP FUNCTION iris.geo_loc_notify();
DROP TRIGGER camera_notify_trig ON iris._camera;
DROP FUNCTION iris.camera_notify();
DROP TRIGGER font_notify_trig ON iris.font;
DROP TRIGGER glyph_notify_trig ON iris.glyph;
DROP FUNCTION iris.font_notify();
DROP TRIGGER sign_detail_trig ON iris.sign_detail;
DROP FUNCTION iris.sign_detail_notify();
DROP TRIGGER sign_config_trig ON iris.sign_config;
DROP FUNCTION iris.sign_config_notify();
DROP TRIGGER sign_message_notify_trig ON iris.sign_message;
DROP FUNCTION iris.sign_message_notify();
DROP TRIGGER dms_notify_trig ON iris._dms;
DROP FUNCTION iris.dms_notify();
DROP TRIGGER incident_notify_trig ON event.incident;
DROP FUNCTION iris.incident_notify();
DROP TRIGGER parking_area_trig ON iris.parking_area;
DROP FUNCTION iris.parking_area_notify();

CREATE FUNCTION iris.geo_loc_notify() RETURNS TRIGGER AS
	$geo_loc_notify$
BEGIN
	IF (TG_OP = 'DELETE') THEN
		IF (OLD.notify_tag IS NOT NULL) THEN
			PERFORM pg_notify(OLD.notify_tag, OLD.name);
		END IF;
	ELSIF (NEW.notify_tag IS NOT NULL) THEN
		PERFORM pg_notify(NEW.notify_tag, NEW.name);
	END IF;
	RETURN NULL; -- AFTER trigger return is ignored
END;
$geo_loc_notify$ LANGUAGE plpgsql;

CREATE TRIGGER geo_loc_notify_trig
	AFTER INSERT OR UPDATE OR DELETE ON iris.geo_loc
	FOR EACH ROW EXECUTE PROCEDURE iris.geo_loc_notify();

CREATE FUNCTION iris.camera_notify() RETURNS TRIGGER AS
	$camera_notify$
BEGIN
	NOTIFY camera;
	RETURN NULL; -- AFTER trigger return is ignored
END;
$camera_notify$ LANGUAGE plpgsql;

CREATE TRIGGER camera_notify_trig
	AFTER INSERT OR UPDATE OR DELETE ON iris._camera
	FOR EACH STATEMENT EXECUTE PROCEDURE iris.camera_notify();

CREATE FUNCTION iris.table_notify() RETURNS TRIGGER AS
	$table_notify$
BEGIN
	PERFORM pg_notify(TG_TABLE_NAME, '');
	RETURN NULL; -- AFTER trigger return is ignored
END;
$table_notify$ LANGUAGE plpgsql;

CREATE TRIGGER font_notify_trig
	AFTER INSERT OR UPDATE OR DELETE ON iris.font
	FOR EACH STATEMENT EXECUTE PROCEDURE iris.table_notify();

CREATE TRIGGER glyph_notify_trig
	AFTER INSERT OR UPDATE OR DELETE ON iris.glyph
	FOR EACH STATEMENT EXECUTE PROCEDURE iris.table_notify();

CREATE TRIGGER sign_detail_notify_trig
	AFTER INSERT OR UPDATE OR DELETE ON iris.sign_detail
	FOR EACH STATEMENT EXECUTE PROCEDURE iris.table_notify();

CREATE TRIGGER sign_config_notify_trig
	AFTER INSERT OR UPDATE OR DELETE ON iris.sign_config
	FOR EACH STATEMENT EXECUTE PROCEDURE iris.table_notify();

CREATE TRIGGER sign_message_notify_trig
	AFTER INSERT OR UPDATE OR DELETE ON iris.sign_message
	FOR EACH STATEMENT EXECUTE PROCEDURE iris.table_notify();

CREATE TRIGGER graphic_notify_trig
	AFTER INSERT OR UPDATE OR DELETE ON iris.graphic
	FOR EACH STATEMENT EXECUTE PROCEDURE iris.table_notify();

CREATE FUNCTION iris.dms_notify() RETURNS TRIGGER AS
	$dms_notify$
BEGIN
	IF (NEW.msg_current IS DISTINCT FROM OLD.msg_current) THEN
		NOTIFY dms, 'msg_current';
	ELSE
		NOTIFY dms;
	END IF;
	RETURN NULL; -- AFTER trigger return is ignored
END;
$dms_notify$ LANGUAGE plpgsql;

CREATE TRIGGER dms_notify_trig
	AFTER INSERT OR UPDATE OR DELETE ON iris._dms
	FOR EACH ROW EXECUTE PROCEDURE iris.dms_notify();

CREATE TRIGGER incident_notify_trig
	AFTER INSERT OR UPDATE OR DELETE ON event.incident
	FOR EACH STATEMENT EXECUTE PROCEDURE iris.table_notify();

CREATE FUNCTION iris.parking_area_notify() RETURNS TRIGGER AS
	$parking_area_notify$
BEGIN
	IF (NEW.time_stamp IS DISTINCT FROM OLD.time_stamp) THEN
		NOTIFY parking_area, 'time_stamp';
	ELSE
		NOTIFY parking_area;
	END IF;
	RETURN NULL; -- AFTER trigger return is ignored
END;
$parking_area_notify$ LANGUAGE plpgsql;

CREATE TRIGGER parking_area_notify_trig
	AFTER INSERT OR UPDATE OR DELETE ON iris.parking_area
	FOR EACH ROW EXECUTE PROCEDURE iris.parking_area_notify();

-- Update geo_loc names for r_nodes
ALTER TABLE iris.r_node DROP CONSTRAINT r_node_geo_loc_fkey;
UPDATE iris.geo_loc g
   SET name = n.name, notify_tag = 'r_node'
  FROM iris.r_node n
 WHERE g.name = n.geo_loc;
UPDATE iris.r_node SET geo_loc = name;
ALTER TABLE iris.r_node ADD CONSTRAINT r_node_geo_loc_fkey
    FOREIGN KEY (geo_loc) REFERENCES iris.geo_loc(name);

-- Update geo_loc names for cabinets
ALTER TABLE iris.cabinet DROP CONSTRAINT cabinet_geo_loc_fkey;
UPDATE iris.geo_loc g
   SET name = c.name, notify_tag = 'cabinet'
  FROM iris.cabinet c
 WHERE g.name = c.geo_loc;
UPDATE iris.cabinet SET geo_loc = name;
ALTER TABLE iris.cabinet ADD CONSTRAINT cabinet_geo_loc_fkey
    FOREIGN KEY (geo_loc) REFERENCES iris.geo_loc(name);

-- Update geo_loc names for cameras
ALTER TABLE iris._camera DROP CONSTRAINT _camera_geo_loc_fkey;
UPDATE iris.geo_loc g
   SET name = c.name, notify_tag = 'camera'
  FROM iris._camera c
 WHERE g.name = c.geo_loc;
UPDATE iris._camera SET geo_loc = name;
ALTER TABLE iris._camera ADD CONSTRAINT _camera_geo_loc_fkey
    FOREIGN KEY (geo_loc) REFERENCES iris.geo_loc(name);

-- Update geo_loc names for dms
ALTER TABLE iris._dms DROP CONSTRAINT _dms_geo_loc_fkey;
UPDATE iris.geo_loc g
   SET name = d.name, notify_tag = 'dms'
  FROM iris._dms d
 WHERE g.name = d.geo_loc;
UPDATE iris._dms SET geo_loc = name;
ALTER TABLE iris._dms ADD CONSTRAINT _dms_geo_loc_fkey
    FOREIGN KEY (geo_loc) REFERENCES iris.geo_loc(name);

-- Update geo_loc names for ramp meters
ALTER TABLE iris._ramp_meter DROP CONSTRAINT _ramp_meter_geo_loc_fkey;
UPDATE iris.geo_loc g
   SET name = m.name, notify_tag = 'ramp_meter'
  FROM iris._ramp_meter m
 WHERE g.name = m.geo_loc;
UPDATE iris._ramp_meter SET geo_loc = name;
ALTER TABLE iris._ramp_meter ADD CONSTRAINT _ramp_meter_geo_loc_fkey
    FOREIGN KEY (geo_loc) REFERENCES iris.geo_loc(name);

-- Update geo_loc names for tag readers
ALTER TABLE iris._tag_reader DROP CONSTRAINT _tag_reader_geo_loc_fkey;
UPDATE iris.geo_loc g
   SET name = r.name, notify_tag = 'tag_reader'
  FROM iris._tag_reader r
 WHERE g.name = r.geo_loc;
UPDATE iris._tag_reader SET geo_loc = name;
ALTER TABLE iris._tag_reader ADD CONSTRAINT _tag_reader_geo_loc_fkey
    FOREIGN KEY (geo_loc) REFERENCES iris.geo_loc(name);

-- Update geo_loc names for beacons
ALTER TABLE iris._beacon DROP CONSTRAINT _beacon_geo_loc_fkey;
UPDATE iris.geo_loc g
   SET name = b.name, notify_tag = 'beacon'
  FROM iris._beacon b
 WHERE g.name = b.geo_loc;
UPDATE iris._beacon SET geo_loc = name;
ALTER TABLE iris._beacon ADD CONSTRAINT _beacon_geo_loc_fkey
    FOREIGN KEY (geo_loc) REFERENCES iris.geo_loc(name);

-- Update geo_loc names for gate arm arrays
ALTER TABLE iris._gate_arm_array DROP CONSTRAINT _gate_arm_array_geo_loc_fkey;
UPDATE iris.geo_loc g
   SET name = ga.name, notify_tag = 'gate_arm_array'
  FROM iris._gate_arm_array ga
 WHERE g.name = ga.geo_loc;
UPDATE iris._gate_arm_array SET geo_loc = name;
ALTER TABLE iris._gate_arm_array ADD CONSTRAINT _gate_arm_array_geo_loc_fkey
    FOREIGN KEY (geo_loc) REFERENCES iris.geo_loc(name);

-- Update geo_loc names for weather sensors
ALTER TABLE iris._weather_sensor DROP CONSTRAINT _weather_sensor_geo_loc_fkey;
UPDATE iris.geo_loc g
   SET name = w.name, notify_tag = 'weather_sensor'
  FROM iris._weather_sensor w
 WHERE g.name = w.geo_loc;
UPDATE iris._weather_sensor SET geo_loc = name;
ALTER TABLE iris._weather_sensor ADD CONSTRAINT _weather_sensor_geo_loc_fkey
    FOREIGN KEY (geo_loc) REFERENCES iris.geo_loc(name);

-- Delete unloved geo_loc records
DELETE FROM iris.geo_loc WHERE notify_tag IS NULL;

-- Add dms_attribute_view
CREATE VIEW dms_attribute_view AS
	SELECT name, value
	FROM iris.system_attribute
	WHERE name LIKE 'dms\_%';
GRANT SELECT ON dms_attribute_view TO PUBLIC;

-- Add geo_loc to r_node_view
DROP VIEW r_node_view;
CREATE VIEW r_node_view AS
	SELECT n.name, n.geo_loc, roadway, road_dir, cross_mod, cross_street,
	       cross_dir, nt.name AS node_type, n.pickable, n.above,
	       tr.name AS transition, n.lanes, n.attach_side, n.shift, n.active,
	       n.abandoned, n.station_id, n.speed_limit, n.notes
	FROM iris.r_node n
	JOIN geo_loc_view l ON n.geo_loc = l.name
	JOIN iris.r_node_type nt ON n.node_type = nt.n_type
	JOIN iris.r_node_transition tr ON n.transition = tr.n_transition;
GRANT SELECT ON r_node_view TO PUBLIC;

COMMIT;
