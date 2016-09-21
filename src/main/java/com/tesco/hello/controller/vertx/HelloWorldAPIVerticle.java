package com.tesco.hello.controller.vertx;


import com.tesco.hello.controller.context.HelloWorldAPIContext;
import com.tesco.hello.utils.ContextDelegator;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.http.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Created by bruno on 20/04/16.
 */
public class HelloWorldAPIVerticle extends io.vertx.rxjava.core.AbstractVerticle {
    private static final Logger LOGGER = LoggerFactory.getLogger(HelloWorldAPIVerticle.class);
    private static final Marker MARKER = MarkerFactory.getMarker("VERTICLE");
    private HttpServer httpServer;

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        LOGGER.info(MARKER, "Starting API verticle");
        JsonObject config = vertx.getOrCreateContext().config();
        HelloWorldAPIContext context = ContextDelegator.getInstance().getContext(vertx, config);
        vertx.createHttpServer(new HttpServerOptions()
                .setHost(config.getString("host"))
                .setPort(config.getInteger("port")))
                .requestHandler(context.getRouter()::accept)
                .listenObservable()
                .subscribe(server -> {
                        httpServer = server;
                    },
                    error -> {
                        LOGGER.error(MARKER, "Server unable to start: {}", error.getMessage());
                        startFuture.fail("Server unable to start");
                    },
                    () -> {
                        LOGGER.info(MARKER, "HelloWorld API started at: {}",
                                ZonedDateTime.now().format(DateTimeFormatter.BASIC_ISO_DATE.ISO_INSTANT));
                        startFuture.complete();
                    });
    }

    @Override
    public void stop() {
        httpServer.close();
    }
}
