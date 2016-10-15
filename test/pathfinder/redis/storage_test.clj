(ns pathfinder.redis.storage-test
  (:require [pathfinder.redis.storage :as rs]
            [pathfinder.redis.connection :refer [wcar*]]
            [pathfinder.core :as p]
            [pathfinder.test-utils :refer [dev-config] :as tu]
            [mount.core :as mount]
            [taoensso.carmine :as car]
            [clojure.test :refer :all]
            [pathfinder.test-fns :as tf]))

(use-fixtures
  :each
  tu/with-mount-dev-config
  tu/with-redis-flush)

(defn tracks-by-id
  [id]
  (wcar* (car/lrange id 0 -1)))

(deftest ping-test
  (testing "simple redis ping"
    (is (= ["PONG" "OK" {:a 1}] (rs/ping)))))

(deftest one-path-test
  (testing "eval trace in let, should be in redis; after timeout it should be removed"
    (let [x 1 y {:a 2}]
      (let [{:keys [id]} (p/trace-env-ttl "PT1S")
            stored (tracks-by-id id)]
        (println "Stored data from" id)
        (is (= 1 (count stored)))
        ;; wait for 1s ttl
        (Thread/sleep (* 1000 2))
        (is (= [] (tracks-by-id id))))))
  (testing "second track should prolongate path expiration"
    (let [x 2 y {:a 3}]
      (p/trace-env-ttl "PT2S")
      (Thread/sleep 1000)
      (let [z (+ x (:a y))
            {:keys [id]} (p/trace-env-ttl "PT1S")
            stored (tracks-by-id id)]
        (println "Stored data from" id)
        (is (= 2 (count stored)))
        (Thread/sleep 1500)
        (is (= [] (tracks-by-id id)))))))


