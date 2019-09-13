## Controllers

Select `View ➔ Maintenance ➔ Comm Links` menu item

A **controller** is an end-point for a [comm link].  Each controller can have
one or more associated [devices], depending on the [protocol].  Sometimes a
controller represents a separate physical _box_, which is connected to devices,
and other times the controller may be embedded within the device.  In either
case, IRIS requires a controller for any communication to a device.  Controllers
can be assigned to a **cabinet**, which has location information, to allow
displaying on a map.

### Drop Address

Some [protocol]s support _multi-drop_ addressing, while others are _single-drop_
only.  Multi-drop addressing allows multiple controllers to share the same
[comm link] — typically for serial communication.  Each controller must be
assigned a unique (to the comm link) drop address, which is used to route all
messages sent to the controller.  For single-drop protocols, the drop address is
ignored.

### Controller Password

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

### IO Pins

Each controller has a set of **I/O pins**, to which [devices] can be assigned.
The function of these pins is protocol specific — see the [protocol] table for
details.


[CBW]: admin_guidel.html#cbw
[comm link]: admin_guide.html#comm_links
[devices]: admin_guide.html#devices
[protocol]: admin_guide.html#prot_table
[NTCIP]: admin_guide.html#ntcip
[SierraGX]: admin_guide.html#sierragx
