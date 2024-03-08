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
  SELECT name, description, controller, state \
  FROM iris.alarm \
  ORDER BY description";

/// SQL query for one alarm (full)
pub const ALARM_ONE: &str = "\
  SELECT name, description, controller, pin, state, trigger_time \
  FROM iris.alarm \
  WHERE name = $1";

/// SQL query for all beacons (minimal)
pub const BEACON_ALL: &str = "\
  SELECT b.name, location, controller, message, notes, state \
  FROM iris.beacon b \
  LEFT JOIN geo_loc_view gl ON b.geo_loc = gl.name \
  ORDER BY name";

/// SQL query for one beacon (full)
pub const BEACON_ONE: &str = "\
  SELECT b.name, location, controller, pin, verify_pin, ext_mode, geo_loc, \
         message, notes, preset, state \
  FROM iris.beacon b \
  LEFT JOIN geo_loc_view gl ON b.geo_loc = gl.name \
  WHERE b.name = $1";

/// SQL query for beacon states (LUT)
pub const BEACON_STATE_LUT: &str = "\
  SELECT id, description \
  FROM iris.beacon_state \
  ORDER BY id";

/// SQL query for all cabinet styles (minimal)
pub const CABINET_STYLE_ALL: &str = "\
  SELECT name \
  FROM iris.cabinet_style \
  ORDER BY name";

/// SQL query for one cabinet style (full)
pub const CABINET_STYLE_ONE: &str = "\
  SELECT name, police_panel_pin_1, police_panel_pin_2, watchdog_reset_pin_1, \
         watchdog_reset_pin_2, dip \
  FROM iris.cabinet_style \
  WHERE name = $1";

/// SQL query for all cameras (minimal)
pub const CAMERA_ALL: &str = "\
  SELECT c.name, location, controller, notes, cam_num, publish \
  FROM iris.camera c \
  LEFT JOIN geo_loc_view gl ON c.geo_loc = gl.name \
  ORDER BY cam_num, c.name";

/// SQL query for one camera (full)
pub const CAMERA_ONE: &str = "\
  SELECT c.name, location, geo_loc, controller, pin, notes, cam_num, publish, \
         streamable, cam_template, encoder_type, enc_address, enc_port, \
         enc_mcast, enc_channel, video_loss \
  FROM iris.camera c \
  LEFT JOIN geo_loc_view gl ON c.geo_loc = gl.name \
  WHERE c.name = $1";

/// SQL query for all cameras (public)
pub const CAMERA_PUB: &str = "\
  SELECT name, publish, streamable, roadway, road_dir, cross_street, \
         location, lat, lon, ARRAY(\
           SELECT view_num \
           FROM iris.encoder_stream \
           WHERE encoder_type = c.encoder_type \
           AND view_num IS NOT NULL \
           ORDER BY view_num\
         ) AS views \
  FROM camera_view c \
  ORDER BY name";

/// SQL query for all comm configs (minimal)
pub const COMM_CONFIG_ALL: &str = "\
  SELECT name, description \
  FROM iris.comm_config \
  ORDER BY description";

/// SQL query for one comm config (full)
pub const COMM_CONFIG_ONE: &str = "\
  SELECT name, description, protocol, poll_period_sec, long_poll_period_sec, \
         timeout_ms, idle_disconnect_sec, no_response_disconnect_sec \
  FROM iris.comm_config \
  WHERE name = $1";

/// SQL query for all comm links (minimal)
pub const COMM_LINK_ALL: &str = "\
  SELECT name, description, uri, comm_config, poll_enabled, connected \
  FROM iris.comm_link \
  ORDER BY regexp_replace(name, '[0-9]', '', 'g'), \
          (regexp_replace(name, '[^0-9]', '', 'g') || '0')::INTEGER";

/// SQL query for one comm link (full)
pub const COMM_LINK_ONE: &str = "\
  SELECT name, description, uri, comm_config, poll_enabled, connected \
  FROM iris.comm_link \
  WHERE name = $1";

/// SQL query for comm protocols (LUT)
pub const COMM_PROTOCOL_LUT: &str = "\
  SELECT id, description \
  FROM iris.comm_protocol \
  ORDER BY description";

