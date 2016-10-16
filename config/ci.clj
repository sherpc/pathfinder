(-> (load-file "config/dynamic.clj")
    (assoc-in [:redis] {:pool {} :spec {:host "localhost" :port 6380}})
    (assoc-in [:ttl] "PT10S")
    (assoc-in [:redis-saver] {:last-n-buffer-size 10}))
