# Protocols

A [comm link] communicates with remote [controller]s using one of several
protocols, each of which supports specific [device] types.  Some also
support **multi-drop** addressing, with more than one controller per
_comm link_.

## Device Control

Protocol        | Beacon | Camera | DMS | Detection | Gates | GPS | Weather
----------------|--------|--------|-----|-----------|-------|-----|--------
ADEC TDC        |        |        |     | ✔️        |       |     |
Axis PTZ        |        | ✔️     |     |           |       |     |
Banner DXM      |        |        |     | ✔️        |       |     |
Canoga          |        |        |     | ✔️        |       |     |
CBW             | ✔️     |        |     |           |       |     |
Central Park    |        |        |     | ✔️        |       |     |
Cohu PTZ        |        | ✔️     |     |           |       |     |
DLI DIN Relay   | ✔️     |        |     |           |       |     |
DMS XML         |        |        | ✔️  |           |       |     |
DR-500          |        |        |     | ✔️        |       |     |
Gate NDORv5     |        |        |     |           | ✔️    |     |
HySecurity STC  |        |        |     |           | ✔️    |     |
Infinova PTZ    |        | ✔️     |     |           |       |     |
Manchester PTZ  |        | ✔️     |     |           |       |     |
MnDOT 170       | ✔️     |        |     | ✔️        |       |     |
Natch           | ✔️     |        |     | ✔️        |       |     |
NDOT Beacon     | ✔️     |        |     |           |       |     |
NTCIP           |        |        | ✔️  | ✔️        |       |     | ✔️
ONVIF PTZ       |        | ✔️     |     |           |       |     |
OSi ORG-815     |        |        |     |           |       |     | ✔️
Pelco D PTZ     |        | ✔️     |     |           |       |     |
RedLion GPS     |        |        |     |           |       | ✔️  |
RTMS G4         |        |        |     | ✔️        |       |     |
SierraGX        |        |        |     |           |       | ✔️  |
SmartSensor     |        |        |     | ✔️        |       |     |
Vicon PTZ       |        | ✔️     |     |           |       |     |

## ADEC TDC

The `ADEC TDC` protocol can collect [vehicle detection] data, logging event
data for every vehicle.  The _default scheme_ is `tcp`.  _Multi-drop_ is
supported with drops 1 - 255.  One detector can be associated with each
[controller], using [IO pin] 1.

## Axis PTZ

The `axisptz` protocol can be used for [PTZ] control of Axis [camera]s.  The
_default scheme_ is `http`.  _Multi-drop_ is not supported.  One camera can be
associated with each [controller], using [IO pin] 1.

## CampbellCloud

The `CampbellCloud` protocol is for [weather sensor] data collected by
Campbell Scientific, and retrieved through their cloud API.

## Canoga

The `canoga` protocol can collect [vehicle detection] data, with
[vehicle logging] instead of [binned data].  The _default scheme_ is `tcp`.
_Multi-drop_ is supported with drops 0 - 15 (backplane) or 128 - 255 (EEPROM).
Up to 4 detectors can be associated with each [controller], using [IO pin]s
1 - 4.

## CAP-IPAWS

This Common Alerting Protocol [CAP] is used for polling the Integrated Public
Alert and Warning System [IPAWS].  [Alerts] can be used to automatically post
weather and other messages to Dynamic Message Signs.  This requires an `HTTPS`
URI provided by the Federal Emergency Management Agency and a [controller] set
to `ACTIVE` condition.

## CAP-NWS

This Common Alerting Protocol [CAP] is used for polling the National Weather
Service weather feed.  [Alerts] can be used to automatically post weather
messages to Dynamic Message Signs.  This requires a [controller] set to
`ACTIVE` condition.

## CBW

The `cbw` protocol can be used for [beacons], using a Control-By-Web controller.
The _default scheme_ is `http`.  _Multi-drop_ is not supported.  Depending on
the model, up to 16 [beacons] can be associated with each [controller].

The [IO Pin]s are outputs for controlling relays.

| Model Number | IO Pins |
|--------------|---------|
| X-WR-1R12    | 1       |
| X-301        | 1 - 2   |
| X-401        | 1 - 2   |
| X-310        | 1 - 4   |
| X-410        | 1 - 4   |
| X-WR-10R12   | 1 - 10  |
| X-332        | 1 - 16  |

## Central Park

The Drivewyze Central Park system can detect vehicle presence for
[parking area] monitoring.  The _default scheme_ is `https`.  _Multi-drop_ is
not supported.  Up to 64 detectors can be associated with each [controller],
using [IO pin]s 1 - 64.  The comm link URI must be the "Data per stall"
endpoint (ending in `/integration/spot`).

## ClearGuide

The `clearguide` protocol can be used for to connect with a [ClearGuide]
external system feed.

