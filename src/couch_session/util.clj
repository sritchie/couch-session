(ns couch-session.util
  (:use [clojure.string :only (replace-first)]))

(def couch-kwds #{:_id :_rev})
(def UNDERSCORE "UNDERSCORE")

(defn key-type [k]
  (cond (couch-kwds k) :default
        (string? k)    :string
        (keyword? k)   :keyword
        :else :default))

(defmulti encode-key key-type)

(defmethod encode-key :default [k] k)

(defmethod encode-key :keyword [k]
  (-> k name encode-key))

(defmethod encode-key :string [k]
  (keyword
   (if-not (.startsWith k "_")
     k
     (replace-first k "_" UNDERSCORE))))

(defmulti decode-key key-type)

(defmethod decode-key :default [k]
  (replace-first k "UNDERSCORE" "_"))

(defmethod decode-key :keyword [k]
  (-> k name decode-key))

(defmethod decode-key :string [k]
  (if-not (.startsWith k UNDERSCORE)
    k
    (replace-first k UNDERSCORE "_")))

(defn update-keys [update-fn m]
  (->> (for [[k v] m]
         [(update-fn k) v])
       (into {})))

(def encode (partial update-keys encode-key))
(def decode (partial update-keys decode-key))
