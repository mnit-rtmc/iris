# GPS

A portable [DMS] can have an associated GPS (global positioning system)
receiver.  This allows the sign to be tracked and automatically updated with the
nearest roadway and direction.

To create a GPS device, select the DMS and open its `Properties` form.  On the
`Location`, there are controls for configuring the associated GPS.  Once the GPS
is enabled, it must be associated with a controller.  Its name will be the same
as the DMS, with `_gps` appended.  For [NTCIP], the GPS should be associated
with the same controller as the DMS (using pin 2).  For [RedLion] or [SierraGX],
a new comm link and controller must be created to communicate with the modem.

The GPS will be polled once every 5 minutes, unless on a `Modem` comm link.  In
that case, the comm link period will be used.  The `Query GPS` button can be
used to manually poll the coördinates.


[DMS]: dms.html
[NTCIP]: comm_links.html#ntcip
[RedLion]: comm_links.html#redlion
[SierraGX]: comm_links.html#sierragx
