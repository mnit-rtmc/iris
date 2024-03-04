// query.rs
//
// Copyright (C) 2018-2024  Minnesota Department of Transportation
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//

/// SQL query for all alarms (minimal)
pub const ALARM_ALL: &str = "\
SELECT row_to_json(r)::text FROM (\
  SELECT name, description, controller, state \
  FROM iris.alarm \
  ORDER BY description\
) r";

/// SQL query for beacon states (LUT)
pub const BEACON_STATE_LUT: &str = "\
SELECT row_to_json(r)::text FROM (\
  SELECT id, description \
  FROM iris.beacon_state \
  ORDER BY id\
) r";

/// SQL query for all beacons (minimal)
pub const BEACON_ALL: &str = "\
SELECT row_to_json(r)::text FROM (\
  SELECT b.name, location, controller, message, notes, state \
  FROM iris.beacon b \
  LEFT JOIN geo_loc_view gl ON b.geo_loc = gl.name \
  ORDER BY name\
) r";

/// SQL query for all cabinet styles (minimal)
pub const CABINET_STYLE_ALL: &str = "\
SELECT row_to_json(r)::text FROM (\
  SELECT name \
  FROM iris.cabinet_style \
  ORDER BY name\
) r";

/// SQL query for all cameras (minimal)
pub const CAMERA_ALL: &str = "\
SELECT row_to_json(r)::text FROM (\
  SELECT c.name, location, controller, notes, cam_num, publish \
  FROM iris.camera c \
  LEFT JOIN geo_loc_view gl ON c.geo_loc = gl.name \
  ORDER BY cam_num, c.name\
) r";

/// SQL query for all cameras (public)
pub const CAMERA_PUB: &str = "\
SELECT row_to_json(r)::text FROM (\
  SELECT name, publish, streamable, roadway, road_dir, cross_street, \
         location, lat, lon, ARRAY(\
           SELECT view_num \
           FROM iris.encoder_stream \
           WHERE encoder_type = c.encoder_type \
           AND view_num IS NOT NULL \
           ORDER BY view_num\
         ) AS views \
  FROM camera_view c \
  ORDER BY name\
) r";

/// SQL query for all comm configs (minimal)
pub const COMM_CONFIG_ALL: &str = "\
SELECT row_to_json(r)::text FROM (\
  SELECT name, description \
  FROM iris.comm_config \
  ORDER BY description\
) r";

/// SQL query for all comm links (minimal)
pub const COMM_LINK_ALL: &str = "\
SELECT row_to_json(r)::text FROM (\
  SELECT name, description, uri, comm_config, poll_enabled, connected \
  FROM iris.comm_link \
  ORDER BY regexp_replace(name, '[0-9]', '', 'g'), \
          (regexp_replace(name, '[^0-9]', '', 'g') || '0')::INTEGER\
) r";

/// SQL query for comm protocols (LUT)
pub const COMM_PROTOCOL_LUT: &str = "\
SELECT row_to_json(r)::text FROM (\
  SELECT id, description \
  FROM iris.comm_protocol \
  ORDER BY description\
) r";

/// SQL query for controller conditions (LUT)
pub const CONDITION_LUT: &str = "\
SELECT row_to_json(r)::text FROM (\
  SELECT id, description \
  FROM iris.condition \
  ORDER BY description\
) r";

/// SQL query for all controllers (minimal)
pub const CONTROLLER_ALL: &str = "\
SELECT row_to_json(r)::text FROM (\
  SELECT c.name, location, comm_link, drop_id, cabinet_style, condition, \
         notes, setup, fail_time \
  FROM iris.controller c \
  LEFT JOIN geo_loc_view gl ON c.geo_loc = gl.name \
  ORDER BY COALESCE(regexp_replace(comm_link, '[0-9]', '', 'g'), ''), \
          (regexp_replace(comm_link, '[^0-9]', '', 'g') || '0')::INTEGER, \
           drop_id\
) r";

