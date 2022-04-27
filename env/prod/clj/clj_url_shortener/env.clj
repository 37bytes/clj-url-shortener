(ns clj-url-shortener.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[clj-url-shortener started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[clj-url-shortener has shut down successfully]=-"))
   :middleware identity})
