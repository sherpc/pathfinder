(ns pathfinder.redis.query
  (:require [pathfinder.query :refer [TracksReadModel]]
            [pathfinder.redis.connection :refer [wcar*]]
            [pathfinder.redis.utils :as ru]
            [clojure.string :as str]
            [taoensso.carmine :as car]
            [mount.core :refer [defstate]]
            [clojure.tools.logging :as log]))

(defrecord RedisReadModel []
  TracksReadModel
  (search [_ query]
    (let [params (str/split query #" ")
          results (wcar* :as-pipeline (mapv car/smembers params))]
      (->>
       results
       (map #(into #{} %))
       ru/intersect)))
  (last-n [_ n]
    (log/debugf "last '%s' tracks" n)
    (->>
     (wcar* (car/lrange ru/last-n 0 n))
     (mapv (fn [id]
             [id (wcar* (car/lrange id 0 -1))]))
     (into {})))
  (stats [_]
    {}))

(defstate read-model
  :start (->RedisReadModel))
