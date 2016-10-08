(ns pathfinder.storage
  (:require [mount.core :refer [defstate]]))

(defprotocol TracksSaver
  (save! [this track]))

(defrecord SqlSaver []
  TracksSaver
  (save! [_ track]
    (clojure.pprint/pprint track)))

(defstate tracks-saver
  :start (->SqlSaver))
