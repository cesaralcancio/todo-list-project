(ns todo-list.service
  (:require [io.pedestal.http.route :as route]
            [todo-list.controller.list :as controller.list]))

(def db-interceptor
  {:name :database-interceptor
   :enter
         (fn [context]
           (let [store (-> context :request :components :database :store)]
             (update context :request assoc :store @store)))
   :leave
         (fn [context]
           (let [store (-> context :request :components :database :store)]
             (if-let [[op & args] (:tx-data context)]
               (do
                 (apply swap! store op args)
                 (assoc-in context [:request :store] @store))
               context)))})

(def routes
  (route/expand-routes
    #{["/todo" :post controller.list/create :route-name :list-create]
      ["/todo" :get controller.list/fetch-all :route-name :list-fetch-all]

      ["/todo/:list-id" :post controller.list/item-create :route-name :list-item-create]
      ["/todo/:list-id" :get [controller.list/entity-render db-interceptor controller.list/fetch-by-id]]

      ["/todo/:list-id/:item-id" :get [controller.list/entity-render controller.list/item-fetch-by-id db-interceptor]]

      ; not implemented yet
      ["/todo/:list-id/:item-id" :put controller.list/echo :route-name :list-item-update]
      ["/todo/:list-id/:item-id" :delete controller.list/echo :route-name :list-item-delete]

      ["/version" :get [controller.list/version controller.list/handle-version] :route-name :handle-version]}))
