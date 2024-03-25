# GPS

A portable [DMS] can have an associated GPS (global positioning system)
receiver.  This allows the sign to be tracked and automatically updated with
the nearest roadway and direction.

<details>
<summary>API Resources 🕵️ </summary>

* `iris/api/gps` (primary)
* `iris/api/gps/{name}`

| Access       | Primary    | Secondary                              |
|--------------|------------|----------------------------------------|
| 👁️  View      | name       | latest\_poll, latest\_sample, lat, lon |
| 💡 Manage    | notes      |                                        |
| 🔧 Configure | controller | pin                                    |

</details>

To create a GPS device, select the DMS and open its `Properties` form.  On the
`Location`, there are controls for configuring the associated GPS.  Once the
GPS is enabled, it must be associated with a controller.  Its name will be the
same as the DMS, with `_gps` appended.  For [NTCIP], the GPS should be
associated with the same controller as the DMS (using pin 2).  For [RedLion] or
[SierraGX], a new comm link and controller must be created to communicate with
the modem.

The GPS location will be polled on the _long poll period_ of the [comm config].
The `Query GPS` button can be used to manually poll the coördinates.


[comm config]: comm_config.html
[DMS]: dms.html
[NTCIP]: protocols.html#ntcip
[RedLion]: protocols.html#redlion
[SierraGX]: protocols.html#sierragx
