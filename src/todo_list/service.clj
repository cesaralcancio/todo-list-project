(ns todo-list.service
  (:require [io.pedestal.http.route :as route]))

(defn response [status body & {:as headers}]
  {:status status :body body :headers (merge {"Content-Type" "application/json"} headers)})

(def ok (partial response 200))
(def created (partial response 201))
(def accepted (partial response 202))

(defn find-list-by-id [dbval db-id]
  (get dbval db-id))

(defn find-list-item-by-ids [dbval list-id item-id]
  (get-in dbval [list-id :items item-id] nil))

(defn list-item-add
  [dbval list-id item-id new-item]
  (if (contains? dbval list-id)
    (assoc-in dbval [list-id :items item-id] new-item)
    dbval))

(defn make-list [nm]
  {:name  nm
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
                 response (ok context)]
             (assoc context :response response)))})

(def entity-render
  {:name :entity-render
   :leave
         (fn [context]
           (if-let [item (:result context)]
             (assoc context :response (ok item))
             context))})

(def list-create
  {:name :list-create
   :enter
         (fn [context]
           (let [nm (get-in context [:request :query-params :name] "Unnamed List")
                 new-list (make-list nm)
                 db-id (str (gensym "l"))
                 url (route/url-for :list-view :params {:list-id db-id})]
             (assoc context
               :response (created new-list "Location" url)
               :tx-data [assoc db-id new-list])))})

(defn list-todos [request]
  (ok (-> request :store)))

(def list-view
  {:name :list-view
   :enter
         (fn [context]
           (if-let [db-id (get-in context [:request :path-params :list-id])]
             (if-let [the-list (find-list-by-id (get-in context [:request :store]) db-id)]
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

(def routes
  (route/expand-routes
    #{["/todo" :post [db-interceptor list-create]]
      ["/todo" :get [db-interceptor list-todos] :route-name :list-todos]

      ["/todo/:list-id" :post [entity-render list-item-view db-interceptor list-item-create]]
      ["/todo/:list-id" :get [entity-render db-interceptor list-view]]

      ["/todo/:list-id/:item-id" :get [entity-render list-item-view db-interceptor]]
      ; not implemented yet
      ["/todo/:list-id/:item-id" :put echo :route-name :list-item-update]
      ["/todo/:list-id/:item-id" :delete echo :route-name :list-item-delete]

      ["/version" :get [version handle-version] :route-name :handle-version]}))
