# Alarms

Select `View ➔ Maintenance ➔ Alarms` menu item

An alarm is a device which has a boolean `state` indicating whether or not it is
_triggered_.  The `description` of the alarm might indicate an equipment
failure, high temperature, low voltage, _etc_.

An alarm can be created for controllers using a [protocol] that generates
alarms, such as [MnDOT-170].

<details>
<summary>API Resources</summary>

* `iris/api/alarm`
* `iris/api/alarm/{name}`

Attribute [permissions]:

| Access       | Minimal                 | Full          |
|--------------|-------------------------|---------------|
| Read Only    | name, state             | trigger\_time |
| 🔧 Configure | description, controller | pin           |

</details>

## Events

The `state` field is set to `true` when _triggered_.  When it changes, a
time-stamped record is added to the `alarm_event` table.  These records are
purged automatically when older than the value of the `alarm_event_purge_days`
[system attribute].


[MnDOT-170]: protocols.html#mndot-170
[permissions]: permissions.html
[protocol]: protocols.html
[system attribute]: system_attributes.html
