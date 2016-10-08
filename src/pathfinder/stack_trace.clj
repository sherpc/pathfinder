(ns pathfinder.stack-trace
  (:require [clojure.string :as s]))

(def ^:private ignored-call-stack-ns
  #{"callers" "dbg" "clojure.lang" "swank" "nrepl" "eval"})

(defn ignored?
  [{:keys [classname]}]
  (some #(re-find (re-pattern %) classname) ignored-call-stack-ns))

(defn java?
  [{:keys [classname]}]
  (.startsWith classname "java."))

(defn ->clj-format
  [classname]
  (->
   classname
   (s/replace #"\$" "/")
   (s/replace #"_STAR_" "*")
   (s/replace #"_BANG_" "!")
   (s/replace #"_" "-")))

(defn stack-trace-element
  [ste]
  {:classname (.getClassName ste)
   :file (.getFileName ste)
   :line (.getLineNumber ste)})

(defn callers
  "Return keywords seq of callstack functions."
  []
  (->>
   (Throwable.)
   .fillInStackTrace
   .getStackTrace
   (map stack-trace-element)
   (remove ignored?)
   doall
   vec
   distinct
   (remove java?)
   (map #(update % :classname ->clj-format))
   (remove #(= "pathfinder.core/trace-env*" (:classname %)))
   (map #(update % :classname keyword))))
