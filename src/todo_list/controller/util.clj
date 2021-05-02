(ns todo-list.controller.util)

(defn response [status body & {:as headers}]
  {:status  status
   :body    body
   :headers (merge {"Content-Type" "application/json"} headers)})

(def ok (partial response 200))
(def created (partial response 201))
(def accepted (partial response 202))

(defn uuid [] (java.util.UUID/randomUUID))

(defn ->uuid [db-id]
  (java.util.UUID/fromString db-id))
