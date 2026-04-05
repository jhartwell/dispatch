(ns build
  (:require [clojure.tools.build.api :as b]))

(def lib 'io.github.jhartwell/dispatch)
(def version "0.1.0")
(def class-dir "target/classes")
(def basis (delay (b/create-basis {:project "deps.edn"})))
(def jar-file "target/dispatch.jar")

(defn clean [_]
  (b/delete {:path "target"}))

(defn jar [_]
  (clean nil)
  (b/write-pom {:class-dir class-dir
                :lib       lib
                :version   version
                :basis     @basis
                :src-dirs  ["src"]})
  (b/copy-file {:src  (b/pom-path {:lib lib :class-dir class-dir})
                :target "pom.xml"})
  (b/copy-dir {:src-dirs   ["src" "resources"]
               :target-dir class-dir})
  (b/jar {:class-dir class-dir
          :jar-file  jar-file}))
