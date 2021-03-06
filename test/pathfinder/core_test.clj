(ns pathfinder.core-test
  (:require [clojure.test :refer :all]
            [mount.core :as mount]
            [pathfinder.core :as t]
            [pathfinder.keeper :refer [paths]]
            [pathfinder.test-utils :refer [db] :as tu]
            [pathfinder.test-fns :refer :all]
            [clojure.core.async :as a]))

(use-fixtures
  :each
  (fn [f]
    (->
     (mount/swap-states
      {#'pathfinder.redis.storage/tracks-saver #'pathfinder.test-utils/atom-saver})
     (mount/swap {#'pathfinder.redis.expired-listener/expired-listener nil})
     mount/start)
    (f)
    (mount/stop)))

(deftest single-thread-test
  (testing "simple let"
    (let [x 1]
      (t/trace-env)
      (is (= 1 (count @db)))
      (let [{:keys [project-name env seq-id]} (first @db)]
        (is (= "pathfinder" project-name))
        (is (= {"x" 1} env))
        (is (= 1 seq-id)))))
  (testing "let with inner let"
    (reset! db [])
    (let [y 2]
      (t/trace-env)
      (let [z 3]
        (t/trace-env)
        (let [a 4]
          (t/trace-env)
          ;;(clojure.pprint/pprint @db)
          (is (= 3 (count @db)))
          (is (= 1 (tu/paths-count @db)))
          (is (= 3 (->> @db (map :seq-id) (apply max)))))))))

(defn assert-threads-tracks
  [n generate-threads-fn]
  (reset! db [])
  (generate-threads-fn n)
  (let [tracks-by-path (group-by :path-id @db)]
    ;; in every path there is 2 tracks (for two fns)
    (is (->>
         tracks-by-path
         vals
         (map count)
         (every? #(= % 2))))
    ;; there is N track groups (aka paths)
    (is (= n (count tracks-by-path)))))

(defmacro gen-threads
  [new-thread wait-thread]
  `(fn [n#]
     (->>
      (range n#)
      (mapv (fn [_#] (~new-thread (test-fn 1 2))))
      (mapv ~wait-thread))))

(def generate-futures (gen-threads future deref))
(def generate-go-channels (gen-threads a/go #(a/<!! %)))

(deftest future-threads-test
  (testing "N futures should produce N different paths"
    (assert-threads-tracks 20 generate-futures))
  (testing "N go channels should produce N different paths"
    (assert-threads-tracks 20 generate-go-channels)))

(deftest same-path-accross-different-threads
  (testing "use path from main thread in future"
    (future-test-fn 1 2)
    (is (= 3 (count @db)))
    (is (= 1 (tu/paths-count @db)))))

(deftest recursion
  (testing "recursion should keep same path"
    (gcd 1023 858)
    (is (= 4 (count @db)))
    (is (= 1 (tu/paths-count @db)))))
