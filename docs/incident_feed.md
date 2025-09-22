## Incident Feed Format

An incident feed is an external system that IRIS can poll to get real-time
roadway [incident] information.  When polled, the external system must respond
with an ASCII text file, with one line per active incident.

Each line should contain 7 fields, separated by comma characters `,` and
terminated with a single newline character `\n` (ASCII 0x0A).  The fields are:

1. **incident ID**
2. **type**: `CRASH`, `STALL`, `ROADWORK` or `HAZARD`
3. **incident detail**: may be blank, or one of the _incident detail_ names
4. **latitude**
5. **longitude**
6. **camera ID**: may be blank, or the ID of a [camera] to view the incident
7. **direction**: `NB`, `SB`, `EB` or `WB`

_Latitude_ and _longitude_ define coördinates using the WGS 84 datum.


[camera]: cameras.html
[incident]: incidents.html
