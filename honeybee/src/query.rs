// query.rs
//
// Copyright (C) 2018-2025  Minnesota Department of Transportation
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

/// SQL query for all action plans (primary)
pub const ACTION_PLAN_ALL: &str = "\
  SELECT name, notes, active, default_phase, phase \
  FROM iris.action_plan \
  ORDER BY name";

/// SQL query for one action plan (secondary)
pub const ACTION_PLAN_ONE: &str = "\
  SELECT name, notes, active, sync_actions, sticky, ignore_auto_fail, \
         default_phase, phase \
  FROM iris.action_plan \
  WHERE name = $1";

/// SQL query for all alarms (primary)
pub const ALARM_ALL: &str = "\
  SELECT name, description, controller, state \
  FROM iris.alarm \
  ORDER BY description";

/// SQL query for one alarm (secondary)
pub const ALARM_ONE: &str = "\
  SELECT name, description, controller, pin, state, trigger_time \
  FROM iris.alarm \
  WHERE name = $1";

/// SQL query for all beacons (primary)
pub const BEACON_ALL: &str = "\
  SELECT b.name, location, controller, message, notes, state \
  FROM iris.beacon b \
  LEFT JOIN geo_loc_view gl ON b.geo_loc = gl.name \
  ORDER BY name";

/// SQL query for one beacon (secondary)
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

/// SQL query for all cabinet styles (primary)
pub const CABINET_STYLE_ALL: &str = "\
  SELECT name \
  FROM iris.cabinet_style \
  ORDER BY name";

/// SQL query for one cabinet style (secondary)
pub const CABINET_STYLE_ONE: &str = "\
  SELECT name, police_panel_pin_1, police_panel_pin_2, watchdog_reset_pin_1, \
         watchdog_reset_pin_2, dip \
  FROM iris.cabinet_style \
  WHERE name = $1";

/// SQL query for all cameras (primary)
pub const CAMERA_ALL: &str = "\
  SELECT c.name, location, controller, notes, cam_num, publish \
  FROM iris.camera c \
  LEFT JOIN geo_loc_view gl ON c.geo_loc = gl.name \
  ORDER BY cam_num, c.name";

/// SQL query for one camera (secondary)
pub const CAMERA_ONE: &str = "\
  SELECT c.name, location, geo_loc, controller, pin, notes, cam_num, publish, \
         cam_template, encoder_type, enc_address, enc_port, enc_mcast, \
         enc_channel, video_loss \
  FROM iris.camera c \
  LEFT JOIN geo_loc_view gl ON c.geo_loc = gl.name \
  WHERE c.name = $1";

/// SQL query for all cameras (public)
///
/// FIXME: remove streamable when client has updated
pub const CAMERA_PUB: &str = "\
  SELECT c.name, cam_num, publish, s.name IS NOT NULL AS streamable, hashtags,\
         roadway, road_dir, cross_street, location, lat, lon, \
         ARRAY(\
           SELECT view_num \
           FROM iris.encoder_stream \
           WHERE encoder_type = c.encoder_type \
           AND view_num IS NOT NULL \
           ORDER BY view_num\
         ) AS views \
  FROM camera_view c \
  LEFT JOIN (\
    SELECT name \
    FROM iris.hashtag \
    WHERE resource_n = 'camera' AND hashtag = '#LiveStream'\
  ) s ON c.name = s.name \
  LEFT JOIN (\
    SELECT name, string_agg(hashtag, ' ' ORDER BY hashtag) AS hashtags \
    FROM iris.hashtag \
    WHERE resource_n = 'camera' \
    GROUP BY name\
  ) h ON c.name = h.name \
  ORDER BY c.name";

/// SQL query for all camera presets (primary)
pub const CAMERA_PRESET_ALL: &str = "\
  SELECT name, camera, preset_num \
  FROM iris.camera_preset \
  ORDER BY camera, preset_num";

/// SQL query for one camera preset (secondary)
pub const CAMERA_PRESET_ONE: &str = "\
  SELECT name, camera, preset_num, direction \
  FROM iris.camera_preset \
  WHERE name = $1";

/// SQL query for all comm configs (primary)
pub const COMM_CONFIG_ALL: &str = "\
  SELECT name, description \
  FROM iris.comm_config \
  ORDER BY description";

