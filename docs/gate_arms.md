# Gate Arms

Select `View ➔ Gate Arm` menu item

Gate arms are traffic control devices which restrict access to a section of
roadway.  They are commonly used for on-ramps or reversible lanes.

<details>
<summary>API Resources 🕵️ </summary>

* `iris/gate_arm_interlock` (lookup table)
* `iris/gate_arm_state` (lookup table)
* `iris/api/gate_arm` (primary)
* `iris/api/gate_arm/{name}`

| Access       | Primary                               | Secondary       |
|--------------|---------------------------------------|-----------------|
| 👁️  View      | name, location, arm\_state, interlock | geo\_loc, fault |
| 👉 Operate   |                                       | lock †          |
| 🔧 Configure | controller, notes                     | pin, preset, opposing, downstream_hashtag |

† _Write only_

</details>

## Setup

The gate arm properties form has setup information.

Field             | Description
------------------|---------------------------------------------------
Notes             | administrator notes, possibly including [hashtag]s
Preset            | verification [camera] preset
Opposing          | interlock check for gates on this road in another direction
Downstream (#tag) | [hashtag] for downstream gates which must open prior to this

The **notes** field may contain [hashtag]s to be referenced in [device action]s.

The verification [camera] preset allows operators to check traffic conditions
before opening or closing the gates.  The _Swap_ button allows camera images
from multiple gates to be swapped between larger and smaller views.

An [action plan] is required to control gate arms.  It must have three phases:
- `ga_open`: gates open and optional [DMS] displaying "open" message
- `ga_change`: gates open, with DMS displaying "closed"
- `ga_closed`: gates closed and optional DMS displaying "closed"

[Device action]s must be assigned to these phases to control devices:
- Gate arm hashtags should be assigned to `ga_open` and `ga_change`, but not
  `ga_close`.
- Different DMS messages can be displayed for each phase, as necessary.

## States and Interlocks

Gate arms are continuously monitored, and can be in one of these states:

State        | Description             | Possibly Open | Possibly Closed
-------------|-------------------------|---------------|----------------
`OPENING`    | opening in progress     | ✔️             | ✔️
`OPEN`       | gate open               | ✔️             |
`CLOSING`    | closing in progress     | ✔️             | ✔️
`CLOSED`     | gate closed             |               | ✔️
`FAULT`      | fault in gate operation | ✔️             | ✔️
`UNKNOWN`    | no commnuication        | ✔️             | ✔️

If communication is lost to a gate arm for longer than the value of
`gate_arm_alert_timeout_secs` [system attribute], its state will be set to
`UNKNOWN`.

A gate arm **interlock** can prevent dangerous traffic conflicts:
- **Deny Open** prevents opening the gate, due to opposing gates _possibly_
  open or downstream gates _possibly_ closed.
- **Deny Close** prevents closing the gate, due to upstream gates _possibly_
  open.
- **Deny All** prevents opening or closing the gate (both above conditions).
- **System Disable** prevents operating any gates.

If a constraint is broken, IRIS will never automatically try to resolve it.
Instead, an _alert_ will logged in the `email_event` table.

## Control

Depending on the current action plan phase, one of the buttons will be enabled.
When an operator presses a button, its corresponding phase will be requested:
- `Open` ⇒ `ga_open`
- `Change` ⇒ `ga_change` †
- `Close` ⇒ `ga_closed`

† `Change` is only enabled if a [device action] is configured with the
`ga_change` phase.  Operators should use this phase to warn motorists when
the gates will soon be changing.  After checking the verification cameras,
the next phase can be selected.

DMS messages for these are displayed to the right of the camera view, for up to
two signs.

## Security

There are a couple of extra security features to restrict access to gate arm
control.

### Allowlist

There is a list of IP addresses from which clients are allowed to control a
gate arm action plan.  It is specified as the `gate.arm.allowlist` property in
the `/etc/iris/iris-server.properties` configuration file.  The property
contains a list of addresses in [CIDR] notation (exact IP, or ranges specified
such as `192.168.1.0/24`).

### System Disable

Another security feature causes the entire _gate arm system_ to be disabled
whenever a configuration change is made to any gate arm.  This includes any
changes to a gate arm, [controller] or associated [comm link] or [action plan].
IRIS will not send any command to open or close any gate arm while in this
state.  The only way to re-enable gate arm control is to create a file in the
server filesystem at `/var/lib/iris/gate_arm_enable` (using the touch command).


[action plan]: action_plans.html
[camera]: cameras.html
[CIDR]: https://en.wikipedia.org/wiki/Classless_Inter-Domain_Routing
[comm link]: comm_links.html
[controller]: controllers.html
[device action]: action_plans.html#device-actions
[DMS]: dms.html
[hashtag]: hashtags.html
[phase]: action_plans.html#plan-phases
[system attribute]: system_attributes.html
