(ns pathfinder.redis.expired-listener
  (:require [pathfinder.redis.connection :refer [connection wcar*]]
            [pathfinder.redis.utils :as ru]
            [mount.core :refer [defstate]]
            [taoensso.carmine :as car]))

(defn notify-key-expired
  ;; [type pattern match content]
  ;; we need only content
  [[_ _ _ k :as msg]] 
  (when-let [path-id (ru/<-expire-key k)]
    (println (format "Removing key '%s'" path-id))
    (let [[tracks _] (wcar*
                      (car/lrange path-id 0 -1)
                      (car/del path-id))
          index (->>
                 tracks
                 (map :env)
                 (map ru/build-index)
                 (map #(into #{} %))
                 ru/intersect)]
      (wcar* (mapv #(car/srem % path-id) index)))))

(def pattern "__key*__:expired")

(defn create-listener
  []
  (car/with-new-pubsub-listener
    (:spec connection)
    {pattern notify-key-expired}
    (car/psubscribe pattern)))

(defstate expired-listener
  :start (create-listener)
  :stop (when expired-listener
          (car/close-listener expired-listener)))
