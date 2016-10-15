(ns pathfinder.redis.storage
  (:require [pathfinder.storage :refer [TracksSaver]]
            [pathfinder.redis.connection :refer [wcar*]]
            [pathfinder.config :refer [config]]
            [taoensso.carmine :as car]
            [mount.core :refer [defstate]]))

(def last-n "last-n")

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

(defrecord RedisSaver [last-n-buffer-size]
  TracksSaver
  (save! [_ {:keys [path-id] :as track} ttl]
    (wcar*
     (car/lpush path-id track)
     (car/expire path-id (total-seconds ttl)))
    (let [last-buffer (into #{} (wcar* (car/lrange last-n 0 -1)))]
      (when-not (last-buffer path-id)
        (wcar*
         (car/lpush last-n path-id)
         (car/ltrim last-n 0 (dec last-n-buffer-size)))))
    (println "Pushed to redis with ttl" ttl)
    (clojure.pprint/pprint track)))

(defstate tracks-saver
  :start (map->RedisSaver (:redis-saver config)))
