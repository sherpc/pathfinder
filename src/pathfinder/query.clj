(ns pathfinder.query
  (:require [mount.core :refer [defstate]]
            [clojure.tools.logging :as log]))

(defprotocol TracksQueryHandler
  (search [this params] "Search tracks.")
  (last-n [this n] "List last N tracks."))

(defrecord RedisQueryHandler []
  TracksQueryHandler
  (search [_ params]
    (log/debugf "searching tracks with '%s' params." params))
  (last-n [_ n]
    (log/debugf "last '%s' tracks" n)))

(defstate tracks-query-handler
  :start (->RedisQueryHandler))
