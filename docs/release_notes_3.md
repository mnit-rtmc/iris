# IRIS 3.x Release Notes

3.150.0 (24 September 2012)

 - IRIS can now manage user passwords directly &mdash; LDAP is still supported,
   but no longer required.
 - Added support for 24-bit color DMS graphics.
 - Fixed bug (sign not blanking) when invalid scheduled message is active.
 - Fixed problems with client reading sensor XML file for traffic segment map.
 - Reserve protocol id for Axis 292.

3.149.0 (31 August 2012)

 - Added PREFIX_PAGE activation priority for scheduled DMS actions.  Scheduled
   messages using this will be "prepended" to each page of all user deployed
   messages.
 - Fixed a bug which caused traffic map "segment layer" to sporadically turn all
   gray for 30 seconds or more.
 - Refined DMS "message composer" user interface.
 - Changed user properties file to always be located at ~/iris/user.properties
   (also works in Windows).
 - Added "scale" property to user.properties file -- it allows client user
   interface to be scaled from 0.25 to 4.0 scale.
 - Removed dependencies for tms-log and tdxml projects.

3.148.0 (10 August 2012)

 - Convert all geographic locations to lat/lon (instead of UTM).
 - Internationalized all (?) text strings in client user interface.
 - Fixed a bug which prevented scheduled DMS messages from blanking.
 - Reorganized client subpackage classes.
 - Added preliminary support for scaling entire user interface.
 - Enhance G4 driver to handle up to 12 lanes.

3.147.0 (25 July 2012)

 - Added new protocol driver for low-cost DLI DIN relay.  Currently, it supports
   LCS devices only.
 - Improved LCS user interface to show lane configuration at site of LCS array.
 - Add SSI protocol driver for RWIS stations.
 - Cleaned up code for K-adaptive metering algorithm, fixing a couple of bugs in
   process.
 - Improved parsing of all text line protocols.
 - Added "Basic authorization" support to all HTTP protocols, using controller
   password (specified as name:password).
 - Fixed problem with MULTI strings with `[pb..]` or `[cr..]` tags being treated
   as blank messages.
 - Fixed a couple of bugs in EIS G4 driver.
 - Fixed a minor client map layer rendering bug.
 - Allow i18n files to "cascade" property lookup from more specific to more
   generic.  This removes a lot of duplication in English localization files.
 - Replaced temp_fahrenheit_enable system attribute with client_units_si.
 - Fixed problem parsing data from newer OSI ORG-815 sensors.

3.146.0 (2 July 2012)

 - Totally reworked queue management for K-Adaptive metering algorithm,
   including diagrams.
 - Fixed ramp meter action plan scheduling bug.
 - Added UMN density metering algorithm.
 - Added more content to administrator guide.
 - Reserve new comm protocol IDs.

3.145.0 (22 May 2012)

 - Added lots of new content to Administrator Guide.
 - Lots of cleanups to enable sample data binning periods other than 30 seconds
   (incomplete).
 - Fixed some sample data problems with Canoga protocol.
 - Fixed a regression in sample data logging (infinite loop).
 - Cause controllers to fail when comm link is not valid.
 - Cleaned up client user interface (side pane, tabs).
 - Updated K-Adaptive metering algorithm with better queue management.
 - Cleaned up some weird coding issues (interfaces vs. static import).
 - Added msg_feed_verify system attribute to allow skipping message verification
   (in trusted environments).

3.144.0 (3 April 2012)

 - Added driver for EIS G4 vehicle detection system.
 - Added more information to DMS xml output.
 - Added (very basic) "comm" tab to client interface.
 - Cleaned up code for VDS archiving.

3.143.0 (23 February 2012)

 - Added new K Adaptive Metering algorithm.
 - Cleaned up code for variable speed advisories.
 - Fixed LCS status not showing up as "User Deployed" when it was also scheduled.

3.142.0 (20 January 2012)

 - Added client_event table in database to record client
   connect/disconnect/authenticate events.  Also, removed these messages from
   iris.stderr log.
 - Fixed a NullPointerException on systems which do not collect traffic data
   samples.
 - Added "bypass_authentication" option to sonar.ldap.urls property.

3.141.0 (10 January 2012)

 - Added a state attribute to modems, and a client widget on tool bar to monitor
   it.
 - Reworked operation and message poller infrastructure.
 - Changed operation queue to disallow "equal" operations.
 - Fixed blanking sticky scheduled DMS messages.
 - Audited and cleaned up NTCIP operations.
 - Simplified manchester PTZ "streaming" functionality.
 - Fixed a rare client NullPointerException.
 - Increased maximum server heap size to 512M.

