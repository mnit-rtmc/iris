## Incidents

The **Incident** tab on the client interface is used to manage incidents within
IRIS.  Traffic incidents have several attributes, including _location_, _type_,
_detail_, _camera_ and _lane impact_.  Active incidents are written to an
[XML file], which can be processed by external systems.

### Creating

The interface for creating incidents is streamlined to allow operators to
process them quickly.

 1. Click one of the incident type buttons: **Crash**, **Stall**, **Road Work**
    or **Hazard**
 2. Select lane type — _Mainline_ is the default, but _Exit_, _Entrance_ and
    _CD Road_ are available
 3. Select incident location on the map (the cursor will change to a crosshair)
 4. Select incident detail (for _hazards_)
 5. Select verification camera.  The camera list will be populated with a few
    cameras near the location selected.
 6. Specify impact for each lane.  This is represented by a row of boxes, one
    for each lane at the incident location.  Both shoulders are displayed even
    if there are no actual shoulders at the location.  Each lane can be marked
    _clear_ (default), _blocked_ (red) or _affected_ (yellow) by clicking on the
    box.
 7. Finally, press the **Log** button to create the incident

A triangular icon will appear on the map pointing in the direction of travel,
color-coded with the incident type.

### Editing

To change the location or verification camera, the **Edit** button must be
pressed first.  To change the lane impact or to clear the incident, the **Edit**
button is not required.  After making changes, be sure to press the **Log**
button again.

### Clearing

Once an incident has been cleared, it will remain in the list for some time.
This can be configured with the `incident_clear_secs` [system attribute] —
the default value is 300 seconds (5 minutes).  During this time, a cleared
incident can be reactivated if necessary.

### Deploying Devices

Devices can be deployed based on the incident impact by pressing the **Deploy**
button.  This will bring up a form with a list of suggested devices to deploy
for the incident.  The list may include [DMS] or [LCS] devices.  All DMS signs
within the range of the incident are [checked](incident_dms.html).

### Incident Events

Incidents and all updates are recorded in the database.  There are two views
available:
 * `incident_view` contains one record for each recorded incident
 * `incident_update_view` contains a record for every update to all incidents


[DMS]: admin_guide.html#dms
[LCS]: admin_guide.html#lcs
[system attribute]: admin_guide.html#sys_attr
[XML file]: troubleshooting.html#xml-output
