--
-- PostgreSQL database template for IRIS
--
-- ** Overview **
--
-- * public schema contains only VIEWs
-- * LUTs are updated only on version changes
-- * LUTs should have 'id' INTEGER + 'description' VARCHAR (no len constraint)
-- * Columns named 'notes' may include hashtags.
--
SET client_encoding = 'UTF8';

\set ON_ERROR_STOP
BEGIN;

ALTER SCHEMA public OWNER TO tms;

CREATE SCHEMA iris;
ALTER SCHEMA iris OWNER TO tms;

CREATE SCHEMA event;
ALTER SCHEMA event OWNER TO tms;

CREATE SCHEMA cap;
ALTER SCHEMA cap OWNER TO tms;

CREATE EXTENSION postgis;

SET SESSION AUTHORIZATION 'tms';

SET search_path = public, pg_catalog;

--
-- System attributes
--
CREATE TABLE iris.system_attribute (
    name VARCHAR(32) PRIMARY KEY,
    value VARCHAR(64) NOT NULL
);

CREATE FUNCTION iris.table_notify() RETURNS TRIGGER AS
    $table_notify$
BEGIN
    PERFORM pg_notify(LTRIM(TG_TABLE_NAME, '_'), '');
    RETURN NULL; -- AFTER trigger return is ignored
END;
$table_notify$ LANGUAGE plpgsql;

CREATE TRIGGER system_attribute_notify_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris.system_attribute
    FOR EACH STATEMENT EXECUTE FUNCTION iris.table_notify();

COPY iris.system_attribute (name, value) FROM stdin;
action_plan_alert_list	
alert_clear_secs	300
alert_sign_thresh_auto_meters	1000
alert_sign_thresh_opt_meters	4000
camera_autoplay	true
camera_blank_url	
camera_construction_url	
camera_image_base_url	
camera_kbd_panasonic_enable	false
camera_latest_ptz_enable	false
camera_num_blank	999
camera_out_of_service_url	
camera_playlist_dwell_sec	5
camera_ptz_blind	true
camera_stream_controls_enable	false
cap_save_enable	true
clearguide_key	
client_units_si	true
database_version	5.68.0
detector_auto_fail_enable	true
detector_data_archive_enable	true
detector_occ_spike_secs	60
dms_comm_loss_enable	true
dms_message_tooltip_enable	false
dms_page_on_max_secs	10.0
dms_page_on_min_secs	0.5
dms_pixel_off_limit	2
dms_pixel_on_limit	1
dms_pixel_test_timeout_secs	30
dms_send_confirmation_enable	false
dms_update_font_table	true
email_rate_limit_hours	0
email_sender_server	
email_smtp_host	
gate_arm_alert_timeout_secs	90
gps_jitter_m	100
help_trouble_ticket_enable	false
help_trouble_ticket_url	
incident_clear_advice_multi	JUST CLEARED
incident_clear_secs	300
legacy_xml_config_enable	true
legacy_xml_detector_enable	true
legacy_xml_incident_enable	true
legacy_xml_sign_message_enable	true
legacy_xml_weather_sensor_enable	true
map_extent_name_initial	Home
map_icon_size_scale_max	30
map_segment_max_meters	2000
meter_green_secs	1.3
meter_max_red_secs	13.0
meter_min_red_secs	0.1
meter_yellow_secs	0.7
msg_feed_verify	true
route_max_legs	8
route_max_miles	16
rwis_auto_max_dist_miles	1.0
rwis_obs_age_limit_secs	900
rwis_slippery_1_percent	70
rwis_slippery_2_degrees	0
rwis_slippery_3_percent	60
rwis_windy_1_kph	64
rwis_windy_2_kph	96
rwis_visibility_1_m	1609
rwis_visibility_2_m	402
rwis_flooding_1_mm	6
rwis_flooding_2_mm	8
speed_limit_min_mph	45
speed_limit_default_mph	55
speed_limit_max_mph	75
toll_density_alpha	0.045
toll_density_beta	1.1
toll_min_price	0.25
toll_max_price	8
travel_time_min_mph	15
uptime_log_enable	false
vid_connect_autostart	true
vid_connect_fail_next_source	true
vid_connect_fail_sec	20
vid_lost_timeout_sec	10
vid_max_duration_sec	0
vid_reconnect_auto	true
vid_reconnect_timeout_sec	10
vsa_bottleneck_id_mph	55
vsa_control_threshold	-1000
vsa_downstream_miles	0.2
vsa_max_display_mph	60
vsa_min_display_mph	30
vsa_min_station_miles	0.1
vsa_start_intervals	3
vsa_start_threshold	-1500
vsa_stop_threshold	-750
window_title	IRIS: 
work_request_url	
\.

-- Helper function to check and update database version from migrate scripts
CREATE FUNCTION iris.update_version(TEXT, TEXT) RETURNS TEXT AS
    $update_version$
DECLARE
    ver_prev ALIAS FOR $1;
    ver_new ALIAS FOR $2;
    ver_db TEXT;
BEGIN
    SELECT value INTO ver_db FROM iris.system_attribute
        WHERE name = 'database_version';
    IF ver_db != ver_prev THEN
        RAISE EXCEPTION 'Cannot migrate database -- wrong version: %',
            ver_db;
    END IF;
    UPDATE iris.system_attribute SET value = ver_new
        WHERE name = 'database_version';
    RETURN ver_new;
END;
$update_version$ language plpgsql;

--
-- Resources and Hashtags
--
CREATE TABLE iris.resource_type (
    name VARCHAR(16) PRIMARY KEY,
    base VARCHAR(16) REFERENCES iris.resource_type
);

COPY iris.resource_type (name, base) FROM stdin;
action_plan	\N
day_matcher	action_plan
day_plan	action_plan
device_action	action_plan
plan_phase	action_plan
time_action	action_plan
alert_config	\N
alert_info	alert_config
alert_message	alert_config
beacon	\N
camera	\N
camera_preset	camera
camera_template	camera
cam_vid_src_ord	camera
encoder_stream	camera
encoder_type	camera
vid_src_template	camera
controller	\N
alarm	controller
comm_link	controller
geo_loc	controller
gps	controller
modem	controller
detector	\N
r_node	detector
road	detector
station	detector
dms	\N
font	dms
glyph	dms
graphic	dms
msg_line	dms
msg_pattern	dms
sign_config	dms
sign_detail	dms
sign_message	dms
word	dms
gate_arm	\N
gate_arm_array	gate_arm
incident	\N
incident_detail	incident
inc_advice	incident
inc_descriptor	incident
inc_locator	incident
road_affix	incident
lcs	\N
lcs_state	lcs
parking_area	\N
permission	\N
connection	permission
domain	permission
role	permission
user_id	permission
ramp_meter	\N
system_attribute	\N
cabinet_style	system_attribute
comm_config	system_attribute
event_config	system_attribute
map_extent	system_attribute
rpt_conduit	system_attribute
toll_zone	\N
tag_reader	toll_zone
video_monitor	\N
flow_stream	video_monitor
monitor_style	video_monitor
play_list	video_monitor
weather_sensor	\N
\.

CREATE FUNCTION iris.resource_is_base(VARCHAR(16)) RETURNS BOOLEAN AS
    $resource_is_base$
SELECT EXISTS (
    SELECT 1
    FROM iris.resource_type
    WHERE name = $1 AND base IS NULL
);
$resource_is_base$ LANGUAGE sql;

CREATE TABLE iris.hashtag (
    resource_n VARCHAR(16) NOT NULL REFERENCES iris.resource_type,
    name VARCHAR(20) NOT NULL,
    hashtag VARCHAR(16) NOT NULL,

    CONSTRAINT hashtag_ck CHECK (hashtag ~ '^#[A-Za-z0-9]+$')
);
ALTER TABLE iris.hashtag ADD PRIMARY KEY (resource_n, name, hashtag);

CREATE VIEW hashtag_view AS
    SELECT resource_n, name, hashtag
    FROM iris.hashtag;
GRANT SELECT ON hashtag_view TO PUBLIC;

CREATE FUNCTION iris.parse_tags(notes TEXT) RETURNS SETOF TEXT AS
    $parse_tags$
BEGIN
    RETURN QUERY SELECT tag[1] FROM (
        SELECT regexp_matches(notes, '(#[A-Za-z0-9]+)', 'g') AS tag
    ) AS tags;
END;
$parse_tags$ LANGUAGE plpgsql STABLE;

CREATE FUNCTION iris.hashtag_trig() RETURNS TRIGGER AS
    $hashtag_trig$
BEGIN
    IF (NEW.notes IS DISTINCT FROM OLD.notes) THEN
        IF (TG_OP != 'INSERT') THEN
            DELETE FROM iris.hashtag
            WHERE resource_n = TG_ARGV[0] AND name = OLD.name;
        END IF;
        IF (TG_OP != 'DELETE') THEN
            INSERT INTO iris.hashtag (resource_n, name, hashtag)
            SELECT TG_ARGV[0], NEW.name, iris.parse_tags(NEW.notes);
        END IF;
    END IF;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$hashtag_trig$ LANGUAGE plpgsql;

--
-- Events
--
CREATE SEQUENCE event.event_id_seq;

CREATE TABLE iris.event_config (
    name VARCHAR(32) PRIMARY KEY,
    enable_store BOOLEAN NOT NULL,
    enable_purge BOOLEAN NOT NULL,
    purge_days INTEGER NOT NULL
);

INSERT INTO iris.event_config (name, enable_store, enable_purge, purge_days)
VALUES
    ('action_plan_event', true, true, 90),
    ('alarm_event', true, false, 0),
    ('beacon_event', true, false, 0),
    ('brightness_sample', true, false, 0),
    ('camera_switch_event', true, true, 30),
    ('camera_video_event', true, true, 14),
    ('cap_alert', true, true, 7),
    ('client_event', true, false, 0),
    ('comm_event', true, true, 14),
    ('detector_event', true, true, 90),
    ('email_event', true, true, 30),
    ('gate_arm_event', true, false, 0),
    ('incident', true, false, 0),
    ('incident_update', true, false, 0),
    ('meter_event', true, true, 14),
    ('meter_lock_event', true, false, 0),
    ('price_message_event', true, false, 0),
    ('sign_event', true, false, 0),
    ('tag_read_event', true, false, 0),
    ('travel_time_event', true, true, 1),
    ('weather_sensor_sample', true, true, 90),
    ('weather_sensor_settings', true, false, 0);

CREATE TRIGGER event_config_notify_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris.event_config
    FOR EACH STATEMENT EXECUTE FUNCTION iris.table_notify();

CREATE TABLE event.event_description (
    event_desc_id INTEGER PRIMARY KEY,
    description text NOT NULL
);

COPY event.event_description (event_desc_id, description) FROM stdin;
1	Alarm TRIGGERED
2	Alarm CLEARED
8	Comm ERROR
9	Comm RESTORED
10	Comm QUEUE DRAINED
11	Comm POLL TIMEOUT
12	Comm PARSING ERROR
13	Comm CHECKSUM ERROR
14	Comm CONTROLLER ERROR
15	Comm CONNECTION REFUSED
21	Incident CRASH
22	Incident STALL
23	Incident HAZARD
24	Incident ROADWORK
65	Comm FAILED
81	DMS MSG ERROR
82	DMS PIXEL ERROR
83	DMS MSG RESET
89	LCS DEPLOYED
90	LCS CLEARED
91	Sign DEPLOYED
92	Sign CLEARED
94	NO HITS
95	LOCKED ON
96	CHATTER
97	NO CHANGE
98	OCC SPIKE
101	Sign BRIGHTNESS LOW
102	Sign BRIGHTNESS GOOD
103	Sign BRIGHTNESS HIGH
201	Client CONNECT
202	Client AUTHENTICATE
203	Client FAIL AUTHENTICATION
204	Client DISCONNECT
205	Client CHANGE PASSWORD
206	Client FAIL PASSWORD
207	Client FAIL DOMAIN
208	Client FAIL DOMAIN XFF
209	Client UPDATE PASSWORD
301	Gate Arm UNKNOWN
302	Gate Arm FAULT
303	Gate Arm OPENING
304	Gate Arm OPEN
305	Gate Arm WARN CLOSE
306	Gate Arm CLOSING
307	Gate Arm CLOSED
308	Gate Arm SYSTEM
401	Meter event
402	Meter LOCK
501	Beacon STATE
601	Tag Read
651	Price DEPLOYED
652	Price VERIFIED
701	TT Link too long
703	TT No destination data
704	TT No origin data
705	TT No route
801	Camera SWITCHED
811	Camera Video LOST
812	Camera Video RESTORED
900	Action Plan ACTIVATED
901	Action Plan DEACTIVATED
902	Action Plan Phase CHANGED
903	Action Plan SYSTEM
\.

CREATE TABLE event.email_event (
    id SERIAL PRIMARY KEY,
    event_date TIMESTAMP WITH time zone DEFAULT NOW() NOT NULL,
    event_desc INTEGER NOT NULL REFERENCES event.event_description,
    subject VARCHAR(32) NOT NULL,
    message VARCHAR NOT NULL
);

CREATE VIEW email_event_view AS
    SELECT ev.id, event_date, ed.description, subject, message
    FROM event.email_event ev
    JOIN event.event_description ed ON ev.event_desc = ed.event_desc_id;
GRANT SELECT ON email_event_view TO PUBLIC;

--
-- Domains, Roles, Users, and Permissions
--
CREATE TABLE iris.domain (
    name VARCHAR(15) PRIMARY KEY,
    block CIDR NOT NULL,
    enabled BOOLEAN NOT NULL
);

CREATE TRIGGER domain_notify_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris.domain
    FOR EACH STATEMENT EXECUTE FUNCTION iris.table_notify();

INSERT INTO iris.domain (name, block, enabled) VALUES
    ('any_ipv4', '0.0.0.0/0', true),
    ('any_ipv6', '::0/0', true),
    ('local_ipv6', '::1/128', true);

CREATE TABLE iris.role (
    name VARCHAR(15) PRIMARY KEY,
    enabled BOOLEAN NOT NULL
);

INSERT INTO iris.role (name, enabled) VALUES
    ('administrator', true), ('operator', true);

CREATE TRIGGER role_notify_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris.role
    FOR EACH STATEMENT EXECUTE FUNCTION iris.table_notify();

CREATE TABLE iris.role_domain (
    role VARCHAR(15) NOT NULL REFERENCES iris.role,
    domain VARCHAR(15) NOT NULL REFERENCES iris.domain
);
ALTER TABLE iris.role_domain ADD PRIMARY KEY (role, domain);

CREATE FUNCTION iris.role_domain_notify() RETURNS TRIGGER AS
    $role_domain_notify$
BEGIN
    IF (TG_OP = 'DELETE') THEN
        PERFORM pg_notify('role', OLD.role);
    ELSE
        PERFORM pg_notify('role', NEW.role);
    END IF;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$role_domain_notify$ LANGUAGE plpgsql;

CREATE TRIGGER role_domain_notify_trig
    AFTER INSERT OR DELETE ON iris.role_domain
    FOR EACH ROW EXECUTE FUNCTION iris.role_domain_notify();

INSERT INTO iris.role_domain (role, domain) VALUES
    ('administrator', 'any_ipv4'), ('administrator', 'any_ipv6');

CREATE TABLE iris.user_id (
    name VARCHAR(15) PRIMARY KEY,
    full_name VARCHAR(31) NOT NULL,
    password VARCHAR(64) NOT NULL,
    dn VARCHAR(128) NOT NULL,
    role VARCHAR(15) REFERENCES iris.role,
    enabled BOOLEAN NOT NULL
);

INSERT INTO iris.user_id (name, full_name, password, dn, role, enabled)
    VALUES (
        'admin', 'IRIS Administrator',
        '+vAwDtk/0KGx9k+kIoKFgWWbd3Ku8e/FOHoZoHB65PAuNEiN2muHVavP0fztOi4=',
        '', 'administrator', true
    );

CREATE TRIGGER user_id_notify_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris.user_id
    FOR EACH STATEMENT EXECUTE FUNCTION iris.table_notify();

CREATE VIEW user_id_view AS
    SELECT name, full_name, dn, role, enabled
    FROM iris.user_id;
GRANT SELECT ON user_id_view TO PUBLIC;

CREATE TABLE iris.access_level (
    id INTEGER PRIMARY KEY,
    description VARCHAR NOT NULL
);

INSERT INTO iris.access_level (id, description) VALUES
    (1, 'View'),
    (2, 'Operate'),
    (3, 'Manage'),
    (4, 'Configure');

CREATE TABLE iris.permission (
    name VARCHAR(8) PRIMARY KEY,
    role VARCHAR(15) NOT NULL REFERENCES iris.role ON DELETE CASCADE,
    base_resource VARCHAR(16) NOT NULL REFERENCES iris.resource_type,
    hashtag VARCHAR(16),
    access_level INTEGER NOT NULL REFERENCES iris.access_level,

    CONSTRAINT hashtag_ck CHECK (hashtag ~ '^#[A-Za-z0-9]+$'),
    -- hashtag cannot be applied to "View" access level
    CONSTRAINT hashtag_access_ck CHECK (hashtag IS NULL OR access_level != 1)
);

ALTER TABLE iris.permission
    ADD CONSTRAINT base_resource_ck
        CHECK (iris.resource_is_base(base_resource)) NOT VALID;

CREATE UNIQUE INDEX permission_role_base_resource_hashtag_idx
    ON iris.permission (role, base_resource, COALESCE(hashtag, ''));

INSERT INTO iris.permission (name, role, base_resource, access_level)
VALUES
    ('prm_1', 'administrator', 'action_plan', 4),
    ('prm_2', 'administrator', 'alert_config', 4),
    ('prm_3', 'administrator', 'beacon', 4),
    ('prm_4', 'administrator', 'camera', 4),
    ('prm_5', 'administrator', 'controller', 4),
    ('prm_6', 'administrator', 'detector', 4),
    ('prm_7', 'administrator', 'dms', 4),
    ('prm_8', 'administrator', 'gate_arm', 4),
    ('prm_9', 'administrator', 'incident', 4),
    ('prm_10', 'administrator', 'lcs', 4),
    ('prm_11', 'administrator', 'parking_area', 4),
    ('prm_12', 'administrator', 'permission', 4),
    ('prm_13', 'administrator', 'ramp_meter', 4),
    ('prm_14', 'administrator', 'system_attribute', 4),
    ('prm_15', 'administrator', 'toll_zone', 4),
    ('prm_16', 'administrator', 'video_monitor', 4),
    ('prm_17', 'administrator', 'weather_sensor', 4);

CREATE TRIGGER permission_notify_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris.permission
    FOR EACH STATEMENT EXECUTE FUNCTION iris.table_notify();

CREATE VIEW permission_view AS
    SELECT name, role, base_resource, hashtag, description AS access_level
    FROM iris.permission p
    JOIN iris.access_level a ON a.id = p.access_level;
GRANT SELECT ON permission_view TO PUBLIC;

CREATE TABLE event.client_event (
    id SERIAL PRIMARY KEY,
    event_date TIMESTAMP WITH time zone DEFAULT NOW() NOT NULL,
    event_desc INTEGER NOT NULL REFERENCES event.event_description,
    host_port VARCHAR(64) NOT NULL,
    user_id VARCHAR(15)
);

CREATE VIEW client_event_view AS
    SELECT ev.id, event_date, ed.description, host_port, user_id
    FROM event.client_event ev
    JOIN event.event_description ed ON ev.event_desc = ed.event_desc_id;
GRANT SELECT ON client_event_view TO PUBLIC;

--
-- Lane Codes, Direction, Road, Map Extent, Geo Loc
--
CREATE TABLE iris.lane_code (
    lcode VARCHAR(1) PRIMARY KEY,
    description VARCHAR(12) NOT NULL
);

COPY iris.lane_code (lcode, description) FROM stdin;
	Mainline
A	Auxiliary
B	Bypass
C	CD Lane
D	Shoulder
G	Green
H	HOV
K	Parking
M	Merge
O	Omnibus
P	Passage
Q	Queue
R	Reversible
T	HOT
V	Velocity
X	Exit
Y	Wrong Way
\.

CREATE VIEW lane_code_view AS
    SELECT lcode, description FROM iris.lane_code;
GRANT SELECT ON lane_code_view TO PUBLIC;

CREATE TABLE iris.direction (
    id SMALLINT PRIMARY KEY,
    direction VARCHAR(4) NOT NULL,
    dir VARCHAR(4) NOT NULL
);

COPY iris.direction (id, direction, dir) FROM stdin;
0		
1	NB	N
2	SB	S
3	EB	E
4	WB	W
5	N-S	NS
6	E-W	EW
\.

CREATE TABLE iris.road_class (
    id INTEGER PRIMARY KEY,
    description VARCHAR(12) NOT NULL,
    grade CHAR NOT NULL,
    scale REAL NOT NULL
);

COPY iris.road_class (id, description, grade, scale) FROM stdin;
0			1
1	residential	A	2
2	business	B	3
3	collector	C	3
4	arterial	D	4
5	expressway	E	4
6	freeway	F	6
7	CD road		3.5
\.

CREATE TABLE iris.road_modifier (
    id SMALLINT PRIMARY KEY,
    modifier TEXT NOT NULL,
    mod VARCHAR(2) NOT NULL
);

COPY iris.road_modifier (id, modifier, mod) FROM stdin;
0	@	
1	N of	N
2	S of	S
3	E of	E
4	W of	W
5	N Jct	Nj
6	S Jct	Sj
7	E Jct	Ej
8	W Jct	Wj
\.

CREATE TABLE iris.road (
    name VARCHAR(20) PRIMARY KEY,
    abbrev VARCHAR(6) NOT NULL,
    r_class SMALLINT NOT NULL REFERENCES iris.road_class(id),
    direction SMALLINT NOT NULL REFERENCES iris.direction(id)
);

CREATE FUNCTION iris.road_notify() RETURNS TRIGGER AS
    $road_notify$
BEGIN
    IF (TG_OP = 'DELETE') THEN
        PERFORM pg_notify('road', OLD.name);
    ELSE
        PERFORM pg_notify('road', NEW.name);
    END IF;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$road_notify$ LANGUAGE plpgsql;

CREATE TRIGGER road_notify_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris.road
    FOR EACH STATEMENT EXECUTE FUNCTION iris.road_notify();

CREATE VIEW road_view AS
    SELECT name, abbrev, rcl.description AS r_class, dir.direction
    FROM iris.road r
    LEFT JOIN iris.road_class rcl ON r.r_class = rcl.id
    LEFT JOIN iris.direction dir ON r.direction = dir.id;
GRANT SELECT ON road_view TO PUBLIC;

CREATE TABLE iris.map_extent (
    name VARCHAR(20) PRIMARY KEY,
    lat real NOT NULL,
    lon real NOT NULL,
    zoom INTEGER NOT NULL
);

CREATE TABLE iris.geo_loc (
    name VARCHAR(20) PRIMARY KEY,
    resource_n VARCHAR(16) NOT NULL REFERENCES iris.resource_type,
    roadway VARCHAR(20) REFERENCES iris.road(name),
    road_dir SMALLINT NOT NULL REFERENCES iris.direction(id),
    cross_street VARCHAR(20) REFERENCES iris.road(name),
    cross_dir SMALLINT NOT NULL REFERENCES iris.direction(id),
    cross_mod SMALLINT NOT NULL REFERENCES iris.road_modifier(id),
    landmark VARCHAR(24),
    lat double precision,
    lon double precision
);

CREATE FUNCTION iris.geo_loc_notify() RETURNS TRIGGER AS
    $geo_loc_notify$
BEGIN
    IF (NEW.roadway IS DISTINCT FROM OLD.roadway) OR
       (NEW.road_dir IS DISTINCT FROM OLD.road_dir) OR
       (NEW.cross_street IS DISTINCT FROM OLD.cross_street) OR
       (NEW.cross_dir IS DISTINCT FROM OLD.cross_dir) OR
       (NEW.cross_mod IS DISTINCT FROM OLD.cross_mod) OR
       (NEW.landmark IS DISTINCT FROM OLD.landmark)
    THEN
        PERFORM pg_notify(NEW.resource_n, '');
    ELSIF (NEW.lat IS DISTINCT FROM OLD.lat) OR
          (NEW.lon IS DISTINCT FROM OLD.lon)
    THEN
        PERFORM pg_notify(NEW.resource_n, NEW.name);
    END IF;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$geo_loc_notify$ LANGUAGE plpgsql;

CREATE TRIGGER geo_loc_notify_trig
    AFTER UPDATE ON iris.geo_loc
    FOR EACH ROW EXECUTE FUNCTION iris.geo_loc_notify();

CREATE FUNCTION iris.geo_location(TEXT, TEXT, TEXT, TEXT, TEXT, TEXT)
    RETURNS TEXT AS $geo_location$
DECLARE
    roadway ALIAS FOR $1;
    road_dir ALIAS FOR $2;
    cross_mod ALIAS FOR $3;
    cross_street ALIAS FOR $4;
    cross_dir ALIAS FOR $5;
    landmark ALIAS FOR $6;
    corridor TEXT;
    xloc TEXT;
    lmrk TEXT;
BEGIN
    corridor = trim(roadway || concat(' ', road_dir));
    xloc = trim(concat(cross_mod, ' ') || cross_street
        || concat(' ', cross_dir));
    lmrk = replace('(' || landmark || ')', '()', '');
    RETURN NULLIF(trim(concat(corridor, ' ' || xloc, ' ' || lmrk)), '');
END;
$geo_location$ LANGUAGE plpgsql;

CREATE VIEW geo_loc_view AS
    SELECT l.name, r.abbrev AS rd, l.roadway, r_dir.direction AS road_dir,
           r_dir.dir AS rdir, m.modifier AS cross_mod, m.mod AS xmod,
           c.abbrev as xst, l.cross_street, c_dir.direction AS cross_dir,
           l.landmark, l.lat, l.lon,
           trim(l.roadway || concat(' ', r_dir.direction)) AS corridor,
           iris.geo_location(l.roadway, r_dir.direction, m.modifier,
           l.cross_street, c_dir.direction, l.landmark) AS location
    FROM iris.geo_loc l
    LEFT JOIN iris.road r ON l.roadway = r.name
    LEFT JOIN iris.road_modifier m ON l.cross_mod = m.id
    LEFT JOIN iris.road c ON l.cross_street = c.name
    LEFT JOIN iris.direction r_dir ON l.road_dir = r_dir.id
    LEFT JOIN iris.direction c_dir ON l.cross_dir = c_dir.id;
GRANT SELECT ON geo_loc_view TO PUBLIC;