3.140.0 (3 January 2012)

 - Fixed a problem with scheduled blank DMS messages.
 - Fixed some problems with dial-up modem operation.
 - Improved robustness of NTCIP pixel test operation.
 - Provide better feedback when NTCIP font upload fails.
 - Added a couple of DMS font scripts.

3.139.0 (23 November 2011)

 - Added a "sticky" flag to action plans to make messages remain even after
   communication loss or power failure.
 - Added VSA_MAX_DISPLAY_MPH system attribute to limit the largest speed to use
   for VSA messages.
 - Improved algorithm to send fonts to DMS.  Also, fixed a rare exception in
   font algorithm.
 - Improved MULTI parsing and unit tests.

3.138.0 (18 November 2011)

 - Added a "scheduled date" to time actions as an alternative to using a day
   plan.
 - Allow DMS MULTI string messages to contain lowercase letters if defined in
   the font.
 - Added support for more MULTI tags: pb (page background color), cr (color
   rectangle), and sc (character spacing).
 - Cleaned up code dealing with MULTI parsing and rendering.
 - Fixed a modem config bug (removed extra characters).

3.137.0 (15 November 2011)

 - Added configurable phases to action plans, replacing the hard-coded
   "undeployed", "deployed", etc. states.
 - Replaced action plan toolbar widget with a full action plan tab.
 - Cleaned up lots of quirks in "Plans and Schedules" form.
 - Removed "period" (AM/PM) from holiday table.  Just use alternate day plans
   for this functionality.
 - Added some sample fonts in sql/fonts/ directory.

3.136.0 (3 November 2011)

 - Ramp meter "timing plans" replaced with meter actions, linked to action
   plans, as used with DMS.  Special care must be taken with day plans when
   upgrading -- only if ramp metering is being used.  The migrate script assumes
   two day plans exist, DEFAULT_AM and DEFAULT_PM.
 - A couple of bugs fixed for replacing incidents.

3.135.0 (17 October 2011)

 - Allow incidents to be "edited" (replaced), changing location, camera or
   detail fields.
 - Record initial incident impact by changing trigger for incident_update table.
 - Fixed user interface problems with map point selectors for incidents and
   r_nodes.
 - Fixed some user interface quirks on the incident tab.
 - Traffic sample data archive now includes district name in archive directory.
   Some minor adjustments are required on server to keep data archive consistent.
 - Added controllers and comm_links to XML config file.
 - Removed "inactive" and "no controller" DMS status from user interface.
 - Increased the DMS brightness level feedback adjustment.
 - Fixed problems with video monitor "camera" attribute.

3.134.0 (4 October 2011)

 - Improved dial-up modem support.
 - Added "dialup_poll_period_mins" system attribute to control polling period
   for dialup devices.
 - Fixed problems with failed controller table.
 - Sign pixel panel can better cope with invalid sign dimensions returned from a
   sign.
 - Fixed a serious bug in Infinova PTZ driver.

3.133.0 (26 September 2011)

 - Added Infinova D camera control protocol (wrapping Pelco D in a tcp stream).
 - Added basic dial-up modem support.
 - Allow specifying tcp:// or udp:// connection in comm link URI.

3.132.0 (19 September 2011)

 - Store controller fail time stamp in database so it will not be lost on a
   server restart.
 - Sort failed controller list by fail time, and allow user to sort based on
   other columns as well.
 - Improved map tile performance.
 - Allow msgfeed protocol to be used through an HTTP proxy server.  New
   properties in server.properties file.
 - Lots of fixes and improvements for canoga protocol.

3.131.0 (30 August 2011)

 - Added "active" field to r_nodes.  Deactivated r_node detectors will be
   ignored for segment maps, travel time, variable speed advisory and ramp
   metering.
 - Fixed user interface problems with sign text with included MULTI tags.

3.130.0 (26 August 2011)

 - Fixed problems with new msg_feed protocol.
 - User interface now displays full-color view of DMS which are capable of
   full-color operation.
 - Allow some MULTI tags to be included on sign text message libraries.  These
   tags include foreground color `[cf]` and line justification `[jl]`.

