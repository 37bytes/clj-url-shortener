(ns clj-url-shortener.dev-middleware
  (:require
    [ring.middleware.reload :refer [wrap-reload]]
    [selmer.middleware :refer [wrap-error-page]]
    [prone.middleware :refer [wrap-exceptions]]))

(defn wrap-dev [handler]
  (-> handler
      wrap-reload
      ; todo I dont need this either
      ;wrap-error-page
      ;(wrap-exceptions {:app-namespaces ['clj-url-shortener]})
      ))
