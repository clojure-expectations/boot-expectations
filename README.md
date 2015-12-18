# boot-expectations

[Boot] task for running [Expectations] tests

[](dependency)
```clojure
[seancorfield/boot-expectations "0.1.0-SNAPSHOT"] ;; latest release
```
[](/dependency)

## Usage

Add `boot-expectations` to your `build.boot` dependencies and `require` the
namespace:

```clj
(set-env! :dependencies '[[seancorfield/boot-expectations "X.Y.Z" :scope "test"]])
(require '[seancorfield.boot-expectations :refer :all])
```

If your tests are in a directory that is not included in the classpath, you will need to add it

```
(set-env! :source-paths #{"test"})
```

You can see the options available on the command line:

```bash
$ boot expectations -h
```

or in the REPL:

```clj
boot.user=> (doc expectations)
```

## Continuous Testing

Whisper some magic incantations to boot, and it will run tests every time you save a file
```
boot watch expectations
```
with sound!
```
boot watch speak expectations
```

## License

Copyright © 2015 Sean Corfield

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

[1]: http://clojars.org/seancorfield/boot-expectations/latest-version.svg?cache=5
[2]: http://clojars.org/seancorfield/boot-expectations
[Boot]: https://github.com/boot-clj/boot
[Expectations]: https://github.com/jaycfields/expectations
