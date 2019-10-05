(ns ahungry.p99
  (:require
   [ahungry.gui :as gui]
   )
  (:gen-class))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (apply gui/main args)
  (println "Hello, World!"))
