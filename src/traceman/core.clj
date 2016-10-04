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

(defn foo
  "I don't do a whole lot."
  [x]
  (trace-env)
  (println x "Hello, World!")
  (let [x 3 z 2]
    (trace-env)))

(defn rock-n-roll!
  [data]
  (trace-env)
  (foo data))

(defn bar
  []
  (println "some bar, calling foo")
  (rock-n-roll! {:y 1}))
