(ns pathfinder.test-utils
  (:require [clojure.test :as t]
            [mount.core :refer [defstate] :as mount]
            [pathfinder.storage :refer [TracksSaver]]))

(defonce db (atom []))

(defrecord AtomSaver []
  TracksSaver
  (save! [_ track]
    (swap! db conj track)))

(defstate atom-saver
  :start (do
           (reset! db [])
           (->AtomSaver))
  :stop (reset! db []))

(defn tracks-count
  [tracks]
  (->>
   tracks
   (map :path-id)
   distinct
   count))