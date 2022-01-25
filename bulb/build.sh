#!/bin/sh

set -ex
cargo build --release
wasm-bindgen --target web ./target/wasm32-unknown-unknown/release/bulb.wasm --out-dir ./pkg
