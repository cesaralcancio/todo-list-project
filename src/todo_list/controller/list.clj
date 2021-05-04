(ns todo-list.controller.list
  (:require [io.pedestal.http.route :as route]
            [todo-list.controller.util :as util]))

(defn find-list-by-id [dbval db-id]
  (get dbval db-id))

(defn find-list-item-by-ids [dbval list-id item-id]
  (get-in dbval [list-id :items item-id] nil))

(defn list-item-add
  [dbval list-id item-id new-item]
  (if (contains? dbval list-id)
    (assoc-in dbval [list-id :items item-id] new-item)
    dbval))

(defn make-list [id nm]
  {:id    id
   :name  nm
   :items {}})

(defn make-list-item [list-id item-id nm status]
  {:list-id list-id
   :item-id item-id
   :name    nm
   :done?   (Boolean/valueOf status)})

(def echo
  {:name :echo
   :enter
         (fn [context]
           (let [response (util/ok context)]
             (assoc context :response response)))})

(def entity-render
  {:name :entity-render
   :leave
         (fn [context]
           (if-let [item (:result context)]
             (assoc context :response (util/ok item))
             context))})

(defn create [{{{store :store} :database} :components query-params :query-params}]
  (let [id (util/uuid)
        name (get-in query-params [:name] "Unnamed List")
        new-list (make-list id name)
        url (route/url-for :list-view :params {:list-id id})]
    (apply swap! store assoc [id new-list])
    (util/created new-list "Location" url)))

(defn fetch-all [request]
  (util/ok @(-> request :components :database :store)))

(defn fetch-by-id [request]
  (let [db-id (get-in request [:path-params :list-id])
        store (-> request :components :database :store)
        the-list (find-list-by-id @store (util/->uuid db-id))]
    (util/ok the-list)))

(defn item-fetch-by-id [request]
  (let [store (-> request :components :database :store)
        list-id (get-in request [:path-params :list-id])
        item-id (get-in request [:path-params :item-id])
        item (find-list-item-by-ids @store (util/->uuid list-id) (util/->uuid item-id))]
    (util/ok item)))

(defn item-create [request]
  (if-let [list-id (util/->uuid (get-in request [:path-params :list-id]))]
    (let [store (-> request :components :database :store)
          item-id (util/uuid)
          item-name (get-in request [:query-params :name] "Unnamed Item")
          item-status (get-in request [:query-params :status] false)
          new-item (make-list-item list-id item-id item-name item-status)]
      (apply swap! store list-item-add [list-id item-id new-item])
      (util/ok (find-list-item-by-ids @store list-id item-id)))))

(def version
  {:name :version
   :enter
         (fn [context]
           (-> context
               (update :request assoc :version "1.0.0")))})

(defn handle-version [request]
  {:status 200
   :body   {:version     (str "version-" (:version request))
            :user        (-> request :components :config :user)
            :environment (-> request :components :config :env)}})
