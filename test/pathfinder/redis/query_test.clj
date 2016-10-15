(ns pathfinder.redis.query-test
  (:require [pathfinder.redis.query :as rq]
            [pathfinder.query :as q]
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
    (let [result (q/last-n rq/tracks-query-handler 10)]
      (is (= 2 (count result))))))
