(ns todo-list.components.config
  (:require [com.stuartsierra.component :as component]))

(defrecord Config [env]
  component/Lifecycle

  (start [this]
    (println ";; Starting Config")
    (-> this
        (assoc :user "cesar-alcancio-user")
        (assoc :env env)))

  (stop [this]
    (println ";; Stopping Config")
    this))

(defn new-config [env]
  (map->Config {:env env}))
