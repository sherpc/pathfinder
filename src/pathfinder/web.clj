(ns pathfinder.web
  (:require [mount.core :refer [defstate]]
            [clojure.tools.logging :as log]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [compojure.handler :as handler]
            [ring.util.response :refer [response header]]
            [ring.middleware.json :as json-m]
            [ring.adapter.jetty :as j]
            [pathfinder.redis.query :refer [tracks-query-handler]]
            [pathfinder.query :as q]))

(defn last-n
  [n]
  (response
   (let [safe-n (or (and n (Integer/parseInt n)) 10)]
     (q/last-n tracks-query-handler safe-n))))

(defroutes app-routes
  (GET "/stats" [] (response (q/stats tracks-query-handler)))
  (GET "/:n" [n] (last-n n))
  (GET "/" [] (last-n nil))
  (route/not-found {:error "not found"}))

(defn wrap-cors
  [handler]
  (fn [req]
    (->
     req
     handler
     (header "Access-Control-Allow-Origin" "*"))))

(def app
  (->
   (handler/site app-routes)
   json-m/wrap-json-response
   json-m/wrap-json-params
   wrap-cors))

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
