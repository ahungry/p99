;; Code related to finding the proper zone files to work with for various features.

(ns ahungry.fs.zones)

(defn get-resource-dir
  "Return the current resource (res/) directory."
  []
  "resources/")

(defn get-zonelist
  "Get the zones in format such as:
  [{:label \"The Forgotten Halls\" :id \"fhalls\"}]"
  []
  (->> (clojure.string/split (slurp (str (get-resource-dir) "/zonelist.txt")) #"\n")
       (map #(clojure.string/split % #";"))
       (map #(zipmap [:label :id] %))))

(defn load-map [zone]
  (slurp (str (get-resource-dir) "/zones/" zone ".txt")))
