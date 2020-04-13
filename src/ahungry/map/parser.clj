(ns ahungry.map.parser
  (:require
   [clojure.tools.logging :as log]
   [ahungry.events :as e :refer [fire listen]]
   [ahungry.fs.logs :as logs]
   [ahungry.fs.zones :as zones]
   )
  )

;; Code related to parsing the files on disk.

;; (parse-map-lines "/home/mcarter/src/ahungry-map/res/maps/tutorialb.txt")
;; Line format is as such:
;; L 1186.0742, -2175.0840, 3.1260,  1215.0065, -2174.9312, 3.1260,  150, 0, 200
;; L -12.7163, 162.0129, 0.0020,  12.6721, 162.0129, 0.0020,  0, 0, 0
;; Hmm, note that label ones ignore the xyz2 so would be in slot :g
;; P 624.6537, 2031.0975, 90.6260,  0, 0, 0,  3,  to_The_Estate_of_Unrest
(defn parse-line [s]
  (->> (clojure.string/split s #",* +")
       (zipmap [:t :x1 :y1 :z1 :x2 :y2 :z2 :r :g :b :a :label])))

(defn parse-map-lines [file-content]
  (->>
   (->
    (clojure.string/replace file-content #"\r" "")
    (clojure.string/split #"\n"))
   (map parse-line)))

(defn parse-zone [zone]
  (when zone
    (-> (zones/load-map zone)
        parse-map-lines)))

(defn parse-zone-label-from-log-line [s]
  (last (re-find #".*You have entered (.*)\." s)))

(defn get-last-entered-zone-slow
  "This is a slower way to get the zone, it parses
  all lines in the file."
  []
  (let [content (logs/get-content-of-newest-file)]
    (->> (clojure.string/split content #"\r\n")
         (filter #(re-matches #".*You have entered (.*)\." %))
         last
         parse-zone-label-from-log-line)))

(defn get-last-entered-zone-fast
  "This is the faster way to get the zone, but could end up
  not finding a match."
  []
  (let [content (logs/get-lines-of-newest-file)]
    (->> content
         (filter #(re-matches #".*You have entered (.*)\." %))
         last
         parse-zone-label-from-log-line
         )))

(def *state (atom {
                   :last-entered-zone ""
                   :player-position nil
                   } ))

(defn ack-last-entered-zone [{:keys [name]}]
  (when name
    (swap! *state assoc-in [:last-entered-zone] name)))

(listen :ev-last-entered-zone #'ack-last-entered-zone)

(defn ack-player-position [{:keys [x y z] :as m}]
  (when m
    (swap! *state assoc-in [:player-position] {:x x :y y :z z})))

(listen :ev-player-position #'ack-player-position)

(defn get-last-entered-zone-from-state []
  (:last-entered-zone @*state))

(defn get-current-position-from-state []
  (:player-position @*state))

;; Using the parsing based method, 62ms
(defn get-last-entered-zone-from-disk []
  (try
    (or (get-last-entered-zone-fast)
        (get-last-entered-zone-slow))
    (catch Exception e
      (log/debug "Error loading in get-last-entered-zone: " e)
      (get-last-entered-zone-slow))))

(defn get-last-entered-zone []
  (or (get-last-entered-zone-from-state)
      (get-last-entered-zone-from-disk)))

(defn get-zone-id-from-label [label]
  (let [zl (zones/get-zonelist)]
    (first (filter #(= (:label %) label) zl))))

(defn get-current-zone []
  (let [zone-label (get-last-entered-zone)
        zone-file-name (:id (get-zone-id-from-label zone-label))]
    (log/debug "Current zone found: " zone-file-name)
    zone-file-name))

(def parse-current-zone (comp #'parse-zone #'get-current-zone))

(defn parse-position-from-log-line [s]
  (->>
   (re-find #".*Your Location is (.*?), (.*?), (.*)$" s)
   (zipmap [:_ :y :x :z])))

(defn last-or-default
  [def col]
  (or (last col) def))

;; Your Location is 1192.57, -495.48, 3.41
(defn get-current-position-from-disk []
  (let [content (logs/get-lines-of-newest-file)]
    (->> content
         (filter #(re-matches #".*Your Location is.*" %))
         (last-or-default "Your Location is 0, 0, 0")
         parse-position-from-log-line)))

(defn get-current-position []
  (or (get-current-position-from-state)
      (get-current-position-from-disk)))
