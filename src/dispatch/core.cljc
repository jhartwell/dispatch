(ns dispatch.core)

#?(:clj
   (defn- fn-arities [f]
     (let [methods      (.getDeclaredMethods (class f))
           invoke-ms    (filter #(= "invoke" (.getName %)) methods)
           do-invoke-ms (filter #(= "doInvoke" (.getName %)) methods)
           variadic?    (boolean (seq do-invoke-ms))
           arities      (->> invoke-ms (map #(count (.getParameterTypes %))) set)
           min-arity    (if variadic?
                          (dec (count (.getParameterTypes (first do-invoke-ms))))
                          (when (seq arities) (apply min arities)))]
       {:arities   arities
        :variadic? variadic?
        :min-arity min-arity})))

#?(:clj
   (defn- verify-then-dispatch [v args]
     (let [{:keys [arities variadic? min-arity]} (fn-arities v)
           n         (count args)
           arity-ok? (if variadic?
                       (>= n min-arity)
                       (contains? arities n))]
       (if arity-ok?
         (apply v args)
         (throw (ex-info "Argument count mismatch"
                         {:provided n
                          :expected (if variadic? #{min-arity} arities)}))))))

(defn dispatch [args spec]
  (when (seq args)
    (let [k (keyword (first args))
          v (get spec k)]
      (cond
        (nil? v) nil
        (fn? v)  #?(:clj  (verify-then-dispatch v (rest args))
                    :cljs (apply v (rest args)))
        (map? v) (dispatch (rest args) v)))))
