# Controllers

Select `View ➔ Maintenance ➔ Comm Links` menu item

A **controller** is an end-point for a [comm link].  Each controller can have
one or more associated [devices](#devices), depending on the [protocol].
Sometimes a controller represents a separate physical _box_, which is connected
to devices, and other times the controller may be embedded within the device.
In either case, a controller is required for any communication to a device.

<details>
<summary>API Resources 🕵️ </summary>

* `iris/api/controller` (primary)
* `iris/api/controller/{name}`
* `iris/api/controller_io/{name}`

The read-only `controller_io` resource returns an array of objects consisting
of `pin`, `name` and `resource_n` of associated [devices](#devices).

| Access       | Primary                              | Secondary         |
|--------------|--------------------------------------|-------------------|
| 👁️  View      | name, location, setup, fail\_time    | geo\_loc, status  |
| 👉 Operate   | condition                            | device\_request † |
| 💡 Manage    | notes                                |                   |
| 🔧 Configure | comm\_link, drop\_id, cabinet\_style | password          |

† _Write only_

</details>

## Drop Address

Some [protocol]s support _multi-drop_ addressing, while others are _single-drop_
only.  Multi-drop addressing allows multiple controllers to share the same
[comm link] — typically for serial communication.  Each controller must be
assigned a unique (to the comm link) drop address, which is used to route all
messages sent to the controller.  For single-drop protocols, the drop address is
ignored.

## Controller Password

Authentication is supported or required by some communication protocols.  The
controller **password** field is used to enter authentication data.

* For [NTCIP], this represents the SNMP **community** name.  If no controller
  password is set, the `Public` community name will be used.
* Web-based devices may require HTTP Basic Authentication.  For these types of
  devices, the password field should contain both the user name and password,
  separated by a colon (`user:password`).
* For [Central Park], this is a token which is sent in the `x-access-token`
  HTTP header.
* For [CBW], the user name portion must be `none`.  HTTP Basic Authentication
  can be enabled on the setup page of the [CBW] device (setup.html).
* [SierraGX] modems can be configured to require authentiation.  In this case,
  separate the username and password with a colon, in the same manner as HTTP
  basic authentication.

## Setup JSON

Configuration data read from the controller is stored as JSON in `setup`.

Key          | Value
-------------|--------------------------------
`serial_num` | Unique controller serial number
`version`    | Version number / tag
`hw`         | Array of hardware modules: `make`, `model`, `version`
`sw`         | Array of software modules: `make`, `model`, `version`

## Devices

A _device_ is one of several types of traffic control or sensing systems.  These
include:

* [alarms]
* [beacons]
* [cameras]
* [dynamic message signs] — DMS
* [gate arms]
* [GPS]
* [lane-use control signs] — LCS
* [ramp meters]
* [tag readers]
* [vehicle detection systems]
* [video monitors]
* [weather sensors]

The controller must also be associated with a [comm link] which communicates
using an appropriate protocol for the device.

### IO Pins

Each controller has a set of **IO pins** for connecting [devices](#devices) or
[flow streams].  Every _device_ or _flow stream_ must be assigned to an _IO pin_
to be used.  The function of these pins is [protocol] specific.

# Cabinet Styles

Controllers can have an associated cabinet style, used for MnDOT-170 and Natch
[protocol]s.

<details>
<summary>API Resources 🕵️ </summary>

* `iris/api/cabinet_style`
* `iris/api/cabinet_style/{name}`

| Access       | Primary    | Secondary |
|--------------|------------|-----------|
| 👁️  View      | name       |           |
| 🔧 Configure |            | police\_panel\_pin\_1, police\_panel\_pin\_2, watchdog\_reset\_pin\_1, watchdog\_reset\_pin\_2, dip |

</details>


[alarms]: alarms.html
[beacons]: beacons.html
[cameras]: cameras.html
[CBW]: protocols.html#cbw
[Central Park]: protocols#central-park
[comm link]: comm_links.html
[dynamic message signs]: dms.html
[flow streams]: flow_streams.html
[gate arms]: gate_arms.html
[GPS]: gps.html
[lane-use control signs]: lcs.html
[protocol]: protocols.html
[NTCIP]: protocols.html#ntcip
[ramp meters]: ramp_meters.html
[SierraGX]: protocols.html#sierragx
[tag readers]: tolling.html#tag-readers
[vehicle detection systems]: vehicle_detection.html
[video monitors]: video.html
[weather sensors]: weather_sensors.html
