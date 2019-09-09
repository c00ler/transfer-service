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

## Endpoints

There is an [e2e test](https://github.com/c00ler/transfer-service/blob/master/src/test/java/com/revolut/transfer/EndToEndTestScenarioIT.java) 
that shows the whole flow.

All provided examples are using [HTTPie](https://httpie.org/) for making calls.

### Create a new account

Request: `POST /api/v1/accounts`

Request body: `No`

Example:

As soon as application is using in-memory database, accounts for testing have to be first created. To created a new account
the following command can be used:

```shell script
http --verbose POST :7000/api/v1/accounts
``` 

The output of the command should be similar to the following:

```shell script
HTTP/1.1 201 Created
Content-Length: 0
Content-Type: application/json
Date: Sun, 08 Sep 2019 20:41:57 GMT
Location: /api/v1/accounts/09bba65c-763d-4fe4-b687-f4aee07c88c2
Server: Javalin
```

`Location` header in the response contains an ID of a newly created account.

### Get account information

Request: `GET /api/v1/accounts/:account_id`

Request body: `No`

Example:

Information about an account can be obtained using the following command:

```shell script
http --verbose GET :7000/api/v1/accounts/09bba65c-763d-4fe4-b687-f4aee07c88c2
```

The output of the command should be similar to the following:

```shell script
HTTP/1.1 200 OK
Content-Length: 57
Content-Type: application/json
Date: Sun, 08 Sep 2019 20:47:55 GMT
Server: Javalin

{
    "balance": 0,
    "id": "09bba65c-763d-4fe4-b687-f4aee07c88c2"
}
```

Newly created accounts always have a `0` balance.

### Top-up account balance

Request: `POST /api/v1/accounts/:account_id/credit-transactions`

Request body: JSON document with the following properties:
- `id` - id of the credit transaction. It is used for idempotency. It should be a **new random UUID** every time.
- `amount` - amount to credit in **cents**.

Example:

Account balance can be topped-up by creating a credit transaction to it:

```shell script
http --verbose POST :7000/api/v1/accounts/09bba65c-763d-4fe4-b687-f4aee07c88c2/credit-transactions id=e49cec55-90de-4076-8f8e-98a6a43ad0a5 amount:=20000

{
    "amount": 20000,
    "id": "e49cec55-90de-4076-8f8e-98a6a43ad0a5"
}
```

The output of the command should be similar to the following:

```shell script
HTTP/1.1 200 OK
Content-Length: 0
Content-Type: application/json
Date: Sun, 08 Sep 2019 20:53:50 GMT
Server: Javalin
```

### Transfer money between accounts

Request: `POST /api/v1/transfers`

Request body: JSON document with the following properties:
- `id` - id of the transfer. It is used for idempotency. It should be a **new random UUID** every time.
- `source_account_id` - id of the source account. It will be debited.
- `target_account_id` - id of the target account. It will be credited.
- `amount` - amount to transfer in **cents**.

Example:

To transfer money between two previously created accounts the following command can be used:

```shell script
http --verbose POST :7000/api/v1/transfers id=aa8050b2-8fb2-4854-ba98-61278fb5e95a source_account_id=09bba65c-763d-4fe4-b687-f4aee07c88c2 target_account_id=b838bf5a-7277-4874-baae-a87dd4868a0b amount:=10000

{
    "amount": 10000,
    "id": "aa8050b2-8fb2-4854-ba98-61278fb5e95a",
    "source_account_id": "09bba65c-763d-4fe4-b687-f4aee07c88c2",
    "target_account_id": "b838bf5a-7277-4874-baae-a87dd4868a0b"
}
```

The output of the command should be similar to the following:

```shell script
HTTP/1.1 200 OK
Content-Length: 0
Content-Type: application/json
Date: Sun, 08 Sep 2019 21:04:21 GMT
Server: Javalin
```

After a successful transfer, new balances can be observed by querying the account information endpoint:

```shell script
http --verbose GET :7000/api/v1/accounts/09bba65c-763d-4fe4-b687-f4aee07c88c2
``` 

The output shows a new balance of a source account:

```shell script
HTTP/1.1 200 OK
Content-Length: 61
Content-Type: application/json
Date: Sun, 08 Sep 2019 21:13:32 GMT
Server: Javalin

{
    "balance": 10000,
    "id": "09bba65c-763d-4fe4-b687-f4aee07c88c2"
}
```
