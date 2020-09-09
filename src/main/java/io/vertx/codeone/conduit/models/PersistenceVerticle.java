package io.vertx.codeone.conduit.models;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.mongo.MongoAuth;
import io.vertx.ext.mongo.MongoClient;
public class PersistenceVerticle extends AbstractVerticle{
    private static final String EMAIL = "email";
    private static final String USER_OBJECT = "user";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    
    // for DB access
    private MongoClient mongoClient;

    // Authentication provider for logging in
    private MongoAuth loginAuthProvider;

    public void start(Promise<Void> startFuture) {
    // Configure the MongoClient inline.  This should be externalized into a config file
    mongoClient = MongoClient.createShared(
        vertx, 
        new JsonObject().put(
                "db_name", 
                config().getString("db_name", "conduit_dev")).put("connection_string", 
                config().getString("connection_string", "mongodb://localhost:27017")
        )
    );

    // Configure authentication with MongoDB
    loginAuthProvider = MongoAuth.create(mongoClient, new JsonObject());
    loginAuthProvider.setUsernameField(EMAIL);
    loginAuthProvider.setUsernameCredentialField(EMAIL);

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
        "username": "Jacob",
        "bio": "I work at statefarm",
        "image": null
    }
    *
    * @param message
    */
    private void registerUser(Message<JsonObject> message) {

        JsonObject userToRegister = message.body().getJsonObject(USER_OBJECT);

        loginAuthProvider.insertUser(userToRegister.getString(EMAIL), userToRegister.getString(PASSWORD), null, null, ar -> {
            if (ar.succeeded()) {
            String id = ar.result();

            JsonObject query = new JsonObject().put("_id", id);
            JsonObject update = new JsonObject()
                .put("$set", new JsonObject().put(USERNAME, userToRegister.getString(USERNAME)));

            mongoClient.updateCollection(USER_OBJECT, query, update, res -> {
                if (res.succeeded()) {
                    message.reply(Json.encode(userToRegister));
                }else{
                    message.fail(2, "insert failed: " + res.cause().getMessage());
                }
            });

            } else{
                message.fail(2, "insert failed: " + ar.cause().getMessage());
            }
        });
    }
}
