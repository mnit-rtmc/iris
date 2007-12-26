--
-- PostgreSQL database dump
--

SET client_encoding = 'SQL_ASCII';
SET check_function_bodies = false;

SET SESSION AUTHORIZATION 'tms';

SET search_path = public, pg_catalog;

--
-- TOC entry 213 (OID 19211188)
-- Name: plpgsql_call_handler(); Type: FUNC PROCEDURAL LANGUAGE; Schema: public; Owner: tms
--

CREATE FUNCTION plpgsql_call_handler() RETURNS language_handler
    AS '/usr/lib/pgsql/plpgsql.so', 'plpgsql_call_handler'
    LANGUAGE c;


SET SESSION AUTHORIZATION DEFAULT;

--
-- TOC entry 212 (OID 19211189)
-- Name: plpgsql; Type: PROCEDURAL LANGUAGE; Schema: public; Owner: 
--

CREATE TRUSTED PROCEDURAL LANGUAGE plpgsql HANDLER plpgsql_call_handler;


SET SESSION AUTHORIZATION 'postgres';

--
-- TOC entry 4 (OID 2200)
-- Name: public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
GRANT ALL ON SCHEMA public TO PUBLIC;


SET SESSION AUTHORIZATION 'tms';

--
-- TOC entry 5 (OID 19211190)
-- Name: lane_type_id_seq; Type: SEQUENCE; Schema: public; Owner: tms
--

CREATE SEQUENCE lane_type_id_seq
    INCREMENT BY 1
    MAXVALUE 2147483647
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 7 (OID 19211192)
-- Name: tms_log_seq; Type: SEQUENCE; Schema: public; Owner: tms
--

CREATE SEQUENCE tms_log_seq
    INCREMENT BY 1
    MAXVALUE 2147483647
    NO MINVALUE
    CACHE 1
    CYCLE;


--
-- TOC entry 11 (OID 19211194)
-- Name: vault_object; Type: TABLE; Schema: public; Owner: tms
--

CREATE TABLE vault_object (
    vault_oid integer NOT NULL,
    vault_type integer NOT NULL,
    vault_refs smallint DEFAULT 1 NOT NULL
);


--
-- TOC entry 12 (OID 19211194)
-- Name: vault_object; Type: ACL; Schema: public; Owner: tms
--

REVOKE ALL ON TABLE vault_object FROM PUBLIC;
GRANT SELECT ON TABLE vault_object TO PUBLIC;


SET SESSION AUTHORIZATION 'tms';

--
-- TOC entry 13 (OID 19211197)
-- Name: vault_types; Type: TABLE; Schema: public; Owner: tms
--

CREATE TABLE vault_types (
    "table" text NOT NULL,
    "className" text NOT NULL
)
INHERITS (vault_object);


--
-- TOC entry 14 (OID 19211203)
-- Name: vault_counter; Type: TABLE; Schema: public; Owner: tms
--

CREATE TABLE vault_counter (
    id integer NOT NULL,
    logging boolean NOT NULL
)
INHERITS (vault_object);


--
-- TOC entry 15 (OID 19211206)
-- Name: vault_log_entry; Type: TABLE; Schema: public; Owner: tms
--

CREATE TABLE vault_log_entry (
    redo text NOT NULL,
    undo text NOT NULL
)
INHERITS (vault_object);


--
-- TOC entry 16 (OID 19211212)
-- Name: vault_list; Type: TABLE; Schema: public; Owner: tms
--

CREATE TABLE vault_list (
    "listId" integer NOT NULL,
    "index" integer NOT NULL,
    "elementId" integer NOT NULL
);


--
-- TOC entry 17 (OID 19211212)
-- Name: vault_list; Type: ACL; Schema: public; Owner: tms
--

REVOKE ALL ON TABLE vault_list FROM PUBLIC;
GRANT SELECT ON TABLE vault_list TO PUBLIC;


SET SESSION AUTHORIZATION 'tms';

--
-- TOC entry 18 (OID 19211214)
-- Name: java_util_AbstractCollection; Type: TABLE; Schema: public; Owner: tms
--

CREATE TABLE "java_util_AbstractCollection" (
    dummy_24 boolean
)
INHERITS (vault_object);


--
-- TOC entry 19 (OID 19211217)
-- Name: java_util_AbstractList; Type: TABLE; Schema: public; Owner: tms
--

CREATE TABLE "java_util_AbstractList" (
    dummy_23 boolean
)
INHERITS ("java_util_AbstractCollection");


--
-- TOC entry 22 (OID 19211226)
-- Name: vault_transaction; Type: TABLE; Schema: public; Owner: tms
--

CREATE TABLE vault_transaction (
    stamp timestamp with time zone NOT NULL,
    "user" text NOT NULL,
    entries integer NOT NULL,
    "lastId" integer NOT NULL
)
INHERITS (vault_object);


--
-- TOC entry 23 (OID 19211232)
-- Name: java_lang_Number; Type: TABLE; Schema: public; Owner: tms
--

CREATE TABLE "java_lang_Number" (
    dummy_30 boolean
)
INHERITS (vault_object);


--
-- TOC entry 24 (OID 19211235)
-- Name: java_lang_Integer; Type: TABLE; Schema: public; Owner: tms
--

CREATE TABLE "java_lang_Integer" (
    value integer NOT NULL
)
INHERITS ("java_lang_Number");


--
-- TOC entry 25 (OID 19211238)
-- Name: java_util_ArrayList; Type: TABLE; Schema: public; Owner: tms
--

CREATE TABLE "java_util_ArrayList" (
    size integer NOT NULL
)
INHERITS ("java_util_AbstractList");


--
-- TOC entry 26 (OID 19211241)
-- Name: tms_object; Type: TABLE; Schema: public; Owner: tms
--

CREATE TABLE tms_object (
    dummy_41 boolean
)
INHERITS (vault_object);


--
-- TOC entry 27 (OID 19211241)
-- Name: tms_object; Type: ACL; Schema: public; Owner: tms
--

REVOKE ALL ON TABLE tms_object FROM PUBLIC;
GRANT SELECT ON TABLE tms_object TO PUBLIC;


SET SESSION AUTHORIZATION 'tms';

--
-- TOC entry 28 (OID 19211244)
-- Name: abstract_list; Type: TABLE; Schema: public; Owner: tms
--

CREATE TABLE abstract_list (
    dummy_40 boolean
)
INHERITS (tms_object);


--
-- TOC entry 29 (OID 19211247)
-- Name: device; Type: TABLE; Schema: public; Owner: tms
--

CREATE TABLE device (
    controller integer NOT NULL,
    pin integer NOT NULL,
    notes text NOT NULL,
    "location" integer NOT NULL
)
INHERITS (tms_object);


--
-- TOC entry 30 (OID 19211247)
-- Name: device; Type: ACL; Schema: public; Owner: tms
--

REVOKE ALL ON TABLE device FROM PUBLIC;
GRANT SELECT ON TABLE device TO PUBLIC;


SET SESSION AUTHORIZATION 'tms';

--
-- TOC entry 31 (OID 19211253)
-- Name: traffic_device; Type: TABLE; Schema: public; Owner: tms
--

CREATE TABLE traffic_device (
    id text NOT NULL
)
INHERITS (device);


--
-- TOC entry 32 (OID 19211253)
-- Name: traffic_device; Type: ACL; Schema: public; Owner: tms
--

REVOKE ALL ON TABLE traffic_device FROM PUBLIC;
GRANT SELECT ON TABLE traffic_device TO PUBLIC;


SET SESSION AUTHORIZATION 'tms';

--
-- TOC entry 33 (OID 19211259)
-- Name: dms; Type: TABLE; Schema: public; Owner: tms
--

CREATE TABLE dms (
    camera integer NOT NULL,
    mile real NOT NULL,
    travel text NOT NULL,
)
INHERITS (traffic_device);


--
-- TOC entry 34 (OID 19211259)
-- Name: dms; Type: ACL; Schema: public; Owner: tms
--

REVOKE ALL ON TABLE dms FROM PUBLIC;
GRANT SELECT ON TABLE dms TO PUBLIC;


SET SESSION AUTHORIZATION 'tms';

--
-- TOC entry 35 (OID 19211265)
-- Name: vault_map; Type: TABLE; Schema: public; Owner: tms
--

CREATE TABLE vault_map (
    "mapId" integer NOT NULL,
    "keyId" integer NOT NULL,
    "valueId" integer NOT NULL
);


--
-- TOC entry 36 (OID 19211267)
-- Name: java_util_AbstractMap; Type: TABLE; Schema: public; Owner: tms
--

CREATE TABLE "java_util_AbstractMap" (
    dummy_64 boolean
)
INHERITS (vault_object);


--
-- TOC entry 37 (OID 19211270)
-- Name: java_util_TreeMap; Type: TABLE; Schema: public; Owner: tms
--

CREATE TABLE "java_util_TreeMap" (
    comparator integer NOT NULL
)
INHERITS ("java_util_AbstractMap");


--
-- TOC entry 38 (OID 19211273)
-- Name: roadway; Type: TABLE; Schema: public; Owner: tms
--

CREATE TABLE roadway (
    name text NOT NULL,
    abbreviated text NOT NULL,
    "type" smallint NOT NULL,
    direction smallint NOT NULL,
    segment1 integer NOT NULL,
    segment2 integer NOT NULL
)
INHERITS (tms_object);


--
-- TOC entry 39 (OID 19211273)
-- Name: roadway; Type: ACL; Schema: public; Owner: tms
--

REVOKE ALL ON TABLE roadway FROM PUBLIC;
GRANT SELECT ON TABLE roadway TO PUBLIC;


SET SESSION AUTHORIZATION 'tms';

--
-- TOC entry 41 (OID 19211285)
-- Name: indexed_list; Type: TABLE; Schema: public; Owner: tms
--

CREATE TABLE indexed_list (
    list integer NOT NULL
)
INHERITS (abstract_list);


--
-- TOC entry 44 (OID 19211297)
-- Name: detector; Type: TABLE; Schema: public; Owner: tms
--

CREATE TABLE detector (
    "index" integer NOT NULL,
    "laneType" smallint NOT NULL,
    "laneNumber" smallint NOT NULL,
    abandoned boolean NOT NULL,
    hov boolean NOT NULL,
    "forceFail" boolean NOT NULL,
    "fieldLength" real NOT NULL,
    fake text NOT NULL
)
INHERITS (device);


--
-- TOC entry 45 (OID 19211297)
-- Name: detector; Type: ACL; Schema: public; Owner: tms
--

REVOKE ALL ON TABLE detector FROM PUBLIC;
GRANT SELECT ON TABLE detector TO PUBLIC;


SET SESSION AUTHORIZATION 'tms';

--
-- TOC entry 46 (OID 19211303)
-- Name: ramp_meter; Type: TABLE; Schema: public; Owner: tms
--

CREATE TABLE ramp_meter (
    detector integer NOT NULL,
    "controlMode" integer NOT NULL,
    "singleRelease" boolean NOT NULL,
    "storage" integer NOT NULL,
    "maxWait" integer NOT NULL,
    camera integer NOT NULL
)
INHERITS (traffic_device);


--
-- TOC entry 47 (OID 19211303)
-- Name: ramp_meter; Type: ACL; Schema: public; Owner: tms
--

REVOKE ALL ON TABLE ramp_meter FROM PUBLIC;
GRANT SELECT ON TABLE ramp_meter TO PUBLIC;



CREATE FUNCTION graphic_bpp(TEXT) RETURNS INTEGER AS '
	DECLARE n ALIAS FOR $1;
		b INTEGER;
	BEGIN SELECT INTO b bpp FROM graphic WHERE name = n;
		RETURN b;
	END;'
LANGUAGE PLPGSQL;

CREATE FUNCTION font_height(TEXT) RETURNS INTEGER AS '
	DECLARE n ALIAS FOR $1;
		h INTEGER;
	BEGIN SELECT INTO h height FROM font WHERE name = n;
		RETURN h;
	END;'
LANGUAGE PLPGSQL;

CREATE FUNCTION font_width(TEXT) RETURNS INTEGER AS '
	DECLARE n ALIAS FOR $1;
		w INTEGER;
	BEGIN SELECT INTO w width FROM font WHERE name = n;
		RETURN w;
	END;'
LANGUAGE PLPGSQL;

CREATE FUNCTION graphic_height(TEXT) RETURNS INTEGER AS '
	DECLARE n ALIAS FOR $1;
		h INTEGER;
	BEGIN SELECT INTO h height FROM graphic WHERE name = n;
		RETURN h;
	END;'
LANGUAGE PLPGSQL;

CREATE FUNCTION graphic_width(TEXT) RETURNS INTEGER AS '
	DECLARE n ALIAS FOR $1;
		w INTEGER;
	BEGIN SELECT INTO w width FROM graphic WHERE name = n;
		RETURN w;
	END;'
LANGUAGE PLPGSQL;

CREATE FUNCTION font_glyph(TEXT) RETURNS TEXT AS '
	DECLARE n ALIAS FOR $1;
		f TEXT;
	BEGIN SELECT INTO f font FROM glyph WHERE graphic = n;
		RETURN f;
	END;'
LANGUAGE PLPGSQL;

CREATE FUNCTION graphic_glyph(TEXT) RETURNS TEXT AS '
	DECLARE n ALIAS FOR $1;
		g TEXT;
	BEGIN SELECT INTO g graphic FROM glyph WHERE font = n;
		RETURN g;
	END;'
LANGUAGE PLPGSQL;

CREATE TABLE graphic (
	name TEXT PRIMARY KEY,
	bpp INTEGER NOT NULL,
	height INTEGER NOT NULL,
	width INTEGER NOT NULL,
	pixels TEXT NOT NULL
);

