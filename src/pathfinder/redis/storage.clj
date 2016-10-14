(ns pathfinder.redis.storage
  (:require [pathfinder.storage :refer [TracksSaver]]
            [pathfinder.config :refer [config]]
            [taoensso.carmine :as car :refer (wcar)]
            [mount.core :refer [defstate]]))

(defmacro wcar* [& body]
  `(car/wcar tracks-saver ~@body))

(defn ping
  []
  (wcar* (car/ping)))

(defrecord RedisSaver [config]
  TracksSaver
  (save! [_ track]
    (println track)))

(defstate tracks-saver
  :start (->RedisSaver (:redis config)))


