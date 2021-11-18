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

-- Add gate arm state look-up table
CREATE TABLE iris.gate_arm_state (
	id INTEGER PRIMARY KEY,
	description VARCHAR(10) NOT NULL
);

COPY iris.gate_arm_state(id, description) FROM stdin;
0	unknown
1	fault
2	opening
3	open
4	warn close
5	closing
6	closed
\.

-- Add gate arm interlock look-up table
CREATE TABLE iris.gate_arm_interlock (
	id INTEGER PRIMARY KEY,
	description VARCHAR(16) NOT NULL
);

COPY iris.gate_arm_interlock(id, description) FROM stdin;
0	none
1	deny open
2	deny close
3	deny all
4	system disable
\.

-- Drop gate arm views and functions
DROP VIEW gate_arm_event_view;
DROP VIEW gate_arm_view;
DROP VIEW iris.gate_arm;
DROP FUNCTION iris.gate_arm_insert();
DROP FUNCTION iris.gate_arm_update();
DROP VIEW gate_arm_array_view;
DROP VIEW iris.gate_arm_array;
DROP FUNCTION iris.gate_arm_array_insert();
DROP FUNCTION iris.gate_arm_array_update();

-- Drop unnecessary open/closed phase configuration
ALTER TABLE iris._gate_arm_array DROP COLUMN open_phase;
ALTER TABLE iris._gate_arm_array DROP COLUMN closed_phase;

-- Add arm_state to gate arm
ALTER TABLE iris._gate_arm
    ADD COLUMN arm_state INTEGER REFERENCES iris.gate_arm_state;
UPDATE iris._gate_arm SET arm_state = 0;
ALTER TABLE iris._gate_arm
    ALTER COLUMN arm_state SET NOT NULL;

-- Add fault to gate arm
ALTER TABLE iris._gate_arm ADD COLUMN fault VARCHAR(32);

-- Add opposing to gate arm array
ALTER TABLE iris._gate_arm_array ADD COLUMN opposing BOOLEAN;
UPDATE iris._gate_arm_array SET opposing = true;
ALTER TABLE iris._gate_arm_array ALTER COLUMN opposing SET NOT NULL;

-- Add arm_state to gate arm array
ALTER TABLE iris._gate_arm_array
    ADD COLUMN arm_state INTEGER REFERENCES iris.gate_arm_state;
UPDATE iris._gate_arm_array SET arm_state = 0;
ALTER TABLE iris._gate_arm_array
    ALTER COLUMN arm_state SET NOT NULL;

-- Add interlock to gate arm array
ALTER TABLE iris._gate_arm_array
    ADD COLUMN interlock INTEGER REFERENCES iris.gate_arm_interlock;
UPDATE iris._gate_arm_array SET interlock = 0;
ALTER TABLE iris._gate_arm_array
    ALTER COLUMN interlock SET NOT NULL;

-- Recreate gate arm views and functions
CREATE VIEW iris.gate_arm_array AS
	SELECT _gate_arm_array.name, geo_loc, controller, pin, notes, opposing,
	       prereq, camera, approach, action_plan, arm_state, interlock
	FROM iris._gate_arm_array JOIN iris._device_io
	ON _gate_arm_array.name = _device_io.name;

CREATE FUNCTION iris.gate_arm_array_insert() RETURNS TRIGGER AS
	$gate_arm_array_insert$
BEGIN
	INSERT INTO iris._device_io (name, controller, pin)
	     VALUES (NEW.name, NEW.controller, NEW.pin);
	INSERT INTO iris._gate_arm_array (
	    name, geo_loc, notes, opposing, prereq, camera, approach,
	    action_plan, arm_state, interlock
	) VALUES (
	    NEW.name, NEW.geo_loc, NEW.notes, NEW.opposing, NEW.prereq,
	    NEW.camera, NEW.approach, NEW.action_plan, NEW.arm_state,
	    NEW.interlock
	);
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
	       opposing = NEW.opposing,
	       prereq = NEW.prereq,
	       camera = NEW.camera,
	       approach = NEW.approach,
	       action_plan = NEW.action_plan,
	       arm_state = NEW.arm_state,
	       interlock = NEW.interlock
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
	       ga.opposing, ga.prereq, ga.camera, ga.approach, ga.action_plan,
	       gas.description AS arm_state, gai.description AS interlock
	FROM iris.gate_arm_array ga
	JOIN iris.gate_arm_state gas ON ga.arm_state = gas.id
	JOIN iris.gate_arm_interlock gai ON ga.interlock = gai.id
	LEFT JOIN geo_loc_view l ON ga.geo_loc = l.name
	LEFT JOIN controller_view ctr ON ga.controller = ctr.name;
