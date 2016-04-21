package com.tesco.disco.browse.service.context;

import com.tesco.disco.browse.service.BrowseService;
import com.tesco.disco.browse.service.elasticsearch.ElasticSearchManager;
import com.tesco.disco.browse.service.impl.BrowseServiceImpl;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.Vertx;
import com.tesco.search.commons.context.Context;

/**
 * Created by bruno on 21/04/16.
 */
public class BrowseServiceContextImpl implements BrowseServiceContext {
    private Vertx vertx;
    private JsonObject config;
    private BrowseService browseService;
    private ElasticSearchManager elasticSearchManager;

    @Context
    public BrowseServiceContextImpl(Vertx vertx, JsonObject config) {
        this.vertx = vertx;
        this.config = config;
        init();
    }

    public void init() {
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
