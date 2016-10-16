(ns user.mount
  (:require [mount.core :as mount]
            [pathfinder.web :refer [web-server]]
            [pathfinder.test-utils :refer [db] :as tu]
            [pathfinder.test-fns :as tf]
            [pathfinder.core :as t]))

(defn go
  []
  (mount/stop)
  (->
   (mount/swap-states
    {#'pathfinder.redis.storage/tracks-saver #'pathfinder.test-utils/atom-saver
     #'pathfinder.redis.query/read-model #'pathfinder.test-utils/atom-query-handler})
   (mount/swap {#'pathfinder.redis.expired-listener/expired-listener nil})
   mount/start)
  (tf/run-all))
