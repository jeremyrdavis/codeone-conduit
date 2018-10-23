package io.vertx.codeone.conduit.models;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

public class PersistenceVerticle extends AbstractVerticle{

  // for DB access
  private MongoClient mongoClient;

  @Override
  public void start(Future<Void> startFuture) {
    // Configure the MongoClient inline.  This should be externalized into a config file
    mongoClient = MongoClient.createShared(vertx, new JsonObject().put("db_name", config().getString("db_name", "conduit")).put("connection_string", config().getString("connection_string", "mongodb://localhost:27017")));
    EventBus eventBus = vertx.eventBus();
    MessageConsumer<JsonObject> consumer = eventBus.consumer("persistence-address");

    consumer.handler(message -> {

      String action = message.body().getString("action");

      switch (action) {
        case "register-user":
          registerUser(message);
          break;
        default:
          message.fail(1, "Unkown action: " + message.body());
      }
    });

    startFuture.complete();

  }

  /**
   * Receive Json in the following format:
   *
    {
     "username": "Jacob",
     "email": "jake@jake.jake",
     "password": "jakejake"
     }
   *
   * and return Json in the following format:
   *
   {
       "email": "jake@jake.jake",
       "token": "jwt.token.here",
       "username": "jake",
       "bio": "I work at statefarm",
       "image": null
   }
   *
   * @param message
   */
  private void registerUser(Message<JsonObject> message) {

    message.reply(new JsonObject()
      .put("email", "jake@jake.jake")
      .put("token", "jwt.token.here")
      .put("username", "Jacob")
      .put("bio", "I work at statefarm")
      .put("image", (String) null));

  }
}
