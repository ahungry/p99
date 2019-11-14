(ns ahungry.auctions.core
  (:require
   [clj-http.client :as client]
   [cheshire.core :as cheshire]
   [clojure.tools.logging :as log]
   [ahungry.fs.logs :as logs]
   [ahungry.net :as net]))

(defn green? [s]
  (if (re-find #"(?i)(green)" s)
    1
    0))

(defn teal? [s]
  (if (re-find #"(?i)(teal)" s)
    1
    0))

(defn get-auctions-for-player []
  (let [{:keys [name server]} (logs/get-player-info)]
    {:green (green? server)
     :teal (teal? server)
     :dump
     (->> (logs/get-auctions name)
          (clojure.string/join "\r\n"))}))

(defn post-auctions
  "Send the auctions to the server, hooray."
  []
  (client/post
   "https://ahungry.com/aucDump.php"
   ;; {:body (cheshire/encode (get-auctions-for-player))}
   {:form-params (get-auctions-for-player)}
   ))
