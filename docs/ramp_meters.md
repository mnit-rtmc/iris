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

<details>
<summary>API Resources 🕵️ </summary>

* `iris/api/ramp_meter` (primary)
* `iris/api/ramp_meter/{name}`

| Access       | Primary                | Secondary                        |
|--------------|------------------------|----------------------------------|
| 👁️  View      | name, location, status | geo\_loc                         |
| 👉 Operate   | lock                   |                                  |
| 💡 Manage    | notes                  | storage, max\_wait, algorithm, am\_target, pm\_target |
| 🔧 Configure | controller             | pin, meter\_type, beacon, preset |

</details>

## Setup

Field                  | Description
-----------------------|---------------------------------------------------
Notes                  | administrator notes, possibly including [hashtag]s
Meter Type             | number of metered lanes, etc.
Storage                | distance from meter to queue detector
Max Wait               | maximum allowed wait time (estimated)
Metering Algorithm     | **simple** or **density adaptive**
AM Target              | historical hourly AM target rate
PM Target              | historical hourly PM target rate
Advance Warning Beacon | beacon activated automatically when active

For basic time-of-day operation, the **simple** metering `algorithm` will run
the meter at a fixed release rate, equal to the target rate for the current
period (AM or PM).

The [density adaptive] algorithm requires a bit more configuration, since it
depends on the [road topology].  An _entrance_ [r_node] must exist, with
matching `roadway`, `road_dir`, `cross_street`, `cross_dir` and `cross_mod`.
This `r_node` must have one [green] detector, plus optional _queue_,
_passage_, _bypass_ and _merge_ detectors.

There are special rules for meters on [CD roads]:
* The meter's `roadway` must be the main road
* The entrance [r_node] must be on the CD road
* The name of the CD road must be the same as the main roadway, but contain
  the word-token "CD"

Advance warning beacons are flashing lights on a static sign, e.g. "PREPARE TO
STOP WHEN FLASHING".  Typically, they are hard-wired to flash when the meter
is operating, but they can be controlled by IRIS if that is not feasable.
In that case, beacons will also flash if a [merge] detector on the entrance
ramp has high occupancy (30%+).

## Locks

Operators can _lock_ a ramp meter either ON or OFF by selecting a lock reason.
OFF locks prevent the meter from turning on, and will stay in place until
manually removed.  ON locks have an expiration depending on the reason, and
the metering rate can be adjusted using the _Shrink_ and _Grow_ buttons.

Reason       | State    | Expiration
-------------|----------|-----------
incident     | OFF / ON | 30 minutes
testing      | OFF / ON | 5 minutes
knocked down | OFF      | N/A
indication   | OFF      | N/A
maintenance  | OFF      | N/A
construction | OFF      | N/A


[CD roads]: road_topology.html#rnode-transitions
[density adaptive]: density_adaptive.html
[green]: vehicle_detection.html#lane-type
[hashtag]: hashtags.html
[merge]: vehicle_detection.html#lane-type
[r_node]: road_topology.html#rnodes
[road topology]: road_topology.html
