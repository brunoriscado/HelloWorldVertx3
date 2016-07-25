package com.tesco.disco.browse.controller.context;

import com.tesco.disco.browse.controller.BrowseController;
import com.tesco.disco.browse.controller.impl.BrowseControllerImpl;
import com.tesco.disco.browse.service.BrowseService;
import com.tesco.disco.browse.utils.Context;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.ext.web.Router;
import io.vertx.serviceproxy.ProxyHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/**
 * Created by bruno on 21/04/16.
 */
@Context
public class BrowseAPIContextImpl implements BrowseAPIContext {
    private static final Logger LOGGER = LoggerFactory.getLogger(BrowseAPIContextImpl.class);
    private static final Marker MARKER = MarkerFactory.getMarker("CONTEXT");
    private Vertx vertx;
    private JsonObject config;
    private Router router;
    private com.tesco.disco.browse.service.rxjava.BrowseService browseService;
    private BrowseController browseController;

    public BrowseAPIContextImpl(Vertx vertx, JsonObject config) {
        this.vertx = vertx;
        this.config = config;
        init();
    }

    public void init() {
        LOGGER.info(MARKER, "Initializing API context container");
        router = Router.router(vertx);
        browseService = new com.tesco.disco.browse.service.rxjava.BrowseService(ProxyHelper.createProxy(BrowseService.class, ((io.vertx.core.Vertx)vertx.getDelegate()), BrowseService.class.getName()));
        browseController = new BrowseControllerImpl(vertx, router, browseService);
    }

    public com.tesco.disco.browse.service.rxjava.BrowseService getBrowseService() {
        return browseService;
    }

    public void setBrowseService(com.tesco.disco.browse.service.rxjava.BrowseService browseService) {
        this.browseService = browseService;
    }

    public BrowseController getBrowseController() {
        return browseController;
    }

    public void setBrowseController(BrowseController browseController) {
        this.browseController = browseController;
    }

    public Router getRouter() {
        return router;
    }

    public void setRouter(Router router) {
        this.router = router;
    }
}
