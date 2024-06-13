# Users

Select `View ➔ System ➔ Users` menu item

User authentication is determined by [user ID](#user-ids), [role](#roles),
and [domains](#domains).

## User IDs

A user must have an ID to log in to IRIS.  The user's permissions are
determined by their [role](#roles).

If the user has a **distinguished name** (dn), then authentication is
performed using [LDAP].  Otherwise, the supplied password is checked against
the stored password hash for the account.

In addition to the password, these checks are performed:
 - The user must be enabled
 - The role must be enabled
 - The connection IP must be within an enabled domain for the role
 - (Web UI) All IPs in the `X-Forwarded-For` HTTP header must be within enabled
   domains

<details>
<summary>API Resources 🕵️ </summary>

* `iris/api/user_id`
* `iris/api/user_id/{name}`

| Access       | Primary          | Secondary |
|--------------|------------------|-----------|
| 👁️  View      | name             |           |
| 💡 Manage    | enabled          |           |
| 🔧 Configure | full\_name, role | dn        |

</details>

## Roles

A role defines the set of [capabilities](#capabilities) associated with a _user_
account (any other _capabilities_ will not be available).  The default _roles_
are `administrator` and `operator`.  The `administrator` _role_ has
[capabilities](#capabilities) which allow unfettered access to the system.
Other _roles_ can be created to allow different capability sets, as needed.

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

A network _domain_ uses [CIDR] to restrict the IP addresses from which a _user_
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

A _capability_ is a set of [privileges](#privileges) which can be associated
with _roles_.  It grants all necessary _privileges_ to perform a specific task.

There are typically 3 _capabilities_ for each [device] type:

* `_tab` — Grant **view** privileges
* `_control` — Grant **control** privileges
* `_admin` — Grant **administration** privileges

_Capabilities_ can be disabled, preventing all users from having access to them.
For example, if a system does not contain any [LCS] devices, the `lcs_tab`
_capability_ could be disabled, preventing that tab from appearing in the user
interface for all users.

**WARNING: the** `base_admin` **capability can grant access for all IRIS
functions.**

(NOTE: capabilities are being phased out in favor of [permissions])

## Privileges

A _privilege_ grants read or write access to one type of object.  There are 5
fields required to fully specify a _privilege_.

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