--
-- Incidents, descriptors, locators, advice, road affix
--
CREATE TABLE event.incident_detail (
    name VARCHAR(8) PRIMARY KEY,
    description VARCHAR(32) NOT NULL
);

INSERT INTO event.incident_detail (name, description)
VALUES
    ('animal', 'Animal on Road'),
    ('debris', 'Debris'),
    ('detour', 'Detour'),
    ('emrg_veh', 'Emergency Vehicles'),
    ('event', 'Event Congestion'),
    ('flooding', 'Flash Flooding'),
    ('gr_fire', 'Grass Fire'),
    ('ice', 'Ice'),
    ('jacknife', 'Jacknifed Trailer'),
    ('pavement', 'Pavement Failure'),
    ('ped', 'Pedestrian'),
    ('rollover', 'Rollover'),
    ('sgnl_out', 'Traffic Lights Out'),
    ('snow_rmv', 'Snow Removal'),
    ('spill', 'Spilled Load'),
    ('spin_out', 'Vehicle Spin Out'),
    ('test', 'Test Incident'),
    ('veh_fire', 'Vehicle Fire');

CREATE TRIGGER incident_detail_notify_trig
    AFTER INSERT OR UPDATE OR DELETE ON event.incident_detail
    FOR EACH STATEMENT EXECUTE FUNCTION iris.table_notify();

CREATE TABLE event.incident (
    id SERIAL PRIMARY KEY,
    name VARCHAR(16) NOT NULL UNIQUE,
    replaces VARCHAR(16) REFERENCES event.incident(name),
    event_date TIMESTAMP WITH time zone DEFAULT NOW() NOT NULL,
    event_desc INTEGER NOT NULL REFERENCES event.event_description,
    detail VARCHAR(8) REFERENCES event.incident_detail(name),
    lane_code VARCHAR(1) NOT NULL REFERENCES iris.lane_code,
    road VARCHAR(20) NOT NULL,
    dir SMALLINT NOT NULL REFERENCES iris.direction(id),
    lat double precision NOT NULL,
    lon double precision NOT NULL,
    camera VARCHAR(20),
    impact VARCHAR(20) NOT NULL,
    cleared BOOLEAN NOT NULL,
    confirmed BOOLEAN NOT NULL,
    user_id VARCHAR(15),

    CONSTRAINT impact_ck CHECK (impact ~ '^[!?\.]*$')
);

CREATE TRIGGER incident_notify_trig
    AFTER INSERT OR UPDATE OR DELETE ON event.incident
    FOR EACH STATEMENT EXECUTE FUNCTION iris.table_notify();

CREATE FUNCTION event.incident_blocked_lanes(TEXT)
    RETURNS INTEGER AS $incident_blocked_lanes$
DECLARE
    impact ALIAS FOR $1;
    imp TEXT;
    lanes INTEGER;
BEGIN
    lanes = length(impact) - 2;
    IF lanes > 0 THEN
        imp = substring(impact FROM 2 FOR lanes);
        RETURN lanes - length(replace(imp, '!', ''));
    ELSE
        RETURN 0;
    END IF;
END;
$incident_blocked_lanes$ LANGUAGE plpgsql;

CREATE FUNCTION event.incident_blocked_shoulders(TEXT)
    RETURNS INTEGER AS $incident_blocked_shoulders$
DECLARE
    impact ALIAS FOR $1;
    len INTEGER;
    imp TEXT;
BEGIN
    len = length(impact);
    IF len > 2 THEN
        imp = substring(impact FROM 1 FOR 1) ||
              substring(impact FROM len FOR 1);
        RETURN 2 - length(replace(imp, '!', ''));
    ELSE
        RETURN 0;
    END IF;
END;
$incident_blocked_shoulders$ LANGUAGE plpgsql;

CREATE VIEW incident_view AS
    SELECT i.id, name, event_date, ed.description, road, d.direction,
           impact, event.incident_blocked_lanes(impact) AS blocked_lanes,
           event.incident_blocked_shoulders(impact) AS blocked_shoulders,
           cleared, confirmed, user_id, camera, lc.description AS lane_type,
           detail, replaces, lat, lon
    FROM event.incident i
    LEFT JOIN event.event_description ed ON i.event_desc = ed.event_desc_id
    LEFT JOIN iris.direction d ON i.dir = d.id
    LEFT JOIN iris.lane_code lc ON i.lane_code = lc.lcode;
GRANT SELECT ON incident_view TO PUBLIC;

CREATE TABLE event.incident_update (
    id SERIAL PRIMARY KEY,
    incident VARCHAR(16) NOT NULL REFERENCES event.incident(name),
    event_date TIMESTAMP WITH time zone DEFAULT NOW() NOT NULL,
    impact VARCHAR(20) NOT NULL,
    cleared BOOLEAN NOT NULL,
    confirmed BOOLEAN NOT NULL,
    user_id VARCHAR(15)
);

CREATE FUNCTION event.incident_update_trig() RETURNS TRIGGER AS
$incident_update_trig$
BEGIN
    IF (NEW.impact IS DISTINCT FROM OLD.impact) OR
       (NEW.cleared IS DISTINCT FROM OLD.cleared)
    THEN
        INSERT INTO event.incident_update (
            incident, event_date, impact, cleared, confirmed, user_id
        ) VALUES (
            NEW.name, now(), NEW.impact, NEW.cleared, NEW.confirmed, NEW.user_id
        );
    END IF;
    RETURN NEW;
END;
$incident_update_trig$ LANGUAGE plpgsql;

CREATE TRIGGER incident_update_trigger
    AFTER INSERT OR UPDATE ON event.incident
    FOR EACH ROW EXECUTE FUNCTION event.incident_update_trig();

CREATE VIEW incident_update_view AS
    SELECT iu.id, name, iu.event_date, ed.description, road,
           d.direction, iu.impact, iu.cleared, iu.confirmed, iu.user_id,
           camera, lc.description AS lane_type, detail, replaces, lat, lon
    FROM event.incident i
    JOIN event.incident_update iu ON i.name = iu.incident
    LEFT JOIN event.event_description ed ON i.event_desc = ed.event_desc_id
    LEFT JOIN iris.direction d ON i.dir = d.id
    LEFT JOIN iris.lane_code lc ON i.lane_code = lc.lcode;
GRANT SELECT ON incident_update_view TO PUBLIC;

CREATE TABLE iris.inc_descriptor (
    name VARCHAR(10) PRIMARY KEY,
    event_desc_id INTEGER NOT NULL
        REFERENCES event.event_description(event_desc_id),
    detail VARCHAR(8) REFERENCES event.incident_detail(name),
    lane_code VARCHAR(1) NOT NULL REFERENCES iris.lane_code,
    multi VARCHAR(64) NOT NULL
);

CREATE FUNCTION iris.inc_descriptor_ck() RETURNS TRIGGER AS
    $inc_descriptor_ck$
BEGIN
    -- Only incident event IDs are allowed
    IF NEW.event_desc_id < 21 OR NEW.event_desc_id > 24 THEN
        RAISE EXCEPTION 'invalid incident event_desc_id';
    END IF;
    -- Only mainline, cd road, merge and exit lane types are allowed
    IF NEW.lane_code != '' AND NEW.lane_code != 'C' AND
       NEW.lane_code != 'M' AND NEW.lane_code != 'X' THEN
        RAISE EXCEPTION 'invalid incident lane_code';
    END IF;
    RETURN NEW;
END;
$inc_descriptor_ck$ LANGUAGE plpgsql;

CREATE TRIGGER inc_descriptor_ck_trig
    BEFORE INSERT OR UPDATE ON iris.inc_descriptor
    FOR EACH ROW EXECUTE FUNCTION iris.inc_descriptor_ck();

CREATE TRIGGER inc_descriptor_notify_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris.inc_descriptor
    FOR EACH STATEMENT EXECUTE FUNCTION iris.table_notify();

CREATE VIEW inc_descriptor_view AS
    SELECT id.name, ed.description AS event_description, detail,
           lc.description AS lane_type, multi
    FROM iris.inc_descriptor id
    JOIN event.event_description ed ON id.event_desc_id = ed.event_desc_id
    LEFT JOIN iris.lane_code lc ON id.lane_code = lc.lcode;
GRANT SELECT ON inc_descriptor_view TO PUBLIC;

COPY iris.inc_descriptor (name, event_desc_id, detail, lane_code, multi) FROM stdin;
idsc_00001	21	\N		CRASH
idsc_00002	21	\N	X	CRASH ON EXIT
idsc_00003	22	\N		STALLED VEHICLE
idsc_00004	23	\N		INCIDENT
idsc_00005	23	animal		ANIMAL ON ROAD
idsc_00006	23	debris		DEBRIS ON ROAD
idsc_00007	23	emrg_veh		EMERGENCY VEHICLES
idsc_00008	23	event		EVENT CONGESTION
idsc_00009	23	event	X	CONGESTION ON RAMP
idsc_00010	23	flooding		FLASH FLOODING
idsc_00011	23	gr_fire		GRASS FIRE
idsc_00012	23	ice		ICE
idsc_00013	23	pavement		PAVEMENT FAILURE
idsc_00014	23	ped		PEDESTRIAN ON ROAD
idsc_00015	23	rollover		CRASH
idsc_00016	23	snow_rmv		SNOW REMOVAL
idsc_00017	23	spin_out		CRASH
idsc_00018	23	spin_out	X	CRASH ON EXIT
idsc_00019	23	test		TEST
idsc_00020	23	veh_fire		VEHICLE FIRE
idsc_00021	24	\N		ROAD WORK
idsc_00022	24	\N	X	ROAD WORK ON RAMP
\.

CREATE TABLE iris.inc_impact (
    id INTEGER PRIMARY KEY,
    description VARCHAR(24) NOT NULL
);

COPY iris.inc_impact (id, description) FROM stdin;
0	lanes blocked
1	left lanes blocked
2	right lanes blocked
3	center lanes blocked
4	lanes affected
5	left lanes affected
6	right lanes affected
7	center lanes affected
8	both shoulders blocked
9	left shoulder blocked
10	right shoulder blocked
11	both shoulders affected
12	left shoulder affected
13	right shoulder affected
14	free flowing
\.

CREATE TABLE iris.inc_range (
    id INTEGER PRIMARY KEY,
    description VARCHAR(10) NOT NULL
);

COPY iris.inc_range (id, description) FROM stdin;
0	ahead
1	near
2	middle
3	far
\.

CREATE TABLE iris.inc_locator (
    name VARCHAR(10) PRIMARY KEY,
    range INTEGER NOT NULL REFERENCES iris.inc_range(id),
    branched BOOLEAN NOT NULL,
    picked BOOLEAN NOT NULL,
    multi VARCHAR(64) NOT NULL
);

CREATE TRIGGER inc_locator_notify_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris.inc_locator
    FOR EACH STATEMENT EXECUTE FUNCTION iris.table_notify();

CREATE VIEW inc_locator_view AS
    SELECT il.name, rng.description AS range, branched, picked, multi
    FROM iris.inc_locator il
    LEFT JOIN iris.inc_range rng ON il.range = rng.id;
GRANT SELECT ON inc_locator_view TO PUBLIC;

COPY iris.inc_locator (name, range, branched, picked, multi) FROM stdin;
iloc_00001	0	f	f	AHEAD
iloc_00002	0	f	t	AHEAD
iloc_00003	0	t	f	AHEAD
iloc_00004	0	t	t	AHEAD
iloc_00005	1	f	f	[locmi] MILES AHEAD
iloc_00006	1	f	t	[locmd] [locxn]
iloc_00007	1	t	f	ON [locrn] [locrd]
iloc_00008	1	t	t	ON [locrn] [locrd]
iloc_00009	2	f	f	[locmi] MILES AHEAD
iloc_00010	2	f	t	[locmd] [locxn]
iloc_00011	2	t	f	ON [locrn] [locrd]
iloc_00012	2	t	t	ON [locrn] [locrd] [locmd] [locxn]
iloc_00013	3	f	f	[locmi] MILES AHEAD
iloc_00014	3	f	t	[locmd] [locxn]
iloc_00015	3	t	f	ON [locrn] [locrd]
iloc_00016	3	t	t	ON [locrn] [locrd] [locmd] [locxn]
\.

CREATE TABLE iris.inc_advice (
    name VARCHAR(10) PRIMARY KEY,
    impact INTEGER NOT NULL REFERENCES iris.inc_impact(id),
    open_lanes INTEGER,
    impacted_lanes INTEGER,
    range INTEGER NOT NULL REFERENCES iris.inc_range(id),
    lane_code VARCHAR(1) NOT NULL REFERENCES iris.lane_code,
    multi VARCHAR(64) NOT NULL
);

CREATE FUNCTION iris.inc_advice_ck() RETURNS TRIGGER AS
    $inc_advice_ck$
BEGIN
    -- Only mainline, cd road, merge and exit lane codes are allowed
    IF NEW.lane_code != '' AND NEW.lane_code != 'C' AND
       NEW.lane_code != 'M' AND NEW.lane_code != 'X' THEN
        RAISE EXCEPTION 'invalid incident lane_code';
    END IF;
    RETURN NEW;
END;
$inc_advice_ck$ LANGUAGE plpgsql;

CREATE TRIGGER inc_advice_ck_trig
    BEFORE INSERT OR UPDATE ON iris.inc_advice
    FOR EACH ROW EXECUTE FUNCTION iris.inc_advice_ck();

CREATE TRIGGER inc_advice_notify_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris.inc_advice
    FOR EACH STATEMENT EXECUTE FUNCTION iris.table_notify();

CREATE VIEW inc_advice_view AS
    SELECT a.name, imp.description AS impact, lc.description AS lane_type,
           rng.description AS range, open_lanes, impacted_lanes, multi
    FROM iris.inc_advice a
    LEFT JOIN iris.inc_impact imp ON a.impact = imp.id
    LEFT JOIN iris.inc_range rng ON a.range = rng.id
    LEFT JOIN iris.lane_code lc ON a.lane_code = lc.lcode;
GRANT SELECT ON inc_advice_view TO PUBLIC;

COPY iris.inc_advice (name, impact, lane_code, range, open_lanes, impacted_lanes, multi) FROM stdin;
iadv_00001	0		0	0	\N	ROAD CLOSED
iadv_00002	0		0	\N	\N	LANES CLOSED
iadv_00003	0		1	0	\N	ROAD CLOSED
iadv_00004	0		1	\N	\N	LANES CLOSED
iadv_00005	0		2	\N	1	1 LANE CLOSED
iadv_00006	0		2	\N	2	2 LANES CLOSED
iadv_00007	0		2	\N	3	3 LANES CLOSED
iadv_00008	0		2	0	\N	ROAD CLOSED
iadv_00009	0		2	1	\N	SINGLE LANE
iadv_00010	0		2	\N	\N	LANES CLOSED
iadv_00011	0		3	\N	1	1 LANE CLOSED
iadv_00012	0		3	\N	2	2 LANES CLOSED
iadv_00013	0		3	\N	3	3 LANES CLOSED
iadv_00014	0		3	0	\N	ROAD CLOSED
iadv_00015	0		3	1	\N	SINGLE LANE
iadv_00016	0		3	\N	\N	LANES CLOSED
iadv_00017	1		0	\N	1	LEFT LANE CLOSED
iadv_00018	1		0	\N	2	LEFT 2 LANES CLOSED
iadv_00019	1		0	\N	3	LEFT 3 LANES CLOSED
iadv_00020	1		0	\N	\N	LEFT LANES CLOSED
iadv_00021	1		1	\N	1	LEFT LANE CLOSED
iadv_00022	1		1	\N	2	LEFT 2 LANES CLOSED
iadv_00023	1		1	\N	3	LEFT 3 LANES CLOSED
iadv_00024	1		1	\N	\N	LEFT LANES CLOSED
iadv_00025	1		2	\N	1	LANE CLOSED
iadv_00026	1		2	\N	2	2 LANES CLOSED
iadv_00027	1		2	\N	3	3 LANES CLOSED
iadv_00028	1		2	1	\N	SINGLE LANE
iadv_00029	1		2	\N	\N	LANES CLOSED
iadv_00030	1		3	\N	1	LANE CLOSED
iadv_00031	1		3	\N	2	2 LANES CLOSED
iadv_00032	1		3	\N	3	3 LANES CLOSED
iadv_00033	1		3	1	\N	SINGLE LANE
iadv_00034	1		3	\N	\N	LANES CLOSED
iadv_00035	2		0	\N	1	RIGHT LANE CLOSED
iadv_00036	2		0	\N	2	RIGHT 2 LANES CLOSED
iadv_00037	2		0	\N	3	RIGHT 3 LANES CLOSED
iadv_00038	2		0	\N	\N	RIGHT LANES CLOSED
iadv_00039	2		1	\N	1	RIGHT LANE CLOSED
iadv_00040	2		1	\N	2	RIGHT 2 LANES CLOSED
iadv_00041	2		1	\N	3	RIGHT 3 LANES CLOSED
iadv_00042	2		1	\N	\N	RIGHT LANES CLOSED
iadv_00043	2		2	\N	1	LANE CLOSED
iadv_00044	2		2	\N	2	2 LANES CLOSED
iadv_00045	2		2	\N	3	3 LANES CLOSED
iadv_00046	2		2	1	\N	SINGLE LANE
iadv_00047	2		2	\N	\N	LANES CLOSED
iadv_00048	2		3	\N	1	LANE CLOSED
iadv_00049	2		3	\N	2	2 LANES CLOSED
iadv_00050	2		3	\N	3	3 LANES CLOSED
iadv_00051	2		3	1	\N	SINGLE LANE
iadv_00052	2		3	\N	\N	LANES CLOSED
iadv_00053	3		0	\N	1	CENTER LANE CLOSED
iadv_00054	3		0	\N	\N	CENTER LANES CLOSED
iadv_00055	3		1	\N	1	CENTER LANE CLOSED
iadv_00056	3		1	\N	\N	CENTER LANES CLOSED
iadv_00057	3		2	\N	1	LANE CLOSED
iadv_00058	3		2	\N	2	2 LANES CLOSED
iadv_00059	3		2	\N	3	3 LANES CLOSED
iadv_00060	3		2	\N	\N	LANES CLOSED
iadv_00061	3		3	\N	1	LANE CLOSED
iadv_00062	3		3	\N	2	2 LANES CLOSED
iadv_00063	3		3	\N	3	3 LANES CLOSED
iadv_00064	3		3	\N	\N	LANES CLOSED
iadv_00065	4		0	0	1	IN LANE
iadv_00066	4		0	0	2	IN BOTH LANES
iadv_00067	4		0	0	\N	IN ALL LANES
iadv_00068	4		1	0	1	IN LANE
iadv_00069	4		1	0	2	IN BOTH LANES
iadv_00070	4		1	0	\N	IN ALL LANES
iadv_00071	5		0	\N	1	IN LEFT LANE
iadv_00072	5		0	\N	2	IN LEFT 2 LANES
iadv_00073	5		0	\N	3	IN LEFT 3 LANES
iadv_00074	5		0	\N	4	IN LEFT 4 LANES
iadv_00075	5		0	\N	\N	IN LEFT LANES
iadv_00076	5		1	\N	1	IN LEFT LANE
iadv_00077	5		1	\N	2	IN LEFT 2 LANES
iadv_00078	5		1	\N	3	IN LEFT 3 LANES
iadv_00079	5		1	\N	4	IN LEFT 4 LANES
iadv_00080	5		1	\N	\N	IN LEFT LANES
iadv_00081	6		0	\N	1	IN RIGHT LANE
iadv_00082	6		0	\N	2	IN RIGHT 2 LANES
iadv_00083	6		0	\N	3	IN RIGHT 3 LANES
iadv_00084	6		0	\N	4	IN RIGHT 4 LANES
iadv_00085	6		0	\N	\N	IN RIGHT LANES
iadv_00086	6		1	\N	1	IN RIGHT LANE
iadv_00087	6		1	\N	2	IN RIGHT 2 LANES
iadv_00088	6		1	\N	3	IN RIGHT 3 LANES
iadv_00089	6		1	\N	4	IN RIGHT 4 LANES
iadv_00090	6		1	\N	\N	IN RIGHT LANES
iadv_00091	7		0	\N	1	IN CENTER LANE
iadv_00092	7		0	\N	\N	IN CENTER LANES
iadv_00093	7		1	\N	1	IN CENTER LANE
iadv_00094	7		1	\N	\N	IN CENTER LANES
iadv_00095	8		0	\N	\N	ON BOTH SHOULDERS
iadv_00096	8		1	\N	\N	ON BOTH SHOULDERS
iadv_00097	9		0	\N	\N	ON LEFT SHOULDER
iadv_00098	9		1	\N	\N	ON LEFT SHOULDER
iadv_00099	10		0	\N	\N	ON RIGHT SHOULDER
iadv_00100	10		1	\N	\N	ON RIGHT SHOULDER
iadv_00101	11		0	\N	\N	IN BOTH SHOULDERS
iadv_00102	11		1	\N	\N	IN BOTH SHOULDERS
iadv_00103	12		0	\N	\N	IN LEFT SHOULDER
iadv_00104	12		1	\N	\N	IN LEFT SHOULDER
iadv_00105	13		0	\N	\N	IN RIGHT SHOULDER
iadv_00106	13		1	\N	\N	IN RIGHT SHOULDER
\.

CREATE TABLE iris.road_affix (
    name VARCHAR(12) PRIMARY KEY,
    prefix BOOLEAN NOT NULL,
    fixup VARCHAR(12),
    allow_retain BOOLEAN NOT NULL
);

COPY iris.road_affix (name, prefix, fixup, allow_retain) FROM stdin;
C.S.A.H.	t	CTY	f
CO RD	t	CTY	f
I-	t		f
U.S.	t	HWY	f
T.H.	t	HWY	f
AVE	f		t
BLVD	f		f
CIR	f		f
DR	f		t
HWY	f		f
LN	f		f
PKWY	f		f
PL	f		f
RD	f		t
ST	f		t
TR	f		f
WAY	f		f
\.

CREATE TRIGGER road_affix_notify_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris.road_affix
    FOR EACH STATEMENT EXECUTE FUNCTION iris.table_notify();

--
-- Comm Protocols, Comm Links, Modems, Cabinets, Controllers
--
CREATE TABLE iris.comm_protocol (
    id SMALLINT PRIMARY KEY,
    description VARCHAR(20) NOT NULL
);

INSERT INTO iris.comm_protocol (id, description)
VALUES
    (0, 'NTCIP Class B'),
    (1, 'MnDOT 170 (4-bit)'),
    (2, 'MnDOT 170 (5-bit)'),
    (3, 'SmartSensor 105'),
    (4, 'Canoga'),
    (5, 'Pelco P'),
    (6, 'Pelco D PTZ'),
    (7, 'NTCIP Class C'),
    (8, 'Manchester PTZ'),
    (9, 'DMS XML'),
    (10, 'MSG_FEED'),
    (11, 'NTCIP Class A'),
    (12, 'Banner DXM'),
    (13, 'Vicon PTZ'),
    (14, 'SmartSensor 125 HD'),
    (15, 'OSi ORG-815'),
    (16, 'Infinova D PTZ'),
    (17, 'RTMS G4'),
    (18, 'RTMS G4 vlog'),
    (19, 'SmartSensor 125 vlog'),
    (20, 'Natch'),
    (21, 'Central Park'),
    (22, 'ADEC TDC'),
    (23, 'CAP-NWS Feed'),
    (24, 'NDOT Beacon'),
    (25, 'DLI DIN Relay'),
    (26, 'Axis 292'),
    (27, 'Axis PTZ'),
    (28, 'HySecurity STC'),
    (29, 'Cohu PTZ'),
    (30, 'DR-500'),
    (31, 'ADDCO'),
    (32, 'TransCore E6'),
    (33, 'CBW'),
    (34, 'Incident Feed'),
    (35, 'MonStream'),
    (36, 'Gate NDORv5'),
    (37, 'GPS TAIP'),
    (38, 'SierraGX'),
    (39, 'GPS RedLion'),
    (40, 'Cohu Helios PTZ'),
    (41, 'Streambed'),
    (42, 'CAP-IPAWS Feed'),
    (43, 'ClearGuide'),
    (44, 'GPS Digi WR'),
    (45, 'ONVIF PTZ'),
    (46, 'Sierra SSH GPS');

CREATE TABLE iris.comm_config (
    name VARCHAR(10) PRIMARY KEY,
    description VARCHAR(20) NOT NULL UNIQUE,
    protocol SMALLINT NOT NULL REFERENCES iris.comm_protocol(id),
    timeout_ms INTEGER NOT NULL,
    retry_threshold INTEGER NOT NULL,
    poll_period_sec INTEGER NOT NULL,
    long_poll_period_sec INTEGER NOT NULL,
    idle_disconnect_sec INTEGER NOT NULL,
    no_response_disconnect_sec INTEGER NOT NULL
);

ALTER TABLE iris.comm_config
    ADD CONSTRAINT retry_threshold_ck
    CHECK (retry_threshold >= 0 AND retry_threshold <= 8);

ALTER TABLE iris.comm_config
    ADD CONSTRAINT poll_period_ck
    CHECK (poll_period_sec >= 5 AND long_poll_period_sec >= poll_period_sec);

INSERT INTO iris.comm_config (
    name, description, protocol, timeout_ms, retry_threshold, poll_period_sec,
    long_poll_period_sec, idle_disconnect_sec, no_response_disconnect_sec
) VALUES (
     'cfg_0', 'NTCIP udp', 11, 1000, 3, 30, 300, 0, 0
);

CREATE TRIGGER comm_config_notify_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris.comm_config
    FOR EACH STATEMENT EXECUTE FUNCTION iris.table_notify();

CREATE VIEW comm_config_view AS
    SELECT cc.name, cc.description, cp.description AS protocol,
           timeout_ms, retry_threshold, poll_period_sec, long_poll_period_sec,
           idle_disconnect_sec, no_response_disconnect_sec
    FROM iris.comm_config cc
    JOIN iris.comm_protocol cp ON cc.protocol = cp.id;
GRANT SELECT ON comm_config_view TO PUBLIC;

CREATE TABLE iris.comm_link (
    name VARCHAR(20) PRIMARY KEY,
    description VARCHAR(32) NOT NULL,
    uri VARCHAR(256) NOT NULL,
    poll_enabled BOOLEAN NOT NULL,
    comm_config VARCHAR(10) NOT NULL REFERENCES iris.comm_config,
    connected BOOLEAN NOT NULL
);