GRANT SELECT ON gate_arm_array_view TO PUBLIC;

CREATE VIEW iris.gate_arm AS
	SELECT _gate_arm.name, ga_array, idx, controller, pin, notes, arm_state,
	       fault
	FROM iris._gate_arm JOIN iris._device_io
	ON _gate_arm.name = _device_io.name;

CREATE FUNCTION iris.gate_arm_insert() RETURNS TRIGGER AS
	$gate_arm_insert$
BEGIN
	INSERT INTO iris._device_io (name, controller, pin)
	     VALUES (NEW.name, NEW.controller, NEW.pin);
	INSERT INTO iris._gate_arm (
	    name, ga_array, idx, notes, arm_state, fault
	) VALUES (
	    NEW.name, NEW.ga_array, NEW.idx, NEW.notes, NEW.arm_state, NEW.fault
	);
	RETURN NEW;
END;
$gate_arm_insert$ LANGUAGE plpgsql;

CREATE TRIGGER gate_arm_insert_trig
    INSTEAD OF INSERT ON iris.gate_arm
    FOR EACH ROW EXECUTE PROCEDURE iris.gate_arm_insert();

CREATE FUNCTION iris.gate_arm_update() RETURNS TRIGGER AS
	$gate_arm_update$
BEGIN
	UPDATE iris._device_io
	   SET controller = NEW.controller, pin = NEW.pin
	WHERE name = OLD.name;
        UPDATE iris._gate_arm
	   SET ga_array = NEW.ga_array,
	       idx = NEW.idx,
	       notes = NEW.notes,
	       arm_state = NEW.arm_state,
	       fault = NEW.fault
	WHERE name = OLD.name;
        RETURN NEW;
END;
$gate_arm_update$ LANGUAGE plpgsql;

CREATE TRIGGER gate_arm_update_trig
    INSTEAD OF UPDATE ON iris.gate_arm
    FOR EACH ROW EXECUTE PROCEDURE iris.gate_arm_update();

CREATE TRIGGER gate_arm_delete_trig
    INSTEAD OF DELETE ON iris.gate_arm
    FOR EACH ROW EXECUTE PROCEDURE iris.device_delete();

CREATE VIEW gate_arm_view AS
	SELECT g.name, g.ga_array, g.notes, ga.geo_loc,
	       l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir,
	       l.landmark, l.lat, l.lon, l.corridor, l.location,
	       g.controller, g.pin, ctr.comm_link, ctr.drop_id, ctr.condition,
	       ga.opposing, ga.prereq, ga.camera, ga.approach,
	       gas.description AS arm_state, fault
	FROM iris.gate_arm g
	JOIN iris.gate_arm_state gas ON g.arm_state = gas.id
	JOIN iris.gate_arm_array ga ON g.ga_array = ga.name
	LEFT JOIN geo_loc_view l ON ga.geo_loc = l.name
	LEFT JOIN controller_view ctr ON g.controller = ctr.name;
GRANT SELECT ON gate_arm_view TO PUBLIC;

-- Insert gate arm action plan phases if they don't exist
INSERT INTO iris.plan_phase (name, hold_time)
    VALUES ('ga_open', 0), ('ga_closed', 0)
    ON CONFLICT DO NOTHING;

-- Add fault column to gate arm event
ALTER TABLE event.gate_arm_event ADD COLUMN fault VARCHAR(32);

CREATE VIEW gate_arm_event_view AS
	SELECT e.event_id, e.event_date, ed.description, device_id, e.iris_user,
	       e.fault
	FROM event.gate_arm_event e
	JOIN event.event_description ed ON e.event_desc_id = ed.event_desc_id;
GRANT SELECT ON gate_arm_event_view TO PUBLIC;

COMMIT;
