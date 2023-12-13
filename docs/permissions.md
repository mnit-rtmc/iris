# Permissions

**Permissions** are a newer access control feature intended to replace
[capabilities] and [privileges].  For now, they determine permissions for web
access only.

<details>
<summary>API Resources</summary>

* `iris/api/permission`
* `iris/api/permission/{id}`
* `iris/api/access`

| Access       | Minimal                               |
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


[capabilities]: user_roles.html#capabilities
[hashtag]: hashtags.html
[privileges]: user_roles.html#privileges
[resource]: rest_api.html#resource-types
[restricted resource]: rest_api.html#restricted-resources
[role]: user_roles.html#roles
