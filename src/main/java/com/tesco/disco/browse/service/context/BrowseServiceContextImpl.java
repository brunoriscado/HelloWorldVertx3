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
@Context
public class BrowseServiceContextImpl implements BrowseServiceContext {
    private Vertx vertx;
    private JsonObject config;
    private BrowseService browseService;

    private ElasticSearchManager elasticSearchManager;

    public BrowseServiceContextImpl(Vertx vertx, JsonObject config) {
        this.vertx = vertx;
        this.config = config;
        init();
    }

    public void init() {
        elasticSearchManager = ElasticSearchManager.getINSTANCE(config.getJsonObject("elasticsearch"));
        browseService = new BrowseServiceImpl(vertx, elasticSearchManager);
    }

    public BrowseService getBrowseService() {
        return browseService;
    }

    public void setBrowseService(BrowseService browseService) {
        this.browseService = browseService;
    }

    public ElasticSearchManager getElasticSearchManager() {
        return elasticSearchManager;
    }

    public void setElasticSearchManager(ElasticSearchManager elasticSearchManager) {
        this.elasticSearchManager = elasticSearchManager;
    }
}
