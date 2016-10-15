(ns pathfinder.test-utils
  (:require [clojure.test :as t]
            [mount.core :refer [defstate] :as mount]
            [taoensso.carmine :as car]
            [pathfinder.storage :refer [TracksSaver]]
            [pathfinder.query :refer [TracksQueryHandler]]
            [pathfinder.redis.connection :refer [wcar*]]))

;; TracksSaver

(def default-state '())
(defonce db (atom default-state))

(defrecord AtomSaver []
  TracksSaver
  (save! [_ track ttl]
    (swap! db conj track)))

(defstate atom-saver
  :start (do
           (reset! db default-state)
           (->AtomSaver))
  :stop (reset! db default-state))

(defn paths-count
  [tracks]
  (->>
   tracks
   (map :path-id)
   distinct
   count))

;; TracksQueryHandler

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
     (into {})))
  (stats [_]
    {:tracks (count @db)
     :paths (paths-count @db)}))

(defstate atom-query-handler
  :start (->AtomQueryHandler))

;; Config

(defstate dev-config
  :start (load-file "config/dev.clj"))

;; Redis dev mount

(defn with-mount-dev-config
  [f]
  (mount/start-with-states
   {#'pathfinder.config/config #'pathfinder.test-utils/dev-config})
  (f)
  (mount/stop))

(defn with-redis-flush
  [f]
  (wcar*
   (car/flushall))
  (f))
