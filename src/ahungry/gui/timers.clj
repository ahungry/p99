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

;; TODO Track objects not just names (include timers etc.).
(def *state (atom {:slain []}))

(defn make-labels []
  (ss/vertical-panel
   :id :slain
   :items (map (partial ss/button :text)
               (:slain @*state)
               ;; ["An orc" "A kobold"]
               )))

(def slain-labels (make-labels))

(defn redraw []
  (ss/config!
   (ss/select slain-labels [:#slain])
   :items (map (partial ss/button :text) (:slain @*state))))

(defn ack-slain [{:keys [name]}]
  (when name
    (swap! *state update-in [:slain] conj name)
    (redraw)))

(listen :slain #'ack-slain)

(defn make []
  (ss/border-panel
   ;; :class :xa
   :hgap 5 :vgap 5 :border 5
   :center (ss/vertical-panel
            ;; :paint paint-image
            :items
            [
             :separator
             (ss/scrollable slain-labels)
             :separator
             ])))
