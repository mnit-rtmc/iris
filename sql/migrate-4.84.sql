\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

BEGIN;

SELECT iris.update_version('4.83.0', '4.84.0');

-- Add road affix table
CREATE TABLE iris.road_affix (
	name VARCHAR(12) PRIMARY KEY,
	prefix BOOLEAN NOT NULL,
	fixup VARCHAR(12)
);

-- Add default road affix rows
COPY iris.road_affix (name, prefix, fixup) FROM stdin;
C.S.A.H.	t	CTY
CO RD	t	CTY
I-	t	
U.S.	t	HWY
T.H.	t	HWY
AVE	f	
BLVD	f	
CIR	f	
DR	f	
HWY	f	
LN	f	
PKWY	f	
PL	f	
RD	f	
ST	f	
TR	f	
WAY	f	
\.

-- Add road_affix to sonar type lut
INSERT INTO iris.sonar_type (name) VALUES ('road_affix');

-- Add default values to privilege columns
ALTER TABLE iris.privilege ALTER COLUMN obj_n SET DEFAULT ''::VARCHAR;
ALTER TABLE iris.privilege ALTER COLUMN group_n SET DEFAULT ''::VARCHAR;
ALTER TABLE iris.privilege ALTER COLUMN attr_n SET DEFAULT ''::VARCHAR;

-- Add privileges for road_affix
INSERT INTO iris.privilege (name, capability, type_n, write)
	(SELECT 'prv_ra' || ROW_NUMBER() OVER (ORDER BY name), capability,
	 'road_affix', write
	 FROM iris.privilege
	 WHERE type_n = 'road');

-- Add last_fail (and rename count to event_count)
DROP VIEW detector_auto_fail_view;
CREATE VIEW detector_auto_fail_view AS
	WITH af AS (SELECT device_id, event_desc_id, count(*) AS event_count,
		    max(event_date) AS last_fail
		    FROM event.detector_event
		    GROUP BY device_id, event_desc_id)
	SELECT device_id, label, ed.description, event_count, last_fail
	FROM af
	JOIN event.event_description ed ON af.event_desc_id = ed.event_desc_id
	JOIN detector_label_view dl ON af.device_id = dl.det_id;
GRANT SELECT ON detector_auto_fail_view TO PUBLIC;

-- Add detector "no change" event
INSERT INTO event.event_description (event_desc_id, description)
	VALUES (97, 'NO CHANGE');

-- Add connection refused event
INSERT INTO event.event_description (event_desc_id, description)
	VALUES (15, 'Comm CONNECTION REFUSED');

COMMIT;
