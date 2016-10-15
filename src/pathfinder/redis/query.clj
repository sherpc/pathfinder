(ns pathfinder.redis.query
  (:require [pathfinder.query :refer [TracksQueryHandler]]
            [pathfinder.redis.connection :refer [wcar*]]
            [pathfinder.redis.utils :as ru]
            [clojure.string :as str]
            [taoensso.carmine :as car]
            [mount.core :refer [defstate]]
            [clojure.tools.logging :as log]))

(defn intersect
  [& sets]
  (if (empty? sets)
    #{}
    (apply clojure.set/intersection sets)))

(defrecord RedisQueryHandler []
  TracksQueryHandler
  (search [_ query]
    (let [params (str/split query #" ")
          results (wcar* :as-pipeline (mapv car/smembers params))]
      (->>
       results
       (map #(into #{} %))
       (apply intersect))))
  (last-n [_ n]
    (log/debugf "last '%s' tracks" n)
    (wcar* (car/lrange ru/last-n 0 n)))
  (stats [_]
    {}))

(defstate tracks-query-handler
  :start (->RedisQueryHandler))
