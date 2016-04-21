package com.tesco.disco.browse.service.vertx;

import com.tesco.disco.browse.service.BrowseService;
import com.tesco.disco.browse.service.impl.BrowseServiceImpl;
import io.vertx.rxjava.core.Vertx;

/**
 * Created by bruno on 21/04/16.
 */
public class BrowseServiceContextImpl implements BrowseServiceContext {
    public Vertx vertx;

    BrowseService browseService;

    public BrowseServiceContextImpl(Vertx vertx) {
        vertx = vertx;
    }

    public void init() {
        browseService = new BrowseServiceImpl(vertx);
    }

    public BrowseService getBrowseService() {
        return browseService;
    }

    public void setBrowseService(BrowseService browseService) {
        this.browseService = browseService;
    }
}
