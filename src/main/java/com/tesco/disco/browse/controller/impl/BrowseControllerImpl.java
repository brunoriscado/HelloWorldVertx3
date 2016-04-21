package com.tesco.disco.browse.controller.impl;

import com.tesco.disco.browse.controller.BrowseController;
import com.tesco.disco.browse.model.taxonomy.model.*;
import com.tesco.disco.browse.service.BrowseService;
import io.vertx.core.AsyncResult;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rx.java.ObservableHandler;
import io.vertx.rx.java.RxHelper;
import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.core.http.HttpServer;
import io.vertx.rxjava.core.http.HttpServerResponse;
import io.vertx.rxjava.ext.web.Router;
import io.vertx.rxjava.ext.web.RoutingContext;
import org.elasticsearch.common.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Observer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

        subRouter.get("/cenas/").handler(this::browseHandler);

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

        if (query.isEmpty()) {
            query = null;
        }

        browse(query, context.response());
    }

    public void browse(JsonObject payload, HttpServerResponse response) {
        //Call the Browse service and responde
        ObservableHandler<AsyncResult<JsonObject>> handler = RxHelper.observableHandler();
        browseService.getBrowseResults(payload, handler.toHandler());
        handler.flatMap(esResponse -> {
                    if (esResponse.succeeded()) {
                        return Observable.just(esResponse.result());
                    } else {
                        return Observable.error(new RuntimeException("oops"));
                    }
                })
                .subscribe(
                      next -> {},
                        error -> {},
                        () -> {
                            response.setStatusCode(200);
                            response.end("Pong");
                        });
    }

    //TODO unit test this method -  possibly converted to RXjava?
    //Also there might be more performant ways of mapping the response
    //this method might have a bit of overhead
    protected JsonObject getTaxonomy(JsonObject ESResponse) {
        LOGGER.debug("Mapping elastic aggregations response to API taxonomy");
        Taxonomy taxonomy = null;
        if (ESResponse.containsKey("aggregations")) {
            taxonomy = new Taxonomy();
            if (ESResponse.getJsonObject("aggregations").containsKey("superDepartments")) {
                List<SuperDepartment> superDepartmentArray = new ArrayList<SuperDepartment>();
                LOGGER.debug("Iterating over superDepartments in taxonomy");
                JsonArray sdBuckets = ESResponse.getJsonObject("aggregations").getJsonObject("superDepartments").getJsonArray("buckets");
                for (Iterator itSd = sdBuckets.iterator(); itSd.hasNext();) {
                    JsonObject sdBucketEntry = (JsonObject)itSd.next();

                    SuperDepartment sd = new SuperDepartment(
                            sdBucketEntry.getString("key"),
                            sdBucketEntry.getInteger("doc_count"));

                    List<Department> departmentArray = new ArrayList<Department>();

                    LOGGER.debug("Iterating over departments in taxonomy");
                    JsonArray departmentBuckets  = sdBucketEntry.getJsonObject("departments").getJsonArray("buckets");
                    for (Iterator itDep = departmentBuckets.iterator(); itDep.hasNext();) {
                        JsonObject departmentBucketEntry = (JsonObject)itDep.next();

                        Department department = new Department(
                                departmentBucketEntry.getString("key"),
                                departmentBucketEntry.getInteger("doc_count"));

                        List<Aisle> aislesArray = new ArrayList<Aisle>();

                        LOGGER.debug("Iterating over aisles in taxonomy");
                        JsonArray aisleBuckets  = departmentBucketEntry.getJsonObject("aisles").getJsonArray("buckets");
                        for (Iterator itAisle = aisleBuckets.iterator(); itAisle.hasNext();) {
                            JsonObject aisleBucketEntry = (JsonObject)itAisle.next();

                            Aisle aisle = new Aisle(
                                    aisleBucketEntry.getString("key"),
                                    aisleBucketEntry.getInteger("doc_count"));

                            List<Shelf> shelvesArray = new ArrayList<Shelf>();

                            LOGGER.debug("Iterating over shelves in taxonomy");
                            JsonArray shelfBuckets  = aisleBucketEntry.getJsonObject("shelves").getJsonArray("buckets");

                            for (Iterator itShelf = shelfBuckets.iterator(); itShelf.hasNext();) {
                                JsonObject shelfBucketEntry = (JsonObject)itShelf.next();

                                Shelf shelf = new Shelf(
                                        shelfBucketEntry.getString("key"),
                                        shelfBucketEntry.getInteger("doc_count"));

                                shelvesArray.add(shelf);
                            }

                            if (!shelvesArray.isEmpty()) {
                                aisle.addShelves(shelvesArray);
                                aislesArray.add(aisle);
                            }
                        }

                        if (!aislesArray.isEmpty()) {
                            department.addAisles(aislesArray);
                            departmentArray.add(department);
                        }
                    }

                    if(!departmentArray.isEmpty()) {
                        sd.addDepartments(departmentArray);
                        superDepartmentArray.add(sd);
                    }
                }
                if(!superDepartmentArray.isEmpty()) {
                    taxonomy.addSuperDepartments(superDepartmentArray);
                }
            } else {
                LOGGER.debug("No aggregations in the elastic response, taxonomy will be empty.");
                taxonomy = new Taxonomy();
            }
        }
        return taxonomy.toJson();
    }
}