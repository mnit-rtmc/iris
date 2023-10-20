# Database

IRIS uses the [PostgreSQL] database for storage of configuration and event
data.

## Notifications

Notifications are sent by trigger functions when records are changed.

The CHANNEL matches the name of a table.  After INSERT or DELETE, and when an
UPDATE changes any *minimal* attribute, a notification is sent to that CHANNEL
(with an empty PAYLOAD).  Some channels are also notified when an associated
`geo_loc` __(G)__, `controller_io` __(C)__ or `hashtag` __(H)__ record is
updated.

Some tables have an alternate CHANNEL with `$1` appended to the name.  These
are notified on *all* updates, with a PAYLOAD containing the __name__ of the
updated record.

CHANNEL            | __(G)__ | __(C)__ | __(H)__ | Alternate
-------------------|---------|---------|---------|-----------
`alarm`            | ❌      | ✔️       | ❌      | ❌
`beacon`           | ✔️       | ✔️       | ❌      | `beacon$1`
`cabinet_style`    | ❌      | ❌      | ❌      | ❌
`camera`           | ✔️       | ✔️       | ❌      | `camera$1`
`comm_config`      | ❌      | ❌      | ❌      | ❌
`comm_link`        | ❌      | ❌      | ❌      | `comm_link$1`
`controller`       | ✔️       | ❌      | ❌      | `controller$1`
`detector`         | ❌      | ✔️       | ❌      | `detector$1`
`dms`              | ✔️       | ✔️       | ✔️       | `dms$1`
`flow_stream`      | ❌      | ✔️       | ❌      | ❌
`gps`              | ✔️       | ✔️       | ❌      | `gps$1`
`gate_arm`         | ❌      | ✔️       | ❌      | ❌
`gate_arm_array`   | ✔️       | ✔️       | ❌      | `gate_arm_array$1`
`graphic`          | ❌      | ❌      | ❌      | ❌
`i_user`           | ❌      | ❌      | ❌      | ❌
`incident`         | ❌      | ❌      | ❌      | ❌
`lane_marking`     | ✔️       | ✔️       | ❌      | ❌
`lcs_array`        | ❌      | ✔️       | ❌      | ❌
`lcs_indication`   | ❌      | ✔️       | ❌      | ❌
`modem`            | ❌      | ❌      | ❌      | ❌
`msg_pattern`      | ❌      | ❌      | ❌      | ❌
`msg_line`         | ❌      | ❌      | ❌      | ❌
`parking_area`     | ✔️       | ❌      | ❌      | `parking_area$1`
`permission`       | ❌      | ❌      | ❌      | ❌
`ramp_meter`       | ✔️       | ✔️       | ❌      | `ramp_meter$1`
❌                 | ✔️       | ❌      | ❌      | `r_node$1`
❌                 | ❌      | ❌      | ❌      | `road$1`
`role`             | ❌      | ❌      | ❌      | ❌
`sign_config`      | ❌      | ❌      | ❌      | ❌
`sign_detail`      | ❌      | ❌      | ❌      | ❌
`sign_message`     | ❌      | ❌      | ❌      | ❌
`system_attribute` | ❌      | ❌      | ❌      | ❌
`tag_reader`       | ✔️       | ✔️       | ❌      | `tag_reader$1`
`video_monitor`    | ❌      | ✔️       | ❌      | `video_monitor$1`
`weather_sensor`   | ✔️       | ✔️       | ❌      | `weather_sensor$1`
`word`             | ❌      | ❌      | ❌      | ❌

## Backup & Restore

It is a good idea to backup the IRIS database on a regular basis.  This can be
done with a command such as the following:
```
pg_dump tms | gzip > tms-20190827.sql.gz
```

This command can be placed in a python or bash script which is run by cron once
per day.  For off-site backups, the file could be copied to another host, using
scp or a similar utility.

To restore the `tms` database from one of these backup files, you will first
need to shut down the IRIS server (and anything else connected to the tms
database).  Then, run the following commands (as tms user):
```
dropdb tms
createdb tms
zcat tms-20190827.sql.gz | pgsql tms
```

## IRIS Upgrades

It is a good idea to backup the database before attempting an upgrade.

IRIS uses a versioning scheme similar to [semver], in the form of
`major.minor.patch`.  Each part of the version is a natural number, starting
from zero.  Whenever one of the numbers is incremented, any later parts are
reset to 0.  Patch version updates are for bug fixes and trivial enhancements.
Minor and major version changes are for new features which require database
changes.  Always check the release notes for instructions on each upgrade.

With a new rpm file available, a `patch` upgrade can be accomplished with the
following commands (as root):
```
dnf update iris-{major}.{minor}.{patch}-{build}.noarch.rpm
iris_ctl update
systemctl restart iris
```

A `minor` or `major` upgrade requires an additional step — migrating the
database schema.  The following commands can be used in this case (as root):
```
dnf update iris-{major}.{minor}.{patch}-{build}.noarch.rpm
psql tms -f /var/lib/iris/sql/migrate-{major}.{minor}.sql
iris_ctl update
systemctl restart iris
```

Note: when skipping one or more minor versions in an upgrade, **all**
intervening migrate scripts must be run.  So, when upgrading from 4.55.0 to
4.57.0, the database upgrade would consist of these commands:
```
psql tms -f /var/lib/iris/sql/migrate-4.56.sql
psql tms -f /var/lib/iris/sql/migrate-4.57.sql
```

Warning: there is no supported method of **downgrading** an IRIS system.  If a
downgrade is required, the database should be restored from a backup and the
IRIS rpm should be reinstalled.


[semver]: https://semver.org
[PostgreSQL]: http://www.postgresql.org
