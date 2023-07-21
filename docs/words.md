# Words

Select `View ➔ Message Signs ➔ Words` menu item

Words can either be **banned** or **allowed** for use in messages.

If an operator has **Manage** [permissions] for a [DMS], they can enter
free-form text as long as none of the words are banned.

## Abbreviation

Allowed words are used for automatic abbreviation of [message lines] and
[incident] messages, when necessary.

On lines which are too wide, a single _allowed_ word is replaced with its
abbreviated form.  Then the line is checked again, and if still too wide, the
process repeats.  If it still doesn't fit after all possible words have been
abbreviated, the message is discarded.


[DMS]: dms.html
[incident]: incident_dms.html
[message lines]: dms.html#composing-messages
[permissions]: permissions.html
