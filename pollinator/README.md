# Pollinator

*Pollinator* (ˈpō-lə-ˌnā-tər) is a server for polling IRIS devices.  It is
in the early stages of development, and only supports these protocols:

- RTMS Echo

## Install

First, install the current stable [Rust].

Then, build pollinator:

```sh
git clone https://github.com/mnit-rtmc/iris.git
cd iris/pollinator/
cargo build --release
```

Then, as root:
```sh
cp ./target/release/pollinator /usr/local/bin
cp pollinator.service /etc/systemd/system
systemctl enable pollinator.service
systemctl start pollinator.service
```


[rust]: https://www.rust-lang.org/tools/install
