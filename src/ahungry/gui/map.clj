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

(def *scale (atom 0.1))

(defn scale-data [{:keys [x1 x2 y1 y2]}]
  {:x1 (* @*scale (read-string x1))
   :x2 (* @*scale (read-string x2))
   :y1 (* @*scale (read-string y1))
   :y2 (* @*scale (read-string y2))})

(defn scale-player [{:keys [x y]}]
  {:x (* @*scale (read-string x))
   :y (* @*scale (read-string y))})

(defn line->polygon [{:keys [x1 x2 y1 y2]}]
  (ssg/polygon [x1 y1] [x2 y2]))

(defn zone->lines
  "Takes a collection of lines that look like those in the
  ahungry.map.parser/parse-zone response and draws as lines."
  [points g2d]
  ;; TODO: Do something with 't' (the type)
  (doseq [{:keys [b g r t] :as m} points]
    (try
      (when (= "L" t)
        (ssg/draw g2d
                  (line->polygon (scale-data m))
                  (ssg/style :foreground java.awt.Color/BLACK
                             :background (ssc/color (read-string r)
                                                    (read-string g)
                                                    (read-string b))
                             :stroke (ssg/stroke :width 1))
                  ))
      ;; TODO Render the fonts
      (when (= "P" t)
        ;; (prn "Render a string at x1 y1: " (str g))
        nil
        )
      ;; Bunch of NPE happening here, hm...
      (catch Exception e (log/error (str e))))
    ))

(defn paint
  [offset-x offset-y points player]
  (fn [c g]
    (ssg/push
     g
     ;; Translate to the map offsets we require
     (ssg/translate g offset-x offset-y)
     ;; Draw the map lines
     (zone->lines points g)
     ;; Now draw an indicator for where player should be
     (let [{:keys [x y]} (scale-player player)]
       ;; (log/info x y)
       ;; Oh yes, player coords are inverted.  Wonderful.
       (ssg/translate g (* -1 x) (* -1 y)))
     (ssg/draw g star
               (ssg/style :foreground java.awt.Color/BLACK
                          :background java.awt.Color/YELLOW))
     )))

(defn make
  ([] (make []))
  ([points]
   (ss/canvas :id :map
              ;; :background "#"
              :paint (paint 500 500 points {:x "0" :y "0"})
              )))

(log/debug "fin")
