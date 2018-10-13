akka-http-slick

## Prereq

* [SBT](https://www.scala-sbt.org/download.html)
* [Docker](https://docs.docker.com/install/)

## Running

> terminal1

```
$ docker run --name example-db -p5432:5432 -d postgres
$ sbt
> reStart
```

use `reStop` to stop the server

> terminal2

```
$ curl -X GET http://localhost:8080/examples/1
```

