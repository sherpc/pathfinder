(ns pathfinder.core-test
  (:require [clojure.test :refer :all]
            [mount.core :as mount]
            [pathfinder.core :as t]
            [pathfinder.keeper :refer [paths]]
            [pathfinder.test-utils :refer [db] :as tu]

            [clojure.core.async :as a]))

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

(defn future-test-fn
  [a b]
  (let [p (t/trace-env)]
    @(future
       (let [x (+ a b)]
         (t/trace-path p)
         x))))

(deftest same-path-accross-different-threads
  (testing "use path from main thread in future"
    (future-test-fn 1 2)
    (clojure.pprint/pprint @db)
    (is (= 2 (count @db)))))
