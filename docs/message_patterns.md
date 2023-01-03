# Message Patterns

Select `View ➔ Message Signs ➔ Message Patterns` or
`View ➔ Message Signs ➔ Message Editor` menu items

A _message pattern_ is a partially or fully composed [MULTI] message associated
with a sign config.  They can be scheduled by [DMS action]s as part of an
[action plan] or selected by operators when [composing messages].

## Message Combining

In some cases, a scheduled message can be combined with the operator composed
message:
- **Sequence**: separate pages
- **Shared**: split pages using _text rectangles_

The foreground color, font, and justification are reset to default values
between messages by inserting the [MULTI] string `[cf][fo][jl][jp]`.

### Sequenced Message Combining

If the scheduled message ends with a default `[cf]` tag, it can be combined
with an operator message to make a repeating _sequence_ of pages.

Example:
- Scheduled message:
  `[cr1,1,160,54,0,0,125][cr1,18,160,1,255,255,255][tr1,1,160,17][cf255,255,255][fo5][jp3]TRUCK PARKING[tr4,24,154,30][jl2]REST AREA[jl4]4 MI[nl5][jl2]SPACES OPEN[jl4]10[cf]`
- Operator message:
  `STALLED VEHICLE[nl]IN RIGHT LANE[nl]USE CAUTION`
- Combined message:
  `[cr1,1,160,54,0,0,125][cr1,18,160,1,255,255,255][tr1,1,160,17][cf255,255,255][fo5][jp3]TRUCK PARKING[tr4,24,154,30][jl2]REST AREA[jl4]4 MI[nl5][jl2]SPACES OPEN[jl4]10[cf][fo][jl][jp][np]STALLED VEHICLE[nl]IN RIGHT LANE[nl]USE CAUTION`

![](images/msg_combined_sequenced.gif)

### Shared Message Combining

With this method, the sign is partitioned into two regions, displaying both
messages simultaneously.  The scheduled message is prepended to each page of
the operator message.

- The scheduled message must contain no `[np]` tags.
- The scheduled message must end with a `[tr…]` tag.
- Each page of the operator message must start with that same `[tr…]` tag.
- The operator message must not contain any other `[tr…]` tags.

Example:
- Scheduled message:
  `[cr1,1,240,24,1,23,9][cf250,250,250][fo13][tr1,5,240,18][jl3]EXPRESS LANE[tr1,31,240,40]OPEN TO ALL[nl6]TRAFFIC[g7,110,75][cr241,1,2,96,255,255,255][tr243,1,350,96]`
- Operator message:
  `[tr243,1,350,96]STALLED VEHICLE[nl]IN RIGHT LANE[nl]USE CAUTION`
- Combined message:
  `[cr1,1,240,24,1,23,9][cf250,250,250][fo13][tr1,5,240,18][jl3]EXPRESS LANE[tr1,31,240,40]OPEN TO ALL[nl6]TRAFFIC[g7,110,75][cr241,1,2,96,255,255,255][cf][fo][jl][jp][tr243,1,350,96]STALLED VEHICLE[nl]IN RIGHT LANE[nl]USE CAUTION`

![](images/msg_combined_shared.gif)


[action plan]: action_plans.html
[composing messages]: dms.html#composing-messages
[DMS action]: action_plans.html#dms-actions
[MULTI]: multi.html
[WYSIWYG editor]: wysiwyg.html
