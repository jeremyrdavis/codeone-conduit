package io.vertx.codeone.conduit;

import io.vertx.codeone.conduit.models.User;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.jwt.JWTOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.JWTAuthHandler;
import io.vertx.ext.web.handler.StaticHandler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HttpVerticle extends AbstractVerticle {
    private static final Logger logger = LogManager.getLogger(HttpVerticle.class.getName());
   
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String MEDIA_JSON = "application/json; charset=utf-8";
    private static final String MEDIA_TEXT = "text/plain";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_TYPE = "type";
    private static final String KEY_PATH = "path";

    private JWTAuth jwtAuth;

    @Override
    public void start(Promise<Void> startFuture) {

    // Configure authentication with JWT
    JsonObject authConfig = new JsonObject().put("keyStore", new JsonObject()
        .put(KEY_TYPE, "jceks")
        .put(KEY_PATH, "resources/keystore.jceks")
        .put(KEY_PASSWORD, "secret"));

    jwtAuth = JWTAuth.create(vertx, new JWTAuthOptions(authConfig));

    Router baseRouter = Router.router(vertx);
    Router apiRouter = Router.router(vertx);

    baseRouter.route("/").handler(routingContext -> {
        logIncoming(routingContext);
        HttpServerResponse response = routingContext.response();
        response.putHeader(CONTENT_TYPE, MEDIA_TEXT).end("Hello Vert.x!");
    });

    apiRouter.route("/user*").handler(BodyHandler.create());
    apiRouter.post("/users").handler(this::registerUser);

    // secure endpoint
    apiRouter.get("/user-secure").handler(JWTAuthHandler.create(jwtAuth)).handler(this::getCurrentUser);

    apiRouter.get("/user").handler(this::getCurrentUser);

    baseRouter.mountSubRouter("/api", apiRouter);

    // Serve the non private static pages under webroot
    baseRouter.route("/static/*").handler(StaticHandler.create());

    baseRouter.route("/*").handler(routingContext -> {
        logIncoming(routingContext);
        HttpServerResponse response = routingContext.response();
        response.putHeader(CONTENT_TYPE, MEDIA_TEXT).end("Goodbye Vert.x!");
    });

    vertx.createHttpServer()
        .requestHandler(baseRouter)
        .listen(8080, result -> {
            if (result.succeeded()) {
                startFuture.complete();
            } else {
                startFuture.fail(result.cause());
            }
        });
    }

    private void getCurrentUser(RoutingContext routingContext) {
        User jacob = new User("Jacob", "jake@jake.jake", null, KEY_PASSWORD, null, "jwt.token.here");

        routingContext.response()
            .setStatusCode(201)
            .putHeader(CONTENT_TYPE, MEDIA_JSON)
            //.putHeader("Content-Length", String.valueOf(userResult.toString().length()))
            .end(Json.encodePrettily(jacob.toConduitJson()));
    }
   
    private void registerUser(RoutingContext routingContext) {
        logIncoming(routingContext);
        JsonObject message = new JsonObject().put("action", "register-user").put("user",
                routingContext.getBodyAsJson());

        vertx.eventBus().request("persistence-address", message, ar -> {

            if (ar.succeeded()) {
                String token = "token-not-set";

                User returnedUser = null;
                try {
                    returnedUser = Json.decodeValue(ar.result().body().toString(), User.class);
                } catch (Exception e) {
                    logger.info(e);
                }

                try {
                    //keytool -genseckey -keystore keystore.jceks -storetype jceks -storepass secret -keyalg HMacSHA512 -keysize 2048 -alias HS512 -keypass secret

                    token = jwtAuth.generateToken(
                            new JsonObject().put("email", returnedUser.getEmail()).put(KEY_PASSWORD,
                                    returnedUser.getPassword()),
                            new JWTOptions().setIgnoreExpiration(true).setAlgorithm("HS512"));
                } catch (RuntimeException re) {
                    logger.info(re);
                }

                logger.info("token: {}", token);

                returnedUser.setToken(token);

                routingContext.response().setStatusCode(201).putHeader(CONTENT_TYPE, MEDIA_JSON)
                        //.putHeader("Content-Length", String.valueOf(userResult.toString().length()))
                        .end(Json.encodePrettily(returnedUser.toConduitJson()));
            } else {
                routingContext.response().setStatusCode(500).putHeader(CONTENT_TYPE, MEDIA_JSON)
                        //.putHeader("Content-Length", String.valueOf(userResult.toString().length()))
                        .end(Json.encodePrettily(ar.cause().getMessage()));

            }
        });

    }
    private void logIncoming(RoutingContext routingContext) {
        HttpServerRequest request = routingContext.request();
        SocketAddress remoteAddress = request.remoteAddress();
        String remoteHost = remoteAddress.host();
        
        logger.info("{} requesting {}", remoteHost, routingContext.request().absoluteURI().toString());
    }
}
