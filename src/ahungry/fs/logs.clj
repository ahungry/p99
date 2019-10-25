;; Code related to finding the proper log files to work with for various features.

(ns ahungry.fs.logs)

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

(defn get-content-of-newest-file []
  (slurp (get-newest-log)))

(defn get-player-info
  "Extract the player name and the server they're on."
  ([] (get-player-info (get-newest-log)))
  ([newest-log]
   (->> newest-log
        (re-matches #".*eqlog_(.*?)_(.*?)\..*")
        rest
        (zipmap [:name :server]))))
