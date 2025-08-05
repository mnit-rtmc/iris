# Slow Traffic Warning

Warning messages for slow traffic can be displayed on DMS using
[device actions].  A `[slow` *…* `]` [action tag] in the [message pattern]
will be replaced with the appropriate value.  Additionally, the entire message
will be displayed **conditionally** in the presence of slow traffic.  This tag
has the following format:

`[slow` *speed,distance,mode* `]`

**Parameters**

1. `speed`: Highest speed to activate the warning, in mph.
2. `distance`: (tenths of mile) to search for slow traffic, relative to the DMS
   location.
3. `mode`: Tag replacement mode (`none` if omitted)
   - `none`: a blank string (**Condition** tag mode)
   - `dist`: distance rounded to nearest mile
   - `speed`: speed rounded to nearest 5 mph

## Examples

Conditionally display message if traffic slower than 35 mph within 1 mile:
```
[slow35,10]SLOW[nl]TRAFFIC[nl]AHEAD
```

Display message if traffic slower than 40 mph within 6 miles:
```
SLOW TRAFFIC[nl][slow40,60,dist] MILES[nl]USE CAUTION
```

Display message if traffic slower than 45 mph within 0.5 miles:
```
[slow45,5,speed] MPH[nl]1/2 MILE[nl]AHEAD
```


[action tag]: action_plans.html#action-tags
[device actions]: action_plans.html#device-actions
[message pattern]: message_patterns.html
