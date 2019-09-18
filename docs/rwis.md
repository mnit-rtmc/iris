## Road Weather Information Systems

Select `View ➔ Weather Sensors` menu item

Road Weather Information Systems, or **RWIS** can sense data such as
precipitation rate, road surface temperature, wind speed, etc.  The [NTCIP]
and [Org-815] protocols can collect RWIS data.

Wind directions are recorded in degrees clockwise from due north.

Field               | Description
--------------------|------------------------------------
air_temp            | Instantaneous air temperature in ℃
wet_bulb_temp       | Instantaneous wet-bulb temperature in ℃
dew_point_temp      | Instantaneous dew point temperature in ℃
min_temp            | Minimum 24 hour air temperature in ℃
max_temp            | Maximum 24 hour air temperature in ℃
wind_spot_speed     | Spot wind speed in KPH
wind_spot_dir       | Spot wind direction
wind_avg_speed      | Two minute average wind speed in KPH
wind_avg_dir        | Two minute average wind direction
wind_gust_speed     | Ten minute maximum wind gust speed in KPH
wind_gust_dir       | Ten minute maximum wind gust direction
precip_rate         | Precipitation rate in mm/hr
precip_situation    | Precipitation situation code
precip_one_hour     | One hour accumulated precipitation in mm
visibility          | Visibility in meters
humidity            | Relative humidity in percent
pressure            | Atmospheric pressure in pascals
pvmt_surf_temp      | Pavement surface temperature in ℃
surf_temp           | Surface temperature in ℃
pvmt_surf_status    | Pavement surface status
surf_freeze_temp    | Pavement surface freeze temperature in ℃
sub_surf_temp       | Subsurface temperature in ℃


[NTCIP]: admin_guide.html#ntcip
[ORG-815]: admin_guide.html#org815
