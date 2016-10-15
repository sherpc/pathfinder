(-> (load-file "config/dynamic.clj")
    (assoc-in [:redis] {:pool {} :spec {:host "localhost" :port 6380}})
    (assoc-in [:ttl] "PT10S"))
