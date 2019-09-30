# Lane-use Control Signs (LCS)

Select `View ➔ Lane Use ➔ LCS` menu item

A _lane-use control sign_ (LCS) is a sign which is mounted over a single lane of
traffic (typically one for each lane).  It can display a set of **indications**
which either permit or restrict use of that lane.

## Indications

These are some typical lane-use indications:

Indication          | Example Image
--------------------|-----------------------------------------------------------
`Dark`              | <span style="background:black;border:0.1rem solid gray">   </span>
`Lane open`         | <span style="background:black;color:#0f0;border:0.1rem solid gray"> ↓ </span>
`Use caution`       | <span style="background:black;color:#ff0;border:0.1rem solid gray"> ↓ </span>
`Lane closed ahead` | <span style="background:black;color:#ff0;border:0.1rem solid gray"> X </span>
`Lane closed`       | <span style="background:black;color:#f00;border:0.1rem solid gray"> X </span>
`HOV / HOT`         | <span style="background:black;color:#fff;border:0.1rem solid gray"> ◊ </span>
`Merge right`       | <span style="background:black;color:#ff0;border:0.1rem solid gray"> 》 </span> (animated)
`Merge left`        | <span style="background:black;color:#ff0;border:0.1rem solid gray"> 《 </span> (animated)
`Merge both`        | <span style="background:black;color:#ff0;border:0.1rem solid gray"> 〈〉 </span> (animated)

## Intelligent LCS

An _intelligent_ LCS is a full-matrix color [DMS] mounted directly over a lane
of traffic.  In addition to being used as an LCS, it can display
[variable speed advisories], or any operator-defined message as a regular DMS.

### Lane-Use MULTI

_Lane-Use MULTI_ is an association from an [indication](#indications) to a
[quick message].  The quick message typically contains a _graphic_ or series of
graphics on multiple pages.  For it to be available for use, the _quick message_
must have a _sign config_ which matches the sign.

## Changeable LCS

A _changeable_ LCS device is much simpler than an ILCS.  It can display one of
these [indications](#indications):

1. `Lane open`
2. `Use caution`
3. `Lane closed`

Each _indication_ must be assigned to a separate [IO pin] on a [controller], as
well as the DMS which represents the LCS.


[controller]: controllers.html
[DMS]: dms.html
[IO pin]: controllers.html#io-pins
[quick message]: dms.html#quick-messages
[variable speed advisory]: vsa.html