3.129.0 (18 August 2011)

 - Converted AWS protocol into more general "msgfeed" protocol.  Messages can be
   read from an external http server and deployed by an action plan.  Removed
   system attributes: dms_aws_log_enable, dms_aws_log_filename,
   dms_aws_read_offset.
 - Added "Status" field for DMS and LCS tabs, which displays error messages when
   signs need maintenance.
 - Added dms_comm_loss_minutes system attribute to control how long before a DMS
   will blank after no communication.
 - Allow some system attributes to be changed without needing to restart client
   or server.

3.128.0 (16 May 2011)

 - Map extent buttons now use lat/lon instead of UTM easting/northing.
   Old map extent buttons will need to be recreated.
 - Changed default XML output directory to /var/www/html/iris_xml/.
 - Fixed error handling when writing XML output files.
 - Added occupancy to det_sample.xml.gz and stat_sample.xml.gz.
   Also removed "D" prefix from detector names.
 - Changed coordinates in incident.xml to lat/lon; gzipped file as well.
 - Replaced dms.xml with sign_message.xml.gz.

3.127.0 (12 May 2011)

 - Consolidated daily XML output to `{district}_config.xml.gz`.  Filename
   dependent on new "district" property from iris-server.properties.
   Coordinates are now lat/lon instead of easting/northing.
 - Renamed video.backend.host0 property to video.host and video.backend.port0 to
   video.port for configuring video servlet access.
 - Added ability to get digital video streams directly from encoder if
   video.host is not specified.  New camera encoder_type is used to determine
   the stream type.
 - MPEG4 video streams using RTSP are supported now using gstreamer-java.
   Gstreamer-java is a new dependency for building IRIS.  It is optional on
   deployment, unless MPEG4 video streams are needed.
 - Replaced camera_num_video_frames system attribute with
   camera_stream_duration_secs.

3.126.0 (26 April 2011)

 - Changed client base map to use tiles instead of shapefiles.  Map is now
   projected to spherical mercator coordinates instead of UTM.
 - Splash screen removed -- startup is very fast now.
 - Fixed problems with NTCIP maintenance status for signs.
 - Fixed bug which prevented IRIS from restarting when the Java virtual machine
   crashes.
 - Timeout LDAP authentications after 5 seconds.
 - Fixed serious SONAR stalling bug.
 - Fixed an infinite loop on corridor milepoint calculation.
 - Fixed bug in dmsxml protocol which caused sign messages to be deleted
   prematurely.
 - Bypass pixel error checks when blanking signs.
 - Fixed infinite panning problem in manchester PTZ protocol.
 - Added camera_id_blank system attribute to specify a camera to use when
   unpublishing a camera.
 - Added dms_pixel_maint_threshold system attribute to specify how many pixels
   need to be failed before a sign goes to maintenance status.

3.125.0 (13 December 2010)

 - Renamed "Roadway" tab to "R_Node" and improved user interface for managing
   r_nodes and detectors.
 - Added "R_Node" button to detector form to look up the r_node for a detector.
 - Added station form, which includes a list of stations.
 - Removed unused attributes from Station objects.
 - Fixed a problem with camera stream exceptions.

3.124.0 (30 November 2010)

 - Fixed lots of problems with segment map.
 - Improved client log-in time.
 - Fixed a data collection bug in SmartSensor drivers.
 - Fixed default font handling for NTCIP.
 - Cleaned up some exception handling.

3.123.0 (28 September 2010)

 - Added default font to DMS objects.

3.122.0 (24 August 2010)

 - Fixed a couple of minor problems with the variable speed advisory code.
 - Fixed a problem with precipitation sample data files.
 - Refactored AwsStatusPanel widget.
 - Added a couple of features to user properties file.
 - Fixed a number of client bugs.
 - Renamed dms_aws_read_time system attribute to dms_aws_read_offset.
 - Fixed a rare SQL exception related to r_nodes.
 - Fixed an exception with vehicle event logging.

3.121.0 (5 August 2010)

 - Added a weather sensor device abstraction.
 - Added protocol to communicate with Optical Scientific ORG-815 precipitation
   sensor.
 - Refactored sample data archiving code to allow sample periods other than 30
   seconds.
 - Added sample_archive_enable and sample_archive_directory system attributes.
 - Added daily job which moves sample data into zip archives.
 - Fixed a couple of minor problems with variable speed advisory algorithm.
 - Added time abstraction to allow IRIS to be run in conjunction with a
   simulator.
 - Started a new IRIS Administrator Guide.

