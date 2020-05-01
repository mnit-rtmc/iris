# Flow Streams

A **flow stream** is a video stream which is not provided directly by a
[camera], but by a [streambed] server.  They can be used to transcode from one
video encoding to another, overlay text, or rebroadcast a unicast RTSP stream to
a [multicast] address (with RTP).

Streambed can run on one or more dedicated computers, and is controlled by IRIS
through the `streambed` [protocol].  A [controller] and associated [comm link]
must exist for each streambed server.  Each flow stream must be assigned to an
[IO pin] on a streambed controller.

## Configuration

Select `View ➔ Video ➔ Flow Streams` menu item

To configure a flow stream, edit the first eight fields in the table.  The
_source_ can be a [camera] or [video monitor], but not both.

Field            | Description
-----------------|----------------------------------------------------------
Flow stream      | Flow stream name
Restricted       | Flag restricting flow to only published cameras
Location overlay | Flag indicating whether [camera] location should be added
Quality          | Encoder [stream] quality
Camera           | _Source_ [Camera] name
Monitor num      | _Source_ [Video monitor] number
Address          | _Sink_ address
Port             | _Sink_ port
Status           | `STARTING`, `PLAYING`, `FAILED`

## Camera Source

A [camera] can be used as the _source_ of a flow stream.  The `camera` field
should be configured, but `monitor num` must be blank.

The camera's [encoder type] must contain a [stream] with the same `quality`
value, but with `flow stream` unchecked.  That stream defines the _source_.

## Camera Sink

With a camera source, the _sink_ is normally defined by the camera's encoder
type.  It must contain a stream with `flow stream` checked and a `quality`
that matches the flow stream.

If the sink _encoding_ is different than the source encoding,  the flow stream
will be _transcoded_.  Warning: this requires more CPU time than simply
rebroadcasting.

The camera sink uses RTP, sent to the camera's multicast address with the
stream's multicast port.

## Video Monitor Source

A [video monitor] can be used as a flow stream _source_ — more precisely, what's
being displayed on that monitor.  The `monitor num` field should be configured,
but `camera` must be blank.

The _source_ is defined by the current camera displayed on the specified monitor
number.  That camera's [encoder type] must contain a [stream] with the same
`quality` value.  If multiple streams match, the one with `flow stream` checked
is used.

## Static Sink

For either type of source, if `address` and `port` are specified, they define a
static _sink_, using RTP.

A static sink encoding is:
* same as the source encoding for a _camera source_
* _h.264_ for a _video monitor source_


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
