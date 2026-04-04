(ns dispatch.core-test
  (:require #?(:clj  [clojure.test :refer [deftest is]]
               :cljs [cljs.test :refer [deftest is]])
            [clojure.string :as str]
            [dispatch.core :refer [dispatch]]))

;; ---------------------------------------------------------------------------
;; Spec fixtures
;; ---------------------------------------------------------------------------

(defn- ->int [s]
  #?(:clj  (parse-long s)
     :cljs (js/parseInt s)))

(defn- greet [name] (str "Hello, " name))
(defn- add [a b] (+ (->int a) (->int b)))
(defn- ping [] "pong")
(defn- echo [& args] (vec args))
(defn- log [level & msgs] (str level ": " (str/join " " msgs)))
(defn- db-connect [host] (str "connected to " host))
(defn- db-disconnect [] "disconnected")
(defn- japanese-greet [name] (str "こんにちは、" name))
(defn- arabic-greet [name] (str "مرحبا، " name))
(defn- korean-greet [name] (str "안녕하세요, " name))
(defn- russian-greet [name] (str "Привет, " name))
(defn- hebrew-greet [name] (str "שלום, " name))

(def spec
  {:greet       greet
   :add         add
   :ping        ping
   :echo        echo
   :log         log
   :db          {:connect    db-connect
                 :disconnect db-disconnect}
   :挨拶         japanese-greet
   :تحية        arabic-greet
   :인사         korean-greet
   :приветствие russian-greet
   :ברכה        {:שלום hebrew-greet}})

;; ---------------------------------------------------------------------------
;; Basic dispatch
;; ---------------------------------------------------------------------------

(deftest dispatches-to-fn-with-single-arg
  (is (= (greet "Alice") (dispatch ["greet" "Alice"] spec))))

(deftest dispatches-to-fn-with-multiple-args
  (is (= (add "1" "2") (dispatch ["add" "1" "2"] spec))))

(deftest dispatches-to-fn-with-no-args
  (is (= (ping) (dispatch ["ping"] spec))))

(deftest dispatches-to-variadic-fn
  (is (= (echo "a" "b" "c") (dispatch ["echo" "a" "b" "c"] spec))))

(deftest variadic-fn-accepts-zero-args
  (is (= (echo) (dispatch ["echo"] spec))))

(deftest dispatches-to-fn-with-required-and-variadic-args
  (is (= (log "INFO" "starting" "up") (dispatch ["log" "INFO" "starting" "up"] spec))))

(deftest dispatches-to-fn-with-required-arg-only
  (is (= (log "WARN") (dispatch ["log" "WARN"] spec))))

;; ---------------------------------------------------------------------------
;; Unicode keys
;; ---------------------------------------------------------------------------

(deftest dispatches-japanese-key
  (is (= (japanese-greet "Alice") (dispatch ["挨拶" "Alice"] spec))))

(deftest dispatches-arabic-key
  (is (= (arabic-greet "Alice") (dispatch ["تحية" "Alice"] spec))))

(deftest dispatches-korean-key
  (is (= (korean-greet "Alice") (dispatch ["인사" "Alice"] spec))))

(deftest dispatches-russian-key
  (is (= (russian-greet "Alice") (dispatch ["приветствие" "Alice"] spec))))

(deftest dispatches-nested-hebrew-keys
  (is (= (hebrew-greet "Alice") (dispatch ["ברכה" "שלום" "Alice"] spec))))

;; ---------------------------------------------------------------------------
;; Nested spec (map value)
;; ---------------------------------------------------------------------------

(deftest dispatches-into-nested-map
  (is (= (db-connect "localhost") (dispatch ["db" "connect" "localhost"] spec))))

(deftest dispatches-into-nested-map-no-args
  (is (= (db-disconnect) (dispatch ["db" "disconnect"] spec))))

;; ---------------------------------------------------------------------------
;; Unknown keys
;; ---------------------------------------------------------------------------

(deftest returns-nil-for-unknown-top-level-key
  (is (nil? (dispatch ["unknown"] spec))))

(deftest returns-nil-for-unknown-nested-key
  (is (nil? (dispatch ["db" "unknown"] spec))))

(deftest returns-nil-for-empty-args
  (is (nil? (dispatch [] spec))))
