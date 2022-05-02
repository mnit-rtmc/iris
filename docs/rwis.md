# Road Weather Information Systems

Select `View ➔ Weather Sensors` menu item

Road Weather Information Systems, or **RWIS** can sense data such as
precipitation rate, road surface temperature, wind speed, etc.  The [NTCIP]
and [Org-815] protocols can collect RWIS data.

## Settings

Sensor settings are checked once per day and stored in the `settings` column of
the `weather_sensor` table as JSONB.  When the value changes, a time-stamped
record is added to the `weather_sensor_settings` table.

The possible settings fields are:

Field                    | Description
-------------------------|---------------------------------------------------
`sys_descr`              | System description
`sys_contact`            | System contact information
`sys_name`               | System name
`sys_location`           | System location
`reference_elevation`    | Reference elevation above mean sea level (meters)
`pressure_sensor_height` | Height relative to `reference_elevation` (meters)
`wind_sensor`            | Array of wind sensor settings objects
↳`height`                | Height relative to `reference_elevation` (meters)
`temperature_sensor`     | Array of temperature sensor settings objects
↳`height`                | Height relative to `reference_elevation` (meters)
`pavement_sensor`        | Array of pavement sensor settings objects
↳`location`              | Sensor location description
↳`pavement_type`         | Pavement type description
↳`height`                | Pavement height relative to `reference_elevation` (meters)
↳`exposure`              | Rough estimate of solar energy (percent)
↳`sensor_type`           | Sensor type description
`sub_surface_sensor`     | Array of sub-surface settings objects
↳`location`              | Sensor location description
↳`sub_surface_type`      | Sub-surface type description
↳`depth`                 | Depth below pavement surface (meters)

## Sample Data

Sample data is collected at regular intervals equal to the polling period of the
[comm link].  It is stored in the `sample` column of the `weather_sensor` table
as JSONB.  When a new sample is stored, a time-stamped record is added to the
`weather_sensor_sample` table.

Only collected data is stored in the `sample` column.  The possible fields are:

Field                         | Description
------------------------------|------------------------------------
`atmospheric_pressure`        | Atmospheric pressure (pascals)
`visibility`                  | Visibility (meters)
`visibility_situation`        | Visibility situation description
`wind_sensor`                 | Array of wind sensor sample objects
↳`avg_speed`                  | Two minute average wind speed (m/s)
↳`avg_direction`              | Two minute average wind direction †
↳`spot_speed`                 | Spot wind speed (m/s)
↳`spot_direction`             | Spot wind direction †
↳`gust_speed`                 | Ten minute maximum wind gust speed (m/s)
↳`gust_direction`             | Ten minute maximum wind gust direction †
↳`situation`                  | Wind situation description
`temperature_sensor`          | Array of temperature sensor sample objects
↳`air_temp`                   | Instantaneous air temperature (℃)
`wet_bulb_temp`               | Instantaneous wet-bulb temperature (℃)
`dew_point_temp`              | Instantaneous dew point temperature (℃)
`max_air_temp`                | Maximum 24 hour air temperature (℃)
`min_air_temp`                | Minimum 24 hour air temperature (℃)
`relative_humidity`           | Relative humidity (percent)
`precip_rate`                 | Precipitation rate (mm/hr)
`precip_1_hour`               | One hour accumulated precipitation (mm)
`precip_3_hours`              | Three hour accumulated precipitation (mm)
`precip_6_hours`              | Six hour accumulated precipitation (mm)
`precip_12_hours`             | Twelve hour accumulated precipitation (mm)
`precip_24_hours`             | Twenty-four hour accumulated precipitation (mm)
`precip_situation`            | Precipitation situation description
`pavement_sensor`             | Array of pavement sensor sample objects
↳`surface_status`             | Surface status description
↳`surface_temp`               | Surface temperature (℃)
↳`pavement_temp`              | Pavement temperature (℃)
↳`sensor_error`               | Sensor error description
↳`ice_or_water_depth`         | Surface ice or water depth (meters)
↳`salinity`                   | Salinity (parts per 100,000 by weight)
↳`freeze_point`               | Surface freeze point (℃)
↳`black_ice_signal`           | Black ice signal description
↳`friction`                   | Coefficient of friction (percent)
`sub_surface_sensor`          | Array of sub-surface sample objects
↳`temp`                       | Sub-surface temperature (℃)
↳`moisture`                   | Sub-surface moisture saturation (percent)
↳`sensor_error`               | Sensor error description
`total_sun`                   | Sun during previous day (minutes; 0-1440)
`cloud_situation`             | Cloud situation description
`solar_radiation`             | Solar radiation (joules / m²) _deprecated_
`instantaneous_terrestrial_radiation` | Terrestrial radiation (watts / m²)
`instantaneous_solar_radiation`       | Solar radiation (watts / m²)
`total_radiation`             | Total from collection period (watts / m²)
`total_radiation_period`      | Total radiation period (seconds)

_† Wind direction in degrees clockwise from due north_.


[comm link]: comm_links.html
[NTCIP]: comm_links.html#ntcip
[ORG-815]: comm_links.html#org815
