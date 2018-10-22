package io.vertx.codeone.conduit;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Handler;

public class MainVerticle extends AbstractVerticle {

  @Override
  public void start(Future<Void> startFuture) {

    deployVerticle(HttpVerticle.class.getName()).setHandler(ar -> {
      if (ar.succeeded()) {
        startFuture.complete();
      }else{
        startFuture.fail(ar.cause());
      }
    });
  }

  Future<Void> deployVerticle(String verticleName) {
    Future<Void> retVal = Future.future();
    vertx.deployVerticle(verticleName, event -> {
      if (event.succeeded()) {
        retVal.complete();
      }else{
        retVal.fail(event.cause());
      }
    });
    return retVal;
  }

}
