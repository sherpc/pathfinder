(ns pathfinder.keeper
  (:require [mount.core :refer [defstate]]
            [pathfinder.storage :refer [tracks-saver] :as storage]))

(defn uuid [] (str (java.util.UUID/randomUUID)))

(defstate paths
  :start (atom {})
  :stop (reset! paths {}))

(defn is-sub-seq?
  [sq sub]
  (->>
   (partition (count sub) 1 sq)
   (filter #(= % sub))
   empty?
   not))

(defn path-id
  [env {:keys [path id seq-id] :as thread-path} callstack]
  (when (and
         thread-path
         (-> callstack reverse (is-sub-seq? path)))
    [id seq-id]))

(defn conj-if-distinct
  [coll x]
  (if (= x (last coll))
    coll
    (conj coll x)))

(defn conj-path
  [path caller]
  (-> path
      (update :path conj-if-distinct caller)
      (update :seq-id inc)))

(defn new-path
  [caller]
  {:path [caller] :id (uuid) :seq-id 1})

(defn project-name
  [caller]
  (some->
   caller
   name
   (clojure.string/split #"/")
   first
   (clojure.string/split #"\.")
   first))

(defn store!
  "Should not be called in parallel in same thread. Returns path id."
  [env [caller :as callstack]]
  (when (instance? clojure.lang.Atom tracks)
    (let [thread-id (.getId (Thread/currentThread))
          thread-path (get @paths thread-id)
          [path-id seq-id] (path-id env thread-track callstack)
          path (if path-id
                 (conj-path thread-path caller)
                 (new-path caller))
          track {:caller caller
                 :project-name (project-name caller)
                 :env env
                 :path-id (:id path)
                 :seq-id (:seq-id path)}]
      (swap! tracks assoc thread-id path)
      (storage/save! tracks-saver track)
      (:id path))))
