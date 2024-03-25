# REST API

The REST API is provided by the [honeybee] service.

```text
iris/
├── api/
├── gif/
├── img/
├── lut/
└── tfon/
```

### Public Resources: `iris/`

These are JSON arrays, fetched using http `GET` requests:

- `camera_pub`  [Camera] locations and configuration
- `detector_pub`  Vehicle [detector]s
- `dms_message`  [DMS] messages and status
- `dms_pub`  [DMS] locations and configuration
- `incident`  Active [incident]s
- `rwis`  [Road Weather Information System]
- `sign_message`  Active DMS [sign message]s
- `station_sample`  Vehicle [detector] station data
- `system_attribute_pub`  Public [system attributes]
- `TPIMS_archive`  Truck [parking] archive data
- `TPIMS_dynamic`  Truck [parking] dynamic data
- `TPIMS_static`  Truck [parking] static data

### Restricted Resources: `iris/api/`

- `login`  Authentication endpoint — `POST iris/api/login` with a JSON object
  containing `username` and `password` values.  A session cookie is created
  for subsequent restricted requests.
- `access`  Access [permission]s — `GET iris/api/access` returns a JSON array
  of [permission] records associated with the authenticated user's role.
- `notify`  Notification endpoint — `POST iris/api/notify` with a JSON array
  containing [channels] of interest.  To get notifications for a single object,
  append `$`_name_ to the channel name.  Using [SSE], a client can receive
  notifications by sending a `GET iris/api/notify` request, with [EventSource].

Restricted resources can be accessed using standard http methods.  Access to
these is restricted by session authentication.

- `GET iris/api/{type}`: Get a JSON array of all objects of `{type}`, with only
  *primary* attributes — those needed for searching and displaying compact
  cards.  The response also contains an [ETag] header, derived from the file's
  *modified* metadata, encoded in hexadecimal.
- `GET iris/api/{type}/{name}`: Get a single object as JSON, with *primary* and
  *secondary* attributes
- `POST iris/api/{type}`: Create a new object of the `{type}`.  Body contains
                          required attributes as JSON
- `PATCH iris/api/{type}/{name}`: Update attributes of one object, with JSON
- `DELETE iris/api/{type}/{name}`: Delete one object

A `Content-Type: application/json` header is included where appropriate.

#### Resource Types

| Access Control | Communication   | Devices    |                  |
|----------------|-----------------|------------|------------------|
| [domain]       | [comm config]   | [alarm]    | [gps]            |
| [permission]   | [comm link]     | [beacon]   | [lcs]            |
| [role]         | [controller]    | [camera]   | [ramp meter]     |
| [user]         | [cabinet style] | [detector] | [tag reader]     |
|                | [modem]         | [dms]      | [video monitor]  |
|                |                 | [gate arm] | [weather sensor] |

Most devices also have an associated [geo loc] resource.

### Static Graphics: `iris/gif/`

[Graphics] which can be used in sign messages are stored here.

### Sign Images: `iris/img/`

These are public GIF images of active [sign message]s from `iris/sign_message`.
They are rendered to appear as the entire face of a sign, with multi-page
messages as animated GIFs.

### Lookup Tables: `iris/lut/`

These are static resources which may only change on IRIS updates:

- `beacon_state`  [Beacon] states
- `comm_protocol`  Communication [protocols]
- `condition`  [Controller] conditions
- `direction`  Travel directions
- `gate_arm_interlock`  [Gate arm] interlocks
- `gate_arm_state`  [Gate arm] states
- `lane_use_indication`  [LCS] lane use indications
- `lcs_lock`  [LCS] lock codes
- `resource_type`  [Resource types] available in `iris/api/`
- `road_modifier`  Road modifiers

### Static Fonts: `iris/tfon/`

[Font]s which can be used in sign messages are stored here.


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
[font]: fonts.html
[gate arm]: gate_arms.html
[geo loc]: geo_loc.html
[gps]: gps.html
[graphics]: graphics.html
[honeybee]: https://github.com/mnit-rtmc/iris/tree/master/honeybee
[incident]: incidents.html
[lcs]: lcs.html
[modem]: modem.html
[parking]: parking_areas.html
[permission]: permissions.html
[protocols]: protocols.html
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
