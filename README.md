# heroku-clojure-app (EU VAT Calculator)

A [Heroku](http://www.heroku.com) web app using Compojure.

This generated project has a few basics set up beyond the bare Compojure defaults:

* Cookie-backed session store
* Stack traces when in development
* Environment-based config via [environ](https://github.com/weavejester/environ)
* [HTTP-based REPL debugging](https://devcenter.heroku.com/articles/debugging-clojure) via [drawbridge](https://github.com/cemerick/drawbridge)

## Project Description

* The goal of this project is to calculate the appropriate VAT depending on
which country is chosen to return the VAT rate.
* This project is hosted on [Heroku](http://whispering-inlet-2503.herokuapp.com/eu_vat_calculator.html)
* It is built using Clojure, JavaScript and HTML.
* The main executable is web.clj found in the src folder.
* The data files (json), Javascript, and HTML files are found in the public folder.

## Usage

To start a local web server for development you can either eval the
commented out forms at the bottom of `web.clj` from your editor or
launch from the command line:

    $ lein run -m heroku-clojure-app.web

    or

    $ lein ring server

    or, using foreman from the Heroku Toolbelt

    $ foreman start

Mostly I use `lein ring server` to test locally.


## License

Copyright © 2013 FIXME

Distributed under the Eclipse Public License, the same as Clojure.
