(ns traceman.core
  (:require [traceman.stack-trace :as st]
            [traceman.keeper :as k]))

(defn trace-env*
  [env]
  (k/store! env (st/callers)))

(defmacro trace-env
  []
  (let [env-v (for [k (keys &env)]
                [(name k) k])
        env (into {} env-v)]
    `(trace-env* ~env)))