/// SQL query for controller conditions (LUT)
pub const CONDITION_LUT: &str = "\
  SELECT id, description \
  FROM iris.condition \
  ORDER BY description";

/// SQL query for all controllers (minimal)
pub const CONTROLLER_ALL: &str = "\
  SELECT c.name, location, comm_link, drop_id, cabinet_style, condition, \
         notes, setup, fail_time \
  FROM iris.controller c \
  LEFT JOIN geo_loc_view gl ON c.geo_loc = gl.name \
  ORDER BY COALESCE(regexp_replace(comm_link, '[0-9]', '', 'g'), ''), \
          (regexp_replace(comm_link, '[^0-9]', '', 'g') || '0')::INTEGER, \
           drop_id";

/// SQL query for one controller (full)
pub const CONTROLLER_ONE: &str = "\
  SELECT c.name, location, geo_loc, comm_link, drop_id, cabinet_style, \
         condition, notes, password, setup, fail_time \
  FROM iris.controller c \
  LEFT JOIN geo_loc_view gl ON c.geo_loc = gl.name \
  WHERE c.name = $1";

/// SQL query for all IO on one controller
pub const CONTROLLER_IO_ONE: &str = "\
  SELECT pin, name, resource_n \
  FROM iris.controller_io \
  WHERE controller = $1 \
  ORDER BY pin";

/// SQL query for all detectors (minimal)
pub const DETECTOR_ALL: &str = "\
  SELECT name, label, controller, notes \
  FROM detector_view \
  ORDER BY regexp_replace(name, '[0-9]', '', 'g'), \
          (regexp_replace(name, '[^0-9]', '', 'g') || '0')::INTEGER";

/// SQL query for one detector (full)
pub const DETECTOR_ONE: &str = "\
  SELECT d.name, label, r_node, controller, pin, notes, lane_code, \
         lane_number, abandoned, fake, field_length, force_fail, auto_fail \
  FROM iris.detector d \
  LEFT JOIN detector_label_view dl ON d.name = dl.det_id \
  WHERE d.name = $1";

/// SQL query for all detectors (public)
pub const DETECTOR_PUB: &str = "\
  SELECT name, r_node, cor_id, lane_number, lane_code, field_length \
  FROM detector_view";

/// SQL query for directions (LUT)
pub const DIRECTION_LUT: &str = "\
  SELECT id, direction, dir \
  FROM iris.direction \
  ORDER BY id";

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

/// SQL query for one DMS (full)
pub const DMS_ONE: &str = "\
  SELECT d.name, location, geo_loc, controller, pin, notes, hashtags, \
         sign_config, sign_detail, status, \
         char_length(status->>'faults') > 0 AS has_faults, \
         msg_user, msg_sched, msg_current, expire_time, stuck_pixels \
  FROM iris.dms d \
  LEFT JOIN geo_loc_view gl ON d.geo_loc = gl.name \
  LEFT JOIN (\
    SELECT dms, string_agg(hashtag, ' ' ORDER BY hashtag) AS hashtags \
    FROM iris.dms_hashtag \
    GROUP BY dms\
  ) h ON d.name = h.dms \
  WHERE d.name = $1";

/// SQL query for all DMS (public)
pub const DMS_PUB: &str = "\
  SELECT name, sign_config, sign_detail, roadway, road_dir, \
         cross_street, location, lat, lon \
  FROM dms_view \
  ORDER BY name";

/// SQL query for all DMS status (public)
///
/// NOTE: the `sources` attribute is deprecated,
///       but required by external systems (for now)
pub const DMS_STATUS: &str = "\
  SELECT name, msg_current, \
         replace(substring(msg_owner FROM 'IRIS; ([^;]*).*'), '+', ', ') \
         AS sources, failed, duration, expire_time \
  FROM dms_message_view WHERE condition = 'Active' \
  ORDER BY name";

/// SQL query for all flow streams (minimal)
pub const FLOW_STREAM_ALL: &str = "\
  SELECT name, controller \
  FROM iris.flow_stream \
  ORDER BY name";

