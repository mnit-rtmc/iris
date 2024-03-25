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

[Resource] is the `{type}` part of a [restricted resource] to be accessed.

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

## Dependent Resources

To simplify administration, some permissions grant access to related resources.
In other words, permissions on the main resource also applies to any dependent
resource, at the same access level.

1. [camera]
   * [flow stream]
2. [dms]
   * [font]
   * [graphic]
   * [message line]
   * [message pattern]
   * [sign configuration]
   * [sign detail]
   * [sign message]
   * [word]
3. [gate arm]
   * [gate arm array]
4. [lcs]
   * [LCS array]
   * [LCS indication]
   * [Lane marking]


[camera]: cameras.html
[capabilities]: user_roles.html#capabilities
[dms]: dms.html
[flow stream]: flow_streams.html
[font]: fonts.html
[gate arm]: gate_arms.html
[gate arm array]: gate_arms.html#arrays
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
[restricted resource]: rest_api.html#restricted-resources
[role]: user_roles.html#roles
[sign configuration]: sign_configuration.html
[sign detail]: sign_configuration.html#sign-details
[sign message]: sign_message.html
[word]: words.html
