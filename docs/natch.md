# Natch Protocol

Natch is a protocol for communicating with ATC devices.  It may or may not stand
for New Advanced Traffic Controller, Howdy.

The controller listens and accepts a single TCP connection on port 8001.  The
IRIS server connects and keeps the same connection open continuously.

Immediately after connecting, IRIS will send configuration messages to the
controller.  These include ramp meter and detector configuration.

## Messages

Each message is a single line of comma-separated parameters (UTF-8), terminated
with a newline character `\n` (U+000A).  General message format:

Parameter | Description
----------|------------
1         | Code
2         | Message ID
…         | Remaining parameters

The __code__ indicates the message type.  For a poll (from IRIS) these codes are
upper-case.  For responses or asynchronous detector counts (from the
controller), the codes are lower-case.

Code | Descripton
-----|----------------
`CS` | Clock status
`DC` | Detector configure
`DS` | Detector status
`MC` | Meter configure
`MS` | Meter status
`MT` | Meter timing table
`PS` | Pin status
`SA` | System attributes
`SC` | System command

The __message identifier__ is used to match polls with responses, but is
otherwise not interpreted by the controller (except for `ds` messages, see
below).

### CS - Clock Status

Parameter | Description
----------|------------------
1         | Code: `CS` / `cs`
2         | Message ID
3         | Date/time

The date/time is formatted according to [RFC 3339].  If it is omitted in a poll,
the response contains the current time.

```
CS,00AB,2021-04-01T12:34:56-05:00
cs,00AB,2021-04-01T12:34:56-05:00
CS,00AC
cs,00AC,2021-04-01T12:34:59-05:00
```

### DC - Detector Configure

Parameter | Description
----------|------------------
1         | Code: `DC` / `dc`
2         | Message ID
3         | Detector Number (0-31)
4         | Input Pin Number (0 means deleted)

If the pin number is not a valid input pin, the detector is *deleted*, and this
is indicated in the response.

If the fourth parameter is omitted in a poll, it is treated as a *query*, and
the response includes the currently configured pin.

The fourth parameter can also be set to a ramp meter output pin (2 or 3).  In
this case, whenever a green indication (on either head) is displayed, it
generates a detector status event.

```
DC,00AD,0,39
dc,00AD,0,39
DC,00AE,0
dc,00AE,0,39
```

### DS - Detector Status

Parameter | Description
----------|------------------
1         | Code: `DS` / `ds`
2         | Message ID
3         | Detector Number (0-31)
4         | Duration (ms)
5         | Headway (ms)
6         | Time (`HH:MM:SS`, local 24-hour)

When a vehicle leaves a detector, a detector status messages is added to a
fixed-size ring buffer, with head and tail pointers.  The __message identifier__
is incremented as a 16-bit hex value.

When this happens, the oldest message is sent by the controller, unless waiting
for an ACK from a previous message.

When a poll is received, the __message identifier__ is compared with the last
sent status message.  If it matches, the poll is an ACK, otherwise it is a NAK.

On ACK, the tail pointer is incremented, "deleting" the message.  If there are
more messages in the ring buffer, the oldest one is then sent.

On NAK, the oldest status message is sent again.

See [vehicle logging] for details on __duration__, __headway__ and __time__.

```
ds,01a5,3,323,4638,17:50:28
DS,01a5
```

### MC - Meter Configure

Parameter | Description
----------|-------------------
1         | Code: `MC` / `mc`
2         | Message ID
3         | Meter number (0-3)
4         | Heads (0: deleted, 1: single, 2: dual)
5         | Release (0: alternating, 1: simultaneous / drag-race)
6         | Turn on output pin (usually 2 or 3)
7         | Red output pin, left head
8         | Yellow output pin, left head
9         | Green output pin, left head
10        | Red output pin, right head
11        | Yellow output pin, right head
12        | Green output pin, right head

If any of the parameters are not valid, the meter is *deleted*, zeroing out
values 4-12, and the response indicates this.  If there are only three
parameters in the poll, it is treated as a *query*, and the response includes
the current meter configuration.

```
MC,0150,0,2,0,2,4,5,6,7,8,9
mc,0150,0,2,0,2,4,5,6,7,8,9
MC,0151,0
mc,0151,0,2,0,2,4,5,6,7,8,9
MC,0152,1,0
mc,0152,1,0,0,0,0,0,0,0,0,0
```

### MS - Meter Status

Parameter | Description
----------|-------------------
1         | Code: `MS` / `ms`
2         | Message ID
3         | Meter number (0-3)
4         | Red dwell time (0.1 sec), 

If red dwell time is set to zero, metering is disabled.  If there are only
three parameters in a poll, it is treated as a *query*, and the response
includes the current red dwell time.

```
MS,00AC,0
ms,00AC,0,45
```

### MT - Meter Timing Table

Parameter | Description
----------|--------------------------
1         | Code: `MT` / `mt`
2         | Message ID
3         | Table entry number (0-15)
4         | Meter number (0-3)
5         | Start time (minute of day; 0-1439)
6         | Stop time (minute of day; 0-1439)
7         | Red dwell time (0.1 sec)

If any of the parameters are not valid, the table entry is *deleted*, zeroing
out values 4-7, and the response indicates this.  If there are only three
parameters in the poll, it is treated as a *query*, and the response includes
the current table entry configuration.

These meter timing values take effect when there has been no successful
communication for longer than the **Comm fail time** system attribute.

```
MT,0234,0
mt,0234,0,1,420,510,65
MT,0235,1,1,900,1080,73
mt,0235,1,1,900,1080,73
MT,0236,2,XX
mt,0235,2,0,0,0,0
```

### PS - Pin Status

Parameter | Description
----------|--------------------------
1         | Code: `PS` / `ps`
2         | Message ID
3         | Pin number
4         | Pin status (0 or 1)

If there are only three parameters in a poll, it is treated as a *query*, and
the response includes the current status.

__Note__: pins associated with meters (Meter Configure) cannot be controlled
with this command.

```
PS,0250,70
ps,0250,70,0
PS,0251,19,1
ps,0251,19,1
```

### SA - System Attributes

Parameter | Description
----------|------------------------------------------
1         | Code: `SA` / `sa`
2         | Message ID
3         | Comm fail time (0.1 sec), default 1800
4         | Start up green time (0.1 sec), default 80
5         | Start up yellow time (0.1 sec), default 50
6         | Metering green time (0.1 sec), default 13
7         | Metering yellow time (0.1 sec), default 7

The meter timing attributes apply to all meters.  If only the code and
identifier are included in the poll, it is treated as a *query*, and the
response includes the current attributes.

```
SA,0291
sa,0291,1800,80,50,13,7
SA,0292,1200,80,50,12,8
sa,0292,1200,80,50,12,8
```

### SC - System Command

Parameter | Description
----------|------------------------------------------
1         | Code: `SC` / `sc`
2         | Message ID
3         | Command

The command `restart` causes the controller program to be restarted.

```
SC,05c1,restart
sc,05c1,restart
```

[RFC 3339]: https://tools.ietf.org/html/rfc3339#section-5.6
[vehicle logging]: vehicle_detection.html#vehicle-logging
