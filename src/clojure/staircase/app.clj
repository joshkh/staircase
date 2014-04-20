(ns staircase.app
  (:use [clojure.tools.logging :only (debug info error)]
        [environ.core :only (env)]
        [staircase.config :only (db-options app-options secrets)]
        staircase.resources.services
        staircase.resources.histories
        staircase.resources.steps)
  (:require [staircase.assets :as assets]
            [staircase.handler :as routing]
            [com.stuartsierra.component :as component]
            [staircase.sessions :as sessions]
            [staircase.data :as data]))

;; Inject dependencies and build up the system.
(defn build-app [options]
  (component/start
    (let [db (db-options options)]
      (-> (component/system-map
            :asset-pipeline (assets/pipeline :js-dir "/js" :css-dir "/css" :engine :v8 :coffee "src/coffee" :less "src/less")
            :config (app-options options)
            :secrets (secrets)
            :session-store (sessions/new-pg-session-store)
            :db (data/new-pooled-db db)
            :histories (new-history-resource)
            :services (new-services-resource)
            :steps (new-steps-resource)
            :router (routing/new-router))
          (component/system-using
            {:router [:config :secrets :session-store :histories :steps :asset-pipeline]
             :session-store [:db :config]
             :services [:db]
             :steps [:db]
             :histories [:db]})))))

(def system
  (delay
    (info "Building system with settings: " env)
    (build-app env)))

(defn handler [req]
  ((get-in @system [:router :handler]) req))
