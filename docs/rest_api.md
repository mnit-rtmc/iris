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

Additional (*full*) attributes are also included in a single object response.

## Resource Types

| Resource Type   | Description                 | Minimal                                   | Full |
|-----------------|-----------------------------|-------------------------------------------|----------------------|
| `alarm`         | Equipment alarms            | `name` `description` `controller` `state` | `pin` `trigger_time` |
| `cabinet_style` | Cabinet I/O                 | `name`                                    | `police_panel_pin_1` `police_panel_pin_2` `watchdog_reset_pin_1` `watchdog_reset_pin_2` `dip` |
| `comm_config`   | Communication configuration | `name` `description`                      | `protocol` `modem` `timeout_ms` `poll_period_sec` `long_poll_period_sec` `idle_disconnect_sec` `no_response_disconnect_sec` |
| `comm_link`     | Communication links         | `name` `description` `uri` `comm_config` `poll_enabled` | `connected` |
| `controller`    | Controllers for field devices | `name` `location` `comm_link` `drop_id` `cabinet_style` `condition` `notes` `version` `fail_time` | `geo_loc` `password` |
| `modem`         | POTS modems                 | `name` `enabled` | `uri` `config` `timeout_ms` |
| `permission`    | Resource permissions for user roles | | |
| `role`          | User access roles | | |
| `user`          | User accounts | | |

## ETags and Caching

Consider using Etags to avoid mid-air collisions
