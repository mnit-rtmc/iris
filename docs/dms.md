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

## Setup

There are a number of fields available for configuring a DMS.

Field           | Description
----------------|----------------------------------------------------
External beacon | [beacon] controlled when sign messages are deployed
Static graphic  | image of static sign in which DMS is inset
Device purpose  | _general_ or _dedicated_ purpose for sign operation
Override font   | font to override the _default font_ in the sign configuration

Sign groups and sign text libraries can be managed in the **Setup** tab.

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
sign group.  When a DMS is selected, a drop-down list is populated with quick
messages from sign groups of which that DMS is a member.  Quick messages are
also used for [DMS actions] as part of an [action plan].  For these messages, it
is best to leave the sign group blank.

**Prefix Page** is a flag which indicates that the quick message can be combined
with an operator message.  When a DMS action uses a _prefix page_ quick message,
operator messages will be modified by prepending the quick message before each
page.  Care must be taken to ensure the combined message can be displayed on the
sign.  This feature could be used, for example, to put a graphic logo on all
messages displayed on a sign.

## Spell Checker

Select `View ➔ Message Signs ➔ Dictionary` menu item

The spell checker verifies DMS messages when an operator presses the **Send**
button.  It uses two word lists: **Allowed** and **Banned**.

### Allowed Words

Allowed word checking is controlled by the `dict_allowed_scheme` [system
attribute]:

`dict_allowed_scheme` | Description
----------------------|-----------------------------------
`0`                   | Disable checking allowed word list
`1`                   | Suggest replacement of words not in allowed list
`2`                   | Reject messages containing words not in allowed list

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


[action plan]: action_plans.html
[beacon]: beacons.html
[DMS actions]: action_plans.html#dms-actions
[fonts]: fonts.html
[Slow traffic]: slow_warning.html
[system attribute]: system_attributes.html
[Travel time]: travel_time.html
[Variable speed advisories]: vsa.html
