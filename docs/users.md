# Users

Select `View ➔ System ➔ Users` menu item

User authentication is determined by [user ID](#user-ids), [role](#roles),
and [domains](#domains).

## User IDs

A user must have an ID to log in to IRIS.

Field     | Description
----------|-------------
Name      | Account name
Full Name | User name
Dn        | **Distinguished name** for [LDAP] authentication
Password  | Hash of password
Role      | [Role](#roles) which determines the authorized permissions
Enabled   | Flag to disable account

On login, these checks are performed:
 - The user and role must be `enabled`
 - The connection IP must be within an `enabled` domain for the role
 - _(Web UI)_ All IPs in the [X-Forwarded-For] HTTP header must be within
   `enabled` domains for the role
 - If `dn` is not NULL, authentication is performed using [LDAP].  On
   successful authentication, the cached password is updated, if necessary.
 - If `dn` is NULL or the LDAP server does not respond, authentication is
   performed with the stored password hash

<details>
<summary>API Resources 🕵️ </summary>

* `iris/api/user_id`
* `iris/api/user_id/{name}`

| Access       | Primary          | Secondary  |
|--------------|------------------|------------|
| 👁️ View      | name             |            |
| 💡 Manage    | enabled          | password † |
| 🔧 Configure | full\_name, role | dn         |

† _Write only_

</details>

## Roles

A role defines the set of [capabilities](#capabilities) associated with a user
account (any other capabilities will not be available).  The default roles
are `administrator` and `operator`.  The `administrator` role has
[capabilities](#capabilities) which allow unfettered access to the system.
Other roles can be created to allow different capability sets, as needed.

**WARNING: if the administrator role or admin user are disabled, the ability to
make further changes will be lost immediately.**

<details>
<summary>API Resources 🕵️ </summary>

* `iris/api/role`
* `iris/api/role/{name}`

| Access       | Primary    | Secondary |
|--------------|------------|-----------|
| 👁️  View      | name       |           |
| 💡 Manage    | enabled    |           |
| 🔧 Configure |            | domains   |

</details>

## Domains

A network domain uses [CIDR] to restrict the IP addresses from which a role
can connect to IRIS.

<details>
<summary>API Resources 🕵️ </summary>

* `iris/api/domain`
* `iris/api/domain/{name}`

| Access       | Primary | Secondary |
|--------------|---------|-----------|
| 👁️  View      | name    |           |
| 💡 Manage    | enabled |           |
| 🔧 Configure |         | block     |

</details>

## Capabilities

A capability is a set of [privileges](#privileges) which can be associated
with roles.  It grants all necessary privileges to perform a specific task.

There are typically 3 capabilities for each [device] type:

* `_tab` — Grant **view** privileges
* `_control` — Grant **control** privileges
* `_admin` — Grant **administration** privileges

Capabilities can be disabled, preventing all users from having access to them.
For example, if a system does not contain any [LCS] devices, the `lcs_tab`
capability could be disabled, preventing that tab from appearing in the user
interface for all users.

**WARNING: the** `base_admin` **capability can grant access for all IRIS
functions.**

(NOTE: capabilities are being phased out in favor of [permissions])

## Privileges

A privilege grants read or write access to one type of object.  There are 5
fields required to fully specify a privilege.

Field     | Description
----------|----------------------------------------------------
Type      | Object type selected from a list of available types
Object    | A regular expression to match object names.
Group     | Used to divide objects into related groups.  NOTE: Write access only.
Attribute | Write access to a specific attribute of an object type can be specified with this field.
Write     | When this checkbox is checked, write access is granted.  Otherwise, the privilege grants read access.  To be granted write access, a role must also have read access to the object type.

(NOTE: privileges are being phased out in favor of [permissions])

## Events

Whenever certain client events occur, a time-stamped record is added to the
`client_event` table:

* CONNECT
* DISCONNECT
* AUTHENTICATE
* FAIL AUTHENTICATION
* FAIL DOMAIN
* FAIL DOMAIN XFF
* CHANGE PASSWORD

These records are purged automatically when older than the value of the
`client_event_purge_days` [system attribute].


[CIDR]: https://en.wikipedia.org/wiki/Classless_Inter-Domain_Routing
[device]: controllers.html#devices
[LCS]: lcs.html
[LDAP]: installation.html#ldap
[permissions]: permissions.html
[system attribute]: system_attributes.html
[x-forwarded-for]: https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/X-Forwarded-For
