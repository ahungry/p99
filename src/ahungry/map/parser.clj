(ns ahungry.map.parser
  (:require
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
  (->> (clojure.string/split file-content #"\r\n")
       (map parse-line)))

(defn parse-zone [zone]
  (-> (zones/load-map zone)
      parse-map-lines))

(defn parse-zone-label-from-log-line [s]
  (last (re-find #".*You have entered (.*)\." s)))

(defn get-last-entered-zone []
  (let [content (logs/get-content-of-newest-file)]
    (->> (clojure.string/split content #"\r\n")
         (filter #(re-matches #".*You have entered (.*)\." %))
         last
         parse-zone-label-from-log-line)))

(defn get-zone-id-from-label [label]
  (let [zl (zones/get-zonelist)]
    (first (filter #(= (:label %) label) zl))))

(defn get-current-zone []
  (let [zone-label (get-last-entered-zone)
        zone-file-name (:id (get-zone-id-from-label zone-label))]
    zone-file-name))

(def parse-current-zone (comp parse-zone get-current-zone))

(defn parse-position-from-log-line [s]
  (->>
   (re-find #".*Your Location is (.*?), (.*?), (.*)$" s)
   (zipmap [:_ :y :x :z])))

;; Your Location is 1192.57, -495.48, 3.41
(defn get-current-position []
  (let [content (logs/get-content-of-newest-file)]
    (->> (clojure.string/split content #"\r\n")
         (filter #(re-matches #".*Your Location is.*" %))
         last
         parse-position-from-log-line)))
