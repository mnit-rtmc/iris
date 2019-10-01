# Travel Time Estimation

The current **travel time** from a DMS to a downstream [station] can be
estimated and displayed on the sign.  A new estimate is made every 30 seconds,
based on speed data from [vehicle detection systems].

A `[tt…]` [action tag] can be used to specify a travel time within a
[quick message].  When the message is deployed, the tag will be replaced by the
estimated travel time, in minutes.  The tag has three parameters, separated by
commas.

 1. Destination [station ID]
 2. **Over limit** mode (`prepend` if omitted)
    - `blank`: do not display travel time when over limit
    - `prepend`: prepend over limit text before travel time
    - `append`: append over limit text after travel time
 3. Over limit text (`OVER ` if omitted)

NOTE: multiple destinations can be included on the same message by specifying
multiple `[tt…]` tags.

## Examples

```
FREEWAY TIME TO[nl][jl2]I-94[jl4][ttS100,prepend,OVER ] MIN
```

```
TIME TO[nl][jl2]I-35W[jl4][ttS200,append,+] MIN
```

```
DOWNTN[nl][ttS300,blank] MIN
```

## Route Pathfinding

This feature requires that the [road topology] is configured for the entire
route.  Using this topology, the shortest route is found from the DMS to the
destination [station].

A route consists of one or more _[corridor] trips_.  Each corridor trip has an
_origin_ and a _destination_, which are r_nodes associated with a single freeway
corridor.  For the first corridor trip, the origin is the DMS location.  A route
can only branch if an r_node with _exit_ [node type] is matched with an
_entrance_ r_node.  In order to match, the **roadway / direction** for the exit
r_node must equal the **cross street / direction** for the entrance, and vice
versa.

Two [system attribute]s control route pathfinding.  `route_max_legs` is the
maximum number of corridors to branch.  `route_max_miles` is the maximum total
route distance.

## Speed Smoothing

To deal with sampling problems at low speeds, a _smoothing_ procedure is used.
For each [station], **running average** and **running minimum** speeds are
calculated from all valid detectors in the station.  If a speed higher than the
[speed limit] is recorded, the speed limit is used instead.

The sample interval used increases as the speed becomes lower:

 - Normally 2 minutes
 - Extend to 3 minutes if < 25 mph
 - Extend to 4 minutes if < 20 mph
 - Extend to 5 minutes if < 15 mph

## Route Travel Time

The distance between each consecutive valid station is divided into 3 **links**
of equal length.  If any link is longer than 0.6 miles, a travel time estimate
would not be reliable, so the process is aborted.  The links which are adjacent
to a station are assigned speed from that station.  "Middle" links are assigned
an average of the immediate upstream and downstream station speeds.

Normally, the running average speed is used.  If the link is less than 1 mile
from the corridor destination, the running minimum is used instead.  Travel time
for each link is estimated by dividing its length by the assigned speed.  If the
route branches, each branch adds a 1 minute **turning delay**.

## Travel Time Limit

To prevent unreasonable messages from being displayed due to data errors, a
limit is calculated.  The total route length is divided by the value of the
`travel_time_min_mph` [system attribute].  The result, rounded up the to next 5
minutes, is the **travel time limit**.

If the calculated travel time is over the travel time limit, the message will be
**over limit**.  If the over limit mode is `blank`, no message will be shown.
Otherwise, the tag will be replaced with the travel time limit, with the over
limit text either prepended or appended.


[action tag]: action_plans.html#dms-action-tags
[corridor]: road_topology.html#corridors
[node type]: road_topology.html#r_node-types
[quick message]: dms.html#quick-messages
[road topology]: road_topology.html
[station]: road_topology.html#r_node-types
[station ID]: road_topology.html#station-id
[system attribute]: system_attributes.html
[vehicle detection systems]: vehicle_detection.html
[speed limit]: road_topology.html#speed-limit
