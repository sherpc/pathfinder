(ns pathfinder.query
  (:require [mount.core :refer [defstate]]
            [clojure.tools.logging :as log]))

(defprotocol TracksQueryHandler
  (search [this params] "Search tracks."))

(defrecord RedisQueryHandler []
  TracksQueryHandler
  (search [_ params]
    (log/debugf "searching tracks with '%s' params." params)))

(defstate tracks-query-handler
  :start (->RedisQueryHandler))
