# Incident DMS Deployment

Sign messages can be suggested from active roadway incidents.  This method uses
incident attributes and location on the freeway network to make suggestions.

Each message is composed of three lines: **descriptor**, **locator** and
**advice**.  Each of these may have an abbreviated version, which is used only
when the normal version does not fit the width of the sign.

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
`middle` |   2-3 |             5 |            10
`far`    |   4-7 |            10 |            20

## Severity

Incident severity determines the **maximum range** and **message priority**.
There are three severity values: `minor`, `normal`, and `major`.

Severity | Maximum Range | Message Priority
---------|---------------|------------------
`minor`  | `near`        | `INCIDENT_LOW`
`normal` | `middle`      | `INCIDENT_MED`
`major`  | `far`         | `INCIDENT_HIGH`

Severity depends on the **lane type** and **lane impact** at the incident
location.

Lane Type   | Lane Impact       | Severity
------------|-------------------|---------
`mainline`  | Shoulder only     | `minor`
`mainline`  | Any lane blocked  | `normal`
`exit`      | All lanes blocked | `normal`
`merge`     | All lanes blocked | `normal`
`CD road`   | All lanes blocked | `normal`
`mainline`  | All lanes blocked | `major`

## Descriptor

The *descriptor* determines the first line of each suggested message.  The
configurable descriptor table contains several columns which are matched to the
incident.  Messages are only suggested if a descriptor matches.

The **lane type** at the incident can be `mainline`, `exit`, `merge` or
`CD road`.

**Event type** can be `CRASH`, `STALL`, `ROAD WORK` or `HAZARD`.  Hazard events
can have an associated **detail** (*animal, debris, ice, etc*).

**Cleared** can be `YES` or `NO`, and indicates whether the incident has just
cleared.

## Locator

A matching *locator* determines the second line of a suggested message.  The
configurable locator table contains columns which are matched to the incident.

**Range** must match the range from the sign to the incident.  For example, if
there are 2 exits between them, the range is *middle*.

**Branched** is `YES` if the sign is on a different roadway than the incident.

If a *pickable* roadway node (r_node) is within 1 mile of the incident, it is
**picked**, and its location is used for *locator tags*.

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

A matching *advice* determines the third line of a suggested message.

As with the locator, the **range** must match from the sign to the incident.

The incident **lane type** can be `mainline`, `exit`, `merge` or `CD road`.

**Lane impact** uses special codes to match each lane of the incident impact.
The number of lanes (characters) must also match.

Code | Lane Impact
-----|----------------------------
`.`  | Not blocked
`?`  | Affected
`!`  | Fully blocked
`:`  | Blocked or affected (`?` or `!`)
`;`  | Not blocked (`.` or `?`)
`,`  | Any (`.` `?` or `!`)

## Clearing

When the incident is cleared, all associated signs are blanked.  The only
exception is for `major` incidents.  In that case, another match is performed
with *cleared* set to `YES`.  If a matching message is found, it will be
deployed with `PSA` message priority for 5 minutes.
