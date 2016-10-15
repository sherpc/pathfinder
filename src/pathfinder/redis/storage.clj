(ns pathfinder.redis.storage
  (:require [pathfinder.storage :refer [TracksSaver]]
            [pathfinder.redis.connection :refer [wcar*]]
            [taoensso.carmine :as car :refer (wcar)]
            [mount.core :refer [defstate]]))

(defn ping
  []
  (wcar*
   (car/ping)
   (car/set "navi" {:a 1})
   (car/get "navi")))

(defn total-seconds
  [ttl]
  (->
   ttl
   java.time.Duration/parse
   .getSeconds))

(defrecord RedisSaver []
  TracksSaver
  (save! [_ {:keys [path-id] :as track} ttl]
    (wcar*
     (car/lpush path-id track)
     (car/expire path-id (total-seconds ttl)))
    (println "Pushed to redis with ttl" ttl)
    (clojure.pprint/pprint track)))

(defstate tracks-saver
  :start (->RedisSaver))
