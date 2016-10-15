(ns pathfinder.redis.query
  (:require [pathfinder.query :refer [TracksQueryHandler]]
            [pathfinder.config :refer [config]]
            [taoensso.carmine :as car :refer (wcar)]
            [mount.core :refer [defstate]]
            [clojure.tools.logging :as log]))

(declare tracks-query-handler)

(defmacro wcar*
  [& body]
  `(car/wcar tracks-query-handler ~@body))

(defrecord RedisQueryHandler [pool spec]
  TracksQueryHandler
  (search [_ params]
    (log/debugf "searching tracks with '%s' params." params))
  (last-n [_ n]
    (log/debugf "last '%s' tracks" n))
  (stats [_]
    {}))

(defstate tracks-query-handler
  :start (map->RedisQueryHandler (:redis config)))
