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
  {:greet       #'greet
   :add         #'add
   :ping        #'ping
   :echo        #'echo
   :log         #'log
   :db          {:connect    #'db-connect
                 :disconnect #'db-disconnect}
   :挨拶         #'japanese-greet
   :تحية        #'arabic-greet
   :인사         #'korean-greet
   :приветствие #'russian-greet
   :ברכה        {:שלום #'hebrew-greet}})

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
;; Arity mismatch
;; ---------------------------------------------------------------------------

(deftest throws-when-required-arg-missing-on-variadic-fn
  (is (thrown? #?(:clj clojure.lang.ExceptionInfo :cljs cljs.core/ExceptionInfo)
               (dispatch ["log"] spec))))

(deftest throws-on-too-few-args
  (is (thrown? #?(:clj clojure.lang.ExceptionInfo :cljs cljs.core/ExceptionInfo)
               (dispatch ["greet"] spec))))

(deftest throws-on-too-many-args
  (is (thrown? #?(:clj clojure.lang.ExceptionInfo :cljs cljs.core/ExceptionInfo)
               (dispatch ["greet" "Alice" "extra"] spec))))

(deftest ex-info-contains-provided-count
  (let [e (try (dispatch ["greet" "Alice" "extra"] spec)
               (catch #?(:clj clojure.lang.ExceptionInfo :cljs :default) e e))]
    (is (= 2 (:provided (ex-data e))))))

(deftest ex-info-contains-expected-arities
  (let [e (try (dispatch ["greet"] spec)
               (catch #?(:clj clojure.lang.ExceptionInfo :cljs :default) e e))]
    (is (contains? (:expected (ex-data e)) 1))))

;; ---------------------------------------------------------------------------
;; Unknown keys
;; ---------------------------------------------------------------------------

(deftest returns-unmapped-for-unknown-top-level-key
  (is (= :dispatch/unmapped (dispatch ["unknown"] spec))))

(deftest returns-unmapped-for-unknown-nested-key
  (is (= :dispatch/unmapped (dispatch ["db" "unknown"] spec))))

(deftest returns-empty-args-for-empty-args
  (is (= :dispatch/empty-args (dispatch [] spec))))

;; ---------------------------------------------------------------------------
;; Anonymous fn with :arg-count metadata
;; ---------------------------------------------------------------------------

(deftest dispatches-anon-fn-with-arg-count-metadata
  (let [spec {:hi (with-meta (fn [name] (str "Hi, " name)) {:arg-count 1})}]
    (is (= "Hi, Alice" (dispatch ["hi" "Alice"] spec)))))

(deftest throws-on-too-few-args-for-anon-fn-with-arg-count
  (let [spec {:hi (with-meta (fn [name] (str "Hi, " name)) {:arg-count 1})}]
    (is (thrown? #?(:clj clojure.lang.ExceptionInfo :cljs cljs.core/ExceptionInfo)
                 (dispatch ["hi"] spec)))))

(deftest throws-on-too-many-args-for-anon-fn-with-arg-count
  (let [spec {:hi (with-meta (fn [name] (str "Hi, " name)) {:arg-count 1})}]
    (is (thrown? #?(:clj clojure.lang.ExceptionInfo :cljs cljs.core/ExceptionInfo)
                 (dispatch ["hi" "Alice" "extra"] spec)))))

(deftest skips-arity-check-for-anon-fn-without-metadata
  (let [spec {:hi (fn [name] (str "Hi, " name))}]
    (is (= "Hi, Alice" (dispatch ["hi" "Alice"] spec)))))

(deftest runtime-throws-when-anon-fn-without-metadata-arity-mismatch
  (let [spec {:hi (fn [name] (str "Hi, " name))}]
    (is (thrown? #?(:clj clojure.lang.ArityException :cljs :default)
                 (dispatch ["hi"] spec)))))
