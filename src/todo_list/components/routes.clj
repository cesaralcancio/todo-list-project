(ns todo-list.components.routes
  (:require [com.stuartsierra.component :as component]
            [todo-list.service :as service]))

(defrecord Routes []
  component/Lifecycle

  (start [this]
    (println ";; Starting Routes")
    (assoc this :local-routes service/routes))

  (stop [this]
    (println ";; Stopping Routes")
    this))

(defn new-routes []
  (->Routes))
