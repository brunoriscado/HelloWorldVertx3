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
    private static final String INDEX = "ghs.taxonomy";
    private static final String TEMPLATE_ID = "ghs.taxonomy.default";
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

        subRouter.get("/browse/*").handler(this::browseHandler);
        subRouter.get("/_status").handler(this::statusHandler);

        router.mountSubRouter("/", subRouter);
    }

    private void statusHandler(RoutingContext context) {
        context.response().setStatusCode(200);
        context.response().end("keepalive");
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

        if (query.isEmpty()) {
            query = null;
        }

        browse(context.request().getParam("geo") == null ? context.request().getParam("geo") : "uk",
                context.request().getParam("distChannel") == null ? context.request().getParam("distChannel") : "ghs",
                query,
                context.response());
    }

    //Annotate this method so that swagger definitions can be generated
    public void browse(String geo, String distChannel, JsonObject payload, HttpServerResponse response) {
        ObservableHandler<AsyncResult<JsonObject>> handler = RxHelper.observableHandler();
        browseService.getBrowseResults(INDEX, TEMPLATE_ID, geo, distChannel, payload, handler.toHandler());
        response.setChunked(true);
        handler.flatMap(esResponse -> {
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
        //TODO - do stuff, check error type and respond accordingly
    }
}