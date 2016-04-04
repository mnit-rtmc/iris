# Incident DMS Deployment

## Incident Severity

Incidents have a *severity* attribute, with possible values of *MINOR*,
*NORMAL* or *MAJOR*.  The severity of an incident affects the distance to
deploy DMS.

Severity | Lane Impact     | Range  | Miles | Branching | Priority
---------|-----------------|--------|-------|-----------|---------------
MINOR    | Shoulder only   | NEAR   | 1.5   | No        | INCIDENT_LOW
NORMAL   | Lane(s) blocked | MIDDLE | 5.0   | Yes       | INCIDENT_MED
MAJOR    | Road closure    | FAR    | 10.0  | Yes       | INCIDENT_HIGH

## Deployment

When "Deploy" button is pressed, all DMS within the range of the incident are
found.  Each of these DMS will be deployed if matching *descriptor*, *locator*
and *advice* are found.

When the incident is cleared, all associated DMS are blanked.  The only
exception is for *MAJOR* incidents.  In that case, another match is performed
with *cleared* set to *YES*.  If a matching message is found, it will be
deployed with PSA priority for 5 minutes.

### Incident Descriptor

An incident matches a descriptor only if the *event type*, *lane type*,
*detail*, and *cleared* attributes match.

Event Type | Lane Type     | Detail   | Cleared | Rank | Multi
-----------|---------------|----------|---------|------|----------------
CRASH      | mainilne      |          | NO      | 1    | CRASH
CRASH      | mainilne      |          | YES     | 1    | CRASH CLEARED
STALL      | mainline      |          | NO      | 1    | STALLED VEHICLE
STALL      | mainline      |          | NO      | 2    | STALL
ROAD WORK  | mainline      |          | NO      | 1    | ROAD WORK
ROAD WORK  | exit ramp     |          | NO      | 1    | ROAD WORK ON RAMP
HAZARD     | mainline      | ice      | NO      | 1    | ICE ON ROAD
HAZARD     | mainline      | ice      | NO      | 2    | ICE
HAZARD     | mainilne      | debris   | NO      | 1    | DEBRIS
HAZARD     | mainline      | animal   | NO      | 1    | ANIMAL ON ROAD
HAZARD     | mainline      | flooding | NO      | 1    | FLASH FLOODING
HAZARD     | mainline      | flooding | NO      | 2    | FLASH FLOOD
HAZARD     | mainline      | flooding | NO      | 3    | FLOODING

Each matching descriptor will be checked to see if the multi string can be
rendered on the DMS.  The lowest ranking match will be used.

### Incident Locator

The incident is associated with the nearest *pickable* r_node.  If the distance
to that node is less than 0.5 miles, it can be used as a *locator*.  If not,
the nearest non-*pickable* r_node is found.  It can be used if distance is less
than 0.5 miles.

Range  | Branched | Pickable | Multi                     | Example
-------|----------|----------|---------------------------|------------------
NEAR   | NO       | Y/N      | AHEAD                     | AHEAD
MIDDLE | NO       | YES      | `[locmd] [locrn]`         | AT HWY 100
MIDDLE | YES      | Y/N      | ON `[locbr]` `[locbd]`    | ON 394 EAST
FAR    | NO       | NO       | `[locmi]` MILES AHEAD     | 8 MILES AHEAD
FAR    | NO       | YES      | `[locmd] [locrn]`         | AT 494
FAR    | YES      | YES      | ON `[locbr]` AT `[locrn]` | ON 394 AT HWY 100

Several MULTI-like tags are defined for incident locators.

Tag       | Description
----------|-----------------------------------------------
`[locmd]` | Location modifier (AT, N OF, S OF, etc.)
`[locrn]` | Road name of node nearest incident
`[locra]` | Abbreviated road name of node nearest incident
`[locbr]` | Branched road name
`[locbd]` | Branched road direction
`[locmi]` | Miles from DMS to node

Road names are converted to all capital letters.  For the `[locrn]` tag, certain
prefixes and suffixes are replaced with other values.  For the `[locra]` tag,
all matching prefixes and suffixes are stripped.

Type   | Value    | Replacement
-------|----------|------------
Prefix | U.S.     | HWY
Prefix | T.H.     | HWY
Prefix | C.S.A.H. | CTY
Prefix | I-       |
Suffix | AVE      | AVE
Suffix | BLVD     | BLVD
Suffix | DR       | DR
Suffix | HWY      | HWY
Suffix | LN       | LN
Suffix | PKWY     | PKWY
Suffix | PL       | PL
Suffix | RD       | RD
Suffix | ST       | ST
Suffix | TR       | TR
Suffix | WAY      | WAY

### Incident Advice

The incident *range*, *lane type* and *impact* will be matched with an incident
advice record.

Code | Lane Impact
-----|----------------------------
.    | Not blocked
?    | Partially blocked
!    | Fully blocked
:    | Partially or fully (? or !)
;    | Not fully blocked (. or ?)
,    | Any (. ? or !)

Range    | Lane Type | Impact | Cleared | Multi
---------|-----------|--------|---------|-------------------
NEAR     | mainline  | ?....  | NO      | IN MEDIAN
NEAR     | mainline  | ?....  | NO      | ON LEFT SHOULDER
NEAR     | mainline  | .?...  | NO      | IN LEFT LANE
NEAR     | mainline  | .??..  | NO      | IN LEFT 2 LANES
NEAR     | mainline  | ..?..  | NO      | IN CENTER LANE
NEAR     | mainline  | ...?.  | NO      | IN RIGHT LANE
NEAR     | mainline  | ..??.  | NO      | IN RIGHT 2 LANES
NEAR     | mainline  | ....?  | NO      | ON RIGHT SHOULDER
NEAR     | mainline  | ....?  | NO      | ON SHOULDER
NEAR     | mainline  | ?...?  | NO      | ON BOTH SHOULDERS
NEAR     | mainline  | ;???;  | NO      | IN ALL LANES
NEAR     | mainline  | ,!;;,  | NO      | LEFT LANE CLOSED
NEAR     | mainline  | ,;;!,  | NO      | RIGHT LANE CLOSED
NEAR     | mainline  | ,;!;,  | NO      | CENTER LANE CLOSED
NEAR     | mainline  | !!!!!  | NO      | ROAD CLOSED
NEAR     | mainline  | ?!!!!  | NO      | USE OTHER ROUTES
NEAR     | mainline  | !!..?  | NO      | REDUCED TO 2 LANES
MIDDLE   | mainline  | ,,,,,  | YES     | USE CAUTION
FAR      | mainline  | ,!;;,  | NO      | EXPECT DELAYS
FAR      | mainline  | ,;!;,  | NO      | EXPECT DELAYS
FAR      | mainline  | ,;;!,  | NO      | EXPECT DELAYS
FAR      | mainline  | ,!!;,  | NO      | MAJOR DELAY
FAR      | mainline  | ,;!!,  | NO      | MAJOR DELAY
FAR      | mainline  | ,!!!,  | NO      | MAJOR DELAY
FAR      | mainline  | ,,,,,  | YES     | ALL LANES OPEN
