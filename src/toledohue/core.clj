(ns toledohue.core
  (:require [clj-http.client :as client]
            [clojure.data.json :as json]
            [toledohue.hue :as myhue]
  )
  (:gen-class))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  
  
  (comment
    (myhue/turn-off-light 1)
    (myhue/turn-off-light 2)
    (myhue/turn-off-light 3)
    (Thread/sleep 5000)
    (myhue/turn-on-light 1)
    (myhue/turn-on-light 2)
    (myhue/turn-on-light 3))


  ;;blinking 3 lights twice
  (def user (myhue/get-user))
  (myhue/turn-off-light 1 user)
 ;; (myhue/flash-light-long 2 user)
 ;; (myhue/flash-light-long 3 user)
;;  (Thread/sleep 1000)
 ;; (myhue/flash-light 1 user)
;;  (myhue/flash-light 2 user)
;;  (myhue/flash-light 3 user)
  )






  (comment

  

)
