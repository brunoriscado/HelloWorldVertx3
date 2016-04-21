package com.tesco.disco.browse.service;

import com.tesco.disco.browse.service.elasticsearch.ElasticSearchManager;
import com.tesco.disco.browse.service.impl.BrowseServiceImpl;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.Vertx;

/**
 * Created by bruno on 21/04/16.
 */
public class BrowseServiceContextImpl implements BrowseServiceContext {
    private Vertx vertx;
    private BrowseService browseService;
    private ElasticSearchManager elasticSearchManager;

    public BrowseServiceContextImpl(Vertx vertx) {
        this.vertx = vertx;
        init();
    }

    public void init() {
        JsonObject config = vertx.getOrCreateContext().config();
        browseService = new BrowseServiceImpl(vertx,
                ElasticSearchManager.getINSTANCE(config.getJsonObject("elasticsearch")));
    }

    public BrowseService getBrowseService() {
        return browseService;
    }

    public void setBrowseService(BrowseService browseService) {
        this.browseService = browseService;
    }
}
