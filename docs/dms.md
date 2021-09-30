# Dynamic Message Signs (DMS)

Select `View ➔ Message Signs ➔ DMS` menu item

A _dynamic message sign_ (DMS) is a sign which is capable of changing the
message displayed to motorists.  They can be classified as _character-matrix_,
_line-matrix_ or _full-matrix_ depending on the spacing between pixels.  Some
are monochrome and others support full color display.  All of these
configurations are supported.

The following features are supported:

* Querying currently displayed message
* Displaying new messages
* Querying configuration information
* Querying diagnostic information
* Querying or sending [fonts]
* Sending graphics
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

Field           | Description
----------------|----------------------------------------------------
External beacon | [beacon] controlled when sign messages are deployed
Static graphic  | image of static sign in which DMS is inset
Device purpose  | _general_ or _dedicated_ purpose for sign operation
Hidden          | hide sign when _available_ or _deployed_ styles are selected

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

## MULTI

**MULTI** is the _MarkUp Language for Transportation Information_ used to
compose DMS messages.  It is defined by the NTCIP 1203 standard.  Messages in
_MULTI_ are ASCII strings, with formatting or other instructions denoted by
tags inside square brackets.  For example, the `[nl]` tag indicates a new line.
Most of the useful MULTI tags are supported.

Tag                                           | Description              | Supported
----------------------------------------------|--------------------------|----------
`[cb`_x_`]`                                   | Message background color | Yes
`[pb`_z_`]` `[pb`_r,g,b_`]`                   | Page background color    | Yes
`[cf`_x_`]` `[cf`_r,g,b_`]`                   | Foreground color         | Yes
`[cr`_x,y,w,h,z_`]` `[cr`_x,y,w,h,r,g,b_`]`   | Color rectangle          | Yes
`[f`_x,y_`]`                                  | Field data               | No
`[flt`_x_`o`_y_`]` `[flo`_y_`t`_x_`]`         | Flashing text            | No
`[fo`_x_`]` `[fo`_x,cccc_`]`                  | Change font              | Yes
`[g`_n_`]` `[g`_n,x,y_`]` `[g`_n,x,y,cccc_`]` | Place graphic            | Yes
`[hc`_n_`]`                                   | Hexadecimal character    | No
`[jl`_n_`]`                                   | Line justification       | Yes
`[jp`_n_`]`                                   | Page justification       | Yes
`[ms`_x,y_`]`                                 | Manufacturer specific    | No
`[mv`_…_`]`                                   | Moving text              | No
`[nl]` `[nl`_s_`]`                            | New line                 | Yes
`[np]`                                        | New page                 | Yes
`[pt`_n_`]` `[pt`_n_`o`_f_`]`                 | Page time                | Yes
`[sc`_x_`]`                                   | Character spacing        | Yes
`[tr`_x,y,w,h_`]`                             | Text rectangle           | Yes

## Quick Messages

A _quick message_ is a fully composed DMS message which can be associated with a
sign group.  They are created using the [WYSIWYG editor].

When a DMS is selected, a drop-down list is populated with quick messages from
sign groups of which that DMS is a member.  Quick messages are also used for
[DMS action]s as part of an [action plan].  For these messages, it is best to
leave the sign group blank.

## Message Combining

It is sometimes necessary to display two messages at the same time.  _Message
Combining_ controls which other messages can be combined with a quick message.

Combining | Description
----------|----------------------------------------
Disable   | May not be combined with other messages
Operator  | May be combined with an operator or [incident] message
Action    | May be combined with a [DMS action] message

NOTE: two messages from [DMS action]s cannot be combined with each other, even
if both are configured with _Action_ combining.  Instead, the highest priority
message will be selected.

There are two methods of combining messages:

1. ![](images/msg_combined_shared.gif)
   **Shared**: Partitioning the sign into two regions, displayed simultaneously.
   In this case, the MULTI string of the first message will be prepended to each
   page of the second message.
   - The first message must end with a `[tr…]` (text rectangle) tag.
   - The first message must contain no `[np]` tags.
   - The second message must contain no `[tr…]` tags.
   - Neither message must contain no `[cb…]` or `[pb…]` tags.
2. ![](images/msg_combined_sequenced.gif)
   **Sequenced**: One message after another in a repeating sequence of pages.
   In this case, the messages will be joined with an `[np]` tag.

Before the second message, tags will be reset to the default values with this
[MULTI] string:

`[cf][fo][jl][jp]`

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
[DMS action]: action_plans.html#dms-actions
[fonts]: fonts.html
[incident]: incident_dms.html
[Slow traffic]: slow_warning.html
[system attribute]: system_attributes.html
[Travel time]: travel_time.html
[Variable speed advisories]: vsa.html
[WYSIWYG editor]: wysiwyg.html
