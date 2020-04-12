(ns ahungry.auctions.core
  (:require
   [clj-http.client :as client]
   [cheshire.core :as cheshire]
   [clojure.tools.logging :as log]
   [java-time :as t]
   [ahungry.fs.logs :as logs]
   [ahungry.events :as e :refer [fire listen]]
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

(defn get-upload-message []
  (let [{:keys [name server]} (logs/get-player-info)]
    (str "Uploaded auctions to server at: "
         (t/local-date-time)
         " for user " name " @ " server)))

(defn post-auctions
  "Send the auctions to the server, hooray."
  []
  (fire :ev-auctions (get-upload-message))
  (client/post
   "https://ahungry.com/aucDump.php"
   ;; {:body (cheshire/encode (get-auctions-for-player))}
   {:form-params (get-auctions-for-player)}
   ))
