;;TODO: create-room, add schedule support
;;Most recent update: added cycles.
(ns toledohue.hue
  (:require [clj-http.client :as http]
            [clojure.data.json :as json]
            [clojure.java.io :as io]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ring.middleware.reload :refer [wrap-reload]]
            [clojure.core.async
             :as a
             :refer [>! <! >!! <!! go chan buffer close! thread
                     alts! alts!! timeout poll! dropping-buffer sliding-buffer]])
  (:use [clojure.pprint])
  (:use [ring.adapter.jetty]))


(ns toledohue.hue
  (:require [clj-http.client :as http]
            [clojure.data.json :as json]
            [clojure.java.io :as io]))



;;INTERFACE:
;;GET-IP
;;CREATE-USER
;;GET-USER
;;TURN-OFF
;;TURN-ON
;;GET-SYS-INFO
;;GET-LIGHT-INFO
;;GET-GROUP-INFO
;;FLASH
;;CUSTOM-LIGHT-CHANGE
;;CUSTOM-GROUP-CHANGE
;;FLASH-LONG
;;BRI
;;CREATE-GROUP
;;BRI-GROUP
;;TURN-GROUP-ON
;;TURN-GROUP-OFF
;;FLASH-GROUP
;;FLASH-LONG-GROUP
;;HUE
;;HUE-GROUP
;;CYCLE-30-SECS
;;CYCLE-1-MIN
;;CYCLE-1-HR
;;CYCLE-1-DAY
;;CYCLE-1-MONTH
;;CYCLE-1-YEAR
;;CYCLE-1-WEEK
;;CYCLE-GROUP-30-SECS
;;CYCLE-GROUP-1-MIN
;;CYCLE-GROUP-1-HR
;;CYCLE-GROUP-1-DAY
;;CYCLE-GROUP-1-MONTH
;;CYCLE-GROUP-1-YEAR
;;CYCLE-GROUP-1-WEEK

;;TURN-ON-GROUP
;;TURN-OFF-GROUP
;;FLASH-GROUP
;;FLASH-LONG-GROUP




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
        (do (println "try again"))))))                 ;;else try again



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
      (clojure.string/trim-newline (slurp file-path)))))



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
      first))



;;TURN-ON
;;----------------------------------------------------------------
(defn turn-on
  "turns on light specified and 
  returns the response of the operation as a map"
  [number user]
  (-> (str "http://" (get-ip) "/api/" user "/lights/" number "/state")
      (http/put {:body "{\"on\":true}" :as :json})
      :body
      first))



;;GET-SYS-INFO
;;----------------------------------------------------------------
(defn get-sys-info
  "returns a string with the information from he system, use pprint for ez reading
  arg1 username"
  [user]
  (-> (str "http://" (get-ip) "/api/" user)
      (http/get {:as :json})   
      :body))



;;GET-LIGHT-INFO
;;----------------------------------------------------------------
(defn get-light-info
  "returns a string with the information from he system, use pprint for ez reading
  arg1 username"
  [user]
  (-> (str "http://" (get-ip) "/api/" user "/lights")
      (http/get {:as :json})   
      :body))



;;GET-GROUP-INFO
;;----------------------------------------------------------------
(defn get-group-info
  "returns a string with the information from he system, use pprint for ez reading
  arg1 username"
  [user]
  (-> (str "http://" (get-ip) "/api/" user "/groups")
      (http/get {:as :json})   
      :body))



;;FLASH
;;----------------------------------------------------------------
(defn flash
  "flashes light once and return the result
  arg is the light number"
  [number user]
  (-> (str "http://" (get-ip) "/api/" user "/lights/" number "/state")
      (http/put {:body "{\"alert\":\"select\"}" :as :json})
      :body
      first))



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



;;FLASH-GROUP
;;----------------------------------------------------------------
(defn flash-group
  "flashes light once and return the result
  arg is the light number"
  [number user]
  (-> (str "http://" (get-ip) "/api/" user "/groups/" number "/action")
      (http/put {:body "{\"alert\":\"select\"}" :as :json})
      :body
      first))



;;FLASH-LONG-GROUP
;;----------------------------------------------------------------
(defn flash-long-group
  "flashes light once and return the result
  arg1 is the light number
  arg2 is the username"
  [number user]
  (-> (str "http://" (get-ip) "/api/" user "/groups/" number "/action")
      (http/put {:body "{\"alert\":\"lselect\"}" :as :json})
      :body
      first))



