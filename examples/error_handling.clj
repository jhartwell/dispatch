;; Error handling — dispatch returns nil for unknown commands and throws
;; an ExceptionInfo for arity mismatches. Both cases should be handled
;; at the call site.

(require '[dispatch.core :refer [dispatch]])

(defn add [a b]
  (+ (parse-long a) (parse-long b)))

(def spec
  {:add add})

;; Unknown command — dispatch returns nil, check before using the result.
(let [result (dispatch ["subtract" "1" "2"] spec)]
  (if (nil? result)
    (println "Unknown command")
    (println result)))

;; Arity mismatch — dispatch throws ExceptionInfo with :provided and :expected.
(try
  (dispatch ["add" "1"] spec)
  (catch clojure.lang.ExceptionInfo e
    (let [{:keys [provided expected]} (ex-data e)]
      (println (str "Expected " expected " argument(s), got " provided)))))

;; Correct usage.
(dispatch ["add" "3" "4"] spec) ;=> 7
