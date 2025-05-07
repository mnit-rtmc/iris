# Alerts and Warnings

IRIS can poll weather and other emergency alerts from feeds using the Common
Alerting Protocol [CAP], and post messages on Dynamic Message Signs (DMS).

When configured properly, the system will perform the following functions:

1. Poll a feed and parse the CAP alerts.
2. Process alerts with configured event types.
3. Identify DMS within the alert area.
4. Post messages to signs.
5. Update messages on signs when alerts become active, expire, or when changes
   are issued by the alerting authority.
6. Remove messages from signs after an alert expires.

The system can operate in either automatic mode, where alert messages are
posted with no human interaction, or in approval mode, where an operator must
confirm messages before they are posted or updated.  The alert system is managed
from the "Alert" tab and other dialogs and system attributes.

## National Weather Service

In the US, the National Weather Service operates an open API providing weather
alerts.  The URI is `https://api.weather.gov/alerts/active?area={XX}`, with
`{XX}` being the 2-letter state ID.

## IPAWS

An alternative to the NWS is the Integrated Public Alert and Warning System
[IPAWS].  This feed is more difficult to set up, but provides additional alert
types not related to weather — a wide variety of public alerts and warnings
that originate from over 1,500 alerting authorities.

The IPAWS Open Platform for Emergency Networks (IPAWS-OPEN) is operated by the
Federal Emergency Management Agency (FEMA).  Obtaining access requires approval
by the agency and a signed Memorandum of Understanding (MOU).  Each organization
running IRIS must obtain their own authorization and may not share it with other
organizations.

The authorization process can be initiated by sending a request to
[ipaws@fema.dhs.gov](mailto:IPAWS@FEMA.DHS.GOV), after which the IPAWS Program
Office will send the necessary forms to sign up.

## CAP Feed

A CAP feed is configured via a [comm link].  The [comm config] must use either
the `CAP-NWS` or `CAP-IPAWS` protocol.  The recommended `timeout` is 8 seconds
and `idle disconnect` time 10 seconds.  A polling period of 60 seconds is also
recommended, but you may use longer periods if desired.

The `comm link` must contain the URL for a valid CAP feed.  For IPAWS-OPEN, the
`path` ends with `recent/`, followed by a date/time stamp.  IRIS will add the
date/time to each request if the provided URL ends with `/`.  The URL must also
contain a query string with a your pin (*e.g.* `?pin=ABC123`).

