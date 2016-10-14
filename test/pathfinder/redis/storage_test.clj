(ns pathfinder.redis.storage-test
  (:require [pathfinder.redis.storage :as rs]
            [pathfinder.core :as p]
            [pathfinder.test-utils :refer [dev-config]]
            [mount.core :as mount]
            [taoensso.carmine :as car]
            [clojure.test :refer :all]))

(use-fixtures
  :each
  (fn [f]
    (mount/start-with-states
     {#'pathfinder.config/config #'pathfinder.test-utils/dev-config})
    (f)
    (mount/stop))
  (fn [f]
    (car/wcar
     rs/tracks-saver
     (car/flushall))
    (f)))

(deftest ping-test
  (testing "simple redis ping"
    (is (= ["PONG" "OK" {:a 1}] (rs/ping)))))

(deftest one-track-test
  (testing "eval trace in let, should be in redis with default timeout"
    (let [x 1 y {:a 2}]
      (let [{:keys [id]} (p/trace-env-ttl "PT2S")
            stored (car/wcar
                    rs/tracks-saver
                    (car/lrange id 0 -1))]
        (println "Stored data from" id)
        (clojure.pprint/pprint stored)
        (is (not (nil? stored)))))))