/// SQL query for one comm config (secondary)
pub const COMM_CONFIG_ONE: &str = "\
  SELECT name, description, protocol, poll_period_sec, long_poll_period_sec, \
         timeout_ms, retry_threshold, idle_disconnect_sec, \
         no_response_disconnect_sec \
  FROM iris.comm_config \
  WHERE name = $1";

/// SQL query for all comm links (primary)
pub const COMM_LINK_ALL: &str = "\
  SELECT name, description, uri, comm_config, poll_enabled, connected \
  FROM iris.comm_link \
  ORDER BY regexp_replace(name, '[0-9]', '', 'g'), \
          (regexp_replace(name, '[^0-9]', '', 'g') || '0')::INTEGER";

/// SQL query for one comm link (secondary)
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

/// SQL query for all controllers (primary)
pub const CONTROLLER_ALL: &str = "\
  SELECT c.name, location, comm_link, drop_id, cabinet_style, condition, \
         notes, setup, fail_time \
  FROM iris.controller c \
  LEFT JOIN geo_loc_view gl ON c.geo_loc = gl.name \
  ORDER BY COALESCE(regexp_replace(comm_link, '[0-9]', '', 'g'), ''), \
          (regexp_replace(comm_link, '[^0-9]', '', 'g') || '0')::INTEGER, \
           drop_id";

/// SQL query for one controller (secondary)
pub const CONTROLLER_ONE: &str = "\
  SELECT c.name, location, geo_loc, comm_link, drop_id, cabinet_style, \
         condition, notes, password, setup, status, fail_time \
  FROM iris.controller c \
  LEFT JOIN geo_loc_view gl ON c.geo_loc = gl.name \
  WHERE c.name = $1";

/// SQL query for all IO on one controller
pub const CONTROLLER_IO_ONE: &str = "\
  SELECT pin, name, resource_n \
  FROM iris.controller_io \
  WHERE controller = $1 \
  ORDER BY pin";

/// SQL query for all day matchers (primary)
pub const DAY_MATCHER_ALL: &str = "\
  SELECT name, day_plan, month, day, weekday, week, shift \
  FROM iris.day_matcher \
  ORDER BY day_plan, month, day, weekday, week, shift";

/// SQL query for one day matcher
pub const DAY_MATCHER_ONE: &str = "\
  SELECT name, day_plan, month, day, weekday, week, shift \
  FROM iris.day_matcher \
  WHERE name = $1";

/// SQL query for all day plans (primary)
pub const DAY_PLAN_ALL: &str = "\
  SELECT name, holidays \
  FROM iris.day_plan \
  ORDER BY name";

/// SQL query for one day plan
pub const DAY_PLAN_ONE: &str = "\
  SELECT name, holidays \
  FROM iris.day_plan \
  WHERE name = $1";

/// SQL query for all detectors (primary)
pub const DETECTOR_ALL: &str = "\
  SELECT name, label, controller, notes \
  FROM detector_view \
  ORDER BY regexp_replace(name, '[0-9]', '', 'g'), \
          (regexp_replace(name, '[^0-9]', '', 'g') || '0')::INTEGER";

/// SQL query for one detector (secondary)
pub const DETECTOR_ONE: &str = "\
  SELECT d.name, label, r_node, controller, pin, notes, lane_code, \
         lane_number, abandoned, fake, field_length, force_fail, auto_fail \
  FROM iris.detector d \
  LEFT JOIN detector_label_view dl ON d.name = dl.det_id \
  WHERE d.name = $1";

/// SQL query for all detectors (public)
pub const DETECTOR_PUB: &str = "\
  SELECT d.name, r_node, cor_id, lane_number, lane_code, speed_limit \
  FROM detector_view d
  JOIN r_node_view r ON d.r_node = r.name
  ORDER BY regexp_replace(d.name, '[0-9]', '', 'g'), \
          (regexp_replace(d.name, '[^0-9]', '', 'g') || '0')::INTEGER";

/// SQL query for all device actions (primary)
pub const DEVICE_ACTION_ALL: &str = "\
  SELECT name, action_plan, hashtag, phase \
  FROM iris.device_action \
  ORDER BY action_plan, name";

