# Tolling

High-occupancy / toll (HOT) lanes can be operated through IRIS.  Functions
include logging vehicle transponder (tag) information, calculating tolls in
real time, and displaying prices on [DMS].

Customer accounts and billing are not supported; those features must be
provided by an external system.

## Tag Readers

Select `View ➔ Lane Use ➔ Tag Readers` menu item

A tag reader is a sensor for in-vehicle transponders (tags).  They can be
mounted over a tolled lane to record customer trips.  These are typically
located just downstream of a pricing DMS.

<details>
<summary>API Resources 🕵️ </summary>

* `iris/api/tag_reader` (primary)
* `iris/api/tag_reader/{name}`

| Access       | Primary        | Secondary          |
|--------------|----------------|--------------------|
| 👁️  View      | name, location | geo\_loc, settings |
| 💡 Manage    | notes          | toll\_zone         |
| 🔧 Configure | controller     | pin                |

</details>

### Tag Read Events

When a tag is read, an [event] can be stored in the `tag_read_event` table.
The event includes the **date and time**, **tag ID**, **toll zone** and
**tollway**.  This can be combined with the price message events to build a
list of trips for each tag ID.  This information can be used to bill
customers.

## Toll Zones

Select `View ➔ Lane Use ➔ Toll Zones` menu item

A toll zone is a segment of roadway between two [station]s.  All HOT lane
detectors between the starting station and ending station can be used to
calculate the toll.  Every 3 minutes, tolls are calculated using data from the
most recent 6 minutes.  Within a zone, each station has an associated toll,
based on the highest density _k_ recorded among HOT lane detectors downstream of
that station.

p = α ⋅ k <sup>β</sup>

* α is the value of the `toll_density_alpha` [system attribute], default 0.045
* β is the value of the `toll_density_beta` [system attribute], default 1.10
* p is the calculated price

The toll price is then rounded to the nearest $0.25.

<details>
<summary>API Resources 🕵️ </summary>

* `iris/api/toll_zone` (primary)
* `iris/api/toll_zone/{name}`

| Access       | Primary | Secondary                                   |
|--------------|---------|---------------------------------------------|
| 👁️  View      | name    |                                             |
| 💡 Manage    | tollway | start\_id, end\_id, alpha, beta, max\_price |

</details>

## Pricing on DMS

The toll price can be displayed in DMS messages using [device actions].  A
`[tz` *…* `]` [action tag] in the [message pattern] will be replaced with the
appropriate value.  It has the following format:

`[tz` *mode,{tz0},…{tzn}* `]`

**Parameters**

1. `mode`: Tolling mode:
   - `p`: _priced_
   - `o`: _open_
   - `c`: _closed_
2. `tz0`: First toll zone ID
3. `tz1` - `tzn`: (Optional) additional toll zone IDs

If the tolling mode is _priced_, the tag will be replaced with the calculated
price, for example `2.75`.  It will be the sum of prices for all specified toll
zones.  The values of the `toll_min_price` and `toll_max_price`
[system attribute]s limit the calculated price.

For _open_ and _closed_ modes, the tag will be replaced with an empty string.
If required, a message such as `OPEN` or `CLOSED` may be appended after the tag.

### Price Message Events

An [event] can be stored in the `price_message_event` table when a message is
sent to a DMS, and again when the message is verified.  The event includes the
**date and time**, **DEPLOYED** or **VERIFIED** status, **DMS id**,
**toll zone**, _highest density_ **detector(s)** and **price**.

The logged _toll zone_ is the last zone specified in the tag.  In _priced_ mode,
the displayed price is logged.  For _open_ or _closed_, a price of 0 is logged.


[action tag]: action_plans.html#action-tags
[device actions]: action_plans.html#device-actions
[DMS]: dms.html
[event]: events.html
[message pattern]: message_patterns.html
[station]: road_topology.html#r_node-types
[system attribute]: system_attributes.html
