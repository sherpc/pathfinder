(ns pathfinder.web
  (:require [mount.core :refer [defstate]]
            [clojure.tools.logging :as log]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [compojure.handler :as handler]
            [ring.util.response :refer [response]]
            [ring.middleware.json :as json-m]
            [ring.adapter.jetty :as j]

            [pathfinder.query :refer [tracks-query-handler] :as q]))

(defroutes app-routes
  (GET "/" [] (response (q/search tracks-query-handler {})))
  (route/not-found {:error "not found"}))

(def app
  (->
   (handler/site app-routes)
   json-m/wrap-json-response
   json-m/wrap-json-params))

(defn main-handler
  [req]
  (let [{:keys [uri request-method]} req]
    (if (and
         (= uri "/metrics")
         (= request-method :get))
      {:status 200 :body "hi"}
      {:status 404})))

(defn create-web-server
  []
  (log/debug "Starting web server")
  (j/run-jetty app {:port (Integer/parseInt
                           (or
                            (System/getenv "WEB_SERVER_PORT")
                            "8386"))
                    :join? false}))
(defstate web-server
  :start (create-web-server)
  :stop (do
          (log/debug "Stopping web server...")
          (.stop web-server)
          (log/debug "Stopped.")))