CREATE TABLE font (
	name TEXT PRIMARY KEY,
	height INTEGER NOT NULL,
	width INTEGER NOT NULL,
	line_spacing INTEGER NOT NULL,
	char_spacing INTEGER NOT NULL,
	version_id INTEGER NOT NULL
);

ALTER TABLE font
	ADD CONSTRAINT font_height_ck
	CHECK (height > 0 AND height < 25);
ALTER TABLE font
	ADD CONSTRAINT font_width_ck
	CHECK (width >= 0 AND width < 25);
ALTER TABLE font
	ADD CONSTRAINT font_line_sp_ck
	CHECK (line_spacing >= 0 AND line_spacing < 9);
ALTER TABLE font
	ADD CONSTRAINT font_char_sp_ck
	CHECK (char_spacing >= 0 AND char_spacing < 9);
CREATE TABLE glyph (
	name TEXT PRIMARY KEY,
	font TEXT NOT NULL,
	code_point INTEGER NOT NULL,
	graphic TEXT NOT NULL
);
ALTER TABLE glyph
	ADD CONSTRAINT fk_glyph_font FOREIGN KEY (font) REFERENCES font(name);
ALTER TABLE glyph
	ADD CONSTRAINT fk_glyph_graphic FOREIGN KEY (graphic)
	REFERENCES graphic(name);
ALTER TABLE glyph
	ADD CONSTRAINT glyph_bpp_ck
	CHECK (graphic_bpp(graphic) = 1);
ALTER TABLE glyph
	ADD CONSTRAINT glyph_size_ck
	CHECK (font_height(font) = graphic_height(graphic) AND
		(font_width(font) = 0 OR
		font_width(font) = graphic_width(graphic)));
ALTER TABLE graphic
	ADD CONSTRAINT graphic_font_ck
	CHECK (font_glyph(name) IS NULL OR
		(font_height(font_glyph(name)) = height AND
		(font_width(font_glyph(name)) = 0 OR
		font_width(font_glyph(name)) = width)));
ALTER TABLE font
	ADD CONSTRAINT font_graphic_ck
	CHECK (graphic_glyph(name) IS NULL OR
		(graphic_height(graphic_glyph(name)) = height AND
		(width = 0 OR graphic_width(graphic_glyph(name)) = width)));

REVOKE ALL ON TABLE graphic FROM PUBLIC;
GRANT SELECT ON TABLE graphic TO PUBLIC;
REVOKE ALL ON TABLE font FROM PUBLIC;
GRANT SELECT ON TABLE font TO PUBLIC;
REVOKE ALL ON TABLE glyph FROM PUBLIC;
GRANT SELECT ON TABLE glyph TO PUBLIC;


CREATE TABLE video_monitor (
	name TEXT PRIMARY KEY,
	description TEXT NOT NULL
);

REVOKE ALL ON TABLE video_monitor FROM PUBLIC;
GRANT SELECT ON TABLE video_monitor TO PUBLIC;


CREATE TABLE holiday (
	name TEXT PRIMARY KEY,
	month INTEGER NOT NULL,
	day INTEGER NOT NULL,
	week INTEGER NOT NULL,
	weekday INTEGER NOT NULL,
	shift INTEGER NOT NULL,
	period INTEGER NOT NULL
);

REVOKE ALL ON TABLE holiday FROM PUBLIC;
GRANT SELECT ON TABLE holiday TO PUBLIC;

SET SESSION AUTHORIZATION 'tms';

--
-- TOC entry 48 (OID 19211309)
-- Name: direction; Type: TABLE; Schema: public; Owner: tms
--

CREATE TABLE direction (
    id smallint NOT NULL,
    cross_dir character varying(4),
    free_dir character varying(4)
);


--
-- TOC entry 49 (OID 19211309)
-- Name: direction; Type: ACL; Schema: public; Owner: tms
--

REVOKE ALL ON TABLE direction FROM PUBLIC;
GRANT SELECT ON TABLE direction TO PUBLIC;


SET SESSION AUTHORIZATION 'tms';

--
-- TOC entry 50 (OID 19211311)
-- Name: timing_plan; Type: TABLE; Schema: public; Owner: tms
--

CREATE TABLE timing_plan (
    "startTime" integer NOT NULL,
    "stopTime" integer NOT NULL,
    active boolean NOT NULL
)
INHERITS (tms_object);


--
-- TOC entry 51 (OID 19211314)
-- Name: meter_plan; Type: TABLE; Schema: public; Owner: tms
--

CREATE TABLE meter_plan (
    dummy_43417 boolean
)
INHERITS (timing_plan);


--
-- TOC entry 52 (OID 19211317)
-- Name: simple_plan; Type: TABLE; Schema: public; Owner: tms
--

CREATE TABLE simple_plan (
    target integer NOT NULL
)
INHERITS (meter_plan);


--
-- TOC entry 53 (OID 19211317)
-- Name: simple_plan; Type: ACL; Schema: public; Owner: tms
--

REVOKE ALL ON TABLE simple_plan FROM PUBLIC;
GRANT SELECT ON TABLE simple_plan TO PUBLIC;


SET SESSION AUTHORIZATION 'tms';

--
-- TOC entry 54 (OID 19211320)
-- Name: circuit; Type: TABLE; Schema: public; Owner: tms
--

CREATE TABLE circuit (
    id text NOT NULL,
    line integer NOT NULL,
    node integer NOT NULL
)
INHERITS (tms_object);


--
-- TOC entry 55 (OID 19211320)
-- Name: circuit; Type: ACL; Schema: public; Owner: tms
--

REVOKE ALL ON TABLE circuit FROM PUBLIC;
GRANT SELECT ON TABLE circuit TO PUBLIC;


SET SESSION AUTHORIZATION 'tms';

--
-- TOC entry 56 (OID 19211326)
-- Name: cabinet_types; Type: TABLE; Schema: public; Owner: tms
--

CREATE TABLE cabinet_types (
    "index" integer NOT NULL,
    name text NOT NULL
);


--
-- TOC entry 57 (OID 19211326)
-- Name: cabinet_types; Type: ACL; Schema: public; Owner: tms
--

REVOKE ALL ON TABLE cabinet_types FROM PUBLIC;
GRANT SELECT ON TABLE cabinet_types TO PUBLIC;


SET SESSION AUTHORIZATION 'tms';

--
-- TOC entry 58 (OID 19211331)
-- Name: lane_type_description; Type: TABLE; Schema: public; Owner: tms
--

CREATE TABLE lane_type_description (
    lane_type_id smallint DEFAULT nextval('lane_type_id_seq'::text) NOT NULL,
    description text NOT NULL
);


--
-- TOC entry 59 (OID 19211337)
-- Name: time_plan_log; Type: TABLE; Schema: public; Owner: tms
--

CREATE TABLE time_plan_log (
    event_id integer DEFAULT nextval('tms_log_seq'::text) NOT NULL,
    vault_oid integer,
    event_date timestamp with time zone NOT NULL,
    logged_by text NOT NULL,
    start_time text NOT NULL,
    stop_time text NOT NULL,
    target integer NOT NULL
);


--
-- TOC entry 60 (OID 19211337)
-- Name: time_plan_log; Type: ACL; Schema: public; Owner: tms
--

REVOKE ALL ON TABLE time_plan_log FROM PUBLIC;
GRANT SELECT ON TABLE time_plan_log TO PUBLIC;


SET SESSION AUTHORIZATION 'tms';

--
-- TOC entry 61 (OID 19211343)
-- Name: add_remove_device_log; Type: TABLE; Schema: public; Owner: tms
--

CREATE TABLE add_remove_device_log (
    event_id integer DEFAULT nextval('tms_log_seq'::text) NOT NULL,
    event_date timestamp with time zone NOT NULL,
    device_type text NOT NULL,
    device_id text NOT NULL,
    event_description character varying(10) NOT NULL,
    edited_by text NOT NULL
);


--
-- TOC entry 62 (OID 19211343)
-- Name: add_remove_device_log; Type: ACL; Schema: public; Owner: tms
--

REVOKE ALL ON TABLE add_remove_device_log FROM PUBLIC;
GRANT SELECT ON TABLE add_remove_device_log TO PUBLIC;


SET SESSION AUTHORIZATION 'tms';

--
-- TOC entry 63 (OID 19211349)
-- Name: camera; Type: TABLE; Schema: public; Owner: tms
--

CREATE TABLE camera (
    encoder text NOT NULL,
    encoder_channel integer NOT NULL,
    nvr text NOT NULL
)
INHERITS (traffic_device);


--
-- TOC entry 64 (OID 19211349)
-- Name: camera; Type: ACL; Schema: public; Owner: tms
--

REVOKE ALL ON TABLE camera FROM PUBLIC;
GRANT SELECT ON TABLE camera TO PUBLIC;


SET SESSION AUTHORIZATION 'tms';

--
-- TOC entry 65 (OID 19211355)
-- Name: segment_list; Type: TABLE; Schema: public; Owner: tms
--

CREATE TABLE segment_list (
    list integer NOT NULL,
    direction smallint NOT NULL,
    "startingLanes" integer NOT NULL,
    freeway integer NOT NULL
)
INHERITS (abstract_list);


--
-- TOC entry 66 (OID 19211355)
-- Name: segment_list; Type: ACL; Schema: public; Owner: tms
--

REVOKE ALL ON TABLE segment_list FROM PUBLIC;
GRANT SELECT ON TABLE segment_list TO PUBLIC;


SET SESSION AUTHORIZATION 'tms';

--
-- TOC entry 67 (OID 19211358)
-- Name: stratified_plan; Type: TABLE; Schema: public; Owner: tms
--

CREATE TABLE stratified_plan (
    dummy_48294 boolean
)
INHERITS (meter_plan);


--
-- TOC entry 68 (OID 19211358)
-- Name: stratified_plan; Type: ACL; Schema: public; Owner: tms
--

REVOKE ALL ON TABLE stratified_plan FROM PUBLIC;
GRANT SELECT ON TABLE stratified_plan TO PUBLIC;


SET SESSION AUTHORIZATION 'tms';

--
-- TOC entry 69 (OID 19211361)
-- Name: segment; Type: TABLE; Schema: public; Owner: tms
--

CREATE TABLE segment (
    "left" boolean NOT NULL,
    delta integer NOT NULL,
    "cdDelta" integer NOT NULL,
    mile real,
    notes text,
    "location" integer NOT NULL
)
INHERITS (tms_object);


--
-- TOC entry 70 (OID 19211367)
-- Name: station_segment; Type: TABLE; Schema: public; Owner: tms
--

CREATE TABLE station_segment (
    "index" integer NOT NULL,
    speed_limit integer NOT NULL
)
INHERITS (segment);


--
-- TOC entry 71 (OID 19211367)
-- Name: station_segment; Type: ACL; Schema: public; Owner: tms
--

REVOKE ALL ON TABLE station_segment FROM PUBLIC;
GRANT SELECT ON TABLE station_segment TO PUBLIC;


SET SESSION AUTHORIZATION 'tms';

--
-- TOC entry 72 (OID 19211373)
-- Name: off_ramp; Type: TABLE; Schema: public; Owner: tms
--

CREATE TABLE off_ramp (
    "toCd" boolean NOT NULL,
    "fromCd" boolean NOT NULL,
    "rampLanes" integer NOT NULL
)
INHERITS (segment);


--
-- TOC entry 73 (OID 19211379)
-- Name: meterable; Type: TABLE; Schema: public; Owner: tms
--

CREATE TABLE meterable (
    "hovBypass" boolean NOT NULL
)
INHERITS (segment);


--
-- TOC entry 74 (OID 19211385)
-- Name: on_ramp; Type: TABLE; Schema: public; Owner: tms
--

CREATE TABLE on_ramp (
    "toCd" boolean NOT NULL,
    "fromCd" boolean NOT NULL,
    "rampLanes" integer NOT NULL
)
INHERITS (meterable);


--
-- TOC entry 75 (OID 19211391)
-- Name: meterable_cd; Type: TABLE; Schema: public; Owner: tms
--

CREATE TABLE meterable_cd (
    dummy_50048 boolean
)
INHERITS (meterable);


--
-- TOC entry 77 (OID 19211403)
-- Name: lcs_module; Type: TABLE; Schema: public; Owner: tms
--

CREATE TABLE lcs_module (
    "sfoRed" integer NOT NULL,
    "sfoYellow" integer NOT NULL,
    "sfoGreen" integer NOT NULL,
    "sfiRed" integer NOT NULL,
    "sfiYellow" integer NOT NULL,
    "sfiGreen" integer NOT NULL
)
INHERITS (tms_object);


--
-- TOC entry 78 (OID 19211406)
-- Name: lcs; Type: TABLE; Schema: public; Owner: tms
--

CREATE TABLE lcs (
    camera integer NOT NULL,
    modules integer[] NOT NULL
)
INHERITS (traffic_device);


--
-- TOC entry 79 (OID 19211406)
-- Name: lcs; Type: ACL; Schema: public; Owner: tms
--

REVOKE ALL ON TABLE lcs FROM PUBLIC;
GRANT SELECT ON TABLE lcs TO PUBLIC;


SET SESSION AUTHORIZATION 'tms';

--
-- TOC entry 80 (OID 19211412)
-- Name: detector_fieldlength_log; Type: TABLE; Schema: public; Owner: tms
--

CREATE TABLE detector_fieldlength_log (
    "index" integer,
    event_date timestamp with time zone,
    field_length real
);


--
-- TOC entry 81 (OID 19211412)
-- Name: detector_fieldlength_log; Type: ACL; Schema: public; Owner: tms
--

REVOKE ALL ON TABLE detector_fieldlength_log FROM PUBLIC;
GRANT SELECT ON TABLE detector_fieldlength_log TO PUBLIC;


