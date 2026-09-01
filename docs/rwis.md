# RWIS

Road weather information system (RWIS) messages can be activated on [DMS],
using data collected from [weather sensor]s.

[Phase actions] can change the phase of an action plan based on conditions
at an RWIS station.

## Condition Fields

There are five weather data fields used for triggering conditions:

1. `friction`: pavement friction coefficient in percent
2. `surface_temp`: temperature in degrees celcius
3. `wind_gust`: gusting wind speed in kilometers per hour (kph)
4. `visibility`: distance in meters (m)
5. `precipitation`: one hour precipitation accumulation (mm)

## System Attributes

The `rwis_obs_age_limit_secs` [system attribute] can be used to adjust the
maximum valid age for an observation.


[device actions]: action_plans.html#device-actions
[DMS]: dms.html
[message pattern]: message_patterns.html
[phase actions]: action_plans.html#phase-actions
[system attribute]: system_attributes.html
[weather sensor]: weather_sensors.html