A CAP `comm link` requires a [controller] in `ACTIVE` condition to operate.
With polling enabled and the controller in active condition, IRIS will poll the
URL provided at the configured polling period.  Each polling cycle will check
for new or updated alerts, parse and process them to determine if they are
relevant and, if appropriate, create messages for deployment.  This processing
is controlled by [alert configuration](#alert-configuration)s.

## Geocoding: Forecast Zones and FIPS Codes

Alerts from the National Weather Service use special codes to define GIS
forecast zones.  This information can be obtained in shapefile format from NWS
and loaded into the `tms` database.

To load geometry data, download the latest [Public Forecast Zones] shapefile
to the IRIS server and unzip it.  To import the file, execute the following
command on the server:
```
shp2pgsql -G z_{date}.shp cap.nws_zones | psql tms
psql tms -c 'GRANT SELECT ON cap.nws_zones TO PUBLIC'
psql tms -c 'ALTER TABLE cap.nws_zones OWNER TO tms'
```

NOTE: Alert areas may change (NWS updates the file roughly every six months), so
it is important to keep them updated.  Administrators should keep records of
when this information was last updated and maintain the latest information in
the database.

As an alternative to forecast zones, FIPS county codes can be used.  For this,
download the latest [Counties] shapefile to the IRIS server and unzip it.
Then, execute the following command on the server:
```
shp2pgsql -G c_{date}.shp cap.nws_counties | psql tms
psql tms -c 'GRANT SELECT ON cap.nws_counties TO PUBLIC'
psql tms -c 'ALTER TABLE cap.nws_counties OWNER TO tms'
```

## Alert Configuration

As only a subset of alerts will typically be of interest to a particular
organization, IRIS provides a detailed framework for configuring the response to
each alert.

Select the `View ➔ Alerts ➔ Alert Configurations` menu item.

This dialog displays the alert configurations and allows creating new ones.
A configuration links specific alert properties with a DMS [hashtag] and one or
more alert messages.

To create a new alert configuration, press the `Create` button.  After the new
configuration appears in the list, select it and assign the desired properties
to match.  Also, select a DMS [hashtag] and create one or more messages.

### Event Types

Each alert will contain an `event` field to describe the subject of the alert.
Some of the more common events that may be observed include:

 - Blizzard Warning (BZW)
 - Flood Warning (FLW)
 - Tornado Warning (TOW)
 - Severe Thunderstorm Warning (SVW)
 - Wind Chill Warning (WCW)
 - Winter Storm Warning (WSW)
 - Winter Weather Advisory (WWY)

### Selection

Alerts can be selected in a configuration based on four parameters:
**responseType**, **urgency**, **severity** and **certainty**.  An alert will
only be selected if all four of these parameters match the configuration.

* Response Types: `Shelter`, `Evacuate`, `Prepare`, `Execute`, `Avoid`, `Monitor`, `All Clear`, `None`
* Urgency: `Unknown`, `Past`, `Future`, `Expected`, `Immediate`
* Severity: `Unknown`, `Minor`, `Moderate`, `Severe`, `Extreme`
* Certainty: `Unknown`, `Unlikely`, `Possible`, `Likely`, `Observed`

### Auto Deploy

An alert configuration can be flagged to automatically deploy when it is matched
with an incoming alert.  In this case, operator approval is not required, and
signs will be deployed with no intervention.

### Hours Before and Hours After

Additional messages may be posted `BEFORE` an alert has started or `AFTER` it
has ended.  The duration of these periods is controlled via the "Hours Before"
and "Hours After" times, respectively.  A value of 0 will disable that period
for the configuration.

### DMS Hashtags

Signs that are eligible for inclusion in an alert configuration should be
tagged with a specific DMS [hashtag].  When an alert matches the configuration,
only signs with that hashtag will be considered when searching the area defined
by the alert CAP message.

Signs that are inside the alert area will be automatically used to display
messages describing the alert, unless an operator decides to exclude them.
If the `alert_sign_thresh_auto_meters` system attribute is set to a non-zero
value, signs within `alert_sign_thresh_auto_meters` meters of the alert area
will also be included.

If the `alert_sign_thresh_opt_meters` system attribute is set to a non-zero
value, signs within the sum of `alert_sign_thresh_auto_meters` and
`alert_sign_thresh_opt_meters` will be suggested for inclusion in the alert
deployment when reviewed in the deployment dialog.

### Alert Messages

An alert message defines an *alert period* and a [message pattern].  The period
can be `BEFORE`, `DURING` or `AFTER`, and selects the time relative to the
start and end of the alert.  The message pattern will be displayed on signs
with a matching **restrict** [hashtag] during the associated period.

A message pattern can contain DMS [action tags], since alerts are deployed as
action plans.  Specifically, the [time action tag] is useful for displaying the
the alert start or end time as part of a message.

## Operating the System

The system is managed in the Alert tab of the IRIS client.  This tab lists all
relevant alert deployments (i.e. those with matching configurations and DMS
in the alert area) that have been processed by the IRIS server.  Alert
deployments are grouped into the following styles:

Style     | Description
----------|--------------------------------------------------------------------
Pending   | Deployment is pending operator approval
Active    | Alert action plan is scheduled and may be deployed
Cleared   | Alert is in the past or cleared by an operator
All       | All recent alert deployments

When an alert deployment is selected from the list, the tab will populate
the area below it with information about the alert and the DMS included in the
deployment.  When an alert is selected, the map will display a polygon showing
the alert area, along with the signs that are included in the alert.  If the
alert area is not visible, you may right click on the alert deployment and click
"Zoom to Alert Area" to center the map on the alert area.

The alert tab also functions as the approval/edit dialog.  This allows operators
to approve alerts for deployment in addition to editing active deployments.

When a new alert is received that is eligible for a message deployment, the
IRIS server will process it to determine the signs for inclusion and the
message(s) that will be displayed.  Each matching alert configuration will add
an entry to the alert list, with a corresponding DMS [hashtag].  If the
configuration is not marked "auto deploy", its state will be "Pending".

Operators may adjust the signs that are included in the deployment by checking
or unchecking the box next to each DMS in the list.  This list is limited to
signs in or near the alert area (with proximity determined by the
`alert_sign_thresh_auto_meters` and `alert_sign_thresh_opt_meters` system
attributes).  Operators must have permissions to approve or edit alert
deployments.

If the alert configuration is flagged as "auto deploy", alert deployments will
automatically be sent to signs with no human interaction.

Active alert deployments can edited by selecting the alert in the list and
clicking on the "Included" check box next to a DMS.

Operators may also cancel any deployment by clicking the "Cleared" check box.
Once cleared, alerts will be found in the "Cleared" style until it is past the
the post-alert time.  Cleared alerts can be redeployed by unchecking the
"Cleared" box.

### Attention Panel

The system includes a notification feature to facilitate operation in approval
mode and keep operators aware of alert deployments in automatic mode.  When the
system encounters alerts that are eligible for deployment, IRIS clients will
notify users via the flashing yellow "Attention" button in the lower-right
corner of the interface.  Clicking this button will open a menu containing
pending alerts, and selecting one will open the Alert tab and select the
corresponding alert.

The "Attention" button will continue to blink until the alert has changed from
pending mode.

## Testing the System

Because the alert system requires alert CAP messages in order to function, it
can be challenging to test.  To address this, IRIS provides a testing mechanism
that allows testing the system with a mocked-up CAP message.  To use this, first
a CAP message must be crafted with:

 - An event type configured in IRIS (which can be a custom "Test" event)
 - An alert area that contains signs suitable for testing
 - Alert start and end times corresponding to a suitable testing period

To do this, it is best to start with a real CAP message taken from IPAWS-OPEN
(e.g. one that is old or targets a different area) and replace the values with
ones suitable for testing.  This must be done with care to ensure the [CAP]
standard is followed and the message can be parsed.

After a CAP message is created, it can be fed into IRIS using a `file` comm
link.  Point the URI to a file on the IRIS server, e.g.
`file:///var/log/iris/cap_test.geojson`.

IRIS will read this file and process the alert as if it were a real alert.
Note that in a production environment this may activate real signs, so care
must be taken to ensure the testing is done in a controlled manner.


[action tags]: action_plans.html#action-tags
[alert configuration]: #alert-configuration
[CAP]: http://docs.oasis-open.org/emergency/cap/v1.2/CAP-v1.2.html
[comm config]: comm_config.html
[comm link]: comm_links.html
[controller]: controllers.html
[Counties]: https://www.weather.gov/gis/Counties
[hashtag]: hashtags.html
[IPAWS]: https://www.fema.gov/emergency-managers/practitioners/integrated-public-alert-warning-system
[message pattern]: message_patterns.html
[Public Forecast Zones]: https://www.weather.gov/gis/PublicZones
[time action tag]: action_plans.html#time-action-tag