## Cohu

The `cohu` protocol can be used for [PTZ] control of Cohu [camera]s.  The
_default scheme_ is `tcp`.  _Multi-drop_ is supported with drops 1 - 223.  One
camera can be associated with each [controller], using [IO pin] 1.

## Din-Relay

The `dinrelay` protocol can be used for [changeable LCS] or [beacons], using a
DLI Din-Relay.  The _default scheme_ is `http`.  _Multi-drop_ is not supported.
Up to 8 [indications] can be associated with each [controller], using [IO pin]s
1 - 8.

## DMS-XML

`DMS-XML` is a protocol for legacy [DMS] control systems.  The _default scheme_
is `tcp`, with multi-drop (0-65535).

## DR-500

The Houston Radar DR-500 doppler radar can be used to collect _speed_ data only.
The _default scheme_ is `tcp`.  _Multi-drop_ is not supported.  Only one
detector can be associated with each [controller], using [IO pin] 1.

## DXM

The Banner Engineering DXM magnetometer can detect vehicle presence for
[parking area] monitoring.  The _default scheme_ is `tcp`.  _Multi-drop_ is not
supported.  Up to 76 detectors can be associated with each [controller], using
[IO pin]s 11 - 86.

## E6

The `e6` protocol can be used for collecting data from Transcore [tag readers].
The _default scheme_ is `udp`.  _Multi-drop_ is not supported.  One tag reader
can be associated with each [controller], using [IO pin] 1.

## G4

The `G4` protocol can collect [vehicle detection] data, including _vehicle
counts_, _occupancy_, _speed_ and _vehicle classification_.  The
_default scheme_ is `tcp`.  _Multi-drop_ is supported with drops 0 - 65535.
Up to 12 detectors can be associated with each [controller], using [IO pin]s
1 - 12.

## Inc-Feed

The `incfeed` protocol can be used to interface IRIS with an external system
that generates [incidents].  Periodically, IRIS will poll the URI (using `http`)
for incidents.

### Incident Feed Format

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

## Infinova

The `infinova` protocol can be used for [PTZ] control of Infinova [camera]s.
The _default scheme_ is `tcp`.  _Multi-drop_ is supported with drops 1 - 254.
One camera can be associated with each [controller], using [IO pin] 1.

## Manchester

The `manchester` protocol can be used for [PTZ] control of some older [camera]s.
The _default scheme_ is `udp`.  _Multi-drop_ is supported with drops 1 - 1024.
One camera can be associated with each [controller], using [IO pin] 1.

## MnDOT-170

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

## MonStream

The `monstream` protocol can be used for switching of [monstream] video
monitors.  The _default scheme_ is `udp`.  _Multi-drop_ is not supported.
Up to 16 [video monitors] can be associated with each [controller], using
[IO pin]s 1 - 16.

## Msg-Feed

The `msgfeed` protocol can be used to interface with an external system that
generates [DMS] messages.  Periodically, IRIS will poll the URI (using `http`)
for DMS messages.

The external system should respond with an ASCII text file, with one line per
message to be deployed.  Each line contains 3 fields: `dms`, `message` and
`expire`, separated by tab characters `\t` (ASCII 0x09), and terminated with a
single newline character `\n` (ASCII 0x0A).

```
V66E37\tSNOW PLOW[nl]AHEAD[nl]USE CAUTION\t2022-10-02 11:37:00-05:00
```

`dms`: Name of the sign to deploy, which must have the [hashtag] referenced
by a [device action].  Additionally, that action must be associated with the
current phase of an active [action plan].  The [message pattern] of the
_device action_ must be a `feed` [action tag].  For example, if the `msgfeed`
_Comm Link_ name is `XYZ`, then the pattern must be `[feedXYZ]`.

`multi`: Message to deploy, using the [MULTI] markup language.  Each line of
the message must exist in the pattern's library.  This check allows only
"administrator-approved" messages, but it can be disabled by changing the
`msg_feed_verify` [system attribute] to `false`.  **WARNING**: only disable
this check if the message feed host is fully trusted, and there is no
possibility of man-in-the-middle attacks.

`expire`: Date/time when the message will expire, using [RFC 3339]
`full-date` / `full-time` separated by a space.  The message will not be
displayed after this time.  Leave `expire` blank to cancel a previous message.

## Natch

Advanced Traffic Controllers (ATC) using the [Natch protocol] can support
several types of [device]s:

- [alarms]
- [beacons]
- [changeable LCS]
- [ramp meters]
- [vehicle detection]

## NTCIP

_National Transportation Communications for Intelligent transportation system
Protocol_ is supported for several different [device] types:

* [DMS] — _NTCIP 1203_
* [LCS], using _DMS_ for lane control
* [weather sensor] — _NTCIP 1204_ environmental sensor station (ESS)
* [vehicle detection] — _NTCIP 1202_
* [GPS] — _NTCIP 1204_

