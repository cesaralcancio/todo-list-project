(ns todo-list.components.database
  (:require [com.stuartsierra.component :as component]))

(defonce store (atom {}))

(defrecord Database []
  component/Lifecycle

  (start [component]
    (println ";; Starting database")
    (-> component
        (assoc :store store)))

  (stop [component]
    (println ";; Stopping database")
    component))

(defn new-database []
  (map->Database {}))
