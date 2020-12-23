# Integrated Public Alert and Warning System (IPAWS) Integration

IRIS can receive weather and other emergency alerts from the
[Integrated Public Alert and Warning System (IPAWS)](https://www.fema.gov/emergency-managers/practitioners/integrated-public-alert-warning-system)
and automatically post messages on Dynamic Message Signs (DMS). Use of this
system requires access to the IPAWS Open Platform for Emergency Networks
(IPAWS-OPEN) which must be approved by the Federal Emergency Management Agency
(FEMA). This system was largely designed to post messages relating to weather
alerts issued by the National Weather Service (NWS), however it is possible
to use the system to post information for other alert types in IRIS by creating
the proper [alert configuration].

When configured properly, the IRIS IPAWS system will perform the following
functions:

1. Poll IPAWS-OPEN for alerts and parse the Common Alerting Protocol (CAP)
XML alert information.
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
is required before messages are posted or updated. The alert system is managed
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

## Setting Up IPAWS Interface

The interface with IPAWS-OPEN is configured via a [comm link]. This comm_link
must use the `IPAWS Alerts` protocol and contain a valid IPAWS-OPEN URL
provided by FEMA. A polling period of 60 seconds is recommended, but you may
use longer polling periods if desired.

An IPAWS comm_link requires a [controller] in `ACTIVE` condition to operate.
With polling enabled and the controller in active condition, IRIS will poll
the IPAWS-OPEN URL provided at the configured polling period. Each polling
cycle will check for new or updated alerts and parse them. 30 seconds after
each polling cycle, the alerts will be processed to determine if they are
relevant and, if appropriate, create messages for deployment. This processing
is controlled by [Alert Configurations].

## Alert Configurations

IPAWS collects a wide variety of public alerts and warnings that originate from
over 1,500 alerting authorities. As only a subset of these will typically be
of interest to a particular organization, IRIS provides a detailed framework
for configuring the response to each alert.

Alert configurations can be accessed from the `View ➔ Alerts ➔ Alert Configurations`
menu item. This dialog displays the list of existing alert configurations and
allows creating new ones. Alert configurations link a specific alert "Event"
with one or more sign groups that may be used for posting messages. Each alert
event and sign group requires a message template stored as a quick message.

To create a new alert configuration, enter an event type in the text box of the
Alert Configurations dialog and press "Create." The new configuration will then
appear in the list. Select a sign group and quick message for the new
configuration using the drop downs. If desired, you may change the [Pre- or
Post-Alert time values](#pre--and-post-alert-times).

### Event Types

Each alert obtained from IPAWS will contain an "Event" field that uses a
phrase to describe the subject of the alert. Some of the more common events
that may be observed include:

 - Flood Warning
 - Winter Weather Advisory
 - Winter Storm Warning
 - Severe Thunderstorm Warning
 - Blizzard Warning
 - Tornado Warning
 - Wind Advisory

A list of possible events that may be encountered is available from the
[National Weather Service](https://alerts.weather.gov/cap/product_list.txt).
Note that this list may be updated periodically, potentially requiring changes
to alert configurations in IRIS. Events used for alert configurations must
match those found in alert CAP XMLs exactly.

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
If the `ipaws_sign_thresh_auto_meters` system attribute is set to a non-zero
value, signs within `ipaws_sign_thresh_auto_meters` meters of the alert area
will also be included.

If the `ipaws_sign_thresh_opt_meters` system attribute is set to a non-zero
value, signs within the sum of `ipaws_sign_thresh_auto_meters` and
`ipaws_sign_thresh_opt_meters` will be suggested for inclusion in the alert
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
be formatted in `h a` format (e.g. `2 PM`), however you may specify a
[Java DateTimeFormatter pattern](https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html)
inside the curly braces to change this (e.g. `{h:mm a}`).

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

Alert Phase                       | Message Text
----------------------------------|--------------------------------------------
Before alert start time           | STARTING AT 2 AM
Between alert start and end times | IN EFFECT UNTIL 1 PM
After alert end time              | ALL CLEAR

#### CAP Response Type Tag

The `[capresponse...]` tag allows substituting a value corresponding to the
response type field sent in the alert CAP message. This tag allows specifying
zero or more response type values that will be used to filter which response
types will trigger a substitution corresponding to the alert event.

Response type substitution fields must be configured in the
`View ➔ Alerts ➔ CAP Response Type Substitutions` dialog. To create one, enter
an event type into the text box in this dialog and press Create. Then select a
response type from the drop down and enter a MULTI string that will be
substituted for any `[capresponse...]` tag encountered in a message template
for alerts of that event type.

If no parameters are provided, the tag will use the response type substitution
configurations for any response type encountered in the alert CAP message.
You may optionally include one or more response type values (separated by
commas in the MULTI tag) to limit which response type values are substituted.

##### Example

Consider the following tag:

```
[capresponseShelter,Prepare]
```

If this tag is used in a template and an alert with the event "Winter Storm
Warning" and a response type of "Shelter" is received, the tag will be replaced
by the CAP Response Type Substitution value corresponding to "Winter Storm
Warning" events and "Shelter" response types. The tag will behave similarly if
an alert with the response type "Prepare" is received, however if the response
type is "Monitor" (or any other value), text will not be substituted.

If the tag is used without arguments, like this:

```
[capresponse]
```

and an alert with any response type is received, the tag will be substituted
with the "CAP Response Type Substitution" value corresponding to that response
type and the alert's event field if one exists, otherwise no text will be
substituted.

#### CAP Urgency Tag

The `[capurgency...]` tag allows substituting a value corresponding to the
urgency field sent in the alert CAP message. This tag allows specifying
zero or more urgency values that will be used to filter which urgency values
will trigger a substitution corresponding to the alert event.

Urgency substitution fields must be configured in the
`View ➔ Alerts ➔ CAP Urgency Substitutions` dialog. To create one, enter
an event type into the text box in this dialog and press Create. Then select an
urgency value from the drop down and enter a MULTI string that will be
substituted for any `[capurgency...]` tag encountered in a message template
for alerts of that event type.

If no parameters are provided, the tag will use the urgency substitution
configurations for any urgency value encountered in the alert CAP message.
You may optionally include one or more urgency values (separated by commas
in the MULTI tag) to limit which urgency values are substituted.

##### Example

Consider the following tag:

```
[capurgencyImmediate,Expected]
```

If this tag is used in a template and an alert with the event "Winter Storm
Warning" and a urgency value of "Immediate" is received, the tag will be
replaced by the CAP Urgency Substitution value corresponding to "Winter Storm
Warning" events and "Immediate" urgency values. The tag will behave similarly
if an alert with the urgency value "Expected" is received, however if the
urgency value is "Future" (or any other value), text will not be substituted.

If the tag is used without arguments, like this:

```
[capurgency]
```

and an alert with any urgency value is received, the tag will be substituted
with the "CAP Urgency Substitution" value corresponding to that urgency value
and the alert's event field if one exists, otherwise no text will be
substituted.

### Pre- and Post-Alert Times

Messages may be posted to DMS before an alert has started or after an alert has
ended. This is controlled via the "Pre-Alert" and "Post-Alert" times in an
alert configuration, respectively, which are specified in hours. For a new
configuration, the default pre-alert time is 6 hours, and the default post-
alert time is 0 hours (meaning no message will be posted after the alert
expires). These can be changed for each alert configuration, and can also be
changed for each alert from the Alert Tab.

## Alert Message Priorities

Messages generated for alerts are automatically given a message priority based
on the urgency, severity, and certainty values contained in the alert CAP
message, which collectively describe the seriousness of an alert. The possible
values that may be seen in an alert XML, ranked from least to most emphatic,
are as follows:

Field     | Possible Values
----------|-----------------------------------------------
Urgency   | Unknown, Past, Future, Expected, Immediate
Severity  | Unknown, Minor, Moderate, Severe, Extreme
Certainty | Unknown, Unlikely, Possible, Likely, Observed

To translate these three values into a single message priority, IRIS transforms
each value to a number on a scale from 0 to 1 based on its relative place in
the list of possible values. It then weights each value by multiplying it by
the value contained in the corresponding system attribute (either
`ipaws_priority_weight_urgency`, `ipaws_priority_weight_severity`, or
`ipaws_priority_weight_certainty`) and sums them all together. The resulting
"priority score" is then used to select from one of the allowed message
priority values (`PSA`, `ALERT`, `AWS`, and `AWS_HIGH`).

By default, these system attribute weights are all set to 1.0, however they
may be adjusted.

## Operating the System

The IPAWS system is managed in the Alert tab of the IRIS client. This tab lists
all relevant alert deployments (i.e. those with matching configurations and DMS
in the alert area) that have been processed by the IRIS server. Alert
deployments are grouped into the following styles:

Style     | Description
----------|--------------------------------------------------------------------
Pending   | Alert deployment is pending operator approval (if approval is required)
Scheduled | Alert deployment is scheduled for deployment
Active    | Alert messages are currently deployed for an active, upcoming, or recently expired alert
Inactive  | Alert messages are not deployed for an active or upcoming alert
Past      | Alert is in the past and past the post-alert time
All       | All alert deployments

When an alert deployment is selected from the list, the tab will populate
the area below it with information about the alert and the DMS included in the
deployment. Selecting a DMS from the list will display a rendering of either
the message currently displayed on that sign or the message generated for the
alert. When an alert is selected, the map will display a polygon showing the
alert area, along with the signs that are included in the alert. If the alert
area is not visible, you may right click on the alert deployment and click
"Zoom to Alert Area" to center the map on the alert area.

The alert tab also functions as the approval/edit dialog. This allows operators
to approve alerts for deployment when operating the system in approval mode,
in addition to editing active alert deployments.

When a new alert is received that is eligible for a message deployment, the
IRIS server will process it to determine the signs for inclusion and the
message(s) that will be displayed. If the system is operating in approval mode
(the default mode), one or more entries will appear in the list as "Pending,"
with each entry corresponding to an alert and sign group.

Operators may adjust the signs that are included in the deployment by checking
or unchecking the box next to each DMS in the list. By default this list is
limited to signs in or near the alert area (with proximity determined by the
`ipaws_sign_thresh_auto_meters` and `ipaws_sign_thresh_opt_meters` system
attributes), however the list may be expanded to include all signs in the group
associated with that deployment by checking the box below the list of DMS.
Operators must have the `ipaws_deploy` capability to approve or edit alert
deployments.

Operators may also change the MULTI string that will be deployed to signs, the
message priority that controls which messages will be displayed when multiple
messages have been sent to the sign, and the pre- or post-alert times for the
deployment. Changes to the MULTI string may be previewed without deploying or
updating the message by clicking the "Preview" button.

If the system is operating in automatic mode (configurable by setting the
`ipaws_deploy_auto_mode` to `true`), alert deployments will automatically be
sent to signs with no human interaction. In automatic mode, the
`ipaws_deploy_auto_timeout_secs` system attribute may optionally be used to
allow operators a chance to review a deployment before it is sent to signs.
If this attribute is set to a non-zero value (in units of seconds), alert
deployments will appear as "Pending" as if in approval mode, however after
the timeout expires the messages will be sent to signs.

When operating in either approval or automatic mode, active alert deployments
can edited by selecting the alert in the list and clicking the "Edit" button.
Any changes made while in edit mode will not be executed until the "Deploy"
button is pressed.

Operators may also cancel any deployment by clicking the "Cancel" button. Alert
deployments that have been canceled will be found in the "Inactive" style until
the alert has expired (and is past the post-alert time). Inactive alerts can be
redeployed by selecting the deployment from the list and clicking the "Deploy"
button.

### Alert Phase Changes

When an alert moves from the pre-alert phase to the alert-active phase (i.e.
after the alert start time has passed), a new alert deployer is created to
post the alert-active message. If the system is in approval mode, the operator
will be required to approve the deployment. If the system is in automatic mode,
the new messages will deploy automatically (after the timeout, if configured).
In either mode, any changes to the signs selected for the alert will carry over
to the new deployer.

### Notification System

The system includes a notification feature to facilitate operation in approval
mode and keep operators aware of alert deployments in automatic mode. When the
system encounters alerts that are eligible for deployment, IRIS clients will
notify users via the flashing yellow "Notifications" button in the lower-right
corner of the interface. Clicking this button will open a dialog containing
active notifications, and double-clicking on a notification will open the Alert
tab and select the corresponding alert.

If the system is operating in approval mode, notifications will alert operators
of alerts requiring approval. The "Notifications" button will continue to blink
until the notification(s) is/are addressed (by double-clicking on the
notification, deploying the alert, or clicking the "Address All" button in the
notification dialog).

If the system is in automatic mode and a timeout is configured, notifications
will alert operators of alerts that are awaiting approval and operate similar
to approval mode until the timeout has passed. After the timeout has passed,
or if there is no timeout configured, the notification will alert operators
that the alert has been deployed.

At this time, the notification system is only used for the IPAWS system. This
system is extensible, however, and may be used for other purposes in the
future.

## Testing the System

Because the IPAWS system requires alert CAP messages in order to function, it
can be challenging to test. To address this, IRIS provides a testing mechanism
that allows testing the system with a mocked-up CAP message. To use this, first
a CAP XML message must be crafted with:

 - An event type configured in IRIS (which can be a custom "Test" event)
 - An alert area that contains signs suitable for testing
 - Alert start and end times corresponding to a suitable testing period

To do this, it is best to start with a real CAP message taken from
IPAWS-OPEN (e.g. one that is old or targets a different area) and replace the
values with ones suitable for testing. This must be done with care to ensure
the [CAP standard](http://docs.oasis-open.org/emergency/cap/v1.2/CAP-v1.2.html)
is followed and the message can be parsed.

After a CAP message is created, it can be fed into IRIS in one of two ways.

1. The file can be hosted on an arbitrary HTTPS server (including the IRIS
server itself if HTTPS is supported). IRIS can then be configured with a comm
link that points to this file on that server.
2. The file can be placed in `/var/log/iris/Ipaws_Test_Alert.xml` on the IRIS
server itself, and an existing IPAWS comm link can be put into the `TESTING`
condition.

In either case IRIS will read this file and process the alert as if it were a
real alert. Note that in a production environment this may activate real signs,
so care must be taken to ensure the testing is done in a controlled manner.
