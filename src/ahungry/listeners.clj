(ns ahungry.listeners
  (:require
   [clojure.tools.logging :as log]
   [ahungry.keys :as keys]
   ))

(log/debug "start")

(def *keymap (atom {}))

(defn dispatch-event [^java.awt.event.KeyEvent e]
  (try
    (let [id (.getID e)]
      (cond
        (= id java.awt.event.KeyEvent/KEY_PRESSED)
        ((keys/handle-key-pressed
          @*keymap
          ;; keys/global-keymap
          ) e)

        (= id java.awt.event.KeyEvent/KEY_RELEASED)
        (keys/handle-key-released e)

        :else
        nil
        ;; (prn "Some unmapped key event...")
        ))
    (catch Exception e (prn e))))

(defn set-listeners!
  "Global listener handling irregardless of what is focused."
  []
  (let [manager (java.awt.KeyboardFocusManager/getCurrentKeyboardFocusManager)
        dispatcher (reify java.awt.KeyEventDispatcher
                     (dispatchKeyEvent [this e]
                       (dispatch-event e)
                       false))]
    (doto manager
      (.addKeyEventDispatcher dispatcher))))

(defonce listeners (set-listeners!))

(defn init!
  "Just a stub for now - nothing needs to be done delayed."
  [keymap]
  (reset! *keymap keymap))

(log/debug "fin")
