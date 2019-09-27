# Lane-use Control Signs (LCS)

Select `View ➔ Lane Use ➔ LCS` menu item

A _lane-use control sign_ (LCS) is a sign which is mounted over a single lane of
traffic (typically one for each lane).  It can display a set of indications
which either permit or restrict use of that lane.

## Intelligent LCS

An _intelligent_ LCS is a full-matrix color [DMS] which is mounted directly over
a lane of traffic.  In addition to being used as an LCS, it can display
[variable speed advisories], or any operator-defined message as a regular DMS.

## Changeable LCS

A _changeable_ LCS device is much simpler than an ILCS.  It can display one of
these indications:

 * `lane open` — _green arrow_
<span style="background:#222;color:#0f0;border:solid white"> ↓ </span>
 * `use caution` — _yellow arrow_
<span style="background:#222;color:#ff0;border:solid white"> ↓ </span>
 * `lane closed` — _red_
<span style="background:#222;color:#f00;border:solid white"> X </span>

Each indication must be assigned to a separate [IO pin] on a [controller], as
well as the DMS which represents the LCS.


[controller]: controllers.html
[DMS]: admin_guide.html#dms
[IO pin]: controllers.html#io-pins
[variable speed advisory]: vsa.html
