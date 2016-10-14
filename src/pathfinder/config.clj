(ns pathfinder.config
  (:require [mount.core :refer [defstate]]))

(defstate config
  :start (load-file "config/dynamic.clj"))

