# Ramp Meters

Select `View ➔ Ramp Meters` menu item

A ramp meter is a traffic signal at an on-ramp which controls the rate of
vehicles entering a freeway.  Typically, one vehicle is permitted to enter for
each green indication displayed on the signal.

The following operations can be performed on a ramp meter:

* Activating and deactivating meter
* Adjusting meter release rate
* Querying meter status
* Querying green counts
* Synchronizing clock
* Configuring time-of-day operation

## Setup

For basic time-of-day operation, the **simple** metering `algorithm` will run
the meter at a fixed release rate, equal to the target rate for the current
period (AM or PM).

The [density adaptive] algorithm requires a bit more configuration, since it
depends on the [road topology].  An _entrance_ [r_node] must exist, with
matching `roadway`, `road_dir`, `cross_street`, `cross_dir` and `cross_mod`.
This `r_node` must have a [green] detector, plus optional _queue_ and
_passage_ detectors.

There is special handling for meters on [CD roads].  The meter `roadway`
should _NOT_ be the CD road, but the entrance [r_node] should.

<details>
<summary>API Resources 🕵️ </summary>

* `iris/api/ramp_meter` (primary)
* `iris/api/ramp_meter/{name}`

| Access       | Primary               | Secondary                        |
|--------------|-----------------------|----------------------------------|
| 👁️  View      | name, location, fault | geo\_loc                         |
| 👉 Operate   | m\_lock               | rate                             |
| 💡 Manage    | notes                 | storage, max\_wait, algorithm, am\_target, pm\_target |
| 🔧 Configure | controller            | pin, meter\_type, beacon, preset |

</details>


[CD roads]: road_topology.html#rnode-transitions
[density adaptive]: density_adaptive.html
[green]: vehicle_detection.html#lane-type
[r_node]: road_topology.html#rnodes
[road topology]: road_topology.html
