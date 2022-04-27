(ns clj-url-shortener.handler
  (:require
    [clj-url-shortener.middleware :as middleware]
    [clj-url-shortener.routes.upload :refer [upload-routes]]
    [reitit.ring :as ring]
    [ring.middleware.content-type :refer [wrap-content-type]]
    [ring.middleware.webjars :refer [wrap-webjars]]
    [clj-url-shortener.env :refer [defaults]]
    [mount.core :as mount]))

(mount/defstate init-app
  :start ((or (:init defaults) (fn [])))
  :stop  ((or (:stop defaults) (fn []))))

(mount/defstate app-routes
  :start
  (ring/ring-handler
    (ring/router
      [(upload-routes)])
    (ring/routes
      (ring/create-resource-handler
        {:path "/"})
      (wrap-content-type
        (wrap-webjars (constantly nil)))
      ; todo get rid of it!
      ;(ring/create-default-handler
      ;  {:not-found
      ;   (constantly (error-page {:status 404, :title "404 - Page not found"}))
      ;   :method-not-allowed
      ;   (constantly (error-page {:status 405, :title "405 - Not allowed"}))
      ;   :not-acceptable
      ;   (constantly (error-page {:status 406, :title "406 - Not acceptable"}))})
      )))

(defn app []
  (middleware/wrap-base #'app-routes))
