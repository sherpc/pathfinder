(ns pathfinder.redis.query
  (:require [pathfinder.query :refer [TracksQueryHandler]]
            [pathfinder.redis.connection :refer [wcar*]]
            [taoensso.carmine :as car]
            [mount.core :refer [defstate]]
            [clojure.tools.logging :as log]))

(defrecord RedisQueryHandler []
  TracksQueryHandler
  (search [_ params]
    (log/debugf "searching tracks with '%s' params." params))
  (last-n [_ n]
    (log/debugf "last '%s' tracks" n)
    [])
  (stats [_]
    {}))

(defstate tracks-query-handler
  :start (->RedisQueryHandler))
