(ns pathfinder.core-test
  (:require [clojure.test :refer :all]
            [mount.core :as mount]
            [pathfinder.core :as t]
            [pathfinder.keeper :refer [paths]]
            [pathfinder.test-utils :refer [db] :as tu]))

(use-fixtures
  :each
  (fn [f]
    (mount/start-with-states
     {#'pathfinder.storage/tracks-saver #'pathfinder.test-utils/atom-saver})
    (f)
    (mount/stop)))

(deftest single-thread-test
  (testing "simple let"
    (let [x 1]
      (t/trace-env)
      (is (= 1 (count @db)))))
  (testing "let with inner let"
    (reset! db [])
    (let [y 2]
      (t/trace-env)
      (let [z 3]
        (t/trace-env)
        (let [a 4]
          (t/trace-env)
          (is (= 3 (count @db)))
          (is (= 1 (tu/tracks-count @db)))
          (is (= 3 (->> @db (map :seq-id) (apply max)))))))))

(defn some-inner-fn
  [a b c]
  (t/trace-env)
  (+ a b c))

(defn test-fn
  [a b]
  (t/trace-env)
  (some-inner-fn a b 3))

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

(defn generate-futures
  [n]
  (let [fs (mapv
            (fn [_] (future (test-fn 1 2)))
            (range n))]
    (while (not (every? realized? fs))
      (Thread/sleep 100))))

(deftest future-threads-test
  (testing "N futures should produce N different paths"
    (assert-threads-tracks 20 generate-futures)))
