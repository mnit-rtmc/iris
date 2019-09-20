## Road Weather Information Systems

Select `View ➔ Weather Sensors` menu item

Road Weather Information Systems, or **RWIS** can sense data such as
precipitation rate, road surface temperature, wind speed, etc.  The [NTCIP]
and [Org-815] protocols can collect RWIS data.

Wind directions are recorded in degrees clockwise from due north.

Field                  | Description
-----------------------|------------------------------------
`air_temp`             | Instantaneous air temperature in ℃
`wet_bulb_temp`        | Instantaneous wet-bulb temperature in ℃
`dew_point_temp`       | Instantaneous dew point temperature in ℃
`min_air_temp`         | Minimum 24 hour air temperature in ℃
`max_air_temp`         | Maximum 24 hour air temperature in ℃
`avg_wind_dir`         | Two minute average wind direction
`avg_wind_speed`       | Two minute average wind speed in MPS
`spot_wind_dir`        | Spot wind direction
`spot_wind_speed`      | Spot wind speed in MPS
`gust_wind_dir`        | Ten minute maximum wind gust direction
`gust_wind_speed`      | Ten minute maximum wind gust speed in MPS
`relative_humidity`    | Relative humidity in percent
`precipitation_rate`   | Precipitation rate in mm/hr
`precipitation_1_hour` | One hour accumulated precipitation in mm
`precip_situation`     | Precipitation situation code
`atmospheric_pressure` | Atmospheric pressure in pascals
`visibility`           | Visibility in meters
`surface_temp`         | Surface temperature in ℃
`pavement_temp`        | Pavement temperature in ℃
`surface_status`       | Pavement surface status
`surface_freeze_point` | Pavement surface freeze temperature in ℃
`sub_surface_temp`     | Subsurface temperature in ℃


[NTCIP]: admin_guide.html#ntcip
[ORG-815]: admin_guide.html#org815
