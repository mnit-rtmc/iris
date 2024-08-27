# Action Plans

Select `View ➔ Plans and Schedules` menu item

*Action plans* provide a way to automate and coördinate control of devices, such
as [DMS]s and [ramp meter]s.

Action plans have a [phase](#plan-phases), which is chosen from a preconfigured
set.  It can be changed by an operator, or at specified times with
[time actions](#time-actions).

Each phase can be associated with any number of device actions.  Advanced
plans can have many phases, each with separate actions.

- **Sync Actions**: if selected, the phase can only be changed if all associated
  devices are online.
- **Sticky**: if selected, messages sent with [device actions](#device-actions)
  will be configured to persist even if communication or power is lost.
- **Ignore Auto-Fail**: if selected, [device action](#device-actions) messages
  will ignore detector [auto-fail] (`[exit` *…* `]` or `[slow` *…* `]` only)

## Plan Phases

The basic phases are **deployed** and **undeployed**.  Additional phases can be
added on the **Plan Phases** tab.  Each phase must have a unique name.
By specifying **Hold Time**, a *transient* phase will advance automatically to
the **Next Phase**.  *Hold Time* must be a multiple of 30 seconds.

## Device Actions

Device actions use [hashtag]s to associate devices with one phase of an action
plan.  These devices can be:
 - [DMS], displays the [message pattern] on the sign
 - [beacon], activates flashing lights
 - [ramp meter], enables metering operation
 - [lane marking], activates in-pavement LEDs
 - [camera], recalls the specified camera [preset] (experimental)

[Priority] determines the priority of messages created by the action.

### Action Tags

A device action [message pattern] is a message to display on a device.  These
patterns support additional *action tags* which are not valid [MULTI].  They
are only usable in device actions - not operator-composed messages.

**Replace** tags are substituted with computed text before being sent to a
sign.  For example, a `[tt` *…* `]` tag will be replaced with the current
estimated travel time.  These tags can be used only on DMS devices.

**Condition** tags add a stipulation which activates the device only when the
condition is met.  These tags can be used with any device type.

Tag              | Description                                | Tag Mode
-----------------|--------------------------------------------|---------
`[cg` *…* `]`    | [ClearGuide] data                          | Replace
`[exit` *…* `]`  | [Exit ramp backup]                         | Condition
`[feed` *…* `]`  | [Msg-Feed] message                         | Replace
`[pa` *…* `]`    | [Parking area] availability                | Replace
`[rwis_` *…* `]` | [RWIS] weather conditions                  | Condition
`[slow` *…* `]`  | [Slow traffic] warning                     | Condition + Replace
`[standby]`      | Standby messages                           | Standby
`[ta` *…* `]`    | Scheduled [time actions](#time-action-tag) | Replace
`[tt` *…* `]`    | [Travel time] estimation                   | Replace
`[tz` *…* `]`    | [Toll zone] pricing                        | Replace
`[vsa]`          | [Variable speed advisory]                  | Condition + Replace

## Time Actions

A *time action* automatically changes the phase at specified dates and times.
These events are scheduled using either a [day plan](#day-plans) or a specific
date (but not both).  A time of day must also be specified (HH:MM in 24-hour
format).  Whenever the scheduled time occurs, the action plan will be changed to
the specified phase.

## Time Action Tag

The time of a scheduled **time action** can be displayed in DMS messages using
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

A day plan is a set of days which can be used for scheduling.  Day plans are
specified with a table of **day matchers**.  The matchers for a day plan
determine whether a specific day is included in the plan or not.  Day matchers
flagged as **holidays** are excluded.  Day matchers are specified by **Month**,
**Day**, **Week** and **Weekday**.  Any day which matches **all** specified
fields will match.  **Shift** is only required for days like *Black Friday*
(Fourth Thursday of November **+1**).

## Manual Control

On the **Plan** tab of the client interface, users can manually change the phase
of an action plan.  If the user is in the list specified by the
`action_plan_alert_list` [system attribute], an email will be sent to the
address specified by the `email_recipient_action_plan` [system attribute].

## Events

Whenever an action plan phase changes, a time-stamped record is added to the
`action_plan_event` table.  These events are purged automatically when older
than the value of the `action_plan_event_purge_days` [system attribute].


[auto-fail]: vehicle_detection.html#auto-fail
[beacon]: beacons.html
[camera]: cameras.html
[ClearGuide]: clearguide.html
[DateTimeFormatter]: https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html
[DMS]: dms.html
[exit ramp backup]: exit_backup.html
[hashtag]: hashtags.html
[lane marking]: lcs.html#lane-markings
[message pattern]: message_patterns.html
[priority]: sign_message.html#message-priority
[Msg-Feed]: protocols.html#msg-feed
[MULTI]: multi.html
[Parking area]: parking_areas.html
[preset]: cameras.html#presets
[ramp meter]: ramp_meters.html
[rwis]: rwis.html
[Slow traffic]: slow_warning.html
[Variable speed advisory]: vsa.html
[Toll zone]: tolling.html
[Travel time]: travel_time.html
[system attribute]: system_attributes.html
