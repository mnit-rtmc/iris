# bulb

*Bulb* is a WebAssembly front-end for IRIS, written in Rust.

## Installation

1. Install the current stable [Rust].
2. Install the [IRIS] package
3. Install the [honeybee] REST server
4. Install and configure the [earthwyrm] map server

Then, build the bulb front-end:

```sh
cargo install wasm-bindgen-cli
git clone https://github.com/mnit-rtmc/iris.git
cd iris/bulb/
./build.sh
```

The WebAssembly files will be located in the `./pkg` directory.  Copy them
to `/var/lib/iris/web/bulb/` and change the ownership to `tms:tms`.

Now, use firefox or chrome to login at http://127.0.0.1/iris/


[earthwyrm]: https://github.com/DougLau/earthwyrm/tree/master/earthwyrm-bin/
[honeybee]: https://github.com/mnit-rtmc/iris/tree/master/honeybee
[iris]: https://mnit-rtmc.github.io/iris/installation.html
[rust]: https://www.rust-lang.org/tools/install
