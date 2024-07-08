# RWIS

Road weather information system (RWIS) messages can be displayed on [DMS],
using data collected from [weather sensor]s.

## RWIS Action Tags

Warning messages for road weather conditions can be displayed on DMS using
[DMS actions].  An `[rwis_` *…* `]` [action tag] in the [message pattern] will
cause a message to be displayed when the specified weather condition is
detected.  The tag has the following format:

`[rwis_` *condition*,*level* `]`

Condition    | Level | Field Threshold †    | Meaning
-------------|-------|----------------------|----------------
`slippery`   | 1     | `friction` < 70      | SLIPPERY
`slippery`   | 2     | + `surface_temp` < 0 | VERY SLIPPERY
`slippery`   | 3     | + `friction` < 60    | ICE DETECTED
`windy`      | 1     | `wind_gust` > 64     | WIND GUSTS
`windy`      | 2     | + `wind_gust` > 96   | HIGH WINDS
`visibility` | 1     | `visibility` < 1609  | REDUCED VISIBILITY
`visibility` | 2     | + `visibility` < 402 | LOW VISIBILITY
`flooding`   | 1     | `precip` > 6         | FLOODING POSSIBLE
`flooding`   | 2     | + `precip` > 8       | FLASH FLOODING

† *Thresholds configurable with [system attributes](#system-attributes)*

## Weather Fields

There are five weather data fields used for condition calculations:

1. `friction`: pavement friction coefficient in percent
2. `surface_temp`: temperature in degrees celcius
3. `wind_gust`: gusting wind speed in kilometers per hour (kph)
4. `visibility`: distance in meters (m)
5. `precip`: one hour precipitation accumulation (mm)

## System Attributes

These [system attributes] can be used to adjust thresholds for RWIS conditions.

Attribute Name            | Field          | Default Threshold
--------------------------|----------------|------------------
`rwis_slippery_1_percent` | `friction`     | 70
`rwis_slippery_2_degrees` | `surface_temp` | 0
`rwis_slippery_3_percent` | `friction`     | 60
`rwis_windy_1_kph`        | `wind_gust`    | 64
`rwis_windy_2_kph`        | `wind_gust`    | 96
`rwis_visibility_1_m`     | `visibility`   | 1609
`rwis_visibility_2_m`     | `visibility`   | 402
`rwis_flooding_1_mm`      | `precip`       | 6
`rwis_flooding_2_mm`      | `precip`       | 8

Also, the `rwis_obs_age_limit_secs` determines the maximum valid age for a
weather sensor observation.

## DMS Weather Sensors

An RWIS action tag can use data from one or more weather sensors.  Each DMS can
have its own set of sensors configured.  A sign with no associated weather
sensors will not activate messages from RWIS action tags.

## Testing Condition Levels

The weather sensor properties form has buttons to test RWIS message automation.
If the "Level 1" button is pressed, then all RWIS conditions from that weather
sensor will report level 1 conditions.  Similarly, the "Level 2" button causes
each condition to report level 2.  The testing mode will clear automatically
the next time the sensor records an observation.


[action tag]: action_plans.html#dms-action-tags
[DMS]: dms.html
[DMS actions]: action_plans.html#dms-actions
[message pattern]: message_patterns.html
[system attributes]: system_attributes.html
[weather sensor]: weather_sensors.html
