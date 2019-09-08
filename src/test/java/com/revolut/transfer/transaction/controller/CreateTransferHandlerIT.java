package com.revolut.transfer.transaction.controller;

import com.revolut.transfer.AbstractIT;
import com.revolut.transfer.account.model.Account;
import com.revolut.transfer.account.repository.AccountRepository;
import com.revolut.transfer.transaction.model.Transaction;
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
 * Tests for POST /transfers
 */
class CreateTransferHandlerIT extends AbstractIT {

    private final AccountRepository accountRepository = new AccountRepository(JOOQ);

    private final TransactionRepository transactionRepository = new TransactionRepository(JOOQ);

    private final UUID sourceAccountId = UUID.randomUUID();

    private final UUID targetAccountId = UUID.randomUUID();

    @Test
    void shouldTransferMoneyBetweenAccounts() {
        accountRepository.persist(Account.of(sourceAccountId));
        transactionRepository.createCreditTransaction(
                new Transaction.Credit(UUID.randomUUID(), sourceAccountId, 300_00L));

        accountRepository.persist(Account.of(targetAccountId));
        transactionRepository.createCreditTransaction(
                new Transaction.Credit(UUID.randomUUID(), targetAccountId, 200_00L));

        var requestBody = String.format(
                "{\"id\": \"%s\", \"source_account_id\": \"%s\", \"target_account_id\": \"%s\", \"amount\": 10000}",
                UUID.randomUUID(), sourceAccountId, targetAccountId);

        given().contentType(ContentType.JSON)
                .body(requestBody)
                .post("/transfers")
                .then()
                .log().all()
                .statusCode(HttpStatus.OK_200);

        assertThat(transactionRepository.getBalance(sourceAccountId)).isEqualTo(200_00L);
        assertThat(transactionRepository.getBalance(targetAccountId)).isEqualTo(300_00L);
    }

    @Nested
    class Validation {

        @Test
        void shouldReturn400IfIdIsMissing() {
            var requestBody = String.format(
                    "{\"source_account_id\": \"%s\", \"target_account_id\": \"%s\", \"amount\": 100}",
                    sourceAccountId, targetAccountId);

            given().contentType(ContentType.JSON)
                    .body(requestBody)
                    .post("/transfers")
                    .then()
                    .log().all()
                    .statusCode(HttpStatus.BAD_REQUEST_400)
                    .body("status", equalTo(400))
                    .body("title", containsStringIgnoringCase("id must not be null"));
        }

        @Test
        void shouldReturn400IfAmountIsMissing() {
            var requestBody = String.format(
                    "{\"id\": \"%s\", \"source_account_id\": \"%s\", \"target_account_id\": \"%s\"}",
                    UUID.randomUUID(), sourceAccountId, targetAccountId);

            given().contentType(ContentType.JSON)
                    .body(requestBody)
                    .post("/transfers")
                    .then()
                    .log().all()
                    .statusCode(HttpStatus.BAD_REQUEST_400)
                    .body("status", equalTo(400))
                    .body("title", containsStringIgnoringCase("amount must be positive"));
        }

        @Test
        void shouldReturn400IfAmountIsNegative() {
            var requestBody = String.format(
                    "{\"id\": \"%s\", \"source_account_id\": \"%s\", \"target_account_id\": \"%s\", \"amount\": -100}",
                    UUID.randomUUID(), sourceAccountId, targetAccountId);

            given().contentType(ContentType.JSON)
                    .body(requestBody)
                    .post("/transfers")
                    .then()
                    .log().all()
                    .statusCode(HttpStatus.BAD_REQUEST_400)
                    .body("status", equalTo(400))
                    .body("title", containsStringIgnoringCase("amount must be positive"));
        }

        @Test
        void shouldReturn404IfSourceAccountNotFound() {
            accountRepository.persist(Account.of(targetAccountId));

            var requestBody = String.format(
                    "{\"id\": \"%s\", \"source_account_id\": \"%s\", \"target_account_id\": \"%s\", \"amount\": 100}",
                    UUID.randomUUID(), sourceAccountId, targetAccountId);

            given().contentType(ContentType.JSON)
                    .body(requestBody)
                    .post("/transfers")
                    .then()
                    .log().all()
                    .statusCode(HttpStatus.NOT_FOUND_404)
                    .body("status", equalTo(404))
                    .body("title", containsStringIgnoringCase("Not Found"));
        }

        @Test
        void shouldReturn404IfTargetAccountNotFound() {
            accountRepository.persist(Account.of(sourceAccountId));

            var requestBody = String.format(
                    "{\"id\": \"%s\", \"source_account_id\": \"%s\", \"target_account_id\": \"%s\", \"amount\": 100}",
                    UUID.randomUUID(), sourceAccountId, targetAccountId);

            given().contentType(ContentType.JSON)
                    .body(requestBody)
                    .post("/transfers")
                    .then()
                    .log().all()
                    .statusCode(HttpStatus.NOT_FOUND_404)
                    .body("status", equalTo(404))
                    .body("title", containsStringIgnoringCase("Not Found"));
        }

        @Test
        void shouldReturn400IfInsufficientFunds() {
            accountRepository.persist(Account.of(sourceAccountId));
            accountRepository.persist(Account.of(targetAccountId));

            transactionRepository.createCreditTransaction(
                    new Transaction.Credit(UUID.randomUUID(), sourceAccountId, 100_00L));

            var requestBody = String.format(
                    "{\"id\": \"%s\", \"source_account_id\": \"%s\", \"target_account_id\": \"%s\", \"amount\": 20000}",
                    UUID.randomUUID(), sourceAccountId, targetAccountId);

            given().contentType(ContentType.JSON)
                    .body(requestBody)
                    .post("/transfers")
                    .then()
                    .log().all()
                    .statusCode(HttpStatus.BAD_REQUEST_400)
                    .body("status", equalTo(400))
                    .body("type", equalTo("https://revolut.com/insufficient-funds"))
                    .body("title", equalTo("Insufficient Funds"));
        }
    }
}
