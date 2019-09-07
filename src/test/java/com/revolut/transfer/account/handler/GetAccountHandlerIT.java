package com.revolut.transfer.account.handler;

import com.revolut.transfer.AbstractIT;
import com.revolut.transfer.account.model.Account;
import com.revolut.transfer.account.repository.AccountRepository;
import io.restassured.http.ContentType;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * Tests for GET accounts/:id endpoint.
 */
class GetAccountHandlerIT extends AbstractIT {

    private final AccountRepository accountRepository = new AccountRepository(JOOQ);

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
    void shouldReturnAccountWithoutTransactions() {
        var id = UUID.randomUUID();
        assertThat(accountRepository.insert(Account.of(id))).isEqualTo(1);

        given().accept(ContentType.JSON)
                .get("accounts/{id}", id)
                .then()
                .log().all()
                .statusCode(HttpStatus.OK_200)
                .body("id", equalTo(id.toString()))
                .body("balance", equalTo(0));
    }
}
