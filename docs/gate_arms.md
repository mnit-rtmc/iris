# Gate Arms

Select `View ➔ Gate Arm Arrays` menu item

Gate arms are traffic control devices which restrict access to a section of
roadway.  They are commonly used for on-ramps or reversible lanes.

They are grouped into arrays of 1-8 arms.  *Opening* or *closing* an array
controls **all** associated arms.

## Verification Cameras

One or two [cameras] can be associated with a gate arm array.  This allows
operators to check traffic conditions and verify the status before opening or
closing the gates.  The second camera can be used to monitor approaching
traffic.  The _Swap_ button allows the camera images to be swapped between
larger and smaller views.

## Warning Action Plan

An [action plan] can be associated with a gate arm array.  The plan's [phase]
will be continuously updated according to the array [state](#states):

- `OPEN`: `ga_open`
- otherwise: `ga_closed`

One or more [DMS actions] can be assigned to these phases to warn motorists of
the gate arm state.

## Operating

An operator can request to change an array to one of three [states](#states):
- `OPENING`
- `WARN_CLOSE` †
- `CLOSING`

† `WARN_CLOSE` is only available when a [warning action plan] is configured.  An
operator should request this state to warn motorists when the gates will soon be
closing.  After checking the verification cameras, the `CLOSING` state can be
requested.

## States

A gate arm array can be in one of 7 states:

State        | Description           | Transition
-------------|-----------------------|------------------
`OPENING`    | open in progress      | `OPEN` or `FAULT`
`OPEN`       | gate open             | N/A
`WARN_CLOSE` | warn before closing † | N/A
`CLOSING`    | closing in progress   | `CLOSED` or `FAULT`
`CLOSED`     | gate closed           | N/A
`FAULT`      | open / close fault    | N/A
`UNKNOWN`    | commnuication failure | N/A

## Interlocks

Great care must be taken to prevent traffic conflicts when operating gate arms.
Two types of _interlock_ are available for this purpose.  An **open interlock**
is a constraint which prevents the gate arm from being opened.  Similarly, a
**close interlock** prevents the gate arm from being closed.

When a gate arm is open, all other gate arms on the same roadway, but in any
other direction will have an _open interlock_.

### Prerequesites

Each gate arm array can be assigned a _prerequisite_ array (on the same roadway
and direction).  This configuration prevents the gate arms from opening until
the prerequisite has been opened, using an _open interlock_.  Once an array and
its prerequisite are both open, the prerequisite array will have a _close
interlock_ until the other array is closed.

### Conflicts

An **open conflict** exists when an _open interlock_ constraint is broken.
Similarly, a **close conflict** exists for _close interlock_ constraints.  IRIS
will not automatically try to resolve conflicts.

If a conflict is detected, an _alert_ email will be sent to the address in the
`email_recipient_gate_arm` [system attribute].

If communication is lost to a gate arm, the state will be unknown.  After an
interval equal to the `gate_arm_alert_timeout_secs` [system attribute], it will
be treated as **possibly open**, and interlock conflicts will be checked.

## Security

There are a couple of extra security features to restrict access to gate arm
control.

### Whitelist

There is a whitelist of client IP addresses from which clients are allowed to
control gate arms.  It is specified as the `gate.arm.whitelist` property in the
`/etc/iris/iris-server.properties` configuration file.  The property contains a
list of addresses in [CIDR] notation (exact IP, or ranges specified such as
`192.168.1.0/24`).

### System Disable

Another security feature causes the entire _gate arm system_ to be disabled
whenever a configuration change is made to any gate arm.  This includes any
changes to a gate arm array, [controller] or associated [comm link].  IRIS will
not send any command to open or close any gate arm while in this state.  The
only way to re-enable gate arm control is to create a file in the server
filesystem at `/var/lib/iris/gate_arm_enable` (using the touch command).


[action plan]: action_plans.html
[cameras]: cameras.html
[CIDR]: https://en.wikipedia.org/wiki/Classless_Inter-Domain_Routing
[comm link]: comm_links.html
[controller]: controllers.html
[DMS actions]: action_plans.html#dms-actions
[phase]: action_plans.html#plan-phases
[system attribute]: system_attributes.html
[warning action plan]: #action-plan
