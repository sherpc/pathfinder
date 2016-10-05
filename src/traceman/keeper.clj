(ns traceman.keeper
  (:require [mount.core :refer [defstate]]))

(defn uuid [] (str (java.util.UUID/randomUUID)))

(def empty-tracks {:path [] :id nil})

(defstate tracks
  :start (atom empty-tracks)
  :stop (reset! tracks empty-tracks))

(defn is-sub-seq?
  [sq sub]
  (->>
   (partition (count sub) 1 sq)
   (filter #(= % sub))
   empty?
   not))

(defn track-id
  [env {:keys [path id]} callstack]
  (let [cs (->> callstack (map keyword) reverse)
        same-path? (is-sub-seq? cs path)]
    (when same-path?
      id)))

(defn conj-if-distinct
  [coll x]
  (if (= x (last coll))
    coll
    (conj coll x)))

(defn push-track
  [tracks track-id caller]
  (-> tracks
      (update-in [:path] conj-if-distinct caller)
      (assoc :id track-id)))

(defn new-track
  [track-id caller]
  {:path [caller] :id track-id})

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
    (let [track-id (track-id env @tracks callstack)
          t-id (or track-id (uuid))
          context {:caller caller
                   :project-name (project-name caller)
                   :env env
                   :track-id t-id}]
      (if track-id
        (swap! tracks push-track t-id (keyword caller))
        (reset! tracks (new-track t-id (keyword caller))))
      (clojure.pprint/pprint context))))
