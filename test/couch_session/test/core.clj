;;; shamelessly lifted from https://github.com/hozumi/mongodb-session/blob/master/test/hozumi/test_mongodb_session.clj


(ns couch-session.test.core
  (:use [couch-session.core]
        [ ring.middleware.session.store]
        [clojure.test])
  (:require [com.ashafa.clutch :as clutch]))

(def test-db (merge (com.ashafa.clutch.utils/url nil)
                    {:path "couch-session-test"
                     :username "couch-session-tester"
                     :password "couch-session-password"}))


(defn server-fixture [f]
  (clutch/delete-database test-db)
  (clutch/create-database test-db)
  (f)
  (clutch/delete-database test-db))
  


(use-fixtures :each server-fixture)

(deftest read-not-exist
  (let [store (couch-store test-db)]
    (is (read-session store "non-existent")
        {})))

(deftest session-create
  (let [store (couch-store test-db)
        sess-key (write-session store nil {:foo "bar"})
        entity (read-session store sess-key)]
    (is (not (nil? sess-key)))
    (is (and (:_id entity) (:_date entity)))
    (is (= (dissoc entity :_id :_date)
           {:foo "bar"}))))

(deftest session-update
  (let [store (couch-store test-db)
        sess-key (write-session store nil {:foo "bar"})
        sess-key* (write-session store sess-key {:bar "baz"})
        entity (read-session store sess-key*)]
    (is (= sess-key sess-key*))
    (is (and (:_id entity) (:_date entity)))
    (is (= (dissoc entity :_id :_date)
           {:bar "baz"}))))

(deftest session-auto-key-change
  (let [store (couch-store test-db)
        sess-key (write-session store nil {:foo "bar"})
        sess-key* (write-session store sess-key {:bar "baz"})
        entity (read-session store sess-key*)]
    (is (not= sess-key sess-key*))
    (is (and (:_id entity) (:_date entity)))
    (is (= (dissoc entity :_id :_date)
           {:bar "baz"}))))

(deftest session-delete
  (let [store (couch-store test-db)
        sess-key (write-session store nil {:foo "bar"})]
    (is (nil? (delete-session store sess-key)))
    (is (= (read-session store sess-key)
           {}))))


