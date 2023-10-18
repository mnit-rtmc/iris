# Honeybee

*Honeybee* is a server for making IRIS resources available on the Web.  The
resources can be JSON files or .gif images.

## Design

It connects to a PostgreSQL database and listens for notifications, which
are sent by trigger functions when records are changed.  When a notification
is received, the appropriate resource is updated and written to a file.

The notification CHANNEL matches the name of a table.  Empty PAYLOAD events
are sent on INSERT or DELETE, and when an UPDATE changes any *minimal*
attribute.  Some channels are also notified when an associated `geo_loc`,
`controller_io` or `hashtag` record is updated.

Some tables have a second CHANNEL with `$1` appended to the name.  These are
notified on *all* updates, with a PAYLOAD containing the __name__ of the
updated record.

CHANNEL            | `geo_loc` | `controller_io` | `hashtag` | Alternate
-------------------|-----------|-----------------|-----------|-----------
`alarm`            | ❌        | ✔️               | ❌        | ❌
`beacon`           | ✔️         | ✔️               | ❌        | `beacon$1`
`cabinet_style`    | ❌        | ❌              | ❌        | ❌
`camera`           | ✔️         | ✔️               | ❌        | `camera$1`
`comm_config`      | ❌        | ❌              | ❌        | ❌
`comm_link`        | ❌        | ❌              | ❌        | `comm_link$1`
`controller`       | ✔️         | ❌              | ❌        | `controller$1`
`detector`         | ❌        | ✔️               | ❌        | `detector$1`
`dms`              | ✔️         | ✔️               | ✔️         | `dms$1`
`flow_stream`      | ❌        | ✔️               | ❌        | ❌
`gps`              | ✔️         | ✔️               | ❌        | `gps$1`
`gate_arm`         | ❌        | ✔️               | ❌        | ❌
`gate_arm_array`   | ✔️         | ✔️               | ❌        | `gate_arm_array$1`
`graphic`          | ❌        | ❌              | ❌        | ❌
`i_user`           | ❌        | ❌              | ❌        | ❌
`incident`         | ❌        | ❌              | ❌        | ❌
`lane_marking`     | ✔️         | ✔️               | ❌        | ❌
`lcs_array`        | ❌        | ✔️               | ❌        | ❌
`lcs_indication`   | ❌        | ✔️               | ❌        | ❌
`modem`            | ❌        | ❌              | ❌        | ❌
`msg_pattern`      | ❌        | ❌              | ❌        | ❌
`msg_line`         | ❌        | ❌              | ❌        | ❌
`parking_area`     | ✔️         | ❌              | ❌        | `parking_area$1`
`permission`       | ❌        | ❌              | ❌        | ❌
`ramp_meter`       | ✔️         | ✔️               | ❌        | `ramp_meter$1`
❌                 | ✔️         | ❌              | ❌        | `r_node$1`
❌                 | ❌        | ❌              | ❌        | `road$1`
`role`             | ❌        | ❌              | ❌        | ❌
`sign_config`      | ❌        | ❌              | ❌        | ❌
`sign_detail`      | ❌        | ❌              | ❌        | ❌
`sign_message`     | ❌        | ❌              | ❌        | ❌
`system_attribute` | ❌        | ❌              | ❌        | ❌
`tag_reader`       | ✔️         | ✔️               | ❌        | `tag_reader$1`
`video_monitor`    | ❌        | ✔️               | ❌        | `video_monitor$1`
`weather_sensor`   | ✔️         | ✔️               | ❌        | `weather_sensor$1`
`word`             | ❌        | ❌              | ❌        | ❌

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
