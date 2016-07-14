# boot-expectations [![Join the chat at https://gitter.im/seancorfield/boot-expectations](https://badges.gitter.im/seancorfield/boot-expectations.svg)](https://gitter.im/seancorfield/boot-expectations?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

[Boot] task for running [Expectations] tests

[](dependency)
```clojure
[seancorfield/boot-expectations "1.0.11"] ;; latest release
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

You will need to do this _outside_ the middleware layer of any prior task since `boot-expectations` creates a pod based on the dependencies outside its middleware layer for efficiency.

You can see the options available on the command line:

```bash
$ boot expectations -h
```

or in the REPL:

```bash
$ boot expecting repl
```

The `expecting` task adds an Expectations context and disables running tests at shutdown.

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

## Changes

- **1.0.11** - 07/14/2016 - Update to Expectations 2.1.9.
- **1.0.10** - 07/07/2016 - Add `expecting` task.
- **1.0.9** - 05/10/2016 - Add `--startup` option.
- **1.0.8** - 04/21/2016 - Update to Expectations 2.1.8.
- **1.0.7** - 03/17/2016 - Reorder pod manipulation to speed up testing (#11).
- **1.0.5** - 01/30/2016 - Bug fix for `--requires` option.
- **1.0.3** - 12/25/2015 - Add `--requires` / `--shutdown` options.
- **1.0.2** - 12/24/2015 - Create pod before task body for efficiency.
- **1.0.1** - 12/20/2015 - Add `distinct` to dedupe list of namespaces (#7).
- **1.0.0** - 12/19/2015 - Initial public release.

## License

Copyright © 2015-2016 Sean Corfield

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

[1]: http://clojars.org/seancorfield/boot-expectations/latest-version.svg?cache=5
[2]: http://clojars.org/seancorfield/boot-expectations
[Boot]: https://github.com/boot-clj/boot
[Expectations]: https://github.com/jaycfields/expectations
