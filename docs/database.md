# Database

IRIS uses a [PostgreSQL] database for storage of configuration and event data.
On [initialization], the `tms` database is created from an SQL script.

## Channels

Database [notifications] are sent by trigger functions when records are
changed.  The [honeybee] server listens for them as part of the [REST API].

A *channel* name matches the name of a table.  A notification will be sent to
that channel after `INSERT`, `DELETE`, or `UPDATE` on the table or an
*associated table* with a matching foreign key.

Associated tables:

- `geo_loc` __(G)__
- `controller_io` __(C)__
- `device_preset` __(P)__
- `role_domain` __(D)__

The notification payload will either be the object/record name of the changed
row, or an empty string (blank).  A blank payload requires a full resource
update, and is caused by one of these conditions:

- `INSERT` / `DELETE` caused a row to be added/removed
- `UPDATE` changed any *primary* attribute
- `UPDATE` of an associated table changed any *primary* attribute

*Notify Channel*   | (G) | (C) | (P) | (D)
-------------------|-----|-----|-----|-----
`action_plan`      |     |     |     |
`alarm`            |     | ☑️   |     |
`beacon`           | ☑️   | ☑️   | ☑️   |
`cabinet_style`    |     |     |     |
`camera`           | ☑️   | ☑️   |     |
`camera_publish` † |     |     |     |
`camera_preset`    |     |     |     |
`comm_config`      |     |     |     |
`comm_link`        |     |     |     |
`controller`       | ☑️   |     |     |
`day_plan`         |     |     |     |
`detector`         |     | ☑️   |     |
`device_action`    |     |     |     |
`dms`              | ☑️   | ☑️   | ☑️   |
`domain`           |     |     |     |
`encoder_stream`   |     |     |     |
`encoder_type`     |     |     |     |
`event_config`     |     |     |     |
`flow_stream`      |     | ☑️   |     |
`gate_arm`         | ☑️   | ☑️   | ☑️   |
`gps`              | ☑️   | ☑️   |     |
`graphic`          |     |     |     |
`incident`         |     |     |     |
`inc_advice`       |     |     |     |
`inc_descriptor`   |     |     |     |
`inc_detail`       |     |     |     |
`inc_locator`      |     |     |     |
`lcs`              | ☑️   | ☑️   | ☑️   |
`lcs_state`        |     | ☑️   |     |
`modem`            |     |     |     |
`monitor_style`    |     |     |     |
`msg_pattern`      |     |     |     |
`msg_line`         |     |     |     |
`parking_area`     | ☑️   |     |     |
`permission`       |     |     |     |
`plan_phase`       |     |     |     |
`play_list`        |     |     |     |
`ramp_meter`       | ☑️   | ☑️   | ☑️   |
`r_node`           | ☑️   |     |     |
`road`             |     |     |     |
`road_affix`       |     |     |     |
`role`             |     |     |     | ☑️ 
`sign_config`      |     |     |     |
`sign_detail`      |     |     |     |
`sign_message`     |     |     |     |
`system_attribute` |     |     |     |
`tag_reader`       | ☑️   | ☑️   |     |
`time_action`      |     |     |     |
`toll_zone`        |     |     |     |
`user_id`          |     |     |     |
`video_monitor`    |     | ☑️   |     |
`weather_sensor`   | ☑️   | ☑️   |     |
`word`             |     |     |     |

† _Notifies only on `UPDATE` to 'publish' attribute, with camera name payload_

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
zcat tms-20190827.sql.gz | psql tms
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


[honeybee]: https://github.com/mnit-rtmc/iris/tree/master/honeybee
[initialization]: installation.html#initialization
[notifications]: https://www.postgresql.org/docs/current/sql-notify.html
[PostgreSQL]: http://www.postgresql.org
[REST API]: rest_api.html
[semver]: https://semver.org
