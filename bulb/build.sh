#!/bin/sh

set -ex
cargo build --profile web
wasm-bindgen --no-typescript --target web ../target/wasm32-unknown-unknown/web/bulb.wasm --out-dir ./pkg
# wasm-opt ./pkg/bulb_bg.wasm -Oz -o ./pkg/bulb.wasm
cp static/bulb.css pkg/
cp static/favicon.png pkg/
cp static/iris.svg pkg/
cp static/map.js pkg/
