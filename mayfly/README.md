**Mayfly** provides an http interface to access IRIS traffic data archives.
Since the .traffic files are very large and would take considerable bandwidth
to transfer to the client analysis tools, Mayfly can locate the requested data
within an archive and provide just the pertinent data set.

This module has nothing to do with storing or zipping the archives; that is all
handled by the IRIS server.

## Building

```
git clone https://github.com/mnit-rtmc/iris.git
cd iris/mayfly/
cargo build --release
```

## Installation

As root:
```
cp ../target/release/mayfly /usr/local/bin
cp mayfly.service /etc/systemd/system
systemctl enable mayfly.service
systemctl start mayfly.service
```

On a server with IRIS installed, `nginx` will be set up to proxy requests to
the `/mayfly` route.

## Additional Setup

The `mayfly` service requires a couple of additional files which are generated
by `honeybee`.  If it's running on a different server, those files must be
copied manually:

- `/var/lib/iris/web/detector_pub`: For `espeed` calculation
- `/var/lib/iris/web/api/domain`: For client network checks and rate-limiting

## Cocoon

Cocoon is a command-line program to add 30-second binned data to .traffic
archive files.  This enables backward-compatibility for the older `trafdat`
service, and also improves performance for some mayfly requests.

After building, it is located at
`../target/release/cocoon`.

### Running

To process data from 2021/07/13:
```
cocoon bin /var/lib/iris/traffic/tms/2021/20210713.traffic
```

To process data from yesterday, use this command:
```
cocoon bin `date -d yesterday +/var/lib/iris/traffic/tms/%Y/%Y%m%d.traffic`
```
