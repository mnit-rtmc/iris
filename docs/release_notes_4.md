# IRIS 4.x Release Notes

4.89.0 (18 Apr 2019)

 - Added camera_video_event table for video lost/restored events.
 - Improved DB notify triggers in postgres.
 - Added landmark, lat, lon to r_node_view.
 - Lots of cleanups and improvements to honeybee.

4.88.0 (19 Mar 2019)

 - Don't disable gate arm "Open" button in "Warn Close" state.
 - Lots of cleanups and improvements to honeybee.
 - Added sign_group_view / dms_sign_group_view.
 - Added geo_loc to r_node_view.
 - Added dms_attribute_view to DB.
 - Change DB NOTIFY triggers to use better channel names.
 - Set notify_tag for all geo_loc records.
 - Remove duplicate sign_detail records in migrate script.

4.87.0 (25 Feb 2019)

 - Add supported_tags, max_pages and max_multi_len to sign_detail table.
 - Added override_foreground and override_background to dms table.
 - Increased incident DMS deploy ranges to 5, 10, 20 miles.
 - Fixed NTCIP default colors for alternate color schemes.
 - Fixed keyboard num-pad problem.
 - Fixed problem with SignText name length.
 - Remove duplicate sign_detail records in migrate script.

4.86.0 (24 Jan 2019)

 - Split sign_detail out of sign_config table.
 - Added hardware_make, hardware_model, software_make and software_model to
   sign_detail.
 - Changed DMS composer to only set combo boxes for operator messages.
 - Saturate out-of-range detector samples instead of discarding them.
 - Clear all auto_fail flags when detector_auto_fail_enable is set to false.
 - Fixed a couple of parking area glitches.
 - Improved documentation for setting up mapping.
 - Increased i_user dn max length to 128 characters.
 - Added missing stuff for reporter.
 - Fixed key handling problem for Mac OSX.

4.85.0 (17 Dec 2018)

 - Added rate limiting for email warnings.
 - Added system attributes for purging event tables:
     alarm_event_purge_days, beacon_event_purge_days,
     client_event_purge_days, gate_arm_event_purge_days,
     price_message_event_purge_days, sign_event_purge_days,
     tag_read_event_purge_days
 - Added system attributes for disabling event logging:
      comm_event_enable, meter_event_enable
 - Fixed XML output for large sample values (&gt; 9999).
 - Cleaned up detector sampling code.
 - Rewrote and expanded detection section of administrator guide.
 - Various improvements to administrator guide.
 - Simplified and documented fake detector feature.
 - Fixed message rendering problems in honeybee.
 - Fixed 5-minute detector polling problem.
 - Added Sierra GX modem GPS driver.
 - Added DMS sign message reporter.

4.84.0 (27 Nov 2018)

 - Fixed E6 tag transaction parsing (HOV, etc.)
 - Added last_fail column to detector_auto_fail_view.
 - Allow parking spaces to have head/tail positions.
 - Added labels for parking lane detector types.
 - Added "NO CHANGE" detector auto fail condition.
 - Improved occupancy parsing for Wavetronix HD protocol.
 - Added road_affix table and class (traveler info).
 - Log connection refused events for comm links.
 - Prefer camera number over name in client UI.
 - Added event date/time to incident dispatcher.
 - Added missing privileges in migrate-4.83.sql script.
 - Fixed monstream status message parsing.

4.83.0 (25 Oct 2018)

 - Enhanced honeybee to create DMS gif files.
 - Added Cohu Helios PTZ protocol (Michael Janson).
 - Add login domains to allow restricting network addres ranges for user login.
 - Ignore case of user names when logging in.
 - Log password change (and fail) events.
 - For scheduled messages, use action plan name as owner.
 - Log name of user who blanks a DMS.
 - Query E6 firmware versions.
 - Fixed rare NTCIP query message problem.
 - Fixed some r_node edit mode UI quirks.
 - Increased MAX_WIDTH of graphics to 225.

