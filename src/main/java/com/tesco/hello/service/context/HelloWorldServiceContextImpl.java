package com.tesco.hello.service.context;

import com.tesco.hello.service.HelloWorldService;
import com.tesco.hello.service.impl.HelloWorldServiceImpl;
import com.tesco.hello.utils.Context;
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
public class HelloWorldServiceContextImpl implements HelloWorldServiceContext {
    private static final Logger LOGGER = LoggerFactory.getLogger(HelloWorldServiceContextImpl.class);
    private static final Marker MARKER = MarkerFactory.getMarker("CONTEXT");
    private Vertx vertx;
    private JsonObject config;
    private HelloWorldService browseService;

    public HelloWorldServiceContextImpl(Vertx vertx, JsonObject config) {
        LOGGER.info(MARKER, "Initializing context container for test service verticle");
        this.vertx = vertx;
        this.config = config;
        init();
    }

    public void init() {
        browseService = new HelloWorldServiceImpl(vertx);
    }

    public HelloWorldService getHelloWorldService() {
        return browseService;
    }

    public void setBrowseService(HelloWorldService browseService) {
        this.browseService = browseService;
    }
}
