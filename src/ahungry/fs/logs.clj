;; Code related to finding the proper log files to work with for various features.

(ns ahungry.fs.logs
  (:require
   [ahungry.events :as e :refer [fire listen]]))

(defn get-resource-dir
  "Return the current resource (res/) directory."
  []
  "resources/")

(defn get-logs-in-dir
  "Get the logs in directory that appear to be an eq log."
  [dir]
  (let [directory (clojure.java.io/file dir)
        files (file-seq directory)]
    (filter #(re-matches #".*eqlog_.*\.txt$" (.toString %)) files)))

(defn get-newest-log-in-dir
  "Sort File results by last modification date."
  [dir]
  (->> (get-logs-in-dir dir)
       (sort (fn [a b] (> (.lastModified a) (.lastModified b))))
       first))

(defn get-newest-log
  "Pull in the name of the newest log file the player last signed into."
  []
  (str (get-newest-log-in-dir (str (get-resource-dir) "/logs/"))))


(def *log (atom {:fh nil
                 :lines []
                 :pos 0
                 }))

(defn raf []
  (let [filename (get-newest-log)
        fh (java.io.RandomAccessFile. filename "r")]
    fh))

(defn log-open
  "Open up the log file, with the cursor set towards the end of file.

  In general, this will get roughly 100 or so lines from the file."
  []
  (when (:fh @*log)
    (.close (:fh @*log)))
  (let [fh (raf)
        len (.length fh)
        byte-offset (max 0 (- len 10000))]
    (.seek fh byte-offset)
    (reset! *log {
                  :fh fh
                  :lines []
                  :pos byte-offset})))

(defn log-at []
  (-> (:fh @*log)
      (.getFilePointer)))

(defn log-len []
  (-> (:fh @*log)
      (.length)))

(defn log-line []
  (let [line (-> (:fh @*log)
                 (.readLine))]
    (fire :read-line line)
    (swap! *log update-in [:lines] conj line)))

(defn log-print []
  (let [m @*log]
    {:line-last (last (:lines m))
     :line-count (count (:lines m))}))

(defn log-load []
  (when-not (:fh @*log)
    (log-open))
  (while (< (log-at)
            (log-len))
    (log-line))
  (log-print))

(defn get-content-of-newest-file []
  (slurp (get-newest-log)))

(defn s->lines [s] (clojure.string/split s #"\r\n"))

;; Slow impl
;; (def get-lines-of-newest-file (comp s->lines get-content-of-newest-file))

(defn get-lines-of-newest-file []
  (log-load)
  (:lines @*log))

(defn get-player-info
  "Extract the player name and the server they're on."
  ([] (get-player-info (get-newest-log)))
  ([newest-log]
   (->> newest-log
        (re-matches #".*eqlog_(.*?)_(.*?)\..*")
        rest
        (zipmap [:name :server]))))

(defn you->player [player-name s]
  (clojure.string/replace s #"You auction" (str player-name " auctions")))

(defn get-auctions
  "Pull the known auction lines and send them out perhaps."
  ([player-name] (get-auctions player-name (get-lines-of-newest-file)))
  ([player-name lines]
   (->> lines
        (filter #(re-matches #".*auction.*" %))
        (map (partial you->player player-name)))))

(defn get-kills
  "Pull out the last kills."
  ([player-name] (get-kills player-name (get-lines-of-newest-file)))
  ([player-name lines]
   (->> lines
        (filter #(re-matches #".*You have slain (.*?)!" %)))))

(def get-most-recent-kill (comp last get-kills))
