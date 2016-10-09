(ns pathfinder.test-utils
  (:require [clojure.test :as t]
            [mount.core :refer [defstate] :as mount]
            [pathfinder.storage :refer [TracksSaver]]
            [pathfinder.query :refer [TracksQueryHandler]]))

(def default-state '())
(defonce db (atom default-state))

(defrecord AtomSaver []
  TracksSaver
  (save! [_ track]
    (swap! db conj track)))

(defstate atom-saver
  :start (do
           (reset! db default-state)
           (->AtomSaver))
  :stop (reset! db default-state))

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
    [])
  (last-n [_ n]
    (->>
     @db
     (group-by :path-id)
     (map (fn [[path tracks]] [path (sort-by :seq-id tracks)]))
     (take n)
     (into {}))))

(defstate atom-query-handler
  :start (->AtomQueryHandler))
