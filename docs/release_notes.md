# IRIS 5.x Release Notes

5.75.0 (19 Sep 2025)
 - Remove gate arm arrays
 - Support gate arm device actions with action plans, with updated UI
 - **WARNING**: the migrate-5.75.sql script does not convert existing
   gate arm arrays to action plans -- manual
   [configuration](gate_arms.html#setup) is required!
 - Exclude inactive devices from action plan counts and checks
 - Check action plan phase hold time every 5 seconds
 - Add `selectable` column to allow non-selectable plan phases
 - Remove `TAB_LIST` from `user.properties`

5.74.0 (5 Sep 2025)
 - Fix action plan device permission checks
 - Enhance gate arms to eventually replace arrays with action plans
 - Process action plans immediately after phase changes
 - Rework action plan processing for devices
 - Use hard-coded values instead of system attributes for DMS pixel checks

5.73.0 (22 Aug 2025)
 - Fix DMS lock duration when combined with planned message
 - Always build sign message on server (replace lock `message` w/ `multi`)
 - ClearGuide: add `sp_cond` speed "condition" tag mode + change `min` to range
 - Add "Action Plan" resource to web UI
 - Add action plans to DMS control card (web UI)
 - Require "Configure" access to store preset on cameras
 - Require "Configure" access to change "notes" fields
 - Add device permission check for changing action plan phase
 - Add gate arm hashtag support
 - Fixed logic to reap/purge old weather alerts
 - onvif: Improve error logging / debugging

5.72.0 (31 Jul 2025)
 - Replace DMS `msg_user` + `expire_time` with `lock` JSON value (`reason`,
   `message`, `expires`, `user_id`)
 - Move SignMessage `incident` to DMS `lock`
 - Replace SignMessage `duration` with `sticky`
 - Handle user updating DMS lock duration
 - DROP sign event `duration`
 - Constrain device action `msg_priority` (1-15)
 - Skip camera device actions for incident cameras
 - Fix ONVIF null pointer exceptions

5.71.0 (7 Jul 2025)
 - Revert broken DMS composer change (displayed msg)
 - Improve logging of blank DMS messages (owner)
 - Improve meter / LCS expire time, labels, etc.
 - Add controller fault when cabinet style is not set
 - Clean up logic for GPS jitter filter bypass
 - Reduce number of ONVIF messages sent during PTZ
 - Handle unexpected NTCIP GenError responses when querying DMS configuration
 - Fix NTCIP ESS pavement friction sensor polling
 - Fix MNDOT download request 208 monitor reset
 - Fix problems with CAP event parsing
 - Fix DMS notes / hashtag updates
 - Add `incident_max_sign_miles` system attribute
 - Fix ordering of alert reaping
 - Fix rare client crash when removing an object

5.70.0 (14 Apr 2025)
 - Add LCS event logging table
 - Clean up hashtags when reaping alerts
 - Fixed issue preventing meter faults from updating
 - Add "reserve" ramp meter lock reason
 - Simplify ramp meter operation (remove on/off)
 - Complete ramp meter operation from web UI
 - Rework web UI html generation
 - Add LCS to web UI map
 - Add placeholder for CampbellCloud protocol

5.69.0 (25 Mar 2025)
 - Refactor LCS database tables / classes
 - Remove lane markings (LCS features can now support them)
 - MnDOT: implement send settings for all devices
 - NTCIP: Fetch DMS graphics on query settings

5.68.0 (3 Mar 2025)
 - Improved ONVIF camera control issues
 - Added `pixel_service` flag to message patterns
 - Added CAP-NWS protocol; renamed CAP to CAP-IPAWS
 - Added support for testing protocols with `file://` scheme
 - Improved ramp meter lock constraints and UI
 - Fixed quirks in ADEC TDC protocol
 - honeybee: Update to axum 0.8 (+ other deps)
 - Update to Rust 2024 Edition

5.67.0 (19 Feb 2025)
 - Add protocol driver for ADEC TDC non-intrusive detector
 - Improved ramp meter locks (15-minute duration + user ID)
 - Added ramp meter `status` JSON (rate, queue, fault)
 - Add `Maintenance` controller condition
 - Simplified `LOCK_ON` auto-fail logic
 - cohuptz: Don't fault on unexpected device requests
 - bulb: Fleshed out ramp meter cards + map markers

5.66.0 (27 Jan 2025)
 - Add `cpark` driver for Drivewyze Central Park API
 - Show offline device status as `OFFLINE`
 - Fix controller `status` read on startup
 - Fix ramp meter state matching for CD road meters

5.65.0 (13 Jan 2025)
 - Add ramp meter `fault` column to diagnose setup problems
 - Allow creating sign message with only DMS hashtag permission
 - Fix problem with text rectangles in message patterns
 - Fix DMS styles not changing on status update
 - Improve controller fault handling
 - Add `ordinal` column to `r_node`

5.64.0 (16 Dec 2024)
 - Fixed controller `status` fault handling
 - Added `email_event` logging table
 - Simplified error handling for a few comm. protocols
 - Added camera device actions (wiper and recall preset)
 - bulb: Fixed serialization errors
 - honeybee: Add more API resources: `toll_zone`, `monitor_style`, `play_list`
 - Removed obsolete system attributes: `camera_wiper_precip_mm_hr`,
   `dmsxml_reinit_detect`, `email_recipient_action_plan`, `email_recipient_aws`,
   `email_recipient_dmsxml_reinit`, `email_recipient_gate_arm`

5.63.0 (10 Dec 2024)
 - Renamed "Maintenance" style to "Fault"
 - Renamed "Failed" style to "Offline"
 - Improved DMS `status` JSON format
 - Improved controller `setup` JSON format/support
 - Add controller `status` JSON
 - honeybee: Add more API resources: `action_plan`, `device_action`,
   `plan_phase`, `time_action`
 - Removed `dms_pixel_maint_threshold` system attribute

5.62.0 (20 Nov 2024)
 - Log sign event with blank msg (including user ID)
 - Log meter lock events (add `meter_lock_event_view`)
 - Add `phase` / `user_id` to action plan event
 - Add `user_id` to beacon event
 - Add `user_id` to incident / `incident_update`
 - Made event column names consistent: `iris_user` => `user_id`
 - Reworked day plan/matcher relationship
 - honeybee: Add LUT resources: `encoding`, `inc_impact`, `inc_range`,
   `r_node_type`, `r_node_transition`, `lane_code`, `road_class`,
   `meter_queue_state`
 - honeybee: Add resources: `encoder_type`, `encoder_stream`, `camera_preset`,
   `incident_detail`, `inc_descriptor`, `inc_locator`, `road_affix`,
   `day_plan`, `day_matcher`
 - Add Sierra Wireless SSH GPS driver
 - Fixed Sonar `phantom` bug on SQL error
 - Fixed client Sonar updates for GPS/ESS

5.61.0 (17 Oct 2024)
 - Remove capabilities/privileges (replace with permissions)
   NOTE: migrate-5.61.sql handles the transition, but non-standard
   capabilities might not be converted correctly
 - Made permission into Sonar object (not DB-only)
 - Added support for video monitor hashtags (for permissions)
 - Drop video monitor `group_n` (use hashtags instead)
 - Replace action plan `description` with `notes` (+ hashtag)
 - Remove catalogs in favor of "meta" play lists
 - Replace play list `description` with `notes` (+ hashtag)
 - Added "scratch" play lists (manually created) to replace "personal" ones
 - Consolidated SQL trigger functions for hashtags
 - Add `pixel_service` flag to sign message (set for sticky action plans)
 - Add system attributes to allow disabling legacy XML output
 - Replace many `*_purge_days` system attributes with more flexible
   event config table
 - Rename system attributes:
   * `sample_archive_enable` => `detector_data_archive_enable`
   * `camera_sequence_dwell_sec` => `camera_playlist_dwell_sec`
 - Replaced `operation_retry_threshold` system attribute with
   `retry_threshold` column on comm config
 - Fixed issue preventing weather sensor settings polls
 - Renamed `gate.arm.whitelist` property => `gate.arm.allowlist`
 - Renamed `http.proxy.whitelist` property => `http.proxy.allowlist`
 - Fixed infinite loop in mndot170 meter settings operation
 - Add camera markers to web UI map
 - Convert mayfly from tide to axum crate
 - REST API:
   * Add `cam_num` to `camera_pub` JSON
   * Add restricted `api/system_attribute`
   * Add restricted `api/event_config`

5.60.0 (27 Aug 2024)
 - Merged DMS/meter/etc. actions into "device_action"
 - Device actions use hashtags for ramp meters, beacons, etc.
 - Device actions can use msg_pattern for condition checks (non-DMS)
 - Added rwis_auto_max_dist_miles system attribute;
   used when weather sensors not set, or non-DMS
 - Add SignConfig cards to Web UI

5.59.0 (12 Aug 2024)
 - Reworked GPS to use with devices other than DMS
 - Renamed "dms_gps_jitter_m" => "gps_jitter_m"
 - Improved DMS photocell status JSON
 - Improved DMS power supply status JSON
 - Improved DMS pixel failure status JSON
 - Add beacons and weather sensors to Web UI map
 - Add checks for invalid DMS temps (NTCIP)
 - Fix minor SQL problems
 - Improve client event logging

5.58.0 (31 Jul 2024)
 - Replace "sonar.ldap.urls" with "sonar.ldap.url" in iris-server.properties
 - Allow login while LDAP server unreachable (CrowdStrike event)
 - Merge hashtags into "notes" for camera / DMS
 - Add hashtag support for more devices: beacon, gps, gate_arm_array,
   ramp_meter, weather_sensor
 - Improve web UI fetch latency
 - Improve consistency of web UI
 - On web UI, "fly" to map location when DMS card is selected

5.57.0 (10 Jul 2024)
 - Add RWIS message automation
 - Drop "streamable" from camera table
 - Another fix for "ignore_auto_fail" calculation
 - Other minor fixes and updates

5.56.0 (27 Jun 2024)
 - Improved consistency of web UI "cards"
 - Add Dms "Request" card (web UI)
 - Replaced "user_id_domain" relation with "role_domain"
 - Check domains on login to REST API
 - Fixed "MSG RENDER FAILED" on sign message expiration
 - Fixed invalid JSON (station_sample)
 - Minor SQL cleanups
 - Removed mail.jar dependency from Java client

5.55.0 (3 Jun 2024)
 - Add hashtag support for camera devices
 - Add "domain" resource to REST API
 - Minor improvements to Web UI
 - Fixed some SQL bugs from previous migrate script
 - Fixed NTCIP "zombie" message problem
 - Fixed problem with LDAPS for SONAR authentication
 - Fixed "ignore_auto_fail" speed calculation

5.54.0 (21 May 2024)
 - Various enhancements to honeybee / bulb for Web UI
 - Add "ignore_auto_fail" to action plans
 - Add configurable video stream timeout
 - Add landmark support for detector labels
 - Simplify permission checks to 16 "base" resources

5.53.0 (23 Apr 2024)
 - Implemented SSE notifications from honeybee to web UI (bulb)
 - Refactored web UI code
 - Fixed ONVIF set preset bug
 - Fixed problem with DMS reporter
 - Add tooltips for DMS / camera / weather sensor

5.52.0 (2 Apr 2024)
 - Improve ONVIF compatibility
 - Added 'camera_publish' notification channel
 - sql: Renamed i_user to user_id (also i_user_view => user_id_view)
 - sql: Make notes nullable (controller, r_node, all devices)
 - Fixed rare exception when querying fonts from signs
 - Fixed nginx caching problem
 - honeybee: Fixed routing problems
 - honeybee: Rearranged directories

5.51.0 (19 Mar 2024)
 - honeybee: Add SSE push notifications
 - Simplified Postgres LISTEN/NOTIFY scheme
 - honeybee: working dir `/var/www/html/iris` => `/var/lib/iris/web`
 - honeybee: serve all needed files (don't rely on nginx)
 - Merged graft into honeybee service

5.50.0 (13 Feb 2024)
 - Fix ONVIF control queueing
 - Escape quotes in JSON values (RWIS locations)
 - ntcip: Use first module row for version (not last)
 - RWIS: improve polling subsurface table (SRF)
 - RWIS: poll subsurface conductivity (SRF)
 - Update earthwyrm for web map
 - Convert graft to axum/tokio

5.49.0 (13 Dec 2023)
 - Fixed blank DMS `msg_user` issue with logging
 - Added basic ONVIF PTZ driver
 - Re-enable camera reset button
 - Remove `camera_preset_store_enable` system attribute
 - Reserved protocol number for GPS Digi WR

5.48.0 (20 Nov 2023)
 - Fixed parsing of gate arm fault codes when multiple faults are present
 - Improved default message pattern selection for DMS message composer
   (java + web UI)
 - Added DB notifications for camera preset updates
 - Replaced DMS fonts with "tfon" format files

5.47.0 (18 Oct 2023)
 - Finished MVP of web UI for DMS (bulb)
 - Reworked PostgreSQL LISTEN/NOTIFY scheme
 - Improved handling of `[tr]` MULTI tags
 - Fixed bug in fontVersionId calculation for large fonts
 - Fixed bug in parsing NTCIP supported MULTI tags
 - Fixed NPE on DMS with no configuration
 - Fixed ifnt import/export bugs

5.46.0 (5 Sep 2023)
 - Changed `sign_config.default_font` to a number
 - Converted all fonts to .ifnt format
 - Removed Java font editor
 - Renamed DMS status->errors JSON to status->faults
 - honeybee: Reworked sign rendering
 - Removed unneeded system attributes: `dms_brightness_enable`,
   `dms_pixel_status_enable`, `dms_reset_enable`, `dms_high_temp_cutoff`

5.45.0 (31 Jul 2023)
 - Improvements to DMS resources within web UI
 - Replaced DevicePurpose/hidden with reserved hashtags
 - NOTIFY listeners when DMS hashtags change
 - Add "errors" to DMS `status` JSON column
 - Changed `sign_message` name generation to avoid clashes
 - Disable caching of MJPEG video streams
 - Added workaround for LX temperature updating issue
 - Fixed free-form text validation issues

5.44.0 (10 Jul 2023)
 - Reworked free-form text validation algorithm
 - Removed `dms_hashtag_view` (use `hashtag_view`)
 - Randomize request-ID generation for LX controllers
 - Fix for creating camera catalogs
 - Clear camera video loss when encoder is not defined
 - Add `msg_pattern` and `msg_line` API resources

5.43.0 (16 May 2023)
 - Check free-form DMS text with `msg_user` permissions
 - Removed `dms_composer_edit_mode` system attribute
 - Removed `dict_allowed_scheme` system attribute
 - Removed `dict_banned_scheme` system attribute
 - Removed unused database columns and views
 - Improved documentation

5.42.0 (7 Mar 2023)
 - Replaced `sign_message` owner with `msg_owner`
 - Removed `sign_message` source (now in `msg_owner`)
 - NTCIP: store `msg_owner` in dmsMessageOwner object
 - Renamed msg priorities to `low_1, low_2 ..., medium_*, high_*``
 - Removed tag reader properties which are now in `settings`
 - Fixed problems in CAP alert processing

5.41.0 (28 Feb 2023)
 - Add support for 6C coalition tolling tags
 - Add tag reader `settings` JSON column
 - Query additional tag reader settings
 - Fixed invalid #tag for active CAP alerts
 - Improved DMS action scheduling
 - Fixed obscure NTCIP parsing errors

5.40.0 (14 Feb 2023)
 - Renamed `beacon_enabled` to `flash_beacon` (DB)
 - Add `flash_beacon` to MsgPattern (remove from DmsAction)
 - Improved UI for editing MsgLine records
 - Clear message composer duration when selecting a new sign

5.39.0 (8 Feb 2023)
 - Replaced DMS sign groups with #hashtags.  **WARNING**: the
   migrate-5.39.sql script requires agency-specific tweaks!
 - Replaced SignText with MsgLine, now tied to MsgPatterns
 - Improved MsgPattern UI (select by sign config)
 - Improved sign pixel preview rendering
 - Abbreviate/filter out msg lines which don't fit on sign
 - For patterns with no msg lines, find substitue pattern for line select
 - Fixed problem with incident auto deploy on "split" signs
 - Fixed NTCIP table corruption for ESS objects

5.38.0 (6 Feb 2023)
 - Always enable "Query Msg" in DMS popup menu
 - Improved handling of NTCIP DMS temperature objects
 - Added VSL device purpose for DMS
 - Fixed problems related to msg pattern changes

5.37.0 (11 Jan 2023)
 - Redesigned DMS [message composer](composer.html), replacing quick messages
   with message patterns and removing many redundant features
 - Simplify message combining, using MULTI only
 - Implement and document [default MULTI values](multi.html#default-values)
 - Fixed weather alert sign group check
 - Improved DMS status JSON
 - Added graphic size constraints to database
 - Improved DMS action tag filtering
 - Added Nebraska beacon protocol
 - Added enhancements for video window layout
 - Added item style for undeployed action plans

5.36.0 (30 Nov 2022)
 - Added DMS `status` and `stuck_pixels` JSON columns to DB
 - Reworked comm priority levels for all protocols
 - Fixed Wavetronix clock sync (PriorityLevel.IDLE)
 - Store modules in controller `setup` as `hw` / `sw` arrays
 - Use software module version for ESS (not `sysDescr`)
 - Query/store modules for NTCIP DMS devices
 - Fixed beacon style ordering
 - Dropped SONAR `sample`/`settings` attributes from `weather_sensor`
 - Renamed `system_attribute` endpoint to `system_attribute_pub`

5.35.0 (18 Nov 2022)
 - Support monitoring externally controlled beacons
 - Added `sonar.protocols` property to control TLS protocol versions
 - Improved REST API documentation
 - Extend "Send Settings" button to also send device settings
 - Store NTCIP module table in controller `setup`
 - Improved support for development with Eclipse

5.34.0 (7 Sep 2022)
 - Added beacon "state" attribute/LUT (replaces "flashing")
 - Fixed beacon verify problem
 - Handle Control-By-Web quirks
 - Improved web UI search/filtering
 - Read device serial numbers in more protocols

5.33.0 (26 Aug 2022)
 - Continue web UI development (beacon control)
 - Expanded ntcip RWIS support (temp, radiation, surface, sub-surface, etc.)
 - Fixed DMS graphic transparency problems
 - Added support for more Control-By-Web models
 - Improved compatibility with Americal Signal and Wanco ntcip signs
 - Fixed natch vehicle event logging
 - Replaced controller table "version" with "setup" (JSONB)
 - Added optional "model" and "serial\_num" to "setup" JSON
 - Fixed a few obscure bugs and exceptions

5.32.0 (11 Apr 2022)
 - Added all controller IO cards to web UI
 - Added "Config" toggle button to web UI
 - Added `weather_sensor_event_purge_days` system attribute

5.31.0 (4 Apr 2022)
 - Added support for NDOR gate arm protocol
 - Added `[standby]` DMS action
 - Added resources to web UI (cameras, video monitors, detectors, tag readers)
 - Changed RWIS status to use imperial units
 - Fixed a couple of natch protocol bugs

5.30.0 (24 Mar 2022)
 - Added controller IO pin links to web UI
 - Added beacons/weather sensors/lane markings to web UI
 - Improved GET requests for REST API
 - Natch: reest watchdog in "send settings"
 - Fixed problems with WYSIWYG editor
 - Fixed problems with subnet checking

5.29.0 (16 Mar 2022)
 - Fixed comm link connection regressions
 - Expanded web-based administration UI
 - Expanded REST API from honeybee / graft
 - Added simpler permission model for web UI
 - Improved caching for REST API

5.28.0 (8 Feb 2022)
 - Added web-based administration UI (alpha)
 - ClearGuide: support limiting travel time by speed limit
 - Fixed SSL handshake stall race on client login
 - Dropped `cabinet` table, moving columns to `controller`
   * `controller_view`: `cabinet` -> `cabinet_style`
   * `controller_loc_view`: `cabinet` -> `cabinet_style`
   * `controller_report`: `type` -> `cabinet_style`
 - Added `connected` to `comm_link` table
 - Removed CommLink status property (use `connected` instead)
 - Renamed Modem `timeout` -> `timeout_ms`

5.27.0 (6 Jan 2022)
 - Added message duration to sign event table
 - Fixed occ check in "exit" DMS action tag
 - Fixed alert UI issues
 - Show supported MULTI tags in `sign_detail_view` (not bit flags)
 - Improved DMS font scripts
 - honeybee: Add `roadway` and `road_dir` to `camera_pub` JSON

5.26.0 (7 Dec 2021)
 - Added [exit backup](exit_backup.md) DMS action tag
 - Fixed wrongly creating SignText when deploying DMS messages
 - Replaced `_device_io` table with `controller_io`
 - Fixed minor bug in binning vehicle events
 - Fixed rare wrong font selection when deploying DMS messages

5.25.0 (19 Nov 2021)
 - Gate arms: skip "Warn Close" state when no action plan exists
 - Gate arms: Add `arm_state` and `fault` to DB gate arm table
 - Gate arms: Add `opposing`, `arm_state` and `interlock` to DB gate arm array
 - Gate arms: Add `fault` to DB gate arm event table
 - Gate arms: Drop configurable open/closed phases from gate arm array table
 - Gate arms: Simplified gate arm states and interlocks (removed TIMEOUT state)
 - Moved `beacon_activation_flag` and `pixel_service_flag` to `sign_detail`

5.24.0 (11 Oct 2021)
 - Added DMS message combining feature
 - Added "Send Settings" button to controller form
 - Added `DETECTOR_OCC_SPIKE_SECS` system attribute
 - Added some pin configuration fields to cabinet style table
 - Fixed rendering of default MULTI tags, like `[cf]`, `[jl]`, `[jp]`, `[fo]`
 - Fixed issues with natch protocol
 - Fixed time zone bug for event stamps

5.23.0 (11 Aug 2021)
 - Added natch protocol for ATC devices
 - Store user messages in DB for DMS
 - Many enhancements to Mayfly server
 - Log DMS pixel and message errors in DB
 - Add millisecond precision for event logging
 - Fixed DMS blanking problem
 - Fixed GStreamer installation bug

5.22.0 (30 Apr 2021)
 - Added vehicle length to vlog file format
 - Added vehicle logging mode for Wavetronix HD protocol
 - Added vehicle logging mode for RTMS G4 protocol
 - Add settings to sign config to exclude fonts and pixel module sizes
 - Add `site_id` and `alt_id` to weather sensors
 - Improved NTCIP compatibility for some vendors
 - Fixed JKS keystore loading problem in newer Java versions
 - Fixed minor alert bugs
 - Write corridor and detector JSON files in honeybee
 - Started mayfly project for serving traffic data
 - Started graft project for serving web client

5.21.0 (26 Feb 2021)
 - Reworked alert configuration (IPAWS)
 - Added `[ta]` DMS action tag (scheduled time actions)
 - Improved sign config names
 - Added sign group form to make it easier to manage members

5.20.0 (12 Feb 2021)
 - Reworked alert processing and user interface (IPAWS)
 - Added ClearGuide protocol driver (contributed by Iteris)
 - Fixes and cleanups to client video handling (contributed by SRF)

5.19.0 (8 Jan 2021)
 - Fixed various IPAWS bugs

5.18.0 (10 Dec 2020)
 - Added IPAWS support for DMS blizzard messages (contributed by SRF)
 - Map segments created by honeybee after any `r_node` changes
 - Start of web-based UI (bulb), using Leaflet JS library
 - Replaced Apache dependency with NGINX, needed for proxying earthwyrm tiles

Upgrade checklist:
 - [ ] Shut off and disable apache server (httpd)
 - [ ] Remove `/usr/share/java/iris-server` symlink
 - [ ] Remove `/var/www/html/iris-client` symlink
 - [ ] For nginx, run: `semanage port -a -t http_port_t -p tcp 3030`
 - [ ] For nginx, run: `setsebool -P httpd_can_network_connect true`

5.17.0 (2 Oct 2020)
 - Fixes for no-response disconnect feature
 - Added `failed` column to `dms_message_view`
 - Added `scale` column to `road_class` table
 - Added NOTIFY triggers for `road` and `road_class`

5.16.0 (3 Sep 2020)
 - Added no-response disconnect feature to comm links
 - Added `comm_config` table for shared configurations between comm links
 - Replaced hard-coded 5-minute value with `long_poll_period_sec` column in
   `comm_config`
 - Replaced `COMM_IDLE_DISCONNECT_` system attributes with `idle_disconnect_sec`
   column in `comm_config` table
 - Replaced `DMSXML_*_OP_TIMEOUT_SECS` system attributes with
   `no_response_disconnect_sec` column in `comm_config` table
 - Replaced `GSTREAMER_VERSION_WINDOWS` system attribute with `gstreamer_version`
   in `project.properties`.  Also added `jna_version` and `gst_java_version`
 - Changed GStreamer auto-download to request zip files from WebStart host
   instead of sonar host
 - Reduce expiration latency for scheduled DMS messages
 - Fixed `msg_feed` bug preventing messages from expiring normally

5.15.0 (6 Aug 2020)
 - Added GStreamer client video enhancements (thanks to SRF)
 - Added `station_id` to `travel_time_event_view`
 - Tweaked auto-fail OCC SPIKE thresholds
 - Fixed corridor calculation bugs

5.14.0 (18 Jun 2020)
 - Auto-fail: add OCC SPIKE check and event type
 - Auto-fail: don't fail abandoned detectors
 - TPIMS: renamed exitId to exitID
 - Send email to `GATE_ARM_ALERT` address when system is disabled
 - Improved corridor mile point calculations
 - Fixed test build errors

5.13.0 (14 May 2020)
 - Added WYSIWYG editor for DMS messages
   (thanks to John Stanley and Gordon Parikh at SRF!)
 - Increased minimum required Java version to 1.8
 - Added `sign_group_text_view` SQL
 - For `inc_feed` protocol, add optional "direction" field
 - Append camera name when notifying "publish" field changes in PostgreSQL

5.12.0 (23 Mar 2020)
 - Added Camera "streamable" flag
 - Fixed camera encoding quality logic bugs
 - Added streambed protocol
 - Renamed "Dictionary" to "Word" in UI and documentation
 - Fixed bug where "prefix page" messages would lose incident association
 - Fixed LCS found on branched corridors for incident deployment

5.11.0 (22 Jan 2020)
 - Added flow streams to manage streambed video streams
 - Don't base camera active / failed status on PTZ controller
 - Incident deployment:
   1. Use landmark for [locxn] tag if cross street is blank
   2. Don't pick `r_node` if cross street and landmark are both blank
   3. Added sample configuration to template DB
   4. Removed [locxa] locator tag
 - Filter out quick messages containing DMS action tags from Quick Msg combo box
 - Reserved streambed comm protocol
 - Improved camera / video monitor documentation
 - Cleaned up database trigger functions

5.10.0 (2 Dec 2019)
 - Use "allowed words" list for abbreviating DMS messages from incidents
 - Removed `abbrev` columns from incident deployment tables
 - Added camera actions to schedule the recall of camera presets
 - Reduced distances for LCS ranges when deploying incidents

5.9.0 (25 Nov 2019)
 - Added `encoder_stream` table associated with `encoder_type`, to allow
   multiple streams per camera
 - Don't clear confirmed incidents when cleared through incident feed
 - Added "external" DMS item style
 - Fixed transparent DMS markers when message sent by other system
 - When checking DMS message CRC, try with and without graphic IDs
 - Moved "hidden" field from `sign_group` to dms
 - Improved MULTI normalization for `sign_text`, `inc_descriptor`, _etc_.
 - Fixed incident deploy `ahead` range
 - Increased incident deploy `near` range to 3 exits
 - Increased maximum width of graphics to 240 pixels
 - Added camera NOTIFY with `publish` payload
 - Fixed problems in database script

5.8.0 (4 Nov 2019)
 - Fixed bug adding $0.25 to all tolling prices
 - Fixed bug ignoring failed detectors for tolling / travel times
 - Include landmark in all `geo_loc` location descriptions
 - Removed `r_node` abandoned column/field
 - Removed `alt_dir` column from road table
 - Removed redundant cached vehicle count values from detector
 - Removed unused `IN` / `OUT` directions
 - honeybee v0.7.0: refactored code, added segments module

5.7.0 (16 Oct 2019)
 - Synchronize clock immediately after collecting a sample with wrong time
   (Wavetronix + HD + G4)
 - Don't record samples with invalid time stamps (Wavetronix + HD + G4)
 - For Houston Radar, set time averaging to match poll period of comm link
 - Canoga: store binned samples in addition to vehicle event log
 - Removed redundant cached occupancy and speed values from detector
 - Incident deployment:
    1. For `tolling` signs, limit suggestions to 1 mile from incident
    2. Don't include exits from CD roads in exit count for range
    3. For new DMS operator messages, keep link if line 1 does not change
    4. Make all lane impact priorities higher than shoulder priorities
    5. Fixed 2-page suggestion bug
    6. Don't suggest DMS if gap between r_nodes is greater than 10 miles
    7. Use linked incident color for DMS pixel panel background

5.6.0 (10 Oct 2019)
 - Add `detector` column to `price_message_event` table
 - Apply minimum toll zone price separately for each zone
 - Fixed toll zones using stale density data when occupancy is missing
 - Separated file mirroring out of honeybee into "mere" project
 - More documentation cleanups and additions

5.5.0 (2 Oct 2019)
 - Converted documentation (administrator guide) to Jekyll markdown
 - Added missing documentation
 - Removed width/height from lane_use_multi table/class
 - Simplified corridor names - don't use `alt dir`
 - Fixed JSON formatting of temperature values
 - Cleaned up ntcip rwis JSON sample data
 - Fixed an NPE in network domain checking on login (for real this time)

5.4.0 (24 Sep 2019)
 - Added JSONB `settings` column to weather_sensor table
 - Added JSON output for NTCIP RWIS operations
 - Fixed an NPE in network domain checking on login
 - Removed SSI protocol driver
 - Improved documentation

5.3.0 (17 Sep 2019)
 - Incident deployment:
    1. Don't suggest DMS too far from an r_node
    2. Don't suggest messages for DMS in opposite direction
    3. Don't search upstream past `COMMON` transition r_node
    4. Treat `INTERSECTION` r_nodes as exits for range
    5. Allow creating 2-page messages for 2-line signs
    6. Changed `ahead` threshold to avoid "1 MILES" problem
    7. Added `allow_retain` flag to road_affix table
    8. Fixed some DMS deployment bugs
 - Added JSONB `sample` column to weather_sensor table
 - Fixed SONAR attribute notifications for weather sensors
 - Draw marker on r_node list cells in corridor list
 - Added `blocked_lanes` and `blocked_shoulders` to incident_view
 - Improved documentation

5.2.0 (10 Sep 2019)
 - Add markers for dedicated purpose DMS (tolling, parking, wayfinding)
 - Fixed some incident DMS deployment bugs
 - Added `incident_clear_advice_multi` and `incident_clear_advice_abbrev`
   system attributes
 - Removed `cleared` from inc_advice table
 - Changed `all_lanes_blocked` incident impact to `lanes_blocked`.  Also,
   `all_lanes_affected` to `lanes_affected`
 - Increased `middle` and `far` incident deployment ranges to 5 and 9 exits,
   respectively
 - Restricted `ahead` range to less than about 1 mile
 - Incident deployment: use dedicated tolling signs when left lane is impacted
   and sign is on same corridor as incident
 - Added `lane use` dedicated device purpose
 - Log FAIL DOMAIN events in client event table
 - Sort comm protocols by description in comm link UI combo box

5.1.0 (4 Sep 2019)
 - Add device purpose for DMS (wayfinding, safety, etc.)
 - Send PTZ stop command to all cameras on startup
 - Reduced ramp meter grow/shrink actions to 50 veh/hour increments
 - Show incident deploy form when incident updates are logged
 - Fixed bugs in new incident deployment features
 - Removed incident descriptor cleared field
 - Removed lanes_blocked and lanes_affected incident impacts
 - Increased middle and far range (number of exits) for incident deployment
 - Use PSA message priority for cleared incidents
 - Rearranged incident advice columns
 - Log user for unexpected blank DMS (NTCIP)

5.0.0 (28 Aug 2019)
 - Switched from Mercurial to Git and moved to github.com
 - "Vendored" scheduler and sonar dependencies
 - Fixed NPE in GeoLocHelper.getParkingRoot
 - Only allow detector samples with reasonable time stamps
 - Fixed auto-fail locked-on clearing bug
 - Reset auto-fail when detector configuration changes
 - Fixed off-by-one error in BitmapGraphic/PixmapGraphic
 - Updated honeybee dependencies
 - Improved compatibility with SierraGX 450 modem

[IRIS 4.x Release Notes](release_notes_4.html)
