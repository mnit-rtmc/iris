# ClearGuide

ClearGuide is a mobility intelligence system that provides real-time route and
workzone delay, travel time and other data. The ClearGuide device driver comm
link URL specifies the HTTPS address of a ClearGuide server. The controller
associated with the comm link requires the password field to contain both the
user name and password in the form username:password. The system attribute
`clearguide_key` should also be specified to indicate the ClearGuide agency
name.

## ClearGuide Action Tag

The `[cg]` [action tag] is used to embed real-time ClearGuide data into DMS
messages and has this format:

`[cg` *dms,wid,min,mode,idx* `]`

**Parameters**

* `dms`: Name of DMS associated with the workzone defined in ClearGuide
* `wid`: The workzone ID defined in ClearGuide
* `min`: The minimum acceptable value for the statistic read from ClearGuide
* `mode`: An identifier for the desired ClearGuide statistic: tt=workzone travel time, delay=workzone delay
* `idx`: The zero-based index of the workzone associated with the DMS. This only applies for DMS with more than 1 associated workzone. It is optional and defaults to zero.

[action tag]: action_plans.html#clearguide

