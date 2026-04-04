(ns dispatch.test-runner
  (:require [cljs.test :as test]
            [dispatch.core-test]))  ;; .cljc — shared tests only

(defmethod test/report [:cljs.test/default :end-run-tests] [m]
  (when-not (test/successful? m)
    (js/process.exit 1)))

(defn -main []
  (test/run-tests 'dispatch.core-test))
