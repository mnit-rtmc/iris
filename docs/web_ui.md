# Web User Interface

This document tracks progress on the web-based user interface for IRIS.

![ui architecture](images/ui_architecture.svg)

## earthwyrm

[Earthwyrm] is a web mapping service for [MVT], or Mapbox Vector Tiles.  Layers
can be made from [OpenStreetMap] data in addition to IRIS devices, such as DMS.

## honeybee

Honeybee is a web service for JSON data in addition to rendered GIF images of
DMS.  It is included in the IRIS repository.

## bulb

Bulb is the web front-end for IRIS.  The mapping portion uses the [Leaflet]
JavaScript library.  The rest of the code is written in Rust, compiled as
WebAssembly.

## Progress to first-stage DMS control

honeybee

- [X] Generate JSON for cameras, DMS, etc.
- [X] Generate GIF images for DMS
- [ ] Turn into web service (warp)
- [ ] Send SSE for DMS notifications
- [ ] generate DMS outlines for osm layer
- [ ] connect to IRIS server with sonar
- [ ] authentication using sonar

earthwyrm
- [X] Serve MVT for OpenStreetMap layers
- [ ] update to user Muon for config

bulb
- [ ] integrate leaflet map
- [ ] connect to honeybee for SSE
- [ ] ui for DMS viewing / control


[earthwyrm]: https://github.com/DougLau/earthwyrm
[Leaflet]: https://github.com/Leaflet/Leaflet
[MVP]: https://docs.mapbox.com/vector-tiles/reference/
[OpenStreetMap]: https://www.openstreetmap.org
