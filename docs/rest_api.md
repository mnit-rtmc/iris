# REST API

These are rough notes about the IRIS web API.

## Requests

Data requests are split into *public* and *restricted* paths:

- `iris/`: Public data (no authentication required)
- `iris/api/`: Restricted data (needs session authentication)
- `iris/api/login`: Authentication endpoint
- `iris/api/access`: User's access permissions

## Public Resources

These resources are JSON arrays, fetched using http `GET` requests.

- `iris/camera_pub`: Camera locations and configuration
- `iris/comm_protocol`: Protocol LUT (may only change on IRIS updates)
- `iris/condition`: Condition LUT (may only change on IRIS updates)
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
- `iris/img/`: Images (gif) of active sign messages in `sign_message`

## Login and Access

A `POST iris/api/login` request is needed to authenticate a session, by
submitting `username` and `password` form values.  This returns a session cookie
which can be used for subsequent restricted requests.

A `GET iris/api/access` request returns a JSON array of `permission` records
associated with the authenticated user's role.  This endpoint is required for
roles which do not have any access to the `permission`, `role` and `user` types.

## Restricted Resources

There are many restricted resource types, which can be accessed using standard
http methods:

- `GET iris/api/{type}`: Get all objects of `{type}` (minimal), as a JSON array
- `GET iris/api/{type}/{name}`: Get one full object as JSON
- `POST iris/api/{type}`: Create a new object of the `{type}`.  Body contains
                          required attributes as JSON
- `PATCH iris/api/{type}/{name}`: Update attributes of one object, with JSON
- `DELETE iris/api/{type}/{name}`: Delete one object

A `Content-Type: application/json` header is included where appropriate.

A `GET` request of all objects of a `{type}` contains only the *minimal*
attributes.  Those are attributes needed for *searching* and *displaying
compact cards*.

## Resource Types

*Full* attributes {`in brackets`} are only included in single object responses.

### `alarm`

| Access    | Attributes                       |
|-----------|----------------------------------|
| Read Only | name, state, {`trigger_time`}    |
| Configure | description, controller, {`pin`} |

### `cabinet_style`

| Access    | Attributes |
|-----------|------------|
| Read Only | name       |
| Configure | {`police_panel_pin_1`}, {`police_panel_pin_2`}, {`watchdog_reset_pin_1`}, {`watchdog_reset_pin_2`}, {`dip`} |

### `comm_config`

| Access    | Attributes |
|-----------|------------|
| Read Only | name       |
| Plan      | {`timeout_ms`}, {`idle_disconnect_sec`}, {`no_response_disconnect_sec`} |
| Configure | description, {`protocol`}, {`modem`}, {`poll_period_sec`}, {`long_poll_period_sec`} |

### `comm_link`

| Access    | Attributes                     |
|-----------|--------------------------------|
| Read Only | name, connected                |
| Plan      | poll\_enabled                  |
| Configure | description, uri, comm\_config |

### `controller`

| Access    | Attributes                                                |
|-----------|-----------------------------------------------------------|
| Read Only | name, location, version, fail\_time, {`geo_loc`}          |
| Operate   | {`download`}, {`device_req`}                              |
| Plan      | condition                                                 |
| Configure | comm\_link, drop\_id, cabinet\_style, notes, {`password`} |

### `modem`

| Access    | Attributes              |
|-----------|-------------------------|
| Read Only | name                    |
| Plan      | enabled, {`timeout_ms`} |
| Configure | {`uri`}, {`config`}     |

### `permission`

| Access    | Attributes                                 |
|-----------|--------------------------------------------|
| Read Only | id                                         |
| Configure | role, resource\_n, {`batch`}, {`access_n`} |

### `role`

| Access    | Attributes |
|-----------|------------|
| Read Only | name       |
| Plan      | enabled    |

### `user`

| Access    | Attributes       |
|-----------|------------------|
| Read Only | name             |
| Plan      | enabled          |
| Configure | full\_name, role |

## ETags and Caching

Consider using Etags to avoid mid-air collisions
