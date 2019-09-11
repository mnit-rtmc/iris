## Traffic Map Layer

The IRIS client user interface includes a traffic map layer which is created
automatically from the [road topology].  By default, this layer uses traffic
**density** to determine the color of each **segment**.  Other themes are
available for **speed** and **flow**.  The **Legend** menu at the top of the map
can be used to view the thresholds used for each color in a theme.

Every 30 seconds, the client will make an HTTP request for the current
[traffic data].  The URL to locate that file is declared as a property in the
`/etc/iris/iris-client.properties` file (on the IRIS server).  The property is
`tdxml.detector.url`, and it should point to the `det_sample.xml.gz` [XML file],
as made available by apache on the IRIS server.

The appearance of the traffic map layer changes depending on the current zoom
level.  If the zoom level is below 10, the layer will not be visible.  At zoom
levels 10 through 13, the layer will display segments as aggregate of all
detectors in each mainline [station].  At zoom level 14 or above, each mainline
[detector] will be displayed as a separate segment.

The maximum distance between adjacent [station]s to draw segments on the map is
specified by the `map_segment_max_meters` [system attribute].  It is also the
maximum downstream distance for associating station data with a segment.


[detector]: admin_guide.html#vds
[road topology]: admin_guide.html#road_topology
[station]: admin_guide.html#station
[system attribute]: admin_guide.html#sys_attr
[traffic data]: admin_guide.html#traffic_data
[XML file]: troubleshooting.html#xml-output
