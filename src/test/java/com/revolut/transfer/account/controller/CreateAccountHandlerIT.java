package com.revolut.transfer.account.controller;

import com.revolut.transfer.AbstractIT;
import com.revolut.transfer.Application;
import com.revolut.transfer.account.repository.AccountRepository;
import io.restassured.http.ContentType;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for POST /accounts endpoint.
 */
class CreateAccountHandlerIT extends AbstractIT {

    private final AccountRepository accountRepository = new AccountRepository(JOOQ);

    @Test
    void shouldCreateNewAccount() {
        var location =
                given().accept(ContentType.JSON)
                        .post("/accounts")
                        .then()
                        .log().all()
                        .statusCode(HttpStatus.CREATED_201)
                        .extract().header(HttpHeader.LOCATION.asString());

        assertThat(location).startsWith(Application.CONTEXT_PATH);

        var id = UUID.fromString(StringUtils.substringAfterLast(location, "/"));
        assertThat(accountRepository.findById(id)).isPresent();
    }
}
