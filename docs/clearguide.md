# ClearGuide

ClearGuide is a mobility intelligence system that provides real-time route and
workzone delay, travel time and other data. The ClearGuide device driver comm
link URL specifies the HTTPS address of a ClearGuide server. The controller
associated with the comm link requires the password field to contain both the
user name and password in the form username:password. The system attribute
`clearguide_key` should also be specified to indicate the ClearGuide agency
name.

## ClearGuide Action Tag

Real-time ClearGuide data can be embedded in DMS messages using [DMS actions].
A `[cg` *…* `]` [action tag] in the [message pattern] will be replaced by the
appropriate value.  The action tag has the following format:

`[cg` *dms,wid,min,mode,idx* `]`

**Parameters**

1. `dms`: Name of DMS associated with the workzone defined in ClearGuide
2. `wid`: The workzone ID defined in ClearGuide
3. `min`: The minimum acceptable value for the statistic read from ClearGuide
4. `mode`: An identifier for the desired ClearGuide statistic:
   - `tt`: workzone travel time calculated as max(tta, ttsl)
   - `ttsl`: workzone travel time at the speed limit
   - `tta`: actual travel time, may be > ttsl
   - `delay`: workzone delay
   - `sp`: workzone speed
5. `idx`: The zero-based index of the workzone associated with the DMS. This only applies for DMS with more than 1 associated workzone. It is optional and defaults to zero.


[action tag]: action_plans.html#dms-action-tags
[DMS actions]: action_plans.html#dms-actions
[message pattern]: message_patterns.html
