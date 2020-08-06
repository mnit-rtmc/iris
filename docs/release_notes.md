# IRIS 5.x Release Notes

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
