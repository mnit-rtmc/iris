# Protocols

A [comm link] communicates with remote [controller]s using one of several
protocols, each of which supports specific [device] types.  Some also
support **multi-drop** addressing, allowing for more than one controller per
_comm link_.

| Protocol         | Default Scheme | Drops           | Device Types        |
|------------------|----------------|-----------------|---------------------|
| [ADEC TDC]       | `tcp`          | 1-255           | [vehicle detection] |
| [Axis]           | `http`         | 1               | [camera]            |
| [Banner DXM]     | `tcp`          | 1               | [vehicle detection] |
| [CampbellCloud]  | `https` †      | 1               | [external]          |
| Canoga           | `tcp`          | 0-15; 128-255 ⸸ | [vehicle detection] |
| CAP-IPAWS        | `https`        | 1               | [external]          |
| CAP-NWS          | `https`        | 1               | [external]          |
| [CBW]            | `http`         | 1               | [beacon]            |
| [Central Park]   | `https`        | 1               | [external]          |
| [ClearGuide]     | `http`         | 1               | [external]          |
| [Cohu]           | `tcp`          | 1-223           | [camera]            |
| [Cohu] Helios    | `tcp`          | 1-223           | [camera]            |
| [DLI] DIN Relay  | `http`         | 1               | [beacon]            |
| DMS-XML          | `tcp`          | 0-65535         | [DMS]               |
| [DR-500]         | `tcp`          | 1               | [vehicle detection] |
| [HySecurity STC] | `tcp`          | 1-99            | [gate arm]          |
| Inc-Feed         | `http`         | 1               | [external]          |
| [Infinova] D     | `tcp`          | 1-254           | [camera]            |
| Manchester (AD)  | `udp`          | 1-1024          | [camera]            |
| MnDOT 4-bit      | `tcp`          | 1-15            | [MnDOT devices]     |
| MnDOT 5-bit      | `tcp`          | 1-31            | [MnDOT devices]     |
| [MonStream]      | `udp`          | 1               | [video]             |
| Msg-Feed         | `http`         | 1               | [external]          |
| Natch            | `tcp`          | 1               | [MnDOT devices]     |
| NDORv5           | `tcp`          | 1               | [gate arm]          |
| NDOT Beacon      | `tcp`          | 1               | [beacon]            |
| [NTCIP] A        | `udp`          | 1               | [NTCIP devices]     |
| [NTCIP] B        | `tcp`          | 1-8191          | [NTCIP devices]     |
| [NTCIP] C        | `tcp`          | 1               | [NTCIP devices]     |
| [ONVIF]          | `http`         | 1               | [camera]            |
| OSi ORG-815      | `tcp`          | 1               | [weather sensor]    |
| [Pelco] D        | `udp`          | 1-254           | [camera]            |
| [Pelco] P        | `tcp`          | 1               | [camera keyboard]   |
| RedLion          | `tcp`          | 1               | [GPS]               |
| RTMS Echo        | `http` †       | 1               | [vehicle detection] |
| RTMS G4          | `tcp`          | 0-65535         | [vehicle detection] |
| SierraGX         | `tcp`          | 1               | [GPS]               |
| Sierra SSH       | `tcp`          | 1               | [GPS]               |
| SmartSensor 105  | `tcp`          | 1-9999          | [vehicle detection] |
| SmartSensor 125  | `tcp`          | 1-65534         | [vehicle detection] |
| Streambed        | `tcp`          | 1               | [flow stream]       |
| Transcore E6     | `udp`          | 1               | [tag reader]        |
| [Vicon]          | `udp`          | 1-254           | [camera]            |

† Communicates using [pollinator] service

⸸ Backplane: 0-15, EEPROM: 128-255

## MnDOT Devices

Old style 170 controllers using MnDOT-170 and Advanced Traffic Controllers
(ATC) using [Natch] support many devices:

| Device Type         | `#` | [IO Pin]s |
|---------------------|-----|-----------|
| [Beacon]            | 1   | 2         |
| [Ramp meter]        | 2   | 2 - 3     |
| [LCS]               | 3   | 19 - 36   |
| [Vehicle detection] | 24  | 39 - 62   |
| [Alarm]             | 10  | 70 - 79   |

