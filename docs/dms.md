# Dynamic Message Signs (DMS)

Select `View ➔ Message Signs ➔ DMS` menu item

A _dynamic message sign_ (DMS) is a sign which is capable of changing the
message displayed to motorists.

The following features are supported:

* Querying currently displayed [sign message]
* Sending and displaying [sign message]s
* Querying [sign configuration]
* Querying diagnostic information
* Querying or sending [font]s
* Sending [graphic] images to be displayed
* [Travel time] estimation
* Road weather information system ([RWIS]) messages
* [Slow traffic] warnings
* [Tolling] messages for congestion pricing
* Free-form text entry with banned [word] checks

<details>
<summary>API Resources 🕵️ </summary>

* `iris/dms_message`
* `iris/dms_pub`
* `iris/api/dms` (primary)
* `iris/api/dms/{name}`

| Access       | Primary                                  | Secondary     |
|--------------|------------------------------------------|---------------|
| 👁️  View      | name, location, msg\_current, has_faults | sign\_config, sign\_detail, geo\_loc, msg\_sched, status, pix\_failures |
| 👉 Operate   | lock                                     | device\_request † |
| 💡 Manage    |                                          | preset        |
| 🔧 Configure | controller, notes                        | pin, static\_graphic, beacon |

† _Write only_

Checks of [free-form text] are also affected by the access level.

</details>

## Setup

The DMS properties form has setup information.

Field          | Description
---------------|---------------------------------------------------
Notes          | administrator notes, possibly including [hashtag]s
Remote beacon  | beacon activated automatically when sign deployed
Static graphic | image of static sign in which DMS is inset

**Internal** beacons are controlled through the DMS controller using the [NTCIP]
protocol.  **Remote** [beacon]s are controlled using a separate [comm link].

## Status JSON

Sign status data is stored as JSON in `status`.

Key              | Value
-----------------|--------------------------------------------------
`faults`         | Current fault conditions, separated by semicolons
`photocells`     | Array of objects: `description`, `error`, `reading`
`light_output`   | Integer light output percentage
`power_supplies` | Array of objects: `description`, `supply_type`, `error`, `detail`, `voltage`
`cabinet_temps`  | Array of integer temps (C)
`ambient_temps`  | Array of integer temps (C)
`housing_temps`  | Array of integer temps (C)

## Styles

Each DMS can have a number of _styles_, depending on its current state.  Styles
are ordered by precedence, determining which color a map marker is drawn.

Style     | Description
----------|---------------------------------------
Available | Sign is blank and ready to use
Deployed  | Displaying an operator-defined message
Schedule  | Displaying a scheduled message
External  | Displaying a message from an external system (not IRIS)
Fault     | One or more faults detected in sign
Offline   | Communication to sign offline
Purpose   | [Dedicated purpose] sign
All       | All signs

## Composing Messages

When a DMS is selected, the [message pattern] selector is populated:
  * only patterns whose **compose** [hashtag] matches the sign
  * only patterns containing **NO** [action tags]

When an operator chooses a pattern, a series of selectors is populated with
[message line]s, depending on the [fillable text rectangles].  Any lines which
are too wide to fit the sign are [abbreviated] as necessary.

The selectors may also allow **free-form text** entry, depending on the
**DMS** permision access level:

* 👉 **Operate**: No free-form text permitted
* 💡 **Manage**: Free-form text checked for **banned** [word]s
* 🔧 **Configure**: Any free-form text permitted (no check)

The message preview is updated as the user composes the message.  When the
**Send** button is pressed, it is set as the `lock` attribute.  The server
performs a validation check ensuring the user has [permissions] for any
free-form text before sending the message to the sign.


[abbreviated]: words.html#abbreviation
[action tags]: action_plans.html#action-tags
[beacon]: beacons.html
[comm link]: comm_links.html
[free-form text]: #composing-messages
[dedicated purpose]: hashtags.html#dedicated-purpose
[fillable text rectangles]: message_patterns.html#fillable-text-rectangles
[font]: fonts.html
[graphic]: graphics.html
[hashtag]: hashtags.html
[message line]: message_patterns.html#message-lines
[message pattern]: message_patterns.html
[NTCIP]: protocols.html#ntcip
[permissions]: permissions.html
[rwis]: rwis.html
[sign configuration]: sign_configuration.html
[sign message]: sign_message.html
[Slow traffic]: slow_warning.html
[tolling]: tolling.html
[travel time]: travel_time.html
[word]: words.html
