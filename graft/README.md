# Graft

*Graft* is a web server for proxying IRIS sonar objects.

## Building

```
git clone https://github.com/mnit-rtmc/iris.git
cd iris/graft/
cargo build --release
```

## Installation

As root:
```
cp ./target/release/graft /usr/local/bin
cp graft.service /etc/systemd/system
systemctl enable graft.service
systemctl start graft.service
```

## Security

Graft connects as a client to the IRIS server with TLS.  Since `rustls` is only
designed for web PKI scenarios, `graft` is configured to always trust the server
certificate.  This configuration is only secure when both `graft` and IRIS are
running on the same host.
