package io.vertx.codeone.conduit;

import io.vertx.core.DeploymentOptions;
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

import java.util.ArrayList;

@RunWith(VertxUnitRunner.class)
public class RegisterUserTest {

  protected Vertx vertx;

  protected WebClient webClient;

  @Before
  public void setUp(TestContext testContext) {

    vertx = Vertx.vertx();
    webClient = WebClient.create(vertx);

    vertx.deployVerticle(MainVerticle.class.getName(), testContext.asyncAssertSuccess());
  }

  @After
  public void tearDown(TestContext testContext) {
   vertx.close();
  }

  @Test
  public void testRegisterUser(TestContext testContext) {
    Async async = testContext.async();

    JsonObject userJson = new JsonObject()
      .put("email", "jake@jake.jake")
      .put("username", "Jacob")
      .put("password", "jakejake");

    JsonObject user = new JsonObject()
      .put("user", userJson);

    webClient.post(8080,"localhost", "/api/users")
      .putHeader("Content-Type", "application/json")
      .putHeader("X-Requested-With", "XMLHttpRequest")
      .sendJsonObject(user, ar -> {
        if (ar.succeeded()) {
          testContext.assertEquals(201, ar.result().statusCode());
          JsonObject returnedJson = ar.result().bodyAsJsonObject();
          JsonObject returnedUser = returnedJson.getJsonObject("user");
          testContext.assertEquals("Jacob", returnedUser.getString("username"));
          testContext.assertEquals("jake@jake.jake", returnedUser.getString("email"));
          testContext.assertNotNull(returnedUser.getString("token"));
          async.complete();
        }else{
          testContext.assertTrue(ar.succeeded());
          async.complete();
        }

    });
  }

}
