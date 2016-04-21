package com.tesco.disco.browse.controller.impl;

import com.tesco.disco.browse.controller.BrowseController;
import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.ext.web.Router;
import io.vertx.rxjava.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by bruno on 20/04/16.
 */
public class BrowseControllerImpl implements BrowseController {
    private static final Logger LOGGER = LoggerFactory.getLogger(BrowseControllerImpl.class.getName());
    Vertx vertx;

    public BrowseControllerImpl(Vertx vertx) {
        vertx = vertx;
    }

    public void init(Router router) {
        LOGGER.info("Initializing routing definitions for controller");
        Router subRouter = Router.router(vertx);

        router.get().handler(this::browseHandler);
    }

    private void browseHandler(RoutingContext context) {

    }

    private void browse() {

    }
}
