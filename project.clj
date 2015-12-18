(defproject clj-mlm "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [juxt/dirwatch "0.2.2" :exclusions [[org.clojure/clojure]]]
                 [net.incongru.watchservice/barbary-watchservice "1.0"]])
