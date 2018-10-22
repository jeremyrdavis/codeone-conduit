package io.vertx.codeone.conduit;

import io.vertx.codeone.conduit.models.User;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class HttpVerticle extends AbstractVerticle{

  @Override
  public void start(Future<Void> startFuture) {

    Router baseRouter = Router.router(vertx);
    Router apiRouter = Router.router(vertx);

    baseRouter.route("/").handler(routingContext -> {
      HttpServerResponse response = routingContext.response();
      response.putHeader("content-type", "text/plain").end("Hello Vert.x!");
    });

    apiRouter.route("/user*").handler(BodyHandler.create());
    apiRouter.post("/users").handler(this::registerUser);

    baseRouter.mountSubRouter("/api", apiRouter);

    vertx.createHttpServer()
      .requestHandler(baseRouter::accept)
      .listen(8080, result -> {
        if (result.succeeded()) {
          startFuture.complete();
        } else {
          startFuture.fail(result.cause());
        }
      });
  }

  private void registerUser(RoutingContext routingContext) {

    User user = new User( "Jacob", "jake@jake.jake", null, "jakejake", null, "jwt.token.here");
    routingContext.response()
      .setStatusCode(201)
      .putHeader("Content-Type", "application/json; charset=utf-8")
      //.putHeader("Content-Length", String.valueOf(userResult.toString().length()))
      .end(Json.encodePrettily(user.toConduitJson()));

  }
}
