(ns staircase.test.steps
  (:import java.sql.SQLException)
  (:use clojure.test
        staircase.resources
        staircase.resources.steps
        staircase.resources.histories
        staircase.protocols
        staircase.helpers
        [clojure.tools.logging :only (info warn debug)]
        [staircase.config :only (db-options)]
        [com.stuartsierra.component :as component]
        [environ.core :only (env)])
  (:require staircase.sql
            [staircase.data :as data]
            [clojure.java.jdbc :as sql]))

;; The next 20 lines are a bit boiler-platey, and could be extracted.
(def db-spec (db-options env))

;; Create a db, but do not start it - it is cycled for each test.
(def db (atom (data/new-pooled-db db-spec)))

;; We set up and tear down by deleting everything in the database.
(defn drop-tables [] 
  (debug "Dropping tables...")
  (staircase.sql/drop-all-tables db-spec))

(defn clean-slate [f]
  (drop-tables)
  (swap! db component/start)
  (f))

(defn clean-up [f]
  (try (f) (finally
             (swap! db component/stop)
             (drop-tables))))

(use-fixtures :each clean-slate clean-up)

;; On to the tests....

(defn get-steps [] (new-steps-resource @db))

(deftest read-empty-steps
  (let [steps (get-steps)
        fake-id (new-id)]
    (testing "get-all"
      (is (= [] (get-all steps))))
    (testing "exists?"
      (is (not (exists? steps fake-id))))
    (testing "get-one"
      (is (= nil (get-one steps fake-id))))
    (testing "delete"
      (is (= nil (delete steps fake-id))))
    (testing "update"
      (is (= nil (update steps fake-id {:title "bar"}))))))

(deftest write-to-empty-steps
  (binding [staircase.resources/context {:user "no-one@no-where.nil"}]
    (let [steps (get-steps)
          histories (new-history-resource (:db steps)) ;; re-use the db.
          my-history (create histories {:title "test history"})
          doc-1 {:history_id my-history
                 :title "step 1"
                 :tool "quicksearch"
                 :stamp "testing"
                 :data {"foo" "bar" "quuxibility" 1.23}
                 }
          doc-2 {:history_id my-history :title "step 2" :tool "resultstable" :stamp "testing"}
          id-1 (create steps doc-1)
          id-2 (create steps doc-2)
          got (get-one steps id-1)
          all (get-all steps)]
      (testing "Autogenerated column values"
        (is (instance? java.util.Date (:created_at got))
            (str (pr-str (:created_at got)) " should be a date"))
        (is (instance? java.util.UUID id-1)))
      (testing "retrieved record" 
        (is (=
             (dissoc got :created_at) ;; Ignore created at in this comparison.
             (-> doc-1 ;; We expect the following transformations on the document:
                 (assoc :id id-1)  ;; It should have an id
                 (dissoc :history_id))))) ;; It should not have a history id.
      (testing "The new state of the world - created step in all steps"
        (is (some #{id-1} (map :id all))))
      (testing "The numbers of steps"
        (is (= 2 (count all))))
      (testing "The existence of the new steps"
        (is (exists? steps id-1))
        (is (exists? steps id-2)))
      (testing "That the step was added to its history"
        (let [history (get-one histories my-history)]
          (is (= 2    (count (:steps history))))
          (is (= id-2 (get-in history [:steps 0]))))) ;; Steps should be listed newest first.
      (let [updated (update steps id-1 {:title "changed the title"})
            retrieved (get-one steps id-1)
            all (get-all steps)]
        (testing "Still in history"
          (is (some #{id-1} (:steps (get-one histories my-history)))))
        (testing "Changed the title"
          (is (= "changed the title" (:title updated)))
          (is (= "changed the title" (:title retrieved))))
        (testing "Changed, and did not add a step"
          (is (= 2 (count all))))))))

