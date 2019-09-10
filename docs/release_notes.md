# IRIS 5.x Release Notes

5.2.0 (10 Sep 2019)
 - Add markers for dedicated purpose DMS (tolling, parking, wayfinding).
 - Fixed some incident DMS deployment bugs.
 - Added `incident_clear_advice_multi` and `incident_clear_advice_abbrev`
   system attributes.
 - Removed `cleared` from inc_advice table.
 - Changed `all_lanes_blocked` incident impact to `lanes_blocked`.  Also,
   `all_lanes_affected` to `lanes_affected`.
 - Increased `middle` and `far` incident deployment ranges to 5 and 9 exits,
   respectively.
 - Restricted `ahead` range to less than about 1 mile.
 - Incident deployment: use dedicated tolling signs when left lane is impacted
   and sign is on same corridor as incident.
 - Added `lane use` dedicated device purpose.
 - Log FAIL DOMAIN events in client event table.
 - Sort comm protocols by description in comm link UI combo box.

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
