package com.tesco.disco.browse.api.vertx;


import com.tesco.disco.browse.api.BrowseAPIContext;
import com.tesco.disco.browse.api.BrowseAPIContextImpl;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.http.HttpServer;
import io.vertx.rxjava.ext.web.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Created by bruno on 20/04/16.
 */
public class BrowseAPIVerticle extends io.vertx.rxjava.core.AbstractVerticle {
    private static final Logger LOGGER = LoggerFactory.getLogger(BrowseAPIVerticle.class);
    private Router router;
    private HttpServer httpServer;

    @Override
    public void start() throws Exception {
        router = Router.router(vertx);
        JsonObject config = vertx.getOrCreateContext().config();
        BrowseAPIContext context = new BrowseAPIContextImpl(vertx);
        vertx.createHttpServer(new HttpServerOptions()
                .setHost(config.getString("host"))
                .setPort(config.getInteger("port")))
                .requestHandler(router::accept)
                .listenObservable()
                .subscribe(server -> {
                        httpServer = server;
                    },
                    error -> {
                        LOGGER.error("Server unable to start: {}", error.getMessage());
                    },
                    () -> {
                        LOGGER.info("Browsing API started at: {}",
                                ZonedDateTime.now().format(DateTimeFormatter.BASIC_ISO_DATE.ISO_INSTANT));
                    });
    }
}
