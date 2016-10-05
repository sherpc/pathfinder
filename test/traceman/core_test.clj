(ns traceman.core-test
  (:require [clojure.test :refer :all]
            [mount.core :as mount]
            [traceman.core :as t]
            [traceman.keeper :refer [tracks]]
            [traceman.test-utils :refer [db] :as tu]))

(use-fixtures
  :each
  (fn [f]
    (mount/start-with-states
     {#'traceman.storage/tracks-saver #'traceman.test-utils/atom-saver})
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
        (is (= 2 (count @db)))
        (is (= 1 (tu/tracks-count @db)))))))

(defn some-inner-fn
  [a b c]
  (t/trace-env)
  (+ a b c))

(defn test-fn
  [a b]
  (t/trace-env)
  (some-inner-fn a b 3))

(deftest future-threads-test
  (testing "two futures should produce different tracks"
    (println @tracks)
    (let [f1 (future (test-fn 1 2))
          f2 (future (test-fn 1 2))]
      (while (not
              (and
               (realized? f1)
               (realized? f2)))
        (Thread/sleep 100))
      (is (= 6 @f1 @f2))
      (clojure.pprint/pprint @db)
      (is (->>
           @db
           (group-by :track-id)
           vals
           (map count)
           (every? #(= % 2)))))))
