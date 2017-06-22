;;TODO: change ip and username to variables
(ns toledohue.hue
  (:require [clj-http.client :as http]
            [clojure.data.json :as json]
            [clojure.java.io :as io]
  ))


;;INTERFACE:
;;GET-IP
;;CREATE-USER
;;TURN-OFF
;;TURN-ON
;;GET-SYS-INFO
;;FLASH
;;CUSTOM-CHANGE
;;FLASH-LONG
;;BRI


;;GET-IP
;;----------------------------------------------------------------
(defn get-ip
  "returns the ip of the bridge
  takes no args"
  []
  (-> (http/get "https://www.meethue.com/api/nupnp" {:as :json})
      :body
      first
      :internalipaddress
      )
  )


;;CREATE-USER
;;----------------------------------------------------------------
(defn create-user
  "creates and a username from the bridge and saves it to ~/.toledohue-user
  if a username file already exists, a new user is not created"
  []
  ;;prepare file name for saving username
  (System/getProperty "user.home")                  
  (def home-path (System/getProperty "user.home"))
  (def file-path (str home-path "/.toledohue-user"))
  (if(.exists (io/file file-path))                       ;;if user file already exists quit
    (do (println "You already have a user account. Stop!"))
    (do                                                  ;;else make new user
      

      (def mybody "{\"devicetype\":\"toledohue\"}")     ;;device name to reg
      ;;send request and store response 
      (def response (-> (str "http://" (get-ip) "/api")   ;;create link to sent post request
                      (http/post {:body mybody :as :json :content-type :json}) ;;post request
                      :body                             ;;get the body
                      first))                           ;;remove the parenthesis 
      (println response)                                ;;print the response
      (if (contains? response :success)                 ;;if success save the user name
        (do (def new-username (-> (:success response)
                                  :username))
            (println (str "new-username is: " new-username ". It's being saved"))
            (spit file-path new-username))
        (do (println "try again")))))                   ;;else try again
)


;;GET-USER
;;----------------------------------------------------------------
(defn get-user
  "returns a string with the user name if it exists"
  []
  (def home-path (System/getProperty "user.home"))
  (def file-path (str home-path "/.toledohue-user"))

  (if-not(.exists (io/file file-path))
    (println "You need to create a username first!")
    (do 
        (slurp file-path)))
)


;;TURN-OFF
;;----------------------------------------------------------------
(defn turn-off
  "turns light off
  arg1 is the light number
  arg2 is the username
  returns the response of the command as a map"
  [number user]
  (-> (str "http://" (get-ip) "/api/" user "/lights/" number "/state")
      (http/put {:body "{\"on\":false}" :as :json})
      :body
      first
      )
)


;;TURN-ON
;;----------------------------------------------------------------
(defn turn-on
  "turns on light specified and 
  returns the response of the operation as a map"
  [number user]
  (-> (str "http://" (get-ip) "/api/" user "/lights/" number "/state")
      (http/put {:body "{\"on\":true}" :as :json})
      :body
      first)
)


;;GET-SYS-INFO
;;----------------------------------------------------------------
(defn get-sys-info
  "returns a string with the information fromt he system, use pprint for ez reading
  arg1 username"
  [user]
  (-> (str "http://" (get-ip) "/api/" user)
      (http/get {:as :json})   
      :body)
)


;;FLASH
;;----------------------------------------------------------------
(defn flash
  "flashes light once and return the result
  arg is the light number"
  [number user]
  (-> (str "http://" (get-ip) "/api/" user "/lights/" number "/state")
      (http/put {:body "{\"alert\":\"select\"}" :as :json})
      :body
      first
      )
)


;;CUSTOM-CHANGE
;;----------------------------------------------------------------
(defn custom-change
  "send a state change command to a light in json format
  arg1 is the light number,
  arg2 is the custom state change
  the state change is going to be a string of a map use appropiate escape sequences
  for quotations
  returns the result of the operation as a map"
  [number body user]
  (-> (str "http://" (get-ip) "/api/" user "/lights/" number "/state")
      (http/put {:body body :as :json})
      :body
      first
      )
)


;;HUE
;;----------------------------------------------------------------
(defn hue
  "changes the light of the specified light by changing its hue value
  arg1 light number
  arg2 specified hue value
  arg3 username
  returns a map in json format indicating the result
  some values: red-0, yellow-12750, green-25500, blue-46920, pink-56100, red-65280
  min-0 max-65535)"
  [number hue user]
  (-> (str "http://" (get-ip) "/api/" user "/lights/" number "/state")
      (http/put {:body (str "{\"hue\":" hue "}") :as :json})
      :body
      first)
)


;;FLASH-LONG
;;----------------------------------------------------------------
(defn flash-long
  "flashes light once and return the result
  arg1 is the light number
  arg2 is the username"
  [number user]
  (-> (str "http://" (get-ip) "/api/" user "/lights/" number "/state")
      (http/put {:body "{\"alert\":\"lselect\"}" :as :json})
      :body
      first
      )
)


;;BRI
;;----------------------------------------------------------------
(defn bri
  "changes the brightness to the indicated value between 0 and 255
  arg1 is the number of the light
  arg2 is the specified value of the brighness
  returns a string with the result of the command in json format"
  [number bri user]
  (-> (str "http://" (get-ip) "/api/" user "/lights/"  number "/state")
      (http/put {:body (str "{\"bri\":" bri "}") :as :json})
      :body
      first
      )
)














(defn tester
  []
  (comment
    (slurp "https://www.meethue.com/api/nupnp")
    (spit "usernamehue.txt" "testusername")
    (slurp "usernamehue.txt")
    ;;(slurp "~/user-toledo.txt")
    (.exists (io/file "/home/toledoal/user-toledo.txt"))
    (spit "/home/toledoal/.toledohue-user" "11EiwfuZLdkcNevkME1fcg5IyUWatwoOPk-joGrY")
    (.exists (io/file "/home/toledoal/.toledohue-user"))
    (spit "/home/.toledohue-user" "11EiwfuZLdkcNevkME1fcg5IyUWatwoOPk-joGrY)")
    (.exists (io/file "/home/toledoal/.toledohue-user"))
  )
  
  (System/getProperty "user.home")
  (def home-path (System/getProperty "user.home"))
  (def file-path (str home-path "/.toledohue-user"))

  (println home-path)
  (println file-path)

  (if-not(.exists (io/file file-path))
    "doesn't exist"
    (do (println "exists")
        (println "You already have a user account. Stop!")))

  (:2 {:1 \a :2 {:6 \b :7 \c} :3 \c})
)



    ;;PUTEST
    ;;----------------------------------------------------------------

  (defn putest
    []
    (def mybody "{\"on\":false}")
    (http/put "http://192.168.1.15/api/11EiwfuZLdkcNevkME1fcg5IyUWatwoOPk-joGrY/lights/1/state" {:body mybody}))




;;http://<bridge ip address>/api/<user-name>/lights/1
;;http://192.168.1.15
;;11EiwfuZLdkcNevkME1fcg5IyUWatwoOPk-joGrY
;; :as :json
;;(json/write-str {:devicetype "toledohue"})
;; :content-type :json

(comment

  (-> (str "http://192.168.1.15/api")
      (http/post {:body mybody :content-type :json})
      (clojure.pprint/pprint))


)