CREATE FUNCTION iris.comm_link_notify() RETURNS TRIGGER AS
    $comm_link_notify$
BEGIN
    -- all attributes are primary
    NOTIFY comm_link;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$comm_link_notify$ LANGUAGE plpgsql;

CREATE TRIGGER comm_link_notify_trig
    AFTER UPDATE ON iris.comm_link
    FOR EACH ROW EXECUTE FUNCTION iris.comm_link_notify();

CREATE TRIGGER comm_link_table_notify_trig
    AFTER INSERT OR DELETE ON iris.comm_link
    FOR EACH STATEMENT EXECUTE FUNCTION iris.table_notify();

CREATE VIEW comm_link_view AS
    SELECT cl.name, cl.description, uri, poll_enabled,
           cp.description AS protocol, cc.description AS comm_config,
           timeout_ms, poll_period_sec, connected
    FROM iris.comm_link cl
    JOIN iris.comm_config cc ON cl.comm_config = cc.name
    JOIN iris.comm_protocol cp ON cc.protocol = cp.id;
GRANT SELECT ON comm_link_view TO PUBLIC;

CREATE TABLE iris.modem (
    name VARCHAR(20) PRIMARY KEY,
    uri VARCHAR(64) NOT NULL,
    config VARCHAR(64) NOT NULL,
    timeout_ms INTEGER NOT NULL,
    enabled BOOLEAN NOT NULL
);

CREATE TRIGGER modem_notify_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris.modem
    FOR EACH STATEMENT EXECUTE FUNCTION iris.table_notify();

CREATE VIEW modem_view AS
    SELECT name, uri, config, timeout_ms, enabled
    FROM iris.modem;
GRANT SELECT ON modem_view TO PUBLIC;

CREATE TABLE iris.cabinet_style (
    name VARCHAR(20) PRIMARY KEY,
    police_panel_pin_1 INTEGER,
    police_panel_pin_2 INTEGER,
    watchdog_reset_pin_1 INTEGER,
    watchdog_reset_pin_2 INTEGER,
    dip INTEGER
);

CREATE TRIGGER cabinet_style_notify_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris.cabinet_style
    FOR EACH STATEMENT EXECUTE FUNCTION iris.table_notify();

CREATE VIEW cabinet_style_view AS
    SELECT name, police_panel_pin_1, police_panel_pin_2,
           watchdog_reset_pin_1, watchdog_reset_pin_2, dip
    FROM iris.cabinet_style;
GRANT SELECT ON cabinet_style_view TO PUBLIC;

CREATE TABLE iris.condition (
    id INTEGER PRIMARY KEY,
    description VARCHAR(12) NOT NULL
);

INSERT INTO iris.condition (id, description)
VALUES
    (0, 'Planned'),
    (1, 'Active'),
    (2, 'Construction'),
    (3, 'Removed'),
    (4, 'Testing'),
    (5, 'Maintenance');

CREATE TABLE iris.controller (
    name VARCHAR(20) PRIMARY KEY,
    drop_id SMALLINT NOT NULL,
    comm_link VARCHAR(20) REFERENCES iris.comm_link(name),
    cabinet_style VARCHAR(20) REFERENCES iris.cabinet_style(name),
    geo_loc VARCHAR(20) NOT NULL REFERENCES iris.geo_loc(name),
    condition INTEGER NOT NULL REFERENCES iris.condition,
    notes VARCHAR CHECK (LENGTH(notes) < 256),
    password VARCHAR CHECK (LENGTH(password) < 128),
    setup JSONB,
    status JSONB,
    fail_time TIMESTAMP WITH time zone
);

CREATE UNIQUE INDEX ctrl_link_drop_idx ON iris.controller
    USING btree (comm_link, drop_id);

CREATE FUNCTION iris.controller_notify() RETURNS TRIGGER AS
    $controller_notify$
BEGIN
    IF (NEW.drop_id IS DISTINCT FROM OLD.drop_id) OR
       (NEW.comm_link IS DISTINCT FROM OLD.comm_link) OR
       (NEW.cabinet_style IS DISTINCT FROM OLD.cabinet_style) OR
       (NEW.condition IS DISTINCT FROM OLD.condition) OR
       (NEW.notes IS DISTINCT FROM OLD.notes) OR
       (NEW.setup IS DISTINCT FROM OLD.setup) OR
       (NEW.fail_time IS DISTINCT FROM OLD.fail_time)
    THEN
        NOTIFY controller;
    ELSE
        PERFORM pg_notify('controller', NEW.name);
    END IF;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$controller_notify$ LANGUAGE plpgsql;

CREATE TRIGGER controller_notify_trig
    AFTER UPDATE ON iris.controller
    FOR EACH ROW EXECUTE FUNCTION iris.controller_notify();

CREATE TRIGGER controller_table_notify_trig
    AFTER INSERT OR DELETE ON iris.controller
    FOR EACH STATEMENT EXECUTE FUNCTION iris.table_notify();

CREATE VIEW controller_view AS
    SELECT c.name, drop_id, comm_link, cabinet_style, geo_loc,
           cnd.description AS condition, notes, setup, status, fail_time
    FROM iris.controller c
    LEFT JOIN iris.condition cnd ON c.condition = cnd.id;
GRANT SELECT ON controller_view TO PUBLIC;

CREATE VIEW controller_loc_view AS
    SELECT c.name, drop_id, comm_link, cabinet_style, condition, c.notes,
           l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir
    FROM controller_view c
    LEFT JOIN geo_loc_view l ON c.geo_loc = l.name;
GRANT SELECT ON controller_loc_view TO PUBLIC;

CREATE TABLE event.comm_event (
    event_id INTEGER PRIMARY KEY DEFAULT nextval('event.event_id_seq'),
    event_date TIMESTAMP WITH time zone DEFAULT NOW() NOT NULL,
    event_desc_id INTEGER NOT NULL
        REFERENCES event.event_description(event_desc_id),
    controller VARCHAR(20) NOT NULL REFERENCES iris.controller(name)
        ON DELETE CASCADE,
    device_id VARCHAR(20)
);

-- DELETE of iris.controller *very* slow without this index
CREATE INDEX ON event.comm_event (controller);

CREATE VIEW comm_event_view AS
    SELECT e.event_id, e.event_date, ed.description, e.controller,
           c.comm_link, c.drop_id
    FROM event.comm_event e
    JOIN event.event_description ed ON e.event_desc_id = ed.event_desc_id
    LEFT JOIN iris.controller c ON e.controller = c.name;
GRANT SELECT ON comm_event_view TO PUBLIC;

CREATE TABLE iris.controller_io (
    name VARCHAR(20) PRIMARY KEY,
    resource_n VARCHAR(16) NOT NULL REFERENCES iris.resource_type,
    controller VARCHAR(20) REFERENCES iris.controller,
    pin INTEGER NOT NULL,
    UNIQUE (controller, pin)
);

CREATE FUNCTION iris.controller_io_notify() RETURNS TRIGGER AS
    $controller_io_notify$
BEGIN
    -- controller is primary; pin is secondary
    IF (NEW.controller IS DISTINCT FROM OLD.controller) THEN
        PERFORM pg_notify(NEW.resource_n, '');
    ELSIF (NEW.pin IS DISTINCT FROM OLD.pin) THEN
        PERFORM pg_notify(NEW.resource_n, NEW.name);
    END IF;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$controller_io_notify$ LANGUAGE plpgsql;

CREATE TRIGGER controller_io_notify_trig
    AFTER UPDATE ON iris.controller_io
    FOR EACH ROW EXECUTE FUNCTION iris.controller_io_notify();

CREATE FUNCTION iris.controller_io_delete() RETURNS TRIGGER AS
    $controller_io_delete$
BEGIN
    DELETE FROM iris.device_preset WHERE name = OLD.name;
    DELETE FROM iris.controller_io WHERE name = OLD.name;
    IF FOUND THEN
        RETURN OLD;
    ELSE
        RETURN NULL;
    END IF;
END;
$controller_io_delete$ LANGUAGE plpgsql;

CREATE VIEW controller_io_view AS
    SELECT name, resource_n, controller, pin
    FROM iris.controller_io;
GRANT SELECT ON controller_io_view TO PUBLIC;

--
-- R_Node, Detectors
--
CREATE TABLE iris.r_node_type (
    id INTEGER PRIMARY KEY,
    description VARCHAR(12) NOT NULL
);

INSERT INTO iris.r_node_type (id, description)
VALUES
    (0, 'station'),
    (1, 'entrance'),
    (2, 'exit'),
    (3, 'intersection'),
    (4, 'access'),
    (5, 'interchange');

CREATE TABLE iris.r_node_transition (
    id INTEGER PRIMARY KEY,
    description VARCHAR(12) NOT NULL
);

INSERT INTO iris.r_node_transition (id, description)
VALUES
    (0, 'none'),
    (1, 'loop'),
    (2, 'leg'),
    (3, 'slipramp'),
    (4, 'CD'),
    (5, 'HOV'),
    (6, 'common'),
    (7, 'flyover');

CREATE TABLE iris.r_node (
    name VARCHAR(10) PRIMARY KEY,
    geo_loc VARCHAR(20) NOT NULL REFERENCES iris.geo_loc(name),
    ordinal INTEGER UNIQUE,
    node_type INTEGER NOT NULL REFERENCES iris.r_node_type,
    pickable BOOLEAN NOT NULL,
    above BOOLEAN NOT NULL,
    transition INTEGER NOT NULL REFERENCES iris.r_node_transition,
    lanes INTEGER NOT NULL,
    attach_side BOOLEAN NOT NULL,
    shift INTEGER NOT NULL,
    active BOOLEAN NOT NULL,
    station_id VARCHAR(10),
    speed_limit INTEGER NOT NULL,
    notes VARCHAR(160)
);

CREATE UNIQUE INDEX r_node_station_idx ON iris.r_node USING btree (station_id);

CREATE FUNCTION iris.r_node_notify() RETURNS TRIGGER AS
    $r_node_notify$
BEGIN
    IF (TG_OP = 'DELETE') THEN
        PERFORM pg_notify('r_node', OLD.name);
    ELSE
        PERFORM pg_notify('r_node', NEW.name);
    END IF;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$r_node_notify$ LANGUAGE plpgsql;

CREATE TRIGGER r_node_notify_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris.r_node
    FOR EACH ROW EXECUTE FUNCTION iris.r_node_notify();

CREATE FUNCTION iris.r_node_left(INTEGER, INTEGER, BOOLEAN, INTEGER)
    RETURNS INTEGER AS $r_node_left$
DECLARE
    node_type ALIAS FOR $1;
    lanes ALIAS FOR $2;
    attach_side ALIAS FOR $3;
    shift ALIAS FOR $4;
BEGIN
    IF attach_side = TRUE THEN
        RETURN shift;
    END IF;
    IF node_type = 0 THEN
        RETURN shift - lanes;
    END IF;
    RETURN shift;
END;
$r_node_left$ LANGUAGE plpgsql;

CREATE FUNCTION iris.r_node_right(INTEGER, INTEGER, BOOLEAN, INTEGER)
    RETURNS INTEGER AS $r_node_right$
DECLARE
    node_type ALIAS FOR $1;
    lanes ALIAS FOR $2;
    attach_side ALIAS FOR $3;
    shift ALIAS FOR $4;
BEGIN
    IF attach_side = FALSE THEN
        RETURN shift;
    END IF;
    IF node_type = 0 THEN
        RETURN shift + lanes;
    END IF;
    RETURN shift;
END;
$r_node_right$ LANGUAGE plpgsql;

ALTER TABLE iris.r_node ADD CONSTRAINT left_edge_ck
    CHECK (iris.r_node_left(node_type, lanes, attach_side, shift) >= 1);
ALTER TABLE iris.r_node ADD CONSTRAINT right_edge_ck
    CHECK (iris.r_node_right(node_type, lanes, attach_side, shift) <= 9);

CREATE VIEW r_node_view AS
    SELECT n.name, n.geo_loc, n.ordinal,
           l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir,
           l.landmark, l.lat, l.lon, l.corridor, l.location,
           nt.description AS node_type, n.pickable, n.above,
           tr.description AS transition, n.lanes, n.attach_side, n.shift,
           n.active, n.station_id, n.speed_limit, n.notes
    FROM iris.r_node n
    JOIN geo_loc_view l ON n.geo_loc = l.name
    JOIN iris.r_node_type nt ON n.node_type = nt.id
    JOIN iris.r_node_transition tr ON n.transition = tr.id;
GRANT SELECT ON r_node_view TO PUBLIC;

CREATE VIEW roadway_station_view AS
    SELECT station_id, roadway, road_dir, cross_mod, cross_street, active,
           speed_limit
    FROM iris.r_node r, geo_loc_view l
    WHERE r.geo_loc = l.name AND station_id IS NOT NULL;
GRANT SELECT ON roadway_station_view TO PUBLIC;

CREATE TABLE iris._detector (
    name VARCHAR(20) PRIMARY KEY,
    r_node VARCHAR(10) NOT NULL REFERENCES iris.r_node(name),
    lane_code VARCHAR(1) NOT NULL REFERENCES iris.lane_code,
    lane_number SMALLINT NOT NULL,
    abandoned BOOLEAN NOT NULL,
    force_fail BOOLEAN NOT NULL,
    auto_fail BOOLEAN NOT NULL,
    field_length REAL NOT NULL,
    fake VARCHAR(32),
    notes VARCHAR(32)
);

ALTER TABLE iris._detector ADD CONSTRAINT _detector_fkey
    FOREIGN KEY (name) REFERENCES iris.controller_io ON DELETE CASCADE;

CREATE FUNCTION iris.detector_notify() RETURNS TRIGGER AS
    $detector_notify$
BEGIN
    -- lane_code, lane_number and abandoned are secondary, but affect label
    IF (NEW.notes IS DISTINCT FROM OLD.notes) OR
       (NEW.lane_code IS DISTINCT FROM OLD.lane_code) OR
       (NEW.lane_number IS DISTINCT FROM OLD.lane_number) OR
       (NEW.abandoned IS DISTINCT FROM OLD.abandoned)
    THEN
        NOTIFY detector;
    ELSE
        PERFORM pg_notify('detector', NEW.name);
    END IF;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$detector_notify$ LANGUAGE plpgsql;

CREATE TRIGGER detector_notify_trig
    AFTER UPDATE ON iris._detector
    FOR EACH ROW EXECUTE FUNCTION iris.detector_notify();

CREATE TRIGGER detector_table_notify_trig
    AFTER INSERT OR DELETE ON iris._detector
    FOR EACH STATEMENT EXECUTE FUNCTION iris.table_notify();

CREATE VIEW iris.detector AS
    SELECT d.name, controller, pin, r_node, lane_code, lane_number,
           abandoned, force_fail, auto_fail, field_length, fake, notes
    FROM iris._detector d
    JOIN iris.controller_io cio ON d.name = cio.name;

CREATE FUNCTION iris.detector_insert() RETURNS TRIGGER AS
    $detector_insert$
BEGIN
    INSERT INTO iris.controller_io (name, resource_n, controller, pin)
         VALUES (NEW.name, 'detector', NEW.controller, NEW.pin);
    INSERT INTO iris._detector (
        name, r_node, lane_code, lane_number, abandoned, force_fail, auto_fail,
        field_length, fake, notes
    ) VALUES (
        NEW.name, NEW.r_node, NEW.lane_code, NEW.lane_number, NEW.abandoned,
        NEW.force_fail, NEW.auto_fail, NEW.field_length, NEW.fake, NEW.notes
    );
    RETURN NEW;
END;
$detector_insert$ LANGUAGE plpgsql;

CREATE TRIGGER detector_insert_trig
    INSTEAD OF INSERT ON iris.detector
    FOR EACH ROW EXECUTE FUNCTION iris.detector_insert();

CREATE FUNCTION iris.detector_update() RETURNS TRIGGER AS
    $detector_update$
BEGIN
    UPDATE iris.controller_io
       SET controller = NEW.controller,
           pin = NEW.pin
     WHERE name = OLD.name;
    UPDATE iris._detector
       SET r_node = NEW.r_node,
           lane_code = NEW.lane_code,
           lane_number = NEW.lane_number,
           abandoned = NEW.abandoned,
           force_fail = NEW.force_fail,
           auto_fail = NEW.auto_fail,
           field_length = NEW.field_length,
           fake = NEW.fake,
           notes = NEW.notes
     WHERE name = OLD.name;
    RETURN NEW;
END;
$detector_update$ LANGUAGE plpgsql;

CREATE TRIGGER detector_update_trig
    INSTEAD OF UPDATE ON iris.detector
    FOR EACH ROW EXECUTE FUNCTION iris.detector_update();

CREATE TRIGGER detector_delete_trig
    INSTEAD OF DELETE ON iris.detector
    FOR EACH ROW EXECUTE FUNCTION iris.controller_io_delete();

CREATE FUNCTION iris.landmark_abbrev(VARCHAR(24)) RETURNS TEXT
    AS $landmark_abbrev$
DECLARE
    lmrk TEXT;
    lmrk2 TEXT;
BEGIN
    lmrk = initcap($1);
    -- Replace common words
    lmrk = replace(lmrk, 'Of ', '');
    lmrk = replace(lmrk, 'Miles', 'MI');
    lmrk = replace(lmrk, 'Mile', 'MI');
    -- Remove whitespace and non-printable characters
    lmrk = regexp_replace(lmrk, '[^[:graph:]]', '', 'g');
    IF length(lmrk) > 6 THEN
        -- Remove lower-case vowels
        lmrk = regexp_replace(lmrk, '[aeiouy]', '', 'g');
    END IF;
    IF length(lmrk) > 6 THEN
        -- Remove all punctuation
        lmrk = regexp_replace(lmrk, '[[:punct:]]', '', 'g');
    END IF;
    lmrk2 = lmrk;
    IF length(lmrk) > 6 THEN
        -- Remove letters
        lmrk = regexp_replace(lmrk, '[[:alpha:]]', '', 'g');
    END IF;
    IF length(lmrk) > 0 THEN
        RETURN left(lmrk, 6);
    ELSE
        RETURN left(lmrk2, 6);
    END IF;
END;
$landmark_abbrev$ LANGUAGE plpgsql;

-- FIXME: rename to label_base
CREATE FUNCTION iris.root_lbl(rd VARCHAR(6), rdir VARCHAR(4), xst VARCHAR(6),
    xdir VARCHAR(4), xmod VARCHAR(2), lmark VARCHAR(24)) RETURNS TEXT AS
$$
    SELECT rd || '/' || COALESCE(
        xdir || replace(xmod, '@', '') || xst,
        iris.landmark_abbrev(lmark)
    ) || rdir;
$$ LANGUAGE sql SECURITY DEFINER;

ALTER FUNCTION iris.root_lbl(VARCHAR(6), VARCHAR(4), VARCHAR(6), VARCHAR(4),
    VARCHAR(2), VARCHAR(24)
)
    SET search_path = pg_catalog, pg_temp;

CREATE FUNCTION iris.detector_label(TEXT, CHAR, SMALLINT, BOOLEAN) RETURNS TEXT
    AS $detector_label$
DECLARE
    root ALIAS FOR $1;
    lcode ALIAS FOR $2;
    lane_number ALIAS FOR $3;
    abandoned ALIAS FOR $4;
    lnum VARCHAR(2);
    suffix VARCHAR(5);
BEGIN
    lnum = '';
    IF lane_number > 0 THEN
        lnum = TO_CHAR(lane_number, 'FM9');
    END IF;
    suffix = '';
    IF abandoned THEN
        suffix = '-ABND';
    END IF;
    RETURN COALESCE(
        root || lcode || lnum || suffix,
        'FUTURE'
    );
END;
$detector_label$ LANGUAGE plpgsql;

CREATE VIEW detector_label_view AS
    SELECT d.name AS det_id,
           iris.detector_label(
               iris.root_lbl(l.rd, l.rdir, l.xst, l.cross_dir, l.xmod, l.landmark),
               d.lane_code, d.lane_number, d.abandoned
           ) AS label, rnd.geo_loc
    FROM iris._detector d
    LEFT JOIN iris.r_node rnd ON d.r_node = rnd.name
    LEFT JOIN geo_loc_view l ON rnd.geo_loc = l.name;
GRANT SELECT ON detector_label_view TO PUBLIC;

CREATE TABLE event.detector_event (
    event_id INTEGER DEFAULT nextval('event.event_id_seq') NOT NULL,
    event_date TIMESTAMP WITH time zone DEFAULT NOW() NOT NULL,
    event_desc_id INTEGER NOT NULL
        REFERENCES event.event_description(event_desc_id),
    device_id VARCHAR(20) REFERENCES iris._detector(name) ON DELETE CASCADE
);

CREATE VIEW detector_view AS
    SELECT d.name, d.r_node, c.comm_link, c.drop_id, cio.controller, cio.pin,
           dl.label, dl.geo_loc, l.rd || '_' || l.road_dir AS cor_id,
           l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir,
           d.lane_number, d.field_length, lc.description AS lane_type,
           d.lane_code, d.abandoned, d.force_fail, d.auto_fail, c.condition,
           d.fake, d.notes
    FROM iris.detector d
    JOIN iris.controller_io cio ON d.name = cio.name
    LEFT JOIN detector_label_view dl ON d.name = dl.det_id
    LEFT JOIN geo_loc_view l ON dl.geo_loc = l.name
    LEFT JOIN iris.lane_code lc ON d.lane_code = lc.lcode
    LEFT JOIN controller_view c ON cio.controller = c.name;
GRANT SELECT ON detector_view TO PUBLIC;

CREATE VIEW detector_event_view AS
    SELECT e.event_id, e.event_date, ed.description, e.device_id, dl.label
    FROM event.detector_event e
    JOIN event.event_description ed ON e.event_desc_id = ed.event_desc_id
    JOIN detector_label_view dl ON e.device_id = dl.det_id;
GRANT SELECT ON detector_event_view TO PUBLIC;

CREATE VIEW detector_auto_fail_view AS
    WITH af AS (
        SELECT device_id, event_desc_id, count(*) AS event_count,
               max(event_date) AS last_fail
        FROM event.detector_event
        GROUP BY device_id, event_desc_id
    )
    SELECT device_id, label, ed.description, event_count, last_fail
    FROM af
    JOIN event.event_description ed ON af.event_desc_id = ed.event_desc_id
    JOIN detector_label_view dl ON af.device_id = dl.det_id;
GRANT SELECT ON detector_auto_fail_view TO PUBLIC;

--
-- Action Plans, Plan Phases, Day Plans, Day Matchers, and Time Actions
--
CREATE TABLE iris.plan_phase (
    name VARCHAR(12) PRIMARY KEY,
    hold_time INTEGER NOT NULL,
    next_phase VARCHAR(12) REFERENCES iris.plan_phase
);

INSERT INTO iris.plan_phase (name, hold_time)
VALUES
    ('deployed', 0),
    ('undeployed', 0),
    ('alert_before', 0),
    ('alert_during', 0),
    ('alert_after', 0),
    ('ga_open', 0),
    ('ga_closed', 0);

CREATE TRIGGER plan_phase_notify_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris.plan_phase
    FOR EACH STATEMENT EXECUTE FUNCTION iris.table_notify();

CREATE TABLE iris.action_plan (
    name VARCHAR(16) PRIMARY KEY,
    notes VARCHAR CHECK (LENGTH(notes) < 256),
    sync_actions BOOLEAN NOT NULL,
    sticky BOOLEAN NOT NULL,
    ignore_auto_fail BOOLEAN NOT NULL,
    active BOOLEAN NOT NULL,
    default_phase VARCHAR(12) NOT NULL REFERENCES iris.plan_phase,
    phase VARCHAR(12) NOT NULL REFERENCES iris.plan_phase
);

CREATE TRIGGER action_plan_hashtag_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris.action_plan
    FOR EACH ROW EXECUTE FUNCTION iris.hashtag_trig('action_plan');

CREATE FUNCTION iris.action_plan_notify() RETURNS TRIGGER AS
    $action_plan_notify$
BEGIN
    IF (NEW.notes IS DISTINCT FROM OLD.notes) OR
       (NEW.active IS DISTINCT FROM OLD.active)
    THEN
        NOTIFY action_plan;
    ELSE
        PERFORM pg_notify('action_plan', NEW.name);
    END IF;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$action_plan_notify$ LANGUAGE plpgsql;

CREATE TRIGGER action_plan_notify_trig
    AFTER UPDATE ON iris.action_plan
    FOR EACH STATEMENT EXECUTE FUNCTION iris.action_plan_notify();

CREATE TRIGGER action_plan_table_notify_trig
    AFTER INSERT OR DELETE ON iris.action_plan
    FOR EACH STATEMENT EXECUTE FUNCTION iris.table_notify();

CREATE VIEW action_plan_view AS
    SELECT name, notes, sync_actions, sticky, ignore_auto_fail, active,
           default_phase, phase
    FROM iris.action_plan;
GRANT SELECT ON action_plan_view TO PUBLIC;

CREATE TABLE iris.day_plan (
    name VARCHAR(10) PRIMARY KEY,
    holidays BOOLEAN NOT NULL
);

INSERT INTO iris.day_plan (name, holidays)
VALUES
    ('ALL_DAYS', true),
    ('WEEKDAYS', true),
    ('METER_AM', true),
    ('METER_PM', true);

CREATE FUNCTION iris.day_plan_notify() RETURNS TRIGGER AS
    $day_plan_notify$
BEGIN
    NOTIFY day_plan;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$day_plan_notify$ LANGUAGE plpgsql;

CREATE TRIGGER day_plan_notify_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris.day_plan
    FOR EACH STATEMENT EXECUTE FUNCTION iris.day_plan_notify();

CREATE TABLE iris.day_matcher (
    name VARCHAR(10) PRIMARY KEY,
    day_plan VARCHAR(10) NOT NULL REFERENCES iris.day_plan,
    month INTEGER CHECK (month >= 1 AND month <= 12),
    day INTEGER CHECK (day >= 1 AND day <= 31),
    weekday INTEGER CHECK (weekday >= 1 AND weekday <= 7),
    week INTEGER CHECK (week >= 1 AND week <= 4 OR week = -1),
    shift INTEGER CHECK (shift >= -2 AND shift <= 2 AND shift != 0),

    CONSTRAINT day_matcher_valid CHECK (
        (COALESCE(month, day, weekday, week) IS NOT NULL) AND
        (day IS NULL OR week IS NULL) AND
        (shift IS NULL OR (weekday IS NOT NULL AND week IS NOT NULL))
    )
);

