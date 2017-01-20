package com.tesco.hello.pingpong.vertx;

import io.vertx.core.Future;
import io.vertx.rxjava.core.AbstractVerticle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by bruno on 06/12/16.
 */
public class PongVerticle extends AbstractVerticle {
    private static final Logger LOGGER = LoggerFactory.getLogger(PongVerticle.class);

    public void start(Future<Void> startFuture) throws Exception {
        LOGGER.info("Starting pong verticle");
        vertx.eventBus().consumer(PongVerticle.class.getName(), (message) -> {
            LOGGER.info("Ping sent an event - Pong is going to reply");
            //TODO - call cache layer



            message.reply("pong");
        });
    }
}