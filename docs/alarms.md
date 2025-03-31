# Alarms

Select `View ➔ Maintenance ➔ Alarms` menu item

An alarm is a device which has a boolean `state` indicating whether or not it is
_triggered_.  The `description` of the alarm might indicate an equipment
failure, high temperature, low voltage, _etc_.

An alarm can be created for controllers using a [protocol] that generates
alarms, such as [MnDOT-170].

<details>
<summary>API Resources 🕵️ </summary>

* `iris/api/alarm` (primary)
* `iris/api/alarm/{name}`

| Access       | Primary     | Secondary     |
|--------------|-------------|---------------|
| 👁️  View      | name, state | trigger\_time |
| 💡 Manage    | description |               |
| 🔧 Configure | controller  | pin           |

</details>

## Events

The `state` field is set to `true` when _triggered_.  When it changes, a
time-stamped [event] record can be stored in the `alarm_event` table.


[MnDOT-170]: protocols.html#mndot-170
[protocol]: protocols.html
[event]: events.html