INSERT INTO iris.day_matcher (name, day_plan, month, day, weekday, week, shift)
VALUES
    ('dm_1', 'METER_AM', 1, 1, NULL, NULL, NULL), -- New Years Day
    ('dm_2', 'METER_PM', 1, 1, NULL, NULL, NULL),
    ('dm_3', 'METER_AM', 5, NULL, 2, -1, NULL), -- Memorial Day
    ('dm_4', 'METER_PM', 5, NULL, 2, -1, NULL),
    ('dm_5', 'METER_AM', 7, 4, NULL, NULL, NULL), -- Independence Day
    ('dm_6', 'METER_PM', 7, 4, NULL, NULL, NULL),
    ('dm_7', 'METER_AM', 9, NULL, 2, 1, NULL), -- Labor Day
    ('dm_8', 'METER_PM', 9, NULL, 2, 1, NULL),
    ('dm_9', 'METER_AM', 11, NULL, 5, 4, NULL), -- Thanksgiving Day
    ('dm_10', 'METER_PM', 11, NULL, 5, 4, NULL),
    ('dm_11', 'METER_AM', 11, NULL, 5, 4, 1), -- Black Friday
    ('dm_12', 'METER_PM', 11, NULL, 5, 4, 1),
    ('dm_13', 'METER_AM', 12, 24, NULL, NULL, NULL), -- Christmas Eve
    ('dm_14', 'METER_PM', 12, 24, NULL, NULL, NULL),
    ('dm_15', 'METER_AM', 12, 25, NULL, NULL, NULL), -- Christmas Day
    ('dm_16', 'METER_PM', 12, 25, NULL, NULL, NULL),
    ('dm_17', 'METER_AM', 12, 31, NULL, NULL, NULL), -- New Years Eve
    ('dm_18', 'METER_PM', 12, 31, NULL, NULL, NULL),
    ('dm_19', 'WEEKDAYS', NULL, NULL, 1, NULL, NULL), -- Sundays
    ('dm_20', 'METER_AM', NULL, NULL, 1, NULL, NULL),
    ('dm_21', 'METER_PM', NULL, NULL, 1, NULL, NULL),
    ('dm_22', 'WEEKDAYS', NULL, NULL, 7, NULL, NULL), -- Saturdays
    ('dm_23', 'METER_AM', NULL, NULL, 7, NULL, NULL),
    ('dm_24', 'METER_PM', NULL, NULL, 7, NULL, NULL);

CREATE TRIGGER day_matcher_notify_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris.day_matcher
    FOR EACH STATEMENT EXECUTE FUNCTION iris.day_plan_notify();

CREATE VIEW day_plan_view AS
    SELECT p.name, holidays, month, day, weekday, week, shift
    FROM iris.day_plan p
    JOIN iris.day_matcher m ON m.day_plan = p.name;
GRANT SELECT ON day_plan_view TO PUBLIC;

CREATE TABLE iris.time_action (
    name VARCHAR(30) PRIMARY KEY,
    action_plan VARCHAR(16) NOT NULL REFERENCES iris.action_plan,
    day_plan VARCHAR(10) REFERENCES iris.day_plan,
    sched_date DATE,
    time_of_day TIME WITHOUT TIME ZONE NOT NULL,
    phase VARCHAR(12) NOT NULL REFERENCES iris.plan_phase,

    CONSTRAINT time_action_date CHECK (
        ((day_plan IS NULL) OR (sched_date IS NULL)) AND
        ((day_plan IS NOT NULL) OR (sched_date IS NOT NULL))
    )
);

CREATE FUNCTION iris.time_action_notify() RETURNS TRIGGER AS
    $time_action_notify$
BEGIN
    PERFORM pg_notify('time_action', NEW.name);
    RETURN NULL; -- AFTER trigger return is ignored
END;
$time_action_notify$ LANGUAGE plpgsql;

CREATE TRIGGER time_action_notify_trig
    AFTER UPDATE ON iris.time_action
    FOR EACH STATEMENT EXECUTE FUNCTION iris.time_action_notify();

CREATE TRIGGER time_action_table_notify_trig
    AFTER INSERT OR DELETE ON iris.time_action
    FOR EACH STATEMENT EXECUTE FUNCTION iris.table_notify();

CREATE VIEW time_action_view AS
    SELECT name, action_plan, day_plan, sched_date, time_of_day, phase
    FROM iris.time_action;
GRANT SELECT ON time_action_view TO PUBLIC;

CREATE TABLE event.action_plan_event (
    id SERIAL PRIMARY KEY,
    event_date TIMESTAMP WITH time zone DEFAULT NOW() NOT NULL,
    event_desc INTEGER NOT NULL REFERENCES event.event_description,
    action_plan VARCHAR(16) NOT NULL,
    phase VARCHAR(12),
    user_id VARCHAR(15)
);

CREATE VIEW action_plan_event_view AS
    SELECT ev.id, event_date, ed.description, action_plan, phase, user_id
    FROM event.action_plan_event ev
    JOIN event.event_description ed ON ev.event_desc = ed.event_desc_id;
GRANT SELECT ON action_plan_event_view TO PUBLIC;

--
-- Cameras, Encoders, Presets
--
CREATE TABLE iris.encoding (
    id INTEGER PRIMARY KEY,
    description VARCHAR(20) NOT NULL
);

COPY iris.encoding (id, description) FROM stdin;
0	UNKNOWN
1	MJPEG
2	MPEG2
3	MPEG4
4	H264
5	H265
6	AV1
\.

CREATE TABLE iris.encoding_quality (
    id INTEGER PRIMARY KEY,
    description VARCHAR(20) NOT NULL
);

COPY iris.encoding_quality (id, description) FROM stdin;
0	Low
1	Medium
2	High
\.

CREATE TABLE iris.encoder_type (
    name VARCHAR(8) PRIMARY KEY,
    make VARCHAR(16) NOT NULL,
    model VARCHAR(16) NOT NULL,
    config VARCHAR(8) NOT NULL,
    UNIQUE(make, model, config)
);

CREATE TRIGGER encoder_type_notify_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris.encoder_type
    FOR EACH STATEMENT EXECUTE FUNCTION iris.table_notify();

CREATE TABLE iris.encoder_stream (
    name VARCHAR(8) PRIMARY KEY,
    encoder_type VARCHAR(8) NOT NULL REFERENCES iris.encoder_type,
    view_num INTEGER CHECK (view_num > 0 AND view_num <= 12),
    flow_stream BOOLEAN NOT NULL,
    encoding INTEGER NOT NULL REFERENCES iris.encoding,
    quality INTEGER NOT NULL REFERENCES iris.encoding_quality,
    uri_scheme VARCHAR(8),
    uri_path VARCHAR(64),
    mcast_port INTEGER CHECK (mcast_port > 0 AND mcast_port <= 65535),
    latency INTEGER NOT NULL,
    UNIQUE(encoder_type, mcast_port)
);

ALTER TABLE iris.encoder_stream
    ADD CONSTRAINT unicast_or_multicast_ck
    CHECK ((uri_scheme IS NULL AND uri_path IS NULL) OR mcast_port IS NULL);

CREATE TRIGGER encoder_stream_notify_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris.encoder_stream
    FOR EACH STATEMENT EXECUTE FUNCTION iris.table_notify();

CREATE VIEW encoder_stream_view AS
    SELECT es.name, encoder_type, make, model, config, view_num,flow_stream,
           enc.description AS encoding, eq.description AS quality,
           uri_scheme, uri_path, mcast_port, latency
    FROM iris.encoder_stream es
    LEFT JOIN iris.encoder_type et ON es.encoder_type = et.name
    LEFT JOIN iris.encoding enc ON es.encoding = enc.id
    LEFT JOIN iris.encoding_quality eq ON es.quality = eq.id;
GRANT SELECT ON encoder_stream_view TO PUBLIC;

CREATE TABLE iris.camera_template (
    name VARCHAR(20) PRIMARY KEY,
    notes text,
    label text
);

CREATE TABLE iris._camera (
    name VARCHAR(20) PRIMARY KEY,
    geo_loc VARCHAR(20) NOT NULL REFERENCES iris.geo_loc(name),
    notes VARCHAR CHECK (LENGTH(notes) < 256),
    cam_num INTEGER UNIQUE,
    cam_template VARCHAR(20) REFERENCES iris.camera_template,
    encoder_type VARCHAR(8) REFERENCES iris.encoder_type,
    enc_address INET,
    enc_port INTEGER CHECK (enc_port > 0 AND enc_port <= 65535),
    enc_mcast INET,
    enc_channel INTEGER CHECK (enc_channel > 0 AND enc_channel <= 16),
    publish BOOLEAN NOT NULL,
    video_loss BOOLEAN NOT NULL
);

ALTER TABLE iris._camera ADD CONSTRAINT _camera_fkey
    FOREIGN KEY (name) REFERENCES iris.controller_io ON DELETE CASCADE;

CREATE TRIGGER camera_hashtag_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris._camera
    FOR EACH ROW EXECUTE FUNCTION iris.hashtag_trig('camera');

CREATE FUNCTION iris.camera_notify() RETURNS TRIGGER AS
    $camera_notify$
BEGIN
    IF (NEW.notes IS DISTINCT FROM OLD.notes) OR
       (NEW.cam_num IS DISTINCT FROM OLD.cam_num) OR
       (NEW.publish IS DISTINCT FROM OLD.publish)
    THEN
        NOTIFY camera;
    ELSE
        PERFORM pg_notify('camera', NEW.name);
    END IF;
    IF (NEW.publish IS DISTINCT FROM OLD.publish) THEN
        PERFORM pg_notify('camera_publish', NEW.name);
    END IF;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$camera_notify$ LANGUAGE plpgsql;

CREATE TRIGGER camera_notify_trig
    AFTER UPDATE ON iris._camera
    FOR EACH ROW EXECUTE FUNCTION iris.camera_notify();

CREATE TRIGGER camera_table_notify_trig
    AFTER INSERT OR DELETE ON iris._camera
    FOR EACH STATEMENT EXECUTE FUNCTION iris.table_notify();

CREATE VIEW iris.camera AS
    SELECT c.name, geo_loc, controller, pin, notes, cam_num, cam_template,
           encoder_type, enc_address, enc_port, enc_mcast, enc_channel,
           publish, video_loss
    FROM iris._camera c
    JOIN iris.controller_io cio ON c.name = cio.name;

CREATE FUNCTION iris.camera_insert() RETURNS TRIGGER AS
    $camera_insert$
BEGIN
    INSERT INTO iris.controller_io (name, resource_n, controller, pin)
         VALUES (NEW.name, 'camera', NEW.controller, NEW.pin);
    INSERT INTO iris._camera (
        name, geo_loc, notes, cam_num, cam_template, encoder_type, enc_address,
        enc_port, enc_mcast, enc_channel, publish, video_loss
    ) VALUES (
        NEW.name, NEW.geo_loc, NEW.notes, NEW.cam_num, NEW.cam_template,
        NEW.encoder_type, NEW.enc_address, NEW.enc_port, NEW.enc_mcast,
        NEW.enc_channel, NEW.publish, NEW.video_loss
    );
    RETURN NEW;
END;
$camera_insert$ LANGUAGE plpgsql;

CREATE TRIGGER camera_insert_trig
    INSTEAD OF INSERT ON iris.camera
    FOR EACH ROW EXECUTE FUNCTION iris.camera_insert();

CREATE FUNCTION iris.camera_update() RETURNS TRIGGER AS
    $camera_update$
BEGIN
    UPDATE iris.controller_io
       SET controller = NEW.controller,
           pin = NEW.pin
     WHERE name = OLD.name;
    UPDATE iris._camera
       SET notes = NEW.notes,
           cam_num = NEW.cam_num,
           cam_template = NEW.cam_template,
           encoder_type = NEW.encoder_type,
           enc_address = NEW.enc_address,
           enc_port = NEW.enc_port,
           enc_mcast = NEW.enc_mcast,
           enc_channel = NEW.enc_channel,
           publish = NEW.publish,
           video_loss = NEW.video_loss
     WHERE name = OLD.name;
    RETURN NEW;
END;
$camera_update$ LANGUAGE plpgsql;

CREATE TRIGGER camera_update_trig
    INSTEAD OF UPDATE ON iris.camera
    FOR EACH ROW EXECUTE FUNCTION iris.camera_update();

CREATE TRIGGER camera_delete_trig
    INSTEAD OF DELETE ON iris.camera
    FOR EACH ROW EXECUTE FUNCTION iris.controller_io_delete();

CREATE VIEW camera_view AS
    SELECT c.name, cam_num, c.cam_template, encoder_type, et.make, et.model,
           et.config, c.enc_address, c.enc_port, c.enc_mcast, c.enc_channel,
           c.publish, c.video_loss, c.geo_loc,
           l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir,
           l.landmark, l.lat, l.lon, l.corridor, l.location,
           cio.controller, ctr.comm_link, ctr.drop_id, ctr.condition, c.notes
    FROM iris._camera c
    JOIN iris.controller_io cio ON c.name = cio.name
    LEFT JOIN iris.encoder_type et ON c.encoder_type = et.name
    LEFT JOIN geo_loc_view l ON c.geo_loc = l.name
    LEFT JOIN controller_view ctr ON cio.controller = ctr.name;
GRANT SELECT ON camera_view TO PUBLIC;

CREATE TABLE iris.vid_src_template (
    name VARCHAR(20) PRIMARY KEY,
    label text,
    config text,
    default_port INTEGER,
    subnets text,
    latency INTEGER,
    encoder VARCHAR(64),
    scheme text,
    codec text,
    rez_width INTEGER,
    rez_height INTEGER,
    multicast BOOLEAN,
    notes text
);

CREATE TABLE iris.cam_vid_src_ord (
    name VARCHAR(24) PRIMARY KEY,
    camera_template VARCHAR(20) REFERENCES iris.camera_template,
    src_order INTEGER,
    src_template VARCHAR(20) REFERENCES iris.vid_src_template
);

CREATE TABLE event.camera_switch_event (
    event_id SERIAL PRIMARY KEY,
    event_date TIMESTAMP WITH time zone DEFAULT NOW() NOT NULL,
    event_desc_id INTEGER NOT NULL
        REFERENCES event.event_description(event_desc_id),
    monitor_id VARCHAR(12),
    camera_id VARCHAR(20),
    source VARCHAR(20)
);

CREATE VIEW camera_switch_event_view AS
    SELECT event_id, event_date, event_description.description, monitor_id,
           camera_id, source
    FROM event.camera_switch_event
    JOIN event.event_description
    ON camera_switch_event.event_desc_id = event_description.event_desc_id;
GRANT SELECT ON camera_switch_event_view TO PUBLIC;

CREATE TABLE event.camera_video_event (
    event_id SERIAL PRIMARY KEY,
    event_date TIMESTAMP WITH time zone DEFAULT NOW() NOT NULL,
    event_desc_id INTEGER NOT NULL
        REFERENCES event.event_description(event_desc_id),
    camera_id VARCHAR(20),
    monitor_id VARCHAR(12)
);

CREATE VIEW camera_video_event_view AS
    SELECT event_id, event_date, event_description.description, camera_id,
           monitor_id
    FROM event.camera_video_event
    JOIN event.event_description
    ON camera_video_event.event_desc_id = event_description.event_desc_id;
GRANT SELECT ON camera_video_event_view TO PUBLIC;

CREATE TABLE iris.camera_preset (
    name VARCHAR(20) PRIMARY KEY,
    camera VARCHAR(20) NOT NULL REFERENCES iris._camera,
    preset_num INTEGER NOT NULL CHECK (preset_num > 0 AND preset_num <= 12),
    direction SMALLINT REFERENCES iris.direction(id),
    UNIQUE(camera, preset_num)
);

CREATE TRIGGER camera_preset_notify_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris.camera_preset
    FOR EACH STATEMENT EXECUTE FUNCTION iris.table_notify();

CREATE TABLE iris.device_preset (
    name VARCHAR(20) PRIMARY KEY,
    resource_n VARCHAR(16) NOT NULL REFERENCES iris.resource_type,
    preset VARCHAR(20) UNIQUE REFERENCES iris.camera_preset(name)
);

CREATE FUNCTION iris.device_preset_notify() RETURNS TRIGGER AS
    $device_preset_notify$
BEGIN
    IF (NEW.preset IS DISTINCT FROM OLD.preset) THEN
        PERFORM pg_notify(NEW.resource_n, NEW.name);
    END IF;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$device_preset_notify$ LANGUAGE plpgsql;

CREATE TRIGGER device_preset_notify_trig
    AFTER UPDATE ON iris.device_preset
    FOR EACH ROW EXECUTE FUNCTION iris.device_preset_notify();

CREATE VIEW camera_preset_view AS
    SELECT cp.name, camera, preset_num, direction, p.name AS device
    FROM iris.camera_preset cp
    JOIN iris.device_preset p ON cp.name = p.preset;
GRANT SELECT ON camera_preset_view TO PUBLIC;

--
-- Alarms
--
CREATE TABLE iris._alarm (
    name VARCHAR(20) PRIMARY KEY,
    description VARCHAR(24) NOT NULL,
    state BOOLEAN NOT NULL,
    trigger_time TIMESTAMP WITH time zone
);

ALTER TABLE iris._alarm ADD CONSTRAINT _alarm_fkey
    FOREIGN KEY (name) REFERENCES iris.controller_io ON DELETE CASCADE;

CREATE TRIGGER alarm_notify_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris._alarm
    FOR EACH STATEMENT EXECUTE FUNCTION iris.table_notify();

CREATE VIEW iris.alarm AS
    SELECT a.name, description, controller, pin, state, trigger_time
    FROM iris._alarm a JOIN iris.controller_io cio ON a.name = cio.name;

CREATE FUNCTION iris.alarm_insert() RETURNS TRIGGER AS
    $alarm_insert$
BEGIN
    INSERT INTO iris.controller_io (name, resource_n, controller, pin)
         VALUES (NEW.name, 'alarm', NEW.controller, NEW.pin);
    INSERT INTO iris._alarm (name, description, state, trigger_time)
         VALUES (NEW.name, NEW.description, NEW.state, NEW.trigger_time);
    RETURN NEW;
END;
$alarm_insert$ LANGUAGE plpgsql;

CREATE TRIGGER alarm_insert_trig
    INSTEAD OF INSERT ON iris.alarm
    FOR EACH ROW EXECUTE FUNCTION iris.alarm_insert();

CREATE FUNCTION iris.alarm_update() RETURNS TRIGGER AS
    $alarm_update$
BEGIN
    UPDATE iris.controller_io
       SET controller = NEW.controller,
           pin = NEW.pin
     WHERE name = OLD.name;
    UPDATE iris._alarm
       SET description = NEW.description,
           state = NEW.state,
           trigger_time = NEW.trigger_time
     WHERE name = OLD.name;
    RETURN NEW;
END;
$alarm_update$ LANGUAGE plpgsql;

CREATE TRIGGER alarm_update_trig
    INSTEAD OF UPDATE ON iris.alarm
    FOR EACH ROW EXECUTE FUNCTION iris.alarm_update();

CREATE TRIGGER alarm_delete_trig
    INSTEAD OF DELETE ON iris.alarm
    FOR EACH ROW EXECUTE FUNCTION iris.controller_io_delete();

CREATE VIEW alarm_view AS
    SELECT a.name, a.description, a.state, a.trigger_time, a.controller, a.pin,
           c.comm_link, c.drop_id
    FROM iris.alarm a
    LEFT JOIN iris.controller c ON a.controller = c.name;
GRANT SELECT ON alarm_view TO PUBLIC;

CREATE TABLE event.alarm_event (
    event_id INTEGER PRIMARY KEY DEFAULT nextval('event.event_id_seq'),
    event_date TIMESTAMP WITH time zone DEFAULT NOW() NOT NULL,
    event_desc_id INTEGER NOT NULL
        REFERENCES event.event_description(event_desc_id),
    alarm VARCHAR(20) NOT NULL REFERENCES iris._alarm(name)
        ON DELETE CASCADE
);

CREATE VIEW alarm_event_view AS
    SELECT e.event_id, e.event_date, ed.description AS event_description,
           e.alarm, a.description
    FROM event.alarm_event e
    JOIN event.event_description ed ON e.event_desc_id = ed.event_desc_id
    JOIN iris._alarm a ON e.alarm = a.name;
GRANT SELECT ON alarm_event_view TO PUBLIC;

--
-- Beacons
--
CREATE TABLE iris.beacon_state (
    id INTEGER PRIMARY KEY,
    description VARCHAR(18) NOT NULL
);

INSERT INTO iris.beacon_state (id, description)
VALUES
    (0, 'Unknown'),
    (1, 'Dark Req'),
    (2, 'Dark'),
    (3, 'Flashing Req'),
    (4, 'Flashing'),
    (5, 'Fault: No Verify'),
    (6, 'Fault: Stuck On'),
    (7, 'Flashing: External');

CREATE TABLE iris._beacon (
    name VARCHAR(20) PRIMARY KEY,
    geo_loc VARCHAR(20) NOT NULL REFERENCES iris.geo_loc(name),
    message VARCHAR(128) NOT NULL,
    notes VARCHAR CHECK (LENGTH(notes) < 256),
    verify_pin INTEGER,
    ext_mode BOOLEAN NOT NULL,
    state INTEGER NOT NULL REFERENCES iris.beacon_state
);

ALTER TABLE iris._beacon ADD CONSTRAINT _beacon_fkey
    FOREIGN KEY (name) REFERENCES iris.controller_io ON DELETE CASCADE;

CREATE TRIGGER beacon_hashtag_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris._beacon
    FOR EACH ROW EXECUTE FUNCTION iris.hashtag_trig('beacon');

CREATE FUNCTION iris.beacon_notify() RETURNS TRIGGER AS
    $beacon_notify$
BEGIN
    IF (NEW.message IS DISTINCT FROM OLD.message) OR
       (NEW.notes IS DISTINCT FROM OLD.notes) OR
       (NEW.state IS DISTINCT FROM OLD.state)
    THEN
        NOTIFY beacon;
    ELSE
        PERFORM pg_notify('beacon', NEW.name);
    END IF;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$beacon_notify$ LANGUAGE plpgsql;

CREATE TRIGGER beacon_notify_trig
    AFTER UPDATE ON iris._beacon
    FOR EACH ROW EXECUTE FUNCTION iris.beacon_notify();

CREATE TRIGGER beacon_table_notify_trig
    AFTER INSERT OR DELETE ON iris._beacon
    FOR EACH STATEMENT EXECUTE FUNCTION iris.table_notify();

CREATE VIEW iris.beacon AS
    SELECT b.name, geo_loc, controller, pin, notes, message, verify_pin,
           ext_mode, preset, state
    FROM iris._beacon b
    JOIN iris.controller_io cio ON b.name = cio.name
    JOIN iris.device_preset p ON b.name = p.name;

CREATE FUNCTION iris.beacon_insert() RETURNS TRIGGER AS
    $beacon_insert$
BEGIN
    INSERT INTO iris.controller_io (name, resource_n, controller, pin)
        VALUES (NEW.name, 'beacon', NEW.controller, NEW.pin);
    INSERT INTO iris.device_preset (name, resource_n, preset)
        VALUES (NEW.name, 'beacon', NEW.preset);
    INSERT INTO iris._beacon (name, geo_loc, notes, message, verify_pin,
                              ext_mode, state)
        VALUES (NEW.name, NEW.geo_loc, NEW.notes, NEW.message,
                NEW.verify_pin, NEW.ext_mode, NEW.state);
    RETURN NEW;
END;
$beacon_insert$ LANGUAGE plpgsql;

CREATE TRIGGER beacon_insert_trig
    INSTEAD OF INSERT ON iris.beacon
    FOR EACH ROW EXECUTE FUNCTION iris.beacon_insert();

CREATE FUNCTION iris.beacon_update() RETURNS TRIGGER AS
    $beacon_update$
BEGIN
    UPDATE iris.controller_io
       SET controller = NEW.controller,
           pin = NEW.pin
     WHERE name = OLD.name;
    UPDATE iris.device_preset
       SET preset = NEW.preset
     WHERE name = OLD.name;
    UPDATE iris._beacon
       SET notes = NEW.notes,
           message = NEW.message,
           verify_pin = NEW.verify_pin,
           ext_mode = NEW.ext_mode,
           state = NEW.state
     WHERE name = OLD.name;
    RETURN NEW;
END;
$beacon_update$ LANGUAGE plpgsql;

CREATE TRIGGER beacon_update_trig
    INSTEAD OF UPDATE ON iris.beacon
    FOR EACH ROW EXECUTE FUNCTION iris.beacon_update();

CREATE TRIGGER beacon_delete_trig
    INSTEAD OF DELETE ON iris.beacon
    FOR EACH ROW EXECUTE FUNCTION iris.controller_io_delete();

CREATE VIEW beacon_view AS
    SELECT b.name, b.notes, b.message, cp.camera, cp.preset_num, b.geo_loc,
           l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir,
           l.landmark, l.lat, l.lon, l.corridor, l.location,
           cio.controller, cio.pin, b.verify_pin, b.ext_mode,
           ctr.comm_link, ctr.drop_id, ctr.condition, bs.description AS state
    FROM iris._beacon b
    JOIN iris.beacon_state bs ON b.state = bs.id
    JOIN iris.controller_io cio ON b.name = cio.name
    LEFT JOIN iris.device_preset p ON b.name = p.name
    LEFT JOIN iris.camera_preset cp ON cp.name = p.preset
    LEFT JOIN geo_loc_view l ON b.geo_loc = l.name
    LEFT JOIN controller_view ctr ON cio.controller = ctr.name;
GRANT SELECT ON beacon_view TO PUBLIC;

CREATE TABLE event.beacon_event (
    id SERIAL PRIMARY KEY,
    event_date TIMESTAMP WITH time zone DEFAULT NOW() NOT NULL,
    beacon VARCHAR(20) NOT NULL REFERENCES iris._beacon ON DELETE CASCADE,
    state INTEGER NOT NULL REFERENCES iris.beacon_state,
    user_id VARCHAR(15)
);

CREATE VIEW beacon_event_view AS
    SELECT be.id, event_date, beacon, bs.description AS state, user_id
    FROM event.beacon_event be
    JOIN iris.beacon_state bs ON be.state = bs.id;
GRANT SELECT ON beacon_event_view TO PUBLIC;

--
-- GPS
--
CREATE TABLE iris._gps (
    name VARCHAR(20) PRIMARY KEY,
    notes VARCHAR CHECK (LENGTH(notes) < 256),
    geo_loc VARCHAR(20) REFERENCES iris.geo_loc,
    latest_poll TIMESTAMP WITH time zone,
    latest_sample TIMESTAMP WITH time zone,
    lat double precision,
    lon double precision
);

