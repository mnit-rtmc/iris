# Dynamic Message Signs (DMS)

Select `View ➔ Message Signs ➔ DMS` menu item

A _dynamic message sign_ (DMS) is a sign which is capable of changing the
message displayed to motorists.  They can be classified as _character-matrix_,
_line-matrix_ or _full-matrix_ depending on the spacing between pixels.  Some
are monochrome and others support full color display.  All of these
configurations are supported.

The following features are supported:

* Querying currently displayed message
* Sending and displaying new messages
* Querying configuration information
* Querying diagnostic information
* Querying or sending [fonts]
* Sending [graphic images] to be displayed
* [Travel time] estimation
* [Variable speed advisories]
* [Slow traffic] warnings
* Checking banned or allowed [word] lists

## Setup

The DMS properties form can be used to configure the sign.

Field          | Description
---------------|-------------------------------------------------
Hashtags       | space-separated list of tags for selecting signs
Remote beacon  | beacon activated automatically when sign deployed
Static graphic | image of static sign in which DMS is inset
Device purpose | _general_ or _dedicated_ purpose for sign operation
Hidden         | hide sign when _available_ or _deployed_ styles are selected

A **hashtag** is the `#` character, followed by a string of letters and/or
numbers.  The are used to select signs for:
- [message pattern]s for composing
- automated [DMS actions]
- [alert configurations] and [alert messages]
- [lane-use MULTI] indications

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
  * only patterns whose **compose** hashtag matches the sign
  * only patterns containing **NO** [action tags]

When a pattern is selected, a series of selectors is populated with message
lines, depending on the text rectangles of that pattern.  The message preview
is updated as the message is being composed.  Once complete, pressing the
**Send** button will put the message onto the DMS.


[action tags]: action_plans.html#dms-action-tags
[alert configurations]: alert.html#dms-hashtags
[alert messages]: alert.html#alert-messages
[beacon]: beacons.html
[comm link]: comm_links.html
[DMS actions]: action_plans.html#dms-actions
[fonts]: fonts.html
[graphic images]: graphics.html
[lane-use MULTI]: lcs.html#lane-use-multi
[message pattern]: message_patterns.html
[NTCIP]: comm_links.html#ntcip
[Slow traffic]: slow_warning.html
[Travel time]: travel_time.html
[Variable speed advisories]: vsa.html
[word]: words.html
