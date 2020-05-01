# Incident DMS Deployment

Messages can be suggested for [DMS] from active roadway incidents.  The
attributes of the incident are used to make suggestions: [impact](#impact),
[range](#range), [severity](#severity), and location on the freeway network.

Each message is composed of three parts: **descriptor** (_what?_), **locator**
(_where?_) and **advice** (_how?_).  Suggestions are only made if a match is
found for all three parts.

## Impact

Every incident is assigned an **impact**, based on which lanes are _blocked_ or
_affected_.  If any lane is _blocked_, then one of the `*lanes_blocked` impacts
is used.  If any lanes are _affected_ (partially blocked), the impact is one of
the `*lanes_affected` values.  Shoulder impacts are lower priority, first
_blocked_ then _affected_.

Impact                    | Description
--------------------------|------------------------------------------------
`lanes_blocked`           | Left & right lanes blocked (possibly all lanes)
`left_lanes_blocked`      | Left lane blocked, right lane open
`right_lanes_blocked`     | Right lane blocked, left lane open
`center_lanes_blocked`    | Center lane(s) blocked, left & right lanes open
`lanes_affected`          | Left & right lanes affected (possibly all lanes)
`left_lanes_affected`     | Left lane affected, right lane open
`right_lanes_affected`    | Right lane affected, left lane open
`center_lanes_affected`   | Center lane(s) affected, left & right lanes open
`both_shoulders_blocked`  | Left & right shoulders blocked, all lanes open
`left_shoulder_blocked`   | Left shoulder blocked, all lanes open
`right_shoulder_blocked`  | Right shoulder blocked, all lanes open
`both_shoulders_affected` | Left & right shoulders affected, all lanes open
`left_shoulder_affected`  | Left shoulder affected, all lanes open
`right_shoulder_affected` | Right shoulder affected, all lanes open
`free_flowing`            | No impact

## Range

_Range_ is the distance from an upstream sign to the incident.  There are four
defined ranges, each with a number of freeway _[exits]_ in between.

Range    | Exits | Maximum distance
---------|-------|--------------------------------
`ahead`  |     — | 1.5 miles (0.75 miles _picked_)
`near`   |   0-3 | —
`middle` |   4-5 | —
`far`    |   6-9 | —

* Only one exit is counted for each _interchange_ (for example, the off-ramps of
  a cloverleaf)
* _Intersections_ are counted as if they were exits
* Exits from a _CD road_ are **not** included in the count

Searches for upstream signs will not continue on the opposite direction of the
incident roadway.

## Severity

Incident severity determines the **maximum range** and **message priority**.
There are three severity values: `minor`, `normal`, and `major`.

Severity | Maximum Range | Message Priority
---------|---------------|-----------------
`minor`  | `near`        | `INCIDENT_LOW`
`normal` | `middle`      | `INCIDENT_MED`
`major`  | `far`         | `INCIDENT_HIGH`

Severity depends on how many lanes are _blocked_ or _affected_, as well as the
**lane type** at the incident location.

Impact                          | Mainline   | CD/Exit  | Merge
--------------------------------|------------|----------|--------
more than half lanes `blocked`  | `major`    | `normal` | `minor`
half or less lanes `blocked`    | `normal`   | `minor`  | —
either shoulder `blocked`       | `normal`   | `minor`  | —
any lane or shoulder `affected` | `minor`    | —        | —

## Descriptor

The _descriptor_ determines the first line of each suggested message.  The
configurable descriptor table contains fields which are matched to the incident.

Field         | Description
--------------|------------------------------------------
Incident type | `CRASH`, `STALL`, `ROAD WORK` or `HAZARD`
Detail        | usually hazard detail: `animal`, `debris`, `ice`, etc
Lane Type     | `mainline`, `exit`, `merge` or `CD road`

If the _detail_ field is blank, the _descriptor_ will be used as a fallback for
incidents which match the other two fields.

## Locator

A matching _locator_ determines the second line of a suggested message.  The
configurable locator table contains fields which are matched to the incident.

Field    | Description
---------|----------------------------------------------------------
Range    | from sign to incident: `ahead`, `near`, `middle` or `far`
Branched | sign and incident on different roadways
Picked   | a [pickable] r_node is within 1 mile of the incident; its location can be used for _locator tags_

### Locator Tags

Several [MULTI]-like tags are available for locators.  These tags will be
replaced with incident location information when composing a message.  Tags
should only be used if the locator's _picked_ state matches.

Tag       | Picked | Description
----------|--------|-------------------------------------------------
`[locrn]` | —      | Road name
`[locrd]` | —      | Road direction (NORTH, SOUTH, etc.)
`[locmd]` | `YES`  | Location modifier (AT, NORTH OF, SOUTH OF, etc.)
`[locxn]` | `YES`  | Cross-street name
`[locmi]` | `NO`   | Miles from sign to incident

Road and cross-street names are converted to all capital letters.

#### Road Affixes

The _road affix_ table determines how _prefixes_ and _suffixes_ are handled
within `[locrn]` and `[locxn]` tags.  Example suffixes are **AVE** and **RD**.

Field        | Description
-------------|----------------------------
Affix        | road prefix / suffix string
Prefix       | `true` for prefix, `false` for suffix
Fixup        | string to replace affix for sign message display
Allow Retain | `true` if retaining affix is allowed without fixup

* For the `[locrn]` and `[locxn]` locator tags, matched affixes are replaced
  with the _fixup_ value

## Advice

A matching _advice_ determines the third line of a suggested message.  The
configurable advice table contains fields which are matched to the incident.

Field          | Description
---------------|-----------------------------------------
Impact         | incident [impact](#impact)
Lane Type      | `mainline`, `exit`, `merge` or `CD road`
Range          | from sign to incident: `ahead`, `near`, `middle` or `far`
Open Lanes     | count of non-shoulder lanes _not_ impacted by incident
Impacted Lanes | count of non-shoulder lanes impacted

Rows where _open_ and / or _impacted lanes_ are specified will be matched in
preference to rows where they are not.

## Abbreviation

If a message is too wide to fit on the sign, an abbreviated version can be
created using the [allowed words] list.

Each line which doesn't fit is split into words.  Starting at the end, any word
which appears in the _allowed words_ list is replaced with its abbreviated form.
Then the line is checked again, and if still too wide, the process repeats.
Words with empty abbreviations are checked last, only if all other abbreviations
are insufficient.

If a line is still too wide after all words are checked, the message is
discarded, with no suggestion for that sign.

## Dedicated Purpose Signs

_Dedicated purpose_ signs normally cannot be used for incidents.  An exception
is `tolling` signs — they are used if these conditions are met:

* Sign and incident are on the same roadway (not _branched_)
* Sign is less than 1 mile upstream of the incident
* _Left_ lane impacted: `lanes_blocked`, `left_lanes_blocked`, `lanes_affected`
  or `left_lanes_affected`

## Linking

Deployed signs will be **linked** with the incident.  If a sign message is later
changed by an operator, it will keep the incident link unless the _descriptor_
(first line) is changed.

## Updates

If any signs are _linked_ with an incident when an update is logged, the device
deploy logic will be checked again.  If there are suggested changes, the device
deploy form will appear.  All changes will be listed in the form, including new
devices and devices to be blanked.

## Clearing

When a `major` severity incident is cleared, a new message will be sent to each
linked sign.  The _descriptor_ and _locator_ will be the same, but _advice_
will be taken from the `incident_clear_advice_multi` [system attribute].  The
_cleared_ message will be deployed with `PSA` message priority for 5 minutes.


[allowed words]: dms.html#allowed-words
[DMS]: dms.html
[exits]: road_topology.html#r_node-types
[MULTI]: dms.html#multi
[pickable]: road_topology.html#pickable
[system attribute]: system_attributes.html
