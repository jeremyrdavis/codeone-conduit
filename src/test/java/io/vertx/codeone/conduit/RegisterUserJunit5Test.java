package io.vertx.codeone.conduit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.vertx.codeone.conduit.models.PersistenceVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@DisplayName("RegisterUserJunit5Test")
@ExtendWith(VertxExtension.class)
public class RegisterUserJunit5Test {

    @Test
    @DisplayName("Server Started Test")
    public void testServerStart(Vertx vertx, VertxTestContext testContext) {
        System.setProperty("log4j.configurationFile","log4j2-test.xml");
        
        WebClient webClient = WebClient.create(vertx);

        Checkpoint persistenceCheckpoint = testContext.checkpoint();
        Checkpoint deploymentCheckpoint = testContext.checkpoint();
        Checkpoint requestCheckpoint = testContext.checkpoint();

        JsonObject userJson = new JsonObject()
            .put("email", "jake@jake.jake")
            .put("username", "Jacob")
            .put("password", "jakejake");

        JsonObject user = new JsonObject()
            .put("user", userJson);

        vertx.deployVerticle(new PersistenceVerticle(), testContext.succeeding(id -> {
            testContext.completeNow();
            persistenceCheckpoint.flag();
        }));

        try{
            testContext.awaitCompletion(2000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
        }

        vertx.deployVerticle(new MainVerticle(), testContext.succeeding(id -> {
            deploymentCheckpoint.flag();

            webClient.post(8080,"localhost", "/api/users-secure")
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