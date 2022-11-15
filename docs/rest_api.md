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
- `iris/detector_pub`: Vehicle detectors
- `iris/dms_message`: Current DMS messages and status
- `iris/dms_pub`: DMS locations and configuration
- `iris/font`: Bitmapped fonts for DMS
- `iris/graphic`: Graphics for DMS
- `iris/incident`: Currently active incidents
- `iris/rwis`: Weather sensor data
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

- `iris/beacon_state`: Beacon states
- `iris/comm_protocol`: Communication protocols
- `iris/condition`: Controller conditions
- `iris/direction`: Travel directions
- `iris/gate_arm_interlock`: Gate arm interlocks
- `iris/gate_arm_state`: Gate arm states
- `iris/lane_use_indication`: Lane use indications
- `iris/lcs_lock`: LCS lock codes
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

*Full* attributes are only included in single object responses.

### `alarm`

| Access       | Attributes              | Full          |
|--------------|-------------------------|---------------|
| Read Only    | name, state             | trigger\_time |
| 🔧 Configure | description, controller | pin           |

### `beacon`

| Access       | Attributes     | Full                        |
|--------------|----------------|-----------------------------|
| Read Only    | name, location | geo\_loc                    |
| 👉 Operate   | state          |                             |
| 💡 Plan      | message, notes | preset                      |
| 🔧 Configure | controller     | pin, verify\_pin, ext\_mode |

### `cabinet_style`

| Access       | Attributes | Full |
|--------------|------------|------|
| Read Only    | name       |      |
| 🔧 Configure |            | police\_panel\_pin\_1, police\_panel\_pin\_2, watchdog\_reset\_pin\_1, watchdog\_reset\_pin\_2, dip |

### `camera`

| Access       | Attributes           | Full                  |
|--------------|----------------------|-----------------------|
| Read Only    | name, location       | geo\_loc, video\_loss |
| 👉 Operate   |                      | ptz                   |
| 💡 Plan      | notes, publish       | streamable            |
| 🔧 Configure | controller, cam\_num | pin, cam\_template, encoder\_type, enc\_address, enc\_port, enc\_mcast, enc\_channel

### `comm_config`

| Access       | Attributes  | Full |
|--------------|-------------|------|
| Read Only    | name        |      |
| 💡 Plan      |             | timeout\_ms, idle\_disconnect\_sec, no\_response\_disconnect\_sec |
| 🔧 Configure | description | protocol, modem, poll\_period\_sec, long\_poll\_period\_sec |

### `comm_link`

| Access       | Attributes                     | Full |
|--------------|--------------------------------|------|
| Read Only    | name, connected                |      |
| 💡 Plan      | poll\_enabled                  |      |
| 🔧 Configure | description, uri, comm\_config |      |

### `controller`

| Access       | Attributes                           | Full     |
|--------------|--------------------------------------|----------|
| Read Only    | name, location, setup, fail\_time    | geo\_loc |
| 👉 Operate   |                                      | download, device\_req |
| 💡 Plan      | condition, notes                     |          |
| 🔧 Configure | comm\_link, drop\_id, cabinet\_style | password |

Also, a read only `controller_io` resource is available with
`GET iris/api/controller_io/{name}`.  It contains an array of objects consisting
of `pin`, `resource_n` and `name`.

### `detector`

| Access       | Attributes  | Full                       |
|--------------|-------------|----------------------------|
| Read Only    | name, label | auto\_fail                 |
| 👉 Operate   |             | field\_length, force\_fail |
| 💡 Plan      | notes       | abandoned                  |
| 🔧 Configure | controller  | pin, r\_node, lane\_code, lane\_number, fake |

### `dms`

| Access       | Attributes                   | Full      |
|--------------|------------------------------|-----------|
| Read Only    | name, location, msg\_current | sign\_config, sign\_detail, geo\_loc, msg\_sched |
| 👉 Operate   |                              | msg\_user |
| 💡 Plan      | notes                        |           |
| 🔧 Configure | controller                   | pin       |

### `flow_stream`

| Access       | Attributes | Full                       |
|--------------|------------|----------------------------|
| Read Only    | name       | status                     |
| 👉 Operate   |            | camera, mon\_num           |
| 💡 Plan      |            | restricted, address, port  |
| 🔧 Configure | controller | pin, loc\_overlay, quality |

