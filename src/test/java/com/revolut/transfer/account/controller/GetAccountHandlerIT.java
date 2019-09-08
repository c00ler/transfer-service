package com.revolut.transfer.account.controller;

import com.revolut.transfer.AbstractIT;
import com.revolut.transfer.account.model.Account;
import com.revolut.transfer.account.repository.AccountRepository;
import com.revolut.transfer.transaction.model.Transaction;
import com.revolut.transfer.transaction.repository.TransactionRepository;
import io.restassured.http.ContentType;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.hamcrest.Matchers.equalTo;

/**
 * Tests for GET accounts/:id endpoint.
 */
class GetAccountHandlerIT extends AbstractIT {

    private final AccountRepository accountRepository = new AccountRepository(JOOQ);

    private final TransactionRepository transactionRepository = new TransactionRepository(JOOQ);

    @Test
    void shouldReturn404IfAccountNotFound() {
        given().accept(ContentType.JSON)
                .get("accounts/{id}", UUID.randomUUID())
                .then()
                .log().all()
                .statusCode(HttpStatus.NOT_FOUND_404)
                .body("status", equalTo(404))
                .body("title", equalTo("Not Found"));
    }

    @Test
    void shouldReturn400IfAccountIdIsNotUUID() {
        given().accept(ContentType.JSON)
                .get("accounts/{id}", "foobar")
                .then()
                .log().all()
                .statusCode(HttpStatus.BAD_REQUEST_400)
                .body("status", equalTo(400))
                .body("title", containsStringIgnoringCase("is not a valid uuid"));
    }

    @Test
    void shouldReturnAccountWithoutBalance() {
        var id = UUID.randomUUID();
        assertThat(accountRepository.persist(Account.of(id))).isEqualTo(1);

        given().accept(ContentType.JSON)
                .get("accounts/{id}", id)
                .then()
                .log().all()
                .statusCode(HttpStatus.OK_200)
                .body("id", equalTo(id.toString()))
                .body("balance", equalTo(0));
    }

    @Test
    void shouldReturnAccountWithBalance() {
        var accountId = UUID.randomUUID();
        assertThat(accountRepository.persist(Account.of(accountId))).isEqualTo(1);

        transactionRepository.createCreditTransaction(new Transaction.Credit(UUID.randomUUID(), accountId, 100_00L));
        transactionRepository.createCreditTransaction(new Transaction.Credit(UUID.randomUUID(), accountId, 200_00L));
        transactionRepository.createDebitTransaction(new Transaction.Debit(UUID.randomUUID(), accountId, -50_00L));

        given().accept(ContentType.JSON)
                .get("accounts/{id}", accountId)
                .then()
                .log().all()
                .statusCode(HttpStatus.OK_200)
                .body("id", equalTo(accountId.toString()))
                .body("balance", equalTo(250_00));
    }
}
