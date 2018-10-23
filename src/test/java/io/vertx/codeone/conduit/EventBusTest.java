package io.vertx.codeone.conduit;

import io.vertx.codeone.conduit.models.PersistenceVerticle;
import io.vertx.codeone.conduit.models.User;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class EventBusTest {

  private Vertx vertx;

  @Before
  public void setUp(TestContext testContext) {

    vertx = Vertx.vertx();
    vertx.deployVerticle(PersistenceVerticle.class.getName(), testContext.asyncAssertSuccess());

  }

  @After
  public void tearDown(TestContext testContext) {
    vertx.close();
  }

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
  public void testRegisterUserMessage(TestContext testContext) {

    Async async = testContext.async();

    JsonObject userToRegister = new JsonObject()
      .put("email", "jake@jake.jake")
      .put("username", "Jacob")
      .put("password", "jakejake");

    JsonObject message = new JsonObject()
      .put("action", "register-user")
      .put("user", userToRegister);

    vertx.<JsonObject>eventBus().send("persistence-address", message, ar -> {
      if (ar.succeeded()) {
        testContext.assertNotNull(ar.result().body());
        User returnedUser = Json.decodeValue(ar.result().body().toString(), User.class);
        testContext.assertEquals("jake@jake.jake", returnedUser.getEmail());
        testContext.assertEquals("Jacob", returnedUser.getUsername());
        async.complete();
      }else{
        testContext.assertTrue(ar.succeeded());
        async.complete();
      }
    });
  }


}
