(ns pathfinder.storage
  (:require [mount.core :refer [defstate]]))

(defprotocol TracksSaver
  (save! [this track ttl]))
