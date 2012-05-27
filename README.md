Relax!
======

`couch-session` lets you use CouchDB as Clojure/Ring's HTTP session
storage engine.

Installation
============

Add:

```clojure
[couch-session "1.1.1"]
```

to dependencies in your `project.clj`.

Usage
=====

`couch-session` is a drop-in replacement for Ring's native session store:

```clojure
(ns hello
  (:use [couch-session.core :only (couch-store)]
        [com.ashafa.clutch :only (get-database)])

 (def store
  (get-database (assoc (com.ashafa.clutch.utils/url "sessions")
                      :username "username"
                      :password "password")))

(def app
  (-> ...
      ... other middleware ...
      (wrap-session {:store (couch-store store)})
      ....))
```

Testing
=====
To run the tests, create a database called `couch-session-test` on localhost, and a user `couch-session-tester` with password `couch-session-password` which has admin privileges to create and delete databases.

License
=======

Copyright (C) 2011 Sam Ritchie <sritchie09@gmail.com>

Distributed under the Eclipse Public License, the same as Clojure.
