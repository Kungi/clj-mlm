(ns clj-mlm.watch-service-example
  (:require [clojure.edn :as edn]
            [juxt.dirwatch :refer [watch-dir close-watcher]]))

(defonce watcher (atom {}))

(defn file-modified [file]
  (println "Modified: " (get (edn/read-string (slurp file))
                             :foo
                             :no-foo-exists)))

(defn file-created [file]
  (println "Created: " file))

(defn file-deleted [file]
  (println "Deleted: " file))

(defn dispatcher [{:keys [file action] :as e}]
  (case action
    :modify (file-modified file)
    :create (file-created file)
    :delete (file-deleted file)
    (throw Exception (str "No valid action in event " e))))

(defn add-watcher [f path]
  (swap! watcher assoc path (watch-dir f (clojure.java.io/file path))))

#_(if (= :a :a)
    1
    2)

#_(if (= :a :b)
    1
    2)

#_(cond
    (= :a :c) 1
    (= :b :b) 2
    :default 3)

#_(case :c
    :a 1
    :b 2
    3)
