# Video Monitors

A _video monitor_ is a dedicated screen which can display [camera] streams in
real time.  IRIS can quickly switch the stream and title text displayed on a
video monitor, using supported [protocol]s.

## MonStream

[MonStream] is a full-screen video streaming application which runs on low-cost
Linux computers.  IRIS has a _MonStream_ [protocol] driver, which can be used
for video monitor control.

A computer running MonStream can be configured to stream a grid of four or more
video feeds onto a single large monitor.  The only configuration required on the
MonStream computer is to grant access to the `/var/lib/monstream/` directory for
the UID of the monstream process.

A [controller] must be configured to represent each MonStream computer.  It will
need to be on a [comm link] using the MonStream protocol, with a timeout of 2000
ms.  The comm link URI should be of the form: `[ip address]:7001`.  Each video
monitor to be displayed should be associated with an [IO pin] of the controller.
For example, a quad-screen monitor would have monitors associated with pins 1
thru 4.

## Configuration

Select `View ➔ Video ➔ Video Monitor` menu item

Each row of the table represents one _video monitor_.

Field         | Description
--------------|---------------------------------------------------
Name          | Video monitor name
Notes         | Administrator notes, possibly including [hashtag]s
Monitor num   | Number for selecting and switching
Restricted    | Flag restricting monitor to published cameras only
Monitor style | [Style](#style) of title bar, _etc_

Hashtags in notes allow for restricting [permissions] for specific roles.

The `monitor num` is used to identify monitors when selecting [camera]s with
a [switching](#switching) system.

<details>
<summary>API Resources 🕵️ </summary>

* `iris/api/video_monitor` (primary)
* `iris/api/video_monitor/{name}`

| Access       | Primary                     | Secondary                  |
|--------------|-----------------------------|----------------------------|
| 👁️  View      | name                        |                            |
| 👉 Operate   |                             | camera, device\_request †  |                    |
| 💡 Manage    |                             | restricted, monitor\_style |
| 🔧 Configure | mon\_num, controller, notes | pin                        |

† _Write only_

</details>

## Style

Select `View ➔ Video ➔ Monitor Styles` menu item

Field         | Description
--------------|---------------------------------------------------
Monitor Style | Monitor style name
Force aspect  | Flag to preserve video aspect ratio
Accent        | Hexadecimal RGB color of title bar
Font Size     | Size of title bar font (points)
Title Bar     | Flag to enable title bar
Auto Expand   | Flag to use full screen when only one monitor is active

<details>
<summary>API Resources 🕵️ </summary>

* `iris/api/monitor_style` (primary)
* `iris/api/monitor_style/{name}`

| Access       | Primary | Secondary |
|--------------|---------|-----------|
| 👁️  View      | name    |           |
| 🔧 Configure |         | force\_aspect, accent, font\_sz, title\_bar, auto\_expand, hgap, vgap |

</details>

## Switching

There are several methods available for switching video monitors.  Using any of
these methods, when a [camera] is assigned to a `monitor num`, **all** monitors
and [flow streams] with that number will be switched.

### Camera Tab

On the upper-right of the **Camera** tab, there is a `monitor num` selector.
The number chosen here will be switched any time a camera is selected, either
from the map or a list.

The [selector tool] can also be used to change the `monitor num` or camera.

### Keyboards

Certain [camera keyboards] can be used for video monitor switching.

### MonStream Control

With a USB joystick and keypad, a [MonStream](#monstream) computer can be used
for switching.

## Play Lists

Select `View ➔ Video ➔ Play Lists` menu item

Play lists can be created to quickly cycle through [camera]s.  Selecting a
play list on a monitor will cause the cameras to automatically switch after
a short dwell time, specified by the `camera_playlist_dwell_sec`
[system attribute].  A **seq num** is a unique number to select the list.

A **meta** play list consists of (non-meta) sub-lists.  This allows lists to
be broken up and shared with other meta lists.

Each role can have one _scratch_ play list:
* Must have 💡 Manage [permissions] for `video_monitor`, with a [hashtag]
* Exactly one play list must have that hashtag

<details>
<summary>API Resources 🕵️ </summary>

* `iris/api/play_list` (primary)
* `iris/api/play_list/{name}`

| Access       | Primary         | Secondary |
|--------------|-----------------|-----------|
| 👁️  View      | name            |           |
| 💡 Manage    |                 | entries   |
| 🔧 Configure | seq\_num, notes | meta      |

</details>


[camera]: cameras.html
[camera keyboards]: cameras.html#camera-keyboards
[comm link]: comm_links.html
[controller]: controllers.html
[flow streams]: flow_streams.html
[hashtag]: hashtags.html
[IO pin]: controllers.html#io-pins
[MonStream]: https://github.com/mnit-rtmc/monstream
[permissions]: permissions.html
[protocol]: protocols.html
[selector tool]: cameras.html#selector-tool
[system attribute]: system_attributes.html
