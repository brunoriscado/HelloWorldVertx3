package com.tesco.disco.browse;


import io.vertx.core.AbstractVerticle;
import io.vertx.rxjava.ext.web.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by bruno on 20/04/16.
 */
public class BrowseVerticle extends io.vertx.rxjava.core.AbstractVerticle {
    private static final Logger LOGGER = LoggerFactory.getLogger(BrowseVerticle.class);
    private Router router;

    @Override
    public void start() throws Exception {
        router = Router.router(vertx);
        vertx.createHttpServer()
                //TODO - change port to external configuration
                .listen(9003)
                .requestHandler(request -> {
                    LOGGER.info("Start");
                });
    }
}
