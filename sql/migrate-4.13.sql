\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '4.13.0'
	WHERE name = 'database_version';

UPDATE iris.graphic SET width = 2, pixels = 'AAAA' WHERE name = '12_full_32';

UPDATE iris.privilege SET pattern = 'gate_arm_array/.*/armStateNext'
	WHERE pattern = 'gate_arm_array/.*/armState';

DROP VIEW recent_sign_event_view;
CREATE VIEW recent_sign_event_view AS
	SELECT event_id, event_date, description, device_id, message, iris_user
	FROM sign_event_view
	WHERE event_date > (CURRENT_TIMESTAMP - interval '90 days');
GRANT SELECT ON recent_sign_event_view TO PUBLIC;

INSERT INTO iris.system_attribute (name, value) VALUES ('camera_ptz_panel_enable', 'false');

INSERT INTO iris.comm_protocol (id, description) VALUES (29, 'Cohu PTZ');

INSERT INTO iris.system_attribute (name, value) VALUES ('camera_preset_panel_columns', '6');
INSERT INTO iris.system_attribute (name, value) VALUES ('camera_preset_panel_enable', 'true');
INSERT INTO iris.system_attribute (name, value) VALUES ('camera_util_panel_enable', 'false');
INSERT INTO iris.system_attribute (name, value) VALUES ('camera_preset_store_enable', 'false');
