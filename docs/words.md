# Words

Select `View ➔ Message Signs ➔ Words` menu item

Words can either be **banned** or **allowed** for use in messages.

If an operator has **Manage** [permissions] for a [DMS], they can enter
free-form text as long as none of the words are banned.

<details>
<summary>API Resources 🕵️ </summary>

* `iris/api/word` (primary)
* `iris/api/word/{name}`

| Access       | Primary        | Secondary |
|--------------|----------------|-----------|
| 👁️  View      | name           |           |
| 👉 Operate   |                |           |
| 💡 Manage    |                |           |
| 🔧 Configure | abbr, allowed  |           |

</details>

## Abbreviation

_Allowed_ words are used for automatic abbreviation of [message lines] and
[incident] messages, when necessary.

On lines which are too wide, a single word is chosen and replaced with its
abbreviated form.  If it's still too wide, the process repeats.  If a message
doesn't fit after all possible words have been abbreviated, it is discarded.


[DMS]: dms.html
[incident]: incident_dms.html
[message lines]: dms.html#composing-messages
[permissions]: permissions.html
