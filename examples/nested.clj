;; Nested specs — map values that are themselves maps allow subcommand trees,
;; similar to a CLI with command groups (e.g. `git remote add`, `git remote rm`).

(require '[dispatch.core :refer [dispatch]])

(def spec
  {:user {:create (fn [username email]
                    (str "Created user " username " <" email ">"))
          :delete (fn [username]
                    (str "Deleted user " username))
          :list   (fn []
                    "Listing all users")}
   :role {:assign (fn [username role]
                    (str "Assigned role " role " to " username))
          :revoke (fn [username role]
                    (str "Revoked role " role " from " username))}})

(dispatch ["user" "create" "alice" "alice@example.com"] spec) ;=> "Created user alice <alice@example.com>"
(dispatch ["user" "delete" "alice"]                     spec) ;=> "Deleted user alice"
(dispatch ["user" "list"]                               spec) ;=> "Listing all users"
(dispatch ["role" "assign" "alice" "admin"]             spec) ;=> "Assigned role admin to alice"
(dispatch ["role" "revoke" "alice" "admin"]             spec) ;=> "Revoked role admin from alice"