4.82.0 (3 Oct 2018)

 - Added detector auto-fail feature.  Force fail is now managed by
   administrator.
 - Added detector_event_purge_days system attribute.
 - Enhanced honeybee to listen for PostgreSQL notifications and write json
   files, then scp them to another host.
 - Fixed subtle problems with incident feed protocol.

4.81.0 (25 Sep 2018)

 - Allow incidents to be updated from incident feed.
 - Fixed problem with creating new graphics.
 - Write .sql font file when query settings operation happens on a DMS (ntcip).
 - Add triggers to DB to notify when camera, dms, parking area, incidents and
   fonts are updated.
 - Cleaned up tms-template SQL file.
 - Fixed panic in honeybee.

4.80.0 (17 Sep 2018)

 - Improved e6 query/send settings operations.
 - Changed font glyphs to not use graphics.
 - Fixed device style update when controller/pin changes.
 - Added 15 and 26 pixel high fonts.
 - Fixed problems with editing larger fonts.
 - Send heartbeat msg to monstream every 30 seconds.
 - Reset DMS messages / pixel status when SignConfig changes or device is reset.
 - Fixed NPE problems in DMS UI code.

4.79.0 (30 Aug 2018)

 - Add support for newer E6 firmware.
 - Add support for IAG tags from E6 protocol.
 - Fixed thread resource leak in E6 driver.
 - Store some tag reader configuration settings in DB.
 - Increased sign_group name length to 20 characters.
 - Replaced DMS deploy_time with expire_time.
 - Removed some obsolete DMS stuff.
 - Cleaned up test code.

4.78.0 (21 Aug 2018)

 - Added graphic transparent color.
 - Changed graphic bpp to colorScheme.
 - Fixed a couple of regressions from last version.

4.77.0 (1 Aug 2018)

 - Store DMS color scheme in sign configuration table.
 - Made sign messages associated with a sign configuration.
 - Replaced DMS default font with "override" font.
 - Improved interaction with other NTCIP systems.

4.76.0 (10 Jul 2018)

 - Allow NTCIP portable DMS to update location with GPS.  (thanks to SRF).
 - Added graphic_view and font_view to DB.
 - Fix terminology for "normal vectors".
 - Improved travel time calculation when route passes through C/D roads.
 - Lots of travel time code cleanups.
 - NOTE: due to DB schema changes, all GPS devices will need some additional
         configuration.  See the GPS section of the administrator guide.

4.75.0 (30 May 2018)

 - Added iris_ctl cert subcommand -- switch to 2048-bit RSA keys for sonar.
 - Add amenities enum for parking areas (TPIMS json).
 - Add camera_image_base_url for TPIMS json feed.
 - Improved sonar handshake debugging.

4.74.0 (14 May 2018)

 - Allow assigning a "static graphic" to DMS (for hybrid static/dynamic signs).
 - Added description column to play list / catalog tables.
 - Added auto_expand column to monitor_style table.
 - Fixed deploying devices for incidents near intersection r_node.

4.73.0 (1 May 2018)

 - Added abbreviated MULTI strings to IncDescriptor, IncLocator and IncAdvice
   (removed sign_group).
 - Enabled incident "Deploy" button to send messages to DMS.
 - Added automatic "full-screen" mode to monstream protocol
   (camera_full_screen_enable).
 - Remove "multiple" DMS tab.

4.72.0 (23 April 2018)

 - Added toll_density_alpha / toll_density_beta system attributes.
 - Added alpha, beta and max_price to toll zone table.
 - Make devices inactive when comm link is disabled.
 - Added "Testing" controller condition, with support for ntcip and mndot170
   protocols.

4.71.0 (16 April 2018)

 - Added camera catalogs (list of play lists).
 - Reworked comm idle disconnect code to fix problems with incomplete
   operations.
 - Added action plan group_n for permission stuff.
 - Increased open file / thread limits for tms user.

4.70.0 (10 April 2018)

 - Added modem flag to comm_link table to control timeouts and idle disconnect.
 - Added video monitor group_n for permission stuff.
 - Add location to camera_view in DB.
 - Fixed quirks with gate arm DMS control.

