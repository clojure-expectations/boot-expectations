(ns seancorfield.boot-expectations
  {:boot/export-tasks true}
  (:require [boot.core :as core :refer [deftask]]
            [boot.pod :as pod]))

(defn pod-deps []
  (remove pod/dependency-loaded?
          '[[expectations "2.1.4"]
            [org.clojure/tools.namespace "0.2.11"]]))

(defn init [fresh-pod]
  (doseq [r '[[clojure.java.io :as io]
              [clojure.tools.namespace.find :as f]]]
    (pod/require-in fresh-pod r)))

(defn replace-clojure-version
  "Given a desired Clojure version and an artifact/version pair,
  return the artifact/version pair, updated if it was for Clojure."
  [new-version [artifact version :as dep]]
  (if (= 'org.clojure/clojure artifact) [artifact new-version] dep))

(deftask expectations
  "Run Expectations tests in a pod.

  You can specify the version of Clojure to use for testing:
    e.g., boot expectations -v 1.6.0
  If this is not specified, the version of Clojure provided by your project
  will be used.

  You can specify regular expressions for namespaces to include and exclude."
  [c clojure VERSION str   "the version of Clojure for testing."
   e exclude REGEX   regex "the filter for excluded namespaces"
   i include REGEX   regex "the filter for included namespaces"
   v verbose         bool  "Display each namespace completed"]
  (core/with-pass-thru [fs]
    (let [pod-deps (update-in (core/get-env) [:dependencies]
                              (fn [deps]
                                (cond->> (into deps (pod-deps))
                                  clojure (mapv (partial replace-clojure-version clojure)))))
          pods     (pod/pod-pool pod-deps :init init)
          dirs     (mapv (memfn getPath) (core/input-dirs fs))
          include  (or include #".*")
          exclude  (or exclude #"^$")]
      (core/cleanup (pods :shutdown))
      (let [{:keys [fail error] :as summary}
            (pod/with-eval-in (pods :refresh)
              (require '[expectations :as e])
              (e/disable-run-on-shutdown)
              (doseq [n (distinct (mapcat #(f/find-namespaces-in-dir (io/file %)) ~dirs))
                      :when (and (re-find ~include (name n))
                                 (not (re-find ~exclude (name n))))]
                (require n))
              (binding [e/ns-finished (if ~verbose (fn [ns] (println "\nCompleted" ns)) (constantly nil))]
                (e/run-all-tests)))]
        (when (pos? (+ fail error))
          (throw (ex-info "Some tests failed or errored" summary)))))))
