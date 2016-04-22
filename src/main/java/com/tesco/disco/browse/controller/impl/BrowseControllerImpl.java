package com.tesco.disco.browse.controller.impl;

import com.tesco.disco.browse.controller.BrowseController;
import com.tesco.disco.browse.service.BrowseService;
import io.vertx.core.AsyncResult;
import io.vertx.core.json.JsonObject;
import io.vertx.rx.java.ObservableHandler;
import io.vertx.rx.java.RxHelper;
import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.core.http.HttpServerResponse;
import io.vertx.rxjava.ext.web.Router;
import io.vertx.rxjava.ext.web.RoutingContext;
import org.elasticsearch.common.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

/**
 * Created by bruno on 20/04/16.
 */
public class BrowseControllerImpl implements BrowseController {
    private static final Logger LOGGER = LoggerFactory.getLogger(BrowseControllerImpl.class.getName());
    private Vertx vertx;
    private BrowseService browseService;

    public BrowseControllerImpl(Vertx vertx, Router router, BrowseService browseService) {
        this.vertx = vertx;
        this.browseService = browseService;
        init(router);
    }

    public void init(Router router) {
        LOGGER.info("Initializing routing definitions for controller");
        Router subRouter = Router.router(vertx);
        //TODO - add status route
        subRouter.get("/").handler(this::browseHandler);

        router.mountSubRouter("/browse", subRouter);
    }

    private void browseHandler(RoutingContext context) {
        JsonObject query = new JsonObject();

        if (StringUtils.isNotBlank(context.request().getParam("superDepartment"))) {
            query.put("superDepartment", context.request().getParam("superDepartment"));
        }

        if (StringUtils.isNotBlank(context.request().getParam("department"))) {
            query.put("department", context.request().getParam("department"));
        }

        if (StringUtils.isNotBlank(context.request().getParam("aisle"))) {
            query.put("aisle", context.request().getParam("aisle"));
        }

        if (StringUtils.isNotBlank(context.request().getParam("shelf"))) {
            query.put("shelf", context.request().getParam("shelf"));
        }

        if (StringUtils.isNotBlank(context.request().getParam("distChannel"))) {
            query.put("distChannel", context.request().getParam("distChannel"));
        } else {
            query.put("distChannel", "ghs");
        }

        if (StringUtils.isNotBlank(context.request().getParam("geo"))) {
            query.put("geo", context.request().getParam("geo"));
        } else {
            query.put("geo", "uk");
        }

        if (query.isEmpty()) {
            query = null;
        }

        browse(query, context.response());
    }

    public void browse(JsonObject payload, HttpServerResponse response) {
        ObservableHandler<AsyncResult<JsonObject>> handler = RxHelper.observableHandler();
        browseService.getBrowseResults(payload, handler.toHandler());
        response.setChunked(true);
        handler.flatMap(esResponse -> {
                    //TODO - implement service handling correctly
                    if (esResponse.succeeded()) {
                        return Observable.just(esResponse.result());
                    } else {
                        return Observable.error(new RuntimeException("oops"));
                    }
                })
                .subscribe(
                        next -> {
                            response.write(next.encode());
                        },
                        error -> {
                            handlerError(error, response);
                        },
                        () -> {
                            response.setStatusCode(200);
                            response.end();
                        });
    }

    private void handlerError(Throwable error,  HttpServerResponse response) {
        //TODO - do stuff
    }
}