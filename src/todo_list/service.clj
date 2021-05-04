(ns todo-list.service
  (:require [io.pedestal.http.route :as route]
            [todo-list.controller.list :as controller.list]))

(def routes
  (route/expand-routes
    #{["/todo" :get controller.list/fetch-all :route-name :list-fetch-all]
      ["/todo" :post controller.list/create :route-name :list-create]

      ["/todo/:list-id" :get controller.list/fetch-by-id :route-name :list-fetch-by-id]
      ["/todo/:list-id" :post controller.list/item-create :route-name :list-item-create]

      ["/todo/:list-id/:item-id" :get controller.list/item-fetch-by-id :route-name :item-fetch-by-id]

      ; not implemented yet
      ["/todo/:list-id/:item-id" :put controller.list/echo :route-name :list-item-update]
      ["/todo/:list-id/:item-id" :delete controller.list/echo :route-name :list-item-delete]

      ["/version" :get [controller.list/version controller.list/handle-version] :route-name :handle-version]}))
