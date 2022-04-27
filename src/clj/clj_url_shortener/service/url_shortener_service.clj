(ns clj-url-shortener.service.url-shortener-service
  (:require [clj-url-shortener.db.core :as db]
            [clojure.string :as string]
            [clojure.tools.logging :as log]
            [clj-url-shortener.config :refer [env]]
            )
  (:import (org.apache.commons.lang3 RandomStringUtils)
           (java.util Date)))

;; these should be functions instead of variables to escape mount.core.DerefableState exception
;; https://github.com/yogthos/clojure-error-message-catalog/blob/master/lib/mount/derefablestate-cannot-be-cast-to-ifn.md
(defn default-domain [] (env :default-domain))
(defn default-scheme [] (env :default-scheme))
(defn default-ttl [] (env :default-ttl))
(def reserved-contexts ["status" "http-status"])

(defn get-domain-schema-map [request]
  (let [server-name (if (string/blank? (:server-name request)) default-domain (:server-name request))
        req-scheme-value (name (:scheme request))
        scheme (if (string/blank? req-scheme-value) default-scheme req-scheme-value)]
    {:domain server-name
     :scheme scheme}
    )
  )

(defn generate-unsecured-url-map [domain]
  (let [next-code 1                  ;; todo generate next-code
        next-context "context"]      ;; todo generate next-context
    {:domain domain
     :context next-context
     :code next-code}))

; todo rewrite using recursion?
(defn generate-unique-context []
  (let [context (atom (RandomStringUtils/random 10 true true))]
    (while (or (not-empty (db/url-find-by-context @context))
               (.contains reserved-contexts @context))
      (reset! context (RandomStringUtils/random 10 true true))
      )
    @context
    ))

(defn get-url-map [domain-map secured]
  (case secured
    true {:domain  (:domain domain-map)
          :context (generate-unique-context)
          :code    nil}
    false (let [url-map (db/url-find-first-expire-before-order-by-code-asc (Date.))]
            (if (empty? url-map) (generate-unsecured-url-map (:domain domain-map)) url-map)))
  )

(defn check-history-expired [url-map request]
  (if (and (empty? url-map) )))

(defn generate-short-url [request-params request]
  (do
    (let [domain-map (get-domain-schema-map request)
          url-map (get-url-map domain-map (:secured request-params))
          expired-history (check-history-expired url-map request)]
      (log/info "/generate-short-url request-params =" domain-map)
      )))