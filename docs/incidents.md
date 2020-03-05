# Incidents

The **Incident** tab on the client interface is used to manage incidents within
IRIS.  Traffic incidents have several attributes, including _location_, _type_,
_detail_, _camera_ and _lane impact_.  Active incidents are written to an
[XML file], which can be processed by external systems.

## Creating

The interface for creating incidents is streamlined to allow operators to
process them quickly.

 1. Click one of the incident type buttons: **Crash**, **Stall**, **Road Work**
    or **Hazard**
 2. Select lane type — _Mainline_ is the default, but _Exit_, _Entrance_ and
    _CD Road_ are available
 3. Select incident location on the map (the cursor will change to a crosshair)
 4. Select incident detail (for _hazards_)
 5. Select verification camera.  The list will be populated with a few cameras
    near the selected location.
 6. Specify _lane impact_ in a row of boxes, one for each lane at the incident
    location.  Clicking on a box cycles thru lane impact values.  Both shoulders
    are displayed even if there are no actual shoulders at the location.
 7. Finally, press the **Log** button to create the incident

A triangular marker will appear on the map pointing in the direction of travel,
color-coded with the incident type.

### Lane Impact

There are three lane impact values:

 1. _free flowing_ (clear) — lane not impacted
 2. _blocked_ (red) — lane completely blocked by incident
 3. _affected_ (yellow) — lane partially blocked by incident

## Editing

To change the location or verification camera, the **Edit** button must be
pressed first.  To change the lane impact or to clear the incident, the **Edit**
button is not required.  After making changes, be sure to press the **Log**
button again.

## Clearing

Press the **Clear** button to clear the incident.  It will remain in the list
for some time afterward.  This time interval can be configured with the
`incident_clear_secs` [system attribute] — the default value is 300 seconds (5
minutes).  During this time, a cleared incident can be reactivated if necessary.

## Incident Events

Incidents and all updates are recorded in the database.  There are two views
available:
 * `incident_view` contains one record for each recorded incident
 * `incident_update_view` contains a record for every update to each incident

## Deploying Devices

Devices can be deployed based on incident attributes.  Pressing the **Deploy**
button will bring up a form with a list of [suggested DMS] and [suggested LCS]
devices to deploy for the incident.


[DMS]: dms.html
[LCS]: lcs.html
[suggested DMS]: incident_dms.html
[suggested LCS]: incident_lcs.html
[system attribute]: system_attributes.html
[XML file]: troubleshooting.html#xml-output
