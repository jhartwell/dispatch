(ns dispatch.core-jvm-test
  (:require [clojure.test :refer [deftest is]]
            [dispatch.core :refer [dispatch]]
            [dispatch.core-test :refer [spec]]))

;; ---------------------------------------------------------------------------
;; Arity mismatch (JVM only — relies on reflection-based arity checking)
;; ---------------------------------------------------------------------------

(deftest throws-when-required-arg-missing-on-variadic-fn
  (is (thrown? clojure.lang.ExceptionInfo
               (dispatch ["log"] spec))))

(deftest throws-on-too-few-args
  (is (thrown? clojure.lang.ExceptionInfo
               (dispatch ["greet"] spec))))

(deftest throws-on-too-many-args
  (is (thrown? clojure.lang.ExceptionInfo
               (dispatch ["greet" "Alice" "extra"] spec))))

(deftest ex-info-contains-provided-count
  (let [e (try (dispatch ["greet" "Alice" "extra"] spec)
               (catch clojure.lang.ExceptionInfo e e))]
    (is (= 2 (:provided (ex-data e))))))

(deftest ex-info-contains-expected-arities
  (let [e (try (dispatch ["greet"] spec)
               (catch clojure.lang.ExceptionInfo e e))]
    (is (contains? (:expected (ex-data e)) 1))))