SET SESSION AUTHORIZATION 'tms';

--
-- TOC entry 82 (OID 19211414)
-- Name: node; Type: TABLE; Schema: public; Owner: tms
--

CREATE TABLE node (
    id text NOT NULL,
    node_group integer NOT NULL,
    notes text,
    "location" integer NOT NULL
)
INHERITS (tms_object);


--
-- TOC entry 83 (OID 19211420)
-- Name: node_group; Type: TABLE; Schema: public; Owner: tms
--

CREATE TABLE node_group (
    "index" integer NOT NULL,
    description text NOT NULL
)
INHERITS (tms_object);


--
-- TOC entry 84 (OID 19211426)
-- Name: controller; Type: TABLE; Schema: public; Owner: tms
--

CREATE TABLE controller (
    "drop" smallint NOT NULL,
    active boolean NOT NULL,
    notes text NOT NULL,
    mile real NOT NULL,
    circuit integer NOT NULL,
    "location" integer NOT NULL
)
INHERITS (tms_object);


--
-- TOC entry 85 (OID 19211426)
-- Name: controller; Type: ACL; Schema: public; Owner: tms
--

REVOKE ALL ON TABLE controller FROM PUBLIC;
GRANT SELECT ON TABLE controller TO PUBLIC;


SET SESSION AUTHORIZATION 'tms';

--
-- TOC entry 86 (OID 19211432)
-- Name: controller_170; Type: TABLE; Schema: public; Owner: tms
--

CREATE TABLE controller_170 (
    cabinet smallint NOT NULL
)
INHERITS (controller);


--
-- TOC entry 87 (OID 19211432)
-- Name: controller_170; Type: ACL; Schema: public; Owner: tms
--

REVOKE ALL ON TABLE controller_170 FROM PUBLIC;
GRANT SELECT ON TABLE controller_170 TO PUBLIC;


SET SESSION AUTHORIZATION 'tms';

--
-- TOC entry 88 (OID 19211438)
-- Name: communication_line; Type: TABLE; Schema: public; Owner: tms
--

CREATE TABLE communication_line (
    "index" integer NOT NULL,
    description text NOT NULL,
    port text NOT NULL,
    "bitRate" integer NOT NULL,
    protocol smallint NOT NULL,
    timeout integer NOT NULL
)
INHERITS (tms_object);


--
-- TOC entry 89 (OID 19211438)
-- Name: communication_line; Type: ACL; Schema: public; Owner: tms
--

REVOKE ALL ON TABLE communication_line FROM PUBLIC;
GRANT SELECT ON TABLE communication_line TO PUBLIC;


SET SESSION AUTHORIZATION 'tms';

--
-- TOC entry 214 (OID 19211444)
-- Name: time_plan_log(); Type: FUNCTION; Schema: public; Owner: tms
--

CREATE FUNCTION time_plan_log() RETURNS "trigger"
    AS '
	begin if (OLD."startTime" != NEW."startTime" or OLD."stopTime"!= NEW."stopTime" or OLD.target!=NEW.target) 
	then insert into time_plan_log(vault_oid, event_date, logged_by, start_time, stop_time, target) 
	values(OLD.vault_oid, CURRENT_TIMESTAMP, user, OLD."startTime", OLD."stopTime", OLD.target); end if; return old; 
	end; '
    LANGUAGE plpgsql;


--
-- TOC entry 215 (OID 19211445)
-- Name: detector_fieldlength_log(); Type: FUNCTION; Schema: public; Owner: tms
--

CREATE FUNCTION detector_fieldlength_log() RETURNS "trigger"
    AS 'begin
if (OLD."fieldLength" != NEW."fieldLength") then 
insert into detector_fieldlength_log(index, event_date, field_length) 
values (OLD.index, CURRENT_TIMESTAMP, OLD."fieldLength"); 
end if; 
return old; 
end; '
    LANGUAGE plpgsql;


--
-- TOC entry 216 (OID 19211446)
-- Name: add_detector(); Type: FUNCTION; Schema: public; Owner: tms
--

CREATE FUNCTION add_detector() RETURNS "trigger"
    AS '
	begin insert into add_remove_device_log(event_date, device_type, device_id, event_description, edited_by) 
	values(CURRENT_TIMESTAMP, TG_RELNAME, NEW.index, ''add'', user); return NEW; end; '
    LANGUAGE plpgsql;


--
-- TOC entry 217 (OID 19211447)
-- Name: remove_detector(); Type: FUNCTION; Schema: public; Owner: tms
--

CREATE FUNCTION remove_detector() RETURNS "trigger"
    AS '
	begin insert into add_remove_device_log(event_date, device_type, device_id, event_description, edited_by) 
	values(CURRENT_TIMESTAMP, TG_RELNAME, OLD.index, ''remove'', user); return OLD; end; '
    LANGUAGE plpgsql;


--
-- TOC entry 218 (OID 19211448)
-- Name: add_dms(); Type: FUNCTION; Schema: public; Owner: tms
--

CREATE FUNCTION add_dms() RETURNS "trigger"
    AS '
	begin insert into add_remove_device_log(event_date, device_type, device_id, event_description, edited_by) 
	values(CURRENT_TIMESTAMP, TG_RELNAME, NEW.id, ''add'', user); return NEW; end; '
    LANGUAGE plpgsql;


--
-- TOC entry 219 (OID 19211449)
-- Name: remove_dms(); Type: FUNCTION; Schema: public; Owner: tms
--

CREATE FUNCTION remove_dms() RETURNS "trigger"
    AS '
	begin insert into add_remove_device_log(event_date, device_type, device_id, event_description, edited_by) 
	values(CURRENT_TIMESTAMP, TG_RELNAME, OLD.id, ''remove'', user); return OLD; end; '
    LANGUAGE plpgsql;


--
-- TOC entry 220 (OID 19211450)
-- Name: add_meter(); Type: FUNCTION; Schema: public; Owner: tms
--

CREATE FUNCTION add_meter() RETURNS "trigger"
    AS '
	begin insert into add_remove_device_log(event_date, device_type, device_id, event_description, edited_by) 
	values(CURRENT_TIMESTAMP, TG_RELNAME, NEW.id, ''add'', user); return NEW; end; '
    LANGUAGE plpgsql;


--
-- TOC entry 221 (OID 19211451)
-- Name: remove_meter(); Type: FUNCTION; Schema: public; Owner: tms
--

CREATE FUNCTION remove_meter() RETURNS "trigger"
    AS '
	begin insert into add_remove_device_log(event_date, device_type, device_id, event_description, edited_by) 
	values(CURRENT_TIMESTAMP, TG_RELNAME, OLD.id, ''remove'', user); return OLD; end; '
    LANGUAGE plpgsql;


--
-- TOC entry 222 (OID 19211452)
-- Name: add_camera(); Type: FUNCTION; Schema: public; Owner: tms
--

CREATE FUNCTION add_camera() RETURNS "trigger"
    AS '
	begin insert into add_remove_device_log(event_date, device_type, device_id, event_description, edited_by) 
	values(CURRENT_TIMESTAMP, TG_RELNAME, NEW.id, ''add'', user); return NEW; end; '
    LANGUAGE plpgsql;


--
-- TOC entry 223 (OID 19211453)
-- Name: remove_camera(); Type: FUNCTION; Schema: public; Owner: tms
--

CREATE FUNCTION remove_camera() RETURNS "trigger"
    AS '
	begin insert into add_remove_device_log(event_date, device_type, device_id, event_description, edited_by) 
	values(CURRENT_TIMESTAMP, TG_RELNAME, OLD.id, ''remove'', user); return OLD; end; '
    LANGUAGE plpgsql;


--
-- TOC entry 224 (OID 19211454)
-- Name: get_circuit(integer); Type: FUNCTION; Schema: public; Owner: tms
--

CREATE FUNCTION get_circuit(integer) RETURNS text
    AS '
	DECLARE
		id ALIAS FOR $1;
		c text;
	BEGIN
		select into c id from circuit as c where id=c.line;
		RETURN c;
		
	END;
'
    LANGUAGE plpgsql;


--
-- TOC entry 225 (OID 19211455)
-- Name: get_roadway(integer); Type: FUNCTION; Schema: public; Owner: tms
--

CREATE FUNCTION get_roadway(integer) RETURNS text
    AS '
	DECLARE
		id ALIAS FOR $1;
		n text;
	BEGIN
		select into n name from "roadway" where vault_oid=id;
		RETURN n;

	END;
'
    LANGUAGE plpgsql;


--
-- TOC entry 226 (OID 19211456)
-- Name: get_line(integer); Type: FUNCTION; Schema: public; Owner: tms
--

CREATE FUNCTION get_line(integer) RETURNS integer
    AS '
	DECLARE
		id ALIAS FOR $1;
		l int;
	BEGIN
		select into l index from "communication_line" as c where c.vault_oid=id;
		RETURN l;

	END;
'
    LANGUAGE plpgsql;


--
-- TOC entry 227 (OID 19211457)
-- Name: get_roadway_abbre(integer); Type: FUNCTION; Schema: public; Owner: tms
--

CREATE FUNCTION get_roadway_abbre(integer) RETURNS text
    AS '
	DECLARE
		id ALIAS FOR $1;
		n text;
	BEGIN
		select into n abbreviated from "roadway" where vault_oid=id;
		RETURN n;
		
	END;
'
    LANGUAGE plpgsql;


--
-- TOC entry 228 (OID 19211458)
-- Name: get_det_no(integer); Type: FUNCTION; Schema: public; Owner: tms
--

CREATE FUNCTION get_det_no(integer) RETURNS integer
    AS '
	DECLARE
		id ALIAS FOR $1;
		det_no int4;	
	BEGIN
		select into det_no index from "detector" where vault_oid = id;
		return det_no;
	END;
'
    LANGUAGE plpgsql;


--
-- TOC entry 229 (OID 19211459)
-- Name: get_det_no(integer[]); Type: FUNCTION; Schema: public; Owner: tms
--

CREATE FUNCTION get_det_no(integer[]) RETURNS text
    AS '
	DECLARE
		id ALIAS FOR $1;
		det_no text;		
	BEGIN
		/*for i in 1..4 loop
			if id[i] is not null then
				det_no := det_no || id[i] || '' '';
			end if;
		end loop;*/
		if id[1] is not null then
			det_no := get_det_no(id[1]);	
		end if;
		if id[2] is not null then
			det_no := det_no || '' '' || get_det_no(id[2]);
		end if;
		if id[3] is not null then
			det_no := det_no || '' '' || get_det_no(id[3]);
		end if;
		if id[4] is not null then
			det_no := det_no || '' '' || get_det_no(id[4]);
		end if;
		RETURN det_no;
		
	END;
'
    LANGUAGE plpgsql;


SET SESSION AUTHORIZATION 'tms';

--
-- TOC entry 93 (OID 19211469)
-- Name: warning_sign; Type: TABLE; Schema: public; Owner: tms
--

CREATE TABLE warning_sign (
    camera integer NOT NULL,
    text text NOT NULL
)
INHERITS (traffic_device);


--
-- TOC entry 94 (OID 19211469)
-- Name: warning_sign; Type: ACL; Schema: public; Owner: tms
--

REVOKE ALL ON TABLE warning_sign FROM PUBLIC;
GRANT SELECT ON TABLE warning_sign TO PUBLIC;


SET SESSION AUTHORIZATION 'tms';

--
-- TOC entry 9 (OID 19211475)
-- Name: dms_message_seq; Type: SEQUENCE; Schema: public; Owner: tms
--

CREATE SEQUENCE dms_message_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 95 (OID 19211477)
-- Name: dms_message; Type: TABLE; Schema: public; Owner: tms
--

CREATE TABLE dms_message (
    id integer DEFAULT nextval('dms_message_seq'::text) NOT NULL,
    dms text,
    line smallint NOT NULL,
    message character varying(24) DEFAULT ''::character varying NOT NULL,
    abbrev character varying(12) DEFAULT ''::character varying NOT NULL,
    priority smallint DEFAULT 50 NOT NULL,
    CONSTRAINT dms_message_line CHECK (((line >= 1) AND (line <= 6))),
    CONSTRAINT dms_message_priority CHECK (((priority >= 1) AND (priority <= 99)))
);


--
-- TOC entry 96 (OID 19211488)
-- Name: road_modifier; Type: TABLE; Schema: public; Owner: tms
--

CREATE TABLE road_modifier (
    id smallint NOT NULL,
    modifier text NOT NULL
);


--
-- TOC entry 97 (OID 19211488)
-- Name: road_modifier; Type: ACL; Schema: public; Owner: tms
--

REVOKE ALL ON TABLE road_modifier FROM PUBLIC;
GRANT SELECT ON TABLE road_modifier TO PUBLIC;


SET SESSION AUTHORIZATION 'tms';

--
-- TOC entry 98 (OID 19211493)
-- Name: location; Type: TABLE; Schema: public; Owner: tms
--

CREATE TABLE "location" (
    freeway integer NOT NULL,
    free_dir smallint NOT NULL,
    cross_street integer NOT NULL,
    cross_dir smallint NOT NULL,
    cross_mod smallint NOT NULL,
    easting integer NOT NULL,
    east_off integer NOT NULL,
    northing integer NOT NULL,
    north_off integer NOT NULL
)
INHERITS (tms_object);


--
-- TOC entry 99 (OID 19211493)
-- Name: location; Type: ACL; Schema: public; Owner: tms
--

REVOKE ALL ON TABLE "location" FROM PUBLIC;
GRANT SELECT ON TABLE "location" TO PUBLIC;


SET SESSION AUTHORIZATION 'tms';

