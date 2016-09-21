package com.tesco.hello.controller.context;

import com.tesco.hello.controller.HelloWorldController;
import com.tesco.hello.controller.impl.HelloWorldControllerImpl;
import com.tesco.hello.service.HelloWorldService;
import com.tesco.hello.utils.Context;
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
public class HelloWorldAPIContextImpl implements HelloWorldAPIContext {
    private static final Logger LOGGER = LoggerFactory.getLogger(HelloWorldAPIContextImpl.class);
    private static final Marker MARKER = MarkerFactory.getMarker("CONTEXT");
    private Vertx vertx;
    private JsonObject config;
    private Router router;
    private com.tesco.hello.service.rxjava.HelloWorldService helloWorldService;
    private HelloWorldController helloWorldController;

    public HelloWorldAPIContextImpl(Vertx vertx, JsonObject config) {
        this.vertx = vertx;
        this.config = config;
        init();
    }

    public void init() {
        LOGGER.info(MARKER, "Initializing API context container");
        router = Router.router(vertx);
        helloWorldService = new com.tesco.hello.service.rxjava.HelloWorldService(ProxyHelper.createProxy(HelloWorldService.class, ((io.vertx.core.Vertx)vertx.getDelegate()), HelloWorldService.class.getName()));
        helloWorldController = new HelloWorldControllerImpl(vertx, router, helloWorldService);
    }

    public com.tesco.hello.service.rxjava.HelloWorldService getHelloWorldService() {
        return helloWorldService;
    }

    public void setHelloWorldControllerService(com.tesco.hello.service.rxjava.HelloWorldService helloWorldService) {
        this.helloWorldService = helloWorldService;
    }

    public HelloWorldController getHelloWorldController() {
        return helloWorldController;
    }

    public void setBrowseController(HelloWorldController helloWorldController) {
        this.helloWorldController = helloWorldController;
    }

    public Router getRouter() {
        return router;
    }

    public void setRouter(Router router) {
        this.router = router;
    }
}
