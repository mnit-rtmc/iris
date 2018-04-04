\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

BEGIN;

SELECT iris.update_version('4.68.0', '4.69.0');

-- Fix trailing space in iris.geo_location function
CREATE OR REPLACE FUNCTION iris.geo_location(TEXT, TEXT, TEXT, TEXT, TEXT, TEXT)
	RETURNS TEXT AS $geo_location$
DECLARE
	roadway ALIAS FOR $1;
	road_dir ALIAS FOR $2;
	cross_mod ALIAS FOR $3;
	cross_street ALIAS FOR $4;
	cross_dir ALIAS FOR $5;
	landmark ALIAS FOR $6;
	res TEXT;
BEGIN
	res = trim(roadway || ' ' || road_dir);
	IF char_length(cross_street) > 0 THEN
		RETURN trim(concat(res || ' ', cross_mod || ' ', cross_street),
		            ' ' || cross_dir);
	ELSIF char_length(landmark) > 0 THEN
		RETURN concat(res || ' ', '(' || landmark || ')');
	ELSE
		RETURN res;
	END IF;
END;
$geo_location$ LANGUAGE plpgsql;

-- Drop old stuff
DROP VIEW gate_arm_view;
DROP VIEW gate_arm_array_view;
DROP VIEW iris.gate_arm_array;
DROP FUNCTION iris.gate_arm_array_update();

-- Add columns to gate_arm_array
ALTER TABLE iris._gate_arm_array ADD COLUMN action_plan VARCHAR(16)
	REFERENCES iris.action_plan;
ALTER TABLE iris._gate_arm_array ADD COLUMN open_phase VARCHAR(12)
	REFERENCES iris.plan_phase;
ALTER TABLE iris._gate_arm_array ADD COLUMN closed_phase VARCHAR(12)
	REFERENCES iris.plan_phase;

-- Drop columns from gate_arm_array
ALTER TABLE iris._gate_arm_array DROP COLUMN dms;
ALTER TABLE iris._gate_arm_array DROP COLUMN open_msg;
ALTER TABLE iris._gate_arm_array DROP COLUMN closed_msg;

-- Add gate_arm_array
CREATE VIEW iris.gate_arm_array AS
	SELECT _gate_arm_array.name, geo_loc, controller, pin, notes, prereq,
	       camera, approach, action_plan, open_phase, closed_phase
	FROM iris._gate_arm_array JOIN iris._device_io
	ON _gate_arm_array.name = _device_io.name;

-- Add gate_arm_array triggers
CREATE FUNCTION iris.gate_arm_array_insert() RETURNS TRIGGER AS
	$gate_arm_array_insert$
BEGIN
	INSERT INTO iris._device_io (name, controller, pin)
	     VALUES (NEW.name, NEW.controller, NEW.pin);
	INSERT INTO iris._gate_arm_array (name, geo_loc, notes, prereq, camera,
	                                  approach, action_plan, open_phase,
	                                  closed_phase)
	    VALUES (NEW.name, NEW.geo_loc, NEW.notes, NEW.prereq, NEW.camera,
	            NEW.approach, NEW.action_plan, NEW.open_phase,
	            NEW.closed_phase);
	RETURN NEW;
END;
$gate_arm_array_insert$ LANGUAGE plpgsql;

CREATE TRIGGER gate_arm_array_insert_trig
    INSTEAD OF INSERT ON iris.gate_arm_array
    FOR EACH ROW EXECUTE PROCEDURE iris.gate_arm_array_insert();

CREATE FUNCTION iris.gate_arm_array_update() RETURNS TRIGGER AS
	$gate_arm_array_update$
BEGIN
	UPDATE iris._device_io SET controller = NEW.controller, pin = NEW.pin
	WHERE name = OLD.name;
	UPDATE iris._gate_arm_array
	   SET geo_loc = NEW.geo_loc,
	       notes = NEW.notes,
	       prereq = NEW.prereq,
	       camera = NEW.camera,
	       approach = NEW.approach,
	       action_plan = NEW.action_plan,
	       open_phase = NEW.open_phase,
	       closed_phase = NEW.closed_phase
	WHERE name = OLD.name;
	RETURN NEW;
END;
$gate_arm_array_update$ LANGUAGE plpgsql;

CREATE TRIGGER gate_arm_array_update_trig
    INSTEAD OF UPDATE ON iris.gate_arm_array
    FOR EACH ROW EXECUTE PROCEDURE iris.gate_arm_array_update();

CREATE FUNCTION iris.gate_arm_array_delete() RETURNS TRIGGER AS
	$gate_arm_array_delete$
BEGIN
	DELETE FROM iris._device_io WHERE name = OLD.name;
	IF FOUND THEN
		RETURN OLD;
	ELSE
		RETURN NULL;
	END IF;
END;
$gate_arm_array_delete$ LANGUAGE plpgsql;

CREATE TRIGGER gate_arm_array_delete_trig
    INSTEAD OF DELETE ON iris.gate_arm_array
    FOR EACH ROW EXECUTE PROCEDURE iris.gate_arm_array_delete();

-- Add gate_arm_array_view
CREATE VIEW gate_arm_array_view AS
	SELECT ga.name, ga.notes, ga.geo_loc, l.roadway, l.road_dir,
	       l.cross_mod, l.cross_street, l.cross_dir, l.lat, l.lon,
	       ga.controller, ga.pin, ctr.comm_link, ctr.drop_id, ctr.condition,
	       ga.prereq, ga.camera, ga.approach, ga.action_plan, ga.open_phase,
	       ga.closed_phase
	FROM iris.gate_arm_array ga
	LEFT JOIN geo_loc_view l ON ga.geo_loc = l.name
	LEFT JOIN controller_view ctr ON ga.controller = ctr.name;
GRANT SELECT ON gate_arm_array_view TO PUBLIC;

-- Add gate_arm_view
CREATE VIEW gate_arm_view AS
	SELECT g.name, g.ga_array, g.notes, ga.geo_loc, l.roadway, l.road_dir,
	       l.cross_mod, l.cross_street, l.cross_dir, l.lat, l.lon,
	       g.controller, g.pin, ctr.comm_link, ctr.drop_id, ctr.condition,
	       ga.prereq, ga.camera, ga.approach
	FROM iris.gate_arm g
	JOIN iris.gate_arm_array ga ON g.ga_array = ga.name
	LEFT JOIN geo_loc_view l ON ga.geo_loc = l.name
	LEFT JOIN controller_view ctr ON g.controller = ctr.name;
GRANT SELECT ON gate_arm_view TO PUBLIC;

COMMIT;
