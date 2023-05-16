# Permissions

**Permissions** are a newer access control feature intended to replace
[capabilities] and [privileges].  For now, they determine permissions for web
access only.

## Resources

* `iris/api/permission`
* `iris/api/permission/{id}`
* `iris/api/access`

| Access       | Minimal                               |
|--------------|---------------------------------------|
| Read Only    | id                                    |
| 🔧 Configure | role, resource\_n, hashtag, access\_n |

[Role] is the user role associated with the permissions.

[Resource] is the `type` part of the URI.

A **hashtag** is the `#` character, followed by a string of letters and/or
numbers.  They can be used for grouping resources into districts or other
categories.  NOTE: this feature depends on adding hashtags to relevant
resources, which is incomplete.

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
