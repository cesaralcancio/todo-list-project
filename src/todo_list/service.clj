(ns todo-list.service
  (:require [io.pedestal.http.route :as route]
            [todo-list.controller.list :as controller]))

(def routes
  (route/expand-routes
    #{["/todo" :post [controller/list-create] :route-name :list-create]
      ["/todo" :get [controller/db-interceptor controller/list-todos] :route-name :list-todos]

      ["/todo/:list-id" :post [controller/entity-render controller/list-item-view controller/db-interceptor controller/list-item-create]]
      ["/todo/:list-id" :get [controller/entity-render controller/db-interceptor controller/list-view]]

      ["/todo/:list-id/:item-id" :get [controller/entity-render controller/list-item-view controller/db-interceptor]]
      ; not implemented yet
      ["/todo/:list-id/:item-id" :put controller/echo :route-name :list-item-update]
      ["/todo/:list-id/:item-id" :delete controller/echo :route-name :list-item-delete]

      ["/version" :get [controller/version controller/handle-version] :route-name :handle-version]}))
