# Vehicle Detection Systems

There are several types of **vehicle detection systems** (VDS).  The earliest of
these is the _inductive loop_, which is a wire looped under the road surface.
Some systems use _radar_ mounted on the side of the road.  _Video detection_
uses a camera and computer vision software.  Collectively, these systems are
simply called **detectors**.

## Configuration

To create a detector, first select the [r_node] at the proper location.  Then
select the **Detectors** tab for that r_node.  Enter the detector **Name** and
press the **Create** button.

After selecting a detector in the r_node detector table, its properties, such as
**lane type** and **lane #** can be changed.  Lanes are numbered from
_right-to-left_, starting with the right lane as 1.  A **label** will be created
from this information, including abbreviations of the [roads] associated with
the [r_node].

The **field length** of a detector determines how density and speed are
estimated from counts and occupancy.  It is in units of feet.

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

## Transfer

It is possible to move a detector to another r_node.  Select the target r_node
and enter the detector **Name**.  The current label for that detector will
appear on the right.  To move it to the current r_node, press the **Transfer**
button.

## Traffic Data

Most detectors sample traffic data in fixed time intervals and put it into
[bins](#binned-data).  IRIS can store these types of binned traffic data:

Sample Type      | Description                              | Code | Sample Size
-----------------|------------------------------------------|------|------------
Vehicle Count    | Count of vehicles detected               | v    | 8 bits
Motorcycle Count | Count of vehicles up to 7 feet           | vmc  | 8 bits
Small Count      | Count of vehicles between 7 and 20 feet  | vs   | 8 bits
Medium Count     | Count of vehicles between 20 and 43 feet | vm   | 8 bits
Large Count      | Number of vehicles 43 feet or longer     | vl   | 8 bits
Occupancy        | Percent _occupancy_ count (0 to 100.00)  | op   | 16 bits
Scans            | Scan _occupancy_ count (0 to 1800)       | c    | 16 bits
Speed            | Average speed (mph) of detected vehicles | s    | 8 bits

Instead of binning, some detectors can record a [vehicle log](#vehicle-logging),
with information about each detected vehicle, such as headway and speed.

Every 30 seconds, an XML file is generated containing the most recent sample
data from all defined detectors.  The file is called `det_sample.xml.gz`, and it
is written to the [XML output directory].

## Detector Protocols

IRIS supports several different [protocols] for communicating with vehicle
detection systems.  The protocol used depends on the [comm link] of the
[controller] to which it is assigned.  The following table summarizes features
of each protocol.

Protocol    | Binning               | Traffic Data
------------|-----------------------|-----------------
[NTCIP]     | 0-255 seconds         | Count, Occupancy
[MnDOT-170] | 30 seconds            | Count, Scans
[SS105]     | 5 seconds to 1 month  | Count, Occupancy, Speed, Classification
[SS125]     | 5 seconds to 1 month  | Count, Occupancy, Speed, Classification
[G4]        | 10 seconds to 1 hour  | Count, Occupancy, Speed, Classification
[Canoga]    | N/A [vehicle logging] | Timestamp, Speed (double loops)
[DR-500]    | 30-300? seconds       | Speed
[DXM]       | N/A (presence)        | Magnetic Field

For protocols which allow the binning intereval to be adjusted, it will be set
to the poll [period] of the comm link.

## Auto Fail

Traffic data is continuously checked for five common failure conditions.  When
one of these first occurs and every hour that it persists, an event is logged in
the `detector_event` database table.  The `detector_auto_fail_view` can be used
to check recent events.

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
determined by [lane type](#lane-type).  It is also sustained if the occupancy
drops to zero with no intervening values.  The condition will be cleared after
24 hours of good occupancy data.

Lane Type                                                              | Duration
-----------------------------------------------------------------------|--------
Mainline, Auxiliary, CD Lane, Reversible, Velocity, HOV, HOT, Shoulder | 2 minutes
Merge, Queue, Exit, Bypass, Passage, Omnibus, Green, Wrong Way         | 30 minutes
Parking                                                                | 2 weeks

### No Change

If occupancy is greater than zero and does not change for 24 hours, this
condition will be triggered.  It will clear immediately if the occupancy
changes.

### Occ Spike

A spike timer is kept for each detector.  For every 25% change in occupancy
between two consecutive data samples, 30 seconds are added to the timer.  If its
value ever exceeds 60 seconds, the condidtion is triggered.  After every poll,
30 seconds are removed from the timer.  The condition will be cleared after 24
hours of no spikes.

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

## Traffic Data Archiving

Sample data is archived only if the `sample_archive_enable` [system attribute]
is `true`.  Files are stored in `/var/lib/iris/traffic`, in a directory with the
[district] name.  Within that directory a new subdirectory is created for each
year, with a 4-digit name (`1994`-`9999`).

As data is collected, a new subdirectory is created every day — the name is
8-digits: _year_ `1994`-`9999`, _month_ `01`-`12` and _day-of-month_ `01`-`31`.
At 10 PM, all traffic data from the previous day is moved into a single ZIP file
with the 8-digit base name and a `.traffic` extension.

## Binned Data

A binned sample file consists of some number of periods of equal duration.  The
first period begins (and the last period ends) at midnight.  The **binning
interval** determines the number of samples collected per day — a shorter
interval results in more samples.  If the period is longer than 30 seconds, the
samples are allocated evenly into 30-second bins for storage.

Period | Binning Interval | Samples | Stored Bins
-------|------------------|---------|------------
5      | 5 seconds        | 17280   | 5 seconds
6      | 6 seconds        | 14400   | 6 seconds
10     | 10 seconds       | 8640    | 10 seconds
15     | 15 seconds       | 5760    | 15 seconds
20     | 20 seconds       | 4320    | 20 seconds
30     | 30 seconds       | 2880    | 30 seconds
60     | 60 seconds       | 1440    | 30 seconds
90     | 90 seconds       | 960     | 30 seconds
120    | 2 minutes        | 720     | 30 seconds
240    | 4 minutes        | 360     | 30 seconds
300    | 5 minutes        | 288     | 30 seconds
600    | 10 minutes       | 144     | 30 seconds
900    | 15 minutes       | 96      | 30 seconds
1200   | 20 minutes       | 72      | 30 seconds
1800   | 30 minutes       | 48      | 30 seconds
3600   | 60 minutes       | 24      | 30 seconds
7200   | 2 hours          | 12      | 30 seconds
14400  | 4 hours          | 6       | 30 seconds
28800  | 8 hours          | 3       | 30 seconds
43200  | 12 hours         | 2       | 30 seconds
86400  | 24 hours         | 1       | 30 seconds

For each detector, a binned sample file is created for each
[traffic data](#traffic-data) **sample type**.  The base file name is the
detector name.  The extension is the traffic data **code** followed by the
**period** (in seconds).  For example, 60-second vehicle count samples collected
from detector 100 would be stored in a file called `100.v60`, containing 2880
bins.

Each data sample is either an 8- or 16-bit signed integer, depending on the
sample type.  16-bit samples are in high-byte first order.  A negative value
(-1) indicates a missing sample.  Any data outside the valid ranges should be
considered _bad_.

## Vehicle Logging

The `.vlog` format is a comma-separated text log with one line for each vehicle
detected.  Each line ends with a newline `\n` (ASCII 0x0A).  If present,
**duration**, **headway**, and **speed** are positive integer values.  Missing
duration or headway values are represented by a `?` character.  A gap in
sampling data is represented by `*` on a line by itself.

Column | Name       | Description
-------|------------|---------------------------------------------------------
1      | Duration   | Number of milliseconds the vehicle occupied the detector
2      | Headway    | Number of milliseconds bewteen vehicle start times
3      | Time stamp | 24-hour `HH:MM:SS` format (may be omitted if headway is valid)
4      | Speed      | Speed in miles per hour (if available)

### Example Log

Interpreting example `.vlog` data for 11 vehicles:

Log Data            | Duration | Headway | Time     | Speed
--------------------|----------|---------|----------|-------
`296,9930,17:49:36` | 296      | 9930    | 17:49:36 | ?
`231,14069`         | 231      | 14069   | 17:49:50 | ?
`240,453,,45`       | 240      | 453     | 17:49:50 | 45
`296,23510,,53`     | 296      | 23510   | 17:50:14 | 53
`259,1321`          | 259      | 1321    | 17:50:15 | ?
`?,?`               | ?        | ?       | ?        | ?
`249,?`             | 249      | ?       | 17:50:24 | ?
`323,4638,17:50:28` | 323      | 4638    | 17:50:28 | ?
`258,5967`          | 258      | 5967    | 17:50:33 | ?
`111,1542`          | 111      | 1542    | 17:50:35 | ?
`304,12029`         | 304      | 12029   | 17:50:47 | ?

## Traffic Layer

The IRIS client user interface includes a _traffic map layer_ which is created
automatically from the [road topology].  By default, this layer uses traffic
**density** to determine the color of each **segment**.  Other themes are
available for **speed** and **flow**.  The **Legend** menu at the top of the map
can be used to view the thresholds used for each color in a theme.

Every 30 seconds, the client will make an HTTP request for the current
[traffic data](#traffic-data).  The URL to locate that file is declared as a
property in the `/etc/iris/iris-client.properties` file (on the IRIS server).
The property is `tdxml.detector.url`, and it should point to the
`det_sample.xml.gz` [XML file], as made available by apache on the IRIS server.

The appearance of the _traffic map layer_ changes depending on the current zoom
level.  If the zoom level is below 10, the layer will not be visible.  At zoom
levels 10 through 13, the layer will display segments as aggregate of all
detectors in each mainline [station].  At zoom level 14 or above, each mainline
detector will be displayed as a separate segment.

The maximum distance between adjacent [station]s to draw segments on the map is
specified by the `map_segment_max_meters` [system attribute].  It is also the
maximum downstream distance for associating station data with a segment.


[Canoga]: comm_links.html#canoga
[comm link]: comm_links.html
[controller]: controllers.html
[district]: installation.html#server-properties
[DR-500]: comm_links.html#dr-500
[DXM]: comm_links.html#dxm
[G4]: comm_links.html#g4
[IO pins]: controllers.html#io-pins
[MnDOT-170]: comm_links.html#mndot-170
[NTCIP]: comm_links.html#ntcip
[period]: comm_links.html#poll-period
[protocols]: comm_links.html#protocols
[r_node]: road_topology.html#r_nodes
[ramp metering]: ramp_meters.html
[road topology]: road_topology.html
[roads]: road_topology.html#roads
[SS105]: comm_links.html#smartsensor
[SS125]: comm_links.html#smartsensor
[station]: road_topology.html#r_node-types
[system attribute]: system_attributes.html
[travel time]: travel_time.html
[vehicle logging]: #vehicle-logging
[XML file]: troubleshooting.html#xml-output
[XML output directory]: troubleshooting.html#xml-output
