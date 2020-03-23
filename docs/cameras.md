# Cameras

Video cameras with remote _pan_, _tilt_, and _zoom_ capability can be used to
monitor freeway conditions.  The **Camera** tab can display Motion JPEG (MJPEG)
video.  To view MPEG4 or h.264 video, the [MonStream] application is required.

## Encoder Types

Select `View ➔ Video ➔ Encoder Types` menu item

An encoder type represents a specific make and model of video encoder.  All
cameras with the same type share a common configuration.

Field    | Description
---------|------------------
Make     | Manufacturer name
Model    | Device model
Config   | Encoder configuration

### Streams

A _stream_ contains fields needed for consuming video from a camera.  Encoder
types can have multiple streams for differing quality.

For cameras with multiple **fixed-position** _views_, a number can be used to
identify them.

Field          | Description
---------------|-----------------------------------------------------------
View Num       | Fixed-position view, usable as a preset number
Flow stream    | If checked, the stream is a [flow stream] _sink_ from [streambed]
Encoding       | Stream encoding: `MJPEG`, `MPEG2`, `MPEG4`, `H264`, `AV1`
Quality        | Resolution and frame rate comparison: `Low` / `Medium` / `High`
URI scheme     | Scheme part of unicast request URI: `rtsp` / `http`
URI path       | Path part of unicast request URI.  Query parameters may be appended; use `{chan}` for camera's channel number.
Multicast port | Port for camera's [multicast] address
Latency        | Buffering latency (ms) for consuming stream

A stream can be either _unicast_ or _multicast_, but not both.  For a multicast
stream defined by an [SDP] file, specify the _URI scheme_ and _path_ instead of
_multicast port_.

## Setup

Select `View ➔ Video ➔ Cameras` menu item

The **Setup** tab within a camera properties form contains attributes to
configure the video encoder.

Field             | Description
------------------|-------------------------------------------------------
Cam Num           | Camera number, used for [keyboards](#camera-keyboards)
Encoder Type      | The type of video encoder
Encoder Address   | IP address for unicast streams (or [SDP] files)
Encoder Port      | Port number for unicast streams (overrides scheme default)
Multicast Address | IP address for [multicast] streams
Encoder Channel   | Channel number, for encoders which support multiple cameras
Publish           | Flag to allow public viewing of camera video
Streamable        | Flag to indicate whether stream is available

## Pan Tilt and Zoom

Many cameras have built-in pan/tilt/zoom (PTZ) functionality.  The following
operations can be performed:

* Panning or tilting the camera
* Zooming in or out
* Manual focus: near / far
* Manual iris: open / close
* Wiper control
* Recall [preset](#presets) positions
* Store [preset](#presets) positions

For PTZ control to function, the camera must be associated with a [controller]
on a PTZ protocol [comm link].

A selected camera can be panned and/or tilted by clicking on the video display
on the **Camera** tab.  The pan/tilt speed will vary depending on distance from
the mouse-click to the center of the image.  Similarly, using a mouse wheel on
the video display will cause the camera to zoom in or out.

Below the video display, there are also buttons for PTZ, as well as focus, iris
and wiper control.

## Presets

The **Preset** tab within a camera properties form can be used to configure up
to 12 presets per camera.  A preset can be associated with either a compass
direction or a device, such as [DMS] or [ramp meter].  To associate a preset
with a device, enable the preset, then select that preset on the **Location**
tab of the device's properties form.

If a stream is defined with a _view num_ that matches the _preset num_, that
stream will be selected instead of a typical PTZ preset.

## Selector Tool

The _selector tool_ is on the toolbar just below the map.  It has a text entry
field with a camera icon and label.  This tool enables quick switching of the
selected camera.

The keyboard _numpad_ can be used from anywhere within IRIS to control the
selector tool.  When the `Num Lock` key is off, all numpad keystrokes are
directed to the tool.  The `+` and `-` keys can select the next and previous
cameras, respectively.

Pressing the `.` key (on the numpad) changes to [video monitor] selection mode —
the icon will change to a monitor.  Pressing the `*` key (on the numpad) changes
to [play list] selection mode — the icon will change to a play list.

## Camera Keyboards

Dedicated keyboards are supported for easier camera control.  These keyboards
have joysticks for _pan / tilt / zoom_ control.  Two [protocol]s are supported:
[Pelco-P] and Panasonic CU-950.

To configure a [Pelco-P] keyboard, a [comm link] using the `pelcop` protocol
must be created.  One active controller must exist on that comm link, but no
devices are necessary.  IRIS will initiate a TCP connection to connect to the
keyboard — typically, this will be managed by an ethernet-to-serial device.

For Panasonic CU-950 keyboards, the `camera_kbd_panasonic_enable`
[system attribute] must be set to `true`.  This will cause IRIS to listen for
connections on TCP port 7001.  The keyboard must be configured with the IRIS
server's IP address.

## Video Servlet

The **video** servlet is a proxy server which provides MJPEG streams to IRIS
clients.  Properties in the [iris-client.properties] file are used to configure
use of the video servlet: `video.host`, `video.port`, and `district`.  When
specified, video requests will be made to
`http://[video.host]:[video.port]/video/stream/[district]/[camera_name]`.
Otherwise, requests will be made directly to the camera's encoder address.


[comm link]: comm_links.html
[controller]: controllers.html
[DMS]: dms.html
[flow stream]: flow_streams.html
[iris-client.properties]: client_properties.html
[MonStream]: video.html#monstream
[multicast]: https://en.wikipedia.org/wiki/Multicast_address
[Pelco-P]: comm_links.html#pelcop
[play list]: video.html#play-lists
[protocol]: comm_links.html#protocols
[ramp meter]: ramp_meters.html
[SDP]: https://en.wikipedia.org/wiki/Session_Description_Protocol
[streambed]: https://github.com/mnit-rtmc/streambed
[system attribute]: system_attributes.html
[video monitor]: video.html
