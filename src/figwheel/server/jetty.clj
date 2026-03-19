(ns figwheel.server.jetty
  (:require
   [clojure.string :as string]
   [ring.adapter.jetty :as jt]))

(defn set-log-level! [log-lvl]
  (when log-lvl
    (println (format "run-jetty: Skipping (set-log-level! %s). Jetty now uses SLF4J to configure logging."
                     log-lvl))))

(defn run-jetty [handler {:keys [log-level configurator] :as options}]
  (let [configurator'
        (if-let [config-fn
                 (and (symbol? configurator)
                      (try
                        (resolve configurator)
                        (catch Throwable t)))]
          config-fn
          (do
            (when configurator
              (println "Unable to resolve Jetty :configurator"
                       (pr-str configurator)))
            identity))]
    (jt/run-jetty
     handler
     (assoc options
            :configurator
            (fn [server]
              (configurator' server)
              (set-log-level! log-level))))))

;; these default options assume the context of starting a server in development-mode
;; from the figwheel repl
(def default-options {:join? false
                      :thread-idle-timeout (* 1000 60 60 100)
                      :max-idle-time (* 1000 60 60 100)})

(defn run-server [handler options]
  (run-jetty
   handler
   (merge default-options options)))
