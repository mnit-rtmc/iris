# Flows

A **flow** is a video stream which is not provided directly by a [camera], but
by a server called [streambed].  Flows can be used to transcode from one video
encoding to another, overlay text, or rebroadcast a unicast RTSP stream to a
[multicast] address.

Streambed can run on one or more dedicated computers, and is controlled by IRIS
through the `streambed` [protocol].  A [controller] and associated [comm link]
must exist for each streambed server.  Each flow must be assigned to an [IO pin]
on a streambed controller.

## Configuration

Select `View ➔ Video ➔ Flows` menu item

To configure a flow, edit the first eight fields in the table.  A flow can be
either a _camera flow_ or _video monitor flow_, but not both.

Field            | Description
-----------------|----------------------------------------------------------
Flow             | Flow name
Location overlay | Flag indicating whether [camera] location should be added
View num         | Fixed position view number for [encoder type]
Quality          | Encoder [stream] quality
Camera           | [Camera] name
Monitor number   | [Video monitor] number
Address          | Monitor _sink_ address
port             | Monitor _sink_ port

## Status

The current flow status is displayed in the last 4 fields.

Field  | Description
-------|--------------------------------
State  | `STARTING`, `PLAYING`, `FAILED`
Pushed | Pushed packet count
Lost   | Lost packet count
Late   | Late packet count

## Camera Flows

A _camera flow_ uses a [camera] stream for its _source_.  The `camera` field
should be configured, but `monitor number`, `address` and `port` must be blank.

The camera's [encoder type] must contain two [stream]s with matching values for
`view num` and `quality`.  The `flow` field must be checked on one but not the
other.  They define the _sink_ (checked) and _source_ (unchecked) of the flow.

## Video Monitor Flows

A _video monitor flow_ uses a [video monitor] for its _source_ — more precisely,
the [camera] currently displayed on that monitor.  The `monitor number`,
`address` and `port` fields should be configured, but `camera` must be blank.

The _source_ is defined by the current camera displayed on the specified monitor
number.  That camera's [encoder type] must contain a [stream] with matching
`view num` and `quality`.  If multiple streams match, the one with `flow`
checked is used.

The `address` and `port` fields define the flow's _sink_.

## Transcoding

If the _sink_ encoding is different than the _source_, the flow will be
_transcoded_ by streambed.  Warning: transcoding requires more CPU time than
simply rebroadcasting.


[camera]: cameras.html
[comm link]: comm_links.html
[controller]: controllers.html
[encoder type]: cameras.html#encoder-types
[IO pin]: controllers.html#io-pins
[multicast]: https://en.wikipedia.org/wiki/Multicast_address
[protocol]: comm_links.html#protocols
[stream]: cameras.html#streams
[streambed]: https://github.com/mnit-rtmc/streambed
[video monitor]: video.html