/// SQL query for one flow stream (full)
pub const FLOW_STREAM_ONE: &str = "\
  SELECT name, controller, pin, restricted, loc_overlay, quality, camera, \
         mon_num, address, port, status \
  FROM iris.flow_stream \
  WHERE name = $1";

/// SQL query for all fonts (minimal)
pub const FONT_ALL: &str = "\
  SELECT f_number AS font_number, name \
  FROM iris.font \
  ORDER BY f_number";

/// SQL query for one font (full)
pub const FONT_ONE: &str = "\
  SELECT f_number AS font_number, name \
  FROM iris.font \
  WHERE name = $1";

/// SQL query for all gate arms (minimal)
pub const GATE_ARM_ALL: &str = "\
  SELECT g.name, location, g.controller, g.notes, g.arm_state \
  FROM iris.gate_arm g \
  LEFT JOIN iris.gate_arm_array ga ON g.ga_array = ga.name \
  LEFT JOIN geo_loc_view gl ON ga.geo_loc = gl.name \
  ORDER BY name";

/// SQL query for one gate arm (full)
pub const GATE_ARM_ONE: &str = "\
  SELECT g.name, location, g.ga_array, g.idx, g.controller, g.pin, g.notes, \
         g.arm_state, g.fault \
  FROM iris.gate_arm g \
  LEFT JOIN iris.gate_arm_array ga ON g.ga_array = ga.name \
  LEFT JOIN geo_loc_view gl ON ga.geo_loc = gl.name \
  WHERE g.name = $1";

/// SQL query for all gate arm arrays (minimal)
pub const GATE_ARM_ARRAY_ALL: &str = "\
  SELECT ga.name, location, notes, arm_state, interlock \
  FROM iris.gate_arm_array ga \
  LEFT JOIN geo_loc_view gl ON ga.geo_loc = gl.name \
  ORDER BY ga.name";

/// SQL query for one gate arm array (full)
pub const GATE_ARM_ARRAY_ONE: &str = "\
  SELECT ga.name, location, geo_loc, notes, opposing, prereq, camera, \
         approach, action_plan, arm_state, interlock \
  FROM iris.gate_arm_array ga \
  LEFT JOIN geo_loc_view gl ON ga.geo_loc = gl.name \
  WHERE ga.name = $1";

/// SQL query for gate arm interlocks (LUT)
pub const GATE_ARM_INTERLOCK_LUT: &str = "\
  SELECT id, description \
  FROM iris.gate_arm_interlock \
  ORDER BY id";

/// SQL query for gate arm states (LUT)
pub const GATE_ARM_STATE_LUT: &str = "\
  SELECT id, description \
  FROM iris.gate_arm_state \
  ORDER BY id";

/// SQL query for one geo location (full)
pub const GEO_LOC_ONE: &str = "\
  SELECT name, resource_n, roadway, road_dir, cross_street, cross_dir, \
         cross_mod, landmark, lat, lon \
  FROM iris.geo_loc \
  WHERE name = $1";

/// SQL query for all GPS (minimal)
pub const GPS_ALL: &str = "\
  SELECT name, controller, notes \
  FROM iris.gps \
  ORDER BY name";

/// SQL query for one GPS (full)
pub const GPS_ONE: &str = "\
  SELECT name, controller, pin, notes, latest_poll, latest_sample, lat, lon \
  FROM iris.gps \
  WHERE name = $1";

/// SQL query for all graphics (minimal)
pub const GRAPHIC_ALL: &str = "\
  SELECT g_number AS number, 'G' || g_number AS name \
  FROM iris.graphic \
  WHERE g_number < 256 \
  ORDER BY number";

/// SQL query for one graphic (full)
pub const GRAPHIC_ONE: &str = "\
  SELECT g_number AS number, 'G' || g_number AS name \
  FROM iris.graphic \
  WHERE name = $1";

/// SQL query for all active incidents (public)
pub const INCIDENT_PUB: &str = "\
  SELECT name, event_date, description, road, direction, lane_type, \
         impact, confirmed, camera, detail, replaces, lat, lon \
  FROM incident_view \
  WHERE cleared = false";

