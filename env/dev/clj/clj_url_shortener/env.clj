(ns clj-url-shortener.env
  (:require
    [selmer.parser :as parser]
    [clojure.tools.logging :as log]
    [clj-url-shortener.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[clj-url-shortener started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[clj-url-shortener has shut down successfully]=-"))
   :middleware wrap-dev})
