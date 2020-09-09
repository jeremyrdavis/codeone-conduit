package io.vertx.codeone.conduit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@DisplayName("GetCurrentUserTest")
@ExtendWith(VertxExtension.class)
public class GetCurrentUserJunit5Test {
    @Test
    @DisplayName("Get Current User")
    public void testServerStart(Vertx vertx, VertxTestContext testContext) {
        System.setProperty("log4j.configurationFile","log4j2-test.xml");
        
        WebClient webClient = WebClient.create(vertx);

        Checkpoint deploymentCheckpoint = testContext.checkpoint();
        Checkpoint requestCheckpoint = testContext.checkpoint();

        JsonObject userJson = new JsonObject()
        .put("email", "jake@jake.jake")
        .put("username", "Jacob")
        .put("password", "jakejake");

        JsonObject user = new JsonObject().put("user", userJson);

        vertx.deployVerticle(new MainVerticle(), testContext.succeeding(id -> {
            deploymentCheckpoint.flag();

            webClient.get(8080,"localhost", "/api/user")
                .putHeader("Content-Type", "application/json")
                .putHeader("X-Requested-With", "XMLHttpRequest")
                .send(testContext.succeeding(resp -> {
                    testContext.verify(() -> {
                        assertEquals(201, resp.statusCode());
                        JsonObject returnedJson = resp.bodyAsJsonObject();
                        JsonObject returnedUser = returnedJson.getJsonObject("user");
                        assertEquals("Jacob", returnedUser.getString("username"));
                        assertEquals("jake@jake.jake", returnedUser.getString("email"));
                        assertNotNull(returnedUser.getString("token"));
                        requestCheckpoint.flag();
                    });
                }));
        }));
    };
}