(ns ahungry.gui.timers
  (:require
   [seesaw.core :as ss]
   [seesaw.keystroke :as sk]
   [seesaw.graphics :as ssg]
   [seesaw.color :as ssc]
   [seesaw.action :as ssa]
   [seesaw.config :as ssconfig]
   [clojure.tools.logging :as log]
   [ahungry.gui.show :refer [show]]
   [ahungry.events :as e :refer [fire listen]]
   )
  (:gen-class))

(defn make-labels []
  (ss/vertical-panel
   :items (map (partial ss/button :text)
               ["An orc" "A kobold"])))

(defn make []
  (ss/border-panel
   ;; :class :xa
   :hgap 5 :vgap 5 :border 5
   :center (ss/vertical-panel
            ;; :paint paint-image
            :items
            [
             :separator
             (ss/scrollable (make-labels))
             :separator
             ])))
