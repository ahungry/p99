(ns ahungry.map.core
  (:require
   [ahungry.map.parser :as parser]
   ))

;; Some are in brewall (butcher)
;; Some are in the dist
(defn init-lines
  []
  ;; (fs/parse-current-zone)
  (parser/parse-zone "ecommons")
  ;; (str
  ;;  ;; "/home/mcarter/Downloads/brewall/"
  ;;  ;; "/home/mcarter/src/ahungry-map/res/maps/"
  ;;  "resources/maps/"
  ;;  (str (fs/get-current-zone) ".txt")
  ;;  ;; "butcher.txt"
  ;;  ;; "ecommons.txt"
  ;;  )
  )

(def world-map (atom (init-lines)))
