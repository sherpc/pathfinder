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

(defn push-track
  [{:keys [path-id] :as track} ttl]
  (wcar*
   (car/lpush path-id track)
   (car/expire path-id (total-seconds ttl))))

(defn push-last-n
  [path-id size]
  (let [last-buffer (into #{} (wcar* (car/lrange last-n 0 -1)))]
    (when-not (last-buffer path-id)
      (wcar*
       (car/lpush last-n path-id)
       (car/ltrim last-n 0 (dec size))))))

(defn build-index
  [env]
  (->>
   env
   (mapv (fn [[k v]] (format "%s:%s" k v)))))

(defn push-env-indexes
  [{:keys [env path-id]}]
  (wcar*
   (mapv #(car/sadd % path-id) (build-index env))))

(defrecord RedisSaver [last-n-buffer-size]
  TracksSaver
  (save! [_ {:keys [path-id] :as track} ttl]
    (push-track track ttl)
    (push-last-n path-id last-n-buffer-size)
    (push-env-indexes track)
    (println "Pushed to redis with ttl" ttl)
    (clojure.pprint/pprint track)))

(defstate tracks-saver
  :start (map->RedisSaver (:redis-saver config)))
