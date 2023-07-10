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
* Sending [graphic images] to be displayed
* [Travel time] estimation
* [Variable speed advisories]
* [Slow traffic] warnings
* Free-form text entry with banned [word] checks

<details>
<summary>API Resources</summary>

* `iris/dms_pub`
* `iris/api/dms`
* `iris/api/dms/{name}`

Attribute [permissions]:

| Access       | Minimal                      | Full        |
|--------------|------------------------------|-------------|
| Read Only    | name, location, msg\_current | sign\_config, sign\_detail, geo\_loc, msg\_sched, status, stuck\_pixels |
| 👉 Operate   |                              | msg\_user   |
| 💡 Plan      | notes, hashtags              | device\_req |
| 🔧 Configure | controller                   | pin         |

Checks of [free-form text] are also affected by the access level.

</details>

## Setup

The DMS properties form has setup information.

Field          | Description
---------------|------------------------------------------------
Hashtags       | space-separated list of [hashtag]s for the sign
Remote beacon  | beacon activated automatically when sign deployed
Static graphic | image of static sign in which DMS is inset
Device purpose | _general_ or _dedicated_ purpose for sign operation
Hidden         | hide sign when _available_ or _deployed_ styles are selected

**Internal** beacons are controlled through the DMS controller using the [NTCIP]
protocol.  **Remote** [beacon]s are controlled using a separate [comm link].

## Operating

Each DMS can have a number of _styles_, depending on its current state.  Styles
are ordered by precedence, determining which color a map marker is drawn.

Style     | Description
----------|---------------------------------------
Available | Sign is blank and ready to use
Deployed  | Displaying an operator-defined message
Schedule  | Displaying a scheduled message
External  | Displaying a message from an external system (not IRIS)
Maint.    | Sign requires maintenance, but might still be functional
Failed    | Communication failure to sign
Purpose   | _Dedicated-purpose_ sign (travel time, wayfinding, etc.)
All       | All signs

## Composing Messages

When a DMS is selected, a few things happen:
- the sign's location is displayed
- the current message is rendered
- the [message pattern] selector is populated:
  * only patterns whose **compose** [hashtag] matches the sign
  * only patterns containing **NO** [action tags]

When an operator chooses a pattern, a series of selectors is populated with
[message lines], depending on the [fillable text rectangles].  These selectors
may also allow **free-form text** entry, depending on the permision access
level of the user:

* 👉 **Operate**: No free-form text permitted
* 💡 **Plan**: Free-form text checked for **banned** [word]s
* 🔧 **Configure**: Any free-form text permitted (no check)

The message preview is updated as the user composes the message.  When the
**Send** button is pressed, a [sign message] is created and set as `msg_user`.
The server then performs a validation check ensuring the user has permission
for any free-form text.


[action tags]: action_plans.html#dms-action-tags
[beacon]: beacons.html
[comm link]: comm_links.html
[free-form text]: #composing-messages
[DMS actions]: action_plans.html#dms-actions
[fillable text rectangles]: message_patterns.html#fillable-text-rectangles
[font]: fonts.html
[graphic images]: graphics.html
[hashtag]: hashtags.html
[message lines]: message_patterns.html#message-lines
[message pattern]: message_patterns.html
[NTCIP]: protocols.html#ntcip
[permissions]: permissions.html
[sign configuration]: sign_configuration.html
[sign message]: sign_message.html
[Slow traffic]: slow_warning.html
[Travel time]: travel_time.html
[Variable speed advisories]: vsa.html
[word]: words.html