4.69.0 (4 April 2018)

 - Switched gate arm DMS control to use action plans.
 - Fixed problems with DMS messages expiring.
 - Fixed problems with multiple incident feeds.
 - Added new codes for STC driver (gate arm).
 - Added JSON inventory service honeybee (in Rust)

4.68.0 (22 March 2018)

 - Reworked incident feed protocol to allow multiple feeds.
 - Added incident_update_view; replaced incident_view.
 - Fixed travel time bug on otherwise blank message.
 - Preview multi-page messages on quick message dialog.

4.67.0 (20 March 2018)

 - Added preview of quick messages.
 - Added prefix_page and sign_config to quick messages.
 - Merged activation / run-time priorities to msg_priority.
 - Don't replace high priority scheduled message with operator message.
 - Display all cameras on map, regardless of controller condition.

4.66.0 (15 March 2018)

 - Add controller condition to dms_message_view.
 - Removed obsolete "bitmaps" from sign message table.
 - Removed unused ADDCO protocol driver.

4.65.0 (12 March 2018)

 - Added DMS message columns to database table.
 - Improved parking area/space user interface.

4.64.0 (26 February 2018)

 - Improved parking area user interface.
 - Added [pa...] tag for parking area available spaces.
 - Log action plan activation/deactivation/phase changes.
   (Thanks to Michael Darter).
 - Handle more monstream key messages.

4.63.0 (2 February 2018)

 - Added "parking area" to keep track of available spaces.
 - Added parking detector lane type.
 - Added protocol driver for Banner DXM detectors.
 - Allow pausing play lists from monstream.

4.62.0 (26 January 2018)

 - Add ability for monstream clients to switch monitors.
 - Allow proxy authentication using http.proxy property.
 - Fixed client rendering bugs.
 - Added driver for Panasonic CU-950 camera keyboards.
 - Cleaned up video monitor switching code.
 - Added video.proxy property to enable using Live555
   (Thanks to John Stanley).

4.61.0 (22 December 2017)

 - Added play list selection to cam selector tool ('/').
 - Added camera_switch_event_purge_days system attribute.
 - Allow using camera #0 to "blank" monitors.
 - Only send settings once to monstream controllers.
 - Improved administrator guide.
 - Removed Pelco switcher driver.

4.60.0 (11 December 2017)

 - Added persistent camera play lists.
 - Use pelco macro feature to access play lists.
 - Fixed problems with new SONAR group check.
 - Added title_bar column to monitor style.
 - Fixed DMS message composition with "prefix" actions.
 - Changed malfunction thresholds for detector force fail.

4.59.0 (13 September 2017)

 - Fixed a rare client deadlock.
 - Added object "group" to sonar privilege checks.
 - Don't send unnecessary video monitor camera updates.
 - Send settings to all video monitors once per day.
 - Cleaned up client permission code.

4.58.0 (27 July 2017)

 - Added default_font back to DMS to allow overriding the font from sign
   configuration.
 - Fixed runaway camera problem from pelcop driver.
 - Check db version before running migrate script.

4.57.0 (24 July 2017)

 - Added improved weather sensor driver and client tab.
 - Increased max heap for client to 1 GiB.

4.56.0 (18 July 2017)

 - Added GPS drivers to allow device locations to be updated automatically
   (contributed by SRF).
 - Fixed a couple of minor bugs.

4.55.0 (20 June 2017)

 - Parse status messages from monstream protocol.
 - Added camera "video loss" attribute and item style.

4.54.0 (15 June 2017)

 - Added support for NTCIP weather sensor.
 - Increased maximum DMS message length to 1024 characters.
 - Use controller password for camera streaming auth.
 - Fixed a couple of rare bugs.

4.53.0 (24 May 2017)

 - Raised maximum device name length to 20 characters.
 - Don't send scheduled DMS messages before sign has been queried.
 - Fixed a couple of minor NTCIP bugs.