### `gate_arm`

| Access       | Attributes                 | Full                  |
|--------------|----------------------------|-----------------------|
| Read Only    | name, location, arm\_state | ga\_array, idx, fault |
| 💡 Plan      | notes                      |                       |
| 🔧 Configure | controller                 | pin                   |

### `gate_arm_array`

| Access       | Attributes                | Full     |
|--------------|---------------------------|----------|
| Read Only    | name, location, interlock | geo\_loc |
| 👉 Operate   | arm\_state                |          |
| 💡 Plan      | notes                     |          |
| 🔧 Configure |                           | opposing, prereq, camera, approach, action\_plan |

### `geo_loc`

| Access       | Attributes       | Full        |
|--------------|------------------|-------------|
| Read Only    | name             | resource\_n |
| 🔧 Configure | roadway, road\_dir, cross\_street, cross\_dir, cross\_mod, landmark | lat, lon |

Since `geo_loc` resources are only created and deleted with an associated
`resource_n`, there are only two valid endpoints:

- `GET iris/api/{type}/{name}`: Get one full object as JSON
- `PATCH iris/api/{type}/{name}`: Update attributes of one object, with JSON

### `gps`

| Access       | Attributes | Full                                   |
|--------------|------------|----------------------------------------|
| Read Only    | name       | latest\_poll, latest\_sample, lat, lon |
| 💡 Plan      | notes      |                                        |
| 🔧 Configure | controller | pin                                    |

### `lane_marking`

| Access       | Attributes     | Full     |
|--------------|----------------|----------|
| Read Only    | name, location | geo\_loc |
| 👉 Operate   | deployed       |          |
| 💡 Plan      | notes          |          |
| 🔧 Configure | controller     | pin      |

### `lcs_array`

| Access       | Attributes | Full  |
|--------------|------------|-------|
| Read Only    | name       |       |
| 👉 Operate   | lcs\_lock  |       |
| 💡 Plan      | notes      |       |
| 🔧 Configure |            | shift |

### `lcs_indication`

| Access       | Attributes            | Full |
|--------------|-----------------------|------|
| Read Only    | name, lcs, indication |      |
| 🔧 Configure | controller            | pin  |

### `modem`

| Access       | Attributes | Full        |
|--------------|------------|-------------|
| Read Only    | name       |             |
| 💡 Plan      | enabled    | timeout\_ms |
| 🔧 Configure |            | uri, config |

### `permission`

| Access       | Attributes                          | Full |
|--------------|-------------------------------------|------|
| Read Only    | id                                  |      |
| 🔧 Configure | role, resource\_n, batch, access\_n |      |

### `ramp_meter`

| Access       | Attributes     | Full                             |
|--------------|----------------|----------------------------------|
| Read Only    | name, location | geo\_loc                         |
| 👉 Operate   |                | m\_lock, rate                    |
| 💡 Plan      | notes          | storage, max\_wait, algorithm, am\_target, pm\_target |
| 🔧 Configure | controller     | pin, meter\_type, beacon, preset |

### `role`

| Access       | Attributes | Full |
|--------------|------------|------|
| Read Only    | name       |      |
| 💡 Plan      | enabled    |      |

### `tag_reader`

| Access       | Attributes     | Full       |
|--------------|----------------|------------|
| Read Only    | name, location | geo\_loc   |
| 💡 Plan      | notes          | toll\_zone |
| 🔧 Configure | controller     | pin        |

### `user`

| Access       | Attributes       | Full |
|--------------|------------------|------|
| Read Only    | name             |      |
| 💡 Plan      | enabled          |      |
| 🔧 Configure | full\_name, role |      |

### `video_monitor`

| Access       | Attributes           | Full                       |
|--------------|----------------------|----------------------------|
| Read Only    | name                 |                            |
| 👉 Operate   |                      | camera                     |
| 💡 Plan      | notes                | restricted, monitor\_style |
| 🔧 Configure | mon\_num, controller | pin                        |

### `weather_sensor`

| Access       | Attributes               | Full |
|--------------|--------------------------|------|
| Read Only    | name, location           | geo\_loc, settings, sample, sample\_time |
| 💡 Plan      | site\_id, alt\_id, notes |      |
| 🔧 Configure | controller               | pin  |


[permission]: #permission
[resource types]: #resource-types
[role]: #role
[user]: #user