/// SQL query for one device action (secondary)
pub const DEVICE_ACTION_ONE: &str = "\
  SELECT name, action_plan, hashtag, phase, msg_priority, msg_pattern \
  FROM iris.device_action \
  WHERE name = $1";

/// SQL query for directions (LUT)
pub const DIRECTION_LUT: &str = "\
  SELECT id, direction, dir \
  FROM iris.direction \
  ORDER BY id";

/// SQL query for all DMS (primary)
pub const DMS_ALL: &str = "\
  SELECT d.name, location, msg_current, lock, \
         NULLIF(char_length(status->>'faults') > 0, false) AS has_faults, \
         notes, controller \
  FROM iris.dms d \
  LEFT JOIN geo_loc_view gl ON d.geo_loc = gl.name \
  ORDER BY d.name";

/// SQL query for one DMS (secondary)
pub const DMS_ONE: &str = "\
  SELECT d.name, location, geo_loc, controller, pin, notes, \
         static_graphic, beacon, preset, sign_config, sign_detail, \
         lock, status, char_length(status->>'faults') > 0 AS has_faults, \
         msg_sched, msg_current, pixel_failures \
  FROM iris.dms d \
  LEFT JOIN geo_loc_view gl ON d.geo_loc = gl.name \
  WHERE d.name = $1";

/// SQL query for all DMS (public)
pub const DMS_PUB: &str = "\
  SELECT name, sign_config, sign_detail, roadway, road_dir, \
         cross_street, location, lat, lon \
  FROM dms_view \
  ORDER BY name";

/// SQL query for all DMS status (public)
///
/// NOTE: the `sources`, `duration` and `expire_time` attributes are
///       deprecated, but required by external systems (for now)
pub const DMS_STATUS: &str = "\
  SELECT name, msg_current, \
         replace(substring(msg_owner FROM 'IRIS; ([^;]*).*'), '+', ', ') \
         AS sources, failed, NULL AS duration, NULL AS expire_time \
  FROM dms_message_view WHERE condition = 'Active' \
  ORDER BY name";

/// SQL query for all domains (primary)
pub const DOMAIN_ALL: &str = "\
  SELECT name, enabled \
  FROM iris.domain \
  ORDER BY name";

/// SQL query for one domain (secondary)
pub const DOMAIN_ONE: &str = "\
  SELECT name, enabled, block \
  FROM iris.domain \
  WHERE name = $1";

/// SQL query for all encoder streams (primary)
pub const ENCODER_STREAM_ALL: &str = "\
  SELECT name, encoder_type, view_num, encoding \
  FROM iris.encoder_stream \
  ORDER BY name";

/// SQL query for one encoder stream
pub const ENCODER_STREAM_ONE: &str = "\
  SELECT name, encoder_type, view_num, encoding, flow_stream, quality, \
         uri_scheme, uri_path, mcast_port, latency \
  FROM iris.encoder_stream \
  WHERE name = $1";

/// SQL query for all encoder types (primary)
pub const ENCODER_TYPE_ALL: &str = "\
  SELECT name, make, model, config \
  FROM iris.encoder_type \
  ORDER BY name";

/// SQL query for one encoder type
pub const ENCODER_TYPE_ONE: &str = "\
  SELECT name, make, model, config \
  FROM iris.encoder_type \
  WHERE name = $1";

/// SQL query for video encodings (LUT)
pub const ENCODING_LUT: &str = "\
  SELECT id, description \
  FROM iris.encoding \
  ORDER BY description";

/// SQL query for all event configs (primary)
pub const EVENT_CONFIG_ALL: &str = "\
  SELECT name, enable_store, enable_purge, purge_days \
  FROM iris.event_config \
  ORDER BY name";

/// SQL query for event descriptions (LUT)
pub const EVENT_DESCRIPTION_LUT: &str = "\
  SELECT event_desc_id AS id, description \
  FROM event.event_description \
  ORDER BY event_desc_id";

/// SQL query for all flow streams (primary)
pub const FLOW_STREAM_ALL: &str = "\
  SELECT name, controller \
  FROM iris.flow_stream \
  ORDER BY name";

/// SQL query for one flow stream (secondary)
pub const FLOW_STREAM_ONE: &str = "\
  SELECT name, controller, pin, restricted, loc_overlay, quality, camera, \
         mon_num, address, port, status \
  FROM iris.flow_stream \
  WHERE name = $1";

