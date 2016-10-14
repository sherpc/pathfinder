(ns pathfinder.redis.storage
  (:require [pathfinder.storage :refer [TracksSaver]]
            [pathfinder.config :refer [config]]
            [taoensso.carmine :as car :refer (wcar)]
            [mount.core :refer [defstate]]))

(defmacro wcar*
  [& body]
  `(car/wcar tracks-saver ~@body))

(defn ping
  []
  (wcar*
   (car/ping)
   (car/set "navi" {:a 1})
   (car/get "navi")))

(defrecord RedisSaver [config]
  TracksSaver
  (save! [_ {:keys [path-id] :as track}]
    (wcar*
     (car/lpush path-id track))
    (println "Pushed to redis.")
    (clojure.pprint/pprint track)))

(defstate tracks-saver
  :start (->RedisSaver (:redis config)))