3.120.0 (13 July 2010)

 - Made updates to variable speed advisory (VSA) algorithm.
 - Fixed design problem with Completers and data polling.  Now, one unresponsive
   operation won't freeze the rest of the data collection.
 - Fixed a rare problem with smart sensor 105 protocol.
 - Fixed bug in XML output with unescaped quotes and apostrophes.
 - Cleaned up map layer scaling code.
 - Allow DMS to be used if less than half of the power supplies are failed.
 - Updated scheduler, mapbean, trafmap and sonar dependencies.

3.119.0 (16 June 2010)

 - Added capability abstraction for easier managing of user permissions in the
   "Users and Roles" form.
 - Fixed various problems with the variable speed advisory (VSA) algorithm.
 - Don't replace proxy selector provided by Java Web Start if a proxy server is
   not specified in iris-client.properties.
 - Fixed a couple of quirks in the DMS and LCS interfaces.
 - Fixed a bug which prevented LCS from being assigned to an LCS array.
 - Fixed a client deadlock in the font editing form.

3.118.0 (8 June 2010)

 - Implemented variable speed advisory (VSA) algorithm.
 - Fixed a bug with sync_action plans for certain controller errors.
 - Fixed a client deadlock for AWS.
 - Cleaned up several interface quirks with DMS tab.
 - Fixed a bug in SS105 protocol.

3.117.0 (18 May 2010)

 - Added DMS brightness feedback logging and brightness table update.
 - Added DMS photocell status table.
 - Removed unused DMS lamp status stuff.
 - Fixed a couple of ...IndexOutOfBoundsExceptions.

3.116.0 (11 May 2010)

 - Cleaned up comm package classes and interfaces.
 - Added a couple of missing database views.
 - Added support for querying NTCIP 1203v2 power status.
 - Fixed exception dialog bug which allowed it to get hidden behind main client
   window.
 - Cleaned up a few FIXMEs in the code.

3.115.0 (20 April 2010)

 - Removed east_off and north_off attributes from GeoLoc objects.  Also,
   renamed freeway attribute to roadway and free_dir to road_dir.
 - Removed some duplicate code for coordinate conversion.
 - Fixed problems with handling client authentication failure.
 - Fixed several minor problems.
 - Cleaned up relationship between database lookup tables and enum declarations.

3.114.0 (6 April 2010)

 - Renamed incident "debris" to "hazard".
 - Added detail field to incidents.
 - Started work on variable speed advisory algorithm.
 - Fixed a couple of minor problems.
 - Fixed a couple of NullPointerExceptions.
 - Fixed leaks when logging out of client.

3.113.0 (11 March 2010)

 - Updated sonar dependency to 3.0.  This fixes several serious bugs and greatly
   improves client login time.
 - Added an incident deploy form, which recommends LCS indications for an
   incident.
 - Cleaned up incident user interface a bit.
 - Changed LCS sort order to travel direction.
 - Fixed a couple of permission problems in UI.
 - Improved map user interface with respect to tabs and selecting devices.
 - Added "automatic" layer visibility state.  This makes visibility dependent on
   zoom level and currently selected tab.
 - Removed videoclient dependency.  Relevant code has been copied into iris, and
   some video glitches have been fixed.
 - Changed r_node markers to a better icon.

3.112.0 (22 February 2010)

 - Added lane shift attribute to LCS arrays.
 - Improved map icon scaling -- map_icon_size_scale_max attribute controls icon
   sizes.
 - Added buttons to change size of DMS cell renderers.
 - Fixed segment layer handling of on- and off-ramps.
 - Lots of cleanups in user interface: DMS, LCS, incidents.
 - Fixes to allow running client in 1024x768 resolution.
 - Renamed dms_poll_freq_secs system attribute to dms_poll_period_secs.
 - Added dms_aws_read_time system attribute.

3.111.0 (3 February 2010)

 - Added lane_type to incidents, to allow incidents on exit ramps, etc.
 - Improved multiple DMS selection handling.
 - Improved client permission checking for some widgets.
 - Fixed a couple of serious bugs in client code.
 - All devices now have a "Select Point" button on the location tab.

3.110.0 (20 January 2010)

 - Allow larger graphics to be used on DMS (144x144).
 - Add a "sign group" attribute to quick messages.  Now, the quick message
   widget is displayed for any sign in a group which has associated quick
   messages.  Removed dms_qlib_enable system attribute.
 - Changed system attributes related to DMS page times.  Renamed
   dms_pgontime_selection_enable to dms_page_on_selection_enable.  Also,
   renamed `dms_page_[on|off]_secs` to `dms_page_[on|off]_default_secs`.  Added
   dms_page_on_min_secs and dms_page_on_max_secs.
 - Added some changes to make client run better on a 1024x768 screen.
 - Improved client permission checking for some widgets.
 - Lots of cleanups in client code.