4.52.0 (4 May 2017)

 - Added support for NTCIP 1202 detection.
 - Fixed some minor camera control problems.
 - Improved password strength checking.
 - Cleaned up encoder type DB table.

4.51.0 (24 April 2017)

 - Fixed serious memory leak in comm pollers when network errors occur.
 - Fixed problem with UI "freezing".
 - Added idle disconnect system attributes.
 - Added monitor style table.
 - Added camera number to UI.
 - Convert Cohu PTZ driver to new framework.
 - Improved NTCIP interoperability.

4.50.0 (3 April 2017)

 - Renamed holiday table to day_matcher.
 - Added camera_construction_url and camera_out_of_service_url system
   attributes.
 - Allow font MULTI tags in sign text messages.
 - Fixed bug in "next" camera iteration.

4.49.0 (2 March 2017)

 - Added cam_num column to camera table.
 - Allow multiple video monitors to be assigned to a controller in monstream
   protocol.
 - Fixed invalid monitor number bug in pelco driver.

4.48.0 (16 February 2017)

 - Increased maximum controller password to 32 characters.
 - Allow setting privileges for encoder types.
 - Fixed problems with gate arm privileges.
 - Fixed a NullPointerException in monstream protocol.
 - Use G1 garbage collector (for smoother latency).

4.47.0 (6 February 2017)

 - Fixed server OutOfMemoryException caused by login race.
 - Add editable video encoder_type table.
 - Handle wiper commands in Pelco P driver.
 - Improved camera switch event loging.
 - Fixed an NPE in DmsCellRenderer.
 - Exit client when exception dialog is closed with 'X'.
 - Fixed races in new protocol drivers (BasePoller).

4.46.0 (4 January 2017)

 - Added mon_num column to video_monitor table to allow "mirroring" monitors.
 - Log travel time events to database.
 - Log camera switching events to database.
 - Added quick_message_view to database.
 - Fixed canoga driver quirk.
 - Improved r_node styling.
 - Replace route builder with optimized route finder.
 - Added better workaround for Fedora libjli bug.

4.45.0 (21 December 2016)

 - Documented and improved travel time feature.
 - Fail all controllers on a comm link when disconnected.
 - Added better DMS message priority values for DMS actions.
 - Activate ramp meter beacon when traffic backs up over merge detector.
 - Improved work request menu items.
 - Fixed problems with scheduled DMS actions.
 - Optimized toll route calculation.
 - Added experimental route finder to replace route builder.

4.44.0 (5 December 2016)

 - Added sign_config DB table.
 - Added workarounds for NTCIP firmware font upload bugs.
 - Fixed problems with PREFIX_PAGE feature.
 - Display DMS operation when any properties tab is selected.
 - Removed obsolete SZM metering algorithm.
 - Reserved protocol IDs for GPS / NDOR gates.

4.43.0 (21 November 2016)

 - Enhanced DMS PREFIX_PAGE feature to allow scheduled messages to change after
   operator message is deployed.
 - Added DMS_COMM_LOSS_ENABLE system attribute.
 - Fixed an old NullPointerException on login when an r_node with a station_id
   is deleted.
 - Calculate duration of DMS action message from polling period (x3).
 - Fixed problem with camera "publish" style.
 - Internationalized spell check dialogs.
 - Improved handling of DMS actions.
 - Lots of cleanup to DMS code.

4.42.0 (14 November 2016)

 - Added "owner" field to sign_message table, cleaning up procedure to deploy a
   DMS message.
 - Replaced proxy.host and proxy.port with http.proxy property in server/client
   properties files.
 - Replaced no.proxy.hosts property with http.proxy.whitelist.

4.41.0 (7 November 2016)

 - Store controller firmware version in DB.
 - Cleaned up item style handling.
 - Fixed a couple of NTCIP quirks.

