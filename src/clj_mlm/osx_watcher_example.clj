(ns clj-mlm.osx-watcher-example
  (:import (com.barbarysoftware.watchservice WatchableFile
                                             WatchEvent
                                             WatchEvent$Kind
                                             StandardWatchEventKind
                                             WatchService)
           (java.util.concurrent TimeUnit)))


(defonce watcher (atom {}))

(defn kind->keyword
  [kind]
  (get {StandardWatchEventKind/ENTRY_CREATE :create
        StandardWatchEventKind/ENTRY_DELETE :delete
        StandardWatchEventKind/ENTRY_MODIFY :modify}
       kind))

(defn ev->map
  [ev]
  {:file (.context ev)
   :count (.count ev)
   :action (kind->keyword (.kind ev))})

(def watcher-ready? (comp not nil?))

(defn reset-or-cancel!
  [watch-key]
  (when-not (. watch-key reset)
    (. watch-key cancel)))

(defn poll-for-seconds
  [watch-service seconds]
  (.poll watch-service (long seconds) TimeUnit/SECONDS))

(defn is-valid? [watch-key]
  (and watch-key (.isValid watch-key)))

(defn wait-for-events [watch-service f]
  (when (watcher-ready? watch-service)
    (let [watch-key (poll-for-seconds watch-service 10)]
      (when (is-valid? watch-key)
        (doseq [ev (.pollEvents watch-key)]
          (f (ev->map ev)))
        (reset-or-cancel! watch-key))

      (send-off *agent* wait-for-events f)
      watch-service)))

(defn register-watcher
  [watcheable-file watch-service]
  (.register watcheable-file
             watch-service
             (into-array (type StandardWatchEventKind/ENTRY_CREATE)
                         [StandardWatchEventKind/ENTRY_CREATE
                          StandardWatchEventKind/ENTRY_DELETE
                          StandardWatchEventKind/ENTRY_MODIFY])))

(defn add-osx-watcher [f path]
  (let [watch-service (WatchService/newWatchService)
        watchable-file (WatchableFile. (clojure.java.io/file path))]
    (register-watcher watchable-file watch-service)
    (send-off (agent watch-service
                     :meta {::watcher true}
                     :error-handler (fn [ag ex]
                                      (.printStackTrace ex)
                                      (send-off ag wait-for-events f)))
              wait-for-events f)))

(defn add-watcher [f path]
  (swap! watcher assoc path (add-osx-watcher f path)))

(defn close-watcher
  "Close an existing watcher and free up it's resources."
  [watcher]
  {:pre [(::watcher (meta watcher))]}
  (send-off watcher (fn [w]
                      (when w (.close w))
                      nil)))
