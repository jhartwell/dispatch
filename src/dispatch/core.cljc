(ns dispatch.core)

(defn- meta->arity-info [m]
  (if-let [arglists (seq (:arglists m))]
    (let [variadic? (boolean (some #(some #{'&} %) arglists))
          min-arity (when variadic?
                      (->> arglists
                           (filter #(some #{'&} %))
                           (map #(count (take-while (fn [x] (not= '& x)) %)))
                           (apply min)))
          arities   (when-not variadic?
                      (->> arglists (map count) set))]
      {:arities   arities
       :variadic? variadic?
       :min-arity min-arity})
    (when-let [n (:arg-count m)]
      {:arities #{n} :variadic? false :min-arity nil})))

(defn- verify-then-dispatch [f arity-info args]
  (let [n (count args)]
    (if-let [{:keys [arities variadic? min-arity]} arity-info]
      (let [arity-ok? (if variadic?
                        (>= n min-arity)
                        (contains? arities n))]
        (if arity-ok?
          (apply f args)
          (throw (ex-info "Argument count mismatch"
                          {:provided n
                           :expected (if variadic? #{min-arity} arities)}))))
      (apply f args))))

(defn dispatch [args spec]
  (if-not (seq args)
    :dispatch/empty-args
    (let [k (keyword (first args))
          v (get spec k)]
      (cond
        (nil? v) :dispatch/unmapped
        (var? v) (verify-then-dispatch @v (meta->arity-info (meta v)) (rest args))
        (fn? v)  (verify-then-dispatch v  (meta->arity-info (meta v)) (rest args))
        (map? v) (dispatch (rest args) v)))))
