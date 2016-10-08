(ns pathfinder.core
  (:require [pathfinder.stack-trace :as st]
            [pathfinder.keeper :as k]))

(defn trace-env*
  [env]
  (k/store! env (st/callers)))

(defmacro trace-env
  []
  (let [env-v (for [k (keys &env)]
                [(name k) k])
        env (into {} env-v)]
    `(trace-env* ~env)))

