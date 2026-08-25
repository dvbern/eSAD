/*
 * Copyright (C) 2026 DV Bern AG, Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 */

package ch.dvbern.oss.hemed.esc.api.shared.auth;

import java.net.HttpURLConnection;

import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsHelloDataBE;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;

/**
 * Covers the bidirectional client isolation enforced by {@link ClientFilter}: each machine
 * client (here HelloData) may only reach its own resource paths and is refused everywhere else,
 * including on public endpoints. Anonymous requests must still reach public endpoints.
 * <p>
 * The filter resolves the deferred {@link io.quarkus.security.identity.SecurityIdentity}, so these
 * cases pass with both proactive and lazy Quarkus authentication.
 */
@QuarkusTest
@DisplayNameGeneration(ReplaceUnderscores.class)
class ClientFilterAuthorizationITest extends AbstractITest {

    private static final String PUB_CONFIG_PATH = "/pub/client-config/config";
    private static final String HELLODATA_SCHULEN_PATH = "/hellodata/v1/ndjson/schulen";

    @Test
    @TestAsHelloDataBE
    void helloData_client_is_refused_on_public_endpoint() {
        RestAssured.given()
            .when().get(PUB_CONFIG_PATH)
            .then().statusCode(HttpURLConnection.HTTP_FORBIDDEN);
    }

    @Test
    @TestAsHelloDataBE
    void helloData_client_is_allowed_on_its_own_endpoint() {
        RestAssured.given()
            .when().get(HELLODATA_SCHULEN_PATH)
            .then().statusCode(HttpURLConnection.HTTP_OK);
    }

    @Test
    @TestAsAdminBS
    void nonMachine_user_is_allowed_on_public_endpoint() {
        // The filter must only restrict machine clients - a human user (no machine azp) reaches the
        // public endpoint normally. (The anonymous case is equivalent and verified live, but the
        // test harness has no tenant for anonymous requests on localhost.)
        RestAssured.given()
            .when().get(PUB_CONFIG_PATH)
            .then().statusCode(HttpURLConnection.HTTP_OK);
    }
}
