(ns couch-session.core
  "CouchDB session storage."
  (:use [couch-session.util :only (encode decode)]
        ring.middleware.session.store)
  (:require [com.ashafa.clutch :as c]))

(deftype CouchStore [db]
  SessionStore
  (read-session [_ session-key]
    (-> (c/with-db db (c/get-document session-key))
        (decode)))

  (write-session [_ session-key data]
    (c/with-db db
      (:_id (let [doc (c/get-document session-key)
                  data (-> data
                           (encode)
                           (merge (select-keys doc [:_id :_rev])))]
              (cond (not doc) (c/create-document data session-key)
                    (or (= data {}) (= data doc)) doc
                    :else (c/update-document data))))))
  
  (delete-session [_ session-key]
    (c/with-db db
      (when-let [doc (c/get-document session-key)]
        (c/delete-document doc)))
    nil))

(defn couch-store
  "Creates a couchdb-backed session storage engine."
  ([db] (CouchStore. db)))

(comment
  "Example usage:"
  (def example-store
    (couch-store
     (c/get-database {:name  "sessions"
                      :language "clojure"
                      :username "uname"
                      :password "password"}))))