4.40.0 (1 November 2016)

 - Added AXIS ptz protocol.
 - Removed DMS_COMM_LOSS_MINUTES system attribute.
 - "Gray out" drop address on single drop comm links.
 - Cleaned up item style handling.
 - Fix problems in new driver framework.

4.39.0 (24 October 2016)

 - Added Pelco-P driver for keyboards.
 - Fixed problems with new driver framework.
 - Renamed geo_loc "milepoint" to "landmark".
 - Add landmark to description when no cross street.
 - Store camera on video monitor in DB.

4.38.0 (5 October 2016)

 - Simplified user privilege configuration.
   WARNING: database schema migration should be tested carefully for security
   problems.
 - Added MonStream video switching procotol.
 - Added beacon and toll tabs to UI.
 - Added asynchronous driver framework.
 - Converted Vicon PTZ driver to new framework.
 - Improved UI for beacons.
 - Replaced "Tesla" action with "Work Request" menu item.
 - Fixed comm link connection bugs.

4.37.0 (23 August 2016)

 - Added system attributes for camera auth.
 - Added stream_type DB table (from encoder_type).
 - Added enc_mcast (multicast) to camera table.
 - Reserve comm protocol for MonStream.
 - Fixed problems with modems and comm links.
 - Removed dialup_poll_period_mins system attribute.
 - Cleaned up comm link code.

4.36.0 (12 August 2016)

 - Added DMS incident deployment framework.
 - Improved slow traffic warning on DMS (speed).
 - Improved password strength checking.
 - Added camera selection on toolbar, with numpad hotkeys.
 - Fixed Infinova PTZ glitch.
 - Fixed problems in Cohu PTZ driver.
 - Improved modem sharing and states.
 - Fixed problems creating new day plans.
 - Improved user interface consistency.
 - Send default colors to NTCIP signs.
 - Added workaround for Daktronics font upload.
 - Added lots of documentation to administrator guide.
 - Removed some obsolete system attributes.
 - Reworked CommLink polling infrastructure.
 - Cleaned up lots of code.
 - Merged MapBean and geokit into IRIS repository.
 - Replaced beacon marker.

4.35.0 (5 April 2016)

 - Added incident descriptor, locator and advice tables.
 - Added DMS word dictionary with approved and banned list.
   (Thanks to Michael Darter)
 - Added quick messages section and expanded incident deployment section in
   administrator guide.
 - Allow square brackets in DMS messages (doubling `[[`).
 - Refactored MULTI string handling.
 - Improved gate arm system disable logging.
 - Improved incident and ramp meter theme styles.
 - Simplified cabinet style checking for MnDOT protocol.

4.34.0 (15 March 2016)

 - Added ability to verify beacons with a current sensor.
 - Fixed problem with client disconnecting after slow HTTP response.
 - Fixed incident feed bugs.

4.33.0 (7 March 2016)

 - Added "confirmed" flag to incident table.
 - Added incident feed protocol.
 - Added incident section to administrator guide.

4.32.0 (24 February 2016)

 - Added protocol driver for Control By Web (beacons).
 - Allow DMS_COMM_LOSS_MINUTES to be set to 0.
 - Added design document for automatic deployment of DMS from incidents.
 - Cleaned up some error logging stuff.
 - Fixed a couple of problems in addco driver.
 - Cleaned up enum usage.
 - Fixed many Java compiler warnings.

4.31.0 (11 February 2016)

 - Require java 1.7+ for both client and server.
 - Fixed problems with tolling system.
 - Added SamplerSet and VehicleSampler to make detection averaging more general.
 - Improved selection of hidden devices on map.
 - Improved fake detector support.
 - Allow clients to log in without detector / r_node permissions.
 - Fixed some unchecked warnings.

4.30.0 (18 December 2015)

 - Added a couple of event table indexes.
 - Fixed toll price calculation starting at DMS as origin.
 - Added "hidden" flag to sign groups.

4.29.0 (10 December 2015)

 - Added tag_reader_dms_view and dms_action_view.
 - Fixed scheduled message blanking problem.
 - Fixed e6 SeGo CRC calculation.
 - Improved UI to link DMS with tag readers.
 - Fixed ADDCO problem with page time tags.
 - Improved price message logging.

