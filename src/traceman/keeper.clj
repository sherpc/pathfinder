(ns traceman.keeper)

(defn store!
  [env callstack]
  (println "env: ")
  (print "at\n\t")
  (println (clojure.string/join "\n\t" callstack)))
