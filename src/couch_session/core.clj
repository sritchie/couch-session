(ns couch-session.core
  "CouchDB session storage."
  (:use [couch-session.util :only (encode decode)]
        ring.middleware.session.store)
  (:require [com.ashafa.clutch :as c]))

(defn clutch-hack
  "Workaround for clutch, which returns a id-less document with database status
   when you do (get-document nil)."
  [key]
  (if (empty? key) nil (c/get-document key)))

(deftype CouchStore [db]
  SessionStore
  (read-session [_ session-key]
    (-> (c/with-db db (clutch-hack session-key))
        (decode)))

  (write-session [_ session-key data]
    (c/with-db db
      (:_id (let [doc (clutch-hack session-key)
                  data (-> data
                           (encode)
                           (merge (select-keys doc [:_id :_rev])))]
              (cond (not doc) (c/put-document data :id session-key)
                    (or (= data {}) (= data doc)) doc
                    :else (c/put-document data))))))
  
  (delete-session [_ session-key]
    (c/with-db db
      (when-let [doc (clutch-hack session-key)]
        (c/delete-document doc)))
    nil))

(defn couch-store
  "Creates a couchdb-backed session storage engine."
  ([db] (CouchStore. db)))

(comment
  "Example usage:"
  (def example-store
    (couch-store
     (c/get-database (assoc (com.ashafa.clutch.utils/url "sessions") :username "username" :password "password")))))
