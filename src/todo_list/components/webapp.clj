(ns todo-list.components.webapp
  (:require [com.stuartsierra.component :as component]))

(defrecord WebApp []
  component/Lifecycle

  (start [this]
    (println ";; Starting WebApp")
    this)

  (stop [this]
    (println ";; Stopping WebApp")
    this))

(defn new-webapp []
  (->WebApp))
