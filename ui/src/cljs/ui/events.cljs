(ns ui.events
    (:require [re-frame.core :as re-frame]
              [ui.db :as db]))

(re-frame/reg-event-db
 :initialize-db
 (fn  [_ _]
   db/default-db))
