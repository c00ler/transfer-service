# Transfer Service

[![CircleCI](https://circleci.com/gh/c00ler/transfer-service/tree/master.svg?style=svg&circle-token=7fa3b70bbac1be39a5b06f28996a803bfcf91109)](https://circleci.com/gh/c00ler/transfer-service/tree/master)

RESTful API for money transfers between accounts.

## Dependencies

- **Java 11**

Alternatively project can be build and run with **Docker**.

## Tech stack

- [Javalin](https://javalin.io/)
- [H2](https://www.h2database.com/html/main.html)
- [jOOQ](https://www.jooq.org/)
- [Flyway](https://flywaydb.org/)
- [REST-assured](http://rest-assured.io/)

## Maven wrapper

Service is using [maven wrapper](https://github.com/takari/maven-wrapper), so there is no need in having maven in the
execution environment.

## Important notes

- Database is running completely in memory. After the application is stopped, all the 
data will be lost.
- For simplicity all amounts are in **cents** and without a currency.
- Data consistency is guaranteed by append-only approach for storing the data and usage
of idempotency keys.

## Database

Scripts to populate [database schema](https://github.com/c00ler/transfer-service/tree/master/src/main/resources/db/migration) are 
applied during the application startup using flyway. For convenience [classes required by jOOQ](https://github.com/c00ler/transfer-service/tree/master/src/main/jooq/com/revolut/transfer/persistence/jooq)
are pre-generated and stored in a separate source root, to not to be mixed with the application sources. In case
there are changes to the database schema jOOQ classes can be re-generated using the following command:

```shell script
./mvnw -Pjooq-generate
```

## Running tests

To run __unit__ and __integration tests__ execute the following command:

```shell script
./mvnw clean verify
``` 

## Running service locally

Build an executable jar:

```shell script
./mvnw clean package
```

Then run it:

```shell script
java -jar ./target/service-jar-with-dependencies.jar
```

By default server is running on port `7000`. It can be changed using `server.port` system property, e.g.:

```shell script
java -Dserver.port=8080 -jar ./target/service-jar-with-dependencies.jar
```

## Building and running inside docker

**First run may take some time. Provided image is not optimized for any kind of workload 
and can only be used for testing.**

First build an image:

```shell script
docker build -t transfer-service:latest .
```

Start the container using the following command:

```shell script
docker run --rm -it -p 7000:7000 transfer-service:latest
```
