(ns todo-list.main
  (:require [todo-list.components :as components]
            [clojure.pprint :as pp]
            [io.pedestal.test :as test]
            [io.pedestal.http :as http]))

; This is just a class to do manually tests while I am automatizing the integration tests
(def result (components/start-dev))
; (def result (components/start-prod))

; get server from components
(def server (-> result :pedestal :server))
(def store (-> result :database :store))
(pp/pprint store)
(def item (get-in @store [#uuid "c0c47e2d-dc31-4d1a-8521-a81afa31f759" :items #uuid "a1ce89f1-ba35-4076-8b99-2fd66826cf38"] nil))
(println item)
(assoc item :name "Cesar-new")

(defn test-request [verb url]
  (test/response-for (::http/service-fn @server) verb url))

; Validation the version service manually
(println (test-request :get "/version"))

; Validating the services manually
(def todo-cesar (test-request :post "/todo?name=cesar-alcancio-todo-list-1"))
(println todo-cesar)
(println todo-cesar)
(def location-todo-cesar (-> todo-cesar :headers (get "Location")))
(println location-todo-cesar)
(pp/pprint (clojure.edn/read-string (:body (test-request :get location-todo-cesar))))

(def all-todos (test-request :get "/todo"))
(pp/pprint all-todos)

(def item-1 (test-request :post (str location-todo-cesar "?name=cesar-item-1")))
(def item-2 (test-request :post (str location-todo-cesar "?name=cesar-item-2&status=true")))
(def item-3 (test-request :post (str location-todo-cesar "?name=cesar-item-3&status=false")))
(println item-1)
(println item-2)
(println (clojure.edn/read-string (:body item-3)))

(def items (test-request :get location-todo-cesar))
(def first-item-id (-> items :body (clojure.edn/read-string) :items first first))
(pp/pprint items)
(pp/pprint (clojure.edn/read-string (:body items)))
(println first-item-id)

(def url-item-1 (str location-todo-cesar "/" first-item-id))
(println url-item-1)
(def get-item (test-request :get (str location-todo-cesar "/" first-item-id)))
(def put-item (test-request :put (str location-todo-cesar "/" first-item-id "?name=cesar-item-1-updated&status=true")))
(def delete-item (test-request :delete (str location-todo-cesar "/" first-item-id)))
(println (clojure.edn/read-string (:body get-item)))
(println put-item)
(println delete-item)
