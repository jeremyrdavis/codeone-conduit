package io.vertx.codeone.conduit;

import java.util.Arrays;
import java.util.List;

import io.vertx.codeone.conduit.models.PersistenceVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MainVerticle extends AbstractVerticle {
    private static final Logger logger = LogManager.getLogger(MainVerticle.class.getName());
         
    public void start(Promise<Void> startFuture) {
    
        
        Future[] deployArray = new Future[] {
            deployVerticle(HttpVerticle.class.getName()).future(), 
            deployVerticle(PersistenceVerticle.class.getName()).future()
        };

        List<Future> allFutures = Arrays.asList(deployArray);
        CompositeFuture.all(allFutures).onComplete(ar -> {
        if (ar.succeeded()) {
            startFuture.complete();
        }else{
            startFuture.fail(ar.cause());
        }
        });
    }
    Promise<Void> deployVerticle(String verticleName) {
        logger.info("Launching {}", verticleName);

        Promise<Void> vertPromise = Promise.promise();

        vertx.deployVerticle(verticleName, result -> {
            if (result.succeeded()) {
                vertPromise.complete();
            } else {
                vertPromise.fail(result.cause());
            }
        });

        return vertPromise;
    }
    public static void main(String[] args) {
        // Vertx core
        Vertx vertx = Vertx.vertx();

        // Deploy Verticle
        vertx.deployVerticle(new MainVerticle(), res -> {
            if (!res.succeeded()) {
                logger.error("FATAL: Deploy Verticle failed!");
            }
        });
    }
}
