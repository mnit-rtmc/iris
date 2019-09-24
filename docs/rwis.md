## Road Weather Information Systems

Select `View ➔ Weather Sensors` menu item

Road Weather Information Systems, or **RWIS** can sense data such as
precipitation rate, road surface temperature, wind speed, etc.  The [NTCIP]
and [Org-815] protocols can collect RWIS data.

Current settings are stored as JSONB in the `settings` column of the
`weather_sensor` table.  When new settings are stored, a time-stamped record is
added to the `weather_sensor_settings` table.

The settings fields are:

Field                    | Description
-------------------------|-------------------------------------------------------
`wind_sensor_height`     | Height in meters relative to `reference_elevation`
`temperature_sensor`     | Array of temperature settings objects
↳`height`                | Height in meters relative to `reference_elevation`
`reference_elevation`    | Reference elevation in meters above mean sea level
`pressure_sensor_height` | Height in meters relative to `reference_elevation`
`pavement_sensor`        | Array of pavement settings objects
↳`location`              | Sensor location description
↳`pavement_type`         | Pavement type description
↳`height`                | Pavement height in meters relative to `reference_elevation`
↳`exposure`              | Rough estimate of solar energy %
↳`sensor_type`           | Sensor type description
`sub_surface_sensor`     | Array of sub-surface sample objects
↳`location`              | Sensor location description
↳`sub_surface_type`      | Sub-surface type description
↳`sensor_depth`          | Depth in meters below surface

Current sample data is stored as JSONB in the `sample` column of the
`weather_sensor` table.  When a new sample is stored, a time-stamped record is
added to the `weather_sensor_sample` table.

Only collected data will be stored in the `sample` column.  The possible sample
fields are:

Field                    | Description
-------------------------|------------------------------------
`avg_wind_dir`           | Two minute average wind direction †
`avg_wind_speed`         | Two minute average wind speed in MPS
`spot_wind_dir`          | Spot wind direction †
`spot_wind_speed`        | Spot wind speed in MPS
`gust_wind_dir`          | Ten minute maximum wind gust direction †
`gust_wind_speed`        | Ten minute maximum wind gust speed in MPS
`temperature_sensor`     | Array of temperature sample objects
↳`air_temp`              | Instantaneous air temperature in ℃
`wet_bulb_temp`          | Instantaneous wet-bulb temperature in ℃
`dew_point_temp`         | Instantaneous dew point temperature in ℃
`max_air_temp`           | Maximum 24 hour air temperature in ℃
`min_air_temp`           | Minimum 24 hour air temperature in ℃
`relative_humidity`      | Relative humidity in percent
`precipitation_rate`     | Precipitation rate in mm/hr
`precipitation_1_hour`   | One hour accumulated precipitation in mm
`precipitation_3_hours`  | Three hour accumulated precipitation in mm
`precipitation_6_hours`  | Six hour accumulated precipitation in mm
`precipitation_12_hours` | Twelve hour accumulated precipitation in mm
`precipitation_24_hours` | Twenty-four hour accumulated precipitation in mm
`precip_situation`       | Precipitation situation description
`atmospheric_pressure`   | Atmospheric pressure in pascals
`visibility`             | Visibility in meters
`visibility_situation`   | Visibility situation description
`pavement_sensor`        | Array of pavement sample objects
↳`surface_status`        | Surface status description
↳`surface_temp`          | Surface temperature in ℃
↳`pavement_temp`         | Pavement temperature in ℃
↳`sensor_error`          | Sensor error description
↳`surface_water_depth`   | Surface water depth in meters
↳`salinity`              | Salinity in parts per 100,000 by weight
↳`surface_freeze_point`  | Surface freeze point in ℃
↳`black_ice_signal`      | Black ice signal description
`sub_surface_sensor`     | Array of sub-surface sample objects
↳`temp`                  | Sub-surface temperature in ℃
↳`moisture`              | Sub-surface moisture saturation %
↳`sensor_error`          | Sensor error description

_† Wind direction in degrees clockwise from due north_.


[NTCIP]: admin_guide.html#ntcip
[ORG-815]: admin_guide.html#org815
