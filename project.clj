(defproject pathfinder "0.1.0-SNAPSHOT"
  :description "Easy tracking of function calls and context variables."
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [clj-time "0.12.0"]
                 [mount "0.1.10"]

                 ;; redis storage
                 [com.taoensso/carmine "2.14.0"]

                 ;; only for testing purposes
                 [org.clojure/core.async "0.2.391"]
                 ])
