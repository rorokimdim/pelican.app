# pelican.app

http://pelican.app.s3-website-us-west-2.amazonaws.com/

## Overview

A javascript app for buy-vs-rent analysis.

## Development

To get an interactive development environment run:

    ./repl.sh (or clojure -A:fig:build)

This will auto compile and send all changes to the browser without the
need to reload. After the compilation process is complete, you will
get a Browser Connected REPL. An easy way to try it is:

    (js/alert "Am I connected?")

and you should see an alert in the browser window.

To clean all compiled files:

    rm -rf target/public

To create a production build run:

    ./build-release.sh


## License

Copyright © 2018 Amit Shrestha

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