ALTER TABLE iris._gps ADD CONSTRAINT _gps_fkey
    FOREIGN KEY (name) REFERENCES iris.controller_io ON DELETE CASCADE;

CREATE TRIGGER gps_hashtag_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris._gps
    FOR EACH ROW EXECUTE FUNCTION iris.hashtag_trig('gps');

CREATE FUNCTION iris.gps_notify() RETURNS TRIGGER AS
    $gps_notify$
BEGIN
    IF (NEW.notes IS DISTINCT FROM OLD.notes) OR
       (NEW.geo_loc IS DISTINCT FROM OLD.geo_loc)
    THEN
        NOTIFY gps;
    ELSE
        PERFORM pg_notify('gps', NEW.name);
    END IF;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$gps_notify$ LANGUAGE plpgsql;

CREATE TRIGGER gps_notify_trig
    AFTER UPDATE ON iris._gps
    FOR EACH ROW EXECUTE FUNCTION iris.gps_notify();

CREATE TRIGGER gps_table_notify_trig
    AFTER INSERT OR DELETE ON iris._gps
    FOR EACH STATEMENT EXECUTE FUNCTION iris.table_notify();

CREATE VIEW iris.gps AS
    SELECT g.name, controller, pin, notes, geo_loc, latest_poll, latest_sample,
           lat, lon
    FROM iris._gps g
    JOIN iris.controller_io cio ON g.name = cio.name;

CREATE FUNCTION iris.gps_insert() RETURNS TRIGGER AS
    $gps_insert$
BEGIN
    INSERT INTO iris.controller_io (name, resource_n, controller, pin)
         VALUES (NEW.name, 'gps', NEW.controller, NEW.pin);
    INSERT INTO iris._gps (name, notes, geo_loc, latest_poll, latest_sample,
                           lat, lon)
         VALUES (NEW.name, NEW.notes, NEW.geo_loc, NEW.latest_poll,
                 NEW.latest_sample, NEW.lat, NEW.lon);
    RETURN NEW;
END;
$gps_insert$ LANGUAGE plpgsql;

CREATE TRIGGER gps_insert_trig
    INSTEAD OF INSERT ON iris.gps
    FOR EACH ROW EXECUTE FUNCTION iris.gps_insert();

CREATE FUNCTION iris.gps_update() RETURNS TRIGGER AS
    $gps_update$
BEGIN
    UPDATE iris.controller_io
       SET controller = NEW.controller,
           pin = NEW.pin
     WHERE name = OLD.name;
    UPDATE iris._gps
       SET notes = NEW.notes,
           geo_loc = NEW.geo_loc,
           latest_poll = NEW.latest_poll,
           latest_sample = NEW.latest_sample,
           lat = NEW.lat,
           lon = NEW.lon
     WHERE name = OLD.name;
    RETURN NEW;
END;
$gps_update$ LANGUAGE plpgsql;

CREATE TRIGGER gps_update_trig
    INSTEAD OF UPDATE ON iris.gps
    FOR EACH ROW EXECUTE FUNCTION iris.gps_update();

CREATE TRIGGER gps_delete_trig
    INSTEAD OF DELETE ON iris.gps
    FOR EACH ROW EXECUTE FUNCTION iris.controller_io_delete();

CREATE VIEW gps_view AS
    SELECT g.name, controller, pin, notes, geo_loc, latest_poll, latest_sample,
           lat, lon
    FROM iris._gps g
    JOIN iris.controller_io cio ON g.name = cio.name;
GRANT SELECT ON gps_view TO PUBLIC;

--
-- DMS, Graphic, Font, Sign Message, Message Pattern, Word, Color Scheme
--
CREATE TABLE iris.font (
    name VARCHAR(16) PRIMARY KEY,
    f_number INTEGER UNIQUE NOT NULL,
    height INTEGER NOT NULL,
    width INTEGER NOT NULL,
    line_spacing INTEGER NOT NULL,
    char_spacing INTEGER NOT NULL
);

ALTER TABLE iris.font
    ADD CONSTRAINT font_number_ck
    CHECK (f_number > 0 AND f_number <= 255);
ALTER TABLE iris.font
    ADD CONSTRAINT font_height_ck
    CHECK (height > 0 AND height <= 32);
ALTER TABLE iris.font
    ADD CONSTRAINT font_width_ck
    CHECK (width >= 0 AND width <= 32);
ALTER TABLE iris.font
    ADD CONSTRAINT font_line_sp_ck
    CHECK (line_spacing >= 0 AND line_spacing <= 16);
ALTER TABLE iris.font
    ADD CONSTRAINT font_char_sp_ck
    CHECK (char_spacing >= 0 AND char_spacing <= 8);

CREATE FUNCTION iris.font_ck() RETURNS TRIGGER AS
    $font_ck$
DECLARE
    g_width INTEGER;
BEGIN
    IF NEW.width > 0 THEN
        SELECT width INTO g_width FROM iris.glyph WHERE font = NEW.name;
        IF FOUND AND NEW.width != g_width THEN
            RAISE EXCEPTION 'width does not match glyph';
        END IF;
    END IF;
    RETURN NEW;
END;
$font_ck$ LANGUAGE plpgsql;

CREATE TRIGGER font_ck_trig
    BEFORE UPDATE ON iris.font
    FOR EACH ROW EXECUTE FUNCTION iris.font_ck();

CREATE VIEW font_view AS
    SELECT name, f_number, height, width, line_spacing, char_spacing
    FROM iris.font;
GRANT SELECT ON font_view TO PUBLIC;

CREATE TABLE iris.glyph (
    name VARCHAR(20) PRIMARY KEY,
    font VARCHAR(16) NOT NULL REFERENCES iris.font(name),
    code_point INTEGER NOT NULL,
    width INTEGER NOT NULL,
    pixels VARCHAR(128) NOT NULL
);

ALTER TABLE iris.glyph
    ADD CONSTRAINT glyph_code_point_ck
    CHECK (code_point > 0 AND code_point < 65536);
ALTER TABLE iris.glyph
    ADD CONSTRAINT glyph_width_ck
    CHECK (width >= 0 AND width <= 32);

CREATE FUNCTION iris.glyph_ck() RETURNS TRIGGER AS
    $glyph_ck$
DECLARE
    f_width INTEGER;
BEGIN
    SELECT width INTO f_width FROM iris.font WHERE name = NEW.font;
    IF f_width > 0 AND f_width != NEW.width THEN
        RAISE EXCEPTION 'width does not match font';
    END IF;
    RETURN NEW;
END;
$glyph_ck$ LANGUAGE plpgsql;

CREATE TRIGGER glyph_ck_trig
    BEFORE INSERT OR UPDATE ON iris.glyph
    FOR EACH ROW EXECUTE FUNCTION iris.glyph_ck();

CREATE VIEW glyph_view AS
    SELECT name, font, code_point, width, pixels
    FROM iris.glyph;
GRANT SELECT ON glyph_view TO PUBLIC;

CREATE TABLE iris.dms_type (
    id INTEGER PRIMARY KEY,
    description VARCHAR(32) NOT NULL
);

COPY iris.dms_type (id, description) FROM stdin;
0	Unknown
1	Other
2	BOS (blank-out sign)
3	CMS (changeable message sign)
4	VMS Character-matrix
5	VMS Line-matrix
6	VMS Full-matrix
\.

CREATE TABLE iris.color_scheme (
    id INTEGER PRIMARY KEY,
    description VARCHAR(16) NOT NULL
);

COPY iris.color_scheme (id, description) FROM stdin;
0	unknown
1	monochrome1Bit
2	monochrome8Bit
3	colorClassic
4	color24Bit
\.

-- Bit flags for sign_detail.supported_tags
CREATE TABLE iris.multi_tag (
    bit INTEGER PRIMARY KEY,
    tag VARCHAR(3) UNIQUE NOT NULL
);
ALTER TABLE iris.multi_tag ADD CONSTRAINT multi_tag_bit_ck
    CHECK (bit >= 0 AND bit < 32);

COPY iris.multi_tag (bit, tag) FROM stdin;
0	cb
1	cf
2	fl
3	fo
4	g
5	hc
6	jl
7	jp
8	ms
9	mv
10	nl
11	np
12	pt
13	sc
14	f1
15	f2
16	f3
17	f4
18	f5
19	f6
20	f7
21	f8
22	f9
23	f10
24	f11
25	f12
26	f13
27	tr
28	cr
29	pb
\.

CREATE FUNCTION iris.multi_tags(INTEGER)
    RETURNS SETOF iris.multi_tag AS $multi_tags$
DECLARE
    mt RECORD;
    b INTEGER;
BEGIN
    FOR mt IN SELECT bit, tag FROM iris.multi_tag LOOP
        b = 1 << mt.bit;
        IF ($1 & b) = b THEN
            RETURN NEXT mt;
        END IF;
    END LOOP;
END;
$multi_tags$ LANGUAGE plpgsql;

-- Get supported MULTI tags from integer bit flags
CREATE FUNCTION iris.multi_tags_str(INTEGER)
    RETURNS text AS $multi_tags_str$
DECLARE
    bits ALIAS FOR $1;
BEGIN
    RETURN (
        SELECT string_agg(mt.tag, ', ') FROM (
            SELECT bit, tag FROM iris.multi_tags(bits) ORDER BY bit
        ) AS mt
    );
END;
$multi_tags_str$ LANGUAGE plpgsql SECURITY DEFINER;

ALTER FUNCTION iris.multi_tags_str(INTEGER)
    SET search_path = pg_catalog, pg_temp;

CREATE TABLE iris.sign_detail (
    name VARCHAR(12) PRIMARY KEY,
    dms_type INTEGER NOT NULL REFERENCES iris.dms_type,
    portable BOOLEAN NOT NULL,
    technology VARCHAR(12) NOT NULL,
    sign_access VARCHAR(12) NOT NULL,
    legend VARCHAR(12) NOT NULL,
    beacon_type VARCHAR(32) NOT NULL,
    hardware_make VARCHAR(32) NOT NULL,
    hardware_model VARCHAR(32) NOT NULL,
    software_make VARCHAR(32) NOT NULL,
    software_model VARCHAR(32) NOT NULL,
    supported_tags INTEGER NOT NULL,
    max_pages INTEGER NOT NULL,
    max_multi_len INTEGER NOT NULL,
    beacon_activation_flag BOOLEAN NOT NULL,
    pixel_service_flag BOOLEAN NOT NULL
);

CREATE TRIGGER sign_detail_notify_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris.sign_detail
    FOR EACH STATEMENT EXECUTE FUNCTION iris.table_notify();

CREATE VIEW sign_detail_view AS
    SELECT name, dt.description AS dms_type, portable, technology, sign_access,
           legend, beacon_type, hardware_make, hardware_model, software_make,
           software_model,iris.multi_tags_str(supported_tags) AS supported_tags,
           max_pages, max_multi_len, beacon_activation_flag, pixel_service_flag
    FROM iris.sign_detail
    JOIN iris.dms_type dt ON sign_detail.dms_type = dt.id;
GRANT SELECT ON sign_detail_view TO PUBLIC;

CREATE TABLE iris.sign_config (
    name VARCHAR(16) PRIMARY KEY,
    face_width INTEGER NOT NULL,
    face_height INTEGER NOT NULL,
    border_horiz INTEGER NOT NULL,
    border_vert INTEGER NOT NULL,
    pitch_horiz INTEGER NOT NULL,
    pitch_vert INTEGER NOT NULL,
    pixel_width INTEGER NOT NULL,
    pixel_height INTEGER NOT NULL,
    char_width INTEGER NOT NULL,
    char_height INTEGER NOT NULL,
    monochrome_foreground INTEGER NOT NULL,
    monochrome_background INTEGER NOT NULL,
    color_scheme INTEGER NOT NULL REFERENCES iris.color_scheme,
    default_font INTEGER NOT NULL,
    module_width INTEGER CHECK (
        module_width > 0 AND
        (pixel_width % module_width) = 0
    ),
    module_height INTEGER CHECK (
        module_height > 0 AND
        (pixel_height % module_height) = 0
    )
);

CREATE TRIGGER sign_config_notify_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris.sign_config
    FOR EACH STATEMENT EXECUTE FUNCTION iris.table_notify();

CREATE VIEW sign_config_view AS
    SELECT name, face_width, face_height, border_horiz, border_vert,
           pitch_horiz, pitch_vert, pixel_width, pixel_height, char_width,
           char_height, monochrome_foreground, monochrome_background,
           cs.description AS color_scheme, default_font,
           module_width, module_height
    FROM iris.sign_config
    JOIN iris.color_scheme cs ON sign_config.color_scheme = cs.id;
GRANT SELECT ON sign_config_view TO PUBLIC;

CREATE TABLE iris.sign_message (
    name VARCHAR(20) PRIMARY KEY,
    sign_config VARCHAR(16) NOT NULL REFERENCES iris.sign_config,
    incident VARCHAR(16) REFERENCES event.incident(name) ON DELETE SET NULL,
    multi VARCHAR(1024) NOT NULL,
    msg_owner VARCHAR(127) NOT NULL,
    flash_beacon BOOLEAN NOT NULL,
    pixel_service BOOLEAN NOT NULL,
    msg_priority INTEGER NOT NULL,
    duration INTEGER
);

CREATE TRIGGER sign_message_notify_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris.sign_message
    FOR EACH STATEMENT EXECUTE FUNCTION iris.table_notify();

CREATE VIEW sign_message_view AS
    SELECT name, sign_config, incident, multi, msg_owner, flash_beacon,
           pixel_service, msg_priority, duration
    FROM iris.sign_message;
GRANT SELECT ON sign_message_view TO PUBLIC;

CREATE TABLE iris.word (
    name VARCHAR(24) PRIMARY KEY,
    abbr VARCHAR(12),
    allowed BOOLEAN DEFAULT false NOT NULL
);

CREATE TRIGGER word_notify_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris.word
    FOR EACH STATEMENT EXECUTE FUNCTION iris.table_notify();

CREATE VIEW word_view AS
    SELECT name, abbr, allowed
    FROM iris.word;
GRANT SELECT ON word_view TO PUBLIC;

COPY iris.word (name, abbr, allowed) FROM stdin;
ACCESS	ACCS	t
AHEAD	AHD	t
ALL	ALL	t
ALTERNATE	OTHER	t
ANIMAL	ANML	t
AT		t
BLOCKED	BLKD	t
BOTH	BTH	t
BRIDGE	BRDG	t
CANNOT	CANT	t
CENTER	CNTR	t
CLEARED	CLRD	t
CLOSED	CLSD	t
CONGESTED	CONG	t
CONGESTION	CONG	t
CONSTRUCTION	CONST	t
CRASH	CRSH	t
CROSSING	X-ING	t
DEBRIS	DEBRIS	t
DELAYS	DELAY	t
DOWNTOWN	DWNTN	t
EAST	E	t
EMERGENCY	EMRGNCY	t
ENTRANCE	ENT	t
EVENT	EVNT	t
EXIT	EXIT	t
FAILURE	FAIL	t
FLASH	FLSH	t
FLOODING	FLOOD	t
FRONTAGE	FRNTG	t
GRASS	GRSS	t
ICE	ICE	t
IN		t
INCIDENT	INCDNT	t
LANE	LN	t
LANES	LNS	t
LEFT	LFT	t
MAXIMUM	MAX	t
MILE	MI	t
MILES	MI	t
MINIMUM	MIN	t
NORTH	N	t
ON	ON	t
OTHER	ALT	t
OVERSIZED	OVRSZ	t
PARKING	PKNG	t
PAVEMENT	PVMT	t
PEDESTRIAN	PED	t
PREPARE	PREP	t
QUALITY	QLTY	t
RAMP	RMP	t
REMOVAL	RMVL	t
RIGHT	RT	t
ROAD	RD	t
ROUTE	RTE	t
ROUTES	ROUTE	t
SERVICE	SERV	t
SHOULDER	SHLDR	t
SINGLE	SNGL	t
SLIPPERY	SLIP	t
SNOW	SNW	t
SOUTH	S	t
SPEED	SPD	t
STALL	STLL	t
STALLED	STALL	t
TEMPORARY	TEMP	t
TEST	TST	t
TRAFFIC	TRAF	t
VEHICLE	VEH	t
VEHICLES	VEHS	t
WARNING	WARN	t
WEST	W	t
WORK	WRK	t
\.

CREATE TABLE iris.graphic (
    name VARCHAR(20) PRIMARY KEY,
    g_number INTEGER NOT NULL UNIQUE,
    color_scheme INTEGER NOT NULL REFERENCES iris.color_scheme,
    height INTEGER NOT NULL,
    width INTEGER NOT NULL,
    transparent_color INTEGER,
    pixels TEXT NOT NULL
);

CREATE TRIGGER graphic_notify_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris.graphic
    FOR EACH STATEMENT EXECUTE FUNCTION iris.table_notify();

ALTER TABLE iris.graphic
    ADD CONSTRAINT graphic_number_ck
    CHECK (g_number > 0 AND g_number <= 999);
ALTER TABLE iris.graphic
    ADD CONSTRAINT graphic_height_ck
    CHECK (height >= 1 AND height <= 144);
ALTER TABLE iris.graphic
    ADD CONSTRAINT graphic_width_ck
    CHECK (width >= 1 AND width <= 240);

CREATE VIEW graphic_view AS
    SELECT name, g_number, cs.description AS color_scheme, height, width,
           transparent_color, pixels
    FROM iris.graphic
    JOIN iris.color_scheme cs ON graphic.color_scheme = cs.id;
GRANT SELECT ON graphic_view TO PUBLIC;

CREATE TABLE iris._dms (
    name VARCHAR(20) PRIMARY KEY,
    geo_loc VARCHAR(20) NOT NULL REFERENCES iris.geo_loc,
    notes VARCHAR CHECK (LENGTH(notes) < 256),
    static_graphic VARCHAR(20) REFERENCES iris.graphic,
    -- FIXME: only allow one reference to a beacon
    beacon VARCHAR(20) REFERENCES iris._beacon,
    sign_config VARCHAR(16) REFERENCES iris.sign_config,
    sign_detail VARCHAR(12) REFERENCES iris.sign_detail,
    msg_user VARCHAR(20) REFERENCES iris.sign_message,
    msg_sched VARCHAR(20) REFERENCES iris.sign_message,
    msg_current VARCHAR(20) REFERENCES iris.sign_message,
    expire_time TIMESTAMP WITH time zone,
    status JSONB,
    pixel_failures VARCHAR
);

ALTER TABLE iris._dms ADD CONSTRAINT _dms_fkey
    FOREIGN KEY (name) REFERENCES iris.controller_io ON DELETE CASCADE;

CREATE FUNCTION iris.dms_notify() RETURNS TRIGGER AS
    $dms_notify$
BEGIN
    -- has_faults is derived from status (secondary attribute)
    IF (NEW.notes IS DISTINCT FROM OLD.notes) OR
       (NEW.msg_current IS DISTINCT FROM OLD.msg_current) OR
       ((NEW.status->>'faults' IS NOT NULL) IS DISTINCT FROM
        (OLD.status->>'faults' IS NOT NULL))
    THEN
        NOTIFY dms;
    ELSE
        PERFORM pg_notify('dms', NEW.name);
    END IF;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$dms_notify$ LANGUAGE plpgsql;

CREATE TRIGGER dms_notify_trig
    AFTER UPDATE ON iris._dms
    FOR EACH ROW EXECUTE FUNCTION iris.dms_notify();

CREATE TRIGGER dms_table_notify_trig
    AFTER INSERT OR DELETE ON iris._dms
    FOR EACH STATEMENT EXECUTE FUNCTION iris.table_notify();

CREATE VIEW iris.dms AS
    SELECT d.name, geo_loc, controller, pin, notes, static_graphic,
           beacon, preset, sign_config, sign_detail,
           msg_user, msg_sched, msg_current, expire_time, status,
           pixel_failures
    FROM iris._dms d
    JOIN iris.controller_io cio ON d.name = cio.name
    JOIN iris.device_preset p ON d.name = p.name;

CREATE FUNCTION iris.dms_insert() RETURNS TRIGGER AS
    $dms_insert$
BEGIN
    INSERT INTO iris.controller_io (name, resource_n, controller, pin)
         VALUES (NEW.name, 'dms', NEW.controller, NEW.pin);
    INSERT INTO iris.device_preset (name, resource_n, preset)
         VALUES (NEW.name, 'dms', NEW.preset);
    INSERT INTO iris._dms (
        name, geo_loc, notes, static_graphic, beacon,
        sign_config, sign_detail, msg_user, msg_sched, msg_current,
        expire_time, status, pixel_failures
    ) VALUES (
        NEW.name, NEW.geo_loc, NEW.notes, NEW.static_graphic,
        NEW.beacon, NEW.sign_config, NEW.sign_detail,
        NEW.msg_user, NEW.msg_sched, NEW.msg_current, NEW.expire_time,
        NEW.status, NEW.pixel_failures
    );
    RETURN NEW;
END;
$dms_insert$ LANGUAGE plpgsql;

CREATE TRIGGER dms_insert_trig
    INSTEAD OF INSERT ON iris.dms
    FOR EACH ROW EXECUTE FUNCTION iris.dms_insert();

CREATE FUNCTION iris.dms_update() RETURNS TRIGGER AS
    $dms_update$
BEGIN
    UPDATE iris.controller_io
       SET controller = NEW.controller,
           pin = NEW.pin
     WHERE name = OLD.name;
    UPDATE iris.device_preset
       SET preset = NEW.preset
     WHERE name = OLD.name;
    UPDATE iris._dms
       SET notes = NEW.notes,
           static_graphic = NEW.static_graphic,
           beacon = NEW.beacon,
           sign_config = NEW.sign_config,
           sign_detail = NEW.sign_detail,
           msg_user = NEW.msg_user,
           msg_sched = NEW.msg_sched,
           msg_current = NEW.msg_current,
           expire_time = NEW.expire_time,
           status = NEW.status,
           pixel_failures = NEW.pixel_failures
     WHERE name = OLD.name;
    RETURN NEW;
END;
$dms_update$ LANGUAGE plpgsql;

CREATE TRIGGER dms_update_trig
    INSTEAD OF UPDATE ON iris.dms
    FOR EACH ROW EXECUTE FUNCTION iris.dms_update();

CREATE TRIGGER dms_delete_trig
    INSTEAD OF DELETE ON iris.dms
    FOR EACH ROW EXECUTE FUNCTION iris.controller_io_delete();

CREATE VIEW dms_view AS
    SELECT d.name, d.geo_loc, cio.controller, cio.pin, d.notes,
           d.sign_config, d.sign_detail, d.static_graphic, d.beacon,
           cp.camera, cp.preset_num, default_font,
           msg_user, msg_sched, msg_current, expire_time,
           status, pixel_failures,
           l.roadway, l.road_dir, l.cross_mod, l.cross_street,
           l.cross_dir, l.landmark, l.lat, l.lon, l.corridor, l.location
    FROM iris._dms d
    JOIN iris.controller_io cio ON d.name = cio.name
    JOIN iris.device_preset p ON d.name = p.name
    LEFT JOIN iris.camera_preset cp ON cp.name = p.preset
    LEFT JOIN geo_loc_view l ON d.geo_loc = l.name
    LEFT JOIN iris.sign_config sc ON d.sign_config = sc.name;
GRANT SELECT ON dms_view TO PUBLIC;

CREATE VIEW dms_message_view AS
    SELECT d.name, msg_current, cc.description AS condition,
           fail_time IS NOT NULL AS failed, multi, msg_owner, flash_beacon,
           pixel_service, msg_priority, duration, expire_time
    FROM iris._dms d
    LEFT JOIN iris.controller_io cio ON d.name = cio.name
    LEFT JOIN iris.controller c ON cio.controller = c.name
    LEFT JOIN iris.condition cc ON c.condition = cc.id
    LEFT JOIN iris.sign_message sm ON d.msg_current = sm.name;
GRANT SELECT ON dms_message_view TO PUBLIC;

CREATE TRIGGER dms_hashtag_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris._dms
    FOR EACH ROW EXECUTE FUNCTION iris.hashtag_trig('dms');

CREATE TABLE iris.msg_pattern (
    name VARCHAR(20) PRIMARY KEY,
    multi VARCHAR(1024) NOT NULL,
    flash_beacon BOOLEAN NOT NULL,
    pixel_service BOOLEAN NOT NULL,
    compose_hashtag VARCHAR(16),

    CONSTRAINT hashtag_ck CHECK (compose_hashtag ~ '^#[A-Za-z0-9]+$')
);

INSERT INTO iris.msg_pattern (name, multi, flash_beacon, pixel_service,
    compose_hashtag)
VALUES
    ('.1_LINE', '', false, false, '#OneLine'),
    ('.2_LINE', '[np]', false, false, '#TwoLine'),
    ('.3_LINE', '', false, false, '#ThreeLine'),
    ('.4_LINE', '', false, false, '#FourLine'),
    ('.2_PAGE', '[np]', false, false, '#Small'),
    ('RWIS_slippery_1',
        '[rwis_slippery,1]SLIPPERY[nl]ROAD[nl]DETECTED[np]USE[nl]CAUTION',
        false,
        false,
        NULL),
    ('RWIS_slippery_2',
        '[rwis_slippery,2]SLIPPERY[nl]ROAD[nl]DETECTED[np]REDUCE[nl]SPEED',
        false,
        false,
        NULL),
    ('RWIS_slippery_3',
        '[rwis_slippery,3]ICE[nl]DETECTED[np]REDUCE[nl]SPEED',
        false,
        false,
        NULL),
    ('RWIS_windy_1',
        '[rwis_windy,1]WIND GST[nl]>40 MPH[nl]DETECTED[np]USE[nl]CAUTION',
        false,
        false,
        NULL),
    ('RWIS_windy_2',
        '[rwis_windy,2]WIND GST[nl]>60 MPH[nl]DETECTED[np]REDUCE[nl]SPEED',
        false,
        false,
        NULL),
    ('RWIS_visibility_1',
        '[rwis_visibility,1]REDUCED[nl]VISBLITY[nl]DETECTED[np]USE[nl]CAUTION',
        false,
        false,
        NULL),
    ('RWIS_visibility_2',
        '[rwis_visibility,2]LOW[nl]VISBLITY[nl]DETECTED[np]REDUCE[nl]SPEED',
        false,
        false,
        NULL),
    ('RWIS_flooding_1',
        '[rwis_flooding,1]FLOODING[nl]POSSIBLE[np]USE[nl]CAUTION',
        false,
        false,
        NULL),
    ('RWIS_flooding_2',
        '[rwis_flooding,2]FLASH[nl]FLOODING[np]USE[nl]CAUTION',
        false,
        false,
        NULL);

