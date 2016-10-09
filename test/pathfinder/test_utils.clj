(ns pathfinder.test-utils
  (:require [clojure.test :as t]
            [mount.core :refer [defstate] :as mount]
            [pathfinder.storage :refer [TracksSaver]]
            [pathfinder.query :refer [TracksQueryHandler]]))

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

(defrecord AtomQueryHandler []
  TracksQueryHandler
  (search [_ params]
    @db))

(defstate atom-query-handler
  :start (->AtomQueryHandler))
