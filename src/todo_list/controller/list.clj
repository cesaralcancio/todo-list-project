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
           (let [request (:request context)
                 response (util/ok context)]
             (assoc context :response response)))})

(def entity-render
  {:name :entity-render
   :leave
         (fn [context]
           (if-let [item (:result context)]
             (assoc context :response (util/ok item))
             context))})

(defn list-create [{{{store :store} :database} :components query-params :query-params :as request}]
  (let [id (util/uuid)
        name (get-in query-params [:name] "Unnamed List")
        new-list (make-list id name)
        url (route/url-for :list-view :params {:list-id id})]
    (apply swap! store assoc [id new-list])
    (util/created new-list "Location" url)))

(defn list-todos [request]
  (util/ok (-> request :store)))

(def list-view
  {:name :list-view
   :enter
         (fn [context]
           (if-let [db-id (get-in context [:request :path-params :list-id])]
             (if-let [the-list (find-list-by-id (get-in context [:request :store]) (util/->uuid db-id))]
               (assoc context :result the-list)
               context)
             context))})

(def list-item-view
  {:name :list-item-view
   :leave
         (fn [context]
           (if-let [list-id (get-in context [:request :path-params :list-id])]
             (if-let [item-id (get-in context [:request :path-params :item-id])]
               (if-let [item (find-list-item-by-ids (get-in context [:request :store]) list-id item-id)]
                 (assoc context :result item)
                 context)
               context)
             context))})

(def list-item-create
  {:name :list-item-create
   :enter
         (fn [context]
           (if-let [list-id (get-in context [:request :path-params :list-id])]
             (let [item-id (str (gensym "i"))
                   nm (get-in context [:request :query-params :name] "Unnamed Item")
                   status (get-in context [:request :query-params :status] false)
                   new-item (make-list-item list-id item-id nm status)]
               (-> context
                   (assoc :tx-data [list-item-add list-id item-id new-item])
                   (assoc-in [:request :path-params :item-id] item-id)))
             context))})

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
