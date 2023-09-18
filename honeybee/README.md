# Honeybee

*Honeybee* is a server for making IRIS resources available on the Web.  The
resources can be JSON files or .gif images.

## Design

It connects to a PostgreSQL database and listens for notifications, which
are sent by trigger functions when records are changed.  When a notification
is received, the appropriate resource is updated and written to a file.

The notification CHANNEL is the same as a table name, and the PAYLOAD is
either blank or the updated column.

CHANNEL                    | PAYLOAD
---------------------------|--------------------------------
`camera`                   | `video_loss`, `publish ` + name
`comm_link`                | `connected`
`controller`               | `setup`, `fail_time`
`detector`                 | `auto_fail`
`dms`                      | `msg_user`, `msg_sched`, `msg_current`, `expire_time`, `status`, `stuck_pixels`
`parking_area`             | `time_stamp`
`r_node`                   | name
`road`                     | name
`road_class`               | id
`tag_reader`               | `settings`
`weather_sensor`           | `settings`, `sample`
hashtag.resource\_n        | `hashtags`
geo\_loc.resource\_n       | `geo_loc`
controller\_io.resource\_n | `controller_io`

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
