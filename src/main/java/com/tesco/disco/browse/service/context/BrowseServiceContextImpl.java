package com.tesco.disco.browse.service.context;

import com.tesco.disco.browse.service.BrowseService;
import com.tesco.disco.browse.service.elasticsearch.ElasticSearchClientFactory;
import com.tesco.disco.browse.service.impl.BrowseServiceImpl;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.Vertx;
import com.tesco.search.commons.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/**
 * Created by bruno on 21/04/16.
 */
@Context
public class BrowseServiceContextImpl implements BrowseServiceContext {
    private static final Logger LOGGER = LoggerFactory.getLogger(BrowseServiceContextImpl.class);
    private static final Marker MARKER = MarkerFactory.getMarker("CONTEXT");
    private Vertx vertx;
    private JsonObject config;
    private BrowseService browseService;

    private ElasticSearchClientFactory elasticSearchClientFactory;

    public BrowseServiceContextImpl(Vertx vertx, JsonObject config) {
        LOGGER.info(MARKER, "Initializing context container for browse service verticle");
        this.vertx = vertx;
        this.config = config;
        init();
    }

    public void init() {
        elasticSearchClientFactory = ElasticSearchClientFactory.getINSTANCE(config.getJsonObject("elasticsearch"));
        browseService = new BrowseServiceImpl(vertx, elasticSearchClientFactory);
    }

    public BrowseService getBrowseService() {
        return browseService;
    }

    public void setBrowseService(BrowseService browseService) {
        this.browseService = browseService;
    }

    public ElasticSearchClientFactory getElasticSearchClientFactory() {
        return elasticSearchClientFactory;
    }

    public void setElasticSearchClientFactory(ElasticSearchClientFactory elasticSearchClientFactory) {
        this.elasticSearchClientFactory = elasticSearchClientFactory;
    }
}
