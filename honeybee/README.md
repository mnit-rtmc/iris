# Honeybee

*Honeybee* is a REST API backend server for IRIS resources.

It uses the PostgreSQL LISTEN/NOTIFY mechanism to handle [notifications].

## Requests

Protected routes must include a session cookie called `honeybee` which is
authenticated with a valid IRIS `username` and `password`.  This can be
created by using `POST` on the `iris/api/login` route.

For each supported resource type, *res*, these requests are available:

Route                     | Verb     | Function
--------------------------|----------|------------------------
`iris/api/`*res*          | `GET`    | Get all objects of type
`iris/api/`*res*          | `POST`   | Create new object
`iris/api/`*res*`/`*name* | `GET`    | Get one object
`iris/api/`*res*`/`*name* | `PATCH`  | Update attributes of one object
`iris/api/`*res*`/`*name* | `DELETE` | Delete one object

## Install

1. Install the current stable [Rust].
2. Install the [IRIS] package (this creates the `tms` user/database and the
   `/var/lib/iris/web/` working directory)
3. Modify /var/lib/pgsql/data/pg_hba.conf for peer connection:
   `local  all  all  peer`
4. Restart postgresql server

Then, build honeybee:

```sh
git clone https://github.com/mnit-rtmc/iris.git
cd iris/honeybee/
cargo build --release
```

If located outside of the `US/Central` timezone, edit the `honeybee.service`
file and change the line starting with `Environment=PGTZ=`.

Then, as root:
```sh
usermod -a -G earthwyrm tms
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


[iris]: https://mnit-rtmc.github.io/iris/installation.html
[notifications]: https://mnit-rtmc.github.io/iris/database.html#notifications
[rust]: https://www.rust-lang.org/tools/install
