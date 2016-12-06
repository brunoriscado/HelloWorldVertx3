package com.tesco.hello.pingpong.vertx;

import io.vertx.core.Future;
import io.vertx.rxjava.core.AbstractVerticle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by bruno on 06/12/16.
 */
public class PingVerticle extends AbstractVerticle {
    private static final Logger LOGGER = LoggerFactory.getLogger(PingVerticle.class);

    public void start(Future<Void> startFuture) throws Exception {
        LOGGER.info("Starting ping verticle");
        vertx.setPeriodic(2000, (result) -> {
            LOGGER.info("Ping is sending an event");
            vertx.eventBus().send(PongVerticle.class.getName(), "ping", (reply) -> {
                if (reply.succeeded()) {
                    LOGGER.info("Pong replied");
                } else {
                    LOGGER.info("Pong did not failed to reply or replied with error");
                }
            });
        });
    }
}