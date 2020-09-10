# Web User Interface

This document tracks progress on the web-based user interface for IRIS.

![ui architecture](images/ui_architecture.svg)

## Phase 1 Progress (DMS control)

### earthwyrm

[Earthwyrm] is a web mapping service for [MVT], or Mapbox Vector Tiles.  Layers
can be made from [OpenStreetMap] data in addition to IRIS devices, such as DMS.

- [X] Serve MVT for OpenStreetMap layers
- [ ] Update to user Muon for config

### honeybee

Honeybee is a web service for JSON data in addition to rendered GIF images of
DMS.  It is included in the IRIS repository.

- [X] Generate JSON for cameras, DMS, etc.
- [X] Generate GIF images for DMS
- [ ] Turn into web service (warp)
- [ ] Send SSE for DMS notifications
- [ ] Generate DMS outlines for osm layer
- [ ] Connect to IRIS server with sonar
- [ ] Authentication using sonar

## bulb

Bulb is the web front-end for IRIS.  The mapping portion uses the [Leaflet]
JavaScript library.  The rest of the code is written in Rust, compiled as
WebAssembly.

- [X] Set up build using wasm-pack
- [ ] Integrate leaflet map
- [ ] Connect to honeybee for SSE
- [ ] UI for DMS viewing / control
- [ ] Generate DMS previews and insert into img element using data URI


[earthwyrm]: https://github.com/DougLau/earthwyrm
[Leaflet]: https://github.com/Leaflet/Leaflet
[MVT]: https://docs.mapbox.com/vector-tiles/reference/
[OpenStreetMap]: https://www.openstreetmap.org