/// SQL query for all fonts (primary)
pub const FONT_ALL: &str = "\
  SELECT f_number AS font_number, name \
  FROM iris.font \
  ORDER BY f_number";

/// SQL query for one font (secondary)
pub const FONT_ONE: &str = "\
  SELECT f_number AS font_number, name \
  FROM iris.font \
  WHERE name = $1";

/// SQL query for all gate arms (primary)
pub const GATE_ARM_ALL: &str = "\
  SELECT g.name, location, controller, notes, arm_state, interlock \
  FROM iris.gate_arm g \
  LEFT JOIN geo_loc_view gl ON g.geo_loc = gl.name \
  ORDER BY name";

/// SQL query for one gate arm (secondary)
pub const GATE_ARM_ONE: &str = "\
  SELECT g.name, location, geo_loc, controller, pin, preset, notes, \
         opposing, downstream_hashtag, arm_state, interlock, fault \
  FROM iris.gate_arm g \
  LEFT JOIN geo_loc_view gl ON g.geo_loc = gl.name \
  WHERE g.name = $1";

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

/// SQL query for geo location markers
pub const GEO_LOC_MARKER: &str = "\
  SELECT name, roadway, road_dir, lat, lon \
  FROM iris.geo_loc \
  WHERE resource_n = $1";

/// SQL query for one geo location
pub const GEO_LOC_ONE: &str = "\
  SELECT name, resource_n, roadway, road_dir, cross_street, cross_dir, \
         cross_mod, landmark, lat, lon \
  FROM iris.geo_loc \
  WHERE name = $1 AND resource_n = $2";

/// SQL query for all GPS (primary)
pub const GPS_ALL: &str = "\
  SELECT name, controller, notes \
  FROM iris.gps \
  ORDER BY name";

/// SQL query for one GPS (secondary)
pub const GPS_ONE: &str = "\
  SELECT name, controller, pin, notes, geo_loc, latest_poll, latest_sample, \
         lat, lon \
  FROM iris.gps \
  WHERE name = $1";

/// SQL query for all graphics (primary)
pub const GRAPHIC_ALL: &str = "\
  SELECT g_number AS number, 'G' || g_number AS name \
  FROM iris.graphic \
  WHERE g_number < 256 \
  ORDER BY number";

/// SQL query for one graphic (secondary)
pub const GRAPHIC_ONE: &str = "\
  SELECT g_number AS number, 'G' || g_number AS name \
  FROM iris.graphic \
  WHERE name = $1";

/// SQL query for all hashtags
pub const HASHTAG_ALL: &str = "\
  SELECT hashtag, resource_n \
  FROM hashtag_view \
  GROUP BY hashtag, resource_n \
  ORDER BY hashtag";

/// SQL query for all active incidents (primary)
pub const INCIDENT_ALL: &str = "\
  SELECT name, replaces, event_desc, road, dir, detail, cleared, confirmed \
  FROM event.incident \
  WHERE cleared = false \
  ORDER BY event_date";

/// SQL query for one active incident (secondary)
pub const INCIDENT_ONE: &str = "\
  SELECT name, replaces, event_date, event_desc, road, dir, lane_code, \
         impact, cleared, confirmed, camera, detail, lat, lon, user_id \
  FROM event.incident \
  WHERE name = $1";

/// SQL query for incident locations
pub const INCIDENT_LOCS: &str = "\
  SELECT name, road, dir, lat, lon \
  FROM event.incident \
  WHERE cleared = false";

/// SQL query for all active incidents (public)
pub const INCIDENT_PUB: &str = "\
  SELECT name, event_date, description, road, direction, lane_type, \
         impact, confirmed, camera, detail, replaces, lat, lon \
  FROM incident_view \
  WHERE cleared = false";

/// SQL query for all incident details (primary)
pub const INC_DETAIL_ALL: &str = "\
  SELECT name, description \
  FROM iris.inc_detail \
  ORDER BY name";

/// SQL query for one incident detail
pub const INC_DETAIL_ONE: &str = "\
  SELECT name, description \
  FROM iris.inc_detail \
  WHERE name = $1";

/// SQL query for all incident advice (primary)
pub const INC_ADVICE_ALL: &str = "\
  SELECT name, impact \
  FROM iris.inc_advice \
  ORDER BY name";

