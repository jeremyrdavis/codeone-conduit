package io.vertx.codeone.conduit;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.client.WebClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class GetCurrentUserTest {

  private Vertx vertx;

  private WebClient webClient;

  @Before
  public void setUp(TestContext tc) {
    vertx = Vertx.vertx();
    vertx.deployVerticle(MainVerticle.class.getName(), tc.asyncAssertSuccess());

    webClient = WebClient.create(vertx);
  }

  @After
  public void tearDown(TestContext testContext) {
    vertx.close(testContext.asyncAssertSuccess());
  }

  @Test
  public void testJWTAuthentication(TestContext testContext) {

  Async async = testContext.async();

  JsonObject userJson = new JsonObject()
    .put("email", "jake@jake.jake")
    .put("username", "Jacob")
    .put("password", "jakejake");

  JsonObject user = new JsonObject()
    .put("user", userJson);

    webClient.get(8080,"localhost", "/api/user")
      .putHeader("Content-Type", "application/json")
      .putHeader("X-Requested-With", "XMLHttpRequest")
      .sendJsonObject(user, ar -> {
        if (ar.succeeded()) {
          testContext.assertEquals(401, ar.result().statusCode());
          async.complete();
        }else{
          testContext.assertTrue(ar.succeeded());
          async.complete();
        }

    });
  }

}
