(ns clj-url-shortener.routes.upload
  (:require
    [clj-url-shortener.middleware :as middleware]
    [ring.util.response]
    [reitit.ring.coercion :as coercion]
    [reitit.coercion.spec :as spec-coercion]
    [reitit.swagger :as swagger]
    [reitit.swagger-ui :as swagger-ui]
    [reitit.ring.middleware.muuntaja :as muuntaja]
    [reitit.ring.middleware.exception :as exception]
    [reitit.ring.middleware.multipart :as multipart]
    [reitit.ring.middleware.parameters :as parameters]
    [clj-url-shortener.middleware.formats :as formats]
    [clj-url-shortener.controller.upload-controller :as upload-cont]
    [ring.util.http-response :as response])
  (:import (clojure.lang ExceptionInfo)))


(defn upload-routes []
  ["/api"
   {
    :middleware [;; query-params & form-params
                 ;parameters/parameters-middleware
                 ;; content-negotiation
                 muuntaja/format-negotiate-middleware
                 ;; encoding response body
                 muuntaja/format-response-middleware
                 ;; exception handling
                 ;exception/exception-middleware
                 ;; decoding request body
                 muuntaja/format-request-middleware
                 ;; coercing response bodys
                 coercion/coerce-response-middleware
                 ;; coercing request parameters
                 coercion/coerce-request-middleware
                 ;; multipart params
                 multipart/multipart-middleware
                 ]
    :muuntaja   formats/instance
    :coercion   spec-coercion/coercion
    :swagger    {:id ::api}}
   ["" {:no-doc true}
    ["/swagger.json" {:get (swagger/create-swagger-handler)}]
    ["/swagger-ui*" {:get (swagger-ui/create-swagger-ui-handler
                            {:url "/api/swagger.json"})}]]
   ["/short" {:post {:parameters
                     {:body
                      {:url       string?
                       :permanent boolean?
                       ; todo it should be long!
                       :ttl       int?
                       :secured   boolean?}}
                     :responses
                     ; todo we need to send custom status
                     {2000 {:body map?}
                      ; todo errors or body?
                      4004 {:errors map?}}
                     :handler upload-cont/short-url
                     }}]
   ])

