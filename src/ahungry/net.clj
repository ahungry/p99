(ns ahungry.net
  (:require
   [clojure.tools.logging :as log]
   [clj-http.client :as client]
   [cheshire.core :as cheshire]))

(log/debug "start")

(defn as-json
  "Convenience / helper to pull some remote URL as JSON.
  Works off the same interface as a clj-http request."
  [f]
  (fn [url opts]
    (->
     (f url (conj {:as :json
                   :coerce :always}
                  opts))
     :body)))

(def get-json (as-json client/get))
(def post-json (as-json client/post))

(log/debug "fin")