--
-- TOC entry 230 (OID 19211496)
-- Name: get_next_oid(); Type: FUNCTION; Schema: public; Owner: tms
--

CREATE FUNCTION get_next_oid() RETURNS integer
    AS '
	DECLARE
		oid int;
	BEGIN
		UPDATE vault_counter SET id = id + 1;
		SELECT INTO oid id FROM vault_counter;
		RETURN oid;
	END;
'
    LANGUAGE plpgsql;


--
-- TOC entry 231 (OID 19211497)
-- Name: get_dir(smallint); Type: FUNCTION; Schema: public; Owner: tms
--

CREATE FUNCTION get_dir(smallint) RETURNS text
    AS '
	DECLARE
		id ALIAS FOR $1;
		n TEXT;
	BEGIN
		SELECT INTO n free_dir FROM direction AS d WHERE d.id = id;
		RETURN n;
	END;
'
    LANGUAGE plpgsql;


--
-- TOC entry 232 (OID 19211498)
-- Name: get_location(integer, smallint); Type: FUNCTION; Schema: public; Owner: tms
--

CREATE FUNCTION get_location(integer, smallint) RETURNS text
    AS '
	DECLARE
		road ALIAS FOR $1;
		dir ALIAS FOR $2;
	BEGIN
		RETURN trim(get_roadway(road) || '' '' || get_dir(dir));
	END;
'
    LANGUAGE plpgsql;


--
-- TOC entry 233 (OID 19211499)
-- Name: get_location(integer, smallint, integer, smallint, smallint); Type: FUNCTION; Schema: public; Owner: tms
--

CREATE FUNCTION get_location(integer, smallint, integer, smallint, smallint) RETURNS text
    AS '
	DECLARE
		freeway ALIAS FOR $1;
		free_dir ALIAS FOR $2;
		cross_street ALIAS FOR $3;
		cross_dir ALIAS FOR $4;
		mod ALIAS FOR $5;
		f TEXT;
		c TEXT;
		m TEXT;
	BEGIN
		f = get_location(freeway, free_dir);
		c = get_location(cross_street, cross_dir);
		SELECT INTO m modifier FROM road_modifier AS d WHERE d.id = mod;
		RETURN f || '' '' || m || '' '' || c;
	END;
'
    LANGUAGE plpgsql;


--
-- TOC entry 234 (OID 19211500)
-- Name: get_location(integer, integer); Type: FUNCTION; Schema: public; Owner: tms
--

CREATE FUNCTION get_location(integer, integer) RETURNS text
    AS '
	DECLARE
		freeway ALIAS FOR $1;
		cross_street ALIAS FOR $2;
	BEGIN
		RETURN get_roadway(freeway) || '' & '' || get_roadway(cross_street);
	END;
'
    LANGUAGE plpgsql;


--
-- TOC entry 100 (OID 19211503)
-- Name: circuit_node; Type: VIEW; Schema: public; Owner: tms
--

CREATE VIEW circuit_node AS
    SELECT c.vault_oid, c.id, cl."index" AS line, cl."bitRate", l.freeway, l.cross_street FROM circuit c, communication_line cl, node n, "location" l WHERE (((c.line = cl.vault_oid) AND (c.node = n.vault_oid)) AND (n."location" = l.vault_oid));


--
-- TOC entry 101 (OID 19211503)
-- Name: circuit_node; Type: ACL; Schema: public; Owner: tms
--

REVOKE ALL ON TABLE circuit_node FROM PUBLIC;
GRANT SELECT ON TABLE circuit_node TO PUBLIC;


SET SESSION AUTHORIZATION 'tms';

--
-- TOC entry 102 (OID 19211506)
-- Name: controller_device; Type: VIEW; Schema: public; Owner: tms
--

CREATE VIEW controller_device AS
    SELECT d.id, d.controller, d.pin, l.freeway, l.free_dir, l.cross_street, l.cross_dir FROM traffic_device d, "location" l WHERE (d."location" = l.vault_oid);


--
-- TOC entry 103 (OID 19211506)
-- Name: controller_device; Type: ACL; Schema: public; Owner: tms
--

REVOKE ALL ON TABLE controller_device FROM PUBLIC;
GRANT SELECT ON TABLE controller_device TO PUBLIC;


SET SESSION AUTHORIZATION 'tms';

--
-- TOC entry 104 (OID 19211509)
-- Name: controller_report; Type: VIEW; Schema: public; Owner: tms
--

CREATE VIEW controller_report AS
    SELECT cn.id AS circuit, cn.line, c."drop", c.mile, get_location(l.freeway, l.free_dir, l.cross_street, l.cross_dir, l.cross_mod) AS "location", ct.name AS "type", d1.id AS "id (meter1)", get_location(d1.cross_street, d1.cross_dir) AS "from (meter1)", get_location(d1.freeway, d1.free_dir) AS "to (meter1)", d2.id AS meter2, get_location(d2.cross_street, d2.cross_dir) AS "from (meter2)", get_location(d2.freeway, d2.free_dir) AS "to (meter2)", c.notes, cn."bitRate", get_location(cn.freeway, cn.cross_street) AS node_location FROM ((((((controller c LEFT JOIN "location" l ON ((c."location" = l.vault_oid))) LEFT JOIN circuit_node cn ON ((c.circuit = cn.vault_oid))) LEFT JOIN controller_170 c1 ON ((c.vault_oid = c1.vault_oid))) LEFT JOIN cabinet_types ct ON ((c1.cabinet = ct."index"))) LEFT JOIN controller_device d1 ON (((d1.pin = 2) AND (d1.controller = c.vault_oid)))) LEFT JOIN controller_device d2 ON (((d2.pin = 3) AND (d2.controller = c.vault_oid)))) ORDER BY cn.line, c."drop";


--
-- TOC entry 105 (OID 19211509)
-- Name: controller_report; Type: ACL; Schema: public; Owner: tms
--

REVOKE ALL ON TABLE controller_report FROM PUBLIC;
GRANT SELECT ON TABLE controller_report TO PUBLIC;


SET SESSION AUTHORIZATION 'tms';

--
-- TOC entry 106 (OID 19211513)
-- Name: green_detector; Type: VIEW; Schema: public; Owner: tms
--

CREATE VIEW green_detector AS
    SELECT d."index" AS det_no, get_roadway_abbre(l.freeway) AS freeway, get_dir(l.free_dir) AS free_dir, get_roadway_abbre(l.cross_street) AS cross_street, get_dir(l.cross_dir) AS cross_dir, ramp_meter.id AS ramp_id FROM ((detector d LEFT JOIN "location" l ON ((d."location" = l.vault_oid))) JOIN ramp_meter ON ((d.vault_oid = ramp_meter.detector)));


--
-- TOC entry 107 (OID 19211513)
-- Name: green_detector; Type: ACL; Schema: public; Owner: tms
--

REVOKE ALL ON TABLE green_detector FROM PUBLIC;
GRANT SELECT ON TABLE green_detector TO PUBLIC;


SET SESSION AUTHORIZATION 'tms';

--
-- TOC entry 108 (OID 19211517)
-- Name: freeway_station_view; Type: VIEW; Schema: public; Owner: tms
--

CREATE VIEW freeway_station_view AS
    SELECT get_roadway(l.freeway) AS freeway, get_dir(l.free_dir) AS free_dir, get_roadway(l.cross_street) AS cross_street, s."index" AS station_no, s.speed_limit FROM ((station_segment s JOIN "location" l ON ((s."location" = l.vault_oid))) JOIN vault_list v ON ((v."elementId" = s.vault_oid))) ORDER BY l.freeway, l.free_dir, v."index";


--
-- TOC entry 109 (OID 19211517)
-- Name: freeway_station_view; Type: ACL; Schema: public; Owner: tms
--

REVOKE ALL ON TABLE freeway_station_view FROM PUBLIC;
GRANT SELECT ON TABLE freeway_station_view TO PUBLIC;


SET SESSION AUTHORIZATION 'tms';

--
-- TOC entry 110 (OID 19211521)
-- Name: controller_location; Type: VIEW; Schema: public; Owner: tms
--

CREATE VIEW controller_location AS
    SELECT c.vault_oid, c."drop", c.active, c.notes, c.mile, c.circuit, l.freeway, l.free_dir AS "freeDir", l.cross_street AS "crossStreet", l.cross_dir AS "crossDir", l.cross_mod FROM controller c, "location" l WHERE (c."location" = l.vault_oid);


--
-- TOC entry 111 (OID 19211521)
-- Name: controller_location; Type: ACL; Schema: public; Owner: tms
--

REVOKE ALL ON TABLE controller_location FROM PUBLIC;
GRANT SELECT ON TABLE controller_location TO PUBLIC;


SET SESSION AUTHORIZATION 'tms';

--
-- TOC entry 112 (OID 19211522)
-- Name: alarm; Type: TABLE; Schema: public; Owner: tms
--

CREATE TABLE alarm (
    controller integer NOT NULL,
    pin integer NOT NULL,
    notes text NOT NULL
)
INHERITS (tms_object);


--
-- TOC entry 113 (OID 19211528)
-- Name: segment_detector; Type: TABLE; Schema: public; Owner: tms
--

CREATE TABLE segment_detector (
    segment integer,
    detector integer
);


--
-- TOC entry 114 (OID 19211532)
-- Name: segmentlist_view; Type: VIEW; Schema: public; Owner: tms
--

CREATE VIEW segmentlist_view AS
    SELECT get_roadway(l.freeway) AS freeway, get_dir(l.free_dir) AS free_dir, get_roadway(l.cross_street) AS cross_street, get_dir(l.cross_dir) AS cross_dir FROM ((segment s JOIN "location" l ON ((s."location" = l.vault_oid))) JOIN vault_list v ON ((v."elementId" = s.vault_oid))) ORDER BY l.freeway, l.free_dir, v."index";


--
-- TOC entry 115 (OID 19211532)
-- Name: segmentlist_view; Type: ACL; Schema: public; Owner: tms
--

REVOKE ALL ON TABLE segmentlist_view FROM PUBLIC;
GRANT SELECT ON TABLE segmentlist_view TO PUBLIC;


SET SESSION AUTHORIZATION 'tms';

--
-- TOC entry 116 (OID 19211535)
-- Name: device_location; Type: VIEW; Schema: public; Owner: tms
--

CREATE VIEW device_location AS
    SELECT d.vault_oid, d.notes, l.freeway, l.free_dir AS "freeDir", l.cross_street AS "crossStreet", l.cross_dir AS "crossDir", l.cross_mod FROM ((device d JOIN "location" l ON ((d."location" = l.vault_oid))) LEFT JOIN controller c ON ((d.controller = c.vault_oid)));


--
-- TOC entry 117 (OID 19211535)
-- Name: device_location; Type: ACL; Schema: public; Owner: tms
--

REVOKE ALL ON TABLE device_location FROM PUBLIC;
GRANT SELECT ON TABLE device_location TO PUBLIC;


SET SESSION AUTHORIZATION 'tms';

--
-- TOC entry 235 (OID 19211536)
-- Name: boolean_converter(boolean); Type: FUNCTION; Schema: public; Owner: tms
--

CREATE FUNCTION boolean_converter(boolean) RETURNS text
    AS '
	DECLARE
		value ALIAS FOR $1;
	BEGIN
		IF value = ''t'' THEN
			RETURN ''Yes'';
		END IF;
		RETURN ''No'';
	END;
'
    LANGUAGE plpgsql;


--
-- TOC entry 118 (OID 19211539)
-- Name: iris_det; Type: VIEW; Schema: public; Owner: tms
--

CREATE VIEW iris_det AS
    SELECT d."index" AS det_no, get_roadway_abbre(l.freeway) AS freeway_abbre, get_roadway_abbre(l.cross_street) AS crossstreet_abbre, get_roadway(l.freeway) AS freeway, get_dir(l.free_dir) AS free_dir, get_roadway(l.cross_street) AS cross_street, get_dir(l.cross_dir) AS cross_dir, boolean_converter(d.hov) AS hov, d."laneNumber" AS lane_number, d."fieldLength" AS field_length, ln.description AS lane_type, boolean_converter(d.abandoned) AS abandoned, boolean_converter(d."forceFail") AS force_fail, boolean_converter(c.active) AS active, d.fake, d.notes FROM (((detector d LEFT JOIN "location" l ON ((d."location" = l.vault_oid))) LEFT JOIN lane_type_description ln ON ((d."laneType" = ln.lane_type_id))) LEFT JOIN controller c ON ((d.controller = c.vault_oid))) ORDER BY d."index";


--
-- TOC entry 119 (OID 19211539)
-- Name: iris_det; Type: ACL; Schema: public; Owner: tms
--

REVOKE ALL ON TABLE iris_det FROM PUBLIC;
GRANT SELECT ON TABLE iris_det TO PUBLIC;


SET SESSION AUTHORIZATION 'tms';

--
-- TOC entry 121 (OID 19211547)
-- Name: traffic_device_timing_plan; Type: TABLE; Schema: public; Owner: tms
--

CREATE TABLE traffic_device_timing_plan (
    traffic_device text,
    timing_plan integer
);


--
-- TOC entry 122 (OID 19211547)
-- Name: traffic_device_timing_plan; Type: ACL; Schema: public; Owner: tms
--

REVOKE ALL ON TABLE traffic_device_timing_plan FROM PUBLIC;
GRANT SELECT ON TABLE traffic_device_timing_plan TO PUBLIC;


SET SESSION AUTHORIZATION 'tms';

--
-- TOC entry 236 (OID 19211552)
-- Name: hour_min(integer); Type: FUNCTION; Schema: public; Owner: tms
--

CREATE FUNCTION hour_min(integer) RETURNS text
    AS '