/// SQL query for one incident advice
pub const INC_ADVICE_ONE: &str = "\
  SELECT name, impact, open_lanes, impacted_lanes, range, lane_code, multi \
  FROM iris.inc_advice \
  WHERE name = $1";

/// SQL query for all incident descriptor (primary)
pub const INC_DESCRIPTOR_ALL: &str = "\
  SELECT name, event_desc_id \
  FROM iris.inc_descriptor \
  ORDER BY name";

/// SQL query for one incident descriptor
pub const INC_DESCRIPTOR_ONE: &str = "\
  SELECT name, event_desc_id, detail, lane_code, multi \
  FROM iris.inc_descriptor \
  WHERE name = $1";

/// SQL query for incident impacts (LUT)
pub const INC_IMPACT_LUT: &str = "\
  SELECT id, description \
  FROM iris.inc_impact \
  ORDER BY id";

/// SQL query for all incident locators (primary)
pub const INC_LOCATOR_ALL: &str = "\
  SELECT name, range \
  FROM iris.inc_locator \
  ORDER BY name";

/// SQL query for one incident locator
pub const INC_LOCATOR_ONE: &str = "\
  SELECT name, range, branched, picked, multi \
  FROM iris.inc_locator \
  WHERE name = $1";

/// SQL query for incident ranges (LUT)
pub const INC_RANGE_LUT: &str = "\
  SELECT id, description \
  FROM iris.inc_range \
  ORDER BY id";

/// SQL query for lane codes (LUT)
pub const LANE_CODE_LUT: &str = "\
  SELECT lcode, description \
  FROM iris.lane_code \
  ORDER BY lcode";

/// SQL query for all LCS arrays (primary)
pub const LCS_ALL: &str = "\
  SELECT l.name, location, controller, notes, lock, status \
  FROM iris.lcs l \
  LEFT JOIN geo_loc_view gl ON l.geo_loc = gl.name \
  ORDER BY l.name";

/// SQL query for one LCS array (secondary)
pub const LCS_ONE: &str = "\
  SELECT l.name, location, geo_loc, controller, pin, notes, lcs_type, \
         shift, preset, lock, status \
  FROM iris.lcs l \
  LEFT JOIN geo_loc_view gl ON l.geo_loc = gl.name \
  WHERE l.name = $1";

/// SQL query for LCS indications (LUT)
pub const LCS_INDICATION_LUT: &str = "\
  SELECT id, description, symbol \
  FROM iris.lcs_indication \
  ORDER BY id";

/// SQL query for all LCS states (primary)
pub const LCS_STATE_ALL: &str = "\
  SELECT name, controller, lcs, lane, indication \
  FROM iris.lcs_state \
  ORDER BY name";

/// SQL query for one LCS state (secondary)
pub const LCS_STATE_ONE: &str = "\
  SELECT name, controller, pin, lcs, lane, indication, msg_pattern, msg_num \
  FROM iris.lcs_state \
  WHERE name = $1";

/// SQL query for LCS types (LUT)
pub const LCS_TYPE_LUT: &str = "\
  SELECT id, description \
  FROM iris.lcs_type \
  ORDER BY id";

/// SQL query for ramp meter algorithms (LUT)
pub const METER_ALGORITHM_LUT: &str = "\
  SELECT id, description \
  FROM iris.meter_algorithm \
  ORDER BY id";

/// SQL query for ramp meter types (LUT)
pub const METER_TYPE_LUT: &str = "\
  SELECT id, description, lanes \
  FROM iris.meter_type \
  ORDER BY id";

/// SQL query for all modems (primary)
pub const MODEM_ALL: &str = "\
  SELECT name, enabled \
  FROM iris.modem \
  ORDER BY name";

/// SQL query for one modem (secondary)
pub const MODEM_ONE: &str = "\
  SELECT name, uri, config, enabled, timeout_ms \
  FROM iris.modem \
  WHERE name = $1";

/// SQL query for all monitor styles (primary)
pub const MONITOR_STYLE_ALL: &str = "\
  SELECT name \
  FROM iris.monitor_style \
  ORDER BY name";

/// SQL query for one monitor style (secondary)
pub const MONITOR_STYLE_ONE: &str = "\
  SELECT name, force_aspect, accent, font_sz, title_bar, auto_expand, hgap, \
         vgap \
  FROM iris.monitor_style \
  WHERE name = $1";