/// SQL query for all lane markings (minimal)
pub const LANE_MARKING_ALL: &str = "\
  SELECT m.name, location, controller, notes, deployed \
  FROM iris.lane_marking m \
  LEFT JOIN geo_loc_view gl ON m.geo_loc = gl.name \
  ORDER BY name";

/// SQL query for one lane marking (full)
pub const LANE_MARKING_ONE: &str = "\
  SELECT m.name, location, geo_loc, controller, pin, notes, deployed \
  FROM iris.lane_marking m \
  LEFT JOIN geo_loc_view gl ON m.geo_loc = gl.name \
  WHERE m.name = $1";

/// SQL query for lane use indications (LUT)
pub const LANE_USE_INDICATION_LUT: &str = "\
  SELECT id, description \
  FROM iris.lane_use_indication \
  ORDER BY id";

/// SQL query for all LCS arrays (minimal)
pub const LCS_ARRAY_ALL: &str = "\
  SELECT name, notes, lcs_lock \
  FROM iris.lcs_array \
  ORDER BY name";

/// SQL query for one LCS array (full)
pub const LCS_ARRAY_ONE: &str = "\
  SELECT name, notes, shift, lcs_lock \
  FROM iris.lcs_array \
  WHERE name = $1";

/// SQL query for all LCS indications (minimal)
pub const LCS_INDICATION_ALL: &str = "\
  SELECT name, controller, lcs, indication \
  FROM iris.lcs_indication \
  ORDER BY name";

/// SQL query for one LCS indication (full)
pub const LCS_INDICATION_ONE: &str = "\
  SELECT name, controller, pin, lcs, indication \
  FROM iris.lcs_indication \
  WHERE name = $1";

/// SQL query for LCS locks (LUT)
pub const LCS_LOCK_LUT: &str = "\
  SELECT id, description \
  FROM iris.lcs_lock \
  ORDER BY id";

/// SQL query for all modems (minimal)
pub const MODEM_ALL: &str = "\
  SELECT name, enabled \
  FROM iris.modem \
  ORDER BY name";

/// SQL query for one modem (full)
pub const MODEM_ONE: &str = "\
  SELECT name, uri, config, enabled, timeout_ms \
  FROM iris.modem \
  WHERE name = $1";

/// SQL query for all message lines (minimal)
pub const MSG_LINE_ALL: &str = "\
SELECT json_strip_nulls(row_to_json(r))::text FROM (\
  SELECT name, msg_pattern, line, multi, restrict_hashtag \
  FROM iris.msg_line \
  ORDER BY msg_pattern, line, rank, multi, restrict_hashtag\
) r";

/// SQL query for one message line (full)
pub const MSG_LINE_ONE: &str = "\
  SELECT name, msg_pattern, line, multi, rank, restrict_hashtag \
  FROM iris.msg_line \
  WHERE name = $1";

/// SQL query for all message patterns (minimal)
pub const MSG_PATTERN_ALL: &str = "\
SELECT json_strip_nulls(row_to_json(r))::text FROM (\
  SELECT name, multi, compose_hashtag \
  FROM iris.msg_pattern \
  ORDER BY name\
) r";

/// SQL query for one message pattern (full)
pub const MSG_PATTERN_ONE: &str = "\
  SELECT name, multi, flash_beacon, compose_hashtag \
  FROM iris.msg_pattern \
  WHERE name = $1";

/// SQL query for all parking areas (public)
pub const PARKING_AREA_PUB: &str = "\
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
  FROM parking_area_view";

/// SQL query for all parking areas (dynamic)
pub const PARKING_AREA_DYN: &str = "\
  SELECT site_id AS \"siteId\", \
         to_char(time_stamp AT TIME ZONE 'UTC', \
                 'YYYY-mm-dd\"T\"HH24:MI:SSZ') AS \"timeStamp\", \
         to_char(time_stamp_static AT TIME ZONE 'UTC', \
                 'YYYY-mm-dd\"T\"HH24:MI:SSZ') AS \"timeStampStatic\", \
         reported_available AS \"reportedAvailable\", \
         trend, open, trust_data AS \"trustData\", capacity \
  FROM parking_area_view";