;;CUSTOM-LIGHT-CHANGE
;;----------------------------------------------------------------
(defn custom-light-change
  "send a state change command to a light in json format
  arg1 is the light number,
  arg2 is the custom state change e.g. {\"on\":true}
  arg3 is the username
  returns the result of the operation as a map"
  [number body user]
  (-> (str "http://" (get-ip) "/api/" user "/lights/" number "/state")
      (http/put {:body body :as :json})
      :body
      first))



;;CUSTOM-GROUP-CHANGE
;;----------------------------------------------------------------
(defn custom-group-change
  "send a state change command to a light in json format
  arg1 is the group id,
  arg2 is the custom state change e.g. {\"on\":true}
  arg3 is the username
  returns the result of the operation as a map"
  [number body user]
  (-> (str "http://" (get-ip) "/api/" user "/groups/" number "/action")
      (http/put {:body body :as :json})
      :body
      first))



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
      first))



;;CREATE-GROUP
;;----------------------------------------------------------------
(defn create-group
  "creates a new group on the bridge of type LightGroup
  arg1 is the name of the group
  arg2 is the username
  &lights is the list of the lights. e.g \"1\", \"2\", \"3\" without escape"
  [name user & lights]
  (def mybody (json/write-str {"lights" (into [] lights) "name" name "type" "LightGroup"}))
  (println mybody)
  (-> (str "http://" (get-ip) "/api/" user "/groups")
        (http/post {:body mybody :as :json})
        :body
        first))



;;TURN-GROUP-ON
;;----------------------------------------------------------------
(defn turn-group-on
  "turns on the whole group
  arg1 is the group id,
  arg2 is the username
  returns the result of the operation as a map"
  [number user]
  (-> (str "http://" (get-ip) "/api/" user "/groups/" number "/action")
      (http/put {:body "{\"on\":true}" :as :json})
      :body
      first))



;;TURN-GROUP-OFF
;;----------------------------------------------------------------
(defn turn-group-off
  "turns off the whole group
  arg1 is the group id,
  arg2 is the username
  returns the result of the operation as a map"
  [number user]
  (-> (str "http://" (get-ip) "/api/" user "/groups/" number "/action")
      (http/put {:body "{\"on\":false}" :as :json})
      :body
      first))



;;HUE-GROUP
;;----------------------------------------------------------------
(defn hue-group
  "changes the hue of the whole group
  arg1 is the group id,
  arg2 is the desired hue
  arg2 is the username
  returns the result of the operation as a map
  (hue runs from 0 to 65535)"
  [number hue user]
  (-> (str "http://" (get-ip) "/api/" user "/groups/" number "/action")
      (http/put {:body (str "{\"hue\":" hue "}") :as :json})
      :body
      first))



;;BRI-GROUP
;;----------------------------------------------------------------
(defn bri-group
  "changes the bri of the whole group
  arg1 is the group id,
  arg2 is the desired bri
  arg2 is the username
  returns the result of the operation as a map"
  [number bri user]
  (-> (str "http://" (get-ip) "/api/" user "/groups/" number "/action")
      (http/put {:body (str "{\"bri\":" bri "}") :as :json})
      :body
      first))




;;CHANNEL FOR SIGNALING THREADS
(def killchan (chan 1))







(defn kill-cycle
  "we send a signal to the killchan channel to end any running light cycle"
  []
  (poll! killchan)  ;;poll it first to avoid blocking forever
  (>!! killchan :kill))



;;CYCLE-30-SECS
;;----------------------------------------------------------------
(defn cycle-30-secs
  "cycles hue for indicated light from 0 to max and back to 0 (red) 
  over 30 secs (hue runs from 0 to 65535) 0 = red, 65535 = red
  arg1 is the light number
  arg2 is the username" 
  [light user]
  (thread
    (while (nil? (poll! killchan))    ;;While we don't send the kill signal
      ;;Get the hue value for given second
      (def hueval (-> (.format (java.text.SimpleDateFormat. "s") (new java.util.Date))
                read-string
                (mod 30)
                (* 2184)))
      ;;Change the light color to that hue value
      (hue light hueval user)
      ;;Wait for second to change
      (Thread/sleep 1000))))



;;CYCLE-1-MIN
;;----------------------------------------------------------------
(defn cycle-1-min
  "cycles hue for indicated light from 0 to max and back to 0 (red) 
  over 60 secs (hue runs from 0 to 65535) 0 = red, 65535 = red
  arg1 is the light number
  arg2 is the username" 
  [light user]
  (thread
    (while (nil? (poll! killchan))    ;;While we don't send the kill signal
      ;;Get the hue value for given second
      (def hueval (-> (.format (java.text.SimpleDateFormat. "s") (new java.util.Date))
                read-string
                (* 1092)))
      ;;Change the light color to that hue value
      (hue light hueval user)
      ;;Wait for second to change
      (Thread/sleep 1000))))








