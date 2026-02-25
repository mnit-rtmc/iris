# Permissions

A **permission** record grants one [role] access to a [resource].

<details>
<summary>API Resources 🕵️ </summary>

* `iris/api/permission`
* `iris/api/permission/{name}`
* `iris/api/access`

| Access       | Primary                    |
|--------------|----------------------------|
| 👁️  View      | name, role, base\_resource |
| 🔧 Configure | hashtag, access\_level     |

</details>

## Setup

[Role] is the user role associated with the permission.

[Base resource](#base-resources) is the resource to grant permission for the
role.

[Hashtag] restricts the permission to resources which have the assigned tag.
Permissions containing hashtags are only checked for updates to existing
resources, not creation/deletion.

There are 4 **access levels**, with increasing permissiveness:

| Level | Access       | Permissions              |
|-------|--------------|--------------------------|
|     1 | 👁️  View      | Monitor / read           |
|     2 | 👉 Operate   | + Control                |
|     3 | 💡 Manage    | + Policies, scheduling   |
|     4 | 🔧 Configure | + Create, update, delete |

When checks are performed, the **highest** access level of matching permissions
is used.

## Base Resources

To simplify administration, some permissions grant access to related resources.
In other words, permissions on the base resource also applies to any dependent
resource, at the same access level.

| Base Resource      | Dependent Resources                                   |
|--------------------|-------------------------------------------------------|
| [action plan]      | day matcher, [day plan], [device action], [plan phase], [time action], hashtag |
| [alert config]     | alert info, [alert message]                           |
| [beacon]           |                                                       |
| [camera]           | [camera preset], camera template, cam vid src ord, [encoder stream], [encoder type], vid source template |
| [controller]       | [alarm], [comm link], [gps], [modem]                  |
| [detector]         | [r_node], [road], station                             |
| [dms]              | [font], glyph, [graphic], [message line], [message pattern], [sign configuration], [sign detail], [sign message], [word] |
| [gate arm]         |                                                       |
| [incident]         | inc\_advice, inc\_descriptor, inc\_detail, inc\_locator, road\_affix |
| [lcs]              | [lcs state]                                           |
| [parking area]     |                                                       |
| permission         | [domain], [role], [user id], connection               |
| [ramp meter]       |                                                       |
| [system attribute] | [cabinet style], [comm config], [event config], map extent, rpt conduit |
| [toll zone]        | [tag reader]                                          |
| [video monitor]    | [flow stream], [monitor style], [play list]           |
| [weather sensor]   |                                                       |

## Associated Resources

Some resources contain an associated `resource_n`, linking them to another
resource.  These include:

* [geo loc]
* [controller io]
* device [preset]
* [hashtag]


[action plan]: action_plans.html
[alarm]: alarms.html
[alert config]: alerts.html#alert-configuration
[alert message]: alerts.html#alert-messages
[beacon]: beacons.html
[cabinet style]: controllers.html#cabinet-styles
[camera]: cameras.html
[camera preset]: cameras.html#presets
[comm config]: comm_config.html
[comm link]: comm_links.html
[controller]: controllers.html
[controller io]: controllers.html#io-pins
[day plan]: action_plans.html#day-plans
[detector]: vehicle_detection.html
[device action]: action_plans.html#device-actions
[dms]: dms.html
[domain]: users.html#domains
[encoder stream]: cameras.html#streams
[encoder type]: cameras.html#encoder-types
[event config]: events.html#configuration
[flow stream]: flow_streams.html
[font]: fonts.html
[gate arm]: gate_arms.html
[geo loc]: geo_loc.html
[gps]: gps.html
[graphic]: graphics.html
[hashtag]: hashtags.html
[incident]: incidents.html
[lcs]: lcs.html
[lcs state]: lcs.html#lcs-states
[message line]: message_patterns.html#message-lines
[message pattern]: message_patterns.html
[modem]: modem.html
[monitor style]: video.html#style
[parking area]: parking_areas.html
[plan phase]: action_plans.html#plan-phases
[play list]: video.html#play-lists
[preset]: cameras.html#presets
[r_node]: road_topology.html#r_nodes
[ramp meter]: ramp_meters.html
[resource]: rest_api.html#resource-types
[restricted resource]: rest_api.html#restricted-resources-codeirisapicode
[road]: road_topology.html#roads
[role]: users.html#roles
[sign configuration]: sign_configuration.html
[sign detail]: sign_configuration.html#sign-details
[sign message]: sign_message.html
[system attribute]: system_attributes.html
[tag reader]: tolling.html#tag-readers
[time action]: action_plans.html#time-actions
[toll zone]: tolling.html#toll-zones
[user id]: users.html#user-ids
[video monitor]: video.html
[weather sensor]: weather_sensors.html
[word]: words.html