/// SQL query for all parking areas (archive)
pub const PARKING_AREA_ARCH: &str = "\
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
  FROM parking_area_view";

/// SQL query for all permissions (minimal)
pub const PERMISSION_ALL: &str = "\
  SELECT id, role, resource_n, hashtag, access_n \
  FROM iris.permission \
  ORDER BY role, resource_n, id";

/// SQL query for one permission (full)
pub const PERMISSION_ONE: &str = "\
  SELECT id, role, resource_n, hashtag, access_n \
  FROM iris.permission \
  WHERE id = $1";

/// SQL query for all ramp meters (minimal)
pub const RAMP_METER_ALL: &str = "\
  SELECT m.name, location, controller, notes \
  FROM iris.ramp_meter m \
  LEFT JOIN geo_loc_view gl ON m.geo_loc = gl.name \
  ORDER BY m.name";

/// SQL query for one ramp meter (full)
pub const RAMP_METER_ONE: &str = "\
  SELECT m.name, location, geo_loc, controller, pin, notes, meter_type, \
         beacon, preset, storage, max_wait, algorithm, am_target, pm_target, \
         m_lock \
  FROM iris.ramp_meter m \
  LEFT JOIN geo_loc_view gl ON m.geo_loc = gl.name \
  WHERE m.name = $1";

/// SQL query for resource types (LUT)
pub const RESOURCE_TYPE_LUT: &str = "\
SELECT to_json(r.name)::text FROM (\
  SELECT name \
  FROM iris.resource_type \
  ORDER BY name\
) r";

/// SQL query for all Rnodes (full)
pub const RNODE_FULL: &str = "\
  SELECT name, roadway, road_dir, location, lat, lon, transition, \
         lanes, shift, active, station_id, speed_limit \
  FROM r_node_view";

/// SQL query for one RNode (full)
pub const RNODE_ONE: &str = "\
  SELECT name, roadway, road_dir, location, lat, lon, transition, \
         lanes, shift, active, station_id, speed_limit \
  FROM r_node_view n \
  WHERE n.name = $1";

/// SQL query for all roads (minimal)
pub const ROAD_ALL: &str = "\
  SELECT name, abbrev, r_class, direction \
  FROM iris.road \
  ORDER BY name";

/// SQL query for all roads (full)
pub const ROAD_FULL: &str = "\
  SELECT name, abbrev, r_class, direction, scale \
  FROM iris.road \
  JOIN iris.road_class ON r_class = id";

/// SQL query for one road (full)
pub const ROAD_ONE: &str = "\
  SELECT name, abbrev, r_class, direction, scale \
  FROM iris.road \
  JOIN iris.road_class ON r_class = id \
  WHERE name = $1";

/// SQL query for road modifiers (LUT)
pub const ROAD_MODIFIER_LUT: &str = "\
  SELECT id, modifier, mod AS md \
  FROM iris.road_modifier \
  ORDER BY id";

/// SQL query for all roles (minimal)
pub const ROLE_ALL: &str = "\
  SELECT name, enabled \
  FROM iris.role \
  ORDER BY name";

/// SQL query for one role (full)
pub const ROLE_ONE: &str = "\
  SELECT name, enabled \
  FROM iris.role \
  WHERE name = $1";

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

/// SQL query for one sign configuration (full)
pub const SIGN_CONFIG_ONE: &str = "\
  SELECT name, face_width, face_height, border_horiz, border_vert, \
         pitch_horiz, pitch_vert, pixel_width, pixel_height, \
         char_width, char_height, monochrome_foreground, \
         monochrome_background, color_scheme, default_font, \
         module_width, module_height \
  FROM iris.sign_config \
  WHERE name = $1";

/// SQL query for all sign details (minimal)
pub const SIGN_DETAIL_ALL: &str = "\
  SELECT name, dms_type, portable, technology, sign_access, legend, \
         beacon_type, hardware_make, hardware_model, software_make, \
         software_model, supported_tags, max_pages, max_multi_len, \
         beacon_activation_flag, pixel_service_flag \
  FROM sign_detail_view";

