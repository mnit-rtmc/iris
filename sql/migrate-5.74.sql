\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.73.0', '5.74.0');

-- Delete DMS pixel system attributes
DELETE FROM iris.system_attribute WHERE name IN (
    'dms_pixel_off_limit',
    'dms_pixel_on_limit',
    'dms_pixel_test_timeout_secs'
);

-- Add geo_loc, preset, opposing, downstream, interlock to gate arms
DROP VIEW controller_report;
DROP VIEW controller_device_view;
DROP VIEW iris.device_geo_loc_view;
DROP VIEW gate_arm_view;
DROP VIEW iris.gate_arm;

ALTER TABLE iris._gate_arm ADD COLUMN geo_loc VARCHAR(20);
ALTER TABLE iris._gate_arm ADD COLUMN opposing BOOLEAN;
ALTER TABLE iris._gate_arm ADD COLUMN downstream VARCHAR(16);
ALTER TABLE iris._gate_arm ADD COLUMN interlock INTEGER
    REFERENCES iris.gate_arm_interlock;

INSERT INTO iris.geo_loc (
    name, resource_n, roadway, road_dir, cross_street, cross_dir, cross_mod,
    lat, lon, landmark
) (
    SELECT g.name, 'gate_arm', roadway, road_dir, cross_street, cross_dir,
           cross_mod, lat, lon, landmark
    FROM iris._gate_arm g
    JOIN iris._gate_arm_array ga ON ga_array = ga.name
    JOIN iris.geo_loc loc ON loc.name = ga.name
);

UPDATE iris._gate_arm AS g
   SET geo_loc = g.name, opposing = ga.opposing, interlock = ga.interlock
  FROM iris._gate_arm_array AS ga
 WHERE g.ga_array = ga.name;

UPDATE iris._gate_arm AS g
   SET downstream = '#' || ga.prereq
  FROM iris._gate_arm_array AS ga
 WHERE g.ga_array = ga.name;

ALTER TABLE iris._gate_arm ALTER COLUMN geo_loc SET NOT NULL;
ALTER TABLE iris._gate_arm ALTER COLUMN opposing SET NOT NULL;
ALTER TABLE iris._gate_arm ALTER COLUMN interlock SET NOT NULL;

INSERT INTO iris.device_preset (name, resource_n, preset) (
    SELECT name, 'gate_arm', NULL FROM iris._gate_arm
);

CREATE VIEW iris.gate_arm AS
    SELECT g.name, ga_array, idx, geo_loc, controller, pin, preset, notes,
           opposing, downstream, arm_state, interlock, fault
    FROM iris._gate_arm g
    JOIN iris.controller_io cio ON g.name = cio.name
    JOIN iris.device_preset p ON g.name = p.name;

CREATE OR REPLACE FUNCTION iris.gate_arm_insert() RETURNS TRIGGER AS
    $gate_arm_insert$
BEGIN
    INSERT INTO iris.controller_io (name, resource_n, controller, pin)
         VALUES (NEW.name, 'gate_arm', NEW.controller, NEW.pin);
    INSERT INTO iris.device_preset (name, resource_n, preset)
        VALUES (NEW.name, 'gate_arm', NEW.preset);
    INSERT INTO iris._gate_arm (
        name, ga_array, idx, geo_loc, notes, opposing,
        downstream, arm_state, interlock, fault
    ) VALUES (
        NEW.name, NEW.ga_array, NEW.idx, NEW.geo_loc, NEW.notes, NEW.opposing,
        NEW.downstream, NEW.arm_state, NEW.interlock, NEW.fault
    );
    RETURN NEW;
END;
$gate_arm_insert$ LANGUAGE plpgsql;

CREATE TRIGGER gate_arm_insert_trig
    INSTEAD OF INSERT ON iris.gate_arm
    FOR EACH ROW EXECUTE FUNCTION iris.gate_arm_insert();

CREATE OR REPLACE FUNCTION iris.gate_arm_update() RETURNS TRIGGER AS
    $gate_arm_update$
