# Permissions

**Permissions** are a newer access control feature intended to replace
[capabilities] and [privileges].  For now, they determine permissions for web
access only.

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

| Base Resource      | Dependent Resources                            |
|--------------------|------------------------------------------------|
| action plan        | beacon action, camera action, day matcher, day plan, dms action, lane action, meter action, plan phase |
| alert config       |                                                |
| beacon             |                                                |
| [camera]           | catalog, [flow stream], monitor style, play list, [video monitor] |
| [controller]       | [alarm], [comm link], [controller io], [modem] |
| [detector]         | [rnode], [road], station                       |
| [dms]              | [font], [graphic], [message line], [message pattern], [sign configuration], [sign detail], [sign message], [word] |
| [gate arm]         | [gate arm array]                               |
| incident           | inc_advice, inc_descriptor, inc_locator        |
| [lcs]              | [lcs array], [lcs indication], [lane marking]  |
| parking area       |                                                |
| [permission]       | [domain], [role], [user id]                    |
| ramp meter         |                                                |
| [system attribute] | [cabinet style], [comm config]                 |
| [toll zone]        | [tag reader]                                   |
| weather sensor     |                                                |

## Associated Resources

Some resources contain an associated resource_n, linking them to a base resource.
These include:

* __geo loc__
* __controller io__
* __device preset__
* __hashtag__


[alarm]: alarms.html
[camera]: cameras.html
[capabilities]: user_roles.html#capabilities
[controller]: controllers.html
[dms]: dms.html
[flow stream]: flow_streams.html
[font]: fonts.html
[gate arm]: gate_arms.html
[gate arm array]: gate_arms.html#arrays
[geo loc]: geo_loc.html
[graphic]: graphics.html
[hashtag]: hashtags.html
[lane marking]: lcs.html#lane-markings
[lcs]: lcs.html
[lcs array]: lcs.html#arrays
[lcs indication]: lcs.html#indications
[message line]: message_patterns.html#message-lines
[message pattern]: message_patterns.html
[privileges]: user_roles.html#privileges
[resource]: rest_api.html#resource-types
[restricted resource]: rest_api.html#restricted-resources-codeirisapicode
[role]: user_roles.html#roles
[sign configuration]: sign_configuration.html
[sign detail]: sign_configuration.html#sign-details
[sign message]: sign_message.html
[video monitor]: video.html
[word]: words.html