/// SQL query for one sign detail (full)
pub const SIGN_DETAIL_ONE: &str = "\
  SELECT name, dms_type, portable, technology, sign_access, legend, \
         beacon_type, hardware_make, hardware_model, software_make, \
         software_model, supported_tags, max_pages, max_multi_len, \
         beacon_activation_flag, pixel_service_flag \
  FROM iris.sign_detail \
  WHERE name = $1";

/// SQL query for all sign messages (public)
pub const SIGN_MSG_PUB: &str = "\
  SELECT name, sign_config, incident, multi, msg_owner, flash_beacon, \
         msg_priority, duration \
  FROM sign_message_view \
  ORDER BY name";

/// SQL query for one sign message (full)
pub const SIGN_MSG_ONE: &str = "\
  SELECT name, sign_config, incident, multi, msg_owner, flash_beacon, \
         msg_priority, duration \
  FROM iris.sign_message \
  WHERE name = $1";

/// SQL query for system attributes (public)
pub const SYSTEM_ATTRIBUTE_PUB: &str = "\
SELECT jsonb_object_agg(name, value)::text \
  FROM iris.system_attribute \
  WHERE name LIKE 'dms\\_%' OR name LIKE 'map\\_%'";

/// SQL query for all tag readers (minimal)
pub const TAG_READER_ALL: &str = "\
  SELECT t.name, location, controller, notes \
  FROM iris.tag_reader t \
  LEFT JOIN geo_loc_view gl ON t.geo_loc = gl.name \
  ORDER BY t.name";

/// SQL query for one tag reader (full)
pub const TAG_READER_ONE: &str = "\
  SELECT t.name, location, geo_loc, controller, pin, notes, toll_zone, \
         settings \
  FROM iris.tag_reader t \
  LEFT JOIN geo_loc_view gl ON t.geo_loc = gl.name \
  WHERE t.name = $1";

/// SQL query for all users (minimal)
pub const USER_ALL: &str = "\
  SELECT name, full_name, role, enabled \
  FROM iris.i_user \
  ORDER BY name";

/// SQL query for one user (full)
pub const USER_ONE: &str = "\
  SELECT name, full_name, role, enabled \
  FROM iris.i_user \
  WHERE name = $1";

/// SQL query for all video monitors (minimal)
pub const VIDEO_MONITOR_ALL: &str = "\
  SELECT name, mon_num, controller, notes \
  FROM iris.video_monitor \
  ORDER BY mon_num, name";

/// SQL query for one video monitor (full)
pub const VIDEO_MONITOR_ONE: &str = "\
  SELECT name, mon_num, controller, pin, notes, restricted, monitor_style, \
         camera \
  FROM iris.video_monitor \
  WHERE name = $1";

/// SQL query for all weather sensors (minimal)
pub const WEATHER_SENSOR_ALL: &str = "\
  SELECT ws.name, site_id, alt_id, location, controller, notes \
  FROM iris.weather_sensor ws \
  LEFT JOIN geo_loc_view gl ON ws.geo_loc = gl.name \
  ORDER BY ws.name";

/// SQL query for one weather sensor (full)
pub const WEATHER_SENSOR_ONE: &str = "\
  SELECT ws.name, location, geo_loc, controller, pin, site_id, alt_id, notes, \
         settings, sample, sample_time \
  FROM iris.weather_sensor ws \
  LEFT JOIN geo_loc_view gl ON ws.geo_loc = gl.name \
  WHERE ws.name = $1";

/// SQL query for all weather sensors (public)
pub const WEATHER_SENSOR_PUB: &str = "\
  SELECT ws.name, location, lat, lon, settings, sample, sample_time \
  FROM iris.weather_sensor ws \
  LEFT JOIN geo_loc_view gl ON ws.geo_loc = gl.name \
  ORDER BY name";

/// SQL query for all words (minimal)
pub const WORD_ALL: &str = "\
SELECT json_strip_nulls(row_to_json(r))::text FROM (\
  SELECT name, abbr, allowed \
  FROM iris.word \
  ORDER BY name\
) r";

/// SQL query for one word (full)
pub const WORD_ONE: &str = "\
  SELECT name, abbr, allowed \
  FROM iris.word \
  WHERE name = $1";
