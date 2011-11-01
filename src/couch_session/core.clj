(ns couch-session.core
  "CouchDB session storage."
  (:use ring.middleware.session.store)
  (:require [com.ashafa.clutch :as c]))

(deftype CouchStore [db]
  SessionStore
  (read-session [_ session-key]
    (-> (c/with-db db (c/get-document session-key))
        (dissoc :_id :_rev)))
  (write-session [_ session-key data]
    (:_id (c/with-db db (apply c/create-document
                               data
                               session-key))))
  (delete-session [_ session-key]
    (c/with-db db (c/delete-document
                   (c/get-document session-key)))
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
