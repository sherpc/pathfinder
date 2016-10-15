(ns pathfinder.redis.storage
  (:require [pathfinder.storage :refer [TracksSaver]]
            [pathfinder.redis.connection :refer [wcar*]]
            [pathfinder.redis.expired-listener :refer [expired-listener]]
            [pathfinder.redis.utils :as ru]
            [pathfinder.config :refer [config]]
            [taoensso.carmine :as car]
            [mount.core :refer [defstate]]))

(defn ping
  []
  (wcar*
   (car/ping)
   (car/set "navi" {:a 1})
   (car/get "navi")))

(defn push-track
  [{:keys [path-id] :as track} ttl]
  (let [ek (ru/->expire-key path-id)]
    (wcar*
     ;; track
     (car/lpush path-id track)
     ;; shadow key for expiration listener
     (car/set ek nil)
     (car/expire ek (ru/total-seconds ttl)))))

(defn push-last-n
  [path-id size]
  (let [last-buffer (into #{} (wcar* (car/lrange ru/last-n 0 -1)))]
    (when-not (last-buffer path-id)
      (wcar*
       (car/lpush ru/last-n path-id)
       (car/ltrim ru/last-n 0 (dec size))))))

(defn push-env-indexes
  [{:keys [env path-id]}]
  (wcar*
   (mapv #(car/sadd % path-id) (ru/build-index env))))

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