There are three supported variants:

Variant   | Default Scheme | Multi-Drop
----------|----------------|-----------
`NTCIP A` | `udp`          | No
`NTCIP B` | `tcp`          | 1 - 8191
`NTCIP C` | `tcp`          | No

## Org815

The `org815` protocol can be used to collect [weather sensor] data from an
Org-815 precipitation sensor.  The _default scheme_ is `tcp`.  _Multi-drop_ is
not supported.  One device can be associated with each [controller], using
[IO pin] 1.

## Pelco D

The `pelcod` protocol can be used for [PTZ] control of Pelco [camera]s.  The
_default scheme_ is `udp`.  _Multi-drop_ is supported with drops 1 - 254.  One
camera can be associated with each [controller], using [IO pin] 1.

## Pelco P

The `pelcop` protocol can be used for [camera keyboard] control from Pelco
keyboards.  The _default scheme_ is `tcp`.  _Multi-drop_ is not supported.
No [device]s need to be associated with the keyboard [controller].

## RedLion

The `redlion` protocol can be used for [GPS] data from RedLion modems.  The
_default scheme_ is `tcp`.  _Multi-drop_ is not supported.  One GPS modem can be
associated with each [controller], using [IO pin] 1.

## SierraGX

The `sierragx` protocol can be used for [GPS] data from SierraGX modems.  The
_default scheme_ is `tcp`.  _Multi-drop_ is not supported.  One GPS modem can be
associated with each [controller], using [IO pin] 1.

## SmartSensor

There are three SmartSensor protocols for collecting [vehicle detection] data
from Wavetronix sensors.  The `105` and `125 HD` protocols collect binned
_vehicle counts_, _occupancy_, _speed_ and _vehicle classification_.  The
`125 vlog` protocol logs event data for every vehicle.  The _default scheme_ is
`tcp`.

Protocol               | Multi-Drop | `#` | [IO Pin]s
-----------------------|------------|-----|----------
`SmartSensor 105`      | 1 - 9999   | 8   | 1 - 8
`SmartSensor 125 HD`   | 1 - 65534  | 8   | 1 - 8
`SmartSensor 125 vlog` | 1 - 65534  | 8   | 1 - 8

## STC

The `stc` protocol can be used for [gate arm] control for Smart Touch gate arms.
The _default scheme_ is `tcp`.  _Multi-drop_ is supported with drops 1 - 99.
One gate arm can be associated with each [controller], using [IO pin] 1.

## Streambed

The `streambed` protocol is for [flow stream] configuration with a [streambed]
server.  The _default scheme_ is `tcp`.  _Multi-drop_ is not supported.
Up to 150 flow streams can be associated with each [controller], using
[IO pin]s.

## Vicon

The `vicon` protocol can be used for [PTZ] control of Vicon [camera]s.  The
_default scheme_ is `udp`.  _Multi-drop_ is supported with drops 1 - 254.  One
camera can be associated with each [controller], using [IO pin] 1.


[action plan]: action_plans.html
[action tag]: action_plans.html#action-tags
[alarms]: alarms.html
[beacons]: beacons.html
[binned data]: vehicle_detection.html#binned-data
[camera]: cameras.html
[camera keyboard]: cameras.html#camera-keyboards
[CAP]: http://docs.oasis-open.org/emergency/cap/v1.2/CAP-v1.2.html
[changeable LCS]: lcs.html#changeable-lcs
[ClearGuide]: clearguide.html
[comm link]: comm_links.html
[controller]: controllers.html
[device]: controllers.html#devices
[device action]: action_plans.html#device-actions
[DMS]: dms.html
[flow stream]: flow_streams.html
[gate arm]: gate_arms.html
[GPS]: gps.html
[hashtag]: hashtags.html
[incidents]: incidents.html
[indications]: lcs.html#indications
[IO pin]: controllers.html#io-pins
[IPAWS]: https://www.fema.gov/emergency-managers/practitioners/integrated-public-alert-warning-system
[LCS]: lcs.html
[message pattern]: message_patterns.html
[monstream]: video.html#monstream
[MULTI]: multi.html
[Natch protocol]: natch.html
[parking area]: parking_areas.html
[PTZ]: cameras.html#pan-tilt-and-zoom
[ramp meters]: ramp_meters.html
[RFC 3339]: https://tools.ietf.org/html/rfc3339#section-5.6
[streambed]: https://github.com/mnit-rtmc/streambed
[system attribute]: system_attributes.html
[tag readers]: tolling.html#tag-readers
[vehicle detection]: vehicle_detection.html
[vehicle logging]: vehicle_detection.html#vehicle-logging
[video monitors]: video.html
[weather sensor]: weather_sensors.html
