# Alerts - Weather / Emergency

IRIS can poll weather and other emergency alerts from feeds using the Common
Alerting Protocol [CAP], and automatically post messages on Dynamic Message
Signs (DMS).

In the US, the system operated by the Federal Emergency Management Agency (FEMA)
is called Integrated Public Alert and Warning System [IPAWS].  Use of this
system requires access to the IPAWS Open Platform for Emergency Networks
(IPAWS-OPEN) which must be approved by FEMA.  This system was largely designed
to post messages relating to weather alerts issued by the National Weather
Service, however it is possible to use the system to post information for other
alert types in IRIS by creating the proper [alert configuration].

IPAWS collects a wide variety of public alerts and warnings that originate from
over 1,500 alerting authorities.  As only a subset of these will typically be
of interest to a particular organization, IRIS provides a detailed framework
for configuring the response to each alert.

When configured properly, the system will perform the following functions:

1. Poll a feed for alerts and parse the Common Alerting Protocol (CAP) XML alert
   information.
2. Process alerts with event types that have been configured in the system.
3. Identify DMS within the alert area.
4. Generate DMS messages based on predefined message templates.
5. Post messages to signs automatically or after operator approval.
6. Update messages on signs when alerts become active, expire, or when changes
   are issued by the alerting authority automatically or with minimal operator
   interaction.
7. Automatically remove messages from signs after an alert expires.

The system can operate in either automatic mode, where alert messages are
posted with no human interaction, or in approval mode, where operator approval
is required before messages are posted or updated.  The alert system is managed
from the "Alert" tab and other dialogs and system attributes.

## Obtaining Access

Access to IPAWS-OPEN is managed by FEMA, requiring approval by the agency and
a signed Memorandum of Understanding (MOU). Each organization running IRIS must
obtain their own authorization and may not share it with other organizations.

The authorization process can be initiated by sending a request to
[IPAWS@FEMA.DHS.GOV](mailto:IPAWS@FEMA.DHS.GOV), after which the IPAWS Program
Office will send the necessary forms for completion. After the completed forms
have been returned, an MOU will be provided for signature. Once the signed MOU
has been executed, the URL required to access IPAWS-OPEN will be provided.

## Setting Up CAP Interface

The interface with IPAWS-OPEN is configured via a [comm link].  This `comm link`
must use the `CAP` protocol and contain the URL for a valid CAP feed.  A polling
period of 60 seconds is recommended, but you may use longer periods if desired.
Also, set `idle_disconnect_sec` in the comm configuration to 10 seconds.

