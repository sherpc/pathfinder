(ns pathfinder.redis.storage-test
  (:require [pathfinder.redis.storage :as rs]
            [mount.core :as mount]
            [taoensso.carmine :as car]
            [clojure.test :refer :all]))

(use-fixtures
  :each
  (fn [f]
    (mount/start-with-states
     {#'pathfinder.config/config #'pathfinder.test-utils/dev-config
      #'pathfinder.storage/tracks-saver #'pathfinder.redis.storage/tracks-saver})
    (f)
    (mount/stop))
  (fn [f]
    (car/wcar
     rs/tracks-saver
     (car/flushall))
    (f)))

(deftest ping-test
  (testing "simple redis ping"
    (is (= ["PONG" "OK" "bar"] (rs/ping)))))
