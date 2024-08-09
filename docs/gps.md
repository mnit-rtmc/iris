# GPS

Select `View ➔ GPS` menu item

A GPS (global positioning system) receiver allows device locations to be
tracked and automatically updated with the nearest roadway and direction.
They can be created, edited and deleted with the GPS form.

## Setup

Field        | Description
-------------|--------------------------------------------------
Notes        | administrator notes, possibly including [hashtag]s
Geo Location | device location name (same as device name)

To check a GPS device, select the device ([DMS], etc.) and open its `Properties`
form.  On the `Location` tag, there are widgets for querying the GPS status.

<details>
<summary>API Resources 🕵️ </summary>

* `iris/api/gps` (primary)
* `iris/api/gps/{name}`

| Access       | Primary    | Secondary                              |
|--------------|------------|----------------------------------------|
| 👁️  View      | name       | latest\_poll, latest\_sample, lat, lon |
| 💡 Manage    | notes      |                                        |
| 🔧 Configure | controller | geo\_loc, pin                          |

</details>

The GPS must be associated with a controller on an appropriate comm link.

Protocol   | Notes
-----------|-------------------------------------------------------------
[NTCIP]    | same controller as [DMS] / weather sensor (on pin 2)
[RedLion]  | controller on separate comm link for modem (tcp port ????)
[SierraGX] | controller on separate comm link for modem (tcp port 9494)

The GPS location will be polled on the _long poll period_ of the [comm config].
The `Query GPS` button can be used to manually poll the coördinates.


[comm config]: comm_config.html
[DMS]: dms.html
[hashtag]: hashtags.html
[NTCIP]: protocols.html#ntcip
[RedLion]: protocols.html#redlion
[SierraGX]: protocols.html#sierragx
