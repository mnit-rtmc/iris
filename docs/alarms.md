# Alarms

An alarm is a device which has a boolean `state` indicating whether or not it is
_triggered_.  The `description` of the alarm might indicate an equipment
failure, high temperature, low voltage, _etc_.

## Setup

Select `View ➔ Maintenance ➔ Alarms` menu item

An alarm must be associated with a [controller] on an appropriate [comm link].
Several [protocol]s are supported:

| Protocol | [IO Pin]s | Notes                              |
|----------|-----------|------------------------------------|
| CBW      | 1-16      | Uses input state (not relay)       |
| MnDOT    | 70-79     |                                    |
| Natch    | 70-79     |                                    |
| NTCIP    | 11-`??`   | 10 + `auxIOv2PortNumber` (digital) |

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


[comm link]: comm_links.html
[controller]: controllers.html
[IO pin]: controllers.html#io-pins
[protocol]: protocols.html
[event]: events.html