/// SQL query for all message lines (primary)
pub const MSG_LINE_ALL: &str = "\
  SELECT name, msg_pattern, line, multi, restrict_hashtag \
  FROM iris.msg_line \
  ORDER BY msg_pattern, line, rank, multi, restrict_hashtag";

/// SQL query for one message line (secondary)
pub const MSG_LINE_ONE: &str = "\
  SELECT name, msg_pattern, line, multi, rank, restrict_hashtag \
  FROM iris.msg_line \
  WHERE name = $1";

/// SQL query for all message patterns (primary)
pub const MSG_PATTERN_ALL: &str = "\
  SELECT name, multi, compose_hashtag \
  FROM iris.msg_pattern \
  ORDER BY name";

/// SQL query for one message pattern (secondary)
pub const MSG_PATTERN_ONE: &str = "\
  SELECT name, multi, flash_beacon, pixel_service, compose_hashtag \
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

/// SQL query for all permissions (primary)
pub const PERMISSION_ALL: &str = "\
  SELECT name, role, base_resource, hashtag, access_level \
  FROM iris.permission \
  ORDER BY role, base_resource, name";

/// SQL query for one permission (secondary)
pub const PERMISSION_ONE: &str = "\
  SELECT name, role, base_resource, hashtag, access_level \
  FROM iris.permission \
  WHERE name = $1";

/// SQL query for all plan phases (primary)
pub const PLAN_PHASE_ALL: &str = "\
  SELECT name, selectable, hold_time, next_phase \
  FROM iris.plan_phase \
  ORDER BY name";

/// SQL query for one plan phase (secondary)
pub const PLAN_PHASE_ONE: &str = "\
  SELECT name, selectable, hold_time, next_phase \
  FROM iris.plan_phase \
  WHERE name = $1";

/// SQL query for all play lists (primary)
pub const PLAY_LIST_ALL: &str = "\
  SELECT name, seq_num, notes \
  FROM iris.play_list \
  ORDER BY name";

/// SQL query for one play list (secondary)
pub const PLAY_LIST_ONE: &str = "\
  SELECT name, meta, seq_num, notes, ARRAY(\
      SELECT COALESCE(camera, sub_list) \
      FROM iris.play_list_entry \
      WHERE play_list = name \
      ORDER BY ordinal\
  ) AS entries \
  FROM iris.play_list \
  WHERE name = $1";

/// SQL query for all ramp meters (primary)
pub const RAMP_METER_ALL: &str = "\
  SELECT m.name, location, controller, notes, lock, status \
  FROM iris.ramp_meter m \
  LEFT JOIN geo_loc_view gl ON m.geo_loc = gl.name \
  ORDER BY m.name";

/// SQL query for one ramp meter (secondary)
pub const RAMP_METER_ONE: &str = "\
  SELECT m.name, location, geo_loc, controller, pin, notes, meter_type, \
         beacon, preset, storage, max_wait, algorithm, am_target, pm_target, \
         lock, status \
  FROM iris.ramp_meter m \
  LEFT JOIN geo_loc_view gl ON m.geo_loc = gl.name \
  WHERE m.name = $1";

/// SQL query for resource types (LUT)
pub const RESOURCE_TYPE_LUT: &str = "\
  SELECT name, base \
  FROM iris.resource_type \
  ORDER BY name";

/// SQL query for all Rnodes (secondary)
pub const RNODE_FULL: &str = "\
  SELECT name, roadway, road_dir, location, lat, lon, transition, \
         lanes, shift, active, station_id, speed_limit \
  FROM r_node_view";

/// SQL query for one RNode (secondary)
pub const RNODE_ONE: &str = "\
  SELECT name, roadway, road_dir, location, lat, lon, transition, \
         lanes, shift, active, station_id, speed_limit \
  FROM r_node_view n \
  WHERE n.name = $1";

/// SQL query for RNode transitions (LUT)
pub const RNODE_TRANSITION_LUT: &str = "\
  SELECT id, description \
  FROM iris.r_node_transition \
  ORDER BY id";

/// SQL query for RNode types (LUT)
pub const RNODE_TYPE_LUT: &str = "\
  SELECT id, description \
  FROM iris.r_node_type \
  ORDER BY id";

