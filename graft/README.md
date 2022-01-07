# Graft

*Graft* is a web server for proxying IRIS sonar objects to a REST API.

## Requests

All requests must include a `graft` session cookie which is authenticated with
a valid IRIS `username` and `password`.  This can be created by using `POST` on
the `/login` route.

For each supported *type*, these requests are available:

Route              | Verb     | Description
-------------------|----------|------------------------
`/`*type*          | `GET`    | Get all objects of type
`/`*type*          | `POST`   | Create new object
`/`*type*`/`*name* | `GET`    | Get one object
`/`*type*`/`*name* | `PATCH`  | Update attributes of one object
`/`*type*`/`*name* | `DELETE` | Delete one object

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
