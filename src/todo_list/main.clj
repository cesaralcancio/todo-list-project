(ns todo-list.main
  (:require [todo-list.components :as components]
            [clojure.pprint :as pp]
            [io.pedestal.test :as test]
            [io.pedestal.http :as http]))

(def result (components/start-dev))
; (def result (components/start-prod))

; get server from components
(def server (-> result :pedestal :server))
(defn test-request [verb url]
  (io.pedestal.test/response-for (::http/service-fn @server) verb url))

; Validation the version service manually
(println (test-request :get "/version"))

; Validating the services manually
(def todo-cesar (test-request :post "/todo?name=cesar-alcancio-todo-list"))
(println todo-cesar)
(def location-todo-cesar (-> todo-cesar :headers (get "Location")))
(println location-todo-cesar)
(println (test-request :get location-todo-cesar))

(def all-todos (test-request :get "/todo"))
(println all-todos)

(def item-1 (test-request :post (str location-todo-cesar "?name=cesar-item-1")))
(def item-2 (test-request :post (str location-todo-cesar "?name=cesar-item-2&status=true")))
(def item-3 (test-request :post (str location-todo-cesar "?name=cesar-item-2&status=false")))
(println item-1)
(println item-2)
(println item-3)

(def items (test-request :get location-todo-cesar))
(def first-item-id (-> items :body (clojure.edn/read-string) :items first first))
(println items)
(println first-item-id)

(def get-item (test-request :get (str location-todo-cesar "/" first-item-id)))
(def put-item (test-request :put (str location-todo-cesar "/" first-item-id)))
(def delete-item (test-request :delete (str location-todo-cesar "/" first-item-id)))
(println get-item)
(println put-item)
(println delete-item)
