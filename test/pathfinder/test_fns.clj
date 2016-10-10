(ns pathfinder.test-fns
  (:require [pathfinder.core :as t]))

;; simple fns

(defn some-inner-fn
  [a b c]
  (t/trace-env)
  (+ a b c))

(defn test-fn
  [a b]
  (t/trace-env)
  (some-inner-fn a b 3))

;; fns with future

(defn future-inner-fn
  [path x]
  (let [z {:x x :inc (inc x)}]
    (t/trace-path path)))

(defn future-test-fn
  [a b]
  (let [p (t/trace-env)]
    @(future
       (let [x (+ a b)]
         (t/trace-path p)
         (future-inner-fn p x)))))

;; recursion

(defn gcd
  [a b]
  (t/trace-env)
  (if (zero? b)
    a
    (recur b (mod a b))))

;; destructing

(defn fnk-sum-and-gcd
  [{:keys [x y] :as input}]
  (t/trace-env)
  [input (+ x y) (gcd x y)])

;; all

(defn run-all
  []
  (test-fn 1 2)
  (future-test-fn 1 2)
  (gcd 1023 858)
  (fnk-sum-and-gcd {:x 1023 :y 858}))
