(ns todo-list.components.pedestal
  (:require [com.stuartsierra.component :as component]
            [io.pedestal.http :as http]
            [io.pedestal.interceptor :as i]))

(defrecord Pedestal [config database routes webapp]
  component/Lifecycle

  (start [this]
    (def service-map
      {::http/routes (:local-routes routes)
       ::http/type   :jetty
       ::http/port   8890})

    (def components-interceptor (i/interceptor {:name  ::system
                                                :enter (fn [context]
                                                         (assoc-in context
                                                                   [:request :components]
                                                                   webapp))}))

    ; Add components interceptor to all routes
    (def service-map-doc (-> service-map
                             (http/default-interceptors)
                             (update ::http/interceptors conj components-interceptor)))

    ; For Prod
    (defn start []
      (http/start (http/create-server service-map-doc)))

    ;; For interactive development
    (defonce server (atom nil))

    (defn start-dev []
      (reset! server
              (http/start (http/create-server
                            (assoc service-map-doc
                              ::http/join? false)))))

    (defn stop-dev []
      (http/stop @server))

    (defn restart []
      (stop-dev)
      (start-dev))

    (println ";; Pedestal")

    (-> this
        (assoc :server server)
        (assoc :start start)
        (assoc :start-dev start-dev)
        (assoc :stop-dev stop-dev)
        (assoc :restart-dev restart)))

  (stop [this]
    (println ";; Stopping Pedestal")
    this))

(defn new-pedestal []
  (map->Pedestal {}))
