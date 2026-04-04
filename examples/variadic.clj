;; Variadic commands — functions with a required argument followed by
;; optional rest args. Useful for commands that accept a variable number
;; of inputs, like tagging or bulk operations.

(require '[dispatch.core :refer [dispatch]]
         '[clojure.string :as str])

(defn tag [resource & tags]
  (str "Tagged " resource " with: " (str/join ", " tags)))

(defn log [level & messages]
  (str "[" level "] " (str/join " " messages)))

(defn echo [& args]
  (str/join " " args))

(def spec
  {:tag  tag
   :log  log
   :echo echo})

(dispatch ["tag" "server-1" "prod" "us-east" "web"]  spec) ;=> "Tagged server-1 with: prod, us-east, web"
(dispatch ["tag" "server-1"]                          spec) ;=> "Tagged server-1 with: "
(dispatch ["log" "INFO" "starting" "server"]          spec) ;=> "[INFO] starting server"
(dispatch ["log" "ERROR" "connection" "refused"]      spec) ;=> "[ERROR] connection refused"
(dispatch ["echo" "hello" "world"]                    spec) ;=> "hello world"
(dispatch ["echo"]                                    spec) ;=> ""
