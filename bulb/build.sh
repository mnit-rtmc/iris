#!/bin/sh

set -ex
cargo build --lib --release
wasm-bindgen --target web ./target/wasm32-unknown-unknown/release/bulb.wasm --out-dir ./pkg
