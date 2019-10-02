# Troubleshooting

## Error Logs

Serious system errors are written to `stderr`, which is redirected to a file
at `/var/log/iris/iris.stderr`.  Check this file first when any problems are
suspected.

## XML Output

There are a number of XML files which are written by the IRIS server
periodically.  These files contain configuration information about the system as
well as realtime data concerning the current state.  These files are stored in
the `/var/www/html/iris_xml/` directory.

Filename              | Period     | Description
----------------------|------------|---------------
`tms_config.xml.gz`   | 24 hours   | Configuration data for IRIS system.  If the [district] server property is changed, the filename will be changed to match `{district}_config.xml.gz`
`det_sample.xml.gz`   | 30 seconds | Sample data from [vehicle detection systems]
`stat_sample.xml.gz`  | 30 seconds | Mainline [station] data from [vehicle detection systems]
`incident.xml.gz`     | 30 seconds | Current incident information
`sign_message.xml.gz` | 30 seconds | Current DMS sign message information

## Database Event Tables

There are a number of event tables in the database for logging different types
of events.  Each of these tables has a **view** in the public DB schema.  There
is also a **purge threshold** for each table, stored as a [system attribute].
To disable purging older records from an event table, set the corresponding
purge threshold to 0.

View                       | Purge Threshold
---------------------------|----------------------
`action_plan_event_view`   | `action_plan_event_purge_days`
`alarm_event_view`         | `alarm_event_purge_days`
`beacon_event_view`        | `beacon_event_purge_days`
`camera_switch_event_view` | `camera_switch_event_purge_days`
`client_event_view`        | `client_event_purge_days`
`comm_event_view`          | `comm_event_purge_days`
`detector_event_view`      | `detector_event_purge_days`
`gate_arm_event_view`      | `gate_arm_event_purge_days`
`meter_event_view`         | `meter_event_purge_days`
`price_message_event_view` | `price_message_event_purge_days`
`sign_event_view`          | `sign_event_purge_days`
`tag_read_event_view`      | `tag_read_event_purge_days`

## Debug Trace Logs

There are a number of debugging logs which can be enabled in the `/var/log/iris`
directory.  To enable a particular log, use the `touch` command to create a file
with the proper name.  Some of these logs can grow very large, so be sure to
have enough disk space available.

Filename               | Description
-----------------------|-----------------
`{comm-link-name}.log` | Comm link log
`bottleneck`           | Bottleneck calculation for VSA algorithm
`canoga`               | [Canoga] protocol
`cbw`                  | [CBW] protocol
`device`               | Device error log
`dinrelay`             | [DIN-Relay] protocol
`dmsxml`               | [DMS-XML] protocol
`e6`                   | [E6] protocol
`e6_pkt`               | [E6] protocol packets
`feed`                 | [Msg-Feed] protocol
`g4`                   | [G4] protocol
`infinova`             | [Infinova] protocol
`kadaptive`            | K Adaptive metering algorithm
`manchester`           | [Manchester] protocol
`meter`                | Ramp meter configuration errors
`mndot170`             | [MnDOT-170] protocol
`modem`                | Modem error log
`ntcip`                | [NTCIP] protocols
`org815`               | [ORG-815] protocol
`pelcod`               | [Pelco-D] protocol
`polling`              | Generic operaton polling log
`prio`                 | Operation priority log
`profile`              | System profiling log
`sched`                | DMS scheduled message log
`slow`                 | DMS [slow traffic] warning system log
`snmp`                 | SNMP error log
`sonar`                | SONAR connection log
`sql`                  | SQL database error log
`ss105`                | Wavetronix [SS105] protocol
`ss125`                | Wavetronix [SS125] protocol
`stc`                  | [STC] protocol
`sys_attr`             | [System attribute] change log
`toll`                 | Tolling info log
`travel`               | Travel time info log
`vsa`                  | [Variable speed advisory] info log


[Canoga]: comm_links.html#canoga
[CBW]: comm_links.html#cbw
[DIN-Relay]: comm_links.html#din-relay
[district]: installation.html#server-properties
[DMS-XML]: comm_links.html#dms-xml
[E6]: comm_links.html#e6
[G4]: comm_links.html#g4
[Infinova]: comm_links.html#infinova
[Manchester]: comm_links.html#manchester
[MnDOT-170]: comm_links.html#mndot-170
[Msg-Feed]: comm_links.html#msg-feed
[NTCIP]: comm_links.html#ntcip
[ORG-815]: comm_links.html#org815
[Pelco-D]: comm_links.html#pelco-d
[slow traffic]: slow_warning.html
[station]: road_topology.html#r_node-types
[SS105]: comm_links.html#smartsensor
[SS125]: comm_links.html#smartsensor
[STC]: comm_links.html#stc
[system attribute]: system_attributes.html
[Variable speed advisory]: vsa.html
[vehicle detection systems]: vehicle_detection.html
