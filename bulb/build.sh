#!/bin/sh

set -ex
cargo build --release
wasm-bindgen --target web ./target/wasm32-unknown-unknown/release/bulb.wasm --out-dir ./pkg
# wasm-opt ./pkg/bulb_bg.wasm -Oz -o ./pkg/bulb.wasm
