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
