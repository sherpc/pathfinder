(ns pathfinder.redis.query-test
  (:require [pathfinder.redis.query :as rq]
            [pathfinder.query :as q]
            [pathfinder.redis.storage :as rs]
            [pathfinder.redis.connection :refer [wcar*]]
            [pathfinder.core :as p]
            [pathfinder.test-utils :refer [dev-config] :as tu]
            [pathfinder.test-fns :as tf]
            [mount.core :as mount]
            [taoensso.carmine :as car]
            [clojure.test :refer :all]))

(use-fixtures
  :each
  tu/with-mount-dev-config
  tu/with-redis-flush)

(deftest last-n
  (testing "with N paths should be N paths with last-n call"
    (tf/some-inner-fn 1 2 3)
    (tf/test-fn 5 6)
    (let [result (q/last-n rq/tracks-query-handler 1000)]
      (is (= 2 (count result))))))

(deftest last-n-buffer
  (testing "when pushed more than last-n-buffer-size items, in buffer should remain only last last-n-buffer-size items"
    (let [buffer-size (:last-n-buffer-size rs/tracks-saver)
          n (+ buffer-size 10)]
      (doseq [i (range n)]
        (tf/test-fn i (inc i)))
      (let [last-n (q/last-n rq/tracks-query-handler 1000)]
        (is (= buffer-size (count last-n)))))))

(defn x-y-fn
  [x y]
  (:id (p/trace-env)))

(defn is=
  [expected actual]
  (is (= expected actual)))

(deftest search
  (let [x 1
        y 2
        other-y 3
        f-id @(future (x-y-fn x y))
        s-id (x-y-fn x other-y)]
    (testing "when trace integer, should be able to search it"
      (->>
       (str "y:" y)
       (q/search rq/tracks-query-handler)
       (is= #{f-id}))
      )
    (testing "we should be able to search two tracks by same param"
      (->>
       (str "x:" 1)
       (q/search rq/tracks-query-handler)
       (is= #{f-id s-id})))
    (testing "we can use multiple params separated by space as AND meaning"
      (->>
       (format "x:%s y:%s" 1 2)
       (q/search rq/tracks-query-handler)
       (is= #{f-id})))))
