# Words

Select `View ➔ Message Signs ➔ Words` menu item

[DMS] messages can be checked against **allowed** and **banned** word lists when
an operator presses the **Send** button.

## Allowed Words

Allowed word checking is controlled by the `dict_allowed_scheme` [system
attribute]:

`dict_allowed_scheme` | Description
----------------------|-----------------------------------
`0`                   | Disable checking allowed word list
`1`                   | Suggest replacement of words not in allowed list
`2`                   | Reject messages containing words not in allowed list

This list is also used for [abbreviation] of [incident] messages.

## Banned Words

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
[DMS]: dms.html
[incident]: incident_dms.html
[system attribute]: system_attributes.html
