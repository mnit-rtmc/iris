# Natch Protocol

Natch is a protocol for communicating with ATC devices.  It may or may not stand
for New Advanced Traffic Controller, Howdy.

The controller listens and accepts a single TCP connection on port 8001.  The
IRIS server connects and keeps the same connection open continuously.

After connecting, IRIS will send configuration messages for enabled detectors
and ramp meters.

## Messages

Each message is a single line of comma-separated parameters (UTF-8), terminated
with a newline character `\n` (U+000A).  General message format:

Parameter | Description
----------|------------
1         | Code
2         | Message ID
…         | Remaining parameters

__Code__ indicates the message type.  For polls (from IRIS) these are
upper-case, but for responses or asynchronous detector status (from the
controller), they are lower-case.

Code        | Descripton
------------|----------------
`CS` / `cs` | Clock status
`DC` / `dc` | Detector configure
`DS` / `ds` | Detector status
`MC` / `mc` | Meter configure
`MS` / `ms` | Meter status
`MT` / `mt` | Meter timing table
`PS` / `ps` | Pin status
`SA` / `sa` | System attributes
`SC` / `sc` | System command
`V.` / `v.` | Firmware version

__Message ID__ is used to match polls with responses, but is otherwise not
interpreted by the controller (except for `ds` messages, see below).

Polls can be made in two ways:

- __store__: Includes all message parameters.
- __query__: Only includes parameters without a dagger (†).

Responses from the controller include all parameters, for both __store__ and
__query__ polls.

### CS - Clock Status

Parameter | Description
----------|------------------
1         | Code: `CS` / `cs`
2         | Message ID
3 †       | Date / time

The date / time is formatted according to [RFC 3339].

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
4 †       | Input Pin Number (0-104; 0 means deleted)

If the input pin number is not valid, the detector is *deleted*, and this is
indicated in the response.

The input pin number can also be set to a ramp meter output pin (2 or 3).  In
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
3 †       | Detector Number (0-31)
4 †       | Duration (ms)
5 †       | Headway (ms)
6 †       | Time (`HH:MM:SS`, local 24-hour)

The detector status message works differently from the others; there are no
store/query polls.  Instead, messages are initiated by the controller, and IRIS
responds with an ACK or NAK.

Vehicle events waiting to be sent to IRIS are stored in a buffer of `ds`
messages.  It is fixed-size, with *head* and *tail* pointers.  *Head* is
incremented when a new message is added, and *tail* is incremented when the
oldest message is deleted.

A one second *buffer timer* is used to delay sending `ds` messages to IRIS.
After it expires, up to 24 buffered messages are sent, in order from oldest to
newest.

When a vehicle leaves a detector, the __message ID__ is incremented as a 4-digit
hex value.  A `ds` message is added to the buffer, and if the *buffer timer* is
not already running, it is started.

When a `DS` is received from IRIS, the __message ID__ is compared with the
oldest `ds` message in the buffer.  If it matches, the poll is an ACK, otherwise
it is a NAK.  In either case, the *buffer timer* is reset.

- On ACK, the oldest message is *deleted* by incrementing the *tail* pointer.
- On NAK, the message is ignored.

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
4 †       | Heads (0: deleted, 1: single, 2: dual)
5 †       | Release (0: alternating, 1: simultaneous / drag-race)
6 †       | Turn on output pin (usually 2 or 3)
7 †       | Left head, red output pin (1-104)
8 †       | Left head, yellow output pin (1-104)
9 †       | Left head, green output pin (1-104)
10 †      | Right head, red output pin (1-104)
11 †      | Right head, yellow output pin (1-104)
12 †      | Right head, green output pin (1-104)

If any of the parameters are not valid, the meter is *deleted*, zeroing out
values 4-12, and the response indicates this.

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
4 †       | Red dwell time (0.1 sec)

If red dwell time is set to zero, metering is disabled.  If the meter is not
configured, red dwell time in a response will be `INV` (invalid).

```
MS,00AC,0
ms,00AC,0,45
MS,00AD,1
ms,00AD,1,INV
```

### MT - Meter Timing Table

Parameter | Description
----------|--------------------------
1         | Code: `MT` / `mt`
2         | Message ID
3         | Table entry number (0-15)
4 †       | Meter number (0-3)
5 †       | Start time (minute of day; 0-1439)
6 †       | Stop time (minute of day; 0-1439)
7 †       | Red dwell time (0.1 sec)

If any of the parameters are not valid, the table entry is *deleted*, zeroing
out values 4-7, and the response indicates this.

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
3         | Pin number (1-104)
4 †       | Pin status (0 or 1)

__Note__: Input pins and pins associated with meters (Meter Configure) cannot be
controlled with this command.

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
3 †       | Comm fail time (0.1 sec), default 1800
4 †       | Start up green time (0.1 sec), default 80
5 †       | Start up yellow time (0.1 sec), default 50
6 †       | Metering green time (0.1 sec), default 13
7 †       | Metering yellow time (0.1 sec), default 7

The meter timing attributes apply to all meters.

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

### V. - Firmware Version

Parameter | Description
----------|------------------------------------------
1         | Code: `V.` / `v.`
2         | Message ID
3 †       | Firmware version
4 †       | Build date / time

```
V.,A042
v.,A042,0.1.0,2022-01-31T12:34:56-05:00
```

[RFC 3339]: https://tools.ietf.org/html/rfc3339#section-5.6
[vehicle logging]: vehicle_detection.html#vehicle-logging
