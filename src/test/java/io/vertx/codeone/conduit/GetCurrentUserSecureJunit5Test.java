package io.vertx.codeone.conduit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.jwt.JWTOptions;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@DisplayName("Spike Test")
@ExtendWith(VertxExtension.class)
public class GetCurrentUserSecureJunit5Test {

    @Test
    @DisplayName("Server Started Test")
    public void testServerStart(Vertx vertx, VertxTestContext testContext) {
        System.setProperty("log4j.configurationFile","log4j2-test.xml");
        
        WebClient webClient = WebClient.create(vertx);

        Checkpoint deploymentCheckpoint = testContext.checkpoint();
        Checkpoint requestCheckpoint = testContext.checkpoint();

        JsonObject userJson = new JsonObject()
            .put("email", "jake@jake.jake")
            .put("username", "Jacob")
            .put("password", "jakejake");

        JsonObject user = new JsonObject()
            .put("user", userJson);

        JsonObject authConfig = new JsonObject().put("keyStore", new JsonObject()
            .put("type", "jceks")
            .put("path", "resources/keystore.jceks")
            .put("password", "secret"));

        JWTAuth jwtAuth = JWTAuth.create(vertx, new JWTAuthOptions(authConfig));
        String token = jwtAuth.generateToken(user, new JWTOptions().setAlgorithm("HS512").setIgnoreExpiration(true));
        assertNotNull(token);

        vertx.deployVerticle(new MainVerticle(), testContext.succeeding(id -> {
            deploymentCheckpoint.flag();

            webClient.get(8080,"localhost", "/api/user-secure")
                .putHeader("Content-Type", "application/json")
                .putHeader("X-Requested-With", "XMLHttpRequest")
                .putHeader("token", token)
                .send(testContext.succeeding(resp -> {
                    testContext.verify(() -> {
                    assertEquals(401, resp.statusCode());
                    requestCheckpoint.flag();
                    });
                }));
        }));   
    };
}