BEGIN
    UPDATE iris.controller_io
       SET controller = NEW.controller, pin = NEW.pin
     WHERE name = OLD.name;
    UPDATE iris.device_preset
       SET preset = NEW.preset
     WHERE name = OLD.name;
    UPDATE iris._gate_arm
       SET ga_array = NEW.ga_array,
           idx = NEW.idx,
           geo_loc = NEW.geo_loc,
           notes = NEW.notes,
           opposing = NEW.opposing,
           downstream = NEW.downstream,
           arm_state = NEW.arm_state,
           interlock = NEW.interlock,
           fault = NEW.fault
     WHERE name = OLD.name;
    RETURN NEW;
END;
$gate_arm_update$ LANGUAGE plpgsql;

CREATE TRIGGER gate_arm_update_trig
    INSTEAD OF UPDATE ON iris.gate_arm
    FOR EACH ROW EXECUTE FUNCTION iris.gate_arm_update();

CREATE TRIGGER gate_arm_delete_trig
    INSTEAD OF DELETE ON iris.gate_arm
    FOR EACH ROW EXECUTE FUNCTION iris.controller_io_delete();

CREATE VIEW gate_arm_view AS
    SELECT g.name, g.ga_array, g.notes,
           g.geo_loc, l.roadway, l.road_dir, l.cross_mod, l.cross_street,
           l.cross_dir, l.landmark, l.lat, l.lon, l.corridor, l.location,
           cio.controller, cio.pin, ctr.comm_link, ctr.drop_id, ctr.condition,
           cp.camera, cp.preset_num, g.opposing, g.downstream,
           gas.description AS arm_state, gai.description AS interlock, fault
    FROM iris._gate_arm g
    JOIN iris.controller_io cio ON g.name = cio.name
    LEFT JOIN iris.device_preset p ON g.name = p.name
    LEFT JOIN iris.camera_preset cp ON cp.name = p.preset
    JOIN iris.gate_arm_state gas ON g.arm_state = gas.id
    JOIN iris.gate_arm_interlock gai ON g.interlock = gai.id
    LEFT JOIN geo_loc_view l ON g.geo_loc = l.name
    LEFT JOIN controller_view ctr ON cio.controller = ctr.name;
GRANT SELECT ON gate_arm_view TO PUBLIC;

CREATE VIEW iris.device_geo_loc_view AS
    SELECT name, geo_loc FROM iris._beacon UNION ALL
    SELECT name, geo_loc FROM iris._camera UNION ALL
    SELECT name, geo_loc FROM iris._dms UNION ALL
    SELECT name, geo_loc FROM iris._gate_arm UNION ALL
    SELECT name, geo_loc FROM iris._lcs UNION ALL
    SELECT name, geo_loc FROM iris._ramp_meter UNION ALL
    SELECT name, geo_loc FROM iris._tag_reader UNION ALL
    SELECT name, geo_loc FROM iris._weather_sensor UNION ALL
    SELECT d.name, geo_loc FROM iris._detector d
    JOIN iris.r_node rn ON d.r_node = rn.name;

CREATE VIEW controller_device_view AS
    SELECT cio.name, cio.controller, cio.pin, g.geo_loc,
           trim(l.roadway || ' ' || l.road_dir) AS corridor,
           trim(trim(' @' FROM l.cross_mod || ' ' || l.cross_street)
           || ' ' || l.cross_dir) AS cross_loc
      FROM iris.controller_io cio
      JOIN iris.device_geo_loc_view g ON cio.name = g.name
      JOIN geo_loc_view l ON g.geo_loc = l.name;
GRANT SELECT ON controller_device_view TO PUBLIC;

CREATE VIEW controller_report AS
    SELECT c.name, c.comm_link, c.drop_id, l.landmark, c.geo_loc, l.location,
           cabinet_style, d.name AS device, d.pin, d.cross_loc, d.corridor,
           c.notes
    FROM iris.controller c
    LEFT JOIN geo_loc_view l ON c.geo_loc = l.name
    LEFT JOIN controller_device_view d ON d.controller = c.name;
GRANT SELECT ON controller_report TO PUBLIC;

COMMIT;
