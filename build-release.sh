#!/usr/bin/env bash

clojure -A:fig:min
rm -rf release
cp -rf resources/public release
mkdir -p release/cljs-out
cp target/public/cljs-out/dev-main.js release/cljs-out/
