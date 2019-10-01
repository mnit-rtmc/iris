# Tolling

High-occupancy / toll (HOT) lanes can be operated through IRIS.  Functions
include calculating tolls in real time based on density, displaying prices on
DMS, and logging vehicle transponder (tag) information.  Customer accounts and
billing are not supported; those features must be provided by an external
system.

## Toll Zones

Select `View ➔ Lane Use ➔ Toll Zones` menu item

A toll zone is a segment of roadway between two [station]s.  All HOT lane
detectors between the starting station and ending station can be used to
calculate the toll.  Every 3 minutes, tolls are calculated using data from the
most recent 6 minutes.  Within a zone, each station has an associated toll,
based on the highest density recorded among HOT lane detectors downstream of
that station.

p = α ⋅ k <sup>β</sup>, where α = 0.045 and β = 1.10

The toll price is then rounded to the nearest $0.25.

## Pricing on DMS

A DMS can display pricing information for toll zones.  This is accomplished
using a `[tz…]` [action tag].  The tag has two or more parameters, separated by
commas.  The first parameter is the tolling mode, and must be `p`, `o` or `c`.
These indicate _priced_, _open_ or _closed_, respectively.  The second parameter
is the (first) toll zone ID.  If the toll route spans multiple toll zones,
additional zone IDs may be provided after the first.

If the tolling mode is _priced_, the tag will be replaced with the calculated
price, for example `2.75`.  It will be the sum of prices for all specified toll
zones.  The values of the `toll_min_price` and `toll_max_price`
[system attribute]s limit the calculated price.

For _open_ and _closed_ modes, the tag will be replaced with an empty string.
If required, a message such as `OPEN` or `CLOSED` may be appended after the tag.

Price events are logged when a message is sent to a DMS, and again when the
message is verified.  The price event includes the date and time, toll zone,
tolling mode (_priced_, _open_, _closed_), price, sent / verified status and
next downstream tag reader.  The logged toll zone is the last zone specified in
the tag.  In _priced_ mode, the displayed price is logged.  For _open_ or
_closed_, a price of 0 is logged.

## Tag Readers

Select `View ➔ Lane Use ➔ Tag Readers` menu item

A tag reader is a sensor for in-vehicle transponders (tags).  They can be
mounted over a tolled lane to record customer trips.  These are typically
located just downstream of a pricing DMS.

When a tag is read, an event is logged in the `tag_read_event` database table.
The event includes the date and time, tag ID, toll zone and tollway.  This
can be combined with the logged pricing information to build a list of trips for
each tag ID.  This information can be used to bill customers.


[action tag]: action_plans.html#dms-action-tags
[station]: road_topology.html#r_node-types
[system attribute]: system_attributes.html
