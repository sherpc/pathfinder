(ns user.mount
  (:require [mount.core :as mount]
            [pathfinder.web :refer [web-server]]
            [pathfinder.test-utils :refer [db] :as tu]
            [pathfinder.core :as t]))

(defn some-inner-fn
  [a b c]
  (t/trace-env)
  (+ a b c))

(defn test-fn
  [a b]
  (t/trace-env)
  (some-inner-fn a b 3))

(defn go-web-fake-data
  []
  (mount/stop)
  (mount/start-with-states
   {#'pathfinder.storage/tracks-saver #'pathfinder.test-utils/atom-saver
    #'pathfinder.query/tracks-query-handler #'pathfinder.test-utils/atom-query-handler})
  (test-fn 1 2)
  (some-inner-fn 3 4 5))

