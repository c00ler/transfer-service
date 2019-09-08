package com.revolut.transfer.transaction.controller;

import com.revolut.transfer.AbstractIT;
import com.revolut.transfer.account.model.Account;
import com.revolut.transfer.account.repository.AccountRepository;
import com.revolut.transfer.transaction.repository.TransactionRepository;
import io.restassured.http.ContentType;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.hamcrest.Matchers.equalTo;

/**
 * Tests for POST /accounts/:id/credit-transactions
 */
class CreateCreditTransactionHandlerIT extends AbstractIT {

    private final AccountRepository accountRepository = new AccountRepository(JOOQ);

    private final TransactionRepository transactionRepository = new TransactionRepository(JOOQ);

    // By default a new instance of a class is created for each test, so each test gets a new accountId
    private final UUID accountId = UUID.randomUUID();

    @Test
    void shouldCreateCreditTransaction() {
        accountRepository.persist(Account.of(accountId));

        given().contentType(ContentType.JSON)
                .body(String.format("{\"amount\": 10000, \"id\": \"%s\"}", UUID.randomUUID()))
                .post("/accounts/{id}/credit-transactions", accountId)
                .then()
                .log().all()
                .statusCode(HttpStatus.OK_200);

        assertThat(transactionRepository.getBalance(accountId)).isEqualTo(100_00L);
    }

    @Nested
    class Validation {

        @Test
        void shouldReturn400IfIdIsMissing() {
            given().contentType(ContentType.JSON)
                    .body("{\"amount\": 10000}")
                    .post("/accounts/{id}/credit-transactions", accountId)
                    .then()
                    .log().all()
                    .statusCode(HttpStatus.BAD_REQUEST_400)
                    .body("status", equalTo(400))
                    .body("title", containsStringIgnoringCase("id must not be null"));
        }

        @Test
        void shouldReturn400IfIdIsNotUUID() {
            given().contentType(ContentType.JSON)
                    .body("{\"amount\": 10000, \"id\": \"foobar\"}")
                    .post("/accounts/{id}/credit-transactions", accountId)
                    .then()
                    .log().all()
                    .statusCode(HttpStatus.BAD_REQUEST_400)
                    .body("status", equalTo(400))
                    .body("title", containsStringIgnoringCase("couldn't deserialize body"));
        }

        @Test
        void shouldReturn400IfAmountIsMissing() {
            given().contentType(ContentType.JSON)
                    .body(String.format("{\"id\": \"%s\"}", UUID.randomUUID()))
                    .post("/accounts/{id}/credit-transactions", accountId)
                    .then()
                    .log().all()
                    .statusCode(HttpStatus.BAD_REQUEST_400)
                    .body("status", equalTo(400))
                    .body("title", containsStringIgnoringCase("amount must be positive"));
        }

        @Test
        void shouldReturn400IfAmountIsNegative() {
            given().contentType(ContentType.JSON)
                    .body(String.format("{\"amount\": -10000, \"id\": \"%s\"}", UUID.randomUUID()))
                    .post("/accounts/{id}/credit-transactions", accountId)
                    .then()
                    .log().all()
                    .statusCode(HttpStatus.BAD_REQUEST_400)
                    .body("status", equalTo(400))
                    .body("title", containsStringIgnoringCase("amount must be positive"));
        }

        @Test
        void shouldReturn404IfAccountNotFound() {
            given().contentType(ContentType.JSON)
                    .body(String.format("{\"amount\": 10000, \"id\": \"%s\"}", UUID.randomUUID()))
                    .post("/accounts/{id}/credit-transactions", accountId)
                    .then()
                    .log().all()
                    .statusCode(HttpStatus.NOT_FOUND_404)
                    .body("status", equalTo(404))
                    .body("title", containsStringIgnoringCase("Not Found"));
        }
    }
}
