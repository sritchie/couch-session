;;; shamelessly lifted from https://github.com/hozumi/mongodb-session/blob/master/test/hozumi/test_mongodb_session.clj


(ns couch-session.test.core
  (:use [couch-session.core]
        [ ring.middleware.session.store]
        [clojure.test])
  (:require [com.ashafa.clutch :as clutch]))


(def ^:dynamic *test-db* nil)


(defn server-fixture [f]
  (let [db-url (assoc (com.ashafa.clutch.utils/url "couch-session-test")
                 :username "couch-session-tester"
                 :password "couch-session-password")]
    (try
      (binding [*test-db* (clutch/get-database db-url)]
        (f))
      (finally
        (clutch/delete-database db-url)))))
    


(use-fixtures :each server-fixture)

(deftest read-not-exist
  (let [store (couch-store *test-db* )]
    (is (read-session store "non-existent")
        {})))

(deftest session-create
  (let [store (couch-store *test-db* )
        sess-key (write-session store nil {:foo "bar"})
        entity (read-session store sess-key)]
    (is (not (nil? sess-key)))
    (is (and (:_id entity) (:_rev entity)))
    (is (= (dissoc entity :_id :_rev)
           {:foo "bar"}))))

(deftest session-update
  (let [store (couch-store *test-db* )
        sess-key (write-session store nil {:foo "bar"})
        sess-key* (write-session store sess-key {:bar "baz"})
        entity (read-session store sess-key*)]
    (is (= sess-key sess-key*))
    (is (and (:_id entity) (:_rev entity)))
    (is (= (dissoc entity :_id :_rev)
           {:bar "baz"}))))

(deftest session-update-same-key
  (let [store (couch-store *test-db* )
        sess-key (write-session store nil {:foo "bar"})
        sess-key* (write-session store sess-key {:foo "baz"})
        entity (read-session store sess-key*)]
    (is (= sess-key sess-key*))
    (is (and (:_id entity) (:_rev entity)))
    (is (= (dissoc entity :_id :_rev)
           {:foo "baz"}))))

(deftest session-delete
  (let [store (couch-store *test-db* )
        sess-key (write-session store nil {:foo "bar"})]
    (is (nil? (delete-session store sess-key)))
    (is (= (read-session store sess-key)
           {}))))


(deftest session-underscore-key-filtering
  (let [store (couch-store *test-db* )
        sess-key (write-session store nil {:_foo "bar"})
        entity (read-session store sess-key)]
    (is (not (nil? sess-key)))
    (is (and (:_id entity) (:_rev entity)))
    (is (= (dissoc entity :_id :_rev)
           {:_foo "bar"}))))


(deftest session-rekey
  (let [store (couch-store test-db)
        sess-key (write-session store nil {:foo "bar"})
        old-entity (read-session store sess-key)
        sess-key* (write-session store nil old-entity)
        entity (read-session store sess-key*)]
    (is (not= sess-key sess-key*))
    (is (and (:_id entity) (:_rev entity)))
    (is (and (:_id old-entity) (:_rev old-entity)))
    (is (= (dissoc entity :_id :_rev)
           {:foo "bar"}))
    (is (= (dissoc old-entity :_id :_rev)
           (dissoc entity :_id :_rev)))))
