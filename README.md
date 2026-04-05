# dispatch

A Clojure/ClojureScript library for dispatching command-line arguments to functions using a declarative spec map.

## Usage

Add to your `deps.edn`:

```clojure
io.github.jhartwell/dispatch {:mvn/version "0.1.0"}
```

Require the namespace:

```clojure
(require '[dispatch.core :refer [dispatch]])
```

## How it works

`dispatch` takes a vector of string arguments and a spec map, then walks the two together:

- Each string in the argument vector is converted to a keyword and looked up in the current spec map
- If the value is a **map**, dispatch recurses into it with the remaining arguments
- If the value is a **function or Var**, the remaining arguments are passed to it
- If the key is not found, `:dispatch/unmapped` is returned
- If the argument vector is empty, `:dispatch/empty-args` is returned

Use Var references (`#'fn-name`) in your spec so dispatch can read `:arglists` metadata for arity checking:

```clojure
(defn greet [name] (str "Hello, " name))

(def spec {:greet #'greet})

(dispatch ["greet" "Alice"] spec) ;=> "Hello, Alice"
```

## Nested commands

Nest maps to create subcommand trees:

```clojure
(defn create-user [username email] (str "Created " username))
(defn delete-user [username]       (str "Deleted " username))

(def spec
  {:user {:create #'create-user
          :delete #'delete-user}})

(dispatch ["user" "create" "alice" "alice@example.com"] spec) ;=> "Created alice"
(dispatch ["user" "delete" "alice"]                     spec) ;=> "Deleted alice"
```

## Variadic functions

Functions with rest arguments are supported:

```clojure
(defn log [level & messages]
  (str "[" level "] " (clojure.string/join " " messages)))

(def spec {:log #'log})

(dispatch ["log" "INFO" "server" "started"] spec) ;=> "[INFO] server started"
(dispatch ["log" "WARN"]                    spec) ;=> "[WARN] "
```

## Anonymous functions

Anonymous `fn` forms have no `:arglists` metadata, so arity checking is skipped by default. To enable arity checking on an anonymous function, attach `:arg-count` metadata via `with-meta`:

```clojure
;; Arity checked — :arg-count metadata present
(def spec
  {:greet (with-meta (fn [name] (str "Hello, " name))
                     {:arg-count 1})})

(dispatch ["greet" "Alice"] spec) ;=> "Hello, Alice"
(dispatch ["greet"]         spec) ;=> throws ExceptionInfo

;; Arity not checked — no metadata
(def spec
  {:greet (fn [name] (str "Hello, " name))})

(dispatch ["greet"] spec) ;=> throws clojure.lang.ArityException (runtime error)
```

## Error handling

`dispatch` returns `:dispatch/unmapped` for unknown commands and `:dispatch/empty-args` when called with no arguments:

```clojure
(dispatch ["unknown"] spec) ;=> :dispatch/unmapped
(dispatch []          spec) ;=> :dispatch/empty-args
```

Arity mismatches throw `ExceptionInfo`:

```clojure
(try
  (dispatch ["greet"] spec)
  (catch clojure.lang.ExceptionInfo e
    (ex-data e)))
;=> {:provided 0, :expected #{1}}
```

The `ex-data` map contains:

| Key | Description |
|---|---|
| `:provided` | Number of arguments supplied |
| `:expected` | Set of valid argument counts |

## Platform differences

dispatch is a `.cljc` library that runs identically on both the JVM and ClojureScript.

### Arity validation

Arity checking uses metadata attached to functions:

- **Var references** (`#'fn-name`): `:arglists` is attached automatically by `defn` on both JVM and ClojureScript. Use these in your spec for reliable arity checking.
- **Anonymous functions with `:arg-count`**: attach `{:arg-count n}` via `with-meta` to enable exact arity checking.
- **Anonymous functions without metadata**: arity check is skipped entirely. The function is called directly; a wrong argument count results in a runtime error from the platform.

```clojure
;; Var — arity checked on both JVM and ClojureScript
(dispatch ["log"] spec)
;; => throws ExceptionInfo {:provided 0, :expected #{1}}

;; Anonymous fn with :arg-count — arity checked on both platforms
(def spec {:greet (with-meta (fn [name] (str "Hello, " name)) {:arg-count 1})})
(dispatch ["greet"] spec)
;; => throws ExceptionInfo {:provided 0, :expected #{1}}

;; Anonymous fn without metadata — no arity check, runtime throws
(def spec {:greet (fn [name] (str "Hello, " name))})
(dispatch ["greet"] spec)
;; => throws clojure.lang.ArityException (JVM) or JS runtime error (ClojureScript)
```

## Development

Run JVM tests:

```
clj -M:test
```

Run ClojureScript tests (requires Node.js):

```
clj -M:cljs-test
```

Build jar:

```
clj -T:build jar
```

Clean build artifacts:

```
clj -T:build clean
```

## License

[Eclipse Public License 2.0](LICENSE)
