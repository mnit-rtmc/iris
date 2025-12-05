# Protocols

A [comm link] communicates with remote [controller]s using one of several
protocols, each of which supports specific [device] types.  Some also
support **multi-drop** addressing, allowing for more than one controller per
_comm link_.

## Device Control

These protocols support multiple [device] types:

| Device Type         | [NTCIP]  | [Natch] | MnDOT 170 |
|---------------------|----------|---------|-----------|
| [Alarm]             | ☑️        | ☑️       | ☑️         |
| [Beacon]            |          | ☑️       | ☑️         |
| [DMS]               | ☑️ _1203_ |         |           |
| [GPS]               | ☑️ _1204_ |         |           |
| [LCS]               | ☑️ _1203_ | ☑️       | ☑️         |
| [Ramp meter]        |          | ☑️       | ☑️         |
| [Vehicle detection] | ☑️ _1202_ | ☑️       | ☑️         |
| [Weather sensor]    | ☑️ _1204_ |         |           |

| Protocol | Default Scheme | Drops  |
|----------|----------------|--------|
| NTCIP A  | `udp`          | 1      |
| NTCIP B  | `tcp`          | 1-8191 |
| NTCIP C  | `tcp`          | 1      |
| Natch    | `tcp`          | 1      |
| MnDOT v4 | `tcp`          | 1-15   |
| MnDOT v5 | `tcp`          | 1-31   |

Advanced Traffic Controllers (ATC) using [Natch] and older 170-style
controllers using MnDOT-170 can have many devices:

| Device Type         | `#` | [IO Pin]s
|---------------------|-----|----------
| [Beacon]            | 1   | 2
| [Ramp meter]        | 2   | 2 - 3
| [LCS]               | 3   | 19 - 36
| [Vehicle detection] | 24  | 39 - 62
| [Alarm]             | 10  | 70 - 79

## Camera Control

Several protocols for [camera] control / [PTZ] are supported:

| Protocol      | Default Scheme | Drops  | [IO Pin]s |
|---------------|----------------|--------|-----------|
| [ONVIF]       | `http`         | 1      | 1         |
| [Axis]        | `http`         | 1      | 1         |
| [Cohu]        | `tcp`          | 1-223  | 1         |
| [Infinova]    | `tcp`          | 1-254  | 1         |
| [Pelco] D     | `udp`          | 1-254  | 1         |
| [Vicon]       | `udp`          | 1-254  | 1         |
| AD Manchester | `udp`          | 1-1024 | 1         |

## Video Control

Various [video] systems are supported:

| Protocol                  | Default Scheme | Drops  | [IO Pin]s |
|---------------------------|----------------|--------|-----------|
| [MonStream]               | `udp`          | 1      | 1-16      |
| Streambed ([flow stream]) | `tcp`          | 1      | 1-150     |
| [Pelco] P [keyboard]      | `tcp`          | 1      | N/A       |

## Gate Arm Control

Two protocols for [gate arm] control are supported.

| Protocol         | Default Scheme | Drops | [IO Pin]s |
|------------------|----------------|-------|-----------|
| [HySecurity STC] | `tcp`          | 1-99  | 1         |
| NDORv5           | `tcp`          | 1     | 1-8       |

## Beacon Control

| Protocol        | Model      | Default Scheme | Drops | [IO Pin]s |
|-----------------|------------|----------------|-------|-----------|
| [CBW]           |            | `http`         | 1     | _varies_  |
|                 | X-WR-1R12  |                |       | 1         |
|                 | X-301      |                |       | 1-2       |
|                 | X-401      |                |       | 1-2       |
|                 | X-310      |                |       | 1-4       |
|                 | X-410      |                |       | 1-4       |
|                 | X-WR-10R12 |                |       | 1-10      |
|                 | X-332      |                |       | 1-16      |
| [DLI] DIN Relay |            | `http`         | 1     | 1-8       |
| NDOT Beacon     |            | `tcp`          | 1     | 1         |

## Vehicle Detection

There are a few types of [vehicle detection] data:
 - **Binned**, time-based counts, occupancy, speed, classification
 - **Event**, per-vehicle, arrival time, speed, classification
 - **Presence**, suitable for [parking area] monitoring

