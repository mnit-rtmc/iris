# Vehicle Detection

Real time vehicle data is used for several purposes:
- [Traffic maps](#traffic-layer)
- [Travel time]s
- [Ramp metering]
- [Tolling] rate calculation
- [Parking area] availability
- [Variable speed advisories]

Different techonlogies, such as _inductive loops_, _magnetometers_, _radar_ and
_video_ are available for detecting vehicles.  Collectively, these systems are
called **detectors**.

Every 30 seconds, the most recent collected data from all online detectors is
written to files.  An [XML file] called `det_sample.xml.gz` and a JSON file
called `station_sample` are generated.

<details>
<summary>API Resources 🕵️ </summary>

* `iris/detector_pub`
* `iris/station_sample`: collected station data for most recent period
* `iris/api/detector` (primary)
* `iris/api/detector/{name}`

| Access       | Primary           | Secondary                  |
|--------------|-------------------|----------------------------|
| 👁️  View      | name, label       | auto\_fail                 |
| 👉 Operate   |                   | field\_length, force\_fail |
| 💡 Manage    |                   | abandoned                  |
| 🔧 Configure | controller, notes | pin, r\_node, lane\_code, lane\_number, fake |

</details>

## Traffic Data

Detectors can collect several different types of data:

- **Count**: The number of vehicles in a period of time
- **Duration**: Interval of time a vehicle occupied a detector (ms)
- **Occupancy**: Percentage of time period detector was occupied
- **Speed**: Vehicle speed (mph)
- **Headway**: Interval of time between the start of consecutive vehicles (ms)
- **Length**: Physical length of vehicle (ft)

Some data can be _derived_ from collected data:

- **Flow**: Rate of vehicles travelling past a detector (vehicles per hour)
- **Density**: The number of vehicles per mile in a segment of road (one lane)
- **Speed**: If not collected, can be derived from flow divided by density

**Flow** is calculated by multiplying vehicle count by the number of time
periods in an hour.  For example, a count of 10 vehicles in 30 seconds equals
a flow rate of 1200, because there are 120 periods of 30 seconds in an hour.

**Density** can be derived by dividing **flow** (vehicles per hour) by **speed**
(miles per hour).  For detectors which cannot collect speed data, density can
be estimated using just **occupancy** and _field length_ (see below).

## Configuration

To create a detector, first select the [r_node] at the proper location.  Then
select the **Detectors** tab for that `r_node`.  Enter the detector **Name**
and press the **Create** button.

After selecting a detector in the `r_node` detector table, its properties, such
as **lane type** and **lane #** can be changed.  Lanes are numbered from
_right-to-left_, starting with the right lane as 1.  A **label** will be created
from this information, including abbreviations of the [roads] associated with
the [r_node].

**Field length** (ft) is the detection "field" of an average vehicle.  It is
used to derive density from occupancy, for detectors which cannot measure speed
directly.  For **Velocity** type detectors, this is the distance to the start
of the upstream mainline detector.  This enables recording individual vehicle
speeds ([Canoga] protocol only).

If a detector is no longer used, it can be marked **abandoned**.

### Lane Type

Lane Type  | Description
-----------|-----------------
Mainline   | Freeway mainline
Auxiliary  | Mainline auxiliary (ends within a mile)
CD Lane    | Collector / Distributor
Reversible | Reversible mainline
Merge      | Freeway on-ramp (counts all merging traffic)
Queue      | Ramp metering queue
Exit       | Freeway exit-ramp
Bypass     | Ramp meter bypass
Passage    | Ramp meter passage
Velocity   | Mainine speed loop
Omnibus    | Bus only
Green      | Ramp meter displayed green count
Wrong Way  | Exit-ramp wrong way detector
HOV        | High occupancy vehicles only
HOT        | High occupancy or tolling only
Shoulder   | Mainline shoulder
Parking    | Parking space presence detector

### Transfer

To move a detector to another `r_node`, select the target `r_node` and enter the
detector **Name**.  The current label for that detector will appear on the
right.  To move it to the current `r_node`, press the **Transfer** button.

## Detector Protocols

IRIS supports several different [protocols] for communicating with vehicle
detection systems.  The protocol used depends on the [comm link] of the
[controller] to which a detector is assigned.

Traffic data can be collected in two ways: [vehicle logging](#vehicle-logging)
and [binning](#binned-data) in fixed time periods.

Protocol               | Binning         | Traffic Data
-----------------------|-----------------|------------------------
[ADEC TDC]             | N/A             | [vlog]
[SmartSensor] 125 HD   | 5 sec to 1 hour | Count, Occupancy, Speed
[SmartSensor] 125 vlog | N/A             | [vlog]
[SmartSensor] 105      | 5 sec to 1 hour | Count, Occupancy, Speed
RTMS [G4]              | 5 sec to 1 hour | Count, Occupancy, Speed
RTMS [G4] vlog         | N/A             | [vlog]
[Natch]                | N/A             | [vlog]
[MnDOT-170]            | 30 sec          | Count, Occupancy
[Canoga]               | N/A             | [vlog]
[DR-500]               | 30-300? sec     | Speed
[DXM]                  | N/A             | Occupancy
[NTCIP]                | 0-255 sec       | Count, Occupancy

For protocols which allow the binning intereval to be adjusted, it will be set
to the [poll period] of the [comm config].

## Auto-Fail

Traffic data is continuously checked for five common failure conditions.  When
one of these first occurs and every hour that it persists, an [event] can be
stored in the `detector_event` database table.  The `detector_auto_fail_view`
can be used to check recent events.

If the `detector_auto_fail_enable` [system attribute] is `true`, the **auto
fail** flag for each detector will be set and cleared automatically whenever
these conditions change.

### No Hits

This failure condition occurs if no vehicles are counted for a duration
determined by the [lane type](#lane-type).  It clears immediately when a vehicle
is counted.

Lane Types                                        | Duration
--------------------------------------------------|----------------
Mainline, CD Lane, Velocity                       | 4 hours
Exit, Wrong Way, HOV                              | 8 hours
Queue, Passage, Merge                             | 12 hours
Auxiliary                                         | 24 hours
Bypass, Green, Omnibus, HOT, Reversible, Shoulder | 72 hours
Parking                                           | 2 weeks

### Chatter

If a detector reports an unreasonably high count of 38 vehicles or more in a 30
second period, this condition will be triggered.  It will be cleared if 24 hours
pass with all counts below that threshold.

### Locked On

This condition occurs if the detector reports 100% occupancy for a duration
determined by [lane type](#lane-type).  The condition will be cleared after
24 hours of good occupancy data.

Lane Type                                                      | Duration
---------------------------------------------------------------|---------
Parking                                                        | 2 weeks
Merge, Queue, Exit, Bypass, Passage, Omnibus, Green, Wrong Way | 30 minutes
All Others                                                     | 2 minutes

### No Change

If occupancy is greater than zero and does not change for the duration, this
condition will be triggered.  It will clear immediately if the occupancy
changes.

Lane Type  | Duration
-----------|---------
Parking    | 2 weeks
All others | 24 hours

### Occ Spike

A spike timer is kept for each non-parking detector.  For every 25% change in
occupancy between two consecutive data values, 30 seconds are added to the
timer.  If its value ever exceeds 60 seconds, the condidtion is triggered.
After every poll, 30 seconds are removed from the timer.  The condition will
be cleared after 24 hours of no spikes.

## Force Fail

If a detector has a fault which is not handled automatically, it can be **force
failed**.  This flag is only set manually, so it must be cleared once the
failure is corrected.

## Fake Detectors

When a detector is _failed_ (**auto fail** or **force fail**), its data will not
be used for [travel time], [ramp metering], _etc_.  In that case, **fake**
detection can be used — this field can contain one or more other detector names,
separated by spaces.  The average density or speed of those detectors (which are
not also failed) will be used instead.

## Traffic Layer

The IRIS client user interface includes a _traffic map layer_ which is created
automatically from the [road topology].  By default, this layer uses traffic
**density** to determine the color of each **segment**.  Other themes are
available for **speed** and **flow**.  The **Legend** menu at the top of the map
can be used to view the thresholds used for each color in a theme.

Every 30 seconds, the client will make an HTTP request for the current
[XML file].  The URL to locate that file is declared as a property in the
`/etc/iris/iris-client.properties` file (on the IRIS server).  The property is
`tdxml.detector.url`, and it should point to the `det_sample.xml.gz` [XML file],
as made available by `nginx` on the IRIS server.

The appearance of the _traffic map layer_ changes depending on the current zoom
level.  If the zoom level is below 10, the layer will not be visible.  At zoom
levels 10 through 13, the layer will display segments as aggregate of all
detectors in each mainline [station].  At zoom level 14 or above, each mainline
detector will be displayed as a separate segment.

The maximum distance between adjacent [station]s to draw segments on the map is
specified by the `map_segment_max_meters` [system attribute].  It is also the
maximum downstream distance for associating station data with a segment.

## Traffic Data Archiving

Collected data is archived only if the `detector_data_archive_enable`
[system attribute] is `true`.  The [Mayfly] service can be installed to make
this data available on the web.

Traffic data are stored in `/var/lib/iris/traffic`, in a directory with the
[district] name.  Within that directory a new subdirectory is created for each
year, with a 4-digit name (*e.g.* `2021`).

As data is collected, a new subdirectory is created every day — the name is
8-digits: _year_ `1994`-`9999`, _month_ `01`-`12` and _day-of-month_ `01`-`31`.
At 10 PM, all traffic data from the previous day is moved into a single ZIP file
with the 8-digit base name and a `.traffic` extension.

## Vehicle Logging

The `.vlog` format is a comma-separated text log.  Each vehicle event is
recorded as a single line of values, ending with a newline `\n` (U+000A).

Column | Name     | Description
-------|----------|----------------------------------------
1      | Duration | How long vehicle occupied detector (ms)
2      | Headway  | Time since previous vehicle (ms)
3      | Time     | Local 24-hour `HH:MM:SS` format
4      | Speed    | Vehicle speed (mph)
5      | Length   | Vehicle length (ft)

**Duration** is the time a vehicle occupied the detector area, between 1 and
60,000 ms.  An invalid or missing value is represented by a `?` (U+003F).

**Headway** is the difference in arrival time from the previous vehicle to the
current one.  It is a positive integer between 1 and 3,600,000 ms (1 hour).  An
invalid or missing value is represented by a `?` (U+003F).

**Time** is when the vehicle left the detection area.  Normally, this field is
left blank, but it is included when the headway is invalid or missing, or for
the first event after the beginning of each hour.  Changes due to daylight
saving time are not recorded.

**Speed** is the measured vehicle speed.  It is a positive integer value from 5
to 120 mph.  An invalid or missing value is left empty.

**Length** is the measured vehicle length.  It is a positive integer value from
1 to 255 ft.  An invalid or missing value is left empty.

All trailing commas at the end of a line are removed.  This means that an event
with only duration and headway would only contain the two values, separated by
one comma.

A gap in sampling data due to communication errors is represented by `*`
(U+002A) on a line by itself.

### Example Log

Interpreting example `.vlog` data for 11 vehicles:

Log Data            | Duration | Headway | Time     | Speed | Length
--------------------|----------|---------|----------|-------|-------
`296,9930,17:49:36` | 296      | 9930    | 17:49:36 |       |
`231,14069`         | 231      | 14069   | 17:49:50 |       |
`240,453,,45,18`    | 240      | 453     | 17:49:50 | 45    | 18
`496,23510,,53,62`  | 496      | 23510   | 17:50:14 | 53    | 62
`259,1321`          | 259      | 1321    | 17:50:15 |       |
`?,?`               | ?        | ?       |          |       |
`249,?`             | 249      | ?       | 17:50:24 |       |
`323,4638,17:50:28` | 323      | 4638    | 17:50:28 |       |
`258,5967,,55`      | 258      | 5967    | 17:50:33 | 55    |
`111,1542`          | 111      | 1542    | 17:50:35 |       |
`304,12029`         | 304      | 12029   | 17:50:47 |       |

## Binned Data

IRIS can collect these types of binned traffic data:

Data Type     | Description                              | Code | Size
--------------|------------------------------------------|------|------------
Vehicle Count | Count of vehicles detected               | v    | 8 bits
Occupancy     | 30-second scan count (0 to 1800)         | c    | 16 bits
Speed         | Average speed (mph) of detected vehicles | s    | 8 bits

A binned data file consists of some number of periods of equal duration.  The
first period begins (and the last period ends) at midnight.  The **binning
interval** determines the number of periods collected per day — a shorter
interval results in more periods.  If the interval is longer than 30 seconds,
the values are allocated evenly into 30-second bins for storage.

Period | Binning Interval | Values | Stored Bins
-------|------------------|--------|------------
5      | 5 seconds        | 17280  | 5 seconds
6      | 6 seconds        | 14400  | 6 seconds
10     | 10 seconds       | 8640   | 10 seconds
15     | 15 seconds       | 5760   | 15 seconds
20     | 20 seconds       | 4320   | 20 seconds
30     | 30 seconds       | 2880   | 30 seconds

For each detector, a binned data file is created for each **data type**.  The
base file name is the detector name.  The file extension is the **code** and
**period** (in seconds).  For example, 60-second vehicle counts collected from
detector 100 would be stored in a file called `100.v60`, containing 2880 bins.

Each data value is either an 8- or 16-bit signed integer, depending on the
data type.  16-bit value are in high-byte first order.  A negative value (-1)
indicates missing data.  Any data outside the valid ranges should be considered
_missing_.


[ADEC TDC]: protocols.html#adec-tdc
[Canoga]: protocols.html#canoga
[comm config]: comm_config.html
[comm link]: comm_links.html
[controller]: controllers.html
[district]: installation.html#server-properties
[DR-500]: protocols.html#dr-500
[DXM]: protocols.html#dxm
[event]: events.html
[G4]: protocols.html#g4
[IO pins]: controllers.html#io-pins
[Mayfly]: https://github.com/mnit-rtmc/iris/tree/master/mayfly
[MnDOT-170]: protocols.html#mndot-170
[Natch]: protocols.html#natch
[NTCIP]: protocols.html#ntcip
[Parking area]: parking_areas.html
[poll period]: comm_config.html#setup
[protocols]: protocols.html
[r_node]: road_topology.html#r_nodes
[Ramp metering]: ramp_meters.html
[road topology]: road_topology.html
[roads]: road_topology.html#roads
[SmartSensor]: protocols.html#smartsensor
[station]: road_topology.html#r_node-types
[system attribute]: system_attributes.html
[Tolling]: tolling.html
[Travel time]: travel_time.html
[Variable speed advisories]: vsa.html
[vlog]: #vehicle-logging
[XML file]: troubleshooting.html#xml-output
