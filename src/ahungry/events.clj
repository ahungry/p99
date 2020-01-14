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
  (future (>!! input-chan {:msg-type topic :data m})))

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

(listen :ev-foo prn)
(listen :ev-foo (fn [x] (prn "Hello from the second one" x)))

;; Called first, it only runs code from the initial listen
(fire :ev-foo 44)
;; If I call a second time, it then runs the second function

;; TODO: Maybe source these via the gui?  Some type of triggers user wants.
(defn slain? [s]
  (some->> (re-matches #".*You have slain (.*?)!" s)
           (rest)
           (zipmap [:name])))

(defn last-entered-zone? [s]
  (some->>
   (re-matches #".*You have entered (.*)\." s)
   rest
   (zipmap [:name])))

(defn player-position? [s]
  (some->>
   (re-find #".*Your Location is (.*?), (.*?), (.*)$" s)
   (zipmap [:_ :y :x :z])))

;; Chain some events from other events here, sure, why not?
(defn line-handler
  "Dispatch based on line match."
  [s]
  (cond
    ;; If we use fire inside a go block (such as this) bad things!
    ;; It will block indefinitely essentially.
    (slain? s) (fire :ev-slain (slain? s))

    (last-entered-zone? s) (fire :ev-last-entered-zone (last-entered-zone? s))

    (player-position? s) (fire :ev-player-position (player-position? s))

    )
  ;; (cond (slain? s)
  ;;       (prn (slain? s))
  ;;       ;; (fire :ev-slain (slain? s))
  ;;       )
  )

(listen :ev-read-line #'line-handler)
