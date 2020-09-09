package io.vertx.codeone.conduit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.vertx.codeone.conduit.models.PersistenceVerticle;
import io.vertx.codeone.conduit.models.User;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@DisplayName("Spike Test")
@ExtendWith(VertxExtension.class)
public class EventBusJunit5Test {
    /**
     * {
     *     "user" :
     *     {
     *      "email" : "jake@jake.jake,
     *      "token" : null
     *      "username": "Jacob",
     *      "bio" : null,
     *      "image" " null
     *     }
     * }
     * @param testContext
     */
    @Test
    @DisplayName("Server Started Test")
    public void testServerStart(Vertx vertx, VertxTestContext testContext) {
        System.setProperty("log4j.configurationFile","log4j2-test.xml");
        Logger logger = LogManager.getLogger();
        
        WebClient webClient = WebClient.create(vertx);

        Checkpoint persistenceCheckpoint = testContext.checkpoint();
        Checkpoint deploymentCheckpoint = testContext.checkpoint();
        Checkpoint requestCheckpoint = testContext.checkpoint();

        vertx.deployVerticle(new PersistenceVerticle(), testContext.succeeding(id -> {
            persistenceCheckpoint.flag();
            testContext.completeNow();
        }));
        try{
            testContext.awaitCompletion(2000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
        }

        JsonObject userToRegister = new JsonObject()
            .put("email", "jake@jake.jake")
            .put("username", "Jacob")
            .put("password", "jakejake");

        JsonObject message = new JsonObject()
            .put("action", "register-user")
            .put("user", userToRegister);

        vertx.eventBus().request("persistence-address", message, ar -> {
            assertNotNull(ar.result().body());
            
            User returnedUser = null;
            try {
                returnedUser = Json.decodeValue(ar.result().body().toString(), User.class);
            } catch (Exception e) {
            }
            assertEquals("jake@jake.jake", returnedUser.getEmail());
            assertEquals("Jacob", returnedUser.getUsername());
        });
    };
}