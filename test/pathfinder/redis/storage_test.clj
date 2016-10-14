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

(deftest one-path-test
  (testing "eval trace in let, should be in redis; after timeout it should be removed"
    (let [x 1 y {:a 2}]
      (let [{:keys [id]} (p/trace-env-ttl "PT1S")
            stored (rs/wcar*
                    (car/lrange id 0 -1))]
        (println "Stored data from" id)
        (is (= 1 (count stored)))
        (is (= 1 (rs/wcar* (car/ttl id))))
        ;; wait for 1s ttl
        (Thread/sleep (* 1000 2))
        (is (= -2 (rs/wcar* (car/ttl id))))
        (is (= [] (rs/wcar* (car/lrange id 0 -1)))))))
  (testing "second track should prolongate path expiration"
    (let [x 2 y {:a 3}]
      (p/trace-env-ttl "PT2S")
      (Thread/sleep 1000)
      (let [z (+ x (:a y))
            {:keys [id]} (p/trace-env)
            stored (rs/wcar* (car/lrange id 0 -1))]
        (println "Stored data from" id)
        (is (= 2 (count stored)))))))


