(ns ahungry.gui.laf
  (:require
   [seesaw.core :as ss]
   [seesaw.keystroke :as sk]
   [seesaw.graphics :as ssg]
   [seesaw.color :as ssc]
   [seesaw.action :as ssa]
   [seesaw.config :as ssconfig]
   [clojure.tools.logging :as log]
   )
  (:import org.pushingpixels.substance.api.SubstanceCortex$GlobalScope)
  (:gen-class))

(log/debug "start")

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

(log/debug "fin")
