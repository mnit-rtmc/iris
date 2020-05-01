# Controllers

Select `View ➔ Maintenance ➔ Comm Links` menu item

A **controller** is an end-point for a [comm link].  Each controller can have
one or more associated [devices](#devices), depending on the [protocol].
Sometimes a controller represents a separate physical _box_, which is connected
to devices, and other times the controller may be embedded within the device.
In either case, a controller is required for any communication to a device.
Controllers can be assigned to a **cabinet**, which has location information to
allow displaying on a map.

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
* For [CBW], the user name portion must be `none`.  HTTP Basic Authentication
  can be enabled on the setup page of the [CBW] device (setup.html).
* [SierraGX] modems can be configured to require authentiation.  In this case,
  separate the username and password with a colon, in the same manner as HTTP
  basic authentication.

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
* [road weather information systems]
* [tag readers]
* [vehicle detection systems]
* [video monitors]

The controller must also be associated with a [comm link] which communicates
using an appropriate protocol for the device.

### IO Pins

Each controller has a set of **IO pins** for connecting [devices](#devices) or
[flow streams].  Every _device_ or _flow stream_ must be assigned to an _IO pin_
to be used.  The function of these pins is [protocol] specific.


[alarms]: alarms.html
[beacons]: beacons.html
[cameras]: cameras.html
[CBW]: comm_links.html#cbw
[comm link]: comm_links.html
[dynamic message signs]: dms.html
[flow streams]: flow_streams.html
[gate arms]: gate_arms.html
[GPS]: gps.html
[lane-use control signs]: lcs.html
[protocol]: comm_links.html#protocols
[NTCIP]: comm_links.html#ntcip
[ramp meters]: ramp_meters.html
[road weather information systems]: rwis.html
[SierraGX]: comm_links.html#sierragx
[tag readers]: tolling.html#tag-readers
[vehicle detection systems]: vehicle_detection.html
[video monitors]: video.html
