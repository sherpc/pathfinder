(ns pathfinder.redis.connection
  (:require [pathfinder.config :refer [config]]
            [mount.core :refer [defstate]]
            [taoensso.carmine :as car]))

(declare connection)

(defmacro wcar*
  [& body]
  `(car/wcar connection ~@body))

(defstate connection
  :start (:redis config))

