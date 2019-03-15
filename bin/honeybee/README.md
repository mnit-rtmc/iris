# Honeybee

*Honeybee* is a server for making IRIS resources available on the Web.  The
resources can be JSON files, .gif images or MVT files.

## Design

It connects to a PostgreSQL database and listens for notifications, which
happen when table records are changed.  When a notification is received, the
appropriate resource is updated and written to a file, which is then mirrored
to another server using ssh.
