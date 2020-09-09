package io.vertx.codeone.conduit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@DisplayName("Spike Test")
@ExtendWith(VertxExtension.class)
public class MainVerticleJunit5Test {

	@Test
	@DisplayName("Server Started Test")
	public void testServerStart(Vertx vertx, VertxTestContext testContext) {
		System.setProperty("log4j.configurationFile","log4j2-test.xml");
        
		WebClient webClient = WebClient.create(vertx);

		Checkpoint deploymentCheckpoint = testContext.checkpoint();
		Checkpoint requestCheckpoint = testContext.checkpoint();

		vertx.deployVerticle(new MainVerticle(), testContext.succeeding(id -> {
			deploymentCheckpoint.flag();

			webClient.get(8080, "localhost", "/")
			.as(BodyCodec.string())
			.send(testContext.succeeding(resp -> {
				testContext.verify(() -> {
					assertEquals(200, resp.statusCode());
					assertEquals("Hello Vert.x!", resp.body());
					requestCheckpoint.flag();
				});
			}));
		}));
	}
}