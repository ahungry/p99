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
   )
  (:import org.pushingpixels.substance.api.SubstanceCortex$GlobalScope)
  (:gen-class))

(log/debug "start")

;; Tries to match styles to native host some
(ss/native!)
(set! *warn-on-reflection* false)
(listeners/init!)

(def *root (atom nil))

(def normal-font "ARIAL-PLAIN-20")
(def title-font "ARIAL-14-BOLD")

(defn laf-selector []
  (ss/horizontal-panel
   :items ["Substance skin: "
           (ss/combobox
            :model    (vals (SubstanceCortex$GlobalScope/getAllSkins))
            :renderer (fn [this {:keys [value]}]
                        (ss/text! this (.getClassName value)))
            :listen   [:selection (fn [e]
                                        ; Invoke later because CB doens't like changing L&F while
                                        ; it's doing stuff.
                                    (ss/invoke-later
                                     (-> e
                                         ss/selection
                                         .getClassName
                                         SubstanceCortex$GlobalScope/setSkin)))])]))

(defn make-laf-stuff []
  (ss/border-panel
   ;; :class :xa
   :hgap 5 :vgap 5 :border 5
   :center (ss/vertical-panel
            ;; :paint paint-image
            :items [
                    :separator
                    (laf-selector)
                    (ss/text :multi-line? true
                             :text "Start"
                             ;; :text notes
                             :border 5
                             :font normal-font
                             :opaque? true
                             ;; :foreground "#ffffff"
                             ;; Yes! we can paint custom.
                             ;; Would it work with image?
                             ;; Well, almost - the painted image covers up the text...
                             ;; :paint paint-image
                             )
                    :separator
                    (ss/label :text "A Label")
                    (ss/button :text "A Button")
                    (ss/checkbox :text "A checkbox")
                    (ss/combobox :model ["A combobox" "more" "items"])
                    (ss/horizontal-panel
                     :border "Some radio buttons"
                     :items (map (partial ss/radio :text)
                                 ["First" "Second" "Third"]))
                    (ss/scrollable (ss/listbox :model (range 100)))])))

(defn a-test [e]
  (prn e)
  (ss/alert "Hello"))

(defn make-menu []
  (let [a-test (ssa/action :handler a-test :name "Test" :tip "Pop up an alert" :key "menu A")
        tab1 (ssa/action :handler (fn [_e] (ss/selection! @*root 1))
                         :name "Select Tab 1"
                         :tip "Jump to tab 1."
                         :key "menu 1")
        tab2 (ssa/action :handler (fn [_e] (ss/selection! @*root 2))
                         :name "Select Tab 1"
                         :tip "Jump to tab 1."
                         :key "menu 2")
        ]
    (ss/menubar
     :items [(ss/menu :text "File" :items [a-test])
             (ss/menu :text "Tabs" :items [tab1 tab2])])))

(defn make-main []
  (ss/tabbed-panel
   :tabs
   [
    {:title "Look and Feel" :content (make-laf-stuff)}
    ;; {:title "Switchable Canvas" :content (make-switchable-canvas)}
    ;; {:title "Paint1" :content (make-canvas-panel)}
    ;; {:title "Paint2"
    ;;  ;; :icon (slurp "close-icon.png")
    ;;  :content (make-canvas-panel2)}
    ]))


(defn show
  "REPL friendly way to pop up what we're working on."
  [f]
  (keys/init!)
  (ss/invoke-later
   (->
    (ss/frame
     :minimum-size [640 :by 480]
     :menubar (make-menu)
     :title "Widget"
     :content f)
    ;; add-behaviors
    ss/pack!
    ss/show!)
   ;; (set-listeners! f)
   ))

(defn set-root! [x] (reset! *root x) x)

(defn main [& args]
  (ss/invoke-later
   (->
    (ss/frame
     :title "Seesaw Substance/Insubstantial Example"
     :minimum-size [640 :by 480]
     :menubar (make-menu)
     :on-close :exit
     :content
     (-> (make-main) set-root!)
     )
    ss/pack!
    ss/show!)
   ;; Calling this, or setting it via REPL causes some issues...
   (SubstanceCortex$GlobalScope/setSkin "org.pushingpixels.substance.api.skin.NebulaSkin")
   ))

(log/debug "fin")
