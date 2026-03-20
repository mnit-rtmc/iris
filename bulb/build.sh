#!/bin/sh

set -ex
cargo build --profile web
wasm-bindgen --no-typescript --target web ../target/wasm32-unknown-unknown/web/bulb.wasm --out-dir ./pkg
# wasm-opt ./pkg/bulb_bg.wasm -Oz -o ./pkg/bulb.wasm
cp static/index.html static/bulb.css static/wyrm.css static/iris.svg static/favicon.png pkg/
