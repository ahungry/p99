(ns ahungry.gui.show
  (:require
   [seesaw.core :as ss]
   [ahungry.keys :as keys]
   )
  )

(defn show
  "REPL friendly way to pop up what we're working on."
  ([f]
   (keys/init!)
   (ss/invoke-later
    (->
     (ss/frame
      :minimum-size [300 :by 300]
      :title "Widget"
      :content f)
     ;; add-behaviors
     ss/pack!
     ss/show!)))
  ([f menu]
   (keys/init!)
   (ss/invoke-later
    (->
     (ss/frame
      :minimum-size [300 :by 300]
      :menubar menu
      :title "Widget"
      :content f)
     ;; add-behaviors
     ss/pack!
     ss/show!)
    ;; (set-listeners! f)
    )))
