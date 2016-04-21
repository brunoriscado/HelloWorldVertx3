package com.tesco.disco.browse.controller.context;

import com.tesco.disco.browse.controller.BrowseController;
import com.tesco.disco.browse.controller.impl.BrowseControllerImpl;
import com.tesco.disco.browse.service.BrowseService;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.ext.web.Router;
import io.vertx.serviceproxy.ProxyHelper;
import com.tesco.search.commons.context.Context;

/**
 * Created by bruno on 21/04/16.
 */
@Context
public class BrowseAPIContextImpl implements BrowseAPIContext {
    private Vertx vertx;
    private JsonObject config;
    private Router router;
    private BrowseService browseService;
    private BrowseController browseController;

    public BrowseAPIContextImpl(Vertx vertx, JsonObject config) {
        this.vertx = vertx;
        this.config = config;
        router = Router.router(vertx);
        init();
    }

    public void init() {
        browseService = ProxyHelper.createProxy(BrowseService.class, ((io.vertx.core.Vertx)vertx.getDelegate()), BrowseService.class.getName());
        browseController = new BrowseControllerImpl(vertx, router, browseService);
    }

    public BrowseService getBrowseService() {
        return browseService;
    }

    public void setBrowseService(BrowseService browseService) {
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