/// SQL query for all detectors (minimal)
pub const DETECTOR_ALL: &str = "\
SELECT row_to_json(r)::text FROM (\
  SELECT name, label, controller, notes \
  FROM detector_view \
  ORDER BY regexp_replace(name, '[0-9]', '', 'g'), \
          (regexp_replace(name, '[^0-9]', '', 'g') || '0')::INTEGER\
) r";

/// SQL query for all detectors (public)
pub const DETECTOR_PUB: &str = "\
SELECT row_to_json(r)::text FROM (\
  SELECT name, r_node, cor_id, lane_number, lane_code, field_length \
  FROM detector_view\
) r";

/// SQL query for directions (LUT)
pub const DIRECTION_LUT: &str = "\
SELECT row_to_json(r)::text FROM (\
  SELECT id, direction, dir \
  FROM iris.direction \
  ORDER BY id\
) r";

/// SQL query for all DMS (minimal)
pub const DMS_ALL: &str = "\
SELECT json_strip_nulls(row_to_json(r))::text FROM (\
  SELECT d.name, location, msg_current, \
         NULLIF(char_length(status->>'faults') > 0, false) AS has_faults, \
         NULLIF(notes, '') AS notes, hashtags, controller \
  FROM iris.dms d \
  LEFT JOIN geo_loc_view gl ON d.geo_loc = gl.name \
  LEFT JOIN (\
    SELECT dms, string_agg(hashtag, ' ' ORDER BY hashtag) AS hashtags \
    FROM iris.dms_hashtag \
    GROUP BY dms\
  ) h ON d.name = h.dms \
  ORDER BY d.name\
) r";

/// SQL query for all DMS (public)
pub const DMS_PUB: &str = "\
SELECT row_to_json(r)::text FROM (\
  SELECT name, sign_config, sign_detail, roadway, road_dir, \
         cross_street, location, lat, lon \
  FROM dms_view \
  ORDER BY name\
) r";

/// SQL query for all DMS status (public)
///
/// NOTE: the `sources` attribute is deprecated,
///       but required by external systems (for now)
pub const DMS_STATUS: &str = "\
SELECT row_to_json(r)::text FROM (\
  SELECT name, msg_current, \
         replace(substring(msg_owner FROM 'IRIS; ([^;]*).*'), '+', ', ') \
         AS sources, failed, duration, expire_time \
  FROM dms_message_view WHERE condition = 'Active' \
  ORDER BY name\
) r";

/// SQL query for all flow streams (minimal)
pub const FLOW_STREAM_ALL: &str = "\
SELECT row_to_json(r)::text FROM (\
  SELECT name, controller \
  FROM iris.flow_stream \
  ORDER BY name\
) r";

/// SQL query for all fonts (minimal)
pub const FONT_ALL: &str = "\
SELECT row_to_json(r)::text FROM (\
  SELECT f_number AS font_number, name \
  FROM iris.font ORDER BY f_number\
) r";

/// SQL query for all gate arms (minimal)
pub const GATE_ARM_ALL: &str = "\
SELECT row_to_json(r)::text FROM (\
  SELECT g.name, location, g.controller, g.notes, g.arm_state \
  FROM iris.gate_arm g \
  LEFT JOIN iris.gate_arm_array ga ON g.ga_array = ga.name \
  LEFT JOIN geo_loc_view gl ON ga.geo_loc = gl.name \
  ORDER BY name\
) r";

/// SQL query for all gate arm arrays (minimal)
pub const GATE_ARM_ARRAY_ALL: &str = "\
SELECT row_to_json(r)::text FROM (\
  SELECT ga.name, location, notes, arm_state, interlock \
  FROM iris.gate_arm_array ga \
  LEFT JOIN geo_loc_view gl ON ga.geo_loc = gl.name \
  ORDER BY ga.name\
) r";

