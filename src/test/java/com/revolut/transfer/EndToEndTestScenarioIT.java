package com.revolut.transfer;

import io.restassured.http.ContentType;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

class EndToEndTestScenarioIT extends AbstractIT {

    @Test
    void createAccountsAndMoveMoney() {
        // Create source account
        var sourceAccountLocation =
                given().accept(ContentType.JSON)
                        .post("/accounts")
                        .then()
                        .log().all()
                        .statusCode(HttpStatus.CREATED_201)
                        .extract().header(HttpHeader.LOCATION.asString());

        var sourceAccountId = UUID.fromString(StringUtils.substringAfterLast(sourceAccountLocation, "/"));

        // Create target account
        var targetAccountLocation =
                given().accept(ContentType.JSON)
                        .post("/accounts")
                        .then()
                        .log().all()
                        .statusCode(HttpStatus.CREATED_201)
                        .extract().header(HttpHeader.LOCATION.asString());

        var targetAccountId = UUID.fromString(StringUtils.substringAfterLast(targetAccountLocation, "/"));

        // Credit source account
        given().contentType(ContentType.JSON)
                .body(String.format("{\"amount\": 20000, \"id\": \"%s\"}", UUID.randomUUID()))
                .post("/accounts/{id}/credit-transactions", sourceAccountId)
                .then()
                .log().all()
                .statusCode(HttpStatus.OK_200);

        var requestBody = String.format(
                "{\"id\": \"%s\", \"source_account_id\": \"%s\", \"target_account_id\": \"%s\", \"amount\": 10000}",
                UUID.randomUUID(), sourceAccountId, targetAccountId);

        // Move half of the total amount to the target account
        given().contentType(ContentType.JSON)
                .body(requestBody)
                .post("/transfers")
                .then()
                .log().all()
                .statusCode(HttpStatus.OK_200);

        // Check source account balance
        given().accept(ContentType.JSON)
                .get("accounts/{id}", sourceAccountId)
                .then()
                .log().all()
                .statusCode(HttpStatus.OK_200)
                .body("balance", equalTo(100_00));

        // Check target account balance
        given().accept(ContentType.JSON)
                .get("accounts/{id}", targetAccountId)
                .then()
                .log().all()
                .statusCode(HttpStatus.OK_200)
                .body("balance", equalTo(100_00));
    }
}
