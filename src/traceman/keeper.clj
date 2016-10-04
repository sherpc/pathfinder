(ns traceman.keeper
  (:require [mount.core :refer [defstate]]))

(defn uuid [] (str (java.util.UUID/randomUUID)))

(defstate tracks
  :start (atom {})
  :stop (reset! tracks {}))

(defn track-id
  [all-tracks callstack]
  (->> callstack
       reverse
       (map keyword)
       (some #(% all-tracks))))

(defn project-name
  [caller]
  (some->
   caller
   (clojure.string/split #"/")
   first
   (clojure.string/split #"\.")
   first))

(defn store!
  [env [caller :as callstack]]
  (when (instance? clojure.lang.Atom tracks)
    (let [track-id (track-id @tracks callstack)
          t-id (or track-id (uuid))
          context {:caller caller
                   :project-name (project-name caller)
                   :env env
                   :track-id t-id}]
      (when-not track-id
        (swap! tracks assoc (keyword caller) t-id))
      (clojure.pprint/pprint context))))
