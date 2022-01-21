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

## Phase 1 — Segment Map

* Earthwyrm
  - [X] Serve MVT for OpenStreetMap layers
  - [X] Update to use [MuON] for configuration
* Honeybee
  - [X] Generate JSON for cameras, DMS, etc.
  - [X] Generate segment map layer in earthwyrm DB
* IRIS
  - [X] Generate JSON for station flow, speed and density
* Bulb
  - [X] Set up build using wasm-pack
  - [X] Integrate leaflet map
  - [X] Set up nginx configuration
  - [X] Style segment layer with detector data

## Phase 2 — Authentication

* graft
  - [X] Connect to IRIS server with sonar
  - [X] Add web service using tide
  - [X] Session authentication
* bulb
  - [ ] Handle authentication

## Phase 3 — Comm Link Administation

* graft
  - [X] Add comm config endpoints
  - [X] Add comm link endpoints
  - [X] Add controller endpoints
* bulb
  - [ ] Comm config page
  - [ ] Comm link page
  - [ ] Cabinet style page
  - [ ] Modem page
  - [ ] Controller page

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
