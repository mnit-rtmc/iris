# Permissions

**Permissions** determine how much access a [role] has to [resource]s.

<details>
<summary>API Resources 🕵️ </summary>

* `iris/api/permission`
* `iris/api/permission/{id}`
* `iris/api/access`

| Access       | Primary                               |
|--------------|---------------------------------------|
| 👁️  View      | id                                    |
| 🔧 Configure | role, resource\_n, hashtag, access\_n |

</details>

## Setup

[Role] is the user role associated with the permissions.

[Resource] is the `{type}` part of a base [restricted resource] to be accessed.

[Hashtag] restricts the permission to resources which have the assigned tag.
Permissions containing hashtags are only checked for updates to existing
resources, not creation/deletion.

There are 4 **access** levels, with increasing permissiveness:

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
| [action plan]      | day matcher, [day plan], [device action], [plan phase], [time action] |
| alert config       | alert info, alert message                             |
| [beacon]           |                                                       |
| [camera]           | catalog, [flow stream], play list                     |
| [controller]       | [alarm], [comm link], [controller io], [gps], [modem] |
| [detector]         | [r_node], [road], station                             |
| [dms]              | [font], [graphic], [message line], [message pattern], [sign configuration], [sign detail], [sign message], [word] |
| [gate arm]         | [gate arm array]                                      |
| [incident]         | inc_advice, inc_descriptor, inc_locator               |
| [lcs]              | [lcs array], [lcs indication], [lane marking]         |
| [parking area]     |                                                       |
| permission         | [domain], [role], [user id]                           |
| [ramp meter]       |                                                       |
| [system attribute] | [cabinet style], [comm config]                        |
| [toll zone]        | [tag reader]                                          |
| [video monitor]    | monitor style                                         |
| [weather sensor]   |                                                       |

## Associated Resources

Some resources contain an associated resource_n, linking them to a base resource.
These include:

* __geo loc__
* __controller io__
* __device preset__
* __hashtag__


[action plan]: action_plans.html
[alarm]: alarms.html
[beacon]: beacons.html
[cabinet style]: controllers.html#cabinet-styles
[camera]: cameras.html
[comm config]: comm_config.html
[comm link]: comm_links.html
[controller]: controllers.html
[controller io]: controllers.html#io-pins
[day plan]: action_plans.html#day-plans
[detector]: vehicle_detection.html
[device action]: action_plans.html#device-actions
[dms]: dms.html
[domain]: users.html#domains
[flow stream]: flow_streams.html
[font]: fonts.html
[gate arm]: gate_arms.html
[gate arm array]: gate_arms.html#arrays
[geo loc]: geo_loc.html
[gps]: gps.html
[graphic]: graphics.html
[hashtag]: hashtags.html
[incident]: incidents.html
[lane marking]: lcs.html#lane-markings
[lcs]: lcs.html
[lcs array]: lcs.html#arrays
[lcs indication]: lcs.html#indications
[message line]: message_patterns.html#message-lines
[message pattern]: message_patterns.html
[modem]: modem.html
[parking area]: parking_areas.html
[plan phase]: action_plans.html#plan-phases
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
