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

*Full* attributes (in parentheses) are only included in single object responses.

### `alarm`

| Access      | Attributes                         |
|-------------|------------------------------------|
| Read Only   | `name` `state` (`trigger_time`)    |
| 4 Configure | `description` `controller` (`pin`) |

### `cabinet_style`

| Access      | Attributes |
|-------------|------------|
| Read Only   | `name`     |
| 4 Configure | (`police_panel_pin_1`) (`police_panel_pin_2`) (`watchdog_reset_pin_1`) (`watchdog_reset_pin_2`) (`dip`) |

### `comm_config`

| Access      | Attributes |
|-------------|------------|
| Read Only   | `name`     |
| 3 Plan      | (`timeout_ms` `idle_disconnect_sec` `no_response_disconnect_sec`)
| 4 Configure | `description` (`protocol` `modem` `poll_period_sec` `long_poll_period_sec`)

### `comm_link`

| `comm_link` | View (1)    | Plan (3)       | Configure (4) |
|-------------|-------------|----------------|---------------|
| Minimal     | `name`      | `poll_enabled` | `description` `uri` `comm_config` |
| Full        | `connected` |                |               |

| `controller` | View (1)                                | Plan (3)     | Configure (4) |
|--------------|-----------------------------------------|--------------|---------------|
| Minimal      | `name` `location` `version` `fail_time` | `condition`  | `comm_link` `drop_id` `cabinet_style` `notes` |
| Full         | `geo_loc` `password`                    | `download` `device_req` |    |

| `modem` | View (1) | Plan (3)     | Configure (4)  |
|---------|----------|--------------|----------------|
| Minimal | `name`   | `enabled`    |                |
| Full    |          | `timeout_ms` | `uri` `config` |

| `permission` | View (1) | Configure (4)       |
|--------------|----------|---------------------|
| Minimal      | `id`     | `role` `resource_n` |
| Full         |          | `batch` `access_n`  |

| `role`  | View (1) | Plan (3)  |
|---------|----------|-----------|
| Minimal | `name`   | `enabled` |
| Full    |          |           |

| `user`  | View (1) | Plan (3)  | Configure (4)      |
|---------|----------|-----------|--------------------|
| Minimal | `name`   | `enabled` | `full_name` `role` |
| Full    |          |           |                    |

## ETags and Caching

Consider using Etags to avoid mid-air collisions