/// SQL query for gate arm interlocks (LUT)
pub const GATE_ARM_INTERLOCK_LUT: &str = "\
SELECT row_to_json(r)::text FROM (\
  SELECT id, description \
  FROM iris.gate_arm_interlock \
  ORDER BY id\
) r";

/// SQL query for gate arm states (LUT)
pub const GATE_ARM_STATE_LUT: &str = "\
SELECT row_to_json(r)::text FROM (\
  SELECT id, description \
  FROM iris.gate_arm_state \
  ORDER BY id\
) r";

/// SQL query for all GPS (minimal)
pub const GPS_ALL: &str = "\
SELECT row_to_json(r)::text FROM (\
  SELECT name, controller, notes \
  FROM iris.gps \
  ORDER BY name\
) r";

/// SQL query for all graphics (minimal)
pub const GRAPHIC_ALL: &str = "\
SELECT row_to_json(r)::text FROM (\
  SELECT g_number AS number, 'G' || g_number AS name \
  FROM iris.graphic \
  WHERE g_number < 256 \
  ORDER BY number\
) r";

/// SQL query for all active incidents (public)
pub const INCIDENT_PUB: &str = "\
SELECT row_to_json(r)::text FROM (\
  SELECT name, event_date, description, road, direction, lane_type, \
         impact, confirmed, camera, detail, replaces, lat, lon \
  FROM incident_view \
  WHERE cleared = false\
) r";

/// SQL query for all lane markings (minimal)
pub const LANE_MARKING_ALL: &str = "\
SELECT row_to_json(r)::text FROM (\
  SELECT m.name, location, controller, notes, deployed \
  FROM iris.lane_marking m \
  LEFT JOIN geo_loc_view gl ON m.geo_loc = gl.name \
  ORDER BY name\
) r";

/// SQL query for lane use indications (LUT)
pub const LANE_USE_INDICATION_LUT: &str = "\
SELECT row_to_json(r)::text FROM (\
  SELECT id, description \
  FROM iris.lane_use_indication \
  ORDER BY id\
) r";

/// SQL query for all LCS arrays (minimal)
pub const LCS_ARRAY_ALL: &str = "\
SELECT row_to_json(r)::text FROM (\
  SELECT name, notes, lcs_lock \
  FROM iris.lcs_array \
  ORDER BY name\
) r";

/// SQL query for all LCS indications (minimal)
pub const LCS_INDICATION_ALL: &str = "\
SELECT row_to_json(r)::text FROM (\
  SELECT name, controller, lcs, indication \
  FROM iris.lcs_indication \
  ORDER BY name\
) r";

/// SQL query for LCS locks (LUT)
pub const LCS_LOCK_LUT: &str = "\
SELECT row_to_json(r)::text FROM (\
  SELECT id, description \
  FROM iris.lcs_lock \
  ORDER BY id\
) r";

/// SQL query for all modems (minimal)
pub const MODEM_ALL: &str = "\
SELECT row_to_json(r)::text FROM (\
  SELECT name, enabled \
  FROM iris.modem \
  ORDER BY name\
) r";

/// SQL query for all message lines (minimal)
pub const MSG_LINE_ALL: &str = "\
SELECT json_strip_nulls(row_to_json(r))::text FROM (\
  SELECT name, msg_pattern, line, multi, restrict_hashtag \
  FROM iris.msg_line \
  ORDER BY msg_pattern, line, rank, multi, restrict_hashtag\
) r";

/// SQL query for all message patterns (minimal)
pub const MSG_PATTERN_ALL: &str = "\
SELECT json_strip_nulls(row_to_json(r))::text FROM (\
  SELECT name, multi, compose_hashtag \
  FROM iris.msg_pattern \
  ORDER BY name\
) r";