3.109.0 (7 January 2010)

 - Cleaned up client permission checking for tables, view menu and tabs.
 - Simplified system attribute form.
 - Garbage-collect sign message objects which are no longer in use.

3.108.0 (17 December 2009)

 - Lots of cleanups in LCS user interface for error cases.
 - Renamed dms_intermediate_status_enable system attribute to
   dms_op_status_enable.
 - Fix building 32-bit rpm from 64-bit host.

3.107.0 (10 December 2009)

 - Added controller error counters for better diagnostics.
 - Fixed client deadlock when updating object attributes.
 - Added dms_render_size system attribute to allow selecting smaller DMS items
   in summary list.
 - System attributes now indicate whether client or server needs restarting
   after change.  Also, non-default values are displayed in bold.
 - Multiple fixes for problems in DMS user interface.
 - Various improvements to AWS and dmslite protocols.
 - Set LCS maintenance status when appropriate.
 - Clear controller error status when no NTCIP short error flags are set.
 - Added cross street to incident user interface.

3.106.0 (29 October 2009)

 - Added user interface to create and manage incidents.
 - Log changes to incidents in incident_update table.
 - Added incident.xml file with current active incidents.
 - Fixed a couple of client lockup bugs.
 - Cleaned up some quirks in segment layer.
 - Fixed ramp meter status display when a communication failure happens while
   metering.
 - Fixed home extents problem for a couple of layers.

3.105.0 (16 October 2009)

 - Day plans added to allow scheduling time actions on specific days.
 - Action plans now have "deploying" and "undeploying" states, with configurable
   duration.
 - DMS actions now have separate activation and run-time priorities.

3.104.0 (13 October 2009)

 - Added Wavetronix SmartSensor HD (125) protocol driver.
 - Cleaned up Wavetronix SmartSensor 105 protocol driver, fixing a couple of
   bugs.
 - Log changes to LCS indications in event.sign_event database table.

3.103.0 (6 October 2009)

 - Fixed a couple of races which caused device status to be displayed
   incorrectly on client.
 - Enabled LCS indication messages to be reused if present in NTCIP signs.  This
   shortens the time it takes to deploy LCS.
 - Changed sign blanking for NTCIP DMS to use "blank" memory type instead of
   settting remaining time to zero.
 - Verify that the NTCIP message CRC is correct before activating a message.
 - Made LCS icons larger on map interface.
 - Moved "quick messages" setup to a separate form (no longer a tab in DMS
   properties form).

3.102.0 (1 October 2009)

 - Added lane type for dynamic shoulder lanes.
 - Added comm_link_view to public database schema.
 - Query NTCIP message table status.

3.101.0 (29 September 2009)

 - Fixed a client race which caused DMS and LCS lists to be initialized
   incorrectly.
 - Don't leave zombie python subprocesses around after client quits (joystick
   handling).
 - Cleaned up client exception handling.  Only create a single dialog for
   displaying messages and exceptions to the user.
 - Cleaned up LCS status for communication failures.
 - Fixed client permission problem for deploying action plans.
 - Moved both remaining database tables from public schema into iris schema.

3.100.0 (24 September 2009)

 - Added "Sync Actions" flag to action plans.  This checks if all actions can be
   done before allowing the plan to be deployed.  Also, messages from DMS
   actions on a "Sync Actions" plan will remain even if communication is lost.
 - Lane actions now have an "on deploy" flag, so that the logic can be reversed
   if necessary.
 - Executing action plans no longer depends on collecting 30-second sample data.
 - If there are two or more scheduled messages for a DMS, the higher priority
   message will be displayed.
 - Cleaned up some LCS quirks in NTCIP protocol.

3.99.0 (21 September 2009)

 - Fixed a server deadlock which could happen when changing a comm link url
   (race condition).
 - Fixed a sonar race which could cause the client to hang during login.
 - Cleaned up server TIMER job scheduling.  Isolated some unrelated jobs from
   each other.  Disk writing jobs were moved to FLUSH scheduler.
 - Moved holiday, lane_type, and video_monitor tables to iris schema.
 - Added "low visibility" lane use indication.
 - Fixed client page time display regression.