A CAP `comm link` requires a [controller] in `ACTIVE` condition to operate.
With polling enabled and the controller in active condition, IRIS will poll the
URL provided at the configured polling period.  Each polling cycle will check
for new or updated alerts, parse and process them to determine if they are
relevant and, if appropriate, create messages for deployment.  This processing
is controlled by [alert configurations](#alert-configurations).

## Forecast Zones

Alerts from the National Weather Service use special codes to define GIS
forecast zones.  This information can be obtained in shapefile format from NWS
and loaded into the `tms` database.

To load geometry data, download the latest [Public Forecast Zones] shapefile
to the IRIS server and unzip it.  To import the file, execute the following
command on the
server:
```
shp2pgsql -G <nws_shapefile>.shp cap.nws_zones | psql tms
```

NOTE: Alert areas may change (NWS updates the file roughly every six months), so
it is important to keep them updated.  Administrators should keep records of
when this information was last updated and maintain the latest information in
the database.

## Alert Configurations

Select the `View ➔ Alerts ➔ Alert Configurations` menu item.

This dialog displays the alert configurations and allows creating new ones.
Alert configurations link specific alert properties with one or more sign groups
and associated quick message templates.

To create a new alert configuration, press the "Create" button.  After the new
configuration appears in the list, select it and assign the desired alert
properties to match.  Also, select one or more sign groups and quick messages.

### Event Types

Each alert will contain an "Event" field to describe the subject of the alert.
Some of the more common events that may be observed include:

 - BZW, Blizzard Warning
 - FLW, Flood Warning
 - WWY, Winter Weather Advisory
 - WSW, Winter Storm Warning
 - SVW, Severe Thunderstorm Warning
 - TOW, Tornado Warning
 - WIY, Wind Advisory

A list of possible events that may be encountered is available on the alert
config panel.

### Sign Groups

Signs that are eligible for inclusion in an alert deployment should be
collected into a sign group. When an alert of a recognized event type is
received, IRIS will use any sign groups associated with that event (based on
the existing alert configurations) to search for signs in the group that are
within or near the area defined in the alert CAP message. A single event type
may be used in more than one alert configuration and associated with more than
one sign group, allowing the use of different message templates for each sign
group.

Signs that are inside the alert area will be automatically used to display
messages describing the alert, unless an operator decides to exclude them.
If the `alert_sign_thresh_auto_meters` system attribute is set to a non-zero
value, signs within `alert_sign_thresh_auto_meters` meters of the alert area
will also be included.

If the `alert_sign_thresh_opt_meters` system attribute is set to a non-zero
value, signs within the sum of `alert_sign_thresh_auto_meters` and
`alert_sign_thresh_opt_meters` will be suggested for inclusion in the alert
deployment when reviewed in the deployment dialog.

### Message Templates

Messages will be automatically generated for each alert based on a predefined
message template, stored as a quick message. These message templates support
the use of IRIS DMS Action Tags to allow dynamically displaying information
from the alert CAP message in the message displayed on the DMS.

Message templates can be created using the [WYSIWYG editor]. To create a
message template, use the message selector to create a new message for the sign
group for use in the alert deployment. In the message editor, enter any static
elements of the message as text. Then use `MULTI Tag` mode to add CAP Time, CAP
Response Type, or CAP Urgency tags as needed. In the WYSIWYG rendering, these
tags will be displayed as rectangular boxes to indicate the maximum width of
the text that will be placed at that location in the message.

#### CAP Time Tag

The `[captime...]` tag will dynamically substitute a time-dependent sequence of
text or time field from an alert CAP message into a message. This tag has three
parameters, separated by commas:
 - Pre-alert text
 - Alert-active text
 - Post-alert text

Each of these parameters can include a set of curly braces (`{}`) that will be
substituted with either the alert start or end time. By default the time will
be formatted in `h a` format (e.g. `2 PM`), however you may specify a Java
[DateTimeFormatter] pattern inside the curly braces to change this (e.g.
`{h:mm a}`).

Before the alert begins, the pre-alert text will be substituted in place of the
tag. If a time substitution field (curly braces) is included in the text, the
alert **start** time will be substituted in its place.

After the alert has started but before it has expired, the alert-active text
will be substituted in place of the tag. If a time substitution field (curly
braces) is included in the text, the alert **end** time will be substituted
in its place.

After the alert has expired, the post-alert text will be substituted in place
of the tag. If a time substitution field (curly braces) is included in the
text, the alert **end** time will be substituted in its place.

Multiple `[captime...]` tags may be included in a message to allow time-
dependent text to be spread across multiple lines or pages.

##### Example

Consider the following tag:

```
[captimeSTARTING AT {},IN EFFECT UNTIL {},ALL CLEAR]
```

If this tag is used in a message for an alert that starts at 2 AM and ends at
1 PM, the message posted to the sign will substitute text in place of that tag
as follows:

Alert Phase             | Message Text
------------------------|--------------------------------------------
Before alert start time | STARTING AT 2 AM
During alert time       | IN EFFECT UNTIL 1 PM
After alert end time    | ALL CLEAR

### Pre- and Post-Alert Times

Messages may be posted to DMS before an alert has started or after an alert has
ended.  This is controlled via the "Pre-Alert" and "Post-Alert" times in an
alert configuration, respectively, which are specified in hours.  For a new
configuration, the default pre-alert time is 6 hours, and the default post-
alert time is 0 hours (meaning no message will be posted after the alert
expires).  These can be changed for each alert configuration, and can also be
changed for each alert from the Alert Tab.

### Auto Deploy

An alert configuration can be flagged to automatically deploy when it is matched
with an incoming alert.  In this case, operator approval is not required, and
signs will be deployed with no intervention.

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
an entry to the alert list, with a corresponding sign group.  If the
configuration is not marked "auto deploy", its state will be "Pending".

Operators may adjust the signs that are included in the deployment by checking
or unchecking the box next to each DMS in the list.  This list is limited to
signs in or near the alert area (with proximity determined by the
`alert_sign_thresh_auto_meters` and `alert_sign_thresh_opt_meters` system
attributes).  Operators must have the `alert_deploy` capability to approve or
edit alert deployments.

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

The "Attention" button will continue to blink until the alert has changes from
pending mode.

## Testing the System

Because the alert system requires alert CAP messages in order to function, it
can be challenging to test.  To address this, IRIS provides a testing mechanism
that allows testing the system with a mocked-up CAP message.  To use this, first
a CAP XML message must be crafted with:

 - An event type configured in IRIS (which can be a custom "Test" event)
 - An alert area that contains signs suitable for testing
 - Alert start and end times corresponding to a suitable testing period

To do this, it is best to start with a real CAP message taken from IPAWS-OPEN
(e.g. one that is old or targets a different area) and replace the values with
ones suitable for testing.  This must be done with care to ensure the [CAP]
standard is followed and the message can be parsed.

After a CAP message is created, it can be fed into IRIS in one of two ways.

1. The file can be hosted on an arbitrary HTTPS server (including the IRIS
server itself if HTTPS is supported).  IRIS can then be configured with a comm
link that points to this file on that server.
2. The file can be placed in `/var/log/iris/cap_test.xml` on the IRIS server
itself, and the controller of an existing CAP `comm link` can be put into the
`TESTING` condition.

In either case IRIS will read this file and process the alert as if it were a
real alert.  Note that in a production environment this may activate real signs,
so care must be taken to ensure the testing is done in a controlled manner.


[alert configurations]: #alert_configurations
[comm link]: comm_links.html
[controller]: controllers.html
[DateTimeFormatter]: https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html
[CAP]: http://docs.oasis-open.org/emergency/cap/v1.2/CAP-v1.2.html
[IPAWS]: https://www.fema.gov/emergency-managers/practitioners/integrated-public-alert-warning-system
[Public Forecast Zones]: https://www.weather.gov/gis/PublicZones