/// SQL query for all parking areas (public)
pub const PARKING_AREA_PUB: &str = "\
SELECT row_to_json(r)::text FROM (\
  SELECT site_id AS \"siteId\", \
         to_char(time_stamp_static AT TIME ZONE 'UTC', \
                 'YYYY-mm-dd\"T\"HH24:MI:SSZ') AS \"timeStamp\", \
         relevant_highway AS \"relevantHighway\", \
         reference_post AS \"referencePost\", exit_id AS \"exitID\", \
         road_dir AS \"directionOfTravel\", facility_name AS name, \
         json_build_object('latitude', lat, 'longitude', lon, \
         'streetAdr', street_adr, 'city', city, 'state', state, \
         'zip', zip, 'timeZone', time_zone) AS location, \
         ownership, capacity, \
         string_to_array(amenities, ', ') AS amenities, \
         array_remove(ARRAY[camera_image_base_url || camera_1, \
         camera_image_base_url || camera_2, \
         camera_image_base_url || camera_3], NULL) AS images, \
         ARRAY[]::text[] AS logos \
  FROM parking_area_view\
) r";

/// SQL query for all parking areas (dynamic)
pub const PARKING_AREA_DYN: &str = "\
SELECT row_to_json(r)::text FROM (\
  SELECT site_id AS \"siteId\", \
         to_char(time_stamp AT TIME ZONE 'UTC', \
                 'YYYY-mm-dd\"T\"HH24:MI:SSZ') AS \"timeStamp\", \
         to_char(time_stamp_static AT TIME ZONE 'UTC', \
                 'YYYY-mm-dd\"T\"HH24:MI:SSZ') AS \"timeStampStatic\", \
         reported_available AS \"reportedAvailable\", \
         trend, open, trust_data AS \"trustData\", capacity \
  FROM parking_area_view\
) r";

/// SQL query for all parking areas (archive)
pub const PARKING_AREA_ARCH: &str = "\
SELECT row_to_json(r)::text FROM (\
  SELECT site_id AS \"siteId\",
         to_char(time_stamp AT TIME ZONE 'UTC', \
                 'YYYY-mm-dd\"T\"HH24:MI:SSZ') AS \"timeStamp\", \
         to_char(time_stamp_static AT TIME ZONE 'UTC', \
                 'YYYY-mm-dd\"T\"HH24:MI:SSZ') AS \"timeStampStatic\", \
         reported_available AS \"reportedAvailable\", \
         trend, open, trust_data AS \"trustData\", capacity, \
         last_verification_check AS \"lastVerificationCheck\", \
         verification_check_amplitude AS \"verificationCheckAmplitude\", \
         low_threshold AS \"lowThreshold\", \
         true_available AS \"trueAvailable\" \
  FROM parking_area_view\
) r";

/// SQL query for all permissions (minimal)
pub const PERMISSION_ALL: &str = "\
SELECT row_to_json(r)::text FROM (\
  SELECT id, role, resource_n, hashtag, access_n \
  FROM iris.permission \
  ORDER BY role, resource_n, id\
) r";

/// SQL query for all ramp meters (minimal)
pub const RAMP_METER_ALL: &str = "\
SELECT row_to_json(r)::text FROM (\
  SELECT m.name, location, controller, notes \
  FROM iris.ramp_meter m \
  LEFT JOIN geo_loc_view gl ON m.geo_loc = gl.name \
  ORDER BY m.name\
) r";

/// SQL query for resource types (LUT)
pub const RESOURCE_TYPE_LUT: &str = "\
SELECT to_json(r.name)::text FROM (\
  SELECT name \
  FROM iris.resource_type \
  ORDER BY name\
) r";

/// SQL query for all roads (minimal)
pub const ROAD_ALL: &str = "\
SELECT row_to_json(r)::text FROM (\
  SELECT name, abbrev, r_class, direction \
  FROM iris.road \
  ORDER BY name\
) r";

