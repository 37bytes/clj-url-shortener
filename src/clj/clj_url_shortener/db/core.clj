(ns clj-url-shortener.db.core
  (:require
    [clojure.tools.logging :as log]
    [datomic.client.api :as d]
    [mount.core :refer [defstate]]
    [clj-url-shortener.config :refer [env]]
    )
  (:import (java.util Date)))

(defn cfg [] {:server-type        :peer-server
              :access-key         (env :access-key)
              :secret             (env :secret)
              :endpoint           (env :endpoint)
              :validate-hostnames false})


(defn get-conn []
  (do (-> (cfg)
          (d/client)
          (d/connect {:db-name (env :db-name)}))))

(defstate conn
          :start (get-conn)
          :stop (log/info "datomic client api does not provide any means to close connection, so let's consider it closed"))

(defn get-current-db []
  (d/db conn))

(def url-schema [
                 {:db/doc         "Url ID"
                  :db/ident       :url/id
                  :db/valueType   :db.type/uuid
                  :db/cardinality :db.cardinality/one
                  :db/unique      :db.unique/identity}
                 {:db/doc         "Url source"
                  :db/ident       :url/source
                  :db/valueType   :db.type/string
                  :db/cardinality :db.cardinality/one}
                 {:db/doc         "Url context"
                  :db/ident       :url/context
                  :db/valueType   :db.type/string
                  :db/cardinality :db.cardinality/one}
                 {:db/doc         "Url code"
                  :db/ident       :url/code
                  :db/valueType   :db.type/long
                  :db/cardinality :db.cardinality/one}
                 {:db/doc         "Url expiration time"
                  :db/ident       :url/expire
                  :db/valueType   :db.type/instant
                  :db/cardinality :db.cardinality/one}
                 ])

;(d/transact conn {:tx-data url-schema})

(defn show-schema
  "Show currently installed schema"
  [conn]
  (let [system-ns #{"db" "db.type" "db.install" "db.part"
                    "db.lang" "fressian" "db.unique" "db.excise"
                    "db.cardinality" "db.fn" "db.sys" "db.bootstrap"
                    "db.alter"}]
    (d/q '[:find ?ident
           :in $ ?system-ns
           :where
           [?e :db/ident ?ident]
           [(namespace ?ident) ?ns]
           [((comp not contains?) ?system-ns ?ns)]]
         (d/db conn) system-ns)))

(defn query-by-attr [curr-db attr val]
  "used to query any entity by one of their attribute values
  e.g.
  (query-by-attr (get-current-db) :url/context \"test-context-2\")
  "
  (d/q '[:find (pull ?e [*])
         :in $ ?attr ?val
         :where [?e ?attr ?val]]
       curr-db attr val))

(defn url-find-by-context
  "Returns a list of url entity maps queried by their context"
  [input-context]
  (d/q '[:find ?id ?source ?context ?code ?expire
         :keys id source context code expire
         :in $ ?input-context
         :where [?_ :url/id ?id]
                [?_ :url/source ?source]
                [?_ :url/expire ?expire]
                [?_ :url/code ?code]
                [?_ :url/context ?context]
                [?_ :url/context ?input-context]]
  (get-current-db) input-context))

(defn url-find-first-expire-before-order-by-code-asc
  "Returns the first item of the url entity list with the :expire lesser than specified
  and sorted by :code ascending.

  TODO a tedious and error-prone variable binding in :find and :where blocks should be replaced somehow.
  We can use (pull ?e [*]) to query all attributes of an entity, but it'll make it impossible to
  use :keys block
  Also bear in mind that only find-rel (i.e. querying relation) is available in datomic.client.api"
  [date]
  (->> (d/q '[:find ?id ?source ?context ?code ?expire
              :keys id source context code expire
              :in $ ?date
              :where
              [?_ :url/id ?id]
              [?_ :url/source ?source]
              [?_ :url/context ?context]
              [?_ :url/code ?code]
              [?_ :url/expire ?expire]
              [(> ?date ?expire)]
              ]
            (get-current-db) date)
       (sort-by :code)
       (first)
       )
  )



(url-find-first-expire-before-order-by-code-asc (Date.))


;(defn find-one-by
;  [db attr val]
;  (d/q '[:find ?e .
;                   :in $ ?attr ?val
;                   :where [?e ?attr ?val]]
;                 db attr val))

;(defn add-user
;  "e.g.
;    (add-user conn {:id \"aaa\"
;                    :screen-name \"AAA\"
;                    :status :user.status/active
;                    :email \"aaa@example.com\" })"
;  [conn {:keys [id screen-name status email]}]
;  @(d/transact conn [{:user/id     id
;                      :user/name   screen-name
;                      :user/status status
;                      :user/email  email}]))



;(defn make-transaction [attr-val-map]
;  ((d/transact conn {:tx-data [{attr-val-map}]})))


;(d/transact conn {:tx-data [{:url/id      (UUID/randomUUID)
;                             :url/source  "test-source-3"
;                             :url/context "test-context-3"
;                             :url/code    1
;                             :url/expire (java.util.Date.)
;                             }]})

;(defn find-one-by
;  "Given db value and an (attr/val), return the user as EntityMap (datomic.query.EntityMap)
;   If there is no result, return nil.
;
;   e.g.
;    (d/touch (find-one-by (d/db conn) :user/email \"user@example.com\"))
;    => show all fields
;    (:user/first-name (find-one-by (d/db conn) :user/email \"user@example.com\"))
;    => show first-name field"
;  [db attr val]
;  (d/entity db
;            ;;find Specifications using ':find ?a .' will return single scalar
;            (d/q '[:find ?e .
;                   :in $ ?attr ?val
;                   :where [?e ?attr ?val]]
;                 db attr val)))
;
;
;(defn find-user [db id]
;  (d/touch (find-one-by db :user/id id)))
