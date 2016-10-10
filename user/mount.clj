(ns user.mount
  (:require [mount.core :as mount]
            [pathfinder.web :refer [web-server]]
            [pathfinder.test-utils :refer [db] :as tu]
            [pathfinder.test-fns :as tf]
            [pathfinder.core :as t]))

(defn go-web-fake-data
  []
  (mount/stop)
  (mount/start-with-states
   {#'pathfinder.storage/tracks-saver #'pathfinder.test-utils/atom-saver
    #'pathfinder.query/tracks-query-handler #'pathfinder.test-utils/atom-query-handler})
  (tf/run-all))

