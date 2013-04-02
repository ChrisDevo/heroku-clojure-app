(defproject heroku-clojure-app "1.0.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://heroku-clojure-app.herokuapp.com"
  :license {:name "FIXME: choose"
            :url "http://example.com/FIXME"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/java.jdbc "0.2.3"]
                 [compojure "1.1.3"]
                 [ring "1.1.5"]
                 [ring-basic-authentication "1.0.1"]
                 [environ "0.2.1"]
                 [com.cemerick/drawbridge "0.0.6"]
                 [org.clojure/data.json "0.2.1"]
                 [org.clojure/tools.logging "0.2.6"]]
  :min-lein-version "2.0.0"
  :plugins [[environ/environ.lein "0.2.1"]
            [lein-ring "0.8.2"]]
  :hooks [environ.leiningen.hooks]
  :ring {:handler heroku-clojure-app.web/app}
  :profiles {:production {:env {:production true}}
             :dev {:dependencies [[ring-mock "0.1.3"]
                                  [midje "1.5.0"]]}})
