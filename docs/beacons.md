## Beacons

Select `View ➔ Message Signs ➔ Beacons` menu item

A beacon is a light or set of lights that flashes toward oncoming traffic.
Sometimes called _flashers_ or _wig-wags_, their purpose is to draw attention to
a [DMS] or static sign.

Internal and external beacons are supported by IRIS.  Internal beacons are
controlled through the DMS controller, are activated through the [NTCIP] DMS
protocol and are considered part of the DMS.  External beacons use dedicated
controllers and protocols (_e.g._ [MnDOT-170]).

Beacons can be controlled by [action plans] by using [beacon actions].

Whenever a beacon is activated or deactivated, a time-stamped record is added to
the `beacon_event` table.


[action plans]: action_plans.html
[beacon actions]: action_plans.html#beacon-actions
[DMS]: admin_guide.html#dms
[MnDOT-170]: admin_guide.html#mndot170
[NTCIP]: admin_guide.html#ntcip