CREATE TRIGGER msg_pattern_notify_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris.msg_pattern
    FOR EACH STATEMENT EXECUTE FUNCTION iris.table_notify();

CREATE VIEW msg_pattern_view AS
    SELECT name, multi, flash_beacon, pixel_service, compose_hashtag
    FROM iris.msg_pattern;
GRANT SELECT ON msg_pattern_view TO PUBLIC;

CREATE TABLE iris.msg_line (
    name VARCHAR(10) PRIMARY KEY,
    msg_pattern VARCHAR(20) NOT NULL REFERENCES iris.msg_pattern,
    restrict_hashtag VARCHAR(16),
    line SMALLINT NOT NULL,
    multi VARCHAR(64) NOT NULL,
    rank SMALLINT NOT NULL,

    CONSTRAINT hashtag_ck CHECK (restrict_hashtag ~ '^#[A-Za-z0-9]+$'),
    CONSTRAINT msg_line_line CHECK ((line >= 1) AND (line <= 12)),
    CONSTRAINT msg_line_rank CHECK ((rank >= 1) AND (rank <= 99))
);

CREATE TRIGGER msg_line_notify_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris.msg_line
    FOR EACH STATEMENT EXECUTE FUNCTION iris.table_notify();

CREATE VIEW msg_line_view AS
    SELECT name, msg_pattern, restrict_hashtag, line, multi, rank
    FROM iris.msg_line;
GRANT SELECT ON msg_line_view TO PUBLIC;

CREATE TABLE iris.device_action (
    name VARCHAR(30) PRIMARY KEY,
    action_plan VARCHAR(16) NOT NULL REFERENCES iris.action_plan,
    phase VARCHAR(12) NOT NULL REFERENCES iris.plan_phase,
    hashtag VARCHAR(16) NOT NULL,
    msg_pattern VARCHAR(20) REFERENCES iris.msg_pattern,
    msg_priority INTEGER NOT NULL,

    CONSTRAINT hashtag_ck CHECK (hashtag ~ '^#[A-Za-z0-9]+$')
);

CREATE FUNCTION iris.device_action_notify() RETURNS TRIGGER AS
    $device_action_notify$
BEGIN
    IF (NEW.hashtag IS DISTINCT FROM OLD.hashtag) THEN
        NOTIFY device_action;
    ELSE
        PERFORM pg_notify('device_action', NEW.name);
    END IF;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$device_action_notify$ LANGUAGE plpgsql;

CREATE TRIGGER device_action_notify_trig
    AFTER UPDATE ON iris.device_action
    FOR EACH STATEMENT EXECUTE FUNCTION iris.device_action_notify();

CREATE TRIGGER device_action_table_notify_trig
    AFTER INSERT OR DELETE ON iris.device_action
    FOR EACH STATEMENT EXECUTE FUNCTION iris.table_notify();

CREATE VIEW device_action_view AS
    SELECT name, action_plan, phase, hashtag, msg_pattern, msg_priority
    FROM iris.device_action;
GRANT SELECT ON device_action_view TO PUBLIC;

CREATE VIEW dms_action_view AS
    SELECT h.name AS dms, action_plan, phase, h.hashtag, msg_pattern,
           msg_priority
    FROM iris.device_action da
    JOIN iris.hashtag h ON h.hashtag = da.hashtag AND resource_n = 'dms';
GRANT SELECT ON dms_action_view TO PUBLIC;

CREATE TABLE event.sign_event (
    event_id INTEGER PRIMARY KEY DEFAULT nextval('event.event_id_seq'),
    event_date TIMESTAMP WITH time zone DEFAULT NOW() NOT NULL,
    event_desc_id INTEGER NOT NULL
        REFERENCES event.event_description(event_desc_id),
    device_id VARCHAR(20),
    multi VARCHAR(1024),
    msg_owner VARCHAR(127),
    duration INTEGER
);
CREATE INDEX ON event.sign_event(event_date);

CREATE FUNCTION event.multi_message(VARCHAR(1024))
    RETURNS TEXT AS $multi_message$
DECLARE
    multi ALIAS FOR $1;
BEGIN
    RETURN regexp_replace(
        replace(
            replace(multi, '[nl]', E'\n'),
            '[np]', E'\n'
        ),
        '\[.+?\]', ' ', 'g'
    );
END;
$multi_message$ LANGUAGE plpgsql;

CREATE VIEW sign_event_view AS
    SELECT event_id, event_date, description, device_id,
           event.multi_message(multi) as message, multi, msg_owner, duration
    FROM event.sign_event JOIN event.event_description
    ON sign_event.event_desc_id = event_description.event_desc_id;
GRANT SELECT ON sign_event_view TO PUBLIC;

CREATE VIEW recent_sign_event_view AS
    SELECT event_id, event_date, description, device_id, message, multi,
           msg_owner, duration
    FROM sign_event_view
    WHERE event_date > (CURRENT_TIMESTAMP - interval '90 days');
GRANT SELECT ON recent_sign_event_view TO PUBLIC;

CREATE TABLE event.travel_time_event (
    event_id SERIAL PRIMARY KEY,
    event_date TIMESTAMP WITH time zone DEFAULT NOW() NOT NULL,
    event_desc_id INTEGER NOT NULL
        REFERENCES event.event_description(event_desc_id),
    device_id VARCHAR(20),
    station_id VARCHAR(10)
);

CREATE VIEW travel_time_event_view AS
    SELECT event_id, event_date, event_description.description, device_id,
           station_id
    FROM event.travel_time_event
    JOIN event.event_description
    ON travel_time_event.event_desc_id = event_description.event_desc_id;
GRANT SELECT ON travel_time_event_view TO PUBLIC;

CREATE TABLE event.brightness_sample (
    event_id INTEGER PRIMARY KEY DEFAULT nextval('event.event_id_seq'),
    event_date TIMESTAMP WITH time zone DEFAULT NOW() NOT NULL,
    event_desc_id INTEGER NOT NULL
        REFERENCES event.event_description(event_desc_id),
    dms VARCHAR(20) NOT NULL REFERENCES iris._dms(name)
        ON DELETE CASCADE,
    photocell INTEGER NOT NULL,
    output INTEGER NOT NULL
);

--
-- Gate Arms
--
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

CREATE TABLE iris._gate_arm_array (
    name VARCHAR(20) PRIMARY KEY,
    geo_loc VARCHAR(20) NOT NULL REFERENCES iris.geo_loc,
    notes VARCHAR CHECK (LENGTH(notes) < 256),
    opposing BOOLEAN NOT NULL,
    prereq VARCHAR(20) REFERENCES iris._gate_arm_array,
    camera VARCHAR(20) REFERENCES iris._camera,
    approach VARCHAR(20) REFERENCES iris._camera,
    action_plan VARCHAR(16) UNIQUE REFERENCES iris.action_plan,
    arm_state INTEGER NOT NULL REFERENCES iris.gate_arm_state,
    interlock INTEGER NOT NULL REFERENCES iris.gate_arm_interlock
);

-- This constraint ensures that the name is unique among all devices
-- Gate arm arrays are *not* associated with controllers or pins
ALTER TABLE iris._gate_arm_array ADD CONSTRAINT _gate_arm_array_fkey
    FOREIGN KEY (name) REFERENCES iris.controller_io ON DELETE CASCADE;

CREATE TRIGGER gate_arm_array_hashtag_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris._gate_arm_array
    FOR EACH ROW EXECUTE FUNCTION iris.hashtag_trig('gate_arm_array');

CREATE FUNCTION iris.gate_arm_array_notify() RETURNS TRIGGER AS
    $gate_arm_array_notify$
BEGIN
    IF (NEW.notes IS DISTINCT FROM OLD.notes) OR
       (NEW.arm_state IS DISTINCT FROM OLD.arm_state) OR
       (NEW.interlock IS DISTINCT FROM OLD.interlock)
    THEN
        NOTIFY gate_arm_array;
    ELSE
        PERFORM pg_notify('gate_arm_array', NEW.name);
    END IF;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$gate_arm_array_notify$ LANGUAGE plpgsql;

CREATE TRIGGER gate_arm_array_notify_trig
    AFTER UPDATE ON iris._gate_arm_array
    FOR EACH ROW EXECUTE FUNCTION iris.gate_arm_array_notify();

CREATE TRIGGER gate_arm_array_table_notify_trig
    AFTER INSERT OR DELETE ON iris._gate_arm_array
    FOR EACH STATEMENT EXECUTE FUNCTION iris.table_notify();

CREATE VIEW iris.gate_arm_array AS
    SELECT ga.name, geo_loc, controller, pin, notes, opposing, prereq, camera,
           approach, action_plan, arm_state, interlock
    FROM iris._gate_arm_array ga
    JOIN iris.controller_io cio ON ga.name = cio.name;

CREATE FUNCTION iris.gate_arm_array_insert() RETURNS TRIGGER AS
    $gate_arm_array_insert$
BEGIN
    INSERT INTO iris.controller_io (name, resource_n, controller, pin)
         VALUES (NEW.name, 'gate_arm_array', NEW.controller, NEW.pin);
    INSERT INTO iris._gate_arm_array (
        name, geo_loc, notes, opposing, prereq, camera, approach, action_plan,
        arm_state, interlock
    ) VALUES (
        NEW.name, NEW.geo_loc, NEW.notes, NEW.opposing, NEW.prereq, NEW.camera,
        NEW.approach, NEW.action_plan, NEW.arm_state, NEW.interlock
    );
    RETURN NEW;
END;
$gate_arm_array_insert$ LANGUAGE plpgsql;

CREATE TRIGGER gate_arm_array_insert_trig
    INSTEAD OF INSERT ON iris.gate_arm_array
    FOR EACH ROW EXECUTE FUNCTION iris.gate_arm_array_insert();

CREATE FUNCTION iris.gate_arm_array_update() RETURNS TRIGGER AS
    $gate_arm_array_update$
BEGIN
    UPDATE iris.controller_io SET controller = NEW.controller, pin = NEW.pin
     WHERE name = OLD.name;
    UPDATE iris._gate_arm_array
       SET notes = NEW.notes,
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
    FOR EACH ROW EXECUTE FUNCTION iris.gate_arm_array_update();

CREATE TRIGGER gate_arm_array_delete_trig
    INSTEAD OF DELETE ON iris.gate_arm_array
    FOR EACH ROW EXECUTE FUNCTION iris.controller_io_delete();

CREATE VIEW gate_arm_array_view AS
    SELECT ga.name, ga.notes, ga.geo_loc, l.roadway, l.road_dir, l.cross_mod,
           l.cross_street, l.cross_dir, l.landmark, l.lat, l.lon, l.corridor,
           l.location, cio.controller, cio.pin, ctr.comm_link, ctr.drop_id,
           ctr.condition, ga.opposing, ga.prereq, ga.camera, ga.approach,
           ga.action_plan, gas.description AS arm_state,
           gai.description AS interlock
    FROM iris._gate_arm_array ga
    JOIN iris.controller_io cio ON ga.name = cio.name
    JOIN iris.gate_arm_state gas ON ga.arm_state = gas.id
    JOIN iris.gate_arm_interlock gai ON ga.interlock = gai.id
    LEFT JOIN geo_loc_view l ON ga.geo_loc = l.name
    LEFT JOIN controller_view ctr ON cio.controller = ctr.name;
GRANT SELECT ON gate_arm_array_view TO PUBLIC;

CREATE TABLE iris._gate_arm (
    name VARCHAR(20) PRIMARY KEY,
    ga_array VARCHAR(20) NOT NULL REFERENCES iris._gate_arm_array,
    idx INTEGER NOT NULL,
    notes VARCHAR CHECK (LENGTH(notes) < 256),
    arm_state INTEGER NOT NULL REFERENCES iris.gate_arm_state,
    fault VARCHAR(32)
);

ALTER TABLE iris._gate_arm ADD CONSTRAINT _gate_arm_fkey
    FOREIGN KEY (name) REFERENCES iris.controller_io ON DELETE CASCADE;

CREATE UNIQUE INDEX gate_arm_array_idx ON iris._gate_arm
    USING btree (ga_array, idx);

CREATE TRIGGER gate_arm_notify_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris._gate_arm
    FOR EACH STATEMENT EXECUTE FUNCTION iris.table_notify();

CREATE VIEW iris.gate_arm AS
    SELECT g.name, ga_array, idx, controller, pin, notes, arm_state, fault
    FROM iris._gate_arm g
    JOIN iris.controller_io cio ON g.name = cio.name;

CREATE FUNCTION iris.gate_arm_insert() RETURNS TRIGGER AS
    $gate_arm_insert$
BEGIN
    INSERT INTO iris.controller_io (name, resource_n, controller, pin)
         VALUES (NEW.name, 'gate_arm', NEW.controller, NEW.pin);
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
    FOR EACH ROW EXECUTE FUNCTION iris.gate_arm_insert();

CREATE FUNCTION iris.gate_arm_update() RETURNS TRIGGER AS
    $gate_arm_update$
BEGIN
    UPDATE iris.controller_io
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
    FOR EACH ROW EXECUTE FUNCTION iris.gate_arm_update();

CREATE TRIGGER gate_arm_delete_trig
    INSTEAD OF DELETE ON iris.gate_arm
    FOR EACH ROW EXECUTE FUNCTION iris.controller_io_delete();

CREATE VIEW gate_arm_view AS
    SELECT g.name, g.ga_array, g.notes, ga.geo_loc, l.roadway, l.road_dir,
           l.cross_mod, l.cross_street, l.cross_dir, l.landmark, l.lat, l.lon,
           l.corridor, l.location, cio.controller, cio.pin, ctr.comm_link,
           ctr.drop_id, ctr.condition, ga.opposing, ga.prereq, ga.camera,
           ga.approach, gas.description AS arm_state, fault
    FROM iris._gate_arm g
    JOIN iris.controller_io cio ON g.name = cio.name
    JOIN iris.gate_arm_state gas ON g.arm_state = gas.id
    JOIN iris._gate_arm_array ga ON g.ga_array = ga.name
    LEFT JOIN geo_loc_view l ON ga.geo_loc = l.name
    LEFT JOIN controller_view ctr ON cio.controller = ctr.name;
GRANT SELECT ON gate_arm_view TO PUBLIC;

CREATE TABLE event.gate_arm_event (
    id SERIAL PRIMARY KEY,
    event_date TIMESTAMP WITH time zone DEFAULT NOW() NOT NULL,
    event_desc INTEGER NOT NULL REFERENCES event.event_description,
    device_id VARCHAR(20),
    user_id VARCHAR(15),
    fault VARCHAR(32)
);

CREATE VIEW gate_arm_event_view AS
    SELECT ev.id, event_date, ed.description, device_id, user_id, fault
    FROM event.gate_arm_event ev
    JOIN event.event_description ed ON ev.event_desc = ed.event_desc_id;
GRANT SELECT ON gate_arm_event_view TO PUBLIC;

--
-- Alerts
--
CREATE TABLE cap.status (
    id INTEGER PRIMARY KEY,
    description VARCHAR(10) NOT NULL
);

COPY cap.status (id, description) FROM stdin;
0	unknown
1	actual
2	exercise
3	system
4	test
5	draft
\.

CREATE TABLE cap.msg_type (
    id INTEGER PRIMARY KEY,
    description VARCHAR(10) NOT NULL
);

COPY cap.msg_type (id, description) FROM stdin;
0	unknown
1	alert
2	update
3	cancel
4	ack
5	error
\.

CREATE TABLE cap.scope (
    id INTEGER PRIMARY KEY,
    description VARCHAR(10) NOT NULL
);

COPY cap.scope (id, description) FROM stdin;
0	unknown
1	public
2	restricted
3	private
\.

CREATE TABLE cap.category (
    id INTEGER PRIMARY KEY,
    description VARCHAR(10) NOT NULL
);

COPY cap.category (id, description) FROM stdin;
0	geo
1	met
2	safety
3	security
4	rescue
5	fire
6	health
7	env
8	transport
9	infra
10	CBRNE
11	other
\.

CREATE TABLE cap.event (
    code VARCHAR(3) PRIMARY KEY,
    description VARCHAR(32) NOT NULL
);

COPY cap.event (code, description) FROM stdin;
ADR	Administrative Message
SAB	Avalanche Advisory
AVW	Avalanche Warning
AVA	Avalanche Watch
BHS	Beach Hazards Statement
BZW	Blizzard Warning
DUY	Blowing Dust Advisory
BLU	Blue Alert
BWY	Brisk Wind Advisory
CAE	Child Abduction Emergency
CDW	Civil Danger Warning
CEM	Civil Emergency Message
CFW	Coastal Flood Warning
CFA	Coastal Flood Watch
CFY	Coastal Flood Advisory
FGY	Dense Fog Advisory
MFY	Dense Fog Advisory
DSY	Dust Advisory
DSW	Dust Storm Warning
EQW	Earthquake Warning
EAN	Emergency Action Notification
EAT	Emergency Action Termination
EVI	Evacuation Immediate
EWW	Extreme Wind Warning
FRW	Fire Warning
FFS	Flash Flood Statement
FFW	Flash Flood Warning
FFA	Flash Flood Watch
FSW	Flash Freeze Warning
FAY	Flood Advisory
FLY	Flood Advisory
FLS	Flood Statement
FAW	Flood Warning
FLW	Flood Warning
FAA	Flood Watch
FLA	Flood Watch
FWW	Red Flag Warning
FZW	Freeze Warning
FZA	Freeze Watch
ZFY	Freezing Fog Advisory
GLW	Gale Warning
GLA	Gale Watch
HZW	Hard Freeze Warning
HZA	Hard Freeze Watch
HMW	Hazardous Materials Warning
SEW	Hazardous Seas Warning
SEA	Hazardous Seas Watch
UPY	Heavy Freezing Spray Advisory
SUY	High Surf Advisory
SUW	High Surf Warning
HWW	High Wind Warning
HWA	High Wind Watch
HLS	Hurricane Statement
HUW	Hurricane Warning
HUA	Hurricane Watch
ISW	Ice Storm Warning
LEW	Law Enforcement Warning
LWY	Lake Wind Advisory
LAE	Local Area Emergency
LOY	Low Water Advisory
MWS	Marine Weather Statement
NAT	National Audible Test
NIC	National Information Center
NPT	National Periodic Test
NST	National Silent Test
NMN	Network Message Notification
NUW	Nuclear Power Plant Warning
DMO	Practice/Demo Warning
RHW	Radiological Hazard Warning
RMT	Required Monthly Test
RWT	Required Weekly Test
RPS	Rip Current Statement
SVW	Severe Thunderstorm Warning
SVA	Severe Thunderstorm Watch
SVS	Severe Weather Statement
SPW	Shelter in Place Warning
SCY	Small Craft Advisory
SQW	Snowsquall Warning
MAW	Special Marine Warning
SPS	Special Weather Statement
SRW	Storm Warning
SSW	Storm Surge Warning
SSA	Storm Surge Watch
TOE	911 Telephone Outage Emergency
TOW	Tornado Warning
TOA	Tornado Watch
TRW	Tropical Storm Warning
TRA	Tropical Storm Watch
TSW	Tsunami Warning
TSA	Tsunami Watch
VOW	Volcano Warning
WIY	Wind Advisory
WCY	Wind Chill Advisory
WCW	Wind Chill Warning
WCA	Wind Chill Watch
WSW	Winter Storm Warning
WSA	Winter Storm Watch
WWY	Winter Weather Advisory
\.

CREATE TABLE cap.response_type (
    id INTEGER PRIMARY KEY,
    description VARCHAR(10) NOT NULL
);

COPY cap.response_type (id, description) FROM stdin;
0	shelter
1	evacuate
2	prepare
3	execute
4	avoid
5	monitor
6	assess
7	allclear
8	none
\.

CREATE TABLE cap.urgency (
    id INTEGER PRIMARY KEY,
    description VARCHAR(10) NOT NULL
);

COPY cap.urgency (id, description) FROM stdin;
0	unknown
1	past
2	future
3	expected
4	immediate
\.

CREATE TABLE cap.severity (
    id INTEGER PRIMARY KEY,
    description VARCHAR(10) NOT NULL
);

COPY cap.severity(id, description) FROM stdin;
0	unknown
1	minor
2	moderate
3	severe
4	extreme
\.

CREATE TABLE cap.certainty (
    id INTEGER PRIMARY KEY,
    description VARCHAR(10) NOT NULL
);

COPY cap.certainty(id, description) FROM stdin;
0	unknown
1	unlikely
2	possible
3	likely
4	observed
\.

CREATE TABLE iris.alert_config (
    name VARCHAR(20) PRIMARY KEY,
    event VARCHAR(3) REFERENCES cap.event,
    response_shelter BOOLEAN NOT NULL,
    response_evacuate BOOLEAN NOT NULL,
    response_prepare BOOLEAN NOT NULL,
    response_execute BOOLEAN NOT NULL,
    response_avoid BOOLEAN NOT NULL,
    response_monitor BOOLEAN NOT NULL,
    response_all_clear BOOLEAN NOT NULL,
    response_none BOOLEAN NOT NULL,
    urgency_unknown BOOLEAN NOT NULL,
    urgency_past BOOLEAN NOT NULL,
    urgency_future BOOLEAN NOT NULL,
    urgency_expected BOOLEAN NOT NULL,
    urgency_immediate BOOLEAN NOT NULL,
    severity_unknown BOOLEAN NOT NULL,
    severity_minor BOOLEAN NOT NULL,
    severity_moderate BOOLEAN NOT NULL,
    severity_severe BOOLEAN NOT NULL,
    severity_extreme BOOLEAN NOT NULL,
    certainty_unknown BOOLEAN NOT NULL,
    certainty_unlikely BOOLEAN NOT NULL,
    certainty_possible BOOLEAN NOT NULL,
    certainty_likely BOOLEAN NOT NULL,
    certainty_observed BOOLEAN NOT NULL,
    auto_deploy BOOLEAN NOT NULL,
    before_period_hours INTEGER NOT NULL,
    after_period_hours INTEGER NOT NULL,
    dms_hashtag VARCHAR(16),

    CONSTRAINT hashtag_ck CHECK (dms_hashtag ~ '^#[A-Za-z0-9]+$')
);

CREATE TABLE iris.alert_period (
    id INTEGER PRIMARY KEY,
    description VARCHAR(10) NOT NULL
);

COPY iris.alert_period(id, description) FROM stdin;
0	before
1	during
2	after
\.

CREATE TABLE iris.alert_message (
    name VARCHAR(20) PRIMARY KEY,
    alert_config VARCHAR(20) NOT NULL REFERENCES iris.alert_config,
    alert_period INTEGER NOT NULL REFERENCES iris.alert_period,
    msg_pattern VARCHAR(20) REFERENCES iris.msg_pattern,
    sign_config VARCHAR(16) REFERENCES iris.sign_config
);

CREATE TABLE cap.alert (
    identifier VARCHAR(128) PRIMARY KEY,
    alert JSONB NOT NULL,
    receive_date TIMESTAMP WITH time zone NOT NULL
);

CREATE TABLE iris.alert_state (
    id INTEGER PRIMARY KEY,
    description VARCHAR(12) NOT NULL
);

COPY iris.alert_state(id, description) FROM stdin;
0	pending
1	active
2	cleared
3	active_req
4	cleared_req
\.

CREATE TABLE cap.alert_info (
    name VARCHAR(20) PRIMARY KEY,
    alert VARCHAR(128) NOT NULL REFERENCES cap.alert(identifier),
    replaces VARCHAR(24) REFERENCES cap.alert_info,
    start_date TIMESTAMP WITH time zone,
    end_date TIMESTAMP WITH time zone,
    event VARCHAR(3) NOT NULL REFERENCES cap.event,
    response_type INTEGER NOT NULL REFERENCES cap.response_type,
    urgency INTEGER NOT NULL REFERENCES cap.urgency,
    severity INTEGER NOT NULL REFERENCES cap.severity,
    certainty INTEGER NOT NULL REFERENCES cap.certainty,
    headline VARCHAR(256),
    description VARCHAR(4096),
    instruction VARCHAR(4096),
    area_desc VARCHAR(256) NOT NULL,
    geo_poly geometry(multipolygon) NOT NULL,
    lat double precision NOT NULL,
    lon double precision NOT NULL,
    all_hashtag VARCHAR(16) NOT NULL,
    action_plan VARCHAR(16) NOT NULL REFERENCES iris.action_plan,
    alert_state INTEGER NOT NULL REFERENCES iris.alert_state,

    CONSTRAINT hashtag_ck CHECK (all_hashtag ~ '^#[A-Za-z0-9]+$')
);

--
-- Lane-Use Control Signals
--
CREATE TABLE iris.lcs_type (
    id INTEGER PRIMARY KEY,
    description VARCHAR NOT NULL
);

INSERT INTO iris.lcs_type (id, description)
VALUES
    (0, 'Over lane dedicated'),
    (1, 'Over lane DMS'),
    (2, 'Pavement LED');

CREATE TABLE iris._lcs (
    name VARCHAR(20) PRIMARY KEY,
    geo_loc VARCHAR(20) NOT NULL REFERENCES iris.geo_loc,
    notes VARCHAR CHECK (LENGTH(notes) < 256),
    lcs_type INTEGER NOT NULL REFERENCES iris.lcs_type,
    shift INTEGER NOT NULL,
    lock JSONB,
    status JSONB
);

ALTER TABLE iris._lcs ADD CONSTRAINT _lcs_fkey
    FOREIGN KEY (name) REFERENCES iris.controller_io ON DELETE CASCADE;

CREATE TRIGGER lcs_notify_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris._lcs
    FOR EACH STATEMENT EXECUTE FUNCTION iris.table_notify();

CREATE VIEW iris.lcs AS
    SELECT l.name, geo_loc, controller, pin, notes, preset, lcs_type, shift,
           lock, status
    FROM iris._lcs l
    JOIN iris.controller_io cio ON l.name = cio.name
    JOIN iris.device_preset p ON l.name = p.name;

CREATE FUNCTION iris.lcs_insert() RETURNS TRIGGER AS
    $lcs_insert$
