(ns todo-list.integration.list-test
  (:require [clojure.test :refer :all]
            [todo-list.components :as components]
            [io.pedestal.http :as http]
            [io.pedestal.test :as test]))

(def result (components/start-dev))
(def server (-> result :pedestal :server))
(def store (-> result :database :store))

(defn test-request [verb url]
  (test/response-for (::http/service-fn @server) verb url))

(deftest todo-list-test
  (testing "Validate the version api"
    (let [version-response (test-request :get "/version")
          version-body (clojure.edn/read-string (:body version-response))]
      (is "cesar-alcancio-user" (:user version-body))
      (is "version-1.0.0" (:version version-body))
      (is :dev (:environment version-body))))

  (testing "Validate list and items"
    (let [name "cesar-alcancio-list-1"
          list-1 (test-request :post (str "/todo?" name))
          list-1-location (-> list-1 :headers (get "Location"))
          get-list-1 (test-request :get list-1-location)
          get-list-1-body (clojure.edn/read-string (:body get-list-1))
          list-uuid (first (map key get-list-1-body))
          list (get get-list-1-body list-uuid)]
      (is list-uuid (:id list))
      (is name (:id name))
      (is {} (:items name))

      (let [item-name "item-1"
            item-status "true"
            item-created (test-request :post (str "/todo/" list-uuid "?name=" item-name "&status=" item-status))]
        (is item-name (:name (clojure.edn/read-string (:body item-created))))
        (is item-status (:status (clojure.edn/read-string (:body item-created))))))))