/// SQL query for road modifiers (LUT)
pub const ROAD_MODIFIER_LUT: &str = "\
SELECT row_to_json(r)::text FROM (\
  SELECT id, modifier, mod AS md \
  FROM iris.road_modifier \
  ORDER BY id\
) r";

/// SQL query for all roles (minimal)
pub const ROLE_ALL: &str = "\
SELECT row_to_json(r)::text FROM (\
  SELECT name, enabled \
  FROM iris.role \
  ORDER BY name\
) r";

/// SQL query for all sign configs (minimal)
pub const SIGN_CONFIG_ALL: &str = "\
SELECT json_strip_nulls(row_to_json(r))::text FROM (\
  SELECT name, face_width, face_height, border_horiz, border_vert, \
         pitch_horiz, pitch_vert, pixel_width, pixel_height, \
         char_width, char_height, monochrome_foreground, \
         monochrome_background, color_scheme, default_font, \
         module_width, module_height \
  FROM sign_config_view\
) r";

/// SQL query for all sign details (minimal)
pub const SIGN_DETAIL_ALL: &str = "\
SELECT row_to_json(r)::text FROM (\
  SELECT name, dms_type, portable, technology, sign_access, legend, \
         beacon_type, hardware_make, hardware_model, software_make, \
         software_model, supported_tags, max_pages, max_multi_len, \
         beacon_activation_flag, pixel_service_flag \
  FROM sign_detail_view\
) r";

/// SQL query for all sign messages (public)
pub const SIGN_MSG_PUB: &str = "\
SELECT row_to_json(r)::text FROM (\
  SELECT name, sign_config, incident, multi, msg_owner, flash_beacon, \
         msg_priority, duration \
  FROM sign_message_view \
  ORDER BY name\
) r";

/// SQL query for system attributes (public)
pub const SYSTEM_ATTRIBUTE_PUB: &str = "\
SELECT jsonb_object_agg(name, value)::text \
  FROM iris.system_attribute \
  WHERE name LIKE 'dms\\_%' OR name LIKE 'map\\_%'";

/// SQL query for all tag readers (minimal)
pub const TAG_READER_ALL: &str = "\
SELECT row_to_json(r)::text FROM (\
  SELECT t.name, location, controller, notes \
  FROM iris.tag_reader t \
  LEFT JOIN geo_loc_view gl ON t.geo_loc = gl.name \
  ORDER BY t.name\
) r";

/// SQL query for all users (minimal)
pub const USER_ALL: &str = "\
SELECT row_to_json(r)::text FROM (\
  SELECT name, full_name, role, enabled \
  FROM iris.i_user \
  ORDER BY name\
) r";

/// SQL query for all video monitors (minimal)
pub const VIDEO_MONITOR_ALL: &str = "\
SELECT row_to_json(r)::text FROM (\
  SELECT name, mon_num, controller, notes \
  FROM iris.video_monitor \
  ORDER BY mon_num, name\
) r";

/// SQL query for all weather sensors (minimal)
pub const WEATHER_SENSOR_ALL: &str = "\
SELECT row_to_json(r)::text FROM (\
  SELECT ws.name, site_id, alt_id, location, controller, notes \
  FROM iris.weather_sensor ws \
  LEFT JOIN geo_loc_view gl ON ws.geo_loc = gl.name \
  ORDER BY name\
) r";

/// SQL query for all weather sensors (public)
pub const WEATHER_SENSOR_PUB: &str = "\
SELECT row_to_json(r)::text FROM (\
  SELECT ws.name, location, lat, lon, settings, sample, sample_time \
  FROM iris.weather_sensor ws \
  LEFT JOIN geo_loc_view gl ON ws.geo_loc = gl.name \
  ORDER BY name\
) r";

/// SQL query for all words (minimal)
pub const WORD_ALL: &str = "\
SELECT json_strip_nulls(row_to_json(r))::text FROM (\
  SELECT name, abbr, allowed \
  FROM iris.word \
  ORDER BY name\
) r";