DECLARE
	min_of_day ALIAS FOR $1;
	hour integer;
	minute integer;
	output text;
BEGIN
	hour := min_of_day / 60;
	minute := min_of_day % 60;
	output := '''';
	IF hour < 10 THEN
		output := ''0'';
	END IF;
	output := output || hour || '':'';
	IF minute < 10 THEN
		output := output || ''0'';
	END IF;
	output := output || minute;
	RETURN output;
END;'
    LANGUAGE plpgsql;


--
-- TOC entry 123 (OID 19211555)
-- Name: time_plan_log_view; Type: VIEW; Schema: public; Owner: tms
--

CREATE VIEW time_plan_log_view AS
    SELECT t.event_id, rm.id AS ramp_id, t.event_date, t.logged_by, hour_min(int4(t.start_time)) AS start_time, hour_min(int4(t.stop_time)) AS stop_time, t.target FROM ((ramp_meter rm JOIN traffic_device_timing_plan tp ON ((rm.id = tp.traffic_device))) JOIN time_plan_log t ON ((t.vault_oid = tp.timing_plan)));


--
-- TOC entry 124 (OID 19211555)
-- Name: time_plan_log_view; Type: ACL; Schema: public; Owner: tms
--

REVOKE ALL ON TABLE time_plan_log_view FROM PUBLIC;
GRANT SELECT ON TABLE time_plan_log_view TO PUBLIC;


SET SESSION AUTHORIZATION 'tms';

--
-- TOC entry 125 (OID 19211558)
-- Name: simple; Type: VIEW; Schema: public; Owner: tms
--

CREATE VIEW "simple" AS
    SELECT rm.id, hour_min(sp."startTime") AS start_time, hour_min(sp."stopTime") AS stop_time, sp.target, sp.active FROM ((ramp_meter rm JOIN traffic_device_timing_plan tp ON ((rm.id = tp.traffic_device))) JOIN simple_plan sp ON ((tp.timing_plan = sp.vault_oid)));


--
-- TOC entry 126 (OID 19211558)
-- Name: simple; Type: ACL; Schema: public; Owner: tms
--

REVOKE ALL ON TABLE "simple" FROM PUBLIC;
GRANT SELECT ON TABLE "simple" TO PUBLIC;


SET SESSION AUTHORIZATION 'tms';

--
-- TOC entry 127 (OID 19211561)
-- Name: stratified; Type: VIEW; Schema: public; Owner: tms
--

CREATE VIEW stratified AS
    SELECT rm.id, hour_min(sp."startTime") AS start_time, hour_min(sp."stopTime") AS stop_time, sp.active FROM ((ramp_meter rm JOIN traffic_device_timing_plan tp ON ((rm.id = tp.traffic_device))) JOIN stratified_plan sp ON ((tp.timing_plan = sp.vault_oid)));


--
-- TOC entry 128 (OID 19211561)
-- Name: stratified; Type: ACL; Schema: public; Owner: tms
--

REVOKE ALL ON TABLE stratified FROM PUBLIC;
GRANT SELECT ON TABLE stratified TO PUBLIC;


SET SESSION AUTHORIZATION 'tms';

--
-- TOC entry 129 (OID 19211564)
-- Name: ramp_meter_location; Type: VIEW; Schema: public; Owner: tms
--

CREATE VIEW ramp_meter_location AS
    SELECT m.vault_oid, m.notes, m.id, m.detector, m."controlMode", m."singleRelease", m."storage", m."maxWait", m.camera, l.freeway, l.free_dir, l.cross_street, l.cross_dir, l.easting, l.northing, l.east_off, l.north_off FROM ((ramp_meter m JOIN "location" l ON ((m."location" = l.vault_oid))) LEFT JOIN controller c ON ((m.controller = c.vault_oid)));


--
-- TOC entry 130 (OID 19211564)
-- Name: ramp_meter_location; Type: ACL; Schema: public; Owner: tms
--

REVOKE ALL ON TABLE ramp_meter_location FROM PUBLIC;
GRANT SELECT ON TABLE ramp_meter_location TO PUBLIC;


SET SESSION AUTHORIZATION 'tms';

--
-- TOC entry 131 (OID 19211568)
-- Name: line_drop; Type: VIEW; Schema: public; Owner: tms
--

CREATE VIEW line_drop AS
    SELECT ctr.vault_oid, l."index" AS line, ctr."drop" FROM circuit, controller ctr, communication_line l WHERE ((ctr.circuit = circuit.vault_oid) AND (l.vault_oid = circuit.line)) ORDER BY l."index", ctr."drop";


--
-- TOC entry 132 (OID 19211568)
-- Name: line_drop; Type: ACL; Schema: public; Owner: tms
--

REVOKE ALL ON TABLE line_drop FROM PUBLIC;
GRANT SELECT ON TABLE line_drop TO PUBLIC;


SET SESSION AUTHORIZATION 'tms';

--
-- TOC entry 133 (OID 19211571)
-- Name: controller_alarm; Type: VIEW; Schema: public; Owner: tms
--

CREATE VIEW controller_alarm AS
    SELECT l.line, l."drop", a.pin, a.notes FROM line_drop l, alarm a WHERE (l.vault_oid = a.controller);


--
-- TOC entry 134 (OID 19211571)
-- Name: controller_alarm; Type: ACL; Schema: public; Owner: tms
--

REVOKE ALL ON TABLE controller_alarm FROM PUBLIC;
GRANT SELECT ON TABLE controller_alarm TO PUBLIC;


SET SESSION AUTHORIZATION 'tms';

--
-- TOC entry 135 (OID 19211574)
-- Name: iris_detector; Type: VIEW; Schema: public; Owner: tms
--

CREATE VIEW iris_detector AS
    SELECT d.pin, d."index" AS det_no, ld.line, c."drop", get_roadway_abbre(l.freeway) AS freeway_abbre, get_roadway_abbre(l.cross_street) AS crossstreet_abbre, get_roadway(l.freeway) AS freeway, get_dir(l.free_dir) AS free_dir, get_roadway(l.cross_street) AS cross_street, get_dir(l.cross_dir) AS cross_dir, boolean_converter(d.hov) AS hov, d."laneNumber" AS lane_number, d."fieldLength" AS field_length, ln.description AS lane_type, boolean_converter(d.abandoned) AS abandoned, boolean_converter(d."forceFail") AS force_fail, boolean_converter(c.active) AS active, d.fake, d.notes FROM ((((detector d LEFT JOIN "location" l ON ((d."location" = l.vault_oid))) LEFT JOIN lane_type_description ln ON ((d."laneType" = ln.lane_type_id))) LEFT JOIN controller c ON ((d.controller = c.vault_oid))) LEFT JOIN line_drop ld ON ((d.controller = ld.vault_oid))) ORDER BY d."index";


--
-- TOC entry 136 (OID 19211574)
-- Name: iris_detector; Type: ACL; Schema: public; Owner: tms
--

REVOKE ALL ON TABLE iris_detector FROM PUBLIC;
GRANT SELECT ON TABLE iris_detector TO PUBLIC;


SET SESSION AUTHORIZATION 'tms';

--
-- TOC entry 137 (OID 19211576)
-- Name: system_policy; Type: TABLE; Schema: public; Owner: tms
--

CREATE TABLE system_policy (
    name character varying NOT NULL,
    value integer NOT NULL
);


--
-- TOC entry 138 (OID 19211576)
-- Name: system_policy; Type: ACL; Schema: public; Owner: tms
--

REVOKE ALL ON TABLE system_policy FROM PUBLIC;
GRANT SELECT ON TABLE system_policy TO PUBLIC;


SET SESSION AUTHORIZATION 'tms';

--
-- TOC entry 139 (OID 19211583)
-- Name: location_view; Type: VIEW; Schema: public; Owner: tms
--

CREATE VIEW location_view AS
    SELECT l.vault_oid, l.vault_type, l.vault_refs, l.dummy_41, f.name AS freeway, f_dir.free_dir, c.name AS cross_street, c_dir.cross_dir, l.easting, l.east_off, l.northing, l.north_off FROM (((("location" l LEFT JOIN roadway f ON ((l.freeway = f.vault_oid))) LEFT JOIN roadway c ON ((l.cross_street = c.vault_oid))) LEFT JOIN direction f_dir ON ((l.free_dir = f_dir.id))) LEFT JOIN direction c_dir ON ((l.cross_dir = c_dir.id)));


--
-- TOC entry 140 (OID 19211587)
-- Name: dms_view; Type: VIEW; Schema: public; Owner: tms
--

CREATE VIEW dms_view AS
    SELECT d.id, d.notes, d.mile, d.travel, l_view.northing, l_view.north_off, l_view.easting, l_view.east_off, l_view.freeway, l_view.free_dir, l_view.cross_street, l_view.cross_dir FROM dms d, location_view l_view WHERE (d."location" = l_view.vault_oid);


--
-- TOC entry 141 (OID 19211587)
-- Name: dms_view; Type: ACL; Schema: public; Owner: tms
--

REVOKE ALL ON TABLE dms_view FROM PUBLIC;
GRANT SELECT ON TABLE dms_view TO PUBLIC;


SET SESSION AUTHORIZATION 'tms';

--
-- TOC entry 142 (OID 19211588)
-- Name: r_node_detector; Type: TABLE; Schema: public; Owner: tms
--

CREATE TABLE r_node_detector (
    r_node integer NOT NULL,
    detector integer NOT NULL
);


--
-- TOC entry 143 (OID 19211588)
-- Name: r_node_detector; Type: ACL; Schema: public; Owner: tms
--

REVOKE ALL ON TABLE r_node_detector FROM PUBLIC;
GRANT SELECT ON TABLE r_node_detector TO PUBLIC;


SET SESSION AUTHORIZATION 'tms';

--
-- TOC entry 144 (OID 19211590)
-- Name: r_node_type; Type: TABLE; Schema: public; Owner: tms
--

CREATE TABLE r_node_type (
    n_type integer NOT NULL,
    name text NOT NULL
);


--
-- TOC entry 145 (OID 19211595)
-- Name: r_node_transition; Type: TABLE; Schema: public; Owner: tms
--

CREATE TABLE r_node_transition (
    n_transition integer NOT NULL,
    name text NOT NULL
);


--
-- TOC entry 146 (OID 19211600)
-- Name: r_node; Type: TABLE; Schema: public; Owner: tms
--

CREATE TABLE r_node (
    "location" integer NOT NULL,
    node_type integer NOT NULL,
    pickable boolean NOT NULL,
    transition integer NOT NULL,
    lanes integer NOT NULL,
    attach_side boolean NOT NULL,
    shift integer NOT NULL,
    station_id text NOT NULL,
    speed_limit integer NOT NULL,
    notes text NOT NULL
)
INHERITS (tms_object);


--
-- TOC entry 147 (OID 19211600)
-- Name: r_node; Type: ACL; Schema: public; Owner: tms
--

REVOKE ALL ON TABLE r_node FROM PUBLIC;
GRANT SELECT ON TABLE r_node TO PUBLIC;


SET SESSION AUTHORIZATION 'tms';

--
-- TOC entry 148 (OID 19211608)
-- Name: r_node_view; Type: VIEW; Schema: public; Owner: tms
--

CREATE VIEW r_node_view AS
    SELECT n.vault_oid, f.name AS freeway, fd.free_dir, x.name AS cross_street, cd.cross_dir, nt.name AS node_type, n.pickable, tr.name AS transition, n.lanes, n.attach_side, n.shift, n.station_id, n.speed_limit, n.notes FROM r_node n, roadway f, direction fd, "location" l, roadway x, direction cd, r_node_type nt, r_node_transition tr WHERE (((((((n."location" = l.vault_oid) AND (f.vault_oid = l.freeway)) AND (fd.id = l.free_dir)) AND (x.vault_oid = l.cross_street)) AND (cd.id = l.cross_dir)) AND (nt.n_type = n.node_type)) AND (tr.n_transition = n.transition));


--
-- TOC entry 149 (OID 19211608)
-- Name: r_node_view; Type: ACL; Schema: public; Owner: tms
--

REVOKE ALL ON TABLE r_node_view FROM PUBLIC;
GRANT SELECT ON TABLE r_node_view TO PUBLIC;


SET SESSION AUTHORIZATION 'tms';

--
-- TOC entry 150 (OID 19211609)
-- Name: role; Type: TABLE; Schema: public; Owner: tms
--

CREATE TABLE role (
    name character varying(15) NOT NULL,
    pattern character varying(31) DEFAULT ''::character varying NOT NULL,
    priv_r boolean DEFAULT false NOT NULL,
    priv_w boolean DEFAULT false NOT NULL,
    priv_c boolean DEFAULT false NOT NULL,
    priv_d boolean DEFAULT false NOT NULL
);


--
-- TOC entry 151 (OID 19211616)
-- Name: iris_user; Type: TABLE; Schema: public; Owner: tms
--

CREATE TABLE iris_user (
    name character varying(15) NOT NULL,
    dn text NOT NULL,
    full_name character varying(31) NOT NULL
);


--
-- TOC entry 152 (OID 19211621)
-- Name: iris_user_role; Type: TABLE; Schema: public; Owner: tms
--

CREATE TABLE iris_user_role (
    iris_user character varying(15) NOT NULL,
    role character varying(15) NOT NULL
);


--
-- Data for TOC entry 238 (OID 19211197)
-- Name: vault_types; Type: TABLE DATA; Schema: public; Owner: tms
--

COPY vault_types (vault_oid, vault_type, vault_refs, "table", "className") FROM stdin;
3	4	0	vault_object	java.lang.Object
2	4	1	vault_counter	us.mn.state.dot.vault.Counter
24	4	0	java_util_AbstractCollection	java.util.AbstractCollection
23	4	0	java_util_AbstractList	java.util.AbstractList
1272	4	0	indexed_list	us.mn.state.dot.tms.IndexedListImpl
23834	4	0	ramp_meter	us.mn.state.dot.tms.RampMeterImpl
30	4	0	java_lang_Number	java.lang.Number
41	4	0	tms_object	us.mn.state.dot.tms.TMSObjectImpl
40	4	0	abstract_list	us.mn.state.dot.tms.AbstractListImpl
60	4	0	device	us.mn.state.dot.tms.DeviceImpl
59	4	0	traffic_device	us.mn.state.dot.tms.TrafficDeviceImpl
64	4	0	java_util_AbstractMap	java.util.AbstractMap
43417	4	0	meter_plan	us.mn.state.dot.tms.MeterPlanImpl
48330	4	0	meterable	us.mn.state.dot.tms.MeterableImpl
1395	4	0	java_util_TreeMap	java.util.TreeMap
52732	4	0	lcs_module	us.mn.state.dot.tms.LCSModuleImpl
52736	4	0	lcs	us.mn.state.dot.tms.LaneControlSignalImpl
1532	4	0	java_lang_Integer	java.lang.Integer
1536	4	0	vault_map	us.mn.state.dot.vault.MapEntry
51458	4	0	stratified_plan	us.mn.state.dot.tms.StratifiedPlanImpl
48316	4	0	segment	us.mn.state.dot.tms.SegmentImpl
43828	4	0	node_group	us.mn.state.dot.tms.NodeGroupImpl
64501	4	0	warning_sign	us.mn.state.dot.tms.WarningSignImpl
1396	4	0	communication_line	us.mn.state.dot.tms.CommunicationLineImpl
48190	4	0	segment_list	us.mn.state.dot.tms.SegmentListImpl
38	4	0	java_util_ArrayList	java.util.ArrayList
50057	4	0	meterable_cd	us.mn.state.dot.tms.MeterableCd
1535	4	0	controller	us.mn.state.dot.tms.ControllerImpl
63230	4	0	timing_plan	us.mn.state.dot.tms.TimingPlanImpl
43830	4	0	node	us.mn.state.dot.tms.NodeImpl
48326	4	0	off_ramp	us.mn.state.dot.tms.OffRampImpl
48329	4	0	on_ramp	us.mn.state.dot.tms.OnRampImpl
79334	4	0	alarm	us.mn.state.dot.tms.AlarmImpl
58	4	0	dms	us.mn.state.dot.tms.DMSImpl
134	4	0	camera	us.mn.state.dot.tms.CameraImpl
48317	4	0	station_segment	us.mn.state.dot.tms.StationSegmentImpl
43832	4	0	circuit	us.mn.state.dot.tms.CircuitImpl
1534	4	0	controller_170	us.mn.state.dot.tms.Controller170Impl
43415	4	0	simple_plan	us.mn.state.dot.tms.SimplePlanImpl
84656	4	0	r_node	us.mn.state.dot.tms.R_NodeImpl
37	4	0	vault_list	us.mn.state.dot.vault.ListElement
75	4	0	roadway	us.mn.state.dot.tms.RoadwayImpl
68616	4	0	location	us.mn.state.dot.tms.LocationImpl
2065	4	0	detector	us.mn.state.dot.tms.DetectorImpl
4	4	52	vault_types	us.mn.state.dot.vault.Type
\.


--
-- Data for TOC entry 239 (OID 19211203)
-- Name: vault_counter; Type: TABLE DATA; Schema: public; Owner: tms
--

COPY vault_counter (vault_oid, vault_type, vault_refs, id, logging) FROM stdin;
1	2	1	92319	f
\.


--
-- Data for TOC entry 265 (OID 19211309)
-- Name: direction; Type: TABLE DATA; Schema: public; Owner: tms
--

COPY direction (id, cross_dir, free_dir) FROM stdin;
5	\N	N-S
6	\N	E-W
1	NB	NB
3	EB	EB
4	WB	WB
2	SB	SB
0		
\.


--
-- Data for TOC entry 270 (OID 19211326)
-- Name: cabinet_types; Type: TABLE DATA; Schema: public; Owner: tms
--

COPY cabinet_types ("index", name) FROM stdin;
0	336
1	334Z
2	334D
3	334Z-94
4	Drum
5	334DZ
6	334
7	334Z-99
8	Reserved
9	S334Z
10	Prehistoric
11	334Z-00
12	Reserved
13	Reserved
14	Reserved
15	334ZP
\.


--
-- Data for TOC entry 271 (OID 19211331)
-- Name: lane_type_description; Type: TABLE DATA; Schema: public; Owner: tms
--

COPY lane_type_description (lane_type_id, description) FROM stdin;
0	 
1	Mainline
2	Auxilliary
3	CD Lane
4	Reversible
5	Merge
6	Queue
7	Exit
8	Bypass
9	Passage
10	Velocity
11	Omnibus
12	Green
\.


--
-- Data for TOC entry 295 (OID 19211488)
-- Name: road_modifier; Type: TABLE DATA; Schema: public; Owner: tms
--

COPY road_modifier (id, modifier) FROM stdin;
0	@
1	N of
2	S of
3	E of
4	W of
5	N Junction
6	S Junction
7	E Junction
8	W Junction
\.


--
-- Data for TOC entry 301 (OID 19211576)
-- Name: system_policy; Type: TABLE DATA; Schema: public; Owner: tms
--

COPY system_policy (name, value) FROM stdin;
meter_green_time	13
meter_yellow_time	7
meter_min_red_time	1
dms_page_on_time	20
dms_page_off_time	0
ring_radius_0	2
ring_radius_1	5
ring_radius_2	10
\.


--
-- Data for TOC entry 303 (OID 19211590)
-- Name: r_node_type; Type: TABLE DATA; Schema: public; Owner: tms
--

COPY r_node_type (n_type, name) FROM stdin;
0	station
1	entrance
2	exit
3	intersection
4	access
5	interchange
\.


--
-- Data for TOC entry 304 (OID 19211595)
-- Name: r_node_transition; Type: TABLE DATA; Schema: public; Owner: tms
--

COPY r_node_transition (n_transition, name) FROM stdin;
0	none
1	loop
2	leg
3	slipramp
4	CD
5	HOV
6	common
7	flyover
\.


--
-- Data for TOC entry 306 (OID 19211609)
-- Name: role; Type: TABLE DATA; Schema: public; Owner: tms
--

COPY role (name, pattern, priv_r, priv_w, priv_c, priv_d) FROM stdin;
admin		f	f	f	f
alert		f	f	f	f
incidents		f	f	f	f
view	.*	t	f	f	f
dms	dms/.*/message	f	t	f	f
tiger	dms/VT.*	f	t	t	f
activate	.*/.*/active	f	t	f	f
meter	meter/.*/metering	f	t	f	f
lcs	lcs/.*/signals	f	t	f	f
user_admin	user/.*	t	t	t	t
role_admin	role/.*	t	t	t	t
\.


--
-- TOC entry 154 (OID 19211751)
-- Name: vault_object_vault_oid_key; Type: INDEX; Schema: public; Owner: tms
--

CREATE UNIQUE INDEX vault_object_vault_oid_key ON vault_object USING btree (vault_oid);


--
-- TOC entry 153 (OID 19211752)
-- Name: vault_object_pkey; Type: INDEX; Schema: public; Owner: tms
--

CREATE UNIQUE INDEX vault_object_pkey ON vault_object USING btree (vault_oid);


--
-- TOC entry 155 (OID 19211753)
-- Name: vault_types_pkey; Type: INDEX; Schema: public; Owner: tms
--

CREATE UNIQUE INDEX vault_types_pkey ON vault_types USING btree (vault_oid);


--
-- TOC entry 156 (OID 19211754)
-- Name: vault_counter_pkey; Type: INDEX; Schema: public; Owner: tms
--

CREATE UNIQUE INDEX vault_counter_pkey ON vault_counter USING btree (vault_oid);


--
-- TOC entry 157 (OID 19211755)
-- Name: vault_log_entry_pkey; Type: INDEX; Schema: public; Owner: tms
--

CREATE UNIQUE INDEX vault_log_entry_pkey ON vault_log_entry USING btree (vault_oid);


--
-- TOC entry 158 (OID 19211756)
-- Name: va_util_AbstractCollection_pkey; Type: INDEX; Schema: public; Owner: tms
--

CREATE UNIQUE INDEX "va_util_AbstractCollection_pkey" ON "java_util_AbstractCollection" USING btree (vault_oid);


--
-- TOC entry 159 (OID 19211757)
-- Name: java_util_AbstractList_pkey; Type: INDEX; Schema: public; Owner: tms
--

CREATE UNIQUE INDEX "java_util_AbstractList_pkey" ON "java_util_AbstractList" USING btree (vault_oid);


--
-- TOC entry 162 (OID 19211760)
-- Name: vault_transaction_pkey; Type: INDEX; Schema: public; Owner: tms
--

CREATE UNIQUE INDEX vault_transaction_pkey ON vault_transaction USING btree (vault_oid);


--
-- TOC entry 163 (OID 19211761)
-- Name: java_lang_Number_pkey; Type: INDEX; Schema: public; Owner: tms
--

CREATE UNIQUE INDEX "java_lang_Number_pkey" ON "java_lang_Number" USING btree (vault_oid);


--
-- TOC entry 164 (OID 19211762)
-- Name: java_lang_Integer_pkey; Type: INDEX; Schema: public; Owner: tms
--

CREATE UNIQUE INDEX "java_lang_Integer_pkey" ON "java_lang_Integer" USING btree (vault_oid);


--
-- TOC entry 165 (OID 19211763)
-- Name: java_util_ArrayList_pkey; Type: INDEX; Schema: public; Owner: tms
--

CREATE UNIQUE INDEX "java_util_ArrayList_pkey" ON "java_util_ArrayList" USING btree (vault_oid);


--
-- TOC entry 166 (OID 19211764)
-- Name: tms_object_pkey; Type: INDEX; Schema: public; Owner: tms
--

CREATE UNIQUE INDEX tms_object_pkey ON tms_object USING btree (vault_oid);


--
-- TOC entry 167 (OID 19211765)
-- Name: abstract_list_pkey; Type: INDEX; Schema: public; Owner: tms
--

CREATE UNIQUE INDEX abstract_list_pkey ON abstract_list USING btree (vault_oid);


--
-- TOC entry 168 (OID 19211766)
-- Name: device_pkey; Type: INDEX; Schema: public; Owner: tms
--

CREATE UNIQUE INDEX device_pkey ON device USING btree (vault_oid);


--
-- TOC entry 169 (OID 19211767)
-- Name: traffic_device_pkey; Type: INDEX; Schema: public; Owner: tms
--

CREATE UNIQUE INDEX traffic_device_pkey ON traffic_device USING btree (vault_oid);


--
-- TOC entry 171 (OID 19211768)
-- Name: dms_pkey; Type: INDEX; Schema: public; Owner: tms
--

CREATE UNIQUE INDEX dms_pkey ON dms USING btree (vault_oid);


--
-- TOC entry 172 (OID 19211769)
-- Name: java_util_AbstractMap_pkey; Type: INDEX; Schema: public; Owner: tms
--

CREATE UNIQUE INDEX "java_util_AbstractMap_pkey" ON "java_util_AbstractMap" USING btree (vault_oid);


--
-- TOC entry 173 (OID 19211770)
-- Name: java_util_TreeMap_pkey; Type: INDEX; Schema: public; Owner: tms
--

CREATE UNIQUE INDEX "java_util_TreeMap_pkey" ON "java_util_TreeMap" USING btree (vault_oid);


--
-- TOC entry 174 (OID 19211771)
-- Name: roadway_pkey; Type: INDEX; Schema: public; Owner: tms
--

CREATE UNIQUE INDEX roadway_pkey ON roadway USING btree (vault_oid);


--
-- TOC entry 176 (OID 19211773)
-- Name: indexed_list_pkey; Type: INDEX; Schema: public; Owner: tms
--

CREATE UNIQUE INDEX indexed_list_pkey ON indexed_list USING btree (vault_oid);


--
-- TOC entry 180 (OID 19211776)
-- Name: detector_pkey; Type: INDEX; Schema: public; Owner: tms
--

CREATE UNIQUE INDEX detector_pkey ON detector USING btree (vault_oid);


--
-- TOC entry 181 (OID 19211777)
-- Name: ramp_meter_pkey; Type: INDEX; Schema: public; Owner: tms
--

CREATE UNIQUE INDEX ramp_meter_pkey ON ramp_meter USING btree (vault_oid);


--
-- TOC entry 183 (OID 19211778)
-- Name: timing_plan_pkey; Type: INDEX; Schema: public; Owner: tms
--

CREATE UNIQUE INDEX timing_plan_pkey ON timing_plan USING btree (vault_oid);


--
-- TOC entry 184 (OID 19211779)
-- Name: simple_plan_pkey; Type: INDEX; Schema: public; Owner: tms
--

CREATE UNIQUE INDEX simple_plan_pkey ON simple_plan USING btree (vault_oid);


--
-- TOC entry 185 (OID 19211780)
-- Name: circuit_pkey; Type: INDEX; Schema: public; Owner: tms
--

CREATE UNIQUE INDEX circuit_pkey ON circuit USING btree (vault_oid);


--
-- TOC entry 188 (OID 19211781)
-- Name: segment_list_pkey; Type: INDEX; Schema: public; Owner: tms
--

CREATE UNIQUE INDEX segment_list_pkey ON segment_list USING btree (vault_oid);


--
-- TOC entry 189 (OID 19211782)
-- Name: stratified_plan_pkey; Type: INDEX; Schema: public; Owner: tms
--

CREATE UNIQUE INDEX stratified_plan_pkey ON stratified_plan USING btree (vault_oid);


--
-- TOC entry 190 (OID 19211783)
-- Name: segment_pkey; Type: INDEX; Schema: public; Owner: tms
--

CREATE UNIQUE INDEX segment_pkey ON segment USING btree (vault_oid);


--
-- TOC entry 191 (OID 19211784)
-- Name: off_ramp_pkey; Type: INDEX; Schema: public; Owner: tms
--

CREATE UNIQUE INDEX off_ramp_pkey ON off_ramp USING btree (vault_oid);


--
-- TOC entry 192 (OID 19211785)
-- Name: meterable_pkey; Type: INDEX; Schema: public; Owner: tms
--

CREATE UNIQUE INDEX meterable_pkey ON meterable USING btree (vault_oid);


--
-- TOC entry 193 (OID 19211786)
-- Name: on_ramp_pkey; Type: INDEX; Schema: public; Owner: tms
--

CREATE UNIQUE INDEX on_ramp_pkey ON on_ramp USING btree (vault_oid);


--
-- TOC entry 196 (OID 19211788)
-- Name: lcs_module_pkey; Type: INDEX; Schema: public; Owner: tms
--

CREATE UNIQUE INDEX lcs_module_pkey ON lcs_module USING btree (vault_oid);


--
-- TOC entry 197 (OID 19211789)
-- Name: lcs_pkey; Type: INDEX; Schema: public; Owner: tms
--

CREATE UNIQUE INDEX lcs_pkey ON lcs USING btree (vault_oid);


--
-- TOC entry 198 (OID 19211790)
-- Name: node_pkey; Type: INDEX; Schema: public; Owner: tms
--

CREATE UNIQUE INDEX node_pkey ON node USING btree (vault_oid);


--
-- TOC entry 199 (OID 19211791)
-- Name: node_group_pkey; Type: INDEX; Schema: public; Owner: tms
--

CREATE UNIQUE INDEX node_group_pkey ON node_group USING btree (vault_oid);


--
-- TOC entry 200 (OID 19211792)
-- Name: controller_pkey; Type: INDEX; Schema: public; Owner: tms
--

CREATE UNIQUE INDEX controller_pkey ON controller USING btree (vault_oid);


--
-- TOC entry 201 (OID 19211793)
-- Name: controller_170_pkey; Type: INDEX; Schema: public; Owner: tms
--

CREATE UNIQUE INDEX controller_170_pkey ON controller_170 USING btree (vault_oid);


--
-- TOC entry 202 (OID 19211794)
-- Name: communication_line_pkey; Type: INDEX; Schema: public; Owner: tms
--

CREATE UNIQUE INDEX communication_line_pkey ON communication_line USING btree (vault_oid);


--
-- TOC entry 194 (OID 19211795)
-- Name: meterable_cd_pkey; Type: INDEX; Schema: public; Owner: tms
--

CREATE UNIQUE INDEX meterable_cd_pkey ON meterable_cd USING btree (vault_oid);


--
-- TOC entry 170 (OID 19211797)
-- Name: dms_id_index; Type: INDEX; Schema: public; Owner: tms
--

CREATE UNIQUE INDEX dms_id_index ON dms USING btree (id);


--
-- TOC entry 204 (OID 19211798)
-- Name: warning_sign_pkey; Type: INDEX; Schema: public; Owner: tms
--

CREATE UNIQUE INDEX warning_sign_pkey ON warning_sign USING btree (vault_oid);


--
-- TOC entry 207 (OID 19211799)
-- Name: alarm_pkey; Type: INDEX; Schema: public; Owner: tms
--

CREATE UNIQUE INDEX alarm_pkey ON alarm USING btree (vault_oid);


--
-- TOC entry 179 (OID 19211800)
-- Name: detector_index; Type: INDEX; Schema: public; Owner: tms
--

CREATE UNIQUE INDEX detector_index ON detector USING btree ("index");


--
-- TOC entry 209 (OID 19211802)
-- Name: r_node_pkey; Type: INDEX; Schema: public; Owner: tms
--

CREATE UNIQUE INDEX r_node_pkey ON r_node USING btree (vault_oid);


--
-- TOC entry 182 (OID 19211803)
-- Name: direction_pkey; Type: CONSTRAINT; Schema: public; Owner: tms
--

ALTER TABLE ONLY direction
    ADD CONSTRAINT direction_pkey PRIMARY KEY (id);


--
-- TOC entry 186 (OID 19211805)
-- Name: lane_type_description_pkey; Type: CONSTRAINT; Schema: public; Owner: tms
--

ALTER TABLE ONLY lane_type_description
    ADD CONSTRAINT lane_type_description_pkey PRIMARY KEY (lane_type_id);


--
-- TOC entry 187 (OID 19211807)
-- Name: time_plan_log_pkey; Type: CONSTRAINT; Schema: public; Owner: tms
--

ALTER TABLE ONLY time_plan_log
    ADD CONSTRAINT time_plan_log_pkey PRIMARY KEY (event_id);


--
-- TOC entry 205 (OID 19211809)
-- Name: dms_message_pkey; Type: CONSTRAINT; Schema: public; Owner: tms
--

ALTER TABLE ONLY dms_message
    ADD CONSTRAINT dms_message_pkey PRIMARY KEY (id);


--
-- TOC entry 206 (OID 19211811)
-- Name: road_modifier_pkey; Type: CONSTRAINT; Schema: public; Owner: tms
--

ALTER TABLE ONLY road_modifier
    ADD CONSTRAINT road_modifier_pkey PRIMARY KEY (id);


--
-- TOC entry 210 (OID 19211813)
-- Name: role_pkey; Type: CONSTRAINT; Schema: public; Owner: tms
--

ALTER TABLE ONLY role
    ADD CONSTRAINT role_pkey PRIMARY KEY (name);


--
-- TOC entry 211 (OID 19211815)
-- Name: iris_user_pkey; Type: CONSTRAINT; Schema: public; Owner: tms
--

ALTER TABLE ONLY iris_user
    ADD CONSTRAINT iris_user_pkey PRIMARY KEY (name);


--
-- TOC entry 309 (OID 19211817)
-- Name: fk_free_dir; Type: FK CONSTRAINT; Schema: public; Owner: tms
--

ALTER TABLE ONLY "location"
    ADD CONSTRAINT fk_free_dir FOREIGN KEY (free_dir) REFERENCES direction(id);


--
-- TOC entry 310 (OID 19211821)
-- Name: fk_cross_dir; Type: FK CONSTRAINT; Schema: public; Owner: tms
--

ALTER TABLE ONLY "location"
    ADD CONSTRAINT fk_cross_dir FOREIGN KEY (cross_dir) REFERENCES direction(id);


--
-- TOC entry 311 (OID 19211825)
-- Name: fk_cross_mod; Type: FK CONSTRAINT; Schema: public; Owner: tms
--

ALTER TABLE ONLY "location"
    ADD CONSTRAINT fk_cross_mod FOREIGN KEY (cross_mod) REFERENCES road_modifier(id);


--
-- TOC entry 312 (OID 19211829)
-- Name: $1; Type: FK CONSTRAINT; Schema: public; Owner: tms
--

ALTER TABLE ONLY segment_detector
    ADD CONSTRAINT "$1" FOREIGN KEY (detector) REFERENCES detector("index");


--
-- TOC entry 313 (OID 19211833)
-- Name: $2; Type: FK CONSTRAINT; Schema: public; Owner: tms
--

ALTER TABLE ONLY r_node_detector
    ADD CONSTRAINT "$2" FOREIGN KEY (detector) REFERENCES detector("index");


--
-- TOC entry 314 (OID 19211837)
-- Name: $1; Type: FK CONSTRAINT; Schema: public; Owner: tms
--

ALTER TABLE ONLY r_node_detector
    ADD CONSTRAINT "$1" FOREIGN KEY (r_node) REFERENCES r_node(vault_oid);


--
-- TOC entry 315 (OID 19211841)
-- Name: $1; Type: FK CONSTRAINT; Schema: public; Owner: tms
--

ALTER TABLE ONLY iris_user_role
    ADD CONSTRAINT "$1" FOREIGN KEY (iris_user) REFERENCES iris_user(name);


--
-- TOC entry 316 (OID 19211845)
-- Name: $2; Type: FK CONSTRAINT; Schema: public; Owner: tms
--

ALTER TABLE ONLY iris_user_role
    ADD CONSTRAINT "$2" FOREIGN KEY (role) REFERENCES role(name);


--
-- TOC entry 337 (OID 19211849)
-- Name: RI_ConstraintTrigger_19211849; Type: TRIGGER; Schema: public; Owner: tms
--

CREATE CONSTRAINT TRIGGER "<unnamed>"
    AFTER INSERT OR UPDATE ON node
    FROM node_group
    NOT DEFERRABLE INITIALLY IMMEDIATE
    FOR EACH ROW
    EXECUTE PROCEDURE "RI_FKey_check_ins"('<unnamed>', 'node', 'node_group', 'FULL', 'node_group', 'vault_oid');


--
-- TOC entry 343 (OID 19211850)
-- Name: RI_ConstraintTrigger_19211850; Type: TRIGGER; Schema: public; Owner: tms
--

CREATE CONSTRAINT TRIGGER "<unnamed>"
    AFTER DELETE ON node_group
    FROM node
    NOT DEFERRABLE INITIALLY IMMEDIATE
    FOR EACH ROW
    EXECUTE PROCEDURE "RI_FKey_noaction_del"('<unnamed>', 'node', 'node_group', 'FULL', 'node_group', 'vault_oid');


--
-- TOC entry 344 (OID 19211851)
-- Name: RI_ConstraintTrigger_19211851; Type: TRIGGER; Schema: public; Owner: tms
--

CREATE CONSTRAINT TRIGGER "<unnamed>"
    AFTER UPDATE ON node_group
    FROM node
    NOT DEFERRABLE INITIALLY IMMEDIATE
    FOR EACH ROW
    EXECUTE PROCEDURE "RI_FKey_noaction_upd"('<unnamed>', 'node', 'node_group', 'FULL', 'node_group', 'vault_oid');


--
-- TOC entry 327 (OID 19211852)
-- Name: RI_ConstraintTrigger_19211852; Type: TRIGGER; Schema: public; Owner: tms
--

CREATE CONSTRAINT TRIGGER "<unnamed>"
    AFTER INSERT OR UPDATE ON circuit
    FROM node
    NOT DEFERRABLE INITIALLY IMMEDIATE
    FOR EACH ROW
    EXECUTE PROCEDURE "RI_FKey_check_ins"('<unnamed>', 'circuit', 'node', 'FULL', 'node', 'vault_oid');


--
-- TOC entry 338 (OID 19211853)
-- Name: RI_ConstraintTrigger_19211853; Type: TRIGGER; Schema: public; Owner: tms
--

CREATE CONSTRAINT TRIGGER "<unnamed>"
    AFTER DELETE ON node
    FROM circuit
    NOT DEFERRABLE INITIALLY IMMEDIATE
    FOR EACH ROW
    EXECUTE PROCEDURE "RI_FKey_noaction_del"('<unnamed>', 'circuit', 'node', 'FULL', 'node', 'vault_oid');


--
-- TOC entry 339 (OID 19211854)
-- Name: RI_ConstraintTrigger_19211854; Type: TRIGGER; Schema: public; Owner: tms
--

CREATE CONSTRAINT TRIGGER "<unnamed>"
    AFTER UPDATE ON node
    FROM circuit
    NOT DEFERRABLE INITIALLY IMMEDIATE
    FOR EACH ROW
    EXECUTE PROCEDURE "RI_FKey_noaction_upd"('<unnamed>', 'circuit', 'node', 'FULL', 'node', 'vault_oid');


--
-- TOC entry 347 (OID 19211855)
-- Name: RI_ConstraintTrigger_19211855; Type: TRIGGER; Schema: public; Owner: tms
--

CREATE CONSTRAINT TRIGGER "<unnamed>"
    AFTER INSERT OR UPDATE ON controller
    FROM circuit
    NOT DEFERRABLE INITIALLY IMMEDIATE
    FOR EACH ROW
    EXECUTE PROCEDURE "RI_FKey_check_ins"('<unnamed>', 'controller', 'circuit', 'FULL', 'circuit', 'vault_oid');


--
-- TOC entry 328 (OID 19211856)
-- Name: RI_ConstraintTrigger_19211856; Type: TRIGGER; Schema: public; Owner: tms
--

CREATE CONSTRAINT TRIGGER "<unnamed>"
    AFTER DELETE ON circuit
    FROM controller
    NOT DEFERRABLE INITIALLY IMMEDIATE
    FOR EACH ROW
    EXECUTE PROCEDURE "RI_FKey_noaction_del"('<unnamed>', 'controller', 'circuit', 'FULL', 'circuit', 'vault_oid');


--
-- TOC entry 329 (OID 19211857)
-- Name: RI_ConstraintTrigger_19211857; Type: TRIGGER; Schema: public; Owner: tms
--

CREATE CONSTRAINT TRIGGER "<unnamed>"
    AFTER UPDATE ON circuit
    FROM controller
    NOT DEFERRABLE INITIALLY IMMEDIATE
    FOR EACH ROW
    EXECUTE PROCEDURE "RI_FKey_noaction_upd"('<unnamed>', 'controller', 'circuit', 'FULL', 'circuit', 'vault_oid');


--
-- TOC entry 330 (OID 19211858)
-- Name: RI_ConstraintTrigger_19211858; Type: TRIGGER; Schema: public; Owner: tms
--

CREATE CONSTRAINT TRIGGER "<unnamed>"
    AFTER INSERT OR UPDATE ON circuit
    FROM communication_line
    NOT DEFERRABLE INITIALLY IMMEDIATE
    FOR EACH ROW
    EXECUTE PROCEDURE "RI_FKey_check_ins"('<unnamed>', 'circuit', 'communication_line', 'FULL', 'line', 'vault_oid');


--
-- TOC entry 349 (OID 19211859)
-- Name: RI_ConstraintTrigger_19211859; Type: TRIGGER; Schema: public; Owner: tms
--

CREATE CONSTRAINT TRIGGER "<unnamed>"
    AFTER DELETE ON communication_line
    FROM circuit
    NOT DEFERRABLE INITIALLY IMMEDIATE
    FOR EACH ROW
    EXECUTE PROCEDURE "RI_FKey_noaction_del"('<unnamed>', 'circuit', 'communication_line', 'FULL', 'line', 'vault_oid');


--
-- TOC entry 350 (OID 19211860)
-- Name: RI_ConstraintTrigger_19211860; Type: TRIGGER; Schema: public; Owner: tms
--

CREATE CONSTRAINT TRIGGER "<unnamed>"
    AFTER UPDATE ON communication_line
    FROM circuit
    NOT DEFERRABLE INITIALLY IMMEDIATE
    FOR EACH ROW
    EXECUTE PROCEDURE "RI_FKey_noaction_upd"('<unnamed>', 'circuit', 'communication_line', 'FULL', 'line', 'vault_oid');


--
-- TOC entry 326 (OID 19211861)
-- Name: time_plan_log_trig; Type: TRIGGER; Schema: public; Owner: tms
--

CREATE TRIGGER time_plan_log_trig
    AFTER UPDATE ON simple_plan
    FOR EACH ROW
    EXECUTE PROCEDURE time_plan_log();


--
-- TOC entry 322 (OID 19211862)
-- Name: detector_fieldlength_log_trig; Type: TRIGGER; Schema: public; Owner: tms
--

CREATE TRIGGER detector_fieldlength_log_trig
    AFTER UPDATE ON detector
    FOR EACH ROW
    EXECUTE PROCEDURE detector_fieldlength_log();


--
-- TOC entry 321 (OID 19211863)
-- Name: add_detector_trig; Type: TRIGGER; Schema: public; Owner: tms
--

CREATE TRIGGER add_detector_trig
    AFTER INSERT ON detector
    FOR EACH ROW
    EXECUTE PROCEDURE add_detector();


--
-- TOC entry 323 (OID 19211864)
-- Name: remove_detector_trig; Type: TRIGGER; Schema: public; Owner: tms
--

CREATE TRIGGER remove_detector_trig
    AFTER DELETE ON detector
    FOR EACH ROW
    EXECUTE PROCEDURE remove_detector();


--
-- TOC entry 319 (OID 19211865)
-- Name: add_dms_trig; Type: TRIGGER; Schema: public; Owner: tms
--

CREATE TRIGGER add_dms_trig
    AFTER INSERT ON dms
    FOR EACH ROW
    EXECUTE PROCEDURE add_dms();


--
-- TOC entry 320 (OID 19211866)
-- Name: remove_dms_trig; Type: TRIGGER; Schema: public; Owner: tms
--

CREATE TRIGGER remove_dms_trig
    AFTER DELETE ON dms
    FOR EACH ROW
    EXECUTE PROCEDURE remove_dms();


--
-- TOC entry 324 (OID 19211867)
-- Name: add_meter_trig; Type: TRIGGER; Schema: public; Owner: tms
--

CREATE TRIGGER add_meter_trig
    AFTER INSERT ON ramp_meter
    FOR EACH ROW
    EXECUTE PROCEDURE add_meter();


--
-- TOC entry 325 (OID 19211868)
-- Name: remove_meter_trig; Type: TRIGGER; Schema: public; Owner: tms
--

CREATE TRIGGER remove_meter_trig
    AFTER DELETE ON ramp_meter
    FOR EACH ROW
    EXECUTE PROCEDURE remove_meter();


--
-- TOC entry 335 (OID 19211869)
-- Name: add_camera_trig; Type: TRIGGER; Schema: public; Owner: tms
--

CREATE TRIGGER add_camera_trig
    AFTER INSERT ON camera
    FOR EACH ROW
    EXECUTE PROCEDURE add_camera();


--
-- TOC entry 336 (OID 19211870)
-- Name: remove_camera_trig; Type: TRIGGER; Schema: public; Owner: tms
--

CREATE TRIGGER remove_camera_trig
    AFTER DELETE ON camera
    FOR EACH ROW
    EXECUTE PROCEDURE remove_camera();


--
-- TOC entry 340 (OID 19211871)
-- Name: RI_ConstraintTrigger_19211871; Type: TRIGGER; Schema: public; Owner: tms
--

CREATE CONSTRAINT TRIGGER "<unnamed>"
    AFTER INSERT OR UPDATE ON node
    FROM node_group
    NOT DEFERRABLE INITIALLY IMMEDIATE
    FOR EACH ROW
    EXECUTE PROCEDURE "RI_FKey_check_ins"('<unnamed>', 'node', 'node_group', 'FULL', 'node_group', 'vault_oid');


--
-- TOC entry 345 (OID 19211872)
-- Name: RI_ConstraintTrigger_19211872; Type: TRIGGER; Schema: public; Owner: tms
--

CREATE CONSTRAINT TRIGGER "<unnamed>"
    AFTER DELETE ON node_group
    FROM node
    NOT DEFERRABLE INITIALLY IMMEDIATE
    FOR EACH ROW
    EXECUTE PROCEDURE "RI_FKey_noaction_del"('<unnamed>', 'node', 'node_group', 'FULL', 'node_group', 'vault_oid');


--
-- TOC entry 346 (OID 19211873)
-- Name: RI_ConstraintTrigger_19211873; Type: TRIGGER; Schema: public; Owner: tms
--

CREATE CONSTRAINT TRIGGER "<unnamed>"
    AFTER UPDATE ON node_group
    FROM node
    NOT DEFERRABLE INITIALLY IMMEDIATE
    FOR EACH ROW
    EXECUTE PROCEDURE "RI_FKey_noaction_upd"('<unnamed>', 'node', 'node_group', 'FULL', 'node_group', 'vault_oid');


--
-- TOC entry 331 (OID 19211874)
-- Name: RI_ConstraintTrigger_19211874; Type: TRIGGER; Schema: public; Owner: tms
--

CREATE CONSTRAINT TRIGGER "<unnamed>"
    AFTER INSERT OR UPDATE ON circuit
    FROM node
    NOT DEFERRABLE INITIALLY IMMEDIATE
    FOR EACH ROW
    EXECUTE PROCEDURE "RI_FKey_check_ins"('<unnamed>', 'circuit', 'node', 'FULL', 'node', 'vault_oid');


--
-- TOC entry 341 (OID 19211875)
-- Name: RI_ConstraintTrigger_19211875; Type: TRIGGER; Schema: public; Owner: tms
--

CREATE CONSTRAINT TRIGGER "<unnamed>"
    AFTER DELETE ON node
    FROM circuit
    NOT DEFERRABLE INITIALLY IMMEDIATE
    FOR EACH ROW
    EXECUTE PROCEDURE "RI_FKey_noaction_del"('<unnamed>', 'circuit', 'node', 'FULL', 'node', 'vault_oid');


--
-- TOC entry 342 (OID 19211876)
-- Name: RI_ConstraintTrigger_19211876; Type: TRIGGER; Schema: public; Owner: tms
--

CREATE CONSTRAINT TRIGGER "<unnamed>"
    AFTER UPDATE ON node
    FROM circuit
    NOT DEFERRABLE INITIALLY IMMEDIATE
    FOR EACH ROW
    EXECUTE PROCEDURE "RI_FKey_noaction_upd"('<unnamed>', 'circuit', 'node', 'FULL', 'node', 'vault_oid');


--
-- TOC entry 348 (OID 19211877)
-- Name: RI_ConstraintTrigger_19211877; Type: TRIGGER; Schema: public; Owner: tms
--

CREATE CONSTRAINT TRIGGER "<unnamed>"
    AFTER INSERT OR UPDATE ON controller
    FROM circuit
    NOT DEFERRABLE INITIALLY IMMEDIATE
    FOR EACH ROW
    EXECUTE PROCEDURE "RI_FKey_check_ins"('<unnamed>', 'controller', 'circuit', 'FULL', 'circuit', 'vault_oid');


--
-- TOC entry 332 (OID 19211878)
-- Name: RI_ConstraintTrigger_19211878; Type: TRIGGER; Schema: public; Owner: tms
--

CREATE CONSTRAINT TRIGGER "<unnamed>"
    AFTER DELETE ON circuit
    FROM controller
    NOT DEFERRABLE INITIALLY IMMEDIATE
    FOR EACH ROW
    EXECUTE PROCEDURE "RI_FKey_noaction_del"('<unnamed>', 'controller', 'circuit', 'FULL', 'circuit', 'vault_oid');


--
-- TOC entry 333 (OID 19211879)
-- Name: RI_ConstraintTrigger_19211879; Type: TRIGGER; Schema: public; Owner: tms
--

CREATE CONSTRAINT TRIGGER "<unnamed>"
    AFTER UPDATE ON circuit
    FROM controller
    NOT DEFERRABLE INITIALLY IMMEDIATE
    FOR EACH ROW
    EXECUTE PROCEDURE "RI_FKey_noaction_upd"('<unnamed>', 'controller', 'circuit', 'FULL', 'circuit', 'vault_oid');


--
-- TOC entry 334 (OID 19211880)
-- Name: RI_ConstraintTrigger_19211880; Type: TRIGGER; Schema: public; Owner: tms
--

CREATE CONSTRAINT TRIGGER "<unnamed>"
    AFTER INSERT OR UPDATE ON circuit
    FROM communication_line
    NOT DEFERRABLE INITIALLY IMMEDIATE
    FOR EACH ROW
    EXECUTE PROCEDURE "RI_FKey_check_ins"('<unnamed>', 'circuit', 'communication_line', 'FULL', 'line', 'vault_oid');


--
-- TOC entry 351 (OID 19211881)
-- Name: RI_ConstraintTrigger_19211881; Type: TRIGGER; Schema: public; Owner: tms
--

CREATE CONSTRAINT TRIGGER "<unnamed>"
    AFTER DELETE ON communication_line
    FROM circuit
    NOT DEFERRABLE INITIALLY IMMEDIATE
    FOR EACH ROW
    EXECUTE PROCEDURE "RI_FKey_noaction_del"('<unnamed>', 'circuit', 'communication_line', 'FULL', 'line', 'vault_oid');


--
-- TOC entry 352 (OID 19211882)
-- Name: RI_ConstraintTrigger_19211882; Type: TRIGGER; Schema: public; Owner: tms
--

CREATE CONSTRAINT TRIGGER "<unnamed>"
    AFTER UPDATE ON communication_line
    FROM circuit
    NOT DEFERRABLE INITIALLY IMMEDIATE
    FOR EACH ROW
    EXECUTE PROCEDURE "RI_FKey_noaction_upd"('<unnamed>', 'circuit', 'communication_line', 'FULL', 'line', 'vault_oid');


--
-- TOC entry 353 (OID 19211883)
-- Name: RI_ConstraintTrigger_19211883; Type: TRIGGER; Schema: public; Owner: tms
--

CREATE CONSTRAINT TRIGGER "<unnamed>"
    AFTER INSERT OR UPDATE ON dms_message
    FROM dms
    NOT DEFERRABLE INITIALLY IMMEDIATE
    FOR EACH ROW
    EXECUTE PROCEDURE "RI_FKey_check_ins"('<unnamed>', 'dms_message', 'dms', 'UNSPECIFIED', 'dms', 'id');


--
-- TOC entry 317 (OID 19211884)
-- Name: RI_ConstraintTrigger_19211884; Type: TRIGGER; Schema: public; Owner: tms
--

CREATE CONSTRAINT TRIGGER "<unnamed>"
    AFTER DELETE ON dms
    FROM dms_message
    NOT DEFERRABLE INITIALLY IMMEDIATE
    FOR EACH ROW
    EXECUTE PROCEDURE "RI_FKey_cascade_del"('<unnamed>', 'dms_message', 'dms', 'UNSPECIFIED', 'dms', 'id');


--
-- TOC entry 318 (OID 19211885)
-- Name: RI_ConstraintTrigger_19211885; Type: TRIGGER; Schema: public; Owner: tms
--

CREATE CONSTRAINT TRIGGER "<unnamed>"
    AFTER UPDATE ON dms
    FROM dms_message
    NOT DEFERRABLE INITIALLY IMMEDIATE
    FOR EACH ROW
    EXECUTE PROCEDURE "RI_FKey_noaction_upd"('<unnamed>', 'dms_message', 'dms', 'UNSPECIFIED', 'dms', 'id');


--
-- TOC entry 6 (OID 19211190)
-- Name: lane_type_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tms
--

SELECT pg_catalog.setval('lane_type_id_seq', 12, true);


--
-- TOC entry 8 (OID 19211192)
-- Name: tms_log_seq; Type: SEQUENCE SET; Schema: public; Owner: tms
--

SELECT pg_catalog.setval('tms_log_seq', 8284, true);


--
-- TOC entry 10 (OID 19211475)
-- Name: dms_message_seq; Type: SEQUENCE SET; Schema: public; Owner: tms
--

SELECT pg_catalog.setval('dms_message_seq', 4768, true);


SET SESSION AUTHORIZATION 'postgres';

--
-- TOC entry 3 (OID 2200)
-- Name: SCHEMA public; Type: COMMENT; Schema: -; Owner: postgres
--

COMMENT ON SCHEMA public IS 'Standard public schema';
