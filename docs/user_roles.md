# User Roles

Select `View ➔ System ➔ Users and Roles` menu item

IRIS contains a set of user accounts which are allowed to access the system.
Each account must be assigned to a specific [role](#role).  During login, the
user account is checked for validity.  For a successful login, the user and role
must both be enabled.  If the user has a **distinguished name** (dn), then
authentication is performed using [LDAP].  Otherwise, the supplied password is
checked against the stored password hash for the account.

## Roles

A role defines the set of [capabilities](#capabilities) associated with a user
account (any other capabilities will not be available).  The default roles are
`administrator` and `operator`.  The `administrator` role has
[capabilities](#capabilities) which allow unfettered access to the system.
Other roles can be created to allow different capability sets for users, as
needed.

**WARNING: if the administrator role or admin user are disabled, the ability to
make further changes will be lost immediately.**

## Capabilities

A capability is a set of [privileges](#privileges) which can be associated with
roles.  It grants all necessary privileges to perform a specific user task.

There are typically 3 capabilities for each device type:

1. `_tab` — Grant **view** privileges
2. `_control` — Grant **control** privileges
3. `_admin` — Grant **administration** privileges

Capabilities can be disabled, preventing all users from having access to them.
For example, if a system does not contain any [LCS] devices, the `lcs_tab`
capability could be disabled, preventing that tab from appearing in the user
interface for any users.

**WARNING: the** `base_admin` **capability can grant access for all IRIS
functions.**

## Privileges

A privilege grants read or write access to one type of object.  There are 5
fields required to fully specify a privilege.

Field     | Description
----------|----------------------------------------------------
Type      | Object type selected from a list of available types
Object    | A regular expression to match object names.  NOTE: Write access only.  This feature will be removed in a future version of IRIS.
Group     | Used to divide objects into related groups.  This is an experimental feature intended to replace the **object** field.  NOTE: Write access only.
Attribute | Write access to a specific attribute of an object type can be specified with this field.
Write     | When this checkbox is checked, write access is granted.  Otherwise, the privilege grants read access.  To be granted write access, a role must also have read access to the object type.


[LCS]: lcs.html
[LDAP]: admin_guide.html#sonar_config