;;CYCLE-1-HR
;;----------------------------------------------------------------
(defn cycle-1-hr
  "cycles hue for indicated light from 0 to max and back to 0 (red) 
  over 1 hr (hue runs from 0 to 65535) 0 = red, 65535 = red
  arg1 is the light number
  arg2 is the username" 
  [light user]
  (thread
    (while (nil? (poll! killchan))    ;;While we don't send the kill signal
      ;;Get the hue value for given second
      (def hueval (-> (.format (java.text.SimpleDateFormat. "m") (new java.util.Date))
                read-string
                (* 1092)))
      ;;Change the light color to that hue value
      (hue light hueval user)
      ;;Wait for min to change
      (Thread/sleep 60000))))





;;CYCLE-1-DAY
;;----------------------------------------------------------------
(defn cycle-1-day
  "cycles hue for indicated light from 0 to max and back to 0 (red) 
  over 1 day (hue runs from 0 to 65535) 0 = red, 65535 = red
  arg1 is the light number
  arg2 is the username" 
  [light user]
  (thread
    (while (nil? (poll! killchan))    ;;While we don't send the kill signal
      ;;Get the hue value for given second
      (def hueval (-> (.format (java.text.SimpleDateFormat. "s") (new java.util.Date))
                read-string
                (* 2730)))
      ;;Change the light color to that hue value
      (hue light hueval user)
      ;;Wait for hr to change
      (Thread/sleep 3600000))))







;;CYCLE-1-MONTH
;;----------------------------------------------------------------
(defn cycle-1-month
  "cycles hue for indicated light from 0 to max and back to 0 (red) 
  over 1 month (hue runs from 0 to 65535) 0 = red, 65535 = red
  arg1 is the light number
  arg2 is the username" 
  [light user]
  (thread
    (while (nil? (poll! killchan))    ;;While we don't send the kill signal
      ;;Get the hue value for given second
      (def hueval (-> (.format (java.text.SimpleDateFormat. "s") (new java.util.Date))
                read-string
                (* 2114)))
      ;;Change the light color to that hue value
      (hue light hueval user)
      ;;Wait for second to change
      (Thread/sleep 86400000))))




;;CYCLE-1-YEAR
;;----------------------------------------------------------------
(defn cycle-1-year
  "cycles hue for indicated light from 0 to max and back to 0 (red) 
  over 1 year (hue runs from 0 to 65535) 0 = red, 65535 = red
  arg1 is the light number
  arg2 is the username" 
  [light user]
  (thread
    (while (nil? (poll! killchan))    ;;While we don't send the kill signal
      ;;Get the hue value for given second
      (def hueval (-> (.format (java.text.SimpleDateFormat. "s") (new java.util.Date))
                read-string
                (* 5461)))
      ;;Change the light color to that hue value
      (hue light hueval user)
      ;;Wait for second to change
      (Thread/sleep 2592000000))))



;;CYCLE-1-WEEK
;;----------------------------------------------------------------
(defn cycle-1-week
  "cycles hue for indicated light from 0 to max and back to 0 (red) 
  over 1 week (hue runs from 0 to 65535) 0 = red, 65535 = red
  arg1 is the light number
  arg2 is the username" 
  [light user]
  (thread
    (while (nil? (poll! killchan))    ;;While we don't send the kill signal
      ;;Get the hue value for given second
      (def hueval (-> (.format (java.text.SimpleDateFormat. "s") (new java.util.Date))
                read-string
                (* 9362)))
      ;;Change the light color to that hue value
      (hue light hueval user)
      ;;Wait for second to change
      (Thread/sleep 86400000))))






;;CYCLE-GROUP-30-SECS
;;----------------------------------------------------------------
(defn cycle-group-30-secs
  "cycles hue for indicated group from 0 to max and back to 0 (red) 
  over 30 secs (hue runs from 0 to 65535) 0 = red, 65535 = red
  arg1 is the group number
  arg2 is the username" 
  [group user]
  (thread
    (while (nil? (poll! killchan))    ;;While we don't send the kill signal
      ;;Get the hue value for given second
      (def hueval (-> (.format (java.text.SimpleDateFormat. "s") (new java.util.Date))
                read-string
                (mod 30)
                (* 2184)))
      ;;Change the light color to that hue value
      (hue-group group hueval user)
      ;;Wait for second to change
      (Thread/sleep 1000))))



