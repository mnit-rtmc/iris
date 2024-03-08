# Honeybee

*Honeybee* is a REST API backend server for IRIS resources.

It uses the PostgreSQL LISTEN/NOTIFY mechanism to handle [notifications].

## Requests

Protected request routes must include a session cookie called `honeybee` which
is authenticated with a valid IRIS `username` and `password`.  This can be
created by using `POST` on the `/login` route.

For each supported *type*, these requests are available:

Route              | Verb     | Function
-------------------|----------|------------------------
`/`*type*          | `GET`    | Get all objects of type
`/`*type*          | `POST`   | Create new object
`/`*type*`/`*name* | `GET`    | Get one object
`/`*type*`/`*name* | `PATCH`  | Update attributes of one object
`/`*type*`/`*name* | `DELETE` | Delete one object

## Building

```
git clone https://github.com/mnit-rtmc/iris.git
cd iris/honeybee/
cargo build --release
```

## Installation

NOTE: The `tms` database must be set up prior to this step.

As root:
```
cp ./target/release/honeybee /usr/local/bin
cp honeybee.service /etc/systemd/system
systemctl enable honeybee.service
systemctl start honeybee.service
```

## Security

Honeybee connects as a client to the IRIS server with TLS.  Since `rustls` is
only designed for web PKI scenarios, `honeybee` is configured to always trust
the server certificate.  This configuration is only secure when both `honeybee`
and IRIS are running on the same host.


[notifications]: https://mnit-rtmc.github.io/iris/database.html#notifications