3.98.0 (14 September 2009)

 - Added "lane marking" devices to control in-road lighting (LEDs).  This allows
   lane markings (stripes) to change dynamically, controlled by an action plan.
 - Fixed a bug which caused DMS actions to not be deployed on weekends and
   holidays.

3.97.0 (10 September 2009)

 - Replaced incident display in client with new IRIS incidents.  Server and
   database parts have been implemented, but user interface is still in
   development.
 - Fixed bugs in client sign message composer user interface code.
 - Added a button to query the configuration of a sign.
 - Added operation "intermediate" status for dmslite protocol.

3.96.0 (1 September 2009)

 - Improved scaling for client maps.  Devices, such as DMS or ramp meters, now
   scale properly.
 - Added better logging for communication errors.  A new debug log called "comm"
   allows detailed debugging.
 - Moved a few more database tables to the iris schema.
 - Fixed some bugs.

3.95.0 (19 August 2009)

 - Replaced "gpoly" shapefile layer with a dynamic "segment" layer based on
   r_nodes.  The segment layer can display lane-by-lane data.  The
   iris-client.properties file needs a new property called "tdxml.detector.url",
   which points to the det_sample.xml.gz file.
 - Rewrote MULTI renderer to fix some bugs and support the `[tr]` tag (text
   rectangle).
 - New system attributes: dms_composer_edit_mode, actionplan_toolbar_enable,
   xml_output_directory.
 - Fixed some bugs.
 - Added new "geokit" dependency, for coordinate conversion (lat/lon &lt;-&gt;
   utm).

3.94.0 (1 August 2009)

 - Fixed several serious bugs in 3.93.
 - Replaced "view" layer (shapefile) with more dynamic MapExtent objects.
 - Added a controller "password" field, which can be used to specify the SNMP
   community name for the NTCIP protocol.
 - Reduced memory usage related to client mapping by up to 30% (70 MB).
 - Optimized DMS status summary list drawing code.
 - Cleaned up "View" menu by adding sub-menus.

3.93.0 (15 July 2009)

 - Travel time timing plans have been replaced with DMS action plans.
 - Sign messages now have activation and run-time priority, plus a "scheduled"
   flag.  Added more priority levels for use with DMS action plans.  LCS
   indications now have different activation priorities.
 - If a DMS has a scheduled message, blanking the sign will send the scheduled
   message instead.
 - Rewrote the MULTI string parsing and formatting code.
 - Fixed some bugs.

3.92.0 (8 July 2009)

 - Added "action plans", which are more flexible than timing plans.
 - Travel time timing plans still exist, but will be removed in the next
   version.  DMS action plans can also work as travel time plans.
 - Action plans can be deployed using a simple widget on the toolbar.
 - Fixed a few bugs.
 - Tms-log dependency updated to 1.14.1.

3.91.0 (30 June 2009)

 - Lots of code cleaned up for easier maintenance.
 - Bug fixes and improvements to dmslite and aws protocols.
 - Toolbar added to map, with mouse coordinates, AWS status panel and zoom
   buttons.
 - Added "clear" button and optional page on time spinner to DMS sign message
   composer user interface.
 - Added a system attribute (operation_retry_threshold) to control the number of
   retries for each comm operation.
 - SONAR dependency updated to 2.12.1.
   Tms-log dependency updated to 1.14.0.
   Tdxml dependency updated to 1.5.3.
   Mapbean dependency updated to 4.3.3.
   Videoclient dependency updated to 1.33.4.

3.90.0 (23 June 2009)

 - Roles can now contain multiple "privileges".  Individual roles can be enabled
   and disabled.
 - SONAR dependency updated to 2.11.4.

3.89.0 (17 June 2009)

 - Added support for NTCIP 1203v2 graphics.
 - LCS arrays are now supported by NTCIP protocol.
 - Cleaned up protocol packages.
 - Cleaned up some old client code.

3.88.0 (1 June 2009)

 - Lots of code moved around for better organization.
 - Added "quick message" functionality for DMS messages.
 - Cleaned up NTCIP protocol package.  Added a bunch of unit tests.
 - Cleaned up client permission checking code.  Hard-coded role names have been
   removed.

3.87.0 (22 May 2009)

 - Lots of code moved around for better organization.
 - Standard PostgreSQL JDBC driver now required.  Old postgesql.jar dependency
   dropped.
 - Added more XML output for TMS objects.
 - Added Pelco video switch protocol.
 - Added Vicon PTZ camera protocol.
 - Started work on unit testing infrastructure.
 - Fixes for Caltrans D10.

