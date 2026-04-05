;; Error handling — dispatch returns :dispatch/unmapped for unknown commands
;; and :dispatch/empty-args when called with no arguments. Arity mismatches
;; throw ExceptionInfo with :provided and :expected in ex-data.

(require '[dispatch.core :refer [dispatch]])

(defn add [a b]
  (+ (parse-long a) (parse-long b)))

(def spec
  {:add #'add})

;; Unknown command — dispatch returns :dispatch/unmapped.
(let [result (dispatch ["subtract" "1" "2"] spec)]
  (when (= :dispatch/unmapped result)
    (println "Unknown command")))

;; Empty args — dispatch returns :dispatch/empty-args.
(let [result (dispatch [] spec)]
  (when (= :dispatch/empty-args result)
    (println "No command provided")))

;; Arity mismatch — dispatch throws ExceptionInfo with :provided and :expected.
(try
  (dispatch ["add" "1"] spec)
  (catch clojure.lang.ExceptionInfo e
    (let [{:keys [provided expected]} (ex-data e)]
      (println (str "Expected " expected " argument(s), got " provided)))))

;; Correct usage.
(dispatch ["add" "3" "4"] spec) ;=> 7
