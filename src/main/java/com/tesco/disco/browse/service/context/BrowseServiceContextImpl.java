package com.tesco.disco.browse.service.context;

import com.tesco.disco.browse.service.BrowseService;
import com.tesco.disco.browse.service.impl.BrowseServiceImpl;
import com.tesco.disco.browse.utils.Context;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.Vertx;
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

    public BrowseServiceContextImpl(Vertx vertx, JsonObject config) {
        LOGGER.info(MARKER, "Initializing context container for browse service verticle");
        this.vertx = vertx;
        this.config = config;
        init();
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
