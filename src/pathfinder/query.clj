(ns pathfinder.query
  (:require [mount.core :refer [defstate]]))

(defprotocol TracksReadModel
  (search [this params] "Search tracks.")
  (last-n [this n] "List last N tracks.")
  (stats [this] "Storage stats."))
