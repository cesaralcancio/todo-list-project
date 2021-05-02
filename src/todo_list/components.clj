(ns todo-list.components
  (:require [com.stuartsierra.component :as component]
            [todo-list.components.database :as database]
            [todo-list.components.pedestal :as component.pedestal]
            [todo-list.components.routes :as routes]
            [todo-list.components.config :as config]
            [todo-list.components.webapp :as webapp]))

(defn base-system [env]
  (component/system-map
    :config (config/new-config env)
    :database (database/new-database)
    :routes (routes/new-routes)
    :webapp (component/using (webapp/new-webapp) [:config :database :routes])
    :pedestal (component/using (component.pedestal/new-pedestal) [:config :database :routes :webapp])))

(defn start-prod []
  (let [system-return (component/start (base-system :prod))
        start (-> system-return :pedestal :start)]
    (start)))

(defn start-dev []
  (let [system-return (component/start (base-system :dev))
        start-dev (-> system-return :pedestal :start-dev)
        restart (-> system-return :pedestal :restart-dev)]
    (try (start-dev) (catch Exception e (try (restart) (catch Exception e (println "Error in restart!" e)))))
    system-return))