3.86.0 (20 May 2009)

 - Lane-use control signals converted to SONAR.  All objects have now been
   converted; no more RMI.
 - Lots of old code related to RMI has been removed.
 - ObjectVault dependency has been removed.  Standard PostgreSQL JDBC driver
   should work (untested).
 - A few regressions introduced in 3.85.0 have been fixed.

3.85.0 (28 April 2009)

 - System attributes now have tooltips describing their function.
 - System attributes have been changed to be more consistent.

   Old Attribute                    | New Attribute
   ---------------------------------|------------------------------
   cameraviewer_num_preset_btns     | camera_num_presest_btns
   cameraviewer_num_video_frames    | camera_num_video_frames
   agency_id                        | incident_caltrans_enable
   caltrans_d10_op_timeout_secs     | dmslite_op_timeout_secs
   caltrans_d10_mod_op_timeout_secs | dmslite_modem_op_timeout_secs
   kml_create_file                  | kml_file_enable
   menu_help_trouble_ticket         | help_trouble_ticket_enable
   menu_help_trouble_ticket_url     | help_trouble_ticket_url
   log_uptime_active                | uptime_log_enable
   log_uptime_filename              | uptime_log_filename

3.84.0 (23 April 2009)

 - Added UDP communications support for NTCIP.
 - Lots of I18N improvements.
 - New integrated help system added.
 - KML output generation for DMS mapping.
 - Many regression fixes for Caltrans D10.
 - Updates and fixes for the dmslite protocol.

3.83.0 (17 April 2009)

 - Fixed many DMS and ramp metering bugs related to SONAR conversion.
 - Fixed DMS pixel status bugs for NTCIP protocol.

3.82.0 (3 April 2009)

 - The process to build and install IRIS has been greatly simplified, using an
   RPM package.  All dependencies updated to new versions.  Datatools dependency
   is no longer required.  Instructions for building and installing IRIS are now
   included in the docs/ directory.
 - Directory locations have been changed to be more consistent.

   Old Location        | New Location
   --------------------|-----------------------
   /data/meter/        | /var/lib/iris/meter/
   /data/traffic/      | /var/lib/iris/traffic/
   /usr/local/tms/     | /usr/share/java/
   /var/local/tms/dds/ | /var/lib/iris/xml/
   /var/log/tms/       | /var/log/iris/
   /var/www/html/iris/ | /var/www/html/iris-client/

 - Properties files have been cleaned up, with some properties being renamed.
 - Moved email configuration out of .properties files to System Attributes.
   New System Attributes are: email_smtp_host, email_sender_server,
   email_sender_client, email_recipient_aws, and email_recipient_bugs.

3.81.0 (4 March 2009)

 - DMS, ramp meters and timing plans converted to SONAR.
 - Many user interface enhancements were made in DMS tab.  Message preview,
   multiple sign selection, pixel failure feedback, brightness feedback, plus
   much more is included. Some enhancements were also made for the ramp meter
   tab.
 - AMBER alert functionality has changed to match normal DMS operation
   interface.
 - Added ability to select a font for each page of a sign message.
 - Timing plans can no longer be shared by devices.  Each timing plan is now
   associated with a single device (DMS or ramp meter).
 - Many new system attributes have been added to allow a great degree of system
   customization.
 - Lots of code cleanups and bug fixes.
 - SONAR dependency updated to 2.6.0.
 - Scheduler dependency updated to 0.2.1.

3.80.0 (3 November 2008)

 - System attributes replaces system policy table.  This is a system-wide
   configuration widget for IRIS.  This work was done by AHMCT, with additional
   features by Mn/DOT.
 - Fixed some comm link and controller status issues.
 - Lots of code cleanups and minor bug fixes.
 - Font can be selected when creating new DMS message (Optional feature from
   AHMCT).
 - SONAR dependency updated to 2.3.3.
 - TrafMap dependency updated to 4.16.

3.79.0 (21 October 2008)

 - Detector, r_nodes and stations converted to SONAR.
 - Fixed several bugs related to recent SONAR updates.
 - Lots of code cleanups and minor enhancements.
 - SONAR dependency updated to 2.3.2.

