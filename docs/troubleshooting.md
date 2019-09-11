## Troubleshooting

### Error Logs

Serious system errors are written to `stderr`, which is redirected to a file
at `/var/log/iris/iris.stderr`.  Check this file first when any problems are
suspected.

### XML Output

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

### Database Event Tables

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

### Debug Trace Logs

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
`slow`                 | DMS [slow warning] system log
`snmp`                 | SNMP error log
`sonar`                | SONAR connection log
`sql`                  | SQL database error log
`ss105`                | Wavetronix SS-105 protocol
`ss125`                | Wavetronix SS-125 protocol
`ssi`                  | [SSI] protocol
`stc`                  | [STC] protocol
`sys_attr`             | System attribute change log
`toll`                 | Tolling info log
`travel`               | Travel time info log
`vsa`                  | Variable speed advisory info log


[Canoga]: admin_guide.html#canoga
[CBW]: admin_guide.html#cbw
[DIN-Relay]: admin_guide.html#dinrelay
[district]: admin_guide.html#district
[DMS-XML]: admin_guide.html#dms_xml
[E6]: admin_guide.html#e6
[G4]: admin_guide.html#g4
[Infinova]: admin_guide.html#infinova
[Manchester]: admin_guide.html#manchester
[MnDOT-170]: admin_guide.html#mndot170
[Msg-Feed]: admin_guide.html#msg_feed
[NTCIP]: admin_guide.html#ntcip
[ORG-815]: admin_guide.html#org815
[Pelco-D]: admin_guide.html#pelcod
[slow warning]: admin_guide.html#slow
[SSI]: admin_guide.html#ssi
[station]: admin_guide.html#station
[STC]: admin_guide.html#stc
[system attribute]: admin_guide.html#sys_attr
[vehicle detection systems]: admin_guide.html#vds
