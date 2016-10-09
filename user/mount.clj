(ns user.mount
  (:require [mount.core :as mount]
            [pathfinder.web :refer [web-server]]))

(defn go
  []
  (mount/stop)
  (mount/start))

