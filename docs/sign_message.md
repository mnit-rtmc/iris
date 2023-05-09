# Sign Messages

Messages are created when operators compose them, and also when [DMS actions]
are scheduled.  They are deleted automatically after being unused for a few
minutes.

A sign message contains all information needed to display a message on a [DMS]:
- [sign configuration]
- [MULTI] string
- owner (`system`; `sources`; `user`)
- [message priority]
- duration (minutes)

## Resources

* `iris/sign_message`
* `iris/img/{name}.gif`

Attribute [permissions]:

| Access       | Minimal     | `multi` check on `POST` |
|--------------|-------------|-------------------------|
| Read Only    | name, sign\_config, incident, multi, msg\_owner, flash\_beacon, msg\_priority, duration |
| 👉 Operate   |             | Match `msg_pattern` with `compose_hashtag` |
| 💡 Plan      |             | Dictionary [word] check |
| 🔧 Configure |             | No check                |

## Message Priority

Priorities determine precedence between operator messages and [DMS actions].

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


[cleared incidents]: incident_dms.html#clearing
[DMS]: dms.html
[DMS actions]: action_plans.html#dms-actions
[message priority]: #message-priority
[MULTI]: multi.html
[permissions]: user_roles.html#permissions
[sign configuration]: sign_configuration.html
[word]: words.html
