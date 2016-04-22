package com.tesco.disco.browse.service.impl;

import com.tesco.disco.browse.model.taxonomy.*;
import com.tesco.disco.browse.service.BrowseService;
import com.tesco.disco.browse.service.elasticsearch.ElasticSearchClientFactory;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.Vertx;
import io.vertx.serviceproxy.ProxyHelper;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.script.ScriptService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Created by bruno on 20/04/16.
 */
public class BrowseServiceImpl implements BrowseService {
    private static final Logger LOGGER = LoggerFactory.getLogger(BrowseServiceImpl.class);
    private static final String TAXONOMY = "taxonomy";
    private MessageConsumer<JsonObject> consumer;
    private TransportClient client;

    public BrowseServiceImpl(Vertx vertx, ElasticSearchClientFactory elasticSearchClientFactory) {
        client = elasticSearchClientFactory.getElasticsearchClient();
        consumer = ProxyHelper
                .registerService(BrowseService.class, (io.vertx.core.Vertx)vertx.getDelegate(), this, BrowseService.class.getName());
    }

    @Override
    public void getBrowseResults(String index, String templateId, String geo, String distChannel, JsonObject query, Handler<AsyncResult<JsonObject>> response) {
        SearchResponse res = client.prepareSearch()
                .setIndices(index)
                .setTemplateName(templateId)
                .setTemplateType(ScriptService.ScriptType.INDEXED)
                .setTemplateParams(query == null ? new HashMap<String, Object>() : query.getMap())
                .get();

        try {
            XContentBuilder builder = XContentFactory.jsonBuilder();
            builder.startObject();
            res.toXContent(builder, SearchResponse.EMPTY_PARAMS);
            builder.endObject();
            JsonObject resJson = new JsonObject(builder.string());
            response.handle(Future.succeededFuture(new JsonObject()
                    .put(geo, new JsonObject()
                            .put(distChannel, new JsonObject()
                                    .put(TAXONOMY, getBrowsingTaxonomy(resJson))))));
        } catch (IOException e) {
            throw new RuntimeException("oops");
        }
    }

    //TODO unit test this method -  possibly converted to RXjava?
    //Also there might be more performant ways of mapping the response
    //this method might have a bit of overhead
    protected JsonObject getBrowsingTaxonomy(JsonObject ESResponse) {
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


//    protected JsonObject getBrowsingTaxonomyRx(JsonObject elasticResponse) {
//        LOGGER.debug("Mapping elastic aggregations response to API taxonomy");
//        Taxonomy taxonomy = new Taxonomy();
//        Observable.just(elasticResponse)
//                .filter(elasticResp -> {
//                    return elasticResponse.containsKey("aggregations");
//                })
//                .flatMap(elasticResp -> {
//                    elasticResp.getJsonObject("aggregations").getJsonObject("superDepartments").getJsonArray("buckets");
//                })
//
//
//
//
//
//        if (elasticResponse.containsKey("aggregations")) {
//            taxonomy = new Taxonomy();
//            if (ESResponse.getJsonObject("aggregations").containsKey("superDepartments")) {
//                List<SuperDepartment> superDepartmentArray = new ArrayList<SuperDepartment>();
//                LOGGER.debug("Iterating over superDepartments in taxonomy");
//                JsonArray sdBuckets = ESResponse.getJsonObject("aggregations").getJsonObject("superDepartments").getJsonArray("buckets");
//                for (Iterator itSd = sdBuckets.iterator(); itSd.hasNext();) {
//                    JsonObject sdBucketEntry = (JsonObject)itSd.next();
//
//                    SuperDepartment sd = new SuperDepartment(
//                            sdBucketEntry.getString("key"),
//                            sdBucketEntry.getInteger("doc_count"));
//
//                    List<Department> departmentArray = new ArrayList<Department>();
//
//                    LOGGER.debug("Iterating over departments in taxonomy");
//                    JsonArray departmentBuckets  = sdBucketEntry.getJsonObject("departments").getJsonArray("buckets");
//                    for (Iterator itDep = departmentBuckets.iterator(); itDep.hasNext();) {
//                        JsonObject departmentBucketEntry = (JsonObject)itDep.next();
//
//                        Department department = new Department(
//                                departmentBucketEntry.getString("key"),
//                                departmentBucketEntry.getInteger("doc_count"));
//
//                        List<Aisle> aislesArray = new ArrayList<Aisle>();
//
//                        LOGGER.debug("Iterating over aisles in taxonomy");
//                        JsonArray aisleBuckets  = departmentBucketEntry.getJsonObject("aisles").getJsonArray("buckets");
//                        for (Iterator itAisle = aisleBuckets.iterator(); itAisle.hasNext();) {
//                            JsonObject aisleBucketEntry = (JsonObject)itAisle.next();
//
//                            Aisle aisle = new Aisle(
//                                    aisleBucketEntry.getString("key"),
//                                    aisleBucketEntry.getInteger("doc_count"));
//
//                            List<Shelf> shelvesArray = new ArrayList<Shelf>();
//
//                            LOGGER.debug("Iterating over shelves in taxonomy");
//                            JsonArray shelfBuckets  = aisleBucketEntry.getJsonObject("shelves").getJsonArray("buckets");
//
//                            for (Iterator itShelf = shelfBuckets.iterator(); itShelf.hasNext();) {
//                                JsonObject shelfBucketEntry = (JsonObject)itShelf.next();
//
//                                Shelf shelf = new Shelf(
//                                        shelfBucketEntry.getString("key"),
//                                        shelfBucketEntry.getInteger("doc_count"));
//
//                                shelvesArray.add(shelf);
//                            }
//
//                            if (!shelvesArray.isEmpty()) {
//                                aisle.addShelves(shelvesArray);
//                                aislesArray.add(aisle);
//                            }
//                        }
//
//                        if (!aislesArray.isEmpty()) {
//                            department.addAisles(aislesArray);
//                            departmentArray.add(department);
//                        }
//                    }
//
//                    if(!departmentArray.isEmpty()) {
//                        sd.addDepartments(departmentArray);
//                        superDepartmentArray.add(sd);
//                    }
//                }
//                if(!superDepartmentArray.isEmpty()) {
//                    taxonomy.addSuperDepartments(superDepartmentArray);
//                }
//            } else {
//                LOGGER.debug("No aggregations in the elastic response, taxonomy will be empty.");
//                taxonomy = new Taxonomy();
//            }
//        }
//        return taxonomy.toJson();
//    }

    public void unregister() {
        ProxyHelper.unregisterService(consumer);
    }
}
