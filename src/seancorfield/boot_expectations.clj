(ns seancorfield.boot-expectations
  {:boot/export-tasks true}
  (:require [boot.core :as core :refer [deftask]]
            [boot.pod :as pod]))

(def base-pod-deps
  '[[expectations "2.1.3"]
    [org.clojure/tools.namespace "0.2.11"]])

(defn init [fresh-pod]
  (doseq [r '[[clojure.java.io :as io]
              [clojure.tools.namespace.find :as f]]]
    (pod/require-in fresh-pod r)))

(deftask expectations
  "Run Expectations test in a pod.

  There are no options for this task at present."
  [v verbose bool "Display namespace completed for each set of Expectations."]
  (core/with-pass-thru [fs]
    (let [pod-deps (update-in (core/get-env) [:dependencies] into base-pod-deps)
          pods     (pod/pod-pool pod-deps :init init)
          dirs     (mapv (memfn getPath) (core/input-dirs fs))]
      (core/cleanup (pods :shutdown))
      (let [{:keys [fail error] :as summary}
            (pod/with-eval-in (pods :refresh)
              (require '[expectations :as e])
              (doseq [n (mapcat #(f/find-namespaces-in-dir (io/file %)) ~dirs)]
                (require n))
              (e/disable-run-on-shutdown)
              (binding [e/ns-finished (if ~verbose (fn [ns] (println "\nCompleted" ns)) (constantly nil))]
                (e/run-all-tests)))]
        (when (pos? (+ fail error))
          (throw (ex-info "Some tests failed or errored" summary)))))))
