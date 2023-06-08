# MULTI

**MULTI** is the _MarkUp Language for Transportation Information_ used to
compose [DMS] messages, as defined by the [NTCIP] 1203 standard.  Messages in
_MULTI_ are ASCII strings, with formatting or other instructions denoted by tags
inside square brackets.  For example, the `[nl]` tag indicates a new line.

IRIS supports most tags when viewing sign messages:

Tag                                           | Description              | Supported
----------------------------------------------|--------------------------|----------
`[cb`_x_`]`                                   | Message background color | Yes
`[pb`_z_`]` `[pb`_r,g,b_`]`                   | Page background color    | Yes
`[cf]` `[cf`_x_`]` `[cf`_r,g,b_`]`            | Foreground color         | Yes
`[cr`_x,y,w,h,z_`]` `[cr`_x,y,w,h,r,g,b_`]`   | Color rectangle          | Yes
`[f`_x,y_`]`                                  | Field data               | No
`[flt`_x_`o`_y_`]` `[flo`_y_`t`_x_`]`         | Flashing text            | No
`[fo]` `[fo`_x_`]` `[fo`_x,cccc_`]`           | Change font              | Yes
`[g`_n_`]` `[g`_n,x,y_`]` `[g`_n,x,y,cccc_`]` | Place graphic            | Yes
`[hc`_n_`]`                                   | Hexadecimal character    | No
`[jl]` `[jl`_n_`]`                            | Line justification       | Yes
`[jp]` `[jp`_n_`]`                            | Page justification       | Yes
`[ms`_x,y_`]`                                 | Manufacturer specific    | No
`[mv`_…_`]`                                   | Moving text              | No
`[nl]` `[nl`_s_`]`                            | New line                 | Yes
`[np]`                                        | New page                 | Yes
`[pt`_n_`]` `[pt`_n_`o`_f_`]`                 | Page time                | Yes
`[sc`_x_`]`                                   | Character spacing        | Yes
`[tr`_x,y,w,h_`]`                             | Text rectangle           | Yes

## Default Values

Some tags have **default** values, which will be used if a tag is not specified
in a message.

| Description          | Default Value       |
|----------------------|---------------------|
| Background color     | 0 (black)           |
| Background color RGB | 0, 0, 0 (black)     |
| Foreground color     | 9 (amber)           |
| Foreground color RGB | 255, 208, 0 (amber) |
| Flash on time        | 0.0 s               |
| Flash off time       | 0.0 s               |
| Font                 | by [sign config]    |
| Line justificaiton   | 3 (center)          |
| Page justification   | 2 (top)             |
| Page on time         | 2.8 s               |
| Page off time        | 0.0 s               |
| Character set        | 2 (eightBit)        |

## Non-MULTI Tags

Additional non-MULTI tags are supported in specific contexts, interpreted by
IRIS and replaced with dynamic values:
- [Action Tags], for use in scheduled DMS actions
- [Locator Tags], for incident DMS deployment


[Action Tags]: action_plans.html#dms-action-tags
[DMS]: dms.html
[Locator Tags]: incident_dms.html#locator-tags
[NTCIP]: https://www.ntcip.org/document-numbers-and-status/
[sign config]: dms.html#configuration
