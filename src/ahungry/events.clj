(ns ahungry.events
  (:require
   [clojure.tools.logging :as log]
   [clojure.core.async :refer [chan pub sub >!! >! <!! <! go-loop go]]))

(def input-chan (chan))
(def our-pub (pub input-chan :msg-type))
;; (def output-chan (chan))

(defn fire
  "Send in a message / payload that we'll publish to one or more listeners."
  [topic m]
  (>!! input-chan {:msg-type topic :data m}))

(defn listen
  "Recv a message / payload that we'll run some function against."
  [topic f]
  ;; pub topic channel
  (let [output-chan (chan)]
    (sub our-pub topic output-chan)
    (go-loop []
      (let [{:keys [data]} (<! output-chan)]
        ;; (log/info "Received an event on topic: " {:topic topic :data data})
        (f data)
        (recur)))))

(listen :foo prn)
(listen :foo (fn [x] (prn "Hello from the second one" x)))

;; Called first, it only runs code from the initial listen
(fire :foo 44)
;; If I call a second time, it then runs the second function

;; TODO: Maybe source these via the gui?  Some type of triggers user wants.
(defn slain? [s]
  (some->> (re-matches #".*You have slain (.*?)!" s)
           (rest)
           (zipmap [:name])))

;; Chain some events from other events here, sure, why not?
(defn line-handler [s]
  (cond (slain? s)
        (fire :slain (slain? s)))
  )

(listen :read-line line-handler)

(listen :slain (fn [m] (log/info "Oh snap! Someone was slain!" m)))
