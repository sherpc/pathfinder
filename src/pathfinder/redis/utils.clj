(ns pathfinder.redis.utils)

(def last-n "last-n")

(defn total-seconds
  [ttl]
  (->
   ttl
   java.time.Duration/parse
   .getSeconds))

(def ^:private expire-prefix "e:")

(defn ->expire-key
  [path-id]
  (str expire-prefix path-id))

(defn <-expire-key
  [expire-path-id]
  (when expire-path-id
    ;; TODO check for substring exception cause of string length
    (.substring expire-path-id (count expire-prefix))))
