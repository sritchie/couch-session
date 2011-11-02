Relax!
======

`couch-session` lets you use CouchDB as Clojure/Ring's HTTP session
storage engine.

Installation
============

Add:

    [couch-session "1.0.0"]

to dependencies in your `project.clj`.

Usage
=====

`couch-session` is a drop-in replacement for Ring's native session store:

    (ns hello
      (:use [couch-session.core :only (couch-store)]
            [com.ashafa.clutch :only (get-database)])

    (def store
      (get-database {:name  "sessions"
                     :language "clojure"
                     :username "user"
                     :password "pass"}))
    (def app
      (-> ...
          ... other middleware ...
          (wrap-session {:store (couch-store store)})
          ....))

License
=======

Copyright (C) 2011 Sam Ritchie <sritchie09@gmail.com>

Distributed under the Eclipse Public License, the same as Clojure.
