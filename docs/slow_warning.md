# Slow Traffic Warning

The `[slow…]` [action tag] allows warnings to be displayed on DMS when traffic
is slow.  The tag has three parameters, separated by commas.

 1. Highest speed to activate the warning, in mph.
 2. Distance (tenths of mile) to search for slow traffic, relative to the DMS
    location.
 3. Tag replacement mode (`none` if omitted)
    - `none`: a blank string
    - `dist`: distance rounded to nearest mile
    - `speed`: speed rounded to nearest 5 mph

## Examples

Display message if traffic slower than 35 mph within 1 mile:
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


[action tag]: action_plans.html#dms-action-tags
