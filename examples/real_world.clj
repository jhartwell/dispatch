;; Real-world example — a simple file management CLI built with dispatch.
;; Demonstrates combining nested specs, variadic args, and error handling
;; into a cohesive command-line interface.

(require '[dispatch.core :refer [dispatch]]
         '[clojure.string :as str])

;; --- Handlers ---

(defn list-files [dir]
  (str "Listing files in " dir))

(defn copy-file [src dst]
  (str "Copying " src " to " dst))

(defn move-file [src dst]
  (str "Moving " src " to " dst))

(defn delete-file [path]
  (str "Deleting " path))

(defn search-files [dir pattern & flags]
  (let [flag-str (if (seq flags) (str " [" (str/join ", " flags) "]") "")]
    (str "Searching " dir " for \"" pattern "\"" flag-str)))

(defn show-info [path]
  (str "Info for " path))

(defn set-permission [path permission]
  (str "Setting " permission " on " path))

;; --- Spec ---

(def spec
  {:file {:list    #'list-files
          :copy    #'copy-file
          :move    #'move-file
          :delete  #'delete-file
          :search  #'search-files
          :info    #'show-info}
   :perm {:set     #'set-permission}})

;; --- Usage ---

(dispatch ["file" "list" "/home/user"]                          spec)
;=> "Listing files in /home/user"

(dispatch ["file" "copy" "a.txt" "b.txt"]                      spec)
;=> "Copying a.txt to b.txt"

(dispatch ["file" "move" "old.txt" "new.txt"]                   spec)
;=> "Moving old.txt to new.txt"

(dispatch ["file" "delete" "/tmp/scratch.txt"]                  spec)
;=> "Deleting /tmp/scratch.txt"

(dispatch ["file" "search" "/src" "defn" "--recursive"]         spec)
;=> "Searching /src for \"defn\" [--recursive]"

(dispatch ["file" "info" "deps.edn"]                            spec)
;=> "Info for deps.edn"

(dispatch ["perm" "set" "build.clj" "read-only"]                spec)
;=> "Setting read-only on build.clj"

;; --- Error handling at the entry point ---

(defn run [args]
  (try
    (let [result (dispatch args spec)]
      (case result
        :dispatch/unmapped  (println "Unknown command:" (str/join " " args))
        :dispatch/empty-args (println "No command provided")
        (println result)))
    (catch clojure.lang.ExceptionInfo e
      (let [{:keys [provided expected]} (ex-data e)]
        (println (str "Wrong number of arguments — expected: " expected ", got: " provided))))))

(run ["file" "copy" "only-one-arg"]) ;=> Wrong number of arguments — expected: #{2}, got: 1
(run ["unknown" "command"])          ;=> Unknown command: unknown command
(run [])                             ;=> No command provided
