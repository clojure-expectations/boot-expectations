;; copyright (c) 2015-2016 Sean Corfield

(ns seancorfield.boot-expectations
  {:boot/export-tasks true}
  (:require [boot.core :as core :refer [deftask]]
            [boot.pod :as pod]))

(def ^:private expectations-version "2.1.8")

(defn pod-deps []
  (remove pod/dependency-loaded?
          [['expectations expectations-version]
           ['org.clojure/tools.namespace "0.2.11"]]))

(defn init [requires fresh-pod]
  (doseq [r (into '[[clojure.java.io :as io]
                    [clojure.tools.namespace.find :as f]
                    [expectations :as e]]
                  requires)]
    (pod/require-in fresh-pod r))
  (pod/with-eval-in fresh-pod
    (e/disable-run-on-shutdown)))

(defn replace-clojure-version
  "Given a desired Clojure version and an artifact/version pair,
  return the artifact/version pair, updated if it was for Clojure."
  [new-version [artifact version :as dep]]
  (if (= 'org.clojure/clojure artifact) [artifact new-version] dep))

(deftask expecting
  "Provide Expectations execution context.

  Useful for running tests in a REPL if you want to rely on
  boot-expectations to load Expectations for you instead of
  requiring it directly in your own build.boot file:

    boot expecting repl

  In Emacs, use C-u C-c M-j when you jack in and add expecting to
  the Boot command that CIDER shows you it will run."
  []
  (when-not (pod/dependency-loaded? ['expectations expectations-version])
    (core/merge-env! :dependencies [['expectations expectations-version :scope "test"]]))
  (core/with-pass-thru fs
    (require 'expectations)
    (let [disable-run-on-shutdown (resolve 'expectations/disable-run-on-shutdown)]
      (disable-run-on-shutdown))))

(deftask expectations
  "Run Expectations tests in a pod.

  You can specify the version of Clojure to use for testing:
    e.g., boot expectations -c 1.6.0
  If this is not specified, the version of Clojure provided by your project
  will be used.

  You can specify regular expressions for namespaces to include and exclude."
  [c clojure  VERSION str    "the version of Clojure for testing."
   e exclude  REGEX   regex  "the filter for excluded namespaces"
   i include  REGEX   regex  "the filter for included namespaces"
   r requires NS      #{sym} "namespaces to be required at pod startup"
   s shutdown FN      #{sym} "functions to be called prior to pod shutdown"
   S startup  FN      #{sym} "functions to be called at pod startup"
   v verbose          bool   "Display each namespace completed"]
  (let [exclude  (or exclude #"^$")
        include  (or include #".*")
        requires (or requires #{})
        shutdown (or shutdown #{})
        startup  (or startup #{})
        pod-deps (update-in (core/get-env) [:dependencies]
                            (fn [deps]
                              (cond->> (into deps (pod-deps))
                                clojure (mapv (partial replace-clojure-version clojure)))))
        pods     (pod/pod-pool pod-deps :init (partial init requires))]
    (core/cleanup (pods :shutdown))
    (core/with-pass-thru [fs]
      (let [dirs (mapv (memfn getPath) (core/input-dirs fs))]
        (let [{:keys [fail error] :as summary}
              (pod/with-eval-in (pods)
                (doseq [n (distinct (mapcat #(f/find-namespaces-in-dir (io/file %)) ~dirs))
                        :when (and (re-find ~include (name n))
                                   (not (re-find ~exclude (name n))))]
                  (require n))
                (try
                  (doseq [f ~startup] (f))
                  (binding [e/ns-finished (if ~verbose (fn [ns] (println "\nCompleted" ns)) (constantly nil))]
                    (e/run-all-tests))
                  (finally
                    (doseq [f ~shutdown] (f)))))]
          (pods :refresh)
          (when (pos? (+ fail error))
            (throw (ex-info "Some tests failed or errored" summary))))))))