;;CYCLE-GROUP-1-MIN
;;----------------------------------------------------------------
(defn cycle-group-1-min
  "cycles hue for indicated light from 0 to max and back to 0 (red) 
  over 60 secs (hue runs from 0 to 65535) 0 = red, 65535 = red
  arg1 is the group number
  arg2 is the username" 
  [group user]
  (thread
    (while (nil? (poll! killchan))    ;;While we don't send the kill signal
      ;;Get the hue value for given second
      (def hueval (-> (.format (java.text.SimpleDateFormat. "s") (new java.util.Date))
                read-string
                (* 1092)))
      ;;Change the light color to that hue value
      (hue-group group hueval user)
      ;;Wait for second to change
      (Thread/sleep 1000))))








;;CYCLE-GROUP-1-HR
;;----------------------------------------------------------------
(defn cycle-group-1-hr
  "cycles hue for indicated group from 0 to max and back to 0 (red) 
  over 1 hr (hue runs from 0 to 65535) 0 = red, 65535 = red
  arg1 is the group number
  arg2 is the username" 
  [group user]
  (thread
    (while (nil? (poll! killchan))    ;;While we don't send the kill signal
      ;;Get the hue value for given second
      (def hueval (-> (.format (java.text.SimpleDateFormat. "m") (new java.util.Date))
                read-string
                (* 1092)))
      ;;Change the group color to that hue value
      (hue-group group hueval user)
      ;;Wait for min to change
      (Thread/sleep 60000))))





;;CYCLE-GROUP-1-DAY
;;----------------------------------------------------------------
(defn cycle-group-1-day
  "cycles hue for indicated light from 0 to max and back to 0 (red) 
  over 1 day (hue runs from 0 to 65535) 0 = red, 65535 = red
  arg1 is the group number
  arg2 is the username" 
  [group user]
  (thread
    (while (nil? (poll! killchan))    ;;While we don't send the kill signal
      ;;Get the hue value for given second
      (def hueval (-> (.format (java.text.SimpleDateFormat. "s") (new java.util.Date))
                read-string
                (* 2730)))
      ;;Change the group color to that hue value
      (hue-group group hueval user)
      ;;Wait for hr to change
      (Thread/sleep 3600000))))







;;CYCLE-GROUP-1-MONTH
;;----------------------------------------------------------------
(defn cycle-group-1-month
  "cycles hue for indicated group from 0 to max and back to 0 (red) 
  over 1 month (hue runs from 0 to 65535) 0 = red, 65535 = red
  arg1 is the group number
  arg2 is the username" 
  [group user]
  (thread
    (while (nil? (poll! killchan))    ;;While we don't send the kill signal
      ;;Get the hue value for given second
      (def hueval (-> (.format (java.text.SimpleDateFormat. "s") (new java.util.Date))
                read-string
                (* 2114)))
      ;;Change the group color to that hue value
      (hue-group group hueval user)
      ;;Wait for second to change
      (Thread/sleep 86400000))))




;;CYCLE-GROUP-1-YEAR
;;----------------------------------------------------------------
(defn cycle-group-1-year
  "cycles hue for indicated group from 0 to max and back to 0 (red) 
  over 1 year (hue runs from 0 to 65535) 0 = red, 65535 = red
  arg1 is the group number
  arg2 is the username" 
  [group user]
  (thread
    (while (nil? (poll! killchan))    ;;While we don't send the kill signal
      ;;Get the hue value for given second
      (def hueval (-> (.format (java.text.SimpleDateFormat. "s") (new java.util.Date))
                read-string
                (* 5461)))
      ;;Change the group color to that hue value
      (hue-group group hueval user)
      ;;Wait for second to change
      (Thread/sleep 2592000000))))



;;CYCLE-GROUP-1-WEEK
;;----------------------------------------------------------------
(defn cycle-group-1-week
  "cycles hue for indicated group from 0 to max and back to 0 (red) 
  over 1 week (hue runs from 0 to 65535) 0 = red, 65535 = red
  arg1 is the group number
  arg2 is the username" 
  [group user]
  (thread
    (while (nil? (poll! killchan))    ;;While we don't send the kill signal
      ;;Get the hue value for given second
      (def hueval (-> (.format (java.text.SimpleDateFormat. "s") (new java.util.Date))
                read-string
                (* 9362)))
      ;;Change the group color to that hue value
      (hue-group group hueval user)
      ;;Wait for second to change
      (Thread/sleep 86400000))))


















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
