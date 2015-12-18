(set-env!
 :resource-paths #{"src"})

(task-options!
 pom {:project     'seancorfield/boot-expectations
      :version     "0.1.0-SNAPSHOT"
      :description "Run some expectations in boot!"
      :url         "https://github.com/seancorfield/boot-expectations"
      :scm         {:url "https://github.com/seancorfield/boot-expectations"}
      :license     {"Eclipse Public License"
                    "http://www.eclipse.org/legal/epl-v10.html"}})
