# Maintenance

Once an IRIS system is set up, there are a few maintenance tasks which should be
done to ensure reliable operation.

## Database Backup & Restore

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

IRIS uses a typical `major.minor.micro` versioning scheme.  Each part of the
version is a natural number, starting from zero.  Whenever one of the numbers
is incremented, any later parts are reset to 0.  Micro version updates are for
bug fixes and trivial enhancements.  Minor and major version changes are for new
features which require database changes.  Always check the release notes for
instructions on each upgrade.

With a new rpm file available, a `micro` upgrade can be accomplished with the
following commands (as root):
```
dnf update iris-{major}.{minor}.{micro}-{build}.noarch.rpm
iris_ctl update
systemctl restart iris
```

A `minor` or `major` upgrade requires an additional step — migrating the
database schema.  The following commands can be used in this case (as root):
```
dnf update iris-{major}.{minor}.{micro}-{build}.noarch.rpm
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
