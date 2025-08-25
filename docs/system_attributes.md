# System Attributes

Select `View ➔ System ➔ System Attributes` menu item

A _system attribute_ is a configurable value which can be used to customize the
IRIS system.  Each attribute has a corresponding type: _string_, _boolean_,
_integer_ or _floating point_ number.

* _String_ values are limited to 64 characters or less
* _Boolean_ values must be specified as either `true` or `false`
* Numeric attributes can be constrained to a minimum and maximum bound,
  depending on the attribute.

When a _system attribute_ has been modified from its default value, it will be
displayed in **bold** on the **System Attributes** form.

Most attribute changes take effect immediately, but some require the IRIS client
or server to be restarted.

System Attribute                 | Description
---------------------------------|-----------------------------------------------------
`action_plan_alert_list`         | CSV list of users that trigger [action plan] activation alerts
`alert_clear_secs`               | Seconds to leave cleared alerts before removing them
`alert_sign_thresh_auto_meters`  | Threshold around alert area (in meters) for selecting DMS for automatic deployment of alerts
`alert_sign_thresh_opt_meters`   | Threshold around alert area (in meters) for selecting DMS for optional inclusion in alert deployments
`camera_autoplay`                | Automatically stream video when a [camera] is selected
`camera_blank_url`               | Location of PNG image to display for blankd [video monitors]
`camera_construction_url`        | Location of PNG image to display for [camera]s out due to construction
`camera_image_base_url`          | Base location of published [camera] images
`camera_kbd_panasonic_enable`    | Enable [camera] control from Panasonic CU-950 [keyboards]
`camera_latest_ptz_enable`       | Enable tooltip showing latest user who attempted to move a [camera]
`camera_num_blank`               | [Camera] number reserved for blanking [video monitors]
`camera_out_of_service_url`      | Location of PNG image to display for out of service [camera]s
`camera_playlist_dwell_sec`      | Dwell time for [camera] play lists
`camera_ptz_blind`               | Allow [camera] controls to be used even when not currently streaming to the IRIS client
`camera_stream_controls_enable`  | Enable [camera] stream control panel (stop, play, etc.)
`cap_save_enable`                | Enable saving CAP file for parsing errors
`clearguide_key`                 | ClearGuide customer key
`client_units_si`                | `true` for the client to display units using the International System of Units (SI) or `false` for customary (US) units
`database_version`               | IRIS database version; developer attribute.  **Do not change**.
`detector_auto_fail_enable`      | Enable [auto-fail] of traffic detectors with suspicious data
`detector_data_archive_enable`   | Enable archiving of detector data
`detector_occ_spike_secs`        | Duration for occupancy spikes to trigger "auto fail" (0=disabled)
`dms_comm_loss_enable`           | Enable blanking [DMS] after communication time out
`dms_message_tooltip_enable`     | Enable tooltip showing current posted DMS message and user
`dms_page_on_max_secs`           | Maximum selectable [DMS] page on time (seconds)
`dms_page_on_min_secs`           | Minimum selectable [DMS] page on time (seconds)
`dms_pixel_off_limit`            | Number of stuck-off pixels allowed in a [DMS] message
`dms_pixel_on_limit`             | Number of adjacent stuck-on pixels allowed in a [DMS] message
`dms_send_confirmation_enable`   | Enable a confirmation dialog box when the [DMS] Send button is pressed
`dms_update_font_table`          | Enable the updating of the [DMS] controller font table to match the font table in IRIS
`email_rate_limit_hours`         | Hours to wait before sending duplicate emails
`email_sender_server`            | Sender email address of IRIS server
`email_smtp_host`                | SMTP host for sending email
`gate_arm_alert_timeout_secs`    | Time to wait before sending gate arm alerts after comm failure
`gps_jitter_m`                   | Threshold (m) for GPS change to update device location
`help_trouble_ticket_enable`     | Enable help menu item for creating trouble tickets
`help_trouble_ticket_url`        | URL of Trac trouble ticket system
`incident_clear_advice_multi`    | Advice for [DMS] messages when incidents are cleared
`incident_clear_secs`            | Seconds to leave cleared incidents before removing them
`legacy_xml_config_enable`       | Enable saving system configuration as legacy XML
`legacy_xml_detector_enable`     | Enable saving detector data as legacy XML
`legacy_xml_incident_enable`     | Enable saving incidents as legacy XML
`legacy_xml_sign_message_enable` | Enable saving sign messages as legacy XML
`legacy_xml_weather_sensor_enable` | Enable saving weather sensors as legacy XML
`map_extent_name_initial`        | Name of map extent displayed when client starts
`map_icon_size_scale_max`        | Maximum map scale (meters per pixel) to use full icon size
`map_segment_max_meters`         | Maximum distance for connecting map segments
`meter_green_secs`               | [Ramp meter] green interval time
`meter_max_red_secs`             | [Ramp meter] maximum red interval time
`meter_min_red_secs`             | [Ramp meter] minimum red interval time
`meter_yellow_secs`              | [Ramp meter] yellow interval time
`msg_feed_verify`                | Require [DMS] messages from msg_feed to exist in message library
`route_max_legs`                 | Maximum number of corridors for route finding
`route_max_miles`                | Maximum distance (miles) for route finding
`rwis_auto_max_dist_miles`       | Maximum distance to auto-associate a weather sensor to a device
`rwis_flooding_1_mm`             | Accumulation threshold for flooding 1 condition
`rwis_flooding_2_mm`             | Accumulation threshold for flooding 2 condition
`rwis_obs_age_limit_secs`        | Weather sensor observations with an age greater than this value will be ignored
`rwis_slippery_1_percent`        | Friction threshold for slippery 1 condition
`rwis_slippery_2_degrees`        | Temperature threshold for slippery 2 condition
`rwis_slippery_3_percent`        | Friction threshold for slippery 3 condition
`rwis_visibility_1_m`            | Distance threshold for visibility 1 condition
`rwis_visibility_2_m`            | Distance threshold for visibility 2 condition
`rwis_windy_1_kph`               | Gust speed threshold for windy 1 condition
`rwis_windy_2_kph`               | Gust speed threshold for windy 2 condition
`speed_limit_default_mph`        | Default roadway speed limit
`speed_limit_max_mph`            | Maximum roadway speed limit
`speed_limit_min_mph`            | Minimum roadway speed limit
`subnet_target_1`                | Subnets for video source templates (1, 2, 3, etc)
`toll_density_alpha`             | α coefficient to convert density to [tolling] price
`toll_density_beta`              | β coefficient (exponent) to convert density to [tolling] price
`toll_min_price`                 | Minimum [tolling] price for one trip
`toll_max_price`                 | Default maximum [tolling] price for one trip
`travel_time_min_mph`            | Minimum overall speed for estimating [travel time]s
`vid_connect_autostart`          | Automatically start streaming when camera is selected
`vid_connect_fail_next_source`   | If video source fails, automatically try next source?
`vid_connect_fail_sec`           | Max seconds to wait for video source to connect
`vid_lost_timeout_sec`           | Max seconds of not streaming before flagging video lost
`vid_max_duration_sec`           | Max seconds of streaming before expiring video.  Zero = no limit
`vid_reconnect_auto`             | If video is lost, automatically try to reconnect?
`vid_reconnect_timeout_sec`      | Wait this long for a reconnect until retrying
`vsa_bottleneck_id_mph`          | Maximum speed to identify a bottleneck station for [VSA]
`vsa_control_threshold`          | Acceleration threshold for [VSA] control distance
`vsa_downstream_miles`           | Distance downstream of bottleneck to deploy [VSA]
`vsa_max_display_mph`            | Maximum speed to display for [VSA]
`vsa_min_display_mph`            | Minimum speed to display for [VSA]
`vsa_min_station_miles`          | Minimum distance between stations for [VSA]
`vsa_start_intervals`            | Number of intervals before activating a bottleneck for [VSA]
`vsa_start_threshold`            | Acceleration threshold to activate a bottleneck
`vsa_stop_threshold`             | Acceleration threshold to deactivate a bottleneck
`window_title`                   | Window title prefix when user is logged in
`work_request_url`               | URL to submit device work requests

Configuration of [events] is handled with **Event Configuration**.


[action plan]: action_plans.html
[auto-fail]: vehicle_detection.html#auto-fail
[camera]: cameras.html
[DMS]: dms.html
[events]: events.html
[keyboards]: cameras.html#camera-keyboards
[tolling]: tolling.html
[travel time]: travel_time.html
[video monitors]: video.html
[VSA]: vsa.html
