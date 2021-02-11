# Installation

## Operating System

Install [Fedora] onto the server computer.  Other operating systems, such as Red
Hat Enterprise Linux, can also be used, but the procedures may vary slightly.
This machine should have a minimum of 8 GB of RAM and 400 GB hard drive storage
space.  The IRIS server does not have a GUI interface, so this computer can be
set up headless.

After the operating system is installed, make sure the hostname is configured
correctly in DNS.  The hostname is needed for some configuration files in the
following steps.  The following command displays the hostname that can be used
to access the server:
```
hostname -f
```

## Install IRIS

[Download] and install the IRIS package.  This should be a file called
iris-_{major}_._{minor}_._{micro}_-_{build}_.noarch.rpm.  The _{major}_,
_{minor}_, and _{micro}_ version numbers and the _{build}_ number can change
with each release.  It can be installed with the following command (as root):
```
dnf install iris-{major}.{minor}.{micro}-{build}.noarch.rpm
```

This will install dependencies not already installed, including [OpenJDK],
[PostgreSQL] and [nginx].

## Initialize IRIS

Next, you must initialize the IRIS server installation.  Run the following
command (as root):
```
iris_ctl init
```

This will perform the following steps:

1. Update the `/etc/iris/iris-client.properties` file with the server's hostname
2. Create an SSL key pair for encrypted communication between the server and
   clients.
 - The keystore files are `/etc/iris/iris-server.keystore` and
   `/etc/iris/iris-client.keystore`
 - The keystore passwords are put into `/etc/iris/iris-server.properties` and
   `/etc/iris/iris-client.properties`
3. Create a symbolic link to the PostgreSQL JDBC driver to make it available to
   the IRIS server
4. Create the database cluster and start the PostgreSQL server
5. Create the `earthwyrm` databsae (for OpenStreetMap data)
6. Create the `tms` PostgreSQL user, which IRIS uses to connect to the database
7. Create the `tms` database and populate it using a template SQL script
8. Configure the nginx (web server) and IRIS services to start automatically
9. Create symbolic links to the current IRIS software version

The command should finish with the following message:
```
Successfully initialized the IRIS server
```

## Setup GStreamer Autoinstaller

