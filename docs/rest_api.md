# REST API

These are rough notes about the IRIS web API.

## Requests

Data requests are split into *public* and *restricted* paths:

- `iris/`: Public data
- `iris/api/`: Restricted data (needs session authentication)
- `iris/api/login`: Authentication endpoint

## Public Data

This data is available publicly, with no authentication required.  These
resources are JSON arrays, fetched using http `GET` requests.

- `iris/camera_pub`: Camera locations and configuration
- `iris/comm_protocol`: Protocol look-up table (may only change on IRIS updates)
- `iris/condition`: Condition look-up table (may only change on IRIS updates)
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

## Restricted Data

There are many restricted resource types, which can be accessed using standard
http methods.  The `{type}` values include:

- `alarm`: Equipment alarms
- `cabinet_style`: Cabinet styles and I/O pins
- `comm_config`: Communication configurations
- `comm_link`: Communication links
- `controller`: Controllers for field devices
- `modem`: POTS modems
- `permission`: Resource permissions for user roles
- `role`: User access roles
- `user`: User accounts

A `Content-Type: application/json` header is included where appropriate.

- `GET iris/api/{type}`: Get all objects of `{type}` (minimal), as a JSON array
- `POST iris/api/{type}`: Create a new object of the `{type}`.  Body contains
                          required attributes as JSON
- `GET iris/api/{type}/{name}`: Get one full object as JSON
- `PATCH iris/api/{type}/{name}`: Update attributes of one object, with JSON
- `DELETE iris/api/{type}/{name}`: Delete one object

A `GET` request of all objects of a `{type}` contains only the *minimal*
attributes.  Those are attributes needed for *searching* and *displaying
compact cards*.

Additional (*full*) attributes are also included in a single object response.

## ETags and Caching

Consider using Etags to avoid mid-air collisions

## Object definitions

- `alarm`
  * Minimal: `name`, `description`, `controller`, `state`
  * Full: `pin`, `trigger_time`
- `cabinet_style`
  * Minimal: `name`
  * Full: `police_panel_pin_1`, `police_panel_pin_2`, `watchdog_reset_pin_1`,
    `watchdog_reset_pin_2`, `dip`
- `comm_config`
  * Minimal: `name`, `description`
  * Full: `protocol`, `modem`, `timeout_ms`, `poll_period_sec`,
    `long_poll_period_sec`, `idle_disconnect_sec`, `no_response_disconnect_sec`
- `comm_link`
  * Minimal: `name`, `description`, `uri`, `comm_config`, `poll_enabled`,
    `connected`
- `controller`
  * Minimal: `name`, `drop_id`, `comm_link`, `cabinet_style`, `condition`,
    `notes`, `version`, `location`
  * Full: `geo_loc`, `password`, `fail_time`
