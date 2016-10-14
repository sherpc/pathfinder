(-> (load-file "config/dynamic.clj")
    (assoc-in [:redis] {:pool {} :spec {:host "localhost" :port 6379}}))