BEGIN
    INSERT INTO iris.controller_io (name, resource_n, controller, pin)
         VALUES (NEW.name, 'lcs', NEW.controller, NEW.pin);
    INSERT INTO iris.device_preset (name, resource_n, preset)
        VALUES (NEW.name, 'lcs', NEW.preset);
    INSERT INTO iris._lcs (
        name, geo_loc, notes, lcs_type, shift, lock, status
    ) VALUES (
        NEW.name, NEW.geo_loc, NEW.notes, NEW.lcs_type, NEW.shift,
        NEW.lock, NEW.status
     );
    RETURN NEW;
END;
$lcs_insert$ LANGUAGE plpgsql;

CREATE TRIGGER lcs_insert_trig
    INSTEAD OF INSERT ON iris.lcs
    FOR EACH ROW EXECUTE FUNCTION iris.lcs_insert();

CREATE FUNCTION iris.lcs_update() RETURNS TRIGGER AS
    $lcs_update$
BEGIN
    UPDATE iris.controller_io
       SET controller = NEW.controller,
           pin = NEW.pin
     WHERE name = OLD.name;
    UPDATE iris.device_preset
       SET preset = NEW.preset
     WHERE name = OLD.name;
    UPDATE iris._lcs
       SET geo_loc = NEW.geo_loc,
           notes = NEW.notes,
           lcs_type = NEW.lcs_type,
           shift = NEW.shift,
           lock = NEW.lock,
           status = NEW.status
     WHERE name = OLD.name;
    RETURN NEW;
END;
$lcs_update$ LANGUAGE plpgsql;

CREATE TRIGGER lcs_update_trig
    INSTEAD OF UPDATE ON iris.lcs
    FOR EACH ROW EXECUTE FUNCTION iris.lcs_update();

CREATE TRIGGER lcs_delete_trig
    INSTEAD OF DELETE ON iris.lcs
    FOR EACH ROW EXECUTE FUNCTION iris.controller_io_delete();

CREATE VIEW lcs_view AS
    SELECT l.name, geo_loc, controller, pin, notes, camera, preset_num,
           lt.description AS lcs_type, shift, lock, status
    FROM iris._lcs l
    JOIN iris.controller_io cio ON l.name = cio.name
    LEFT JOIN iris.device_preset p ON l.name = p.name
    LEFT JOIN iris.camera_preset cp ON cp.name = p.preset
    LEFT JOIN iris.lcs_type lt ON l.lcs_type = lt.id;
GRANT SELECT ON lcs_view TO PUBLIC;

CREATE TABLE iris.lcs_indication (
    id INTEGER PRIMARY KEY,
    description VARCHAR NOT NULL
);

INSERT INTO iris.lcs_indication (id, description)
VALUES
    (0, 'Unknown'),
    (1, 'Dark'),
    (2, 'Lane open'),
    (3, 'Use caution'),
    (4, 'Lane closed ahead'),
    (5, 'Lane closed'),
    (6, 'Merge right'),
    (7, 'Merge left'),
    (8, 'Merge left or right'),
    (9, 'Must exit right'),
    (10, 'Must exit left'),
    (11, 'HOV / HOT'),
    (12, 'HOV / HOT begins'),
    (13, 'Variable speed advisory'),
    (14, 'Variable speed limit'),
    (15, 'Low visibility');

CREATE TABLE iris._lcs_state (
    name VARCHAR(20) PRIMARY KEY,
    lcs VARCHAR(20) NOT NULL REFERENCES iris._lcs,
    lane INTEGER NOT NULL,
    indication INTEGER NOT NULL REFERENCES iris.lcs_indication,
    msg_pattern VARCHAR(20) REFERENCES iris.msg_pattern,
    msg_num INTEGER
);
CREATE UNIQUE INDEX lcs_lane_indication
    ON iris._lcs_state USING btree (lcs, lane, indication);

ALTER TABLE iris._lcs_state ADD CONSTRAINT _lcs_state_fkey
    FOREIGN KEY (name) REFERENCES iris.controller_io ON DELETE CASCADE;

CREATE TRIGGER lcs_state_notify_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris._lcs_state
    FOR EACH STATEMENT EXECUTE FUNCTION iris.table_notify();

CREATE VIEW iris.lcs_state AS
    SELECT ls.name, controller, pin, lcs, lane, indication, msg_pattern,
           msg_num
    FROM iris._lcs_state ls
    JOIN iris.controller_io cio ON ls.name = cio.name;

CREATE FUNCTION iris.lcs_state_insert() RETURNS TRIGGER AS
    $lcs_state_insert$
BEGIN
    INSERT INTO iris.controller_io (name, resource_n, controller, pin)
         VALUES (NEW.name, 'lcs_state', NEW.controller, NEW.pin);
    INSERT INTO iris._lcs_state
                (name, lcs, lane, indication, msg_pattern, msg_num)
         VALUES (NEW.name, NEW.lcs, NEW.lane, NEW.indication,
                 NEW.msg_pattern, NEW.msg_num);
    RETURN NEW;
END;
$lcs_state_insert$ LANGUAGE plpgsql;

CREATE TRIGGER lcs_state_insert_trig
    INSTEAD OF INSERT ON iris.lcs_state
    FOR EACH ROW EXECUTE FUNCTION iris.lcs_state_insert();

CREATE FUNCTION iris.lcs_state_update() RETURNS TRIGGER AS
    $lcs_state_update$
BEGIN
    UPDATE iris.controller_io
       SET controller = NEW.controller,
           pin = NEW.pin
     WHERE name = OLD.name;
    UPDATE iris._lcs_state
       SET lcs = NEW.lcs,
           lane = NEW.lane,
           indication = NEW.indication,
           msg_pattern = NEW.msg_pattern,
           msg_num = NEW.msg_num
     WHERE name = OLD.name;
    RETURN NEW;
END;
$lcs_state_update$ LANGUAGE plpgsql;

CREATE TRIGGER lcs_state_update_trig
    INSTEAD OF UPDATE ON iris.lcs_state
    FOR EACH ROW EXECUTE FUNCTION iris.lcs_state_update();

CREATE TRIGGER lcs_state_delete_trig
    INSTEAD OF DELETE ON iris.lcs_state
    FOR EACH ROW EXECUTE FUNCTION iris.controller_io_delete();

CREATE VIEW lcs_state_view AS
    SELECT name, controller, pin, lcs, lane, description AS indication,
           msg_pattern, msg_num
    FROM iris.lcs_state
    JOIN iris.lcs_indication ON indication = id;
GRANT SELECT ON lcs_state_view TO PUBLIC;

--
-- Parking Areas
--
CREATE TABLE iris.parking_area (
    name VARCHAR(20) PRIMARY KEY,
    geo_loc VARCHAR(20) NOT NULL REFERENCES iris.geo_loc(name),
    preset_1 VARCHAR(20) REFERENCES iris.camera_preset(name),
    preset_2 VARCHAR(20) REFERENCES iris.camera_preset(name),
    preset_3 VARCHAR(20) REFERENCES iris.camera_preset(name),
    -- static site data
    site_id VARCHAR(25) UNIQUE,
    time_stamp_static TIMESTAMP WITH time zone,
    relevant_highway VARCHAR(10),
    reference_post VARCHAR(10),
    exit_id VARCHAR(10),
    facility_name VARCHAR(30),
    street_adr VARCHAR(30),
    city VARCHAR(30),
    state VARCHAR(2),
    zip VARCHAR(10),
    time_zone VARCHAR(10),
    ownership VARCHAR(2),
    capacity INTEGER,
    low_threshold INTEGER,
    amenities INTEGER,
    -- dynamic site data
    time_stamp TIMESTAMP WITH time zone,
    reported_available VARCHAR(8),
    true_available INTEGER,
    trend VARCHAR(8),
    open BOOLEAN,
    trust_data BOOLEAN,
    last_verification_check TIMESTAMP WITH time zone,
    verification_check_amplitude INTEGER
);

CREATE FUNCTION iris.parking_area_notify() RETURNS TRIGGER AS
    $parking_area_notify$
BEGIN
    IF (NEW.time_stamp_static IS DISTINCT FROM OLD.time_stamp_static) THEN
        NOTIFY parking_area;
    ELSE
        PERFORM pg_notify('parking_area', NEW.name);
    END IF;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$parking_area_notify$ LANGUAGE plpgsql;

CREATE TRIGGER parking_area_notify_trig
    AFTER UPDATE ON iris.parking_area
    FOR EACH ROW EXECUTE FUNCTION iris.parking_area_notify();

CREATE TRIGGER parking_area_table_notify_trig
    AFTER INSERT OR DELETE ON iris.parking_area
    FOR EACH STATEMENT EXECUTE FUNCTION iris.table_notify();

CREATE TABLE iris.parking_area_amenities (
    bit INTEGER PRIMARY KEY,
    amenity VARCHAR(32) NOT NULL
);
ALTER TABLE iris.parking_area_amenities ADD CONSTRAINT amenity_bit_ck
    CHECK (bit >= 0 AND bit < 32);

COPY iris.parking_area_amenities (bit, amenity) FROM stdin;
0	Flush toilet
1	Assisted restroom
2	Drinking fountain
3	Shower
4	Picnic table
5	Picnic shelter
6	Pay phone
7	TTY pay phone
8	Wireless internet
9	ATM
10	Vending machine
11	Shop
12	Play area
13	Pet excercise area
14	Interpretive information
15	Family restroom
\.

CREATE FUNCTION iris.parking_area_amenities(INTEGER)
    RETURNS SETOF iris.parking_area_amenities AS $parking_area_amenities$
DECLARE
    ms RECORD;
    b INTEGER;
BEGIN
    FOR ms IN SELECT bit, amenity FROM iris.parking_area_amenities LOOP
        b = 1 << ms.bit;
        IF ($1 & b) = b THEN
            RETURN NEXT ms;
        END IF;
    END LOOP;
END;
$parking_area_amenities$ LANGUAGE plpgsql SECURITY DEFINER;

ALTER FUNCTION iris.parking_area_amenities(INTEGER)
    SET search_path = pg_catalog, pg_temp;

CREATE VIEW parking_area_view AS
    SELECT pa.name, site_id, time_stamp_static, relevant_highway,
           reference_post, exit_id, facility_name, street_adr, city, state,
           zip, time_zone, ownership, capacity, low_threshold,
           (SELECT string_agg(a.amenity, ', ') FROM
            (SELECT bit, amenity FROM iris.parking_area_amenities(amenities)
             ORDER BY bit) AS a) AS amenities,
           time_stamp, reported_available, true_available, trend, open,
           trust_data, last_verification_check, verification_check_amplitude,
           p1.camera AS camera_1, p2.camera AS camera_2,
           p3.camera AS camera_3,
           l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir,
           l.landmark, l.lat, l.lon, l.corridor, l.location,
           sa.value AS camera_image_base_url
    FROM iris.parking_area pa
    LEFT JOIN iris.camera_preset p1 ON preset_1 = p1.name
    LEFT JOIN iris.camera_preset p2 ON preset_2 = p2.name
    LEFT JOIN iris.camera_preset p3 ON preset_3 = p3.name
    LEFT JOIN geo_loc_view l ON pa.geo_loc = l.name
    LEFT JOIN iris.system_attribute sa ON sa.name = 'camera_image_base_url';
GRANT SELECT ON parking_area_view TO PUBLIC;

--
-- Ramp Meters
--
CREATE TABLE iris.meter_type (
    id INTEGER PRIMARY KEY,
    description VARCHAR(32) NOT NULL,
    lanes INTEGER NOT NULL
);

COPY iris.meter_type (id, description, lanes) FROM stdin;
0	One Lane	1
1	Two Lane, Alternate Release	2
2	Two Lane, Simultaneous Release	2
\.

CREATE TABLE iris.meter_algorithm (
    id INTEGER PRIMARY KEY,
    description VARCHAR(32) NOT NULL
);

COPY iris.meter_algorithm (id, description) FROM stdin;
0	No Metering
1	Simple Metering
2	SZM (obsolete)
3	K Adaptive Metering
\.

CREATE TABLE iris._ramp_meter (
    name VARCHAR(20) PRIMARY KEY,
    geo_loc VARCHAR(20) NOT NULL REFERENCES iris.geo_loc(name),
    notes VARCHAR CHECK (LENGTH(notes) < 256),
    meter_type INTEGER NOT NULL REFERENCES iris.meter_type,
    storage INTEGER NOT NULL CHECK (storage >= 0),
    max_wait INTEGER NOT NULL CHECK (max_wait > 0),
    algorithm INTEGER NOT NULL REFERENCES iris.meter_algorithm,
    am_target INTEGER NOT NULL CHECK (am_target >= 0),
    pm_target INTEGER NOT NULL CHECK (pm_target >= 0),
    -- FIXME: only allow one reference to a beacon
    beacon VARCHAR(20) REFERENCES iris._beacon,
    lock JSONB,
    status JSONB
);

ALTER TABLE iris._ramp_meter ADD CONSTRAINT _ramp_meter_fkey
    FOREIGN KEY (name) REFERENCES iris.controller_io ON DELETE CASCADE;

CREATE TRIGGER ramp_meter_hashtag_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris._ramp_meter
    FOR EACH ROW EXECUTE FUNCTION iris.hashtag_trig('ramp_meter');

CREATE FUNCTION iris.ramp_meter_notify() RETURNS TRIGGER AS
    $ramp_meter_notify$
BEGIN
    IF (NEW.notes IS DISTINCT FROM OLD.notes) OR
       (NEW.lock IS DISTINCT FROM OLD.lock) OR
       (NEW.status IS DISTINCT FROM OLD.status)
    THEN
        NOTIFY ramp_meter;
    ELSE
        PERFORM pg_notify('ramp_meter', NEW.name);
    END IF;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$ramp_meter_notify$ LANGUAGE plpgsql;

CREATE TRIGGER ramp_meter_notify_trig
    AFTER UPDATE ON iris._ramp_meter
    FOR EACH ROW EXECUTE FUNCTION iris.ramp_meter_notify();

CREATE TRIGGER ramp_meter_table_notify_trig
    AFTER INSERT OR DELETE ON iris._ramp_meter
    FOR EACH STATEMENT EXECUTE FUNCTION iris.table_notify();

CREATE VIEW iris.ramp_meter AS
    SELECT m.name, geo_loc, controller, pin, notes, meter_type, storage,
           max_wait, algorithm, am_target, pm_target, beacon, preset,
           lock, status
    FROM iris._ramp_meter m
    JOIN iris.controller_io cio ON m.name = cio.name
    JOIN iris.device_preset p ON m.name = p.name;

CREATE FUNCTION iris.ramp_meter_insert() RETURNS TRIGGER AS
    $ramp_meter_insert$
BEGIN
    INSERT INTO iris.controller_io (name, resource_n, controller, pin)
         VALUES (NEW.name, 'ramp_meter', NEW.controller, NEW.pin);
    INSERT INTO iris.device_preset (name, resource_n, preset)
         VALUES (NEW.name, 'ramp_meter', NEW.preset);
    INSERT INTO iris._ramp_meter (
        name, geo_loc, notes, meter_type, storage, max_wait, algorithm,
        am_target, pm_target, beacon, lock, status
    ) VALUES (
        NEW.name, NEW.geo_loc, NEW.notes, NEW.meter_type, NEW.storage,
        NEW.max_wait, NEW.algorithm, NEW.am_target, NEW.pm_target, NEW.beacon,
        NEW.lock, NEW.status
    );
    RETURN NEW;
END;
$ramp_meter_insert$ LANGUAGE plpgsql;

CREATE TRIGGER ramp_meter_insert_trig
    INSTEAD OF INSERT ON iris.ramp_meter
    FOR EACH ROW EXECUTE FUNCTION iris.ramp_meter_insert();

CREATE FUNCTION iris.ramp_meter_update() RETURNS TRIGGER AS
    $ramp_meter_update$
BEGIN
    UPDATE iris.controller_io
       SET controller = NEW.controller,
           pin = NEW.pin
     WHERE name = OLD.name;
    UPDATE iris.device_preset
       SET preset = NEW.preset
     WHERE name = OLD.name;
    UPDATE iris._ramp_meter
       SET notes = NEW.notes,
           meter_type = NEW.meter_type,
           storage = NEW.storage,
           max_wait = NEW.max_wait,
           algorithm = NEW.algorithm,
           am_target = NEW.am_target,
           pm_target = NEW.pm_target,
           beacon = NEW.beacon,
           lock = NEW.lock,
           status = NEW.status
     WHERE name = OLD.name;
    RETURN NEW;
END;
$ramp_meter_update$ LANGUAGE plpgsql;

CREATE TRIGGER ramp_meter_update_trig
    INSTEAD OF UPDATE ON iris.ramp_meter
    FOR EACH ROW EXECUTE FUNCTION iris.ramp_meter_update();

CREATE TRIGGER ramp_meter_delete_trig
    INSTEAD OF DELETE ON iris.ramp_meter
    FOR EACH ROW EXECUTE FUNCTION iris.controller_io_delete();

CREATE VIEW ramp_meter_view AS
    SELECT m.name, geo_loc, cio.controller, cio.pin, notes,
           mt.description AS meter_type, storage, max_wait,
           alg.description AS algorithm, am_target, pm_target, beacon,
           camera, preset_num, lock, status,
           l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir,
           l.landmark, l.lat, l.lon, l.corridor, l.location, l.rd
    FROM iris._ramp_meter m
    JOIN iris.controller_io cio ON m.name = cio.name
    LEFT JOIN iris.device_preset p ON m.name = p.name
    LEFT JOIN iris.camera_preset cp ON cp.name = p.preset
    LEFT JOIN iris.meter_type mt ON m.meter_type = mt.id
    LEFT JOIN iris.meter_algorithm alg ON m.algorithm = alg.id
    LEFT JOIN geo_loc_view l ON m.geo_loc = l.name;
GRANT SELECT ON ramp_meter_view TO PUBLIC;

CREATE VIEW meter_action_view AS
    SELECT h.name AS ramp_meter, da.action_plan, ta.phase, h.hashtag,
           msg_pattern, time_of_day, day_plan, sched_date
    FROM iris.device_action da
    JOIN iris.hashtag h ON h.hashtag = da.hashtag AND resource_n = 'ramp_meter'
    JOIN iris.action_plan ap ON da.action_plan = ap.name
    LEFT JOIN iris.time_action ta ON ta.action_plan = ap.name
    WHERE active = true
    ORDER BY ramp_meter, time_of_day;
GRANT SELECT ON meter_action_view TO PUBLIC;

CREATE TABLE iris.metering_phase (
    id INTEGER PRIMARY KEY,
    description VARCHAR(16) NOT NULL
);

INSERT INTO iris.metering_phase (id, description)
VALUES
    (0, 'not started'),
    (1, 'metering'),
    (2, 'flushing'),
    (3, 'stopped');

CREATE TABLE iris.meter_queue_state (
    id INTEGER PRIMARY KEY,
    description VARCHAR(16) NOT NULL
);

INSERT INTO iris.meter_queue_state (id, description)
VALUES
    (0, 'unknown'),
    (1, 'empty'),
    (2, 'exists'),
    (3, 'full');

CREATE TABLE iris.meter_limit_control (
    id INTEGER PRIMARY KEY,
    description VARCHAR(16) NOT NULL
);

INSERT INTO iris.meter_limit_control (id, description)
VALUES
    (0, 'passage fail'),
    (1, 'storage limit'),
    (2, 'wait limit'),
    (3, 'target minimum'),
    (4, 'backup limit');

CREATE TABLE event.meter_event (
    id SERIAL PRIMARY KEY,
    event_date TIMESTAMP WITH time zone DEFAULT NOW() NOT NULL,
    event_desc INTEGER NOT NULL REFERENCES event.event_description,
    ramp_meter VARCHAR(20) NOT NULL REFERENCES iris._ramp_meter
        ON DELETE CASCADE,
    phase INTEGER NOT NULL REFERENCES iris.metering_phase,
    q_state INTEGER NOT NULL REFERENCES iris.meter_queue_state,
    q_len REAL NOT NULL,
    dem_adj REAL NOT NULL,
    wait_secs INTEGER NOT NULL,
    limit_ctrl INTEGER NOT NULL REFERENCES iris.meter_limit_control,
    min_rate INTEGER NOT NULL,
    rel_rate INTEGER NOT NULL,
    max_rate INTEGER NOT NULL,
    d_node VARCHAR(10),
    seg_density REAL NOT NULL
);

CREATE VIEW meter_event_view AS
    SELECT me.id, event_date, ed.description, ramp_meter,
           mp.description AS phase, qs.description AS q_state, q_len, dem_adj,
           wait_secs, lc.description AS limit_ctrl, min_rate, rel_rate,
           max_rate, d_node, seg_density
    FROM event.meter_event me
    JOIN event.event_description ed ON me.event_desc = ed.event_desc_id
    JOIN iris.metering_phase mp ON phase = mp.id
    JOIN iris.meter_queue_state qs ON q_state = qs.id
    JOIN iris.meter_limit_control lc ON limit_ctrl = lc.id;
GRANT SELECT ON meter_event_view TO PUBLIC;

CREATE TABLE event.meter_lock_event (
    id SERIAL PRIMARY KEY,
    event_date TIMESTAMP WITH time zone DEFAULT NOW() NOT NULL,
    event_desc INTEGER NOT NULL REFERENCES event.event_description,
    ramp_meter VARCHAR(20) NOT NULL REFERENCES iris._ramp_meter
        ON DELETE CASCADE,
    lock JSONB
);

CREATE VIEW meter_lock_event_view AS
    SELECT ev.id, event_date, ed.description, ramp_meter, lock
    FROM event.meter_lock_event ev
    JOIN event.event_description ed ON ev.event_desc = ed.event_desc_id;
GRANT SELECT ON meter_lock_event_view TO PUBLIC;

--
-- Toll Zones, Tag Readers
--
CREATE TABLE iris.toll_zone (
    name VARCHAR(20) PRIMARY KEY,
    start_id VARCHAR(10) REFERENCES iris.r_node(station_id),
    end_id VARCHAR(10) REFERENCES iris.r_node(station_id),
    tollway VARCHAR(16),
    alpha REAL,
    beta REAL,
    max_price REAL
);

CREATE FUNCTION iris.toll_zone_notify() RETURNS TRIGGER AS
    $toll_zone_notify$
BEGIN
    IF (NEW.tollway IS DISTINCT FROM OLD.tollway) THEN
        NOTIFY toll_zone;
    ELSE
        PERFORM pg_notify('toll_zone', NEW.name);
    END IF;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$toll_zone_notify$ LANGUAGE plpgsql;

CREATE TRIGGER toll_zone_notify_trig
    AFTER UPDATE ON iris.toll_zone
    FOR EACH ROW EXECUTE FUNCTION iris.toll_zone_notify();

CREATE TRIGGER toll_zone_table_notify_trig
    AFTER INSERT OR DELETE ON iris.toll_zone
    FOR EACH STATEMENT EXECUTE FUNCTION iris.table_notify();

CREATE VIEW toll_zone_view AS
    SELECT name, start_id, end_id, tollway, alpha, beta, max_price
    FROM iris.toll_zone;
GRANT SELECT ON toll_zone_view TO PUBLIC;

CREATE TABLE iris._tag_reader (
    name VARCHAR(20) PRIMARY KEY,
    geo_loc VARCHAR(20) NOT NULL REFERENCES iris.geo_loc(name),
    notes VARCHAR CHECK (LENGTH(notes) < 256),
    toll_zone VARCHAR(20) REFERENCES iris.toll_zone(name),
    settings JSONB
);

ALTER TABLE iris._tag_reader ADD CONSTRAINT _tag_reader_fkey
    FOREIGN KEY (name) REFERENCES iris.controller_io ON DELETE CASCADE;

CREATE FUNCTION iris.tag_reader_notify() RETURNS TRIGGER AS
    $tag_reader_notify$
BEGIN
    IF (NEW.notes IS DISTINCT FROM OLD.notes) THEN
        NOTIFY tag_reader;
    ELSE
        PERFORM pg_notify('tag_reader', NEW.name);
    END IF;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$tag_reader_notify$ LANGUAGE plpgsql;

CREATE TRIGGER tag_reader_notify_trig
    AFTER UPDATE ON iris._tag_reader
    FOR EACH ROW EXECUTE FUNCTION iris.tag_reader_notify();

CREATE TRIGGER tag_reader_table_notify_trig
    AFTER INSERT OR DELETE ON iris._tag_reader
    FOR EACH STATEMENT EXECUTE FUNCTION iris.table_notify();

CREATE VIEW iris.tag_reader AS
    SELECT t.name, geo_loc, controller, pin, notes, toll_zone, settings
    FROM iris._tag_reader t
    JOIN iris.controller_io cio ON t.name = cio.name;

CREATE FUNCTION iris.tag_reader_insert() RETURNS TRIGGER AS
    $tag_reader_insert$
BEGIN
    INSERT INTO iris.controller_io (name, resource_n, controller, pin)
         VALUES (NEW.name, 'tag_reader', NEW.controller, NEW.pin);
    INSERT INTO iris._tag_reader (
        name, geo_loc, notes, toll_zone, settings
    ) VALUES (
        NEW.name, NEW.geo_loc, NEW.notes, NEW.toll_zone, NEW.settings
    );
    RETURN NEW;
END;
$tag_reader_insert$ LANGUAGE plpgsql;

CREATE TRIGGER tag_reader_insert_trig
    INSTEAD OF INSERT ON iris.tag_reader
    FOR EACH ROW EXECUTE FUNCTION iris.tag_reader_insert();

CREATE FUNCTION iris.tag_reader_update() RETURNS TRIGGER AS
    $tag_reader_update$
BEGIN
    UPDATE iris.controller_io
       SET controller = NEW.controller,
           pin = NEW.pin
     WHERE name = OLD.name;
    UPDATE iris._tag_reader
       SET notes = NEW.notes,
           toll_zone = NEW.toll_zone,
           settings = NEW.settings
     WHERE name = OLD.name;
    RETURN NEW;
END;
$tag_reader_update$ LANGUAGE plpgsql;

CREATE TRIGGER tag_reader_update_trig
    INSTEAD OF UPDATE ON iris.tag_reader
    FOR EACH ROW EXECUTE FUNCTION iris.tag_reader_update();

CREATE TRIGGER tag_reader_delete_trig
    INSTEAD OF DELETE ON iris.tag_reader
    FOR EACH ROW EXECUTE FUNCTION iris.controller_io_delete();

CREATE VIEW tag_reader_view AS
    SELECT t.name, t.geo_loc, location, controller, pin, notes, toll_zone,
           settings
    FROM iris._tag_reader t
    JOIN iris.controller_io cio ON t.name = cio.name
    LEFT JOIN geo_loc_view l ON t.geo_loc = l.name;
GRANT SELECT ON tag_reader_view TO PUBLIC;

