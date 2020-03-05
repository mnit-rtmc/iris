# User Roles

Select `View ➔ System ➔ Users and Roles` menu item

IRIS contains a set of user accounts which are allowed to access the system.
Each account must be assigned to a specific [role](#roles).  During login, the
_user_ account is checked for validity.  For a successful login, the _user_ and
_role_ must both be enabled.  If the _user_ has a **distinguished name** (dn),
then authentication is performed using [LDAP].  Otherwise, the supplied password
is checked against the stored password hash for the account.

## Roles

A role defines the set of [capabilities](#capabilities) associated with a _user_
account (any other _capabilities_ will not be available).  The default _roles_
are `administrator` and `operator`.  The `administrator` _role_ has
[capabilities](#capabilities) which allow unfettered access to the system.
Other _roles_ can be created to allow different capability sets, as needed.

**WARNING: if the administrator role or admin user are disabled, the ability to
make further changes will be lost immediately.**

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

## Privileges

A _privilege_ grants read or write access to one type of object.  There are 5
fields required to fully specify a _privilege_.

Field     | Description
----------|----------------------------------------------------
Type      | Object type selected from a list of available types
Object    | A regular expression to match object names.  NOTE: Write access only.  This feature will be removed in a future version of IRIS.
Group     | Used to divide objects into related groups.  This is an experimental feature intended to replace the **object** field.  NOTE: Write access only.
Attribute | Write access to a specific attribute of an object type can be specified with this field.
Write     | When this checkbox is checked, write access is granted.  Otherwise, the privilege grants read access.  To be granted write access, a role must also have read access to the object type.

## Domains

A network _domain_ uses [CIDR] to restrict the IP addresses from which a _user_
can connect to IRIS.  To log in, a _user_ must be assigned to a matching
_enabled_ domain.

## Events

Whenever certain client events occur, a time-stamped record is added to the
`client_event` table:

* CONNECT
* DISCONNECT
* AUTHENTICATE
* FAIL AUTHENTICATION
* FAIL DOMAIN
* CHANGE PASSWORD

These records are purged automatically when older than the value of the
`client_event_purge_days` [system attribute].


[CIDR]: https://en.wikipedia.org/wiki/Classless_Inter-Domain_Routing
[device]: controllers.html#devices
[LCS]: lcs.html
[LDAP]: installation.html#ldap
[system attribute]: system_attributes.html