/// SQL query for all roads (primary)
pub const ROAD_ALL: &str = "\
  SELECT name, abbrev, r_class, direction \
  FROM iris.road \
  ORDER BY name";

/// SQL query for all roads (secondary)
pub const ROAD_FULL: &str = "\
  SELECT name, abbrev, r_class, direction, scale \
  FROM iris.road \
  JOIN iris.road_class ON r_class = id";

/// SQL query for one road (secondary)
pub const ROAD_ONE: &str = "\
  SELECT name, abbrev, r_class, direction, scale \
  FROM iris.road \
  JOIN iris.road_class ON r_class = id \
  WHERE name = $1";

/// SQL query for all road affixes (primary)
pub const ROAD_AFFIX_ALL: &str = "\
  SELECT name, prefix, fixup, allow_retain \
  FROM iris.road_affix \
  ORDER BY name";

/// SQL query for one road affix
pub const ROAD_AFFIX_ONE: &str = "\
  SELECT name, prefix, fixup, allow_retain \
  FROM iris.road_affix \
  WHERE name = $1";

/// SQL query for road classes (LUT)
pub const ROAD_CLASS_LUT: &str = "\
  SELECT id, description, grade, scale \
  FROM iris.road_class \
  ORDER BY id";

/// SQL query for road modifiers (LUT)
pub const ROAD_MODIFIER_LUT: &str = "\
  SELECT id, modifier, mod AS md \
  FROM iris.road_modifier \
  ORDER BY id";

/// SQL query for all roles (primary)
pub const ROLE_ALL: &str = "\
  SELECT name, enabled \
  FROM iris.role \
  ORDER BY name";

/// SQL query for one role (secondary)
pub const ROLE_ONE: &str = "\
  SELECT name, enabled, ARRAY(\
           SELECT domain \
           FROM iris.role_domain \
           WHERE role = r.name \
           ORDER BY domain\
         ) AS domains \
  FROM iris.role r \
  WHERE name = $1";

/// SQL query for all sign configs (primary)
pub const SIGN_CONFIG_ALL: &str = "\
  WITH cte AS (\
    SELECT sign_config, count(*) AS sign_count \
    FROM iris._dms \
    GROUP BY sign_config\
  ) \
  SELECT name, face_width, face_height, border_horiz, border_vert, \
         pitch_horiz, pitch_vert, pixel_width, pixel_height, \
         char_width, char_height, monochrome_foreground, \
         monochrome_background, color_scheme, default_font, \
         module_width, module_height, \
         COALESCE(sign_count, 0) AS sign_count \
  FROM sign_config_view sc \
  LEFT JOIN cte ON name = cte.sign_config \
  ORDER BY name";

/// SQL query for one sign configuration (secondary)
pub const SIGN_CONFIG_ONE: &str = "\
  WITH cte AS (\
    SELECT sign_config, count(*) AS sign_count \
    FROM iris._dms \
    GROUP BY sign_config\
  ) \
  SELECT name, face_width, face_height, border_horiz, border_vert, \
         pitch_horiz, pitch_vert, pixel_width, pixel_height, \
         char_width, char_height, monochrome_foreground, \
         monochrome_background, color_scheme, default_font, \
         module_width, module_height, \
         COALESCE(sign_count, 0) AS sign_count \
  FROM sign_config_view sc \
  LEFT JOIN cte ON name = cte.sign_config \
  WHERE name = $1";

/// SQL query for all sign details (primary)
pub const SIGN_DETAIL_ALL: &str = "\
  SELECT name, dms_type, portable, technology, sign_access, legend, \
         beacon_type, hardware_make, hardware_model, software_make, \
         software_model, supported_tags, max_pages, max_multi_len, \
         beacon_activation_flag, pixel_service_flag \
  FROM sign_detail_view \
  ORDER BY name";

/// SQL query for one sign detail (secondary)
pub const SIGN_DETAIL_ONE: &str = "\
  SELECT name, dms_type, portable, technology, sign_access, legend, \
         beacon_type, hardware_make, hardware_model, software_make, \
         software_model, supported_tags, max_pages, max_multi_len, \
         beacon_activation_flag, pixel_service_flag \
  FROM sign_detail_view \
  WHERE name = $1";

