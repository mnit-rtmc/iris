\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '3.103.0'
	WHERE name = 'database_version';

ALTER TABLE iris.lane_use_multi ADD COLUMN msg_num INTEGER;
ALTER TABLE iris.lane_use_multi ADD COLUMN width INTEGER;
ALTER TABLE iris.lane_use_multi ADD COLUMN height INTEGER;
ALTER TABLE iris.lane_use_multi ADD COLUMN quick_message VARCHAR(20)
	REFERENCES iris.quick_message;
UPDATE iris.lane_use_multi SET width = 80;
ALTER TABLE iris.lane_use_multi ALTER COLUMN width SET NOT NULL;
UPDATE iris.lane_use_multi SET height = 64;
ALTER TABLE iris.lane_use_multi ALTER COLUMN height SET NOT NULL;

ALTER TABLE iris.lane_use_multi DROP CONSTRAINT lane_use_multi_indication_key;

CREATE UNIQUE INDEX lane_use_multi_indication_idx ON iris.lane_use_multi
	USING btree (indication, width, height);

CREATE UNIQUE INDEX lane_use_multi_msg_num_idx ON iris.lane_use_multi
	USING btree (msg_num, width, height);

INSERT INTO iris.quick_message (name, multi)
	(SELECT name, multi FROM iris.lane_use_multi);

UPDATE iris.lane_use_multi SET quick_message = name;

ALTER TABLE iris.lane_use_multi DROP COLUMN multi;
