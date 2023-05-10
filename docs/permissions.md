# Permissions

**Permissions** are a newer access control feature intended to replace
[capabilities] and [privileges].  For now, they determine permissions for web
access only.

## Resources

* `iris/api/permission`
* `iris/api/permission/{id}`
* `iris/api/access`

| Access       | Minimal                             | Full |
|--------------|-------------------------------------|------|
| Read Only    | id                                  |      |
| 🔧 Configure | role, resource\_n, batch, access\_n |      |

[Role] is the user role associated with the permissions.

[Resource] is the `type` part of the URI.

A **batch** is a group to which a resource may belong.  These typically are
used for districts or similar regional divisions.

There are 4 **access** levels, with increasing permissiveness:

| Level | Access       | Permissions              |
|-------|--------------|--------------------------|
|     1 | 👁️  View      | Monitor / read           |
|     2 | 👉 Operate   | + Control                |
|     3 | 💡 Plan      | + Policies, scheduling   |
|     4 | 🔧 Configure | + Create, update, delete |


[capabilities]: user_roles.html#capabilities
[privileges]: user_roles.html#privileges
[resource]: rest_api.html#resource-types
[role]: user_roles.html#roles
