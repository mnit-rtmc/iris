# Web User Interface

This document tracks progress on the web-based user interface for IRIS.

![ui architecture](images/ui_architecture.svg)

## Major Systems

- [earthwyrm] — web mapping service for Mapbox Vector Tiles [MVT].  Layers can
  be made from [OpenStreetMap] data in addition to IRIS devices, such as DMS.
- [honeybee] — service for generating public JSON endpoints in addition to
  rendered GIF images of DMS.  It is included in the IRIS repository.
- [graft] — web service for proxying IRIS sonar objects, used for non-public and
  writable values.  It is included in the IRIS repository.
- [bulb] — the web front-end for IRIS.  The mapping portion uses the [Leaflet]
  JavaScript library.  The rest of the code is written in Rust, compiled as
  WebAssembly.

## Phase 1 — Segment Map (Complete)

The traffic data is displayed on a web-based segment map, served by Earthwyrm.

## Phase 2 — Proxying Sonar (Complete)

The graft service handles requests through Sonar, using session-based
authentication.

## Phase 3 — Maintenance Administration (in progress)

* graft
  - [X] Add routes for maintenance resources
  - [X] Date / time formatting (RFC 3339)
  - [ ] Verify user's domain on login
* bulb
  - [X] Resource type selector with card list
  - [X] Search filter
  - [X] Html escaping
  - [X] Maintenance object CRUD
  - [X] Status button / view
  - [ ] Feedback with "snackbar" popups
  - [ ] UI refinements
  - [ ] Authentication and session expiration

## Phase 4 — DMS control

* honeybee
  - [X] Generate GIF images for DMS
  - [ ] Generate DMS map layer in earthwyrm DB
* graft
  - [ ] Add endpoints for controlling DMS
  - [ ] Write full installation documentation
  - [ ] Send SSE for update notifications
* bulb
  - [ ] UI for DMS viewing / control
  - [ ] Generate DMS previews and insert into img element using data URI
  - [ ] Write full installation documentation
  - [ ] Connect to graft for SSE

## Other Bits

* graft
  - [ ] ETags for table versioning


[bulb]: https://github.com/mnit-rtmc/iris/tree/master/bulb
[earthwyrm]: https://github.com/DougLau/earthwyrm
[graft]: https://github.com/mnit-rtmc/iris/tree/master/graft
[honeybee]: https://github.com/mnit-rtmc/iris/tree/master/honeybee
[Leaflet]: https://github.com/Leaflet/Leaflet
[MuON]: https://github.com/muon-data/muon
[MVT]: https://docs.mapbox.com/vector-tiles/reference/
[OpenStreetMap]: https://www.openstreetmap.org
