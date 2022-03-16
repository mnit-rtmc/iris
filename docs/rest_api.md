# REST API

These are rough notes about the IRIS web API.

## Requests

Data requests are split into *public* and *restricted* paths:

- `iris/`: Public data (no authentication)
- `iris/img/`: Public sign images (no authentication)
- `iris/api/`: Restricted data (needs session authentication)
- `iris/api/login`: Authentication endpoint
- `iris/api/access`: User's access permissions

## Public Resources

These resources are JSON arrays, fetched using http `GET` requests.

- `iris/camera_pub`: Camera locations and configuration
- `iris/detector`: Vehicle detectors
- `iris/dms_message`: Current DMS messages and status
- `iris/dms_pub`: DMS locations and configuration
- `iris/font`: Bitmapped fonts for DMS
- `iris/graphic`: Graphics for DMS
- `iris/incident`: Currently active incidents
- `iris/sign_config`: DMS sign configurations
- `iris/sign_detail`: DMS sign detail information
- `iris/sign_message`: Active DMS sign messages
- `iris/station_sample`: Vehicle detection station data
- `iris/system_attribute`: System-wide attributes (public only)
- `iris/TPIMS_archive`: Truck parking archive data
- `iris/TPIMS_dynamic`: Truck parking dynamic data
- `iris/TPIMS_static`: Truck parking static data

### Lookup Tables

These resources are static, and may only change on IRIS updates:

- `iris/comm_protocol`: Communication protocols
- `iris/condition`: Controller conditions
- `iris/direction`: Travel directions
- `iris/resource_type`: [Resource types] available in `iris/api/`
- `iris/road_modifier`: Road modifiers

### Sign Images

The resources in `iris/img/` are GIF images of active sign messages from
`sign_message`.

## Login and Access

Authentication uses a `POST iris/api/login` request with a JSON object
containing `username` and `password` values.  This returns a session cookie
which can be used for subsequent restricted requests.

A `GET iris/api/access` request returns a JSON array of [permission] records
associated with the authenticated user's role.  This endpoint is required for
roles which do not have any access to the [permission], [role] and [user] types.

## Restricted Resources

Restricted resources can be accessed using standard http methods:

- `GET iris/api/{type}`: Get all objects of `{type}` (minimal), as a JSON array
- `GET iris/api/{type}/{name}`: Get one full object as JSON
- `POST iris/api/{type}`: Create a new object of the `{type}`.  Body contains
                          required attributes as JSON
- `PATCH iris/api/{type}/{name}`: Update attributes of one object, with JSON
- `DELETE iris/api/{type}/{name}`: Delete one object

A `Content-Type: application/json` header is included where appropriate.

### All Object Arrays

A `GET iris/api/{type}` returns all objects of `{type}` in a JSON array.
These objects contain only the *minimal* attributes -- those needed for
*searching* and *displaying compact cards*.

The response for these requests contains an ETag header, derived from the file's
*modified* metadata, encoded in hexadecimal.

## Resource Types

*Full* attributes {`in brackets`} are only included in single object responses.

### `alarm`

| Access       | Attributes                       |
|--------------|----------------------------------|
| Read Only    | name, state, {`trigger_time`}    |
| 🔧 Configure | description, controller, {`pin`} |

### `cabinet_style`

| Access       | Attributes |
|--------------|------------|
| Read Only    | name       |
| 🔧 Configure | {`police_panel_pin_1`}, {`police_panel_pin_2`}, {`watchdog_reset_pin_1`}, {`watchdog_reset_pin_2`}, {`dip`} |

### `comm_config`

| Access       | Attributes |
|--------------|------------|
| Read Only    | name       |
| 💡 Plan      | {`timeout_ms`}, {`idle_disconnect_sec`}, {`no_response_disconnect_sec`} |
| 🔧 Configure | description, {`protocol`}, {`modem`}, {`poll_period_sec`}, {`long_poll_period_sec`} |

### `comm_link`

| Access       | Attributes                     |
|--------------|--------------------------------|
| Read Only    | name, connected                |
| 💡 Plan      | poll\_enabled                  |
| 🔧 Configure | description, uri, comm\_config |

### `controller`

| Access       | Attributes                                         |
|--------------|----------------------------------------------------|
| Read Only    | name, location, version, fail\_time, {`geo_loc`}   |
| 👉 Operate   | {`download`}, {`device_req`}                       |
| 💡 Plan      | condition, notes                                   |
| 🔧 Configure | comm\_link, drop\_id, cabinet\_style, {`password`} |

### `geo_loc`

| Access       | Attributes |
|--------------|------------|
| Read Only    | name       |
| 🔧 Configure | roadway, road\_dir, cross\_street, cross\_dir, cross\_mod, landmark, {`lat`}, {`lon`} |

### `modem`

| Access       | Attributes              |
|--------------|-------------------------|
| Read Only    | name                    |
| 💡 Plan      | enabled, {`timeout_ms`} |
| 🔧 Configure | {`uri`}, {`config`}     |

### `permission`

| Access       | Attributes                          |
|--------------|-------------------------------------|
| Read Only    | id                                  |
| 🔧 Configure | role, resource\_n, batch, access\_n |

### `role`

| Access       | Attributes |
|--------------|------------|
| Read Only    | name       |
| 💡 Plan      | enabled    |

### `user`

| Access       | Attributes       |
|--------------|------------------|
| Read Only    | name             |
| 💡 Plan      | enabled          |
| 🔧 Configure | full\_name, role |


[permission]: #permission
[resource types]: #resource-types
[role]: #role
[user]: #user
