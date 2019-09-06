# Incident DMS Deployment

Sign messages can be suggested from active roadway incidents.  This method uses
incident attributes (**range**, **severity**, etc.) and location on the freeway
network to make suggestions.

Each message is composed of three parts: **descriptor** (_what?_), **locator**
(_where?_) and **advice** (_how?_).  Suggestions are only made if a match is
found for all three parts.  Each part may have an abbreviated version, which is
used only when the normal version does not fit the sign.

## Range

*Range* is the distance from an upstream sign to the incident.  There are four
defined ranges, each with a number of freeway *exits* in between.  For this
purpose, exits which are part of the same interchange are treated as one (for
example, both off-ramps of a cloverleaf).  For reference, typical mile distances
for metro and rural freeways are included.

Range    | Exits | Miles (Metro) | Miles (Rural)
---------|-------|---------------|--------------
`ahead`  |     0 |             1 |             2
`near`   |     1 |             2 |             5
`middle` |   2-4 |             5 |            10
`far`    |   5-8 |            10 |            20

## Severity

Incident severity determines the **maximum range** and **message priority**.
There are three severity values: `minor`, `normal`, and `major`.

Severity | Maximum Range | Message Priority
---------|---------------|------------------
`minor`  | `near`        | `INCIDENT_LOW`
`normal` | `middle`      | `INCIDENT_MED`
`major`  | `far`         | `INCIDENT_HIGH`

Every incident is assigned an **impact**, based on which lanes are *blocked* or
*affected*.  They are ordered based on their effect on traffic.  Severity
depends on impact and **lane type** at the incident location.

Impact | Description               | Mainline   | CD/Exit  | Merge
-------|---------------------------|------------|----------|--------
 0     | `all_lanes_blocked`       | `major`    | `normal` | `minor`
 1     | `left_lanes_blocked`      | `normal`   | `minor`  | —
 2     | `right_lanes_blocked`     | `normal`   | `minor`  | —
 3     | `center_lanes_blocked`    | `normal`   | `minor`  | —
 4     | `both_shoulders_blocked`  | `normal`   | `minor`  | —
 5     | `left_shoulder_blocked`   | `normal`   | `minor`  | —
 6     | `right_shoulder_blocked`  | `normal`   | `minor`  | —
 7     | `all_lanes_affected`      | `minor`    | —        | —
 8     | `left_lanes_affected`     | `minor`    | —        | —
 9     | `right_lanes_affected`    | `minor`    | —        | —
10     | `center_lanes_affected`   | `minor`    | —        | —
11     | `both_shoulders_affected` | `minor`    | —        | —
12     | `left_shoulder_affected`  | `minor`    | —        | —
13     | `right_shoulder_affected` | `minor`    | —        | —
14     | `all_free_flowing`        | —          | —        | —

## Descriptor

The *descriptor* determines the first line of each suggested message.  The
configurable descriptor table contains fields which are matched to the incident.

Field         | Description
--------------|---------------------------------------------------
Incident type | `CRASH`, `STALL`, `ROAD WORK` or `HAZARD`
Detail        | usually hazard detail: `animal`, `debris`, `ice`, etc
Lane Type     | `mainline`, `exit`, `merge` or `CD road`

## Locator

A matching *locator* determines the second line of a suggested message.  The
configurable locator table contains fields which are matched to the incident.

Field    | Description
---------|---------------------------------------------------
Range    | from sign to incident: `ahead`, `near`, `middle` or `far`
Branched | `YES` or `NO`: sign and incident on different roadways
Picked   | `YES` or `NO`: a *pickable* node (r_node) is within 1 mile of the incident; its location can be used for *locator tags*

### Locator Tags

Several MULTI-like tags are available for locators.  These tags will be replaced
with incident location information when composing a message.  Tags should only
be used if the locator's *picked* state matches.

Tag       | Picked | Description
----------|--------|-------------------------------------------------
`[locrn]` | —      | Road name
`[locrd]` | —      | Road direction (NORTH, SOUTH, etc.)
`[locmd]` | `YES`  | Location modifier (AT, NORTH OF, SOUTH OF, etc.)
`[locxn]` | `YES`  | Cross-street name
`[locxa]` | `YES`  | Cross-street abbreviation
`[locmi]` | `NO`   | Miles from sign to incident

Road and cross-street names are converted to all capital letters.

For the `[locrn]` and `[locxn]` tags, prefixes and suffixes which match values
in the `road_affix` table are replaced.

For the `[locxa]` tag, matching `road_affix` values are stripped.

## Advice

A matching *advice* determines the third line of a suggested message.  The
configurable advice table contains fields which are matched to the incident.

Field          | Description
---------------|---------------------------------
Impact         | code; see the *severity* section
Lane Type      | `mainline`, `exit`, `merge` or `CD road`
Range          | from sign to incident: `ahead`, `near`, `middle` or `far`
Impacted Lanes | number of non-shoulder lanes, (if blank, any number matches)
Open Lanes     | number of non-shoulder lanes *not* impacted

## Clearing

When a `major` severity incident is cleared, a new message will be sent to each
deployed sign.  The *descriptor* and *locator* will be the same, but *advice*
will be taken from the `incident_clear_advice_multi` [system attribute].  If it
does not fit on the sign, the value of `incident_clear_advice_abbrev` will be
used instead.  The *cleared* message will be deployed with `PSA` message
priority for 5 minutes.

## Updating

If any devices are associated with an incident when an update is logged, the
device deploy logic will be checked again.  If any devices have proposed
changes, the device deploy form will appear.  All proposed changes will be
listed in the form, including new devices and any devices to be blanked.

[system attribute]: admin_guide.html#sys_attr
