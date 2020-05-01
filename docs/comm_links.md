# Comm Links

Select `View ➔ Maintenance ➔ Comm Links` menu item

A **comm link** is a network connection to an external [device] or system.  IRIS
is capable of supporting thousands of simultaneous comm links.

## Modem

The **modem** flag indicates the connection uses a _dial-up_ or _cell_ modem.
The _comm link_ will be disconnected if communication is idle for longer than
the value of the `comm_idle_disconnect_modem_sec` [system attribute].

## URI

The **URI** includes a DNS host name or network IP address, and port number,
using the standard `host:port` convention.  It can also contain an optional
**scheme** prefix, which can be either `udp://`, `tcp://` or `modem://`.  If
present, the scheme will override the _default scheme_ for the selected
protocol.  For example, to use the [Pelco-D](#pelco-d) protocol over TCP
(instead of the default UDP), prepend `tcp://` to the URI.

## Poll Period

Poll **period** determines how frequently controllers on a comm link are polled.
It can range from 5 seconds to 24 hours.

## Timeout

After a poll, if a response is not received before the **timeout** expires, the
communicaton will fail.  For each poll, 2 retries will happen before the
operation is aborted.

## Protocols

**Protocol** determines what type of [device] or system is on the other end of
the comm link.  Each protocol supports specific device types.  Some protocols
support **multi-drop** addressing, with multiple controllers per _comm link_.

### Axis PTZ

The `axisptz` protocol can be used for [PTZ] control of Axis [camera]s.  The
_default scheme_ is `http`.  _Multi-drop_ is not supported.  One camera can be
associated with each [controller], using [IO pin] 1.

### Canoga

The `canoga` protocol can collect [vehicle detection] data, with
[vehicle logging] instead of [binned data].  The _default scheme_ is `tcp`.
_Multi-drop_ is supported with drops 1 - 255.  Up to 4 detectors can be
associated with each [controller], using [IO pin]s 1 - 4.

### CBW

The `cbw` protocol can be used for [beacons], using a Control-By-Web controller.
The _default scheme_ is `http`.  _Multi-drop_ is not supported.  Up to 8
[beacons] can be associated with each [controller], using [IO pin]s 1 - 8.

### Cohu

The `cohu` protocol can be used for [PTZ] control of Cohu [camera]s.  The
_default scheme_ is `tcp`.  _Multi-drop_ is supported with drops 1 - 223.  One
camera can be associated with each [controller], using [IO pin] 1.

### Din-Relay

The `dinrelay` protocol can be used for [changeable LCS] or [beacons], using a
DLI Din-Relay.  The _default scheme_ is `http`.  _Multi-drop_ is not supported.
Up to 8 [indications] can be associated with each [controller], using [IO pin]s
1 - 8.

### DMS-XML

`DMS-XML` is a protocol for legacy [DMS] control systems.  The _default scheme_
is `tcp`, with multi-drop (0-65535).

### DR-500

The Houston Radar DR-500 doppler radar can be used to collect _speed_ data only.
The _default scheme_ is `tcp`.  _Multi-drop_ is not supported.  Only one
detector can be associated with each [controller], using [IO pin] 1.

### DXM

The Banner Engineering DXM magnetometer can detect vehicle presence for
[parking area] monitoring.  The _default scheme_ is `tcp`.  _Multi-drop_ is not
supported.  Up to 76 detectors can be associated with each [controller], using
[IO pin]s 11 - 86.

### E6

The `e6` protocol can be used for collecting data from Transcore [tag readers].
The _default scheme_ is `udp`.  _Multi-drop_ is not supported.  One tag reader
can be associated with each [controller], using [IO pin] 1.

### G4

The `G4` protocol can collect [vehicle detection] data, including _vehicle
counts_, _occupancy_, _speed_ and _vehicle classification_.  The
_default scheme_ is `tcp`.  _Multi-drop_ is supported with drops 0 - 65535.
Up to 12 detectors can be associated with each [controller], using [IO pin]s
1 - 12.

### Inc-Feed

The `incfeed` protocol can be used to interface IRIS with an external system
that generates [incidents].  Periodically, IRIS will poll the URI (using `http`)
for incidents.

#### Incident Feed Format

The external system should respond with an ASCII text file, with one line per
active incident.

Each line should contain 7 fields, separated by comma characters `,` and
terminated with a single newline character `\n` (ASCII 0x0A).  The fields are:

1. **incident ID**
2. **type**: `CRASH`, `STALL`, `ROADWORK` or `HAZARD`
3. **incident detail**: may be blank, or one of the _incident detail_ names
4. **latitude**
5. **longitude**
6. **camera ID**: may be blank, or the ID of a [camera] to view the incident
7. **direction**: `NB`, `SB`, `EB` or `WB`

_Latitude_ and _longitude_ define coördinates using the WGS 84 datum.

### Infinova

The `infinova` protocol can be used for [PTZ] control of Infinova [camera]s.
The _default scheme_ is `tcp`.  _Multi-drop_ is supported with drops 1 - 254.
One camera can be associated with each [controller], using [IO pin] 1.

### Manchester

The `manchester` protocol can be used for [PTZ] control of some older [camera]s.
The _default scheme_ is `udp`.  _Multi-drop_ is supported with drops 1 - 1024.
One camera can be associated with each [controller], using [IO pin] 1.

### MnDOT-170

170 style controllers running the _MnDOT 170_ firmware can support several types
of [device]s:

Device Type         | `#` | [IO Pin]s
--------------------|-----|----------
[vehicle detection] | 24  | 39 - 62
[ramp meters]       | 2   | 2 - 3
[changeable LCS]    | 3   | 19 - 36
[beacons]           | 1   | 2
[alarms]            | 10  | 70 - 79

There are two versions of the protocol supported:

Version   | Default Scheme | Multi-Drop
----------|----------------|-----------
4         | `tcp`          | 1 - 15
5         | `tcp`          | 1 - 31

### MonStream

The `monstream` protocol can be used for switching of [monstream] video
monitors.  The _default scheme_ is `udp`.  _Multi-drop_ is not supported.
Up to 16 [video monitors] can be associated with each [controller], using
[IO pin]s 1 - 16.

### Msg-Feed

The `msgfeed` protocol can be used to interface with an external system that
generates [DMS] messages.  Periodically, IRIS will poll the URI (using `http`)
for DMS messages.

#### Msg-Feed Format

The external system should respond with an ASCII text file, with one line per
message to be deployed.

Each line must contain 3 fields, separated by tab characters `\t` (ASCII 0x09),
and terminated with a single newline character `\n` (ASCII 0x0A).  The fields
are **DMS name**, [MULTI] **string**, and **expiration time**.  The _DMS name_
must exactly match one of the DMS as identified by IRIS.  The _MULTI string_
specifies a message to display on the sign, using the _MULTI_ markup language.
The _expiration time_ field indicates the date and time that the message should
expire, in [RFC 3339] format: `yyyy-MM-dd HH:mm:ssZ`.
```
V66E37	CRASH[nl]5 MILES AHEAD[nl]LEFT LANE CLOSED	2019-10-02 11:37:00-0500
```

#### Msg-Feed Action Plan

An [action plan] is required to associate a [DMS action] with the feed.  The
_DMS action_ must have a [quick message] with a `feed` [action tag].  So, if the
_message feed_ is on a _Comm Link_ called `LFEED`, then the quick message
[MULTI] string must be `[feedLFEED]`.  Also, the _action plan_ must be active
and deployed.  This requirement allows only administrator-approved DMS to be
controlled by the message feed.

#### Msg-Feed Text

All messages used by the feed must be defined in the DMS message library.  This
requirement allows only administrator-approved messages to be deployed by the
_message feed_.  In some circumstances, it may be appropriate to disable this
checking.  For example, if the message feed host is fully trusted and there is
no possibility of man-in-the-middle attacks between IRIS and the feed host.  In
this case the `msg_feed_verify` [system attribute] can be set to `false` to
disable this check.

#### Msg-Feed Beacons

To activate DMS [beacons] through a message feed, configure 2 message feeds.
One is for DMS containing messages with activated beacons and the other for
messages with deactivated beacons.  There is no way to control which message
feed is executed first, so each message feed must list each DMS and at least one
of the message feeds must contain a blank _MULTI_ for each DMS.

### NTCIP

_National Transportation Communications for Intelligent transportation system
Protocol_ is supported for several different [device] types:

* [DMS] — _NTCIP 1203_
* [LCS], using _DMS_ for lane control
* [RWIS] — _NTCIP 1204_
* [vehicle detection] — _NTCIP 1202_
* [GPS] — _NTCIP 1204_

There are three supported variants:

Variant   | Default Scheme | Multi-Drop
----------|----------------|-----------
`NTCIP A` | `udp`          | No
`NTCIP B` | `tcp`          | 1 - 8191
`NTCIP C` | `tcp`          | No

### Org815

The `org815` protocol can be used to collect [rwis] data from an Org-815
precipitation sensor.  The _default scheme_ is `tcp`.  _Multi-drop_ is not
supported.  One device can be associated with each [controller], using [IO pin]
1.

### Pelco D

The `pelcod` protocol can be used for [PTZ] control of Pelco [camera]s.  The
_default scheme_ is `udp`.  _Multi-drop_ is supported with drops 1 - 254.  One
camera can be associated with each [controller], using [IO pin] 1.

### Pelco P

The `pelcop` protocol can be used for [camera keyboard] control from Pelco
keyboards.  The _default scheme_ is `tcp`.  _Multi-drop_ is not supported.
No [device]s need to be associated with the keyboard [controller].

### RedLion

The `redlion` protocol can be used for [GPS] data from RedLion modems.  The
_default scheme_ is `tcp`.  _Multi-drop_ is not supported.  One GPS modem can be
associated with each [controller], using [IO pin] 1.

### SierraGX

The `sierragx` protocol can be used for [GPS] data from SierraGX modems.  The
_default scheme_ is `tcp`.  _Multi-drop_ is not supported.  One GPS modem can be
associated with each [controller], using [IO pin] 1.

### SmartSensor

The SmartSensor (`SS105`) and SmartSensor HD (`SS125`) protocols can collect
[vehicle detection] data, including _vehicle counts_, _occupancy_, _speed_ and
_vehicle classification_.  The _default scheme_ is `tcp` for both.

Protocol | Multi-Drop | `#` | [IO Pin]s
---------|------------|-----|----------
`SS105`  | 1 - 9999   | 8   | 1 - 8
`SS125`  | 0 - 65535  | 8   | 1 - 8

### STC

The `stc` protocol can be used for [gate arm] control for Smart Touch gate arms.
The _default scheme_ is `tcp`.  _Multi-drop_ is supported with drops 1 - 99.
One gate arm can be associated with each [controller], using [IO pin] 1.

### Streambed

The `streambed` protocol is for [flow stream] configuration with a [streambed]
server.  The _default scheme_ is `tcp`.  _Multi-drop_ is not supported.
Up to 150 flow streams can be associated with each [controller], using
[IO pin]s.

### Vicon

The `vicon` protocol can be used for [PTZ] control of Vicon [camera]s.  The
_default scheme_ is `udp`.  _Multi-drop_ is supported with drops 1 - 254.  One
camera can be associated with each [controller], using [IO pin] 1.


[action plan]: action_plans.html
[action tag]: action_plans.html#dms-action-tags
[alarms]: alarms.html
[beacons]: beacons.html
[binned data]: vehicle_detection.html#binned-data
[camera]: cameras.html
[camera keyboard]: cameras.html#camera-keyboards
[changeable LCS]: lcs.html#changeable-lcs
[controller]: controllers.html
[device]: controllers.html#devices
[DMS]: dms.html
[DMS action]: action_plans.html#dms-actions
[flow stream]: flow_streams.html
[gate arm]: gate_arms.html
[GPS]: gps.html
[incidents]: incidents.html
[indications]: lcs.html#indications
[IO pin]: controllers.html#io-pins
[LCS]: lcs.html
[monstream]: video.html#monstream
[MULTI]: dms.html#multi
[parking area]: parking_areas.html
[PTZ]: cameras.html#pan-tilt-and-zoom
[quick message]: dms.html#quick-messages
[ramp meters]: ramp_meters.html
[RFC 3339]: https://tools.ietf.org/html/rfc3339#section-5.6
[RWIS]: rwis.html
[streambed]: https://github.com/mnit-rtmc/streambed
[system attribute]: system_attributes.html
[tag readers]: tolling.html#tag-readers
[vehicle detection]: vehicle_detection.html
[vehicle logging]: vehicle_detection.html#vehicle-logging
[video monitors]: video.html