Client workstations require the native GStreamer video framework to play
advanced video codecs.  While it may be installed manually, doing so requires
administrator privileges and setting some environment variables.  Alternatively
Windows clients can automatically download the required native code from the
IRIS server and set up the environment.  (Linux clients are still required to
install GStreamer via their distribution's pacakage manager.)

To set up the server-side of this process, follow these instructions:

1. From a Windows computer, download the MinGW 64-bit runtime installer for
   the latest version of GStreamer [here](https://gstreamer.freedesktop.org/download/).
2. Run the installer and perform a "complete" installation, installing all
   components.  You may leave the default installation directory unchanged.
3. Once the installer is complete, go to the installation directory 
   (`C:\gstreamer\1.0\x86_64` by default).  Take all the contents of that
   directory (which should include bin, etc, include, lib, libexec, and share
   subdirectories) and zip them into a file named:
   `gstreamer-1.0-mingw-x86_64-<version>.zip` where `<version>` is a version
   number like `1.16.2`.
4. Repeat this process for the 32-bit installer.  This time you will zip the
   contents of `C:\gstreamer\1.0\x86` (unless changed from the default) into a
   file named `gstreamer-1.0-mingw-x86-<version>.zip`.
5. Copy both of these files to the IRIS server and put them in
   `/var/www/html/iris-gstreamer/`.  Make sure the file owner and permissions
   are suitable (`nginx` with read-only (444) permissions).
6. Set `gstreamer_version` in `project.properties` to the version of
   GStreamer you installed (e.g. `1.16.2`).  After changing this value, you
   must rebuild and reinstall the IRIS RPM.
   
Windows clients will now automatically download and unzip these files into
their `<user_home>/iris/` directory and set the necessary environment variables
at runtime when the GStreamer library is needed.

## Initialize Automated Warning System

IRIS can receive weather and other emergency alerts from the Integrated Public
Alert and Warning System (IPAWS) and automatically post them on Dynamic Message
Signs. For IPAWS alerts the PostGIS extension must be installed and enabled in
the `tms` database used by IRIS, which should be done by the `iris_ctl init`
command.

### Alert Area Geometry

In addition to the PostGIS extension, the system also requires alert area
geometry, in particular the National Weather Service's (NWS) Forecast Zones.
This information can be obtained in shapefile format from NWS and loaded into
the `tms` database.

To load geometry data, download the latest ["Public Forecast Zones" shapefile]
(https://www.weather.gov/gis/PublicZones) to the IRIS server and unzip it.
PostGIS provides both command-line and graphical tools to import shapefiles.
To import the file via the command line, execute the following command on the
server:
```
shp2pgsql -G <nws_shapefile>.shp cap.nws_zones | psql tms
```

To import the file via the graphical utility, open the "PostGIS Shapefile
Import/Export Manager." Click the "View connection details..." button, enter
the database connection information and press OK. Click the "Add File" button,
and select the shapefile containing forecast zones. Click the "Options" button
and make sure the box next to "Load into GEOGRAPHY" column is selected, then
click "OK." Finally, click the "Import" button and the file should be imported.

NOTE: When using the graphical method you may need to change the name and/or
schema of the table to cap.nws_zones using another tool (e.g. psql or pgAdmin).

NOTE: Alert areas may change (NWS updates the file roughly every six months
or), so it is important to keep them updated. Administrators should keep
records of when this information was last updated and maintain the latest
information in the database.

## Server Properties

The IRIS server has a configuration file which controls some of its basic
settings — `/etc/iris/iris-server.properties`.  Most of the default settings
should work, but it may be necessary to make some changes.  The file can be
modified using a text editor, such as _gedit_ or _vim_.

Property               | Description
-----------------------|-------------------------------------------------------
`language`             | ISO 639 alpha-2 or alpha-3 language code.  _E.g._ `en`
`country`              | ISO 3166 alpha-2 country code or UN M.49 numeric-3 area code.  _E.g._, `US`
`variant`              | IETF BCP 47 language variant subtag.
`district`             | District name — useful where multiple IRIS servers exist within the same organization
`http.proxy`           | List of HTTP proxy settings (used for downloading map tiles, XML files, etc.)
`http.proxy.whitelist` | List of addresses to bypass using proxy server, in [CIDR] notation (exact IP, or ranges specified such as 192.168.1.0/24)
`db.url`               | URL of PostgreSQL server
`db.user`              | User for PostgreSQL connection
`db.password`          | Password for PostgreSQL connection
`sonar.ldap.urls`      | List of URLs for LDAP authentication
`sonar.port`           | TCP port to connect to SONAR
`sonar.session.file`   | File to store client session IDs
`keystore.file`        | Location of keystore file
`keystore.password`    | Password for accessing keys in `keystore.file` — automatically generated by the `iris_ctl` script

### Internationalization

There are three properties which control internationalization (i18n).  They are
`language`, `country` and `variant`.  These should only be changed if the IRIS
software has been localized for a specific locale.

### Database Connection

The `db.url`, `db.user` and `db.password` properties control how the IRIS server
connects to the PostgreSQL database.  None of these properties should be
changed, since they were configured earlier by the `iris_ctl` script.

### LDAP

The `sonar.ldap.urls` property can be used to let IRIS pass user authentication
requests to one or more LDAP servers.  Multiple URLs must be separated by a
space.  IRIS can manage user accounts and passwords without an LDAP server, but
this feature allows operators to log into IRIS without requiring them to
remember an additional user name and password.  If your organization already has
an LDAP server, such as Active Directory, you should use that.

#### LDAPS

The URL protocol is normally `ldap:`, but must be `ldaps:` for encrypted
communication over SSL.  To use ldaps, you must first import the SSL certificate
into the IRIS keystore.  Once you have obtained the SSL certificate for the LDAP
server, save it as `ldap.cert`.  Now you can import it with the following
command (as root):
```
keytool -import -alias ldap-cert -keystore /etc/iris/iris-server.keystore -file ldap.cert
```

## Start Services

Finally, the IRIS server can now be started with the following command:
```
systemctl start iris
```

Check that everything started OK:
```
tail /var/log/iris/iris.stderr
systemctl status iris
```

This should indicate that IRIS is active and running.

## Initial Login

From a client computer with Java installed, point your web browser at
`http://`_YOUR-SERVER-NAME_`/iris-client/`.  From there, you should be able to
launch the IRIS client Web Start application and log in.

Enter `admin` as the username and `atms_242` as the password for the initial
login.  After [creating] and logging in with a real administrator account, the
`admin` user account should be disabled.


[CIDR]: https://en.wikipedia.org/wiki/Classless_Inter-Domain_Routing
[creating]: user_roles.html
[Download]: http://iris.dot.state.mn.us/rpms/
[Fedora]: http://fedoraproject.org
[ngihnx]: https://nginx.org/en/
[OpenJDK]: http://openjdk.java.net
[PostgreSQL]: http://www.postgresql.org