## NTCIP Devices

Multiple [device] types are supported by [NTCIP]:

| Device Type         | Standard    | [IO Pin]s |
|---------------------|-------------|-----------|
| [Alarm]             | _multiple_  |           |
| [DMS]               | _1203_      | 1         |
| [GPS]               | _1204_      | 2         |
| [LCS]               | _1203_      | 1         |
| [Vehicle detection] | _1202_      | 1-`??`    |
| [Weather sensor]    | _1204_      | 1         |

## External Systems

Some protocols allow IRIS to poll external systems periodically using `http`
or `https`.  Typically, a single [controller] should be assigned and made
`ACTIVE`.

- **CampbellCloud**: Cloud service for [weather sensor] data

- **CAP-IPAWS**: [CAP] feed from Integrated Public Alert and Warning System
  [IPAWS].  [Alert]s can be used to automatically post weather and other
  messages to [DMS].  This requires an `https` URI provided by the Federal
  Emergency Management Agency.

- **CAP-NWS**: [CAP] feed from National Weather Service.  [Alert]s can be used
  to automatically post weather messages to [DMS].

- **Central Park**: [Central Park] feed for [parking area] [vehicle detection].
  Use "Data per stall" endpoint (URI ending in `/integration/spot`).
  
- **ClearGuide**: [ClearGuide] external system [DMS] message feed

- **DMS-XML**: Legacy [DMS] external system

- **Inc-Feed**: External system [incident feed]

- **Msg-Feed**: External system [message feed]


[ADEC TDC]: https://adec-technologies.ch/en/product/tdc3/
[alarm]: alarms.html
[alert]: alerts.html
[Axis]: https://www.axis.com/products/network-cameras
[Banner DXM]: https://www.bannerengineering.com/us/en/products/wireless-sensor-networks/wireless-controllers/industrial-wireless-controller-dxm-series.html
[beacon]: beacons.html
[camera]: cameras.html
[camera keyboard]: cameras.html#camera-keyboards
[CampbellCloud]: https://www.campbellsci.com/campbellcloud
[CAP]: http://docs.oasis-open.org/emergency/cap/v1.2/CAP-v1.2.html
[CBW]: https://controlbyweb.com/
[Central Park]: https://drivewyze.com/drivewyze-central-park-for-truck-parking/
[ClearGuide]: clearguide.html
[Cohu]: https://costarhd.com/
[comm link]: comm_links.html
[controller]: controllers.html
[device]: controllers.html#devices
[DLI]: https://dlidirect.com/
[DMS]: dms.html
[DR-500]: https://houston-radar.com/pdf/HoustonRadar_DR500_UserManual.pdf
[external]: #external-systems
[flow stream]: flow_streams.html
[gate arm]: gate_arms.html
[GPS]: gps.html
[HySecurity STC]: https://hysecurity.com/products/controllers/smart-touch-controller/
[incident feed]: incident_feed.html
[Infinova]: https://www.infinova.com/
[IO pin]: controllers.html#io-pins
[IPAWS]: https://www.fema.gov/emergency-managers/practitioners/integrated-public-alert-warning-system
[LCS]: lcs.html
[message feed]: msg_feed.html
[MnDOT devices]: #mndot-devices
[monstream]: video.html#monstream
[Natch]: natch.html
[NTCIP]: https://www.ntcip.org/
[NTCIP devices]: #ntcip-devices
[ONVIF]: https://en.wikipedia.org/wiki/ONVIF
[parking area]: parking_areas.html
[Pelco]: https://www.pelco.com/
[pollinator]: https://github.com/mnit-rtmc/iris/tree/master/pollinator
[PTZ]: cameras.html#control
[ramp meter]: ramp_meters.html
[streambed]: https://github.com/mnit-rtmc/streambed
[tag reader]: tolling.html#tag-readers
[vehicle detection]: vehicle_detection.html
[Vicon]: https://www.vicon.com/hardware/cameras/
[video]: video.html
[weather sensor]: weather_sensors.html
