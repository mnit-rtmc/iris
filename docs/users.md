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
Role      | [Role](#roles) which determines the authorized [permissions]
Dn        | **Distinguished name** for [LDAP] authentication
Password  | Hash of password
Enabled   | Flag to disable account

On login, these checks are performed:
 - The user and role must be `enabled`
 - The connection IP must be within an `enabled` domain for the role
 - _(Web UI)_ All IPs in the [X-Forwarded-For] HTTP header must be within
   `enabled` domains for the role
 - Password authenticated
   * If `dn` is set: [LDAP] server authentication - on success, update the
     cached password hash
   * If `dn` is NULL (or LDAP connection fails): password hash authentication

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

A role defines the set of [permissions] associated with a user account.
The default roles are `administrator` and `operator`.  The `administrator` role
has permissions which allow unfettered access to the system.  Other roles can
be created to allow different permissions, as needed.

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

## Events

Whenever certain client events occur, a time-stamped [event] record can be
stored in the `client_event` table:

* CONNECT
* DISCONNECT
* AUTHENTICATE
* FAIL AUTHENTICATION
* FAIL DOMAIN
* FAIL DOMAIN XFF
* FAIL PASSWORD
* CHANGE PASSWORD
* UPDATE PASSWORD


[CIDR]: https://en.wikipedia.org/wiki/Classless_Inter-Domain_Routing
[device]: controllers.html#devices
[event]: events.html
[LDAP]: installation.html#ldap
[permissions]: permissions.html
[x-forwarded-for]: https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/X-Forwarded-For
