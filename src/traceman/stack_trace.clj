(ns traceman.stack-trace
  (:require [clojure.string :as s]))

(def ^:private ignored-call-stack-ns
  #{"callers" "dbg" "clojure.lang" "swank" "nrepl" "eval"})

(defn ignored?
  [classname]
  (some #(re-find (re-pattern %) classname) ignored-call-stack-ns))

(defn java?
  [classname]
  (.startsWith classname "java."))

(defn ->clj-format
  [classname]
  (->
   classname
   (s/replace #"\$" "/")
   (s/replace #"_STAR_" "*")
   (s/replace #"_BANG_" "!")
   (s/replace #"_" "-")))

(defn callers
  []
  (->>
   (Throwable.)
   .fillInStackTrace
   .getStackTrace
   (map #(-> % .getClassName str))
   (remove ignored?)
   doall
   vec
   distinct
   (remove java?)
   (map ->clj-format)
   (remove #(= "traceman.core/trace-env*" %))))
