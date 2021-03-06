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
   ;; [ahungry.gui.laf :as gui.laf]
   [ahungry.gui.map :as gui.map]
   [ahungry.gui.auctions :as gui.auctions]
   [ahungry.gui.timers :as gui.timers]
   [ahungry.map.core :as map.core]
   [ahungry.auctions.core :as auction]
   )
  ;; (:import org.pushingpixels.substance.api.SubstanceCortex$GlobalScope)
  (:gen-class))

(log/debug "start")

;; Tries to match styles to native host some
(ss/native!)
(set! *warn-on-reflection* false)

(defn foo [] (prn "called foo from gui"))
(defn bar [] (prn "called bar again from gui"))

(def *state (atom {:x 250
                   :y 250
                   :scale 0.1
                   :moving-star? false
                   :redraw-loop nil}))

(declare move-star)

(defn zoom-in []
  (swap! *state update-in [:scale] #(min (* % 2) 5))
  (move-star))

(defn zoom-out []
  (swap! *state update-in [:scale] #(max (/ % 2) 0.01))
  (move-star))

(defn move-left []
  (swap! *state update-in [:x] #(+ % 50))
  (move-star))

(defn move-right []
  (swap! *state update-in [:x] #(- % 50))
  (move-star))

(defn move-down []
  (swap! *state update-in [:y] #(- % 50))
  (move-star))

(defn move-up []
  (swap! *state update-in [:y] #(+ % 50))
  (move-star))

(def ^:dynamic *log-loop* true)
(def ^:dynamic *log-delay* 500)
(def ^:dynamic *auction-loop* true)
(def ^:dynamic *auction-delay* 30000)
(def ^:dynamic *redraw-loop* true)
(def ^:dynamic *sleep-delay* 1000)

(defn log-load-loop
  "Keep the incoming log data fresh."
  []
  (when (:log-loop @*state)
    (future-cancel (:log-loop @*state)))
  (swap! *state assoc-in [:log-loop]
        (future
          (while *log-loop*
            (Thread/sleep *log-delay*)
            (ahungry.fs.logs/log-load)))))

(defn auction-loop []
  (when (:auction-loop @*state)
    (future-cancel (:auction-loop @*state)))
  (swap! *state assoc-in [:auction-loop]
         (future
           (while *auction-loop*
             (Thread/sleep *auction-delay*)
             (auction/post-auctions)))))

(defn redraw-loop []
  (when (:redraw-loop @*state)
    (future-cancel (:redraw-loop @*state)))
  (swap! *state assoc-in [:redraw-loop]
         (future
           (while *redraw-loop*
             (keys/reset-modkeys!)
             (Thread/sleep *sleep-delay*)
             (move-star)))))

(defn move-reset []
  (swap! *state (fn [m]
                  (-> m
                      (assoc-in [:x] 50)
                      (assoc-in [:y] 50)
                      (assoc-in [:scale] 0.1)))))

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
  "j" #'move-down
  "k" #'move-up
  "r" #'move-reset
  }
 )

(def *root (atom nil))

(def normal-font "ARIAL-PLAIN-20")
(def title-font "ARIAL-14-BOLD")

(defn a-test [e]
  (prn e)
  (ss/alert "Hello"))

(defn make-menu []
  (let [a-test (ssa/action
                :handler a-test
                :name "Test"
                :tip "Pop up an alert"
                :key "menu A")
        tab1 (ssa/action
              :handler (fn [_e] (ss/selection! @*root 0))
              :name "Select Tab 0"
              :tip "Jump to tab 0."
              :key "menu 1")
        tab2 (ssa/action
              :handler (fn [_e] (ss/selection! @*root 1))
              :name "Select Tab 1"
              :tip "Jump to tab 1."
              :key "menu 2")
        ]
    (ss/menubar
     :items [(ss/menu :text "File" :items [a-test])
             (ss/menu :text "Tabs" :items [tab1 tab2])])))

(def *nodes (atom {}))

(defn set-nodes! []
  (reset! *nodes {:map (gui.map/make
                        @map.core/world-map
                        (map.core/player-zone-name))
                  :auctions (gui.auctions/make)
                  ;; :laf (gui.laf/make)
                  :timers (gui.timers/make)
                  }))

;; TODO: This will probably become the way we keep redrawing the map
(defn move-star
  ([]
   (when-not (:moving-star? @*state)
     (swap! *state assoc-in [:moving-star?] true)
     (move-star (:x @*state)
                (:y @*state)
                (:scale @*state))
     (swap! *state assoc-in [:moving-star?] false)))
  ([x y] (move-star x y 0.1))
  ([x y scale]
   ;; Ensure we push down proper scale.
   (reset! gui.map/*scale scale)
   ;; See if we switched zones.
   (let [in-zone (map.core/player-zone)]
     (when-not (= (:zone @*state)
                  in-zone)
       (map.core/update-world-map!)
       (swap! *state assoc-in [:zone] in-zone)))
   (ss/config!
    (ss/select (:map @*nodes) [:#map])
    :paint
    (gui.map/paint
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
    {:title "Auction Uploads" :content (:auctions @*nodes)}
    {:title "Timers" :content (:timers @*nodes)}
    ;; {:title "Look and Feel" :content (:laf @*nodes)}
    ;; {:title "Switchable Canvas" :content (make-switchable-canvas)}
    ;; {:title "Paint1" :content (make-canvas-panel)}
    ;; {:title "Paint2"
    ;;  ;; :icon (slurp "close-icon.png")
    ;;  :content (make-canvas-panel2)}
    ]))

(def x (make))
(reset! *root x)
(defn set-root! [x] (reset! *root x) x)
(.setFocusTraversalKeysEnabled x false)

(defn boot []
  (log-load-loop)
  (auction-loop)
  (redraw-loop))

(defn foo []
  (+ 3 4 5))

(defn main [& args]
  (boot)
  (ss/invoke-later
   (->
    (ss/frame
     :title "Ahungry Map"
     :width 600
     :height 600
     :minimum-size [300 :by 300]
     :menubar (make-menu)
     :on-close :exit
     :content
     ;; (-> (make) set-root!)
     (-> x set-root!)
     )
    ;; ss/pack!
    ss/show!)
   ;; Calling this, or setting it via REPL causes some issues...
   ;; (SubstanceCortex$GlobalScope/setSkin "org.pushingpixels.substance.api.skin.NebulaSkin")
   ))

(log/debug "fin")
