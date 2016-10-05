(ns my
  (:require [mount.core :as mount]
            [traceman.keeper :refer [tracks]]
            [traceman.core :refer [trace-env]]))

(defn go
  []
  (mount/stop)
  (mount/start))

(defn foo
  "I don't do a whole lot."
  [x]
  (trace-env)
  (println x "Hello, World!")
  (let [x 3 z 2]
    (trace-env)
    (let [{:keys [asd]} {:asd "test"}]
      (trace-env))
    :return))

(defn rock-n-roll!
  [data]
  (trace-env)
  (foo data))

(defn bar
  []
  (println "some bar, calling foo")
  (rock-n-roll! {:y 1}))

(defn another
  []
  (println "another root")
  (foo (range 5)))

(defrecord Person [fname lname])
