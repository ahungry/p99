(ns ahungry.gui.map
  (:require
   [seesaw.core :as ss]
   [seesaw.keystroke :as sk]
   [seesaw.graphics :as ssg]
   [seesaw.color :as ssc]
   [seesaw.action :as ssa]
   [seesaw.config :as ssconfig]
   [clojure.tools.logging :as log]
   [ahungry.gui.show :refer [show]]
   )
  (:gen-class))

(log/debug "start")

(def star
  (ssg/path []
            (move-to 0 20) (line-to 5 5)
            (line-to 20 0) (line-to 5 -5)
            (line-to 0 -20) (line-to -5 -5)
            (line-to -20 0) (line-to -5 5)
            (line-to 0 20)))

(defn paint
  [x y]
  (fn [c g]
    (ssg/push
     g
     (ssg/translate g x y)
     (ssg/draw g star
               (ssg/style :foreground java.awt.Color/BLACK
                          :background java.awt.Color/YELLOW)))))

(defn make []
  (ss/canvas :id :map
             ;; :background "#"
             :paint (paint 100 100)
             ))

(log/debug "fin")
