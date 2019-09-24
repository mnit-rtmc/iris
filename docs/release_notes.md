# IRIS 5.x Release Notes

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
