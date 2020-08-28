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
`action_plan_event_purge_days`   | Number of days after which [action plan] events will be purged
`alarm_event_purge_days`         | Number of days after which [alarm] events will be purged
`beacon_event_purge_days`        | Number of days after which [beacon] events will be purged
`camera_autoplay`                | Automatically stream video when a [camera] is selected
`camera_blank_url`               | Location of PNG image to display for blankd [video monitors]
`camera_construction_url`        | Location of PNG image to display for [camera]s out due to construction
`camera_image_base_url`          | Base location of published [camera] images
`camera_kbd_panasonic_enable`    | Enable [camera] control from Panasonic CU-950 [keyboards]
`camera_num_blank`               | [Camera] number reserved for blanking [video monitors]
`camera_out_of_service_url`      | Location of PNG image to display for out of service [camera]s
`camera_sequence_dwell_sec`      | Dwell time for [camera] sequences
`camera_preset_store_enable`     | Enable preset-store button in the [camera] preset control panel
`camera_ptz_blind`               | Allow [camera] controls to be used even when not currently streaming to the IRIS client
`camera_stream_controls_enable`  | Enable [camera] stream control panel (stop, play, etc.)
`camera_switch_event_purge_days` | Number of days after which [camera] switch events will be purged
`camera_video_event_purge_days`  | Number of days after which [camera] video events will be purged
`camera_wiper_precip_mm_hr`      | Precipitation rate to activate [camera] wipers (mm/hour)
`client_event_purge_days`        | Number of days after which client events will be purged
`client_units_si`                | `true` for the client to display units using the International System of Units (SI) or `false` for customary (US) units
`comm_event_enable`              | Enable logging communication events to database
`comm_event_purge_days`          | Number of days after which communication events will be purged
`database_version`               | IRIS database version; developer attribute.  **Do not change**.
`detector_auto_fail_enable`      | Enable "auto fail" of traffic detectors with suspicious data
`detector_event_purge_days`      | Number of days after which detector events will be purged
`dict_allowed_scheme`            | Spell checking scheme for allowed words: 0=none 1=recommend replacement words, 2=messages can only contain allowed words
`dict_banned_scheme`             | Spell checking scheme for banned words: 0=none, 1=recommend to not use banned words, 2=messages can't contain banned words
`dms_brightness_enable`          | Display [DMS] brightness information
`dms_comm_loss_enable`           | Enable blanking [DMS] after communication time out
`dms_composer_edit_mode`         | [DMS] message line combo box mode (0=not editable, 1=always editable, 2=editable after key press if identity sign group exists)
`dms_default_justification_line` | Default [DMS] line justification (2=LEFT, 3=CENTER, 4=RIGHT, 5=FULL)
`dms_default_justification_page` | Default [DMS] page justification (2=TOP, 3=MIDDLE, 4=BOTTOM)
`dms_duration_enable`            | Enable widgets to select [DMS] message duration
`dms_font_selection_enable`      | Allow font to be selected for [DMS] messages
`dms_gps_jitter_m`               | Threshold (m) for GPS change to update [DMS] location
`dms_high_temp_cutoff`           | Temperature at which [DMS] should shut off
`dms_lamp_test_timeout_secs`     | Time to wait for [DMS] lamp test to complete
`dms_manufacturer_enable`        | Enable manufacturer-specific [DMS] widgets
`dms_max_lines`                  | Maximum lines per page on a [DMS]
`dms_message_min_pages`          | Minimum number of pages to allow on [DMS] message interface
`dms_page_off_default_secs`      | Default [DMS] page off time
`dms_page_on_default_secs`       | Default [DMS] page on time
`dms_page_on_max_secs`           | Maximum selectable [DMS] page on time (seconds)
`dms_page_on_min_secs`           | Minimum selectable [DMS] page on time (seconds)
`dms_page_on_selection_enable`   | Allow [DMS] page on time to be selected
`dms_pixel_off_limit`            | Number of stuck-off pixels allowed in a [DMS] message
`dms_pixel_on_limit`             | Number of adjacent stuck-on pixels allowed in a [DMS] message
`dms_pixel_maint_threshold`      | Number of pixel failures before requiring maintenance
`dms_pixel_status_enable`        | Enable reporting of [DMS] pixel status errors
`dms_pixel_test_timeout_secs`    | Time to wait for [DMS] pixel test to complete
`dms_querymsg_enable`            | Enable widgets to query [DMS] message
`dms_quickmsg_store_enable`      | Enable button to store composed [DMS] message as a quick-message
`dms_render_size`                | Specifies Chooser icon size: 0=Large, 1=Medium, 2=Small, 3=Auto
`dms_reset_enable`               | Enable button to reset [DMS]
`dms_send_confirmation_enable`   | Enable a confirmation dialog box when the [DMS] Send button is pressed
`dms_update_font_table`          | Enable the updating of the [DMS] controller font table to match the font table in IRIS
`dmsxml_reinit_detect`           | Enable [DMS] reinitialization detection for DMSXML controllers
`email_rate_limit_hours`         | Hours to wait before sending duplicate emails
`email_recipient_action_plan`    | Recipient of [action plan] emails
`email_recipient_aws`            | Recipient of AWS emails
`email_recipient_dmsxml_reinit`  | Recipient of [DMS] reinit detection emails
`email_recipient_gate_arm`       | Recipient of gate arm alert emails
`email_sender_server`            | Sender email address of IRIS server
`email_smtp_host`                | SMTP host for sending email
`gate_arm_alert_timeout_secs`    | Time to wait before sending gate arm alerts after comm failure
`gate_arm_event_purge_days`      | Number of days after which gate arm events will be purged
`help_trouble_ticket_enable`     | Enable help menu item for creating trouble tickets
`help_trouble_ticket_url`        | URL of Trac trouble ticket system
`incident_clear_advice_multi`    | Advice for [DMS] messages when incidents are cleared
`incident_clear_secs`            | Seconds to leave cleared incidents before removing them
`map_extent_name_initial`        | Name of map extent displayed when client starts
`map_icon_size_scale_max`        | Maximum map scale (meters per pixel) to use full icon size
`map_segment_max_meters`         | Maximum distance for connecting map segments
`meter_event_enable`             | Enable logging [ramp meter] events to database
`meter_event_purge_days`         | Number of days after which [ramp meter] events will be purged
`meter_green_secs`               | [Ramp meter] green interval time
`meter_max_red_secs`             | [Ramp meter] maximum red interval time
`meter_min_red_secs`             | [Ramp meter] minimum red interval time
`meter_yellow_secs`              | [Ramp meter] yellow interval time
`msg_feed_verify`                | Require [DMS] messages from msg_feed to exist in message library
`operation_retry_threshold`      | Number of times a controller operation is retried if not already failed
`price_message_event_purge_days` | Number of days after which price message events will be purged
`route_max_legs`                 | Maximum number of corridors for route finding
`route_max_miles`                | Maximum distance (miles) for route finding
`rwis_high_wind_speed_kph`       | Wind speed (kph) greater than this value triggers the high wind condition
`rwis_low_visibility_distance_m` | Visibility (meters) less than this value triggers the low visibility condition
`rwis_obs_age_limit_secs`        | Weather sensor observations with an age (in secs) greater than this value will be ignored
`rwis_max_valid_wind_speed_kph`  | Maximum valid wind speed (kph).  Use 0 to indicate no maximum
`sample_archive_enable`          | Enable archiving of sample data
`sign_event_purge_days`          | Number of days after which sign events will be purged
`speed_limit_default_mph`        | Default roadway speed limit
`speed_limit_max_mph`            | Maximum roadway speed limit
`speed_limit_min_mph`            | Minimum roadway speed limit
`tag_read_event_purge_days`      | Number of days after which tag read events will be purged
`toll_density_alpha`             | α coefficient to convert density to [tolling] price
`toll_density_beta`              | β coefficient (exponent) to convert density to [tolling] price
`toll_min_price`                 | Minimum [tolling] price for one trip
`toll_max_price`                 | Default maximum [tolling] price for one trip
`travel_time_min_mph`            | Minimum overall speed for estimating [travel time]s
`uptime_log_enable`              | Enable logging of system uptime
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


[action plan]: action_plans.html
[alarm]: alarms.html
[beacon]: beacons.html
[camera]: cameras.html
[DMS]: dms.html
[keyboards]: cameras.html#camera-keyboards
[ramp meter]: ramp_meters.html
[tolling]: tolling.html
[travel time]: travel_time.html
[video monitors]: video.html
[VSA]: vsa.html
