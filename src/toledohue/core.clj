(ns toledohue.core
  (:require [clj-http.client :as client]
            [clojure.data.json :as json]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [clj-http.client :as http]
            [clojure.data.json :as json]
            [clojure.java.io :as io]
            [ring.util.response :as response]
            [ring.util.http-response :as response2]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults site-defaults]]
            [ring.middleware.reload :refer [wrap-reload]]
            [clojure.core.async
             :as a
             :refer [>! <! >!! <!! go chan buffer close! thread
                     alts! alts!! timeout poll! dropping-buffer sliding-buffer]])
  (:use [clojure.pprint])
  (:use [ring.adapter.jetty])
  (:use [toledohue.hue :as myhue])
  (:gen-class))



(def user (myhue/get-user))





(defn handler [request-map]
  {:status 200
   :headers {"Contnet-Type" "text/html"}
   :body (str "<html><body> your IP is: "
              (:remote-addr request-map) " HELLO HELLO "
              "</body></html>")})

(defn handler-with-response [request]
  (response/response
   (str "<html> RESPONSE/RESPONSE </html>")))


(defn handler-with-response2 [request]
  (response2/ok
   (str "<html> RESPONSE2/OK <html>")))


;;-MAIN
;;---------------------------------------------------------------------------
(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (run-jetty (wrap-reload #'handler) {:port 5000 :join? false}))

(defn run-handler-with-response
  []
  (run-jetty (wrap-reload #'handler-with-response) {:port 5001 :join? false}))

(defn run-handler-with-response2
  []
  (run-jetty (wrap-reload #'handler-with-response2) {:port 5002 :join? false}))
