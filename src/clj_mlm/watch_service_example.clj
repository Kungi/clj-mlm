(ns clj-mlm.watch-service-example
  (:require [juxt.dirwatch :refer [watch-dir close-watcher]]))

(def watcher (atom {}))

(defn add-watcher [path]
  (swap! watcher assoc path (watch-dir println (clojure.java.io/file path))))
