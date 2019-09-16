## Road Weather Information Systems

Select `View ➔ Weather Sensors` menu item

Road Weather Information Systems, or **RWIS** can sense data such as
precipitation rate, road surface temperature, wind speed, etc.  The [NTCIP]
and [Org-815] protocols can collect RWIS data.

Field               | Description
--------------------|-----------------------------
air_temp            | Air temperature in ℃
min_temp            | Minimum temperature in ℃
max_temp            | Maximum temperature in ℃
dew_point_temp      | Dew point temperature in ℃
wind_speed          | Wind speed in KPH
wind_dir            | Average wind direction in degrees clockwise from due north
max_wind_gust_speed | Maximum wind gust speed in KPH
max_wind_gust_dir   | Wind gust direction in degrees clockwise from due north
spot_wind_speed     | Spot wind speed in KPH
spot_wind_dir       | Spot wind direction in degrees clockwise from due north
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
