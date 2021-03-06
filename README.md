# Pathfinder

[![Build Status](https://travis-ci.org/sherpc/pathfinder.svg?branch=master)](https://travis-ci.org/sherpc/pathfinder)

A Clojure library designed to track fns execution context and search through it.


## TODO

 - Add logging
 - Validate config on load, not on first usage
 - Add commit sha in trace
 - GitHub sources reading
 - Postpone expiration of selected path

## Usage

Run redis cli to monitor key expirations:

```
docker-compose run redis redis-cli -h redis --csv psubscribe '__key*__:expired'
```

Stop via `docker kill {container-id}` in separated console.

## License

Copyright © 2016 Aleksandr Sher

Distributed under the Eclipse Public License version 1.0.
