#!/bin/sh

set -ex
cargo build --lib --release --target wasm32-unknown-unknown
wasm-bindgen --target web ./target/wasm32-unknown-unknown/release/bulb.wasm --out-dir ./pkg
