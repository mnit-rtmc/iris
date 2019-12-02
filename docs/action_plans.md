# Action Plans

Select `View ➔ Plans and Schedules` menu item

*Action plans* provide a way to automate and coördinate control of devices, such
as [DMS]s and [ramp meter]s.

Action plans have a [phase](#plan-phases), which is chosen from a preconfigured
set.  It can be changed by an operator, or at specified times with
[time actions](#time-actions).

Each phase can be associated with any number of [DMS](#dms-actions),
[ramp meter](#meter-actions), [camera](#camera-actions), or
[beacon](#beacon-actions) actions.  Advanced plans can have many phases, each
with separate actions.

If **Sync Actions** is selected, the phase can only be changed if all associated
devices are online.  If **Sticky** is selected, messages sent with
[DMS actions](#dms-actions) will be configured to persist even if communication
or power is lost.

## Plan Phases

The basic phases are **deployed** and **undeployed**.  Additional phases can be
added on the **Plan Phases** tab.  Each phase must have a unique name.
By specifying **Hold Time**, a *transient* phase will advance automatically to
the **Next Phase**.  *Hold Time* must be a multiple of 30 seconds.

## DMS Actions

[DMS] actions have an associated *sign group* to determine which signs are
affected by the action.  The action happens when the corresponding action plan
phase is selected.  The [quick message] indicates which message is activated.
If **activate beacon** is selected, the sign beacon will also be activated.
**Message priority** determines the priority of messages created by the action.

### DMS Action Tags

Some *[MULTI]-like* tags are supported in [quick message]s used by DMS actions.
These tags are interpreted by IRIS before sending the message to the DMS.

Tag                             | Description
--------------------------------|-------------------
`[feed` *n* `]`                 | [Msg-Feed] message
`[pa` *i,l,c* `]`               | [Parking area] availability
`[slow` *s,d,mode* `]`          | [Slow traffic] warning
`[tt` *s,m,t* `]`               | [Travel time]
`[tz *{p,o,c},{tz0},…{tzn}* `]` | [Toll zone] pricing
`[vsa]`                         | [Variable speed advisory]

## Meter Actions

A [ramp meter] action causes the meter to begin metering when the specified
*phase* is selected.  When the plan is set to any other phase, the meter will
shut off.

## Camera Actions

A [camera] action causes a specific [preset] to be recalled when the specified
*phase* is selected.

## Beacon Actions

A [beacon] action can cause a beacon to be deployed when the action plan is set
to the specified *phase*.  When the plan is set to any other phase, the beacon
will shut off.

## Time Actions

A *time action* automatically changes the phase at specified dates and times.
These events are scheduled using either a [day plan](#day-plans) or a specific
date (but not both).  A time of day must also be specified (HH:MM in 24-hour
format).  Whenever the scheduled time occurs, the action plan will be changed to
the specified phase.

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


[beacon]: beacons.html
[camera]: cameras.html
[DMS]: dms.html
[Msg-Feed]: comm_links.html#msg-feed
[MULTI]: dms.html#multi
[Parking area]: parking_areas.html
[preset]: cameras.html#presets
[quick message]: dms.html#quick-messages
[ramp meter]: ramp_meters.html
[Slow traffic]: slow_warning.html
[Variable speed advisory]: vsa.html
[Toll zone]: tolling.html
[Travel time]: travel_time.html
[system attribute]: system_attributes.html