4.28.0 (1 December 2015)

 - Added tag reader markers on client map.
 - Implemented tz MULTI tag for tolling.
 - Added toll_min_price and toll_max_price system attributes.
 - Replaced SignMessage scheduled field with "source".
 - Added price_message_event table and class.
 - Added tag_reader_dms relation table.
 - Log region/agency in tag_read_event table.
 - Fixed an edit mode problem on the R_Node tab.
 - Combined r_node layer and segment layer.
 - Fixed a permission problem with the gate arm whitelist.

4.27.0 (21 October 2015)

 - Added driver for Houston Radar DR-500 doppler radar.
 - Fixed a couple of problems with E6 driver.
 - Added toll_zone field to TagReader.
 - Added tollway field to TollZone.
 - Cleaned up tag_read_event_view stuff.
 - Renamed dms_op_status_enable to device_op_status_enable, and added some
   support for camera PTZ drivers.

4.26.0 (30 September 2015)

 - Fixed a rare deadlock in comm link error handling.
 - Added driver for Transcore E6 tag readers.
 - Added AXIS JPEG encder type.
 - Added DMS reinit detect for dmsxml protocol.
 - Dropped support for insecure SSLv3.
 - Fixed an NPE in ss105 driver.
 - Fixed a couple of Addco DMS bugs.

4.25.0 (17 August 2015)

 - Added driver protocol for addco DMS signs.
 - Fixed various minor NTCIP problems.
 - Added simple algorithm for toll pricing.
 - Renamed travel_time_max_legs system attribute to route_max_legs.
 - Renamed travel_time_max_miles system attribute to route_max_miles.

4.24.0 (25 June 2015)

 - Added basic toll zone DB table and class.
 - Enhanced NTCIP "send DMS message" operation to check graphic IDs and update
   graphics if necessary.
 - Added NTCIP workaround for Daktronics DMS.
 - Refactored NTCIP MIB code to use Java enum.
 - Fixed NTCIP 24-bit graphics to use BGR (not RGB).
 - Added support for closing `[/sc]` (character spacing) MULTI tags.
 - Fixed problems with NTCIP exception handling.
 - Split out SNMP code from NTCIP driver.
 - Added map_extent_name_initial system attribute.
 - Added speed_limit_min_mph, speed_limit_default_mph, and speed_limit_max_mph
   system attributes.

4.23.0 (20 Apr 2015)

 - Improved on client exception handling.
 - Fixed some DMS message rendering issues.
 - Fixed a couple of client race bugs.
 - Fixed a camera playlist bug.
 - Updated G4 driver for new firmware.
 - Merged duplicate CRC implementations.
 - Added camera_wiper_precip_mm_hr system attribute.
 - Removed KML output.

4.22.0 (26 Jan 2015)

 - Redesigned comm link form to allow sorting and filtering controllers.
 - Added optional camera streaming controls and external viewer support.
 - Internationalized error message dialogs.
 - Improved multiple DMS selection speed.
 - Fixed a couple of minor gate arm problems.
 - Fixed some client repainting problems.

4.21.0 (18 Dec 2014)

 - Removed system attributes which allowed specifying a filesystem path (gate
   arm security fix).  Use hardcoded values for kml_filename,
   uptime_log_filename, xml_output_directory, and sample_archive_directory.
 - Replaced controller "active" boolean with "condition" enum (planned, active,
   construction, removed).
 - Removed obsolete station.xml output.
 - Fixed some user interface glitches.

4.20.0 (11 Dec 2014)

 - Added "Create" button below all object table forms to make UI more
   discoverable.
 - Added logging for beacon events.
 - Added optional "Store" button to create quick messages.
 - Added milepoint column to geo_loc table.
 - Fixed "Failed Controller" update problems.
 - Cleaned up and refactored UI code.

