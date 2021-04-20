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
cp ./target/release/mayfly /usr/local/bin
cp mayfly.service /etc/systemd/system
systemctl enable mayfly.service
systemctl start mayfly.service
```

On a server with IRIS installed, `nginx` will be set up to proxy requests to
the `/mayfly` route.
