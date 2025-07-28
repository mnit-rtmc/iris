# Sign Messages

A sign message is an immutable record containing all information needed to
display a message on a [DMS].  They are created when operators [compose] them,
and also when [device actions] are scheduled.  They are deleted automatically
after being unused for a few minutes.

<details>
<summary>API Resources 🕵️ </summary>

* `iris/sign_message` (primary)
* `iris/img/{name}.gif`
* `iris/api/sign_message` (`POST`)

| Access  | Primary     |
|---------|-------------|
| 👁️  View | name, sign\_config, incident, multi, msg\_owner, sticky, flash\_beacon, pixel\_service, msg\_priority |

</details>

[Sign configuration] determines the type of sign that can display the message.

The [MULTI] string contains the text and/or [graphics] of the message.

The **msg_owner** is a string containing 3 fields, separated by semicolons
(`system`; `sources`; `user`):
- `system`: normally "IRIS"; possibly another system
- `sources`: "operator" or [action tag] sources, separated by `+`
- `user`: name of user who created the message

**Sticky** indicates the message should persist even if communication or power
is lost.

**Flash Beacon** indicates whether an associated beacon should flash.

**Pixel Service** indicates whether the sign should perform scheduled pixel
service while the message is displayed.

### Message Priority

Priority determines precedence between operator messages and [device actions].

| Low          | Medium           | High           |
|--------------|------------------|----------------|
| 1: `low_1`   | 6: `medium_1`    | 11: `high_1`   |
| 2: `low_2`   | 7: `medium_2`    | 12: `high_2`   |
| 3: `low_3`   | 8: `medium_3`    | 13: `high_3`   |
| 4: `low_4`   | 9: `medium_4`    | 14: `high_4`   |
| 5: `low_sys` | 10: `medium_sys` | 15: `high_sys` |

Messages composed by operators have `high_1` priority.  [Cleared incidents] use
the `low_sys` priority.  Messages sent by external systems are assigned to
`medium_sys`.


[action tag]: action_plans.html#action-tags
[cleared incidents]: incident_dms.html#clearing
[compose]: dms.html#composing-messages
[device actions]: action_plans.html#device-actions
[DMS]: dms.html
[graphics]: graphics.html
[MULTI]: multi.html
[sign configuration]: sign_configuration.html
