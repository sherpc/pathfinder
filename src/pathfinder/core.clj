(ns pathfinder.core
  (:require [pathfinder.stack-trace :as st]
            [pathfinder.keeper :as k]))

(defn trace-env*
  [env path-id]
  (k/store! env (st/callers) path-id))

(defn prepare-env
  [env]
  (let [env-pairs (for [k (keys env)]
                [(name k) k])]
    (into {} env-pairs)))

(defmacro trace-env
  []
  (let [env (prepare-env &env)]
    `(trace-env* ~env nil)))

