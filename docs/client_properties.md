# Client Properties

The IRIS server has a configuration file which contains default properties for
clients — `/etc/iris/iris-client.properties`.  These properties include
internationalization, network proxy, and other system configuration.  Note that
even though IRIS has been internationalized, it has not yet been translated to
languages other than english.

Property               | Description
-----------------------|-------------------------------------------------------
`language`             | ISO 639 alpha-2 or alpha-3 language code.  _E.g._ `en`
`country`              | ISO 3166 alpha-2 country code or UN M.49 numeric-3 area code.  _E.g._, `US`
`variant`              | IETF BCP 47 language variant subtag.
`district`             | District name — useful where multiple IRIS servers exist within the same organization
`http.proxy`           | List of HTTP proxy settings (used for downloading map tiles, XML files, etc.)
`http.proxy.whitelist` | List of addresses to bypass using proxy server, in [CIDR] notation (exact IP, or ranges specified such as 192.168.1.0/24)
`keystore.file`        | URL for the client keystore file
`keystore.password`    | Password for the client keystore
`sonar.host`           | IP or hostname of the SONAR server
`sonar.port`           | TCP port number of the SONAR server
`tdxml.detector.url`   | URL for XML detector stream
`map.tile.url`         | Base URL for map tileset — must end in `/`
`video.host`           | IP or hostname of video server/proxy
`video.port`           | TCP port number of video server/proxy
`autologin.username`   | A username to be used for automatic login upon client startup.  Note: use of this property is a security risk
`autologin.password`   | A password to be used for automatic login upon client startup.  Note: use of this property is a security risk
`tab.list`             | List specifying the visibility and ordering of the primary UI tabs in the IRIS client.  The default value is: `incident, dms, camera, lcs, ramp.meter, gate.arm, r_node, action.plan, comm`
`scale`                | User interface scaling factor (0.25 to 4.0) — useful for Hi-DPI screens

## User Properties

The IRIS client also reads a property file on client computers.  The file is
named `user.properties`, and can be found in the `iris` folder of the user's
home directory.  It can contain overrides for any of the values in the
_client properties_ file.

The file is overwritten when logging out.  The window geometry and last selected
tab will be updated, but all other properties will be left unchanged.


[CIDR]: https://en.wikipedia.org/wiki/Classless_Inter-Domain_Routing