| Protocol             | Default Scheme | Drops           | [IO Pin]s | Data Type |
|----------------------|----------------|-----------------|-----------|-----------|
| ADEC TDC             | `tcp`          | 1-255           | 1         | event     |
| Banner DXM           | `tcp`          | 1               | 11-86     | presence  |
| Canoga               | `tcp`          | 0-15; 128-255 † | 1-4       | event     |
| Central Park         | `https` ‡      | 1               | 1-64      | presence  |
| DR-500               | `tcp`          | 1               | 1         | [binned]  |
| RTMS G4              | `tcp`          | 0-65535         | 1-12      | [binned]  |
| RTMS G4 vlog         | `tcp`          | 0-65535         | 1-12      | event     |
| RTMS Echo vev        | `http` ⸸       | 1               | 1-`??`    | event     |
| SmartSensor 105      | `tcp`          | 1-9999          | 1-8       | [binned]  |
| SmartSensor 125 HD   | `tcp`          | 1-65534         | 1-8       | [binned]  |
| SmartSensor 125 vlog | `tcp`          | 1-65534         | 1-8       | event     |

† Backplane: 0-15, EEPROM: 128-255

‡ Use "Data per stall" endpoint (URI ending in `/integration/spot`)

⸸ Communicates using [pollinator] service

## GPS Devices

| Protocol   | Default Scheme | Drops | [IO Pin]s |
|------------|----------------|-------|-----------|
| RedLion    | `tcp`          | 1     | 1         |
| SierraGX   | `tcp`          | 1     | 1         |
| Sierra SSH | `tcp`          | 1     | 1         |

## Weather Data

Some protocols can be used to collect [weather sensor] data.

| Protocol       | Default Scheme | Drops | [IO Pin]s | Notes
|----------------|----------------|-------|-----------|--------------
| OSi ORG-815    | `tcp`          | 1     | 1         | precipitation
| Campbell Cloud | `http`         | 1     | 1         | separate service

## External Systems

Some protocols allow IRIS to poll external systems periodically using `http`
or `https`.  Typically, a single [controller] should be assigned and made
`ACTIVE`, but no devices need to be connected to IO pins.

| Protocol   | Description                         |
|------------|-------------------------------------|
| CAP-IPAWS  | [CAP] feed from Integrated Public Alert and Warning System [IPAWS].  [Alert]s can be used to automatically post weather and other messages to [DMS].  This requires an `https` URI provided by the Federal Emergency Management Agency. |
| CAP-NWS    | [CAP] feed from National Weather Service.  [Alert]s can be used to automatically post weather messages to [DMS]. |
| ClearGuide | [ClearGuide] external system feed   |
| Inc-Feed   | External [incident feed]            |
| Msg-Feed   | External [message feed]             |
| DMS-XML    | Legacy [DMS] system (drops 0-65535) |

## Tag Readers

The `e6` protocol can be used for collecting data from Transcore [tag readers].
The _default scheme_ is `udp`.  _Multi-drop_ is not supported.  One tag reader
can be associated with each [controller], using [IO pin] 1.


[alarm]: alarms.html
[alert]: alerts.html
[Axis]: https://www.axis.com/products/network-cameras
[beacon]: beacons.html
[binned]: vehicle_detection.html#binned-data
[camera]: cameras.html
[CAP]: http://docs.oasis-open.org/emergency/cap/v1.2/CAP-v1.2.html
[CBW]: https://controlbyweb.com/
[ClearGuide]: clearguide.html
[Cohu]: https://costarhd.com/
[comm link]: comm_links.html
[controller]: controllers.html
[device]: controllers.html#devices
[DLI]: https://dlidirect.com/
[DMS]: dms.html
[flow stream]: flow_streams.html
[gate arm]: gate_arms.html
[GPS]: gps.html
[HySecurity STC]: https://hysecurity.com/products/controllers/smart-touch-controller/
[incident feed]: incident_feed.html
[Infinova]: https://www.infinova.com/
[IO pin]: controllers.html#io-pins
[IPAWS]: https://www.fema.gov/emergency-managers/practitioners/integrated-public-alert-warning-system
[keyboard]: cameras.html#camera-keyboards
[LCS]: lcs.html
[message feed]: msg_feed.html
[monstream]: video.html#monstream
[Natch]: natch.html
[NTCIP]: https://www.ntcip.org/
[ONVIF]: https://en.wikipedia.org/wiki/ONVIF
[parking area]: parking_areas.html
[Pelco]: https://www.pelco.com/
[pollinator]: https://github.com/mnit-rtmc/iris/tree/master/pollinator
[PTZ]: cameras.html#pan-tilt-and-zoom
[ramp meter]: ramp_meters.html
[streambed]: https://github.com/mnit-rtmc/streambed
[tag readers]: tolling.html#tag-readers
[vehicle detection]: vehicle_detection.html
[vehicle logging]: vehicle_detection.html#vehicle-logging
[Vicon]: https://www.vicon.com/hardware/cameras/
[video]: video.html
[weather sensor]: weather_sensors.html
