# User Interface

The IRIS _user interface_ contains a **map** in the center, with a set of **side
panels** on the left.

## Map

After logging in, the map takes up most of the interface.  There are
automatically updated **layers** for several [device] types as well as a
[traffic layer].

Above the map, there is a set of [map extents], which are displayed as buttons.
Clicking on an _extent_ button changes the map view to that location.  Also
above the map, there is a menu to _enable_ or _disable_ **layers** of the map.
A **legend** menu shows the various icons.

Below the map, the mouse pointer location is displayed as **latitude** and
**longitude**.  To the left of that is the [selector tool].  To the right is the
**Edit Mode** button.

## Edit Mode

When the edit mode is **OFF**, all configuration changes will be disallowed.
This feature prevents administrators from accidentally modifying the system
while looking up information.  It is recommended to only turn the edit mode
**ON** while updating the system configuration.  Note: edit mode does not grant
any capabilities which are not already associated with the user's [role].

## Side Panels

To the left of the map is a set of tabbed panels.  Which panels are available
is dependant on the [capabilities] of the user's [role].

Tab      | Capability
---------|---------------
Incident | `incident_tab`
DMS      | `dms_tab`
Camera   | `camera_tab`
LCS      | `lcs_tab`
Meter    | `meter_tab`
Gates    | `gate_arm_tab`
Parking  | `parking_tab`
R_Node   | `sensor_tab`
Beacon   | `beacon_tab`
Toll     | `tag_reader_tab`
Plan     | `plan_tab`
Comm     | `comm_tab`
RWIS     | `sensor_tab`

## Session Menu

The **session** menu allows logging in and out, as well as changing the
password.  NOTE: password changes are not supported when using an [LDAP] system
for authentication.

## View Menu

The **view** menu is used for displaying [device] tables and other configuration
dialogs.


[selector tool]: cameras.html#selector-tool
[capabilities]: user_roles.html#capabilities
[device]: controllers.html#devices
[LDAP]: installation.html#ldap
[map extents]: mapping.html#map-extents
[role]: user_roles.html
[traffic layer]: vehicle_detection.html#traffic-layer
