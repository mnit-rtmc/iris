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

## Styles

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

## Setup

There are a number of fields available for configuring a DMS.

Field          | Description
---------------|--------------------------------------------------
Remote beacon  | beacon activated automatically when sign deployed
Static graphic | image of static sign in which DMS is inset
Device purpose | _general_ or _dedicated_ purpose for sign operation
Hidden         | hide sign when _available_ or _deployed_ styles are selected

**Internal** beacons are controlled through the DMS controller using the [NTCIP]
protocol.  **Remote** [beacon]s are controlled using a separate [comm link].

## Messages

_Sign groups_ and associated _sign text_ libraries can be managed in the
**Messages** tab.

All sign groups are displayed in the table, even if the DMS is not a member of
that group.  To add the DMS to a group, select the _Member_ check box.

When a sign group is selected, its message library is displayed in the _sign
text_ table.  Each row contains a message for one _line_ of the sign.  The
_rank_ determines sort order in message lists.

The _message preview_ displays a graphical rendering of the selected sign text.

Select an _override font_ to use a font other than the _default font_ from the
sign configuration.

## Quick Messages

A _quick message_ is a fully composed [MULTI] message which can be associated
with a sign group.  They are created using the [WYSIWYG editor].

When a DMS is selected, a drop-down list is populated with quick messages from
sign groups of which that DMS is a member.  Quick messages are also used for
[DMS action]s as part of an [action plan].  For these messages, it is best to
leave the sign group blank.

## Message Combining

It is sometimes necessary to display two messages on a sign at the same time.
This is controlled with the quick message _Combining_ field:

Combining | Description
----------|--------------------
Disable   | May not be combined
First     | May combine as first message
Second    | May combine as second message
Either    | May combine as first or second message

Messages can only be combined in certain cases:
- The first message is scheduled by a [DMS action] and the second is selected by
  an operator.
- Both messages are selected by an operator, the first being a quick message and
  the second composed (line-by-line).

The [MULTI] string `[cf][fo][jl][jp]` is automatically inserted before the
second message to reset the foreground color, font, and justification tags to
default values.

There are two methods of combining messages: **Shared** and **Sequenced**.

### Shared Message Combining

With this method, the sign is partitioned into two regions, displaying both
messages simultaneously.

- The first message must end with a `[tr…]` (text rectangle) tag.
- The first message must contain no `[np]` tags.
- The second message must contain no `[tr…]` tags.

The first message is prepended to each page of the second message.  If the
resulting combined message cannot be displayed on the sign, the second message
will be used with **NO** combining.

Example:  
- First message:
  `[cr1,1,240,24,1,23,9][cf250,250,250][fo13][tr1,5,240,18][jl3]EXPRESS LANE[tr1,31,240,40]OPEN TO ALL[nl6]TRAFFIC[g7,110,75][cr241,1,2,96,255,255,255][tr243,1,350,96]`
- Second message:
  `STALLED VEHICLE[nl]IN RIGHT LANE[nl]USE CAUTION`
- Combined message:
  `[cr1,1,240,24,1,23,9][cf250,250,250][fo13][tr1,5,240,18][jl3]EXPRESS LANE[tr1,31,240,40]OPEN TO ALL[nl6]TRAFFIC[g7,110,75][cr241,1,2,96,255,255,255][tr243,1,350,96][cf][fo][jl][jp]STALLED VEHICLE[nl]IN RIGHT LANE[nl]USE CAUTION`

![](images/msg_combined_shared.gif)

### Sequenced Message Combining

When the criteria for shared combining are not met, the messages are combined in
a repeating _sequence_ of pages.  The messages are separated by an `[np]` tag.

Example:
- First message:
  `[cr1,1,160,54,0,0,125][cr1,18,160,1,255,255,255][tr1,1,160,17][cf255,255,255][fo5][jp3]TRUCK PARKING[tr4,24,154,30][jl2]REST AREA[jl4]4 MI[nl5][jl2]SPACES OPEN[jl4]10`
- Combined message:
  `[cr1,1,160,54,0,0,125][cr1,18,160,1,255,255,255][tr1,1,160,17][cf255,255,255][fo5][jp3]TRUCK PARKING[tr4,24,154,30][jl2]REST AREA[jl4]4 MI[nl5][jl2]SPACES OPEN[jl4]10[cf][fo][jl][jp][np]STALLED VEHICLE[nl]IN RIGHT LANE[nl]USE CAUTION`

![](images/msg_combined_sequenced.gif)

## Words

Select `View ➔ Message Signs ➔ Words` menu item

DMS messages can be checked against **allowed** and **banned** word lists when
an operator presses the **Send** button.

### Allowed Words

Allowed word checking is controlled by the `dict_allowed_scheme` [system
attribute]:

`dict_allowed_scheme` | Description
----------------------|-----------------------------------
`0`                   | Disable checking allowed word list
`1`                   | Suggest replacement of words not in allowed list
`2`                   | Reject messages containing words not in allowed list

This list is also used for [abbreviation] of [incident] messages.

### Banned Words

Similarly, the `dict_banned_scheme` [system attribute] controls banned word
functionality:

`dict_banned_scheme` | Description
---------------------|----------------------------------
`0`                  | Disable checking banned word list
`1`                  | Suggest replacement of words in banned list
`2`                  | Reject messages containing words in banned list

When appropriate, a form appears to suggest changes or inform the operator of a
rejected message.


[abbreviation]: incident_dms.html#abbreviation
[action plan]: action_plans.html
[beacon]: beacons.html
[comm link]: comm_links.html
[DMS action]: action_plans.html#dms-actions
[fonts]: fonts.html
[graphic images]: graphics.html
[incident]: incident_dms.html
[MULTI]: multi.html
[NTCIP]: comm_links.html#ntcip
[Slow traffic]: slow_warning.html
[system attribute]: system_attributes.html
[Travel time]: travel_time.html
[Variable speed advisories]: vsa.html
[WYSIWYG editor]: wysiwyg.html
