# IRIS 5.x Release Notes

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

5.26.0 (7 Dev 2021)
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
 - Added DMS [message combining](dms.md#message-combining) feature
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
