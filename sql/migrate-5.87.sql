\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.86.0', '5.87.0');

-- Add hashtag to msg_line
ALTER TABLE iris.msg_line ADD COLUMN hashtag VARCHAR(16);
ALTER TABLE iris.msg_line ADD
    CONSTRAINT hashtag_ck CHECK (hashtag ~ '^#[A-Za-z0-9]+$');
UPDATE iris.msg_line
   SET hashtag = compose_hashtag
  FROM iris.msg_pattern mp
 WHERE mp.name = msg_pattern;
UPDATE iris.msg_line SET hashtag = '#Test' WHERE hashtag IS NULL;
ALTER TABLE iris.msg_line ALTER COLUMN hashtag SET NOT NULL;

-- DROP msg_pattern from msg_line
DROP VIEW msg_line_view;
ALTER TABLE iris.msg_line DROP COLUMN msg_pattern;
CREATE VIEW msg_line_view AS
    SELECT name, hashtag, line, rank, multi
    FROM iris.msg_line;
GRANT SELECT ON msg_line_view TO PUBLIC;

-- DROP prototype from msg_pattern
DROP VIEW msg_pattern_view;
ALTER TABLE iris.msg_pattern DROP COLUMN prototype;
CREATE VIEW msg_pattern_view AS
    SELECT name, compose_hashtag, multi, flash_beacon, pixel_service
    FROM iris.msg_pattern;
GRANT SELECT ON msg_pattern_view TO PUBLIC;

-- DELETE superfluous msg_pattern records
DELETE FROM iris.msg_pattern
  WHERE (multi = '' OR multi = '[np]')
    AND name NOT IN ('.1.LINE', '.2.LINE', '.3.LINE', '.4.LINE')
    AND name NOT IN (
        SELECT DISTINCT msg_pattern
        FROM iris.device_action
        WHERE msg_pattern IS NOT NULL);

-- Add "template_label" to camera_view
DROP VIEW camera_view;
CREATE VIEW camera_view AS
    SELECT c.name, cam_num, c.cam_template, t.label AS template_label,
           encoder_type, et.make, et.model,
           et.config, c.enc_address, c.enc_port, c.enc_mcast, c.enc_channel,
           c.publish, c.video_loss, c.geo_loc,
           l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir,
           l.landmark, l.lat, l.lon, l.corridor, l.location,
           cio.controller, ctr.comm_link, ctr.drop_id,
           cnd.description AS condition, c.notes
    FROM iris._camera c
    JOIN iris.controller_io cio ON c.name = cio.name
    LEFT JOIN iris.camera_template t ON c.cam_template = t.name
    LEFT JOIN iris.encoder_type et ON c.encoder_type = et.name
    LEFT JOIN geo_loc_view l ON c.geo_loc = l.name
    LEFT JOIN iris.controller ctr ON cio.controller = ctr.name
    LEFT JOIN iris.condition cnd ON ctr.condition = cnd.id;
GRANT SELECT ON camera_view TO PUBLIC;

COMMIT;
