;; Basic usage — mapping top-level string arguments to functions.
;; Each key in the spec corresponds to a CLI-style command word.

(require '[dispatch.core :refer [dispatch]])

(defn greet [name]
  (str "Hello, " name "!"))

(defn farewell [name]
  (str "Goodbye, " name "!"))

(def spec
  {:greet    greet
   :farewell farewell})

(dispatch ["greet" "Alice"]    spec) ;=> "Hello, Alice!"
(dispatch ["farewell" "Alice"] spec) ;=> "Goodbye, Alice!"
(dispatch ["unknown" "Alice"]  spec) ;=> nil
