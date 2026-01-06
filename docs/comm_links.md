# Comm Links

Select `View ➔ Maintenance ➔ Comm Links` menu item

A **comm link** is a network connection to field [device]s or external system
**feeds**.  IRIS is capable of supporting thousands of simultaneous comm links.

<details>
<summary>API Resources 🕵️ </summary>

* `iris/api/comm_link` (primary)
* `iris/api/comm_link/{name}`

| Access       | Primary           | Secondary |
|--------------|-------------------|-----------|
| 👁️  View      | name              | connected |
| 👉 Operate   | poll\_enabled     |           |
| 💡 Manage    | description       |           |
| 🔧 Configure | uri, comm\_config |           |

</details>

## Setup

**Description** is s short text description of the comm link.

**URI**, or _Uniform Resource Identifier_ includes a DNS host name or network IP
address, and port number, using the standard `host:port` convention.  It can
also contain an optional **scheme** prefix, which can be either `udp://`,
`tcp://` or `modem://` for [modem] links.  If present, the scheme will override
the _default scheme_ for the selected protocol.  For example, to use the
[Pelco-D] protocol over TCP (instead of the default UDP), prepend `tcp://` to
the URI.

**Poll Enabled** is a flag which can enable or disable polling.

[Comm Config] is a set of properties which can be shared among multiple comm
links.


[comm config]: comm_config.html
[device]: controllers.html#devices
[modem]: modem.html
[pelco-d]: protocols.html#pelco-d
