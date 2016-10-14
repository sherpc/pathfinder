(ns pathfinder.core
  (:require [pathfinder.stack-trace :as st]
            [pathfinder.keeper :as k]))

(defn trace-env*
  [env ttl path]
  (k/store! env (st/callers) ttl path))

(defn prepare-env
  [env]
  (let [env-pairs (for [k (keys env)]
                [(name k) k])]
    (into {} env-pairs)))

(defmacro trace-env
  []
  (let [env (prepare-env &env)]
    `(trace-env* ~env nil nil)))

(defmacro trace-env-ttl
  [ttl]
  "TTL in Java Duration format: 'PnDTnHnMn.nS.'. See https://docs.oracle.com/javase/8/docs/api/java/time/Duration.html#parse-java.lang.CharSequence- for details.
"
  (let [env (prepare-env &env)]
    `(trace-env* ~env ~ttl nil)))

(defmacro trace-path
  [path]
  (let [env (-> &env
                prepare-env
                (dissoc (str path)))]
    `(trace-env* ~env nil ~path)))

(defmacro trace-path-ttl
  [ttl path]
  (let [env (-> &env
                prepare-env
                (dissoc (str path)))]
    `(trace-env* ~env ~ttl ~path)))
