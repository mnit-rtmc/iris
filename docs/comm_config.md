# Comm Config

Select `View ➔ Maintenance ➔ Comm Config` menu item

[Protocol] determines what type of [device] or system is on the other end of
a [comm link].

**Modem** is a flag indicating the connection uses a _dial-up_ or _cell_ modem.

**Poll period** determines how frequently [controller]s on a [comm link] are
polled.  It can range from 5 seconds to 24 hours.

**Long Poll Period** is for less frequently performed polling operations,
determined by the protocol.  For modem links with restricted bandwidth, it
may be useful set this the same as **poll period**, to reduce costs.

**Timeout** determines how long to wait after a poll, if a response is not
received, before communicaton will fail.  For each poll, 2 retries will happen
before the operation is aborted.

**Idle Disconnect** will cause the [comm link] to be disconnected after a
period of inactivity.  This can reduce charges for modem links.  Setting this
to zero disables this feature.

**No Response Disconnect** will cause the [comm link] to disconnect after no
response is received from a poll for the specified time.  Setting this to zero
disables this feature.


[comm link]: comm_links.html
[controller]: controllers.html
[device]: controllers.html#devices
[protocol]: protocols.html
