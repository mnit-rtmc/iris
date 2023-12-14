# REST API

The REST API is provided by the `honeybee` and `graft` services.

## Requests

Data requests are split into *public* and *restricted* paths:

- `iris/`: Public data (no authentication)
- `iris/img/`: Public sign images (no authentication)
- `iris/api/`: Restricted data (needs session authentication)
- `iris/api/login`: Authentication endpoint
- `iris/api/access`: User's access [permission]s

## Public Resources

These resources are JSON arrays, fetched using http `GET` requests.

- `iris/camera_pub`: [Camera] locations and configuration
- `iris/detector_pub`: Vehicle detectors
- `iris/dms_message`: Current [DMS] messages and status
- `iris/dms_pub`: [DMS] locations and configuration
- `iris/incident`: Currently active incidents
- `iris/rwis`: [Road Weather Information System]
- `iris/sign_message`: Active DMS [sign message]s
- `iris/station_sample`: Vehicle detection station data
- `iris/system_attribute_pub`: Public [system attributes]
- `iris/TPIMS_archive`: Truck parking archive data
- `iris/TPIMS_dynamic`: Truck parking dynamic data
- `iris/TPIMS_static`: Truck parking static data

### Lookup Tables

These resources are static, and may only change on IRIS updates:

- `iris/beacon_state`: [Beacon] states
- `iris/comm_protocol`: Communication protocols
- `iris/condition`: [Controller] conditions
- `iris/direction`: Travel directions
- `iris/gate_arm_interlock`: Gate arm interlocks
- `iris/gate_arm_state`: Gate arm states
- `iris/lane_use_indication`: Lane use indications
- `iris/lcs_lock`: LCS lock codes
- `iris/resource_type`: [Resource types] available in `iris/api/`
- `iris/road_modifier`: Road modifiers

### Sign Images

The resources in `iris/img/` are GIF images of active [sign message]s from
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

- `GET iris/api/{type}`: Get a JSON array of all objects of `{type}`, with only
  *minimal* attributes -- those needed for searching and displaying compact
  cards.  The response also contains an ETag header, derived from the file's
  *modified* metadata, encoded in hexadecimal.
- `GET iris/api/{type}/{name}`: Get a single object as JSON, with *minimal* and
  *full* attributes
- `POST iris/api/{type}`: Create a new object of the `{type}`.  Body contains
                          required attributes as JSON
- `PATCH iris/api/{type}/{name}`: Update attributes of one object, with JSON
- `DELETE iris/api/{type}/{name}`: Delete one object

A `Content-Type: application/json` header is included where appropriate.

## Resource Types

| Access Control | Communication   | Devices       |                  |
|----------------|-----------------|---------------|------------------|
| [domain]       | [comm config]   | [alarm]       | [lcs]            |
| [permission]   | [comm link]     | [beacon]      | [ramp meter]     |
| [role]         | [controller]    | [camera]      | [tag reader]     |
| [user]         | [cabinet style] | [dms]         | [video monitor]  |
|                | [modem]         | [flow stream] | [weather sensor] |
|                |                 | [gate arm]    |                  |

### `detector`

| Access       | Minimal     | Full                       |
|--------------|-------------|----------------------------|
| 👁️  View      | name, label | auto\_fail                 |
| 👉 Operate   |             | field\_length, force\_fail |
| 💡 Manage    | notes       | abandoned                  |
| 🔧 Configure | controller  | pin, r\_node, lane\_code, lane\_number, fake |

### `geo_loc`

Since `geo_loc` resources can only be created and deleted with an associated
`resource_n`, there are only two valid endpoints:

- `GET iris/api/geo_loc/{name}`: Get a single object as JSON, with *minimal*
  and *full* attributes
- `PATCH iris/api/geo_loc/{name}`: Update attributes of one object, with JSON

| Access       | Minimal          | Full        |
|--------------|------------------|-------------|
| 👁️  View      | name             | resource\_n |
| 🔧 Configure | roadway, road\_dir, cross\_street, cross\_dir, cross\_mod, landmark | lat, lon |

### `gps`

| Access       | Minimal    | Full                                   |
|--------------|------------|----------------------------------------|
| 👁️  View      | name       | latest\_poll, latest\_sample, lat, lon |
| 💡 Manage    | notes      |                                        |
| 🔧 Configure | controller | pin                                    |

### `lane_marking`

| Access       | Minimal        | Full     |
|--------------|----------------|----------|
| 👁️  View      | name, location | geo\_loc |
| 👉 Operate   | deployed       |          |
| 💡 Manage    | notes          |          |
| 🔧 Configure | controller     | pin      |


[alarm]: alarms.html
[beacon]: beacons.html
[cabinet style]: controllers.html#cabinet-styles
[camera]: cameras.html
[comm config]: comm_config.html
[comm link]: comm_links.html
[controller]: controllers.html
[dms]: dms.html
[domain]: user_roles.html#domains
[flow stream]: flow_streams.html
[gate arm]: gate_arms.html
[lcs]: lcs.html
[modem]: modem.html
[permission]: permissions.html
[ramp meter]: ramp_meters.html
[resource types]: #resource-types
[Road Weather Information System]: rwis.html
[role]: user_roles.html#roles
[sign configuration]: sign_configuration.html
[sign detail]: sign_configuration.html#sign-details
[sign message]: sign_message.html
[system attributes]: system_attributes.html
[tag reader]: tolling.html#tag-readers
[user]: user_roles.html
[video monitor]: video.html
[weather sensor]: rwis.html
