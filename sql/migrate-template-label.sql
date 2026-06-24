\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

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
