# ClearGuide

ClearGuide is a mobility intelligence system that provides real-time route and
workzone delay, travel time and other data.

To use the system, one [comm link] must be configured using the `clearguide`
protocol.  The `URI` specifies the HTTPS address of a ClearGuide server.
There must be a single controller associated with the comm link, and its
password field must contain both the user name and password in the form
`username:password`.  The `api_key_clearguide` [system attribute] should also
contain the ClearGuide agency name.

## ClearGuide Action Tag

Real-time ClearGuide data can be embedded in DMS messages using
[device actions].  A `[cg` *…* `]` [action tag] in the [message pattern] will
be replaced by the appropriate value.  The action tag has the following
format:

`[cg` *dms,wid,range,mode,idx* `]`

**Parameters**

1. `dms`: Name of DMS associated with the workzone defined in ClearGuide
2. `wid`: The workzone ID defined in ClearGuide
3. `range`: Numeric range for the mode's statistic, one of:
   - `min-max`: Range of acceptable values (inclusive)
   - `min`: Minimum accepable value
4. `mode`: Tag replacement mode for ClearGuide statistics:
   - `tt`: workzone travel time (minutes), calculated as `max(tta, ttsl)`
   - `ttsl`: workzone travel time (minutes) at the speed limit
   - `tta`: actual travel time (minutes), may be > `ttsl`
   - `delay`: workzone delay (minutes)
   - `sp`: workzone speed (mph)
   - `sp_cond`: **Condition** dependent on workzone speed (mph)
5. `idx`: The zero-based index of the workzone associated with the DMS.
   This only applies for DMS with more than 1 associated workzone.  It is
   optional and defaults to zero.


[action tag]: action_plans.html#action-tags
[comm link]: comm_links.html
[device actions]: action_plans.html#device-actions
[message pattern]: message_patterns.html
[system attribute]: system_attributes.html
