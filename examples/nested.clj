;; Nested specs — map values that are themselves maps allow subcommand trees,
;; similar to a CLI with command groups (e.g. `git remote add`, `git remote rm`).
;;
;; Anonymous functions have no :arglists metadata, so arity checking is skipped
;; unless you attach :arg-count via with-meta.

(require '[dispatch.core :refer [dispatch]])

(def spec
  {:user {:create (with-meta (fn [username email]
                               (str "Created user " username " <" email ">"))
                             {:arg-count 2})
          :delete (with-meta (fn [username]
                               (str "Deleted user " username))
                             {:arg-count 1})
          :list   (with-meta (fn []
                               "Listing all users")
                             {:arg-count 0})}
   :role {:assign (with-meta (fn [username role]
                               (str "Assigned role " role " to " username))
                             {:arg-count 2})
          :revoke (with-meta (fn [username role]
                               (str "Revoked role " role " from " username))
                             {:arg-count 2})}})

(dispatch ["user" "create" "alice" "alice@example.com"] spec) ;=> "Created user alice <alice@example.com>"
(dispatch ["user" "delete" "alice"]                     spec) ;=> "Deleted user alice"
(dispatch ["user" "list"]                               spec) ;=> "Listing all users"
(dispatch ["role" "assign" "alice" "admin"]             spec) ;=> "Assigned role admin to alice"
(dispatch ["role" "revoke" "alice" "admin"]             spec) ;=> "Revoked role admin from alice"
