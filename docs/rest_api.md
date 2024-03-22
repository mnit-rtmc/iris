# REST API

The REST API is provided by the [honeybee] service.

## Requests

Data requests are split into *public* and *restricted* paths:

- `iris/`: Public resources / lookup tables (no authentication)
- `iris/img/`: Public sign images (no authentication)
- `iris/api/`: Restricted data (needs session authentication)
- `iris/api/login`: Authentication endpoint
- `iris/api/access`: User's access [permission]s
- `iris/api/notify`: [Event notification](#event-notification) endpoint

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
  *primary* attributes -- those needed for searching and displaying compact
  cards.  The response also contains an [ETag] header, derived from the file's
  *modified* metadata, encoded in hexadecimal.
- `GET iris/api/{type}/{name}`: Get a single object as JSON, with *primary* and
  *secondary* attributes
- `POST iris/api/{type}`: Create a new object of the `{type}`.  Body contains
                          required attributes as JSON
- `PATCH iris/api/{type}/{name}`: Update attributes of one object, with JSON
- `DELETE iris/api/{type}/{name}`: Delete one object

A `Content-Type: application/json` header is included where appropriate.

## Resource Types

| Access Control | Communication   | Devices    |                  |
|----------------|-----------------|------------|------------------|
| [domain]       | [comm config]   | [alarm]    | [gps]            |
| [permission]   | [comm link]     | [beacon]   | [lcs]            |
| [role]         | [controller]    | [camera]   | [ramp meter]     |
| [user]         | [cabinet style] | [detector] | [tag reader]     |
|                | [modem]         | [dms]      | [video monitor]  |
|                |                 | [gate arm] | [weather sensor] |

Most devices also have an associated [geo loc] resource.

## Event Notification

Clients can subscribe to [channels] by sending a `POST iris/api/notify`, with
a JSON array containing channels of interest.  To get notifications for a
single object, append `$`_name_ to the channel name.

Using [SSE], a client can receive notifications by sending a
`GET iris/api/notify` request, with [EventSource].


[alarm]: alarms.html
[beacon]: beacons.html
[cabinet style]: controllers.html#cabinet-styles
[camera]: cameras.html
[channels]: https://mnit-rtmc.github.io/iris/database.html#channels
[comm config]: comm_config.html
[comm link]: comm_links.html
[controller]: controllers.html
[dependent resources]: permissions.html#dependent-resources
[detector]: vehicle_detection.html
[dms]: dms.html
[domain]: user_roles.html#domains
[etag]: https://en.wikipedia.org/wiki/HTTP_ETag
[EventSource]: https://developer.mozilla.org/en-US/docs/Web/API/EventSource
[gate arm]: gate_arms.html
[geo loc]: geo_loc.html
[gps]: gps.html
[honeybee]: https://github.com/mnit-rtmc/iris/tree/master/honeybee
[lcs]: lcs.html
[modem]: modem.html
[permission]: permissions.html
[ramp meter]: ramp_meters.html
[resource types]: #resource-types
[Road Weather Information System]: rwis.html
[role]: user_roles.html#roles
[sign message]: sign_message.html
[SSE]: https://en.wikipedia.org/wiki/Server-sent_events
[system attributes]: system_attributes.html
[tag reader]: tolling.html#tag-readers
[user]: user_roles.html
[video monitor]: video.html
[weather sensor]: rwis.html