CREATE TABLE iris.tag_reader_dms (
    tag_reader VARCHAR(20) NOT NULL REFERENCES iris._tag_reader,
    dms VARCHAR(20) NOT NULL REFERENCES iris._dms
);
ALTER TABLE iris.tag_reader_dms ADD PRIMARY KEY (tag_reader, dms);

CREATE VIEW tag_reader_dms_view AS
    SELECT tag_reader, dms
    FROM iris.tag_reader_dms;
GRANT SELECT ON tag_reader_dms_view TO PUBLIC;

CREATE TABLE event.tag_type (
    id INTEGER PRIMARY KEY,
    description VARCHAR(16) NOT NULL
);

COPY event.tag_type (id, description) FROM stdin;
0	Unknown
1	SeGo
2	IAG
3	6C
\.

CREATE TABLE event.tag_read_event (
    event_id SERIAL PRIMARY KEY,
    event_date TIMESTAMP WITH time zone DEFAULT NOW() NOT NULL,
    event_desc_id INTEGER NOT NULL
        REFERENCES event.event_description(event_desc_id),
    tag_type INTEGER NOT NULL REFERENCES event.tag_type,
    agency INTEGER,
    tag_id INTEGER NOT NULL,
    tag_reader VARCHAR(20) NOT NULL,
    hov BOOLEAN NOT NULL,
    trip_id INTEGER
);

CREATE INDEX ON event.tag_read_event(tag_id);

CREATE VIEW tag_read_event_view AS
    SELECT event_id, event_date, event_description.description,
           tag_type.description AS tag_type, agency, tag_id, tag_reader,
           toll_zone, tollway, hov, trip_id
    FROM event.tag_read_event
    JOIN event.event_description
    ON tag_read_event.event_desc_id = event_description.event_desc_id
    JOIN event.tag_type
    ON tag_read_event.tag_type = tag_type.id
    JOIN iris._tag_reader
    ON tag_read_event.tag_reader = _tag_reader.name
    LEFT JOIN iris.toll_zone
    ON _tag_reader.toll_zone = toll_zone.name;
GRANT SELECT ON tag_read_event_view TO PUBLIC;

-- Allow trip_id column to be updated by roles which have been granted
-- update permission on tag_read_event_view
CREATE FUNCTION event.tag_read_event_view_update() RETURNS TRIGGER AS
    $tag_read_event_view_update$
BEGIN
    UPDATE event.tag_read_event
       SET trip_id = NEW.trip_id
     WHERE event_id = OLD.event_id;
    RETURN NEW;
END;
$tag_read_event_view_update$ LANGUAGE plpgsql;

CREATE TRIGGER tag_read_event_view_update_trig
    INSTEAD OF UPDATE ON tag_read_event_view
    FOR EACH ROW EXECUTE FUNCTION event.tag_read_event_view_update();

CREATE TABLE event.price_message_event (
    event_id SERIAL PRIMARY KEY,
    event_date TIMESTAMP WITH time zone DEFAULT NOW() NOT NULL,
    event_desc_id INTEGER NOT NULL
        REFERENCES event.event_description(event_desc_id),
    device_id VARCHAR(20) NOT NULL,
    toll_zone VARCHAR(20) NOT NULL,
    price NUMERIC(4,2) NOT NULL,
    detector VARCHAR(20)
);

CREATE INDEX ON event.price_message_event(event_date);
CREATE INDEX ON event.price_message_event(device_id);

CREATE VIEW price_message_event_view AS
    SELECT event_id, event_date, event_description.description,
           device_id, toll_zone, detector, price
    FROM event.price_message_event
    JOIN event.event_description
    ON price_message_event.event_desc_id = event_description.event_desc_id;
GRANT SELECT ON price_message_event_view TO PUBLIC;

CREATE VIEW iris.msg_pattern_priced AS
    SELECT name AS msg_pattern, 'priced'::VARCHAR(6) AS state,
        unnest(string_to_array(substring(multi FROM '%tzp,#"[^]]*#"]%' FOR '#'),
        ',')) AS toll_zone
    FROM iris.msg_pattern WHERE multi LIKE '%tzp%';

CREATE VIEW iris.msg_pattern_open AS
    SELECT name AS msg_pattern, 'open'::VARCHAR(6) AS state,
        unnest(string_to_array(substring(multi FROM '%tzo,#"[^]]*#"]%' FOR '#'),
        ',')) AS toll_zone
    FROM iris.msg_pattern WHERE multi LIKE '%tzo%';

CREATE VIEW iris.msg_pattern_closed AS
    SELECT name AS msg_pattern, 'closed'::VARCHAR(6) AS state,
        unnest(string_to_array(substring(multi FROM '%tzc,#"[^]]*#"]%' FOR '#'),
        ',')) AS toll_zone
    FROM iris.msg_pattern WHERE multi LIKE '%tzc%';

CREATE VIEW iris.msg_pattern_toll_zone AS
    SELECT msg_pattern, state, toll_zone
        FROM iris.msg_pattern_priced UNION ALL
    SELECT msg_pattern, state, toll_zone
        FROM iris.msg_pattern_open UNION ALL
    SELECT msg_pattern, state, toll_zone
        FROM iris.msg_pattern_closed;

CREATE VIEW dms_toll_zone_view AS
    SELECT dms, hashtag, tz.state, toll_zone, action_plan, da.msg_pattern
    FROM dms_action_view da
    JOIN iris.msg_pattern mp
    ON da.msg_pattern = mp.name
    JOIN iris.msg_pattern_toll_zone tz
    ON da.msg_pattern = tz.msg_pattern;
GRANT SELECT ON dms_toll_zone_view TO PUBLIC;

--
-- Video Monitors, Play Lists
--
CREATE TABLE iris.monitor_style (
    name VARCHAR(24) PRIMARY KEY,
    force_aspect BOOLEAN NOT NULL,
    accent VARCHAR(8) NOT NULL,
    font_sz INTEGER NOT NULL,
    title_bar BOOLEAN NOT NULL,
    auto_expand BOOLEAN NOT NULL,
    hgap INTEGER NOT NULL,
    vgap INTEGER NOT NULL
);

CREATE FUNCTION iris.monitor_style_notify() RETURNS TRIGGER AS
    $monitor_style_notify$
BEGIN
    PERFORM pg_notify('monitor_style', NEW.name);
    RETURN NULL; -- AFTER trigger return is ignored
END;
$monitor_style_notify$ LANGUAGE plpgsql;

CREATE TRIGGER monitor_style_notify_trig
    AFTER UPDATE ON iris.monitor_style
    FOR EACH ROW EXECUTE FUNCTION iris.monitor_style_notify();

CREATE TRIGGER monitor_style_table_notify_trig
    AFTER INSERT OR DELETE ON iris.monitor_style
    FOR EACH STATEMENT EXECUTE FUNCTION iris.table_notify();

CREATE VIEW monitor_style_view AS
    SELECT name, force_aspect, accent, font_sz, title_bar, auto_expand,
           hgap, vgap
    FROM iris.monitor_style;
GRANT SELECT ON monitor_style_view TO PUBLIC;

CREATE TABLE iris._video_monitor (
    name VARCHAR(12) PRIMARY KEY,
    notes VARCHAR CHECK (LENGTH(notes) < 256),
    mon_num INTEGER NOT NULL,
    restricted BOOLEAN NOT NULL,
    monitor_style VARCHAR(24) REFERENCES iris.monitor_style,
    camera VARCHAR(20) REFERENCES iris._camera
);

ALTER TABLE iris._video_monitor ADD CONSTRAINT _video_monitor_fkey
    FOREIGN KEY (name) REFERENCES iris.controller_io ON DELETE CASCADE;

CREATE TRIGGER video_monitor_hashtag_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris._video_monitor
    FOR EACH ROW EXECUTE FUNCTION iris.hashtag_trig('video_monitor');

CREATE FUNCTION iris.video_monitor_notify() RETURNS TRIGGER AS
    $video_monitor_notify$
BEGIN
    IF (NEW.mon_num IS DISTINCT FROM OLD.mon_num) OR
       (NEW.notes IS DISTINCT FROM OLD.notes)
    THEN
        NOTIFY video_monitor;
    ELSE
        PERFORM pg_notify('video_monitor', NEW.name);
    END IF;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$video_monitor_notify$ LANGUAGE plpgsql;

CREATE TRIGGER video_monitor_notify_trig
    AFTER UPDATE ON iris._video_monitor
    FOR EACH ROW EXECUTE FUNCTION iris.video_monitor_notify();

CREATE TRIGGER video_monitor_table_notify_trig
    AFTER INSERT OR DELETE ON iris._video_monitor
    FOR EACH STATEMENT EXECUTE FUNCTION iris.table_notify();

CREATE VIEW iris.video_monitor AS
    SELECT m.name, controller, pin, notes, mon_num, restricted, monitor_style,
           camera
    FROM iris._video_monitor m
    JOIN iris.controller_io cio ON m.name = cio.name;

CREATE FUNCTION iris.video_monitor_insert() RETURNS TRIGGER AS
    $video_monitor_insert$
BEGIN
    INSERT INTO iris.controller_io (name, resource_n, controller, pin)
         VALUES (NEW.name, 'video_monitor', NEW.controller, NEW.pin);
    INSERT INTO iris._video_monitor (
        name, notes, mon_num, restricted, monitor_style, camera
    ) VALUES (
        NEW.name, NEW.notes, NEW.mon_num, NEW.restricted, NEW.monitor_style,
        NEW.camera
    );
    RETURN NEW;
END;
$video_monitor_insert$ LANGUAGE plpgsql;

CREATE TRIGGER video_monitor_insert_trig
    INSTEAD OF INSERT ON iris.video_monitor
    FOR EACH ROW EXECUTE FUNCTION iris.video_monitor_insert();

CREATE FUNCTION iris.video_monitor_update() RETURNS TRIGGER AS
    $video_monitor_update$
BEGIN
    UPDATE iris.controller_io
       SET controller = NEW.controller,
           pin = NEW.pin
     WHERE name = OLD.name;
    UPDATE iris._video_monitor
       SET notes = NEW.notes,
           mon_num = NEW.mon_num,
           restricted = NEW.restricted,
           monitor_style = NEW.monitor_style,
           camera = NEW.camera
     WHERE name = OLD.name;
    RETURN NEW;
END;
$video_monitor_update$ LANGUAGE plpgsql;

CREATE TRIGGER video_monitor_update_trig
    INSTEAD OF UPDATE ON iris.video_monitor
    FOR EACH ROW EXECUTE FUNCTION iris.video_monitor_update();

CREATE TRIGGER video_monitor_delete_trig
    INSTEAD OF DELETE ON iris.video_monitor
    FOR EACH ROW EXECUTE FUNCTION iris.controller_io_delete();

CREATE VIEW video_monitor_view AS
    SELECT m.name, m.notes, mon_num, restricted, monitor_style,
           cio.controller, cio.pin, ctr.condition, ctr.comm_link, camera
    FROM iris._video_monitor m
    JOIN iris.controller_io cio ON m.name = cio.name
    LEFT JOIN controller_view ctr ON cio.controller = ctr.name;
GRANT SELECT ON video_monitor_view TO PUBLIC;

CREATE TABLE iris.play_list (
    name VARCHAR(20) PRIMARY KEY,
    meta BOOLEAN NOT NULL, -- immutable
    seq_num INTEGER UNIQUE,
    notes VARCHAR CHECK (LENGTH(notes) < 64)
);

CREATE TRIGGER play_list_hashtag_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris.play_list
    FOR EACH ROW EXECUTE FUNCTION iris.hashtag_trig('play_list');

CREATE FUNCTION iris.play_list_notify() RETURNS TRIGGER AS
    $play_list_notify$
BEGIN
    IF (NEW.seq_num IS DISTINCT FROM OLD.seq_num) OR
       (NEW.notes IS DISTINCT FROM OLD.notes)
    THEN
        NOTIFY play_list;
    ELSE
        PERFORM pg_notify('play_list', NEW.name);
    END IF;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$play_list_notify$ LANGUAGE plpgsql;

CREATE TRIGGER play_list_notify_trig
    AFTER UPDATE ON iris.play_list
    FOR EACH ROW EXECUTE FUNCTION iris.play_list_notify();

CREATE TRIGGER play_list_table_notify_trig
    AFTER INSERT OR DELETE ON iris.play_list
    FOR EACH STATEMENT EXECUTE FUNCTION iris.table_notify();

CREATE FUNCTION iris.play_list_is_meta(VARCHAR(20)) RETURNS BOOLEAN AS
$play_list_is_meta$
    SELECT meta FROM iris.play_list WHERE name = $1;
$play_list_is_meta$ LANGUAGE sql;

CREATE TABLE iris.play_list_entry (
    play_list VARCHAR(20) NOT NULL REFERENCES iris.play_list,
    ordinal INTEGER NOT NULL,
    camera VARCHAR(20) REFERENCES iris._camera,
    sub_list VARCHAR(20) REFERENCES iris.play_list,

    CONSTRAINT camera_ck CHECK (
        iris.play_list_is_meta(play_list) = (camera IS NULL)
    ) NOT VALID,
    CONSTRAINT sub_list_ck CHECK (
        iris.play_list_is_meta(play_list) = (sub_list IS NOT NULL)
        AND NOT iris.play_list_is_meta(sub_list)
    ) NOT VALID
);
ALTER TABLE iris.play_list_entry ADD PRIMARY KEY (play_list, ordinal);

CREATE FUNCTION iris.play_list_entry_notify() RETURNS TRIGGER AS
    $play_list_entry_notify$
BEGIN
    -- Entries are secondary, but it's not worth notifying
    -- for each row in a large batch update
    NOTIFY play_list;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$play_list_entry_notify$ LANGUAGE plpgsql;

CREATE TRIGGER play_list_entry_notify_trig
    AFTER INSERT OR DELETE ON iris.play_list_entry
    FOR EACH STATEMENT EXECUTE FUNCTION iris.play_list_entry_notify();

CREATE VIEW play_list_view AS
    SELECT pl.name AS play_list, pl.seq_num, notes, pe.ordinal,
           se.ordinal AS sub_ordinal, COALESCE(pe.camera, se.camera) AS camera
    FROM iris.play_list pl
    JOIN iris.play_list_entry pe ON pe.play_list = pl.name
    LEFT JOIN iris.play_list_entry se ON se.play_list = pe.sub_list
    ORDER BY pl.name, pe.ordinal, se.ordinal;
GRANT SELECT ON play_list_view TO PUBLIC;

--
-- Video Flow Streams
--
CREATE TABLE iris.flow_stream_status (
    id INTEGER PRIMARY KEY,
    description VARCHAR(8) NOT NULL
);

COPY iris.flow_stream_status (id, description) FROM stdin;
0	FAILED
1	STARTING
2	PLAYING
\.

CREATE TABLE iris._flow_stream (
    name VARCHAR(12) PRIMARY KEY,
    restricted BOOLEAN NOT NULL,
    loc_overlay BOOLEAN NOT NULL,
    quality INTEGER NOT NULL REFERENCES iris.encoding_quality,
    camera VARCHAR(20) REFERENCES iris._camera,
    mon_num INTEGER,
    address INET,
    port INTEGER CHECK (port > 0 AND port <= 65535),
    status INTEGER NOT NULL REFERENCES iris.flow_stream_status,

    CONSTRAINT camera_or_monitor CHECK ((camera IS NULL) != (mon_num IS NULL))
);

ALTER TABLE iris._flow_stream ADD CONSTRAINT _flow_stream_fkey
    FOREIGN KEY (name) REFERENCES iris.controller_io ON DELETE CASCADE;

CREATE TRIGGER flow_stream_notify_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris._flow_stream
    FOR EACH STATEMENT EXECUTE FUNCTION iris.table_notify();

CREATE VIEW iris.flow_stream AS
    SELECT f.name, controller, pin, restricted, loc_overlay, quality, camera,
           mon_num, address, port, status
    FROM iris._flow_stream f JOIN iris.controller_io cio ON f.name = cio.name;

CREATE FUNCTION iris.flow_stream_insert() RETURNS TRIGGER AS
    $flow_stream_insert$
BEGIN
    INSERT INTO iris.controller_io (name, resource_n, controller, pin)
         VALUES (NEW.name, 'flow_stream', NEW.controller, NEW.pin);
    INSERT INTO iris._flow_stream (
        name, restricted, loc_overlay, quality, camera, mon_num, address, port,
        status
    ) VALUES (
        NEW.name, NEW.restricted, NEW.loc_overlay, NEW.quality, NEW.camera,
        NEW.mon_num, NEW.address, NEW.port, NEW.status
    );
    RETURN NEW;
END;
$flow_stream_insert$ LANGUAGE plpgsql;

CREATE TRIGGER flow_stream_insert_trig
    INSTEAD OF INSERT ON iris.flow_stream
    FOR EACH ROW EXECUTE FUNCTION iris.flow_stream_insert();

CREATE FUNCTION iris.flow_stream_update() RETURNS TRIGGER AS
    $flow_stream_update$
BEGIN
    UPDATE iris.controller_io
       SET controller = NEW.controller,
           pin = NEW.pin
     WHERE name = OLD.name;
    UPDATE iris._flow_stream
       SET restricted = NEW.restricted,
           loc_overlay = NEW.loc_overlay,
           quality = NEW.quality,
           camera = NEW.camera,
           mon_num = NEW.mon_num,
           address = NEW.address,
           port = NEW.port,
           status = NEW.status
     WHERE name = OLD.name;
    RETURN NEW;
END;
$flow_stream_update$ LANGUAGE plpgsql;

CREATE TRIGGER flow_stream_update_trig
    INSTEAD OF UPDATE ON iris.flow_stream
    FOR EACH ROW EXECUTE FUNCTION iris.flow_stream_update();

CREATE TRIGGER flow_stream_delete_trig
    INSTEAD OF DELETE ON iris.flow_stream
    FOR EACH ROW EXECUTE FUNCTION iris.controller_io_delete();

CREATE VIEW flow_stream_view AS
    SELECT f.name, cio.controller, cio.pin, condition, comm_link, restricted,
           loc_overlay, eq.description AS quality, camera, mon_num, address,
           port, s.description AS status
    FROM iris._flow_stream f
    JOIN iris.controller_io cio ON f.name = cio.name
    JOIN iris.flow_stream_status s ON f.status = s.id
    LEFT JOIN controller_view ctr ON controller = ctr.name
    LEFT JOIN iris.encoding_quality eq ON f.quality = eq.id;
GRANT SELECT ON flow_stream_view TO PUBLIC;

--
-- Weather Sensors
--
CREATE TABLE iris._weather_sensor (
    name VARCHAR(20) PRIMARY KEY,
    site_id VARCHAR(20),
    alt_id VARCHAR(20),
    geo_loc VARCHAR(20) NOT NULL REFERENCES iris.geo_loc(name),
    notes VARCHAR CHECK (LENGTH(notes) < 256),
    settings JSONB,
    sample JSONB,
    sample_time TIMESTAMP WITH time zone
);

ALTER TABLE iris._weather_sensor ADD CONSTRAINT _weather_sensor_fkey
    FOREIGN KEY (name) REFERENCES iris.controller_io ON DELETE CASCADE;

CREATE TRIGGER weather_sensor_hashtag_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris._weather_sensor
    FOR EACH ROW EXECUTE FUNCTION iris.hashtag_trig('weather_sensor');

CREATE FUNCTION iris.weather_sensor_notify() RETURNS TRIGGER AS
    $weather_sensor_notify$
BEGIN
    IF (NEW.site_id IS DISTINCT FROM OLD.site_id) OR
       (NEW.alt_id IS DISTINCT FROM OLD.alt_id) OR
       (NEW.notes IS DISTINCT FROM OLD.notes)
    THEN
        NOTIFY weather_sensor;
    ELSE
        PERFORM pg_notify('weather_sensor', NEW.name);
    END IF;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$weather_sensor_notify$ LANGUAGE plpgsql;

CREATE TRIGGER weather_sensor_notify_trig
    AFTER UPDATE ON iris._weather_sensor
    FOR EACH ROW EXECUTE FUNCTION iris.weather_sensor_notify();

CREATE TRIGGER weather_sensor_table_notify_trig
    AFTER INSERT OR DELETE ON iris._weather_sensor
    FOR EACH STATEMENT EXECUTE FUNCTION iris.table_notify();

CREATE VIEW iris.weather_sensor AS
    SELECT w.name, site_id, alt_id, geo_loc, controller, pin, notes, settings,
           sample, sample_time
    FROM iris._weather_sensor w
    JOIN iris.controller_io cio ON w.name = cio.name;

CREATE FUNCTION iris.weather_sensor_insert() RETURNS TRIGGER AS
    $weather_sensor_insert$
BEGIN
    INSERT INTO iris.controller_io (name, resource_n, controller, pin)
         VALUES (NEW.name, 'weather_sensor', NEW.controller, NEW.pin);
    INSERT INTO iris._weather_sensor (
        name, site_id, alt_id, geo_loc, notes, settings, sample, sample_time
    ) VALUES (
        NEW.name, NEW.site_id, NEW.alt_id, NEW.geo_loc, NEW.notes, NEW.settings,
        NEW.sample, NEW.sample_time
    );
    RETURN NEW;
END;
$weather_sensor_insert$ LANGUAGE plpgsql;

CREATE TRIGGER weather_sensor_insert_trig
    INSTEAD OF INSERT ON iris.weather_sensor
    FOR EACH ROW EXECUTE FUNCTION iris.weather_sensor_insert();

CREATE FUNCTION iris.weather_sensor_update() RETURNS TRIGGER AS
    $weather_sensor_update$
BEGIN
    UPDATE iris.controller_io
       SET controller = NEW.controller,
           pin = NEW.pin
     WHERE name = OLD.name;
    UPDATE iris._weather_sensor
       SET site_id = NEW.site_id,
           alt_id = NEW.alt_id,
           notes = NEW.notes,
           settings = NEW.settings,
           sample = NEW.sample,
           sample_time = NEW.sample_time
     WHERE name = OLD.name;
    RETURN NEW;
END;
$weather_sensor_update$ LANGUAGE plpgsql;

CREATE TRIGGER weather_sensor_update_trig
    INSTEAD OF UPDATE ON iris.weather_sensor
    FOR EACH ROW EXECUTE FUNCTION iris.weather_sensor_update();

CREATE TRIGGER weather_sensor_delete_trig
    INSTEAD OF DELETE ON iris.weather_sensor
    FOR EACH ROW EXECUTE FUNCTION iris.controller_io_delete();

CREATE VIEW weather_sensor_view AS
    SELECT w.name, site_id, alt_id, w.notes, settings, sample, sample_time,
           w.geo_loc, l.roadway, l.road_dir, l.cross_mod, l.cross_street,
           l.cross_dir, l.landmark, l.lat, l.lon, l.corridor, l.location,
           cio.controller, cio.pin, ctr.comm_link, ctr.drop_id, ctr.condition
    FROM iris._weather_sensor w
    JOIN iris.controller_io cio ON w.name = cio.name
    LEFT JOIN geo_loc_view l ON w.geo_loc = l.name
    LEFT JOIN controller_view ctr ON cio.controller = ctr.name;
GRANT SELECT ON weather_sensor_view TO PUBLIC;

CREATE TABLE iris.dms_weather_sensor (
    dms VARCHAR(20) NOT NULL REFERENCES iris._dms,
    weather_sensor VARCHAR(20) NOT NULL REFERENCES iris._weather_sensor
);
ALTER TABLE iris.dms_weather_sensor ADD PRIMARY KEY (dms, weather_sensor);

CREATE VIEW dms_weather_sensor_view AS
    SELECT dms, weather_sensor
    FROM iris.dms_weather_sensor;
GRANT SELECT ON dms_weather_sensor_view TO PUBLIC;

CREATE TABLE event.weather_sensor_settings (
    event_id SERIAL PRIMARY KEY,
    event_date TIMESTAMP WITH time zone DEFAULT NOW() NOT NULL,
    weather_sensor VARCHAR(20) NOT NULL,
    settings JSONB
);

CREATE TABLE event.weather_sensor_sample (
    event_id SERIAL PRIMARY KEY,
    event_date TIMESTAMP WITH time zone DEFAULT NOW() NOT NULL,
    weather_sensor VARCHAR(20) NOT NULL,
    sample JSONB
);

CREATE INDEX ON event.weather_sensor_sample (weather_sensor);

CREATE FUNCTION event.weather_sensor_sample_trig() RETURNS TRIGGER AS
$weather_sensor_sample_trig$
BEGIN
    IF NEW.settings != OLD.settings THEN
        INSERT INTO event.weather_sensor_settings
                   (event_date, weather_sensor, settings)
            VALUES (now(), NEW.name, NEW.settings);
    END IF;
    IF NEW.sample != OLD.sample THEN
        INSERT INTO event.weather_sensor_sample
                   (event_date, weather_sensor, sample)
            VALUES (now(), NEW.name, NEW.sample);
    END IF;
    RETURN NEW;
END;
$weather_sensor_sample_trig$ LANGUAGE plpgsql;

CREATE TRIGGER weather_sensor_sample_trigger
    AFTER UPDATE ON iris._weather_sensor
    FOR EACH ROW EXECUTE FUNCTION event.weather_sensor_sample_trig();

CREATE VIEW weather_sensor_settings_view AS
    SELECT event_id, event_date, weather_sensor, settings
    FROM event.weather_sensor_settings;
GRANT SELECT ON weather_sensor_settings_view TO PUBLIC;

CREATE VIEW weather_sensor_sample_view AS
    SELECT event_id, event_date, weather_sensor, sample
    FROM event.weather_sensor_sample;
GRANT SELECT ON weather_sensor_sample_view TO PUBLIC;

--
-- Device / Controller views
--
CREATE VIEW iris.device_geo_loc_view AS
    SELECT name, geo_loc FROM iris._beacon UNION ALL
    SELECT name, geo_loc FROM iris._camera UNION ALL
    SELECT name, geo_loc FROM iris._dms UNION ALL
    SELECT name, geo_loc FROM iris._lcs UNION ALL
    SELECT name, geo_loc FROM iris._ramp_meter UNION ALL
    SELECT name, geo_loc FROM iris._tag_reader UNION ALL
    SELECT name, geo_loc FROM iris._weather_sensor UNION ALL
    SELECT d.name, geo_loc FROM iris._detector d
    JOIN iris.r_node rn ON d.r_node = rn.name UNION ALL
    SELECT g.name, geo_loc FROM iris._gate_arm g
    JOIN iris._gate_arm_array ga ON g.ga_array = ga.name;

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
