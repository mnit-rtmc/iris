# Beacons

Select `View ➔ Message Signs ➔ Beacons` menu item

A beacon is a light or set of lights that flashes toward oncoming traffic.
Sometimes called _flashers_ or _wig-wags_, their purpose is to draw attention to
a [DMS] or static sign.

**Internal** and **external** beacons are supported by IRIS.  Internal beacons
are controlled through the DMS controller, are activated through the [NTCIP] DMS
protocol and are considered part of the DMS.  External beacons use dedicated
controllers and protocols (_e.g._ [MnDOT-170]).

Beacons can be controlled by [action plans] by using [beacon actions].

## Events

Whenever a beacon is _activated_ or _deactivated_, a time-stamped record is
added to the `beacon_event` table.  These records are purged automatically when
older than the value of the `beacon_event_purge_days` [system attribute].


[action plans]: action_plans.html
[beacon actions]: action_plans.html#beacon-actions
[DMS]: dms.html
[MnDOT-170]: comm_links.html#mndot-170
[NTCIP]: comm_links.html#ntcip
[system attribute]: system_attributes.html
