(ns clj-url-shortener.controller.upload-controller
  (:require [ring.util.http-response :as response]
            [clojure.string :as string]
            [clj-url-shortener.service.url-shortener-service :as url-short-service])
  (:import (clojure.lang ExceptionInfo)))

(def url-regex (re-pattern "\\w+://.+"))

(defn validate-url [url]
  (cond
    ;; todo for now I copy/paste ex description. Can I reuse it??
    (string/blank? url) (throw (ex-info
                                 "Url cannot be empty"
                                 {:code        4004
                                  :description "Incorrect parameters of request"}))
    (nil? (re-matches url-regex url)) (throw (ex-info
                                               "Incorrect format of URL"
                                               {:code        4004
                                                :description "Incorrect parameters of request"})
                                             )
    )
  )

(defn short-url [request]
  (let [{:keys [body-params]} request]
    (try
      (do
        (validate-url (:url body-params))
        (response/ok (url-short-service/generate-short-url body-params request))
        )
      (catch ExceptionInfo e
        (let [{code        :code
               description :description} (ex-data e)
              message (ex-message e)]
          (response/ok {code         code
                        :description description
                        :message     message}))))
    )
  )
