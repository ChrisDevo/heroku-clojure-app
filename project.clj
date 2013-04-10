(defproject heroku-clojure-app "1.0.0-SNAPSHOT"
  :description "This app determines the applicable country to calculate
                VAT based upon the client's IP address, the credit card Bank
                Identifier Number (BIN) country, and the billing address country
                chosen by the user. It then calculates a total (including VAT)
                based upon a sales total entered by the user."
  :url "http://whispering-inlet-2503.herokuapp.com"
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
                 [org.clojure/tools.logging "0.2.6"]
		             [postgresql "9.1-901.jdbc4"]
                 [hiccup "1.0.2"]]
  :hooks [environ.leiningen.hooks]
  :main heroku-clojure-app.web
  :min-lein-version "2.0.0"
  :plugins [[environ/environ.lein "0.2.1"]
            [lein-ring "0.8.2"]]
  :profiles {:production {:env {:production true}}
             :dev {:dependencies [[ring-mock "0.1.3"]
                                  [midje "1.5.0"]]}}
  :ring {:handler heroku-clojure-app.web/app})
