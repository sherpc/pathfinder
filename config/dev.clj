(-> (load-file "config/dynamic.clj")
    (assoc-in [:redis] {:pool {} :spec {:host "localhost" :port 6379}})
    (assoc-in [:ttl] "PT10S"))
