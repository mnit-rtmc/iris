# Natch Protocol

Natch is a protocol for communicating with ATC devices.  It may or may not stand
for New Advanced Traffic Controller, Howdy.

The controller listens and accepts a single TCP connection on port 8001.  The
IRIS server connects and keeps the same connection open continuously.

Immediately after connecting, IRIS will send configuration messages to the
controller.  These include ramp meter and detector configuration.

## Messages

Each message is a single line of comma-separated values (UTF-8), terminated
with a newline character `\n` (U+000A).

The first value in a message is a __code__ indicating a message type.  For a
poll (from IRIS) these codes are upper-case.  For responses or asynchronous
detector counts (from the controller), the codes are lower-case.

The second value is a __message identifier__.  It is used to match polls with
responses, but is otherwise not interpreted by the controller.  Asynchronous
detector messages use a sequence number which increments automatically.

Code | Descripton
-----|----------------
CS   | Clock status
DC   | Detector configure
DS   | Detector status
MC   | Meter configure
MS   | Meter status
PS   | Pin status
SC   | System command (restart)

### CS - Clock Status

The third value is date and time formatted according to [RFC 3339].  If the
third value is omitted, the response contains the current time.

```
CS,00AB,2021-04-01T12:34:56-05:00
cs,00AB,2021-04-01T12:34:56-05:00
CS,00AC
cs,00AC,2021-04-01T12:34:59-05:00
```

### DC - Detector Configure

The third value is the detector number (0-31).  The fourth value is the input
pin.  If the pin is not a valid input pin, the detector is *deleted*, and the
response indicates this with pin 0.  If the fourth value is omitted, it is
treated as a *query*, and the response includes the currently configured pin.

```
DC,00AD,0,39
dc,00AD,0,39
DC,00AE,0
dc,00AE,0,39
```

### DS - Detector Status

This message is sent by the controller when a vehicle leaves the detector.  The
messages are stored in a fixed-size array until IRIS sends a response, which
clears the message from the array.

The third through sixth values are __duration__, __headway__, __time__ and
__speed__.  See [vehicle logging] for details.

```
ds,01a5,323,4638,17:50:28
DS,01a5
```

### MC - Meter Configure

The third value is the meter number (0-3).  The fourth value is the number of
meter heads.  If any of the values are not valid, the meter is *deleted*, and
the response indicates this.  If there are only three values in the poll, it is
treated as a *query*, and the response includes the current meter configuration.

Value | Description
------|-------------------
3     | Meter number (0-3)
4     | Heads (0-2)
5     | Start up green time (0.1 sec)
6     | Start up yellow time (0.1 sec)
7     | Turn on output pin (usually 2 or 3)
8     | Red output pin, left head
9     | Yellow output pin, left head
10    | Green output pin, left head
11    | Red output pin, right head
12    | Yellow output pin, right head
13    | Green output pin, right head

```
MC,0150,0,2,13,7,2,4,5,6,7,8,9
mc,0150,0,2,13,7,2,4,5,6,7,8,9
MC,0151,0
mc,0151,0,2,13,7,2,4,5,6,7,8,9
MC,0152,1,0
mc,0152,1,0
```

### MS - Meter Status

The third value is the meter number.  The fourth is optional, if included, it
is the red dwell time.  If the red time is set to zero, metering is disabled.

```
MS,00AC,0
ms,00AC,0,45
```

### PS - Pin Status

The third value is the pin number.  The fourth, if included, is the value to set
an output pin (0 or 1).

```
PS,0250,70
ps,0250,70,0
PS,0251,19,1
ps,0251,19,1
```

### SC - System Command

The third value is the command.  `restart` causes the controller program to be
restarted.

```
SC,05c1,restart
sc,05c1,restart
```

[RFC 3339]: https://tools.ietf.org/html/rfc3339#section-5.6
[vehicle logging]: vehicle_detection.html#vehicle-logging