4.19.0 (12 Nov 2014)

 - Added generic protocol debugging infrastructure.
 - Removed obsolete vicon switcher driver.
 - Added ramp meter lock "Construction" value.
 - Cleaned up protocol driver API.
 - Refactored mndot170 protocol driver.

4.18.0 (27 Oct 2014)

 - Add support for "internal" DMS beacons to NTCIP protocol.
 - Enable ramp meters to have external advance warning beacons.
 - Enable DMS to have external beacons.
 - Allow DMS actions to specify beacon state.
 - Add "tag reader" objects for tolling sensors.
 - Added automatic camera wiper function when nearby weather sensor indicates
   high precipitation rate.
 - UI cleanups for camera preset functions.
 - Fixed lots of problems with vicon PTZ protocol.
 - Restored controller_report and related database views.
 - Added better DevicePoller abstraction to allow use of NIO for future protocol
   drivers.

4.17.0 (6 Oct 2014)

 - Added camera presets, which can be associated with DMS, ramp meters or
   beacons.
 - Allow client tab ordering via user.properties file.
 - Added device_controller_view.
 - Removed controller_report and related database views.
 - Improved documentation.

4.16.0 (19 Sep 2014)

 - Ahoy Mateys!
 - Added support for wipers in Pelco D protocol.
 - Added focus, iris and wiper control to Vicon protocol.
 - Added focus, iris, wiper and preset control to manchester protocol.
 - Improved queue control for ramp metering.
 - Added alternate (simpler) user interface for camera PTZ and lens functions.
 - Added a database schema test script.
 - Replaced all SQL rewrite rules with triggers.

4.15.0 (28 Aug 2014)

 - Fixed queue estimation in metering algorithm.
 - Improved meter event logging.
 - Cleaned up DMS sign message composer code.

4.14.0 (5 Aug 2014)

 - Simplified start/stop logic for metering algorithm.
 - Cleaned up code for density adaptive metering algorithm.
 - Enhanced document describing density metering algorithm.
 - Allowed density metering for meters on CD roads.
 - Purge comm events after comm_event_purge_days sys attr.
 - Log meter events in meter_event DB table.
 - Purge meter events after meter_event_purge_days attr.
 - Fallback to iris-client.properties for any properties missing from
   user.properties file.

4.13.0 (25 Jun 2014)

 - Added user interface for camera misc. functions.
 - Added Cohu camera PTZ protocol.
 - Cleaned up code for density adaptive metering algorithm.
 - Added document describing density metering algorithm.
 - Fixed a couple of bugs in NTCIP protocol driver.
 - Fixed URI parsing problem in dinrelay driver.
 - Fixed an exception in DMS message rendering.
 - Improved installation scripts.
 - Improved client login code.

4.12.0 (4 Mar 2014)

 - Added beacon actions for action plans.
 - Fixed an NTCIP error with brightness tables.
 - Added support for beacons to dinrelay protocol.
 - Cleaned up SQL template functions.

4.11.0 (19 Feb 2014)

 - Renamed "Warning Sign" devices to "Beacons".
 - Use comm. link poll period for alarms, weather stations, DMS, Beacons and
   Gate Arms.
 - Added lots of content to administrator guide.
 - Don't recommend invalid lane indications for DLCS.
 - Fixed problems in RPM .spec file.
 - Removed unused ramp metering algorithm.
 - Added "System disable" button for gate arms.
 - Cleaned up more code.

4.10.0 (29 Jan 2014)

 - Added poll_enabled and poll_period to CommLink.
 - Added client "Edit Mode" toggle button.
 - Fixed a couple of client administration bugs.
 - Added support for more NTCIP MULTI tags.
 - Fixed various gate arm problems.
 - Added client IP whitelisting for gate arm control.
 - Improved threading on client.
 - Lots of code cleanups.

4.9.0 (10 Oct 2013)

 - Updated SONAR wire-protocol to allow null references to be encoded properly.
 - Added "styles" attribute to LCS arrays.