3.78.0 (24 September 2008)

 - Merged many changes from AHMCT (UC Davis).
   DMS message comboboxes are editable (agency-specific).
   RWIS tab added for weather stations (agency-specific).
   Internationalization work done on user interface.
   Many bugs fixed for Caltrans.
   Added buttons for camera presets.
 - Vault dependency updated to 1.3.3.
   SONAR dependency updated to 2.1.2.
   Tdxml dependency updated to 1.4.2.
   TrafMap dependency updated to 4.15.
   Video client dependency updated to 1.33.0.

3.77.0 (15 September 2008)

 - Warning signs have been converted to SONAR.
 - A bug related to creating video monitors was fixed.
 - An obscure infinite loop in SmartSensor decode was fixed.
 - SONAR dependency updated to 2.1.1.

3.76.0 (12 September 2008)

 - Alarm and camera tables have moved to new iris schema.  New _device_io table
   added for data integrity.
 - CommLinks will now automatically reconnect if the connection is lost.  This
   involved a significant cleanup of comm package.
 - A serious SONAR login race was fixed.
 - Bugs related to creating/removing cameras were fixed.
 - An obscure infinite loop in BER decode was fixed.
 - SONAR dependency updated to 2.1.0.

3.75.0 (5 September 2008)

 - Alarm objects converted to SONAR.  Alarm events now logged in DB in event
   schema.  New playlist feature added to camera tab.
 - Many SONAR login issues were fixed.  Numerous other bugs also have been fixed.
 - Scheduler dependency updated to 0.2.0.  SONAR dependency updated to 2.0.0.
   SONAR has a new dependency on scheduler.

3.74.0 (27 August 2008)

 - Camera objects converted to SONAR.
 - Multiple cameras can be selected at once, by holding down CTRL key (or shift
   key in lists).  When multiple cameras are selected, popup menu allows all
   cameras to be published/unpublished at once.
 - SONAR dependency updated to 1.9.0.  MapBean dependency updated to 4.3.0.

3.73.0 (14 August 2008)

 - Circuit, Node and Node Group objects removed.
 - There were various other database changes.  Communication Line renamed to
   Comm Link.  Controller170 merged into Controller.  Cabinet and Cabinet Style
   classes/tables added.  These new classes are all SONAR based.  This means new
   roles will need to be assigned to users change these objects.
 - The "Sonet System" GUI completely replaced with a new "Comm Links" GUI.  Some
   features are missing from this new interface (error counters and test
   communications), but they will be added back in a later version.
 - The "log" database is no longer required for IRIS operation.  Tables from
   "log" database moved to "event" schema in the "tms" database.  This means the
   IRIS server requires one less connection to the PostgreSQL server.  It also
   adds some foreign key relationships between the schemas.  Some additional
   database scripts will be required to retain "log" database event history.
   Also, a cron task should be set up to remove old records from the
   event.comm_event table (many events will be created per day).
 - SONAR dependency updated to 1.7.10.  Scheduler dependency updated to 0.1.1.

3.72.0 (23 July 2008)

 - Location objects converted to SONAR.  This involves database changes and RMI
   updates.
 - Since geo_loc is a new SONAR type, the roles must be modified to give
   administrators permission to change these objects.  One possibility is a role
   called "geo_loc" with a pattern of `"geo_loc/.*"`
 - SONAR dependency updated to 1.7.3.
 - Video monitor combo box added to camera tab.  This will allow switching
   cameras in an analog video switch through IRIS.  This work in ongoing.

3.71.0 (14 July 2008)

 - RMI changes in Controller, interfaces to prepare for conversion to SONAR.
 - SONAR dependency updated to 1.7.1.
 - SONAR authentication properties have changed.  Old properties "ldap.host",
   "ldap.port" and "ldap.ssl" are gone.  New porperty "sonar.ldap.urls" is a
   whitespace-seperated list of LDAP server URLs for authenticting.  If a user
   authentication fails using the first URL, the next LDAP server will be
   checked, etc.
 - There is a new property in iris-server.properties file: "sonar.session.file"
   controls location of session file.  This file is updated whenever a user logs
   in or out of IRIS.  It will be used for authenticating streaming video
   clients in the camera tab.
 - Some work has been done to enable restricting camera images on the fly.  This
   work is ongoing.
 - Video client dependency updated to 1.30.
 - Video client properties have changed.  Property "app.name" is no longer
   required.  Property "backend.host0" has changed to "video.backend.host0".
   Property "backend.port0" has changed to "video.backend.port0".
 - Log dependency updated to new version.
