# Honeybee

*Honeybee* is a server for making IRIS resources available on the Web.  The
resources can be JSON files or .gif images.

## Design

It connects to a PostgreSQL database and listens for notifications, which
happen when table records are changed.  When a notification is received, the
appropriate resource is updated and written to a file.

## Building

```
git clone https://github.com/mnit-rtmc/iris.git
cd iris/honeybee/
cargo build --release
```

## Installation

NOTE: The `tms` and `earthwyrm` databases must be set up prior to this step.

As root:
```
cp ./target/release/honeybee /usr/local/bin
cp honeybee.service /etc/systemd/system
systemctl enable honeybee.service
systemctl start honeybee.service
```
