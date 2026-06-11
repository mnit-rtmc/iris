# Action Plans

Select `View ➔ Plans and Schedules` menu item

*Action plans* provide a way to automate and coördinate control of devices, such
as [DMS]s and [ramp meter]s.

- **Notes**: administrator notes, possibly including [hashtag]s, which can be
  used for restricting [permission]s
- **Sync Actions**: if selected, the phase can only be changed if all associated
  devices are online.
- **Sticky**: if selected, messages sent with [device actions](#device-actions)
  will be configured to persist even if communication or power is lost.
- **Ignore Auto-Fail**: if selected, [device action](#device-actions) messages
  will ignore detector [auto-fail] (`[exit` *…* `]` or `[slow` *…* `]` only)

<details>
<summary>API Resources 🕵️ </summary>

* `iris/api/action_plan` (primary)
* `iris/api/action_plan/{name}`

| Access       | Primary                       | Secondary |
|--------------|-------------------------------|-----------|
| 👁️  View      | name                          |           |
| 👉 Operate   | phase                         |           |
| 💡 Manage    | default\_phase, notes, active | sync\_actions, sticky, ignore\_auto\_fail |

</details>

## Plan Phases

The **phase** of an action plan represents its current state.  It can be
changed manually by an operator or on specific conditions using
[phase actions](#phase-actions).  Advanced plans can have many phases, each
with separate actions.

There are 8 built-in phases required for proper action plan operation:

| Phase Name     | Description                  |
|----------------|------------------------------|
| `deployed`     | Generic "in use" actions     |
| `undeployed`   | Generic "not in use" actions |
| `alert_before` | Before [alert] actions       |
| `alert_during` | During [alert] actions       |
| `alert_aftger` | After [alert] actions        |
| `ga_open`      | [Gate arm] open actions      |
| `ga_change`    | [Gate arm] change actions    |
| `ga_closed`    | [Gate arm] closed actions    |

Additional phases can be added on the **Plan Phases** tab.  Each phase must
have a unique name, and names are case-sensitive.

Only **selectable** phases can be chosen by an operator; otherwise they must
be automatic, with [phase actions](#phase-actions).

<details>
<summary>API Resources 🕵️ </summary>

* `iris/api/plan_phase` (primary)
* `iris/api/plan_phase/{name}`

| Access       | Primary    |
|--------------|------------|
| 👁️  View      | name       |
| 🔧 Configure | selectable |

</details>

## Phase Actions

A *phase action* can automatically change the phase of an action plan.  When
the specified condition occurs, it will become `to_phase`.  This can be
limited to specific days using a [day plan](#day-plans).  Also, if
`from_phase` is set, it will only take effect if the current phase matches.

<details>
<summary>API Resources 🕵️ </summary>

* `iris/api/phase_action` (primary)
* `iris/api/phase_action/{name}`

| Access       | Primary                      |
|--------------|------------------------------|
| 👁️  View      | name, action\_plan           |
| 💡 Manage    | day\_plan, condition, params |
| 🔧 Configure | from\_phase, to\_phase       |

</details>

### Conditions

There are several conditions which can trigger a phase change.  Each condition
has parameters which may include keywords for **Field**, **Mode** or
**Period**.  These may be abbreviated, as long as they match a unique prefix
(_e.g._ `occupancy` could be shortened to `occ`).

<details>
<summary>Hold Time</summary>

Condition triggers when the plan has been in `from_phase` for longer than the
hold time.

- **Params**: `s`, `mm:ss` or `HH:mm:ss`

- **Example**: `15:00` (for 15 minutes)

</details>

<details>
<summary>Clock Time</summary>

Condition triggers at a specific time of day, optionally on a specific date.

- **Params**: `HH:mm` or `yyyy-MM-dd'T'HH:mm`

- **Example**: `2026-05-01T14:00` (at 2 PM on May 1, 2026)

</details>

<details>
<summary>Traffic Threshold</summary>

**WARNING: not implemented yet**

Condition triggers when a detector or station field goes above or below a
threshold value.

- **Params**: Detector or station ID, _field_ `<` or `>` value

- **Field** keywords: `speed`, `flow`, `density`, `occupancy`

- **Example**: `1234,den>30` (when detector 1234 has density greater than 30)

</details>

<details>
<summary>RWIS Threshold</summary>

**WARNING: not implemented yet**

Condition triggers when an RWIS field goes above or below a threshold value.

- **Params**: Sensor ID, _field_ `<` or `>` value

- **Field** keywords: `friction`, `surface_temp`, `wind_gust`, `visibility`,
  `precipitation`

- **Example**: `WS0123,fric<70` (station WS0123 friction less than 70)

</details>

<details>
<summary>Toll Mode</summary>

**WARNING: not implemented yet**

Condition triggers when a toll zone changes mode.

- **Params**: Toll zone ID, _mode_

- **Mode** keywords: `priced`, `open`, `closed`

- **Example**: `Z394W01,priced` (when zone Z394W01 is priced)

</details>

<details>
<summary>Alert Period</summary>

**WARNING: not implemented yet**

Condition triggers in a specific alert period.

- **Params**: Alert ID, _period_

- **Period** keywords: `before`, `during`, `after`, `expired`

- **Example**: `ai_20260113183503938,during`

</details>

<details>
<summary>Alarm</summary>

Condition triggers when an alarm is triggered or cleared.

- **Params**: Alarm ID, _state_

- **State** keywords: `triggered`, `cleared`

- **Example**: `A100,trigger`

</details>

## Device Actions

Device actions use [hashtag]s to associate devices with one phase of an action
plan.  These devices can be:
 - [DMS], displays the [message pattern] on the sign
 - [ramp meter], enables metering operation
 - [gate arm], open and close gates
 - [beacon], activates flashing lights
 - [camera], recalls the specified camera [preset]

Any number of device actions can be associated with each phase of an action
plan.

[Priority] determines the priority of messages created by the action.  For
camera actions, instead this indicates:
* `1-12` a [preset] number to recall (ignored if the camera is associated
  with an [incident])
* `15` activate wiper

<details>
<summary>API Resources 🕵️ </summary>

* `iris/api/device_action` (primary)
* `iris/api/device_action/{name}`

| Access       | Primary            | Secondary                   |
|--------------|--------------------|-----------------------------|
| 👁️  View      | name, action\_plan |                             |
| 💡 Manage    | hashtag, phase     | msg\_priority, msg\_pattern |

</details>

### Action Tags

A device action [message pattern] is a message to display on a device.  These
patterns support additional *action tags* which are not valid [MULTI].  They
are only usable in device actions - not operator-composed messages.

**Replace** tags are substituted with computed text before being sent to a
sign.  For example, a `[tt` *…* `]` tag will be replaced with the current
estimated travel time.  These tags can be used only on DMS devices.

**Condition** tags add a stipulation which activates the device only when the
condition is met.  These tags can be used with any device type, such as a
flashing [beacon].

Tag              | Description                      | Tag Mode            | Source
-----------------|----------------------------------|---------------------|-------------
`[cg` *…* `]`    | [ClearGuide] data                | Condition + Replace | `clearguide`
`[exit` *…* `]`  | [Exit ramp backup]               | Condition           | `exit_warning`
`[feed` *…* `]`  | Msg-Feed [protocol] message      | Replace             | N/A
`[pa` *…* `]`    | [Parking area] availability      | Replace             | `parking`
`[rwis_` *…* `]` | [RWIS] weather conditions        | Condition           | `rwis`
`[slow` *…* `]`  | [Slow traffic] warning           | Condition + Replace | `slow_warning`
`[standby]`      | Standby messages                 | Standby             | `standby`
`[ta` *…* `]`    | [Time actions](#time-action-tag) | Replace             | N/A
`[tt` *…* `]`    | [Travel time] estimation         | Replace             | `travel_time`
`[tz` *…* `]`    | [Toll zone] pricing              | Replace             | `tolling`
`[vsa]`          | [Variable speed advisory]        | Condition + Replace | `speed_advisory`

## Time Actions

A *time action* automatically changes the phase at specified dates and times.
These events are scheduled using either a [day plan](#day-plans) or a specific
date (but not both).  A time of day must also be specified (HH:MM in 24-hour
format).  Whenever the scheduled time occurs, the action plan will be changed to
the specified phase.

<details>
<summary>API Resources 🕵️ </summary>

* `iris/api/time_action` (primary)
* `iris/api/time_action/{name}`

| Access       | Primary                                                   |
|--------------|-----------------------------------------------------------|
| 👁️  View      | name, action\_plan, day\_plan, sched\_date, time\_of\_day |
| 💡 Manage    | phase                                                     |

</details>

## Time Action Tag

The time of a scheduled **phase action** can be displayed in DMS messages using
[device actions](#device-actions) within the same action plan.  A `[ta` *…* `]`
[action tag](#action-tags) in the [message pattern] will be replaced with the
appropriate value.  It has the following format:

`[ta` *dir*,*format* `]`

**Parameters**

1. `dir`: Chronological direction
   - `n`: **Next** scheduled *time action* after the current time
   - `p`: **Previous** scheduled *time action* before the current time
2. `format`: Time format pattern (`h a` if omitted)

The format parameter is specified using a Java [DateTimeFormatter] pattern,
summarized in this table:

Symbol | Meaning
-------|------------
h      | hour (1-12)
H      | hour of day (0-23)
mm     | minute (00-59)
a      | AM or PM
E      | weekday (*e.g.* Mon)
EEEE   | full weekday (*e.g.* Monday)
d      | day of month (1-31)
M      | month number (1-12)
L      | month (*e.g.* Jan)
LLLL   | full month (*e.g.* January)

The default pattern, `h a`, would format a time at 2 in the afternoon as `2 PM`.
To include minutes, `h:mm a` could be used instead.

### Example 1

```
ROAD WORK[nl]STARTING AT [tan,h:mm a]
```

If next scheduled time action is at 2:30 AM, the resulting message will be:

```
ROAD WORK
STARTING AT 2:30 AM
```

### Example 2

```
BLIZZARD WARNING[nl]FROM [tap][nl]UNTIL [tan]
```

If the time is between two scheduled time actions, 4 AM to 10 PM, the message
will be:

```
BLIZZARD WARNING
FROM 4 AM
UNTIL 10 PM
```

## Day Plans

A day plan is a set of days which can be used for scheduling.  They contain
a table of **day matchers**, which specify either active days or holidays,
depending on the **holidays** flag.  A matcher contains 5 fields, which can
be `NULL` for "any":

- **Month** (1-12)
- **Day** (1-31)
- **Weekday** (1-7)
- **Week** (1-4, or -1 for last)
- **Shift** - only needed for days like *Black Friday*
  (Fourth Thursday of November **+1**)

<details>
<summary>API Resources 🕵️ </summary>

* `iris/api/day_plan` (primary)
* `iris/api/day_plan/{name}`

| Access       | Primary        |
|--------------|----------------|
| 👁️  View      | name, holidays |

* `iris/api/day_matcher` (primary)
* `iris/api/day_matcher/{name}`

| Access       | Primary                                           |
|--------------|---------------------------------------------------|
| 👁️  View      | name, day\_plan, month, day, weekday, week, shift |

</details>

## Manual Control

On the **Plan** tab of the client interface, users can manually change the
phase of an action plan.  In addition to having "Operate" access [permission]
for action plans, the user must have it for all associated device actions
(every device using the matching hashtag).

## Events

Whenever an action plan phase changes, a time-stamped [event] record can be
stored in the `action_plan_event` table.

When a user changes the phase, if they are in the list specified by the
`action_plan_alert_list` [system attribute], an email event will be logged to
the `email_event` table.


[alert]: alerts.html
[auto-fail]: vehicle_detection.html#auto-fail
[beacon]: beacons.html
[camera]: cameras.html
[ClearGuide]: clearguide.html
[DateTimeFormatter]: https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html
[DMS]: dms.html
[event]: events.html
[exit ramp backup]: exit_backup.html
[gate arm]: gate_arms.html
[hashtag]: hashtags.html
[incident]: incidents.html
[message pattern]: message_patterns.html
[priority]: sign_message.html#message-priority
[MULTI]: multi.html
[Parking area]: parking_areas.html
[permission]: permissions.html
[preset]: cameras.html#presets
[protocol]: protocols.html
[ramp meter]: ramp_meters.html
[rwis]: rwis.html
[Slow traffic]: slow_warning.html
[Variable speed advisory]: vsa.html
[Toll zone]: tolling.html
[Travel time]: travel_time.html
[system attribute]: system_attributes.html
