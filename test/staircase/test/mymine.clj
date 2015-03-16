(ns staircase.test.mymine
  (:import java.sql.SQLException)
  (:use clojure.test
        staircase.resources
        staircase.resources.mymine
        staircase.protocols
        staircase.helpers
        [clojure.tools.logging :only (info warn debug)]
        [staircase.config :only (db-options)]
        [com.stuartsierra.component :as component]
        [environ.core :only (env)])
  (:require [clojure.java.jdbc :as sql]
    [staircase.projects :as projects]
    [cheshire.core :as json]))

(def db-spec (db-options env))

(defn ignore-errors [f]
  (try (f) (catch SQLException e nil)))

(defn drop-tables [] 
  (debug "Dropping tables...")
  (doseq [table [:project_contents :projects]]
    (ignore-errors #(sql/db-do-commands db-spec (str "DELETE FROM " (name table)) (sql/drop-table-ddl table)))
    (debug "Dropped" table)))

(defn clean-slate [f]
  (drop-tables)
  (f))

(defn clean-up [f]
  (try (f) (finally (drop-tables))))

(use-fixtures :each clean-slate clean-up)

(deftest write-to-projects
  (let [resource (component/start (new-mymine-resource :db {:connection db-spec}))
        folder {"title" "My Title" "owner_id" "nil@nil"}
        folderresults (first (projects/create-project folder))]
    (testing "Inserting a new folder"
      (is (number? (:id folderresults))))
    (testing "Adding item to folder"
      (let [item {"project_id" (:id folderresults) "item_id" "A List" "type" "List" "source" "NoMine"}
            itemresults (first (json/parse-string (projects/add-item-to-project (:id folderresults) item)))]
        (is (not (nil? (itemresults "id"))))))))