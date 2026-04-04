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
- If the value is a **function**, the remaining arguments are passed to it
- If the key is not found, `nil` is returned

```clojure
(def spec
  {:greet (fn [name] (str "Hello, " name))})

(dispatch ["greet" "Alice"] spec) ;=> "Hello, Alice"
```

## Nested commands

Nest maps to create subcommand trees:

```clojure
(def spec
  {:user {:create (fn [username email] (str "Created " username))
          :delete (fn [username]       (str "Deleted " username))}})

(dispatch ["user" "create" "alice" "alice@example.com"] spec) ;=> "Created alice"
(dispatch ["user" "delete" "alice"]                     spec) ;=> "Deleted alice"
```

## Variadic functions

Functions with rest arguments are supported:

```clojure
(defn log [level & messages]
  (str "[" level "] " (clojure.string/join " " messages)))

(def spec {:log log})

(dispatch ["log" "INFO" "server" "started"] spec) ;=> "[INFO] server started"
(dispatch ["log" "WARN"]                    spec) ;=> "[WARN] "
```

## Error handling

`dispatch` returns `nil` for unknown commands:

```clojure
(dispatch ["unknown"] spec) ;=> nil
```

On the JVM, arity mismatches throw `ExceptionInfo` (see [Platform differences](#platform-differences)):

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

dispatch is implemented as a `.cljc` file and runs on both the JVM and ClojureScript, with one behavioural difference:

### Arity validation

**JVM:** Before calling a matched function, dispatch uses Java reflection to inspect the function's compiled arities. If the number of provided arguments does not match any valid arity, dispatch throws an `ExceptionInfo` with `:provided` and `:expected` keys before the function is ever invoked.

**ClojureScript:** Java reflection is not available, so arity validation is skipped. The matched function is called directly with the remaining arguments. If the argument count is wrong, the JavaScript engine will throw its own error.

```clojure
;; JVM — controlled ex-info thrown by dispatch before calling the fn
(try
  (dispatch ["log"] spec)
  (catch clojure.lang.ExceptionInfo e
    (ex-data e)))
;=> {:provided 0, :expected #{1}}

;; ClojureScript — dispatch calls the fn, JS engine throws
(try
  (dispatch ["log"] spec)
  (catch :default e
    (.-message e)))
;=> "Invalid arity: 0"
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
