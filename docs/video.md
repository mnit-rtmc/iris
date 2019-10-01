# Video Monitors

Select `View ➔ Video ➔ Video Monitor` menu item

IRIS can be configured to send commands to a video switcher system, such as
[MonStream].  This allows camera streams to be routed to any number of video
monitors.  Each monitor can be assigned to display the video from any camera.

## Monitor Selection

On the upper-right of the **Camera** tab, there is a monitor selector.  The
chosen monitor will switch whenever the user selects a new camera.

## MonStream

[MonStream] is a full-screen video streaming application which runs on low-cost
Linux computers.  IRIS can send switching messages to MonStream, creating a
low-latency IP video system.

A computer running MonStream can be configured to stream a grid of four or more
video feeds onto a single large monitor.  The only configuration required on the
MonStream computer is to grant access to the `/var/lib/monstream/` directory for
the UID of the monstream process.

A [controller] must be configured to represent each MonStream computer.  It will
need to be on a [comm link] using the MonStream protocol, with a timeout of 2000
ms.  The comm link URI should be of the form: `[ip address]:7001`.  Each video
monitor to be displayed should be associated with an IO pin of the controller.
For example, a quad-screen monitor would have monitors associated with pins 1
thru 4.

## Play Lists

Select `View ➔ Video ➔ Play Lists` menu item

Play lists can be created to cycle through related cameras.  Each user can have
a personal play list, but system play lists are available to all users.
Selecting a play list on a monitor will cause the play list to automatically
switch.  The interval is specified by the `camera_sequence_dwell_sec`
[system attribute].

## Catalogs

A catalog is a collection of play lists.  Both play lists and catalogs can be
assigned sequence numbers, so they can be used in the same way.


[comm link]: comm_links.html
[controller]: controllers.html
[MonStream]: https://github.com/mnit-rtmc/monstream
[system attribute]: system_attributes.html