4.8.0 (7 Oct 2013)

 - Added Gate Arm device types, with new user interface.
 - Added STC gate arm driver (HySecurity STC).
 - Removed hard-coded sample file extensions by adding
   SampleArchiveFactory.hasKnownExtension.
 - Added incident_view to database.
 - Added infinova debug log.
 - Fixed ramp meter showing as "METERING" when failed.
 - Updated SQL template to use more modern techniques.
 - Cleaned up user interface code.
 - Added simple protocol emulator (protest) in D.

4.7.0 (29 May 2013)

 - Added "abandoned" flag to r_nodes.
 - Added check constraints for r_node lane shift values.
 - Added SQL script to "center" r_node shift values.
 - Use "lane configuration panel" for incident impact widget.

4.6.0 (22 May 2013)

 - Renamed sign_text priority to rank.
 - Fixed widget rendering problems on Windows.
 - Improved lane configuration on LCS (shoulders).
 - Added "Center Map" menu item to some objects.
 - Cleaned up client component code.

4.5.0 (14 May 2013)

 - Improved font editor user interface.
 - Added 15 standard DMS fonts to database template.
 - Updated LCS deployment policy for incidents.
 - Improved client user interface style consistency.
 - Improved styles updates for DMS and ramp meters.
 - Added cabinet_view to database.
 - Fixed a timestamp problem in RTMS G4 driver.
 - Fixed parsing of NTCIP Counter objects.
 - Improved handling of invalid NTCIP state changes.

4.4.0 (24 April 2013)

 - Improved sign_event_view and recent_sign_event_view.
 - Fixed a bug in fake detector parsing.
 - Improved map tile loading.
 - Improved installation procedure.
 - Fixed quirk in DMS sign pixel panel sizing.
 - Fixed problems with K-adaptive metering algorithm.
 - Added "styles" attribute to meter and DMS.
 - Added some missing NTCIP 1203 objects.
 - Replaced "dms" and "lcs" debug logs with "ntcip".
 - Updated U of M density metering algorithm.

4.3.0 (8 April 2013)

 - Added custom [slow...] MULTI tag for "slow traffic ahead" type warnings.
 - Fixed bug which prevented updates to sign text multi strings from being
   stored in database.
 - Removed more unnecessary attribute updates from server.
 - Cleaned up detector_view in database.
 - Added "units" subpackage with classes defined for Distance, Interval, Speed
   and Temperature.  Converted code to use units instead of raw int and float
   values.
 - Fixed problem with DMS page on/off time and multiple page messages.
 - Fixed a bug in LCS array administration.
 - Fixed a serious bug in dinrelay protocol.

4.2.0 (17 January 2013)

 - Replaced camera PTZ button panel with on-screen PTZ control (mouse click
   pan/tilt; mouse wheel for zoom).
 - Added alarm trigger time attribute.
 - Reduced resource usage of client program.
 - Removed unnecessary attribute updates from server.
 - Fixed resource leak problems with client.
 - Lots of code cleanups.

4.1.0 (2 January 2013)

 - Store vehicle classification data for ss105, ss125 and g4 drivers.
 - Updated Wavetronix HD driver for newer firmware.
 - Fixed whole class of NullPointerException problems caused by deleted SONAR
   objects.
 - Fixed client JNLP problem with proxy servers.
 - Use concurrent data structure for sonar object caches; no locking needed in
   most cases.  This should improve client responsiveness.
 - Added debug logs for each scheduler (thread).
 - Continued major cleanup of comm protocol drivers.
 - Lots of other code cleanups and improvements.

4.0.0 (14 November 2012)

 - Updated to use SystemD instead of old SysV init.
 - Server now depends on OpenJDK 1.7
 - Initial configuration of server now is performed by iris_ctl script (see
   administrator guide).
 - RPM is now "noarch", and can be used on either 32-bit or 64-bit systems.
 - Fixed connection problems with G4 driver.
 - Started major cleanup of protocol drivers.
 - Fixed a couple of minor bugs.

[IRIS 3.x Release Notes](release_notes_3.html)