/// SQL query for all sign messages (public)
pub const SIGN_MSG_PUB: &str = "\
  SELECT name, sign_config, multi, msg_owner, sticky, flash_beacon, \
         pixel_service, msg_priority \
  FROM sign_message_view \
  ORDER BY name";

/// SQL query for one sign message (secondary)
pub const SIGN_MSG_ONE: &str = "\
  SELECT name, sign_config, multi, msg_owner, sticky, flash_beacon, \
         pixel_service, msg_priority \
  FROM iris.sign_message \
  WHERE name = $1";

/// SQL query for system attributes (all)
pub const SYSTEM_ATTRIBUTE_ALL: &str = "\
  SELECT jsonb_object_agg(name, value)::text \
  FROM iris.system_attribute";

/// SQL query for system attributes (public)
pub const SYSTEM_ATTRIBUTE_PUB: &str = "\
  SELECT jsonb_object_agg(name, value)::text \
  FROM iris.system_attribute \
  WHERE name LIKE 'dms\\_%' OR name LIKE 'map\\_%'";

/// SQL query for all tag readers (primary)
pub const TAG_READER_ALL: &str = "\
  SELECT t.name, location, controller, notes \
  FROM iris.tag_reader t \
  LEFT JOIN geo_loc_view gl ON t.geo_loc = gl.name \
  ORDER BY t.name";

/// SQL query for one tag reader (secondary)
pub const TAG_READER_ONE: &str = "\
  SELECT t.name, location, geo_loc, controller, pin, notes, toll_zone, \
         settings \
  FROM iris.tag_reader t \
  LEFT JOIN geo_loc_view gl ON t.geo_loc = gl.name \
  WHERE t.name = $1";

/// SQL query for all time actions (primary)
pub const TIME_ACTION_ALL: &str = "\
  SELECT name, action_plan, day_plan, sched_date, time_of_day, phase \
  FROM iris.time_action \
  ORDER BY action_plan, day_plan, sched_date, time_of_day, name";

/// SQL query for one time action (secondary)
pub const TIME_ACTION_ONE: &str = "\
  SELECT name, action_plan, day_plan, sched_date, time_of_day, phase \
  FROM iris.time_action \
  WHERE name = $1";

/// SQL query for all toll zones (primary)
pub const TOLL_ZONE_ALL: &str = "\
  SELECT name, tollway \
  FROM iris.toll_zone \
  ORDER BY name";

/// SQL query for one toll zone (secondary)
pub const TOLL_ZONE_ONE: &str = "\
  SELECT name, start_id, end_id, tollway, alpha, beta, max_price \
  FROM iris.toll_zone \
  WHERE name = $1";

/// SQL query for all users (primary)
pub const USER_ALL: &str = "\
  SELECT name, full_name, role, enabled \
  FROM iris.user_id \
  ORDER BY name";

/// SQL query for one user (secondary)
pub const USER_ONE: &str = "\
  SELECT name, full_name, dn, role, enabled \
  FROM iris.user_id \
  WHERE name = $1";

/// SQL query for all video monitors (primary)
pub const VIDEO_MONITOR_ALL: &str = "\
  SELECT name, mon_num, controller, notes \
  FROM iris.video_monitor \
  ORDER BY mon_num, name";

/// SQL query for one video monitor (secondary)
pub const VIDEO_MONITOR_ONE: &str = "\
  SELECT name, mon_num, controller, pin, notes, restricted, monitor_style, \
         camera \
  FROM iris.video_monitor \
  WHERE name = $1";

/// SQL query for all weather sensors (primary)
pub const WEATHER_SENSOR_ALL: &str = "\
  SELECT ws.name, site_id, alt_id, location, controller, notes \
  FROM iris.weather_sensor ws \
  LEFT JOIN geo_loc_view gl ON ws.geo_loc = gl.name \
  ORDER BY ws.name";

/// SQL query for one weather sensor (secondary)
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

/// SQL query for all words (primary)
pub const WORD_ALL: &str = "\
  SELECT name, abbr, allowed \
  FROM iris.word \
  ORDER BY name";

/// SQL query for one word (secondary)
pub const WORD_ONE: &str = "\
  SELECT name, abbr, allowed \
  FROM iris.word \
  WHERE name = $1";
