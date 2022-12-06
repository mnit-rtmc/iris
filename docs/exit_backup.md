# Exit Ramp Backup Warning

Warning messages for backed-up exit ramp traffic can be displayed on DMS using
[DMS actions].  An `[exit` *…* `]` [action tag] in the [message pattern] will
cause a message to be displayed when the detector [occupancy] reaches a given
threshold.  This tag has the following format:

`[exit` *det,occ* `]`

**Parameters**

1. `det`: ID of exit ramp detector
2. `occ`: occupancy to trigger message (minimum threshold)

## Examples

Display message if the occupancy at detector 525 is 40 percent or higher:
```
[exit525,40]CONGESTION[nl]ON EXIT TO MAIN ST[nl]USE CAUTION
```


[action tag]: action_plans.html#dms-action-tags
[DMS actions]: action_plans.html#dms-actions
[message pattern]: message_patterns.html
[occupancy]: vehicle_detection.html#Traffic-Data
