## Gate Arms

Select `View ➔ Gate Arm Arrays` menu item

Gate arms are traffic control devices which restrict access to a section of
roadway.  They can be used, for example, for reversible lanes which can be
changed by time of day.

Gate arms are grouped as arrays of 1-8 arms.  Opening or closing an array will
control all gate arms in the array.

### Verification Cameras

One or two [cameras] can be associated with a gate arm array.  This allows
operators to check traffic conditions and verify the status before opening or
closing the gates.  The second camera can be used to monitor approaching
traffic.  The _Swap_ button allows the camera images to be swapped between
larger and smaller views.

### Warning Action Plan

An [action plan] can be associated with a gate arm array, allowing one or more
[DMS] to display appropriate warning messages.  Whenever the state changes, this
plan will immediately be changed to the appropriate [phase] — **Warning Open
Phase** when fully open, otherwise **Warning Closed Phase**.

### Interlocks

Great care must be taken to prevent traffic conflicts when using gate arms.  Two
types of _interlock_ are available for this purpose.  An _open interlock_ is a
constraint which prevents the gate arm from being opened within IRIS.
Similarly, a _close interlock_ prevents the gate arm from being closed.

When a gate arm is open, all other gate arms on the same roadway, but in any
other direction will have an _open interlock_.

Each gate arm can be assigned a _prerequisite_ gate arm (on the same roadway and
direction).  This configuration prevents the gate arm from opening until the
prerequisite has been opened, using an _open interlock_.  Once a gate arm and
its prerequisite are both open, the prerequisite gate arm will have a _close
interlock_ until the other gate arm is closed.

If communication is lost to a gate arm, the state will be unknown by IRIS.
After an interval equal to the `gate_arm_alert_timeout_secs` [system attribute]
(default 90), it will be treated as _possibly open_, causing interlocks to be
checked.  Also, an email will be sent to the address in the
`email_recipient_gate_arm` system attribute.

### Conflicts

If a _conflict_ is detected, an _alert_ email will be sent to the address in the
`email_recipient_gate_arm` [system attribute].  An _open conflict_ exists when
an _open interlock_ constraint is broken.  Similarly, a _close conflict_ exists
for _close interlock_ constraints.  IRIS will not automatically try to resolve
conflicts.

### Gate Arm Security

There are a couple of extra security features to restrict access to gate arm
control.

#### Whitelist

There is a whitelist of IP addresses from which IRIS clients are allowed to
control gate arms.  It is specified as the `gate.arm.whitelist` property in the
`/etc/iris/iris-server.properties` configuration file.  The property contains a
list of addresses in [CIDR] notation (exact IP, or ranges specified such as
`192.168.1.0/24`).

#### System Disable

Another gate arm security feature causes the entire _gate arm system_ to be
disabled whenever any configuration change is made to a gate arm.  This includes
any changes to a gate arm array, [controller] or associated [comm link].  IRIS
will not send any command to open or close any gate arm in this state.  The only
way to re-enable gate arm control is to create a file in the server filesystem
at `/var/lib/iris/gate_arm_enable` (using touch).


[action plans]: action_plans.html
[cameras]: admin_guide.html#cameras
[CIDR]: https://en.wikipedia.org/wiki/Classless_Inter-Domain_Routing
[comm link]: admin_guide.html#comm_links
[controller]: controllers.html
[DMS]: admin_guide.html#dms
[phase]: action_plans.html#plan-phases
[system attribute]: admin_guide.html#sys_attr
