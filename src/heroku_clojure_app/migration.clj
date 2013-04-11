(ns heroku-clojure-app.migration
  (:require [clojure.java.jdbc :as sql]))

(defn migrated? []
  (not (zero?
         (sql/with-connection (System/getenv "DATABASE_URL")
           (sql/with-query-results results
             ["select count(*) from information_schema.tables where table_name='logs'"]
             (:count (first results)))))))

(defn migrate []
  (when (not (migrated?))
    (print "Creating database structure...") (flush)
  (sql/with-connection (System/getenv "DATABASE_URL")
    (sql/create-table :logs
      [:id :serial "PRIMARY KEY"]
      [:body :varchar "NOT NULL"]
      [:created_at :timestamp "NOT NULL" "DEFAULT CURRENT_TIMESTAMP"]))))

(defn -main []
  (migrate)
  (println " done"))