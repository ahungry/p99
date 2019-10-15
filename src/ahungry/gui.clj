(ns ahungry.gui
  (:require
   [seesaw.core :as ss]
   [seesaw.keystroke :as sk]
   [seesaw.graphics :as ssg]
   [seesaw.color :as ssc]
   [seesaw.action :as ssa]
   [seesaw.config :as ssconfig]
   [clojure.tools.logging :as log]
   [ahungry.net :as net]
   [ahungry.keys :as keys]
   [ahungry.listeners :as listeners]
   [ahungry.gui.show :refer [show]]
   [ahungry.gui.laf :as gui.laf]
   [ahungry.gui.map :as gui.map]
   [ahungry.map.core :as map.core]
   )
  (:import org.pushingpixels.substance.api.SubstanceCortex$GlobalScope)
  (:gen-class))

(log/debug "start")

;; Tries to match styles to native host some
(ss/native!)
(set! *warn-on-reflection* false)

(defn foo [] (prn "called foo from gui"))
(defn bar [] (prn "called bar again from gui"))

(def *state (atom {:x 500
                   :y 500
                   :scale 0.1
                   :redraw-loop nil}))

(declare move-star)

(defn zoom-in []
  (swap! *state update-in [:scale] #(+ % 0.1))
  ;; (move-star)
  )

(defn zoom-out []
  (swap! *state update-in [:scale] #(- % 0.1))
  ;; (move-star)
  )

(defn move-left []
  (swap! *state update-in [:x] #(+ % 10))
  ;; (move-star)
  )

(defn move-right []
  (swap! *state update-in [:x] #(- % 10))
  ;; (move-star)
  )

(defn redraw-loop []
  (when (:redraw-loop @*state)
    (future-cancel (:redraw-loop @*state)))
  (swap! *state assoc-in [:redraw-loop]
         (future
           (while true
             (Thread/sleep 500)
             (move-star)))))

(listeners/init!
 ;; Keybinds
 {
  "a" #'foo
  "b" #'foo
  "C-b" #'bar
  "C-c" #'bar
  "i" #'zoom-in
  "o" #'zoom-out
  "h" #'move-left
  "l" #'move-right
  }
 )

(def *root (atom nil))

(def normal-font "ARIAL-PLAIN-20")
(def title-font "ARIAL-14-BOLD")

(defn a-test [e]
  (prn e)
  (ss/alert "Hello"))

(defn make-menu []
  (let [a-test (ssa/action :handler a-test :name "Test" :tip "Pop up an alert" :key "menu A")
        tab1 (ssa/action :handler (fn [_e] (ss/selection! @*root 0))
                         :name "Select Tab 0"
                         :tip "Jump to tab 0."
                         :key "menu 1")
        tab2 (ssa/action :handler (fn [_e] (ss/selection! @*root 1))
                         :name "Select Tab 1"
                         :tip "Jump to tab 1."
                         :key "menu 2")
        ]
    (ss/menubar
     :items [(ss/menu :text "File" :items [a-test])
             (ss/menu :text "Tabs" :items [tab1 tab2])])))

(def *nodes (atom {}))

(defn set-nodes! []
  (reset! *nodes {:map (gui.map/make @map.core/world-map)
                  :laf (gui.laf/make)
                  }))

;; TODO: This will probably become the way we keep redrawing the map
(defn move-star
  ([] (move-star (:x @*state)
                 (:y @*state)
                 (:scale @*state)))
  ([x y] (move-star x y 0.1))
  ([x y scale]
   (reset! gui.map/*scale scale)
   (ss/config!
    (:map @*nodes)
    :paint (gui.map/paint
            x y
            @map.core/world-map
            (map.core/player)
            ))))

(defn show-map []
  (show (gui.map/make @map.core/world-map)))

(defn make []
  (set-nodes!)
  (ss/tabbed-panel
   :tabs
   [
    {:title "Map" :content (:map @*nodes)}
    {:title "Look and Feel" :content (:laf @*nodes)}
    ;; {:title "Switchable Canvas" :content (make-switchable-canvas)}
    ;; {:title "Paint1" :content (make-canvas-panel)}
    ;; {:title "Paint2"
    ;;  ;; :icon (slurp "close-icon.png")
    ;;  :content (make-canvas-panel2)}
    ]))

;; FIXME: Most this probably belongs in a function so it doesn't run during tests.
(def x (make))
(reset! *root x)
(defn set-root! [x] (reset! *root x) x)
(.setFocusTraversalKeysEnabled x false)

(redraw-loop)

(defn main [& args]
  (ss/invoke-later
   (->
    (ss/frame
     :title "Ahungry Map"
     :minimum-size [640 :by 480]
     :menubar (make-menu)
     :on-close :exit
     :content
     (-> (make) set-root!)
     )
    ss/pack!
    ss/show!)
   ;; Calling this, or setting it via REPL causes some issues...
   (SubstanceCortex$GlobalScope/setSkin "org.pushingpixels.substance.api.skin.NebulaSkin")
   ))

(log/debug "fin")
