(ns ahungry.gui.auctions
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

(def *state (atom {:auctions []}))

(defn make-labels []
  (ss/vertical-panel
   :id :auctions
   :items (map (partial ss/button :text)
               (:auctions @*state)
               ;; ["An orc" "A kobold"]
               )))

(def auctions-labels (make-labels))

(defn redraw []
  (ss/config!
   (ss/select auctions-labels [:#auctions])
   :items (map (partial ss/button :text) (:auctions @*state))))

(defn push-to-auc-state
  "Push a message to top of list, keeping the latest 5."
  [xs msg]
  (->>
   (cons msg xs)
   (take 5)))

(defn ack-auctions [msg]
  (when msg
    (swap! *state update-in [:auctions] #'push-to-auc-state msg)
    (redraw)))

(listen :ev-auctions #'ack-auctions)

(defn make []
  (ss/border-panel
   ;; :class :xa
   :hgap 5 :vgap 5 :border 5
   :center (ss/vertical-panel
            ;; :paint paint-image
            :items
            [
             :separator
             (ss/scrollable auctions-labels)
             :separator
             ])))
