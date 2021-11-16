\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.24.0', '5.25.0');

-- Change existing TIMEOUT (308) gate arm events to UNKNOWN (301)
UPDATE event.gate_arm_event SET event_desc_id = 301 WHERE event_desc_id = 308;

-- Delete gate arm TIMEOUT event type
DELETE FROM event.event_description WHERE event_desc_id = 308;

-- Don't allow using the same action plan for more than one gate arm array
ALTER TABLE iris._gate_arm_array
    ADD CONSTRAINT _gate_arm_array_action_plan_key UNIQUE (action_plan);

-- Drop gate arm views and functions
DROP VIEW gate_arm_view;
DROP VIEW gate_arm_array_view;
DROP VIEW iris.gate_arm_array;
DROP FUNCTION iris.gate_arm_array_insert();
DROP FUNCTION iris.gate_arm_array_update();

-- Drop unnecessary open/closed phase configuration
ALTER TABLE iris._gate_arm_array DROP COLUMN open_phase;
ALTER TABLE iris._gate_arm_array DROP COLUMN closed_phase;

-- Recreate gate arm views and functions
CREATE VIEW iris.gate_arm_array AS
	SELECT _gate_arm_array.name, geo_loc, controller, pin, notes, prereq,
	       camera, approach, action_plan
	FROM iris._gate_arm_array JOIN iris._device_io
	ON _gate_arm_array.name = _device_io.name;

CREATE FUNCTION iris.gate_arm_array_insert() RETURNS TRIGGER AS
	$gate_arm_array_insert$
BEGIN
	INSERT INTO iris._device_io (name, controller, pin)
	     VALUES (NEW.name, NEW.controller, NEW.pin);
	INSERT INTO iris._gate_arm_array (name, geo_loc, notes, prereq, camera,
	                                  approach, action_plan)
	    VALUES (NEW.name, NEW.geo_loc, NEW.notes, NEW.prereq, NEW.camera,
	            NEW.approach, NEW.action_plan);
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
	       action_plan = NEW.action_plan
	WHERE name = OLD.name;
	RETURN NEW;
END;
$gate_arm_array_update$ LANGUAGE plpgsql;

CREATE TRIGGER gate_arm_array_update_trig
    INSTEAD OF UPDATE ON iris.gate_arm_array
    FOR EACH ROW EXECUTE PROCEDURE iris.gate_arm_array_update();

CREATE TRIGGER gate_arm_array_delete_trig
    INSTEAD OF DELETE ON iris.gate_arm_array
    FOR EACH ROW EXECUTE PROCEDURE iris.device_delete();

CREATE VIEW gate_arm_array_view AS
	SELECT ga.name, ga.notes, ga.geo_loc,
	       l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir,
	       l.landmark, l.lat, l.lon, l.corridor, l.location,
	       ga.controller, ga.pin, ctr.comm_link, ctr.drop_id, ctr.condition,
	       ga.prereq, ga.camera, ga.approach, ga.action_plan
	FROM iris.gate_arm_array ga
	LEFT JOIN geo_loc_view l ON ga.geo_loc = l.name
	LEFT JOIN controller_view ctr ON ga.controller = ctr.name;
GRANT SELECT ON gate_arm_array_view TO PUBLIC;

CREATE VIEW gate_arm_view AS
	SELECT g.name, g.ga_array, g.notes, ga.geo_loc,
	       l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir,
	       l.landmark, l.lat, l.lon, l.corridor, l.location,
	       g.controller, g.pin, ctr.comm_link, ctr.drop_id, ctr.condition,
	       ga.prereq, ga.camera, ga.approach
	FROM iris.gate_arm g
	JOIN iris.gate_arm_array ga ON g.ga_array = ga.name
	LEFT JOIN geo_loc_view l ON ga.geo_loc = l.name
	LEFT JOIN controller_view ctr ON g.controller = ctr.name;
GRANT SELECT ON gate_arm_view TO PUBLIC;

-- Insert gate arm action plan phases if they don't exist
INSERT INTO iris.plan_phase (name, hold_time)
    VALUES ('ga_open', 0), ('ga_closed', 0)
    ON CONFLICT DO NOTHING;

COMMIT;
