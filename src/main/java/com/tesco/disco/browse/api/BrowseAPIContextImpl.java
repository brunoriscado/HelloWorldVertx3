package com.tesco.disco.browse.api;

import com.tesco.disco.browse.service.BrowseService;
import io.vertx.rxjava.core.Vertx;
import io.vertx.serviceproxy.ProxyHelper;

/**
 * Created by bruno on 21/04/16.
 */
public class BrowseAPIContextImpl implements BrowseAPIContext {
    private Vertx vertx;

    private BrowseService browseService;

    public BrowseAPIContextImpl(Vertx vertx) {
        this.vertx = vertx;
        init();
    }

    public void init() {
        browseService = ProxyHelper.createProxy(BrowseService.class, ((io.vertx.core.Vertx)vertx.getDelegate()), BrowseService.class.getName());
    }

    public BrowseService getBrowseService() {
        return browseService;
    }

    public void setBrowseService(BrowseService browseService) {
        this.browseService = browseService;
    }
}
