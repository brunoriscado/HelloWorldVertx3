package com.tesco.disco.browse.service.impl;

import com.tesco.disco.browse.exceptions.ServiceException;
import com.tesco.disco.browse.model.taxonomy.*;
import com.tesco.disco.browse.service.BrowseService;
import com.tesco.disco.browse.service.elasticsearch.ElasticSearchClientFactory;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.Json;
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
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import rx.Observable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by bruno on 20/04/16.
 */
public class BrowseServiceImpl implements BrowseService {
    private static final Logger LOGGER = LoggerFactory.getLogger(BrowseServiceImpl.class);
    private static final Marker MARKER = MarkerFactory.getMarker("SERVICE");
    private static final String TAXONOMY = "taxonomy";
    private MessageConsumer<JsonObject> consumer;
    private TransportClient client;
    private Vertx vertx;

    public BrowseServiceImpl() {
    }

    public BrowseServiceImpl(Vertx vertx, ElasticSearchClientFactory elasticSearchClientFactory) {
        this.vertx = vertx;
        client = elasticSearchClientFactory.getElasticsearchClient();
        consumer = ProxyHelper.registerService(BrowseService.class,
                (io.vertx.core.Vertx) vertx.getDelegate(), this, BrowseService.class.getName());
    }

    @Override
    public void getBrowseResults(String index,
                                 String templateId,
                                 String geo,
                                 String distChannel,
                                 JsonObject query,
                                 Handler<AsyncResult<JsonObject>> response) {
        LOGGER.info(MARKER, "Fetching browse results for index: {}, templateId: {} - using query params: {} ",
                index, templateId, query != null ? query.encode() : "");
        vertx.<JsonObject>executeBlockingObservable(handleBlocking -> {
            JsonObject result = null;
            SearchResponse res = client.prepareSearch()
                    .setIndices(index)
                    .setTemplateName(templateId)
                    .setTemplateType(ScriptService.ScriptType.INDEXED)
                    .setTemplateParams(query == null ? new HashMap<String, Object>() : query.getMap())
                    .execute().actionGet();
            try {
                XContentBuilder builder = XContentFactory.jsonBuilder();
                builder.startObject();
                res.toXContent(builder, SearchResponse.EMPTY_PARAMS);
                builder.endObject();
                result = new JsonObject(builder.string());
            } catch (IOException e) {
                handleBlocking.fail(new ServiceException(e.getMessage()));
            }
            handleBlocking.complete(result);
        })
        .flatMap(elasticResponse -> {
            return getBrowsingTaxonomyRx(elasticResponse);
        })
        .map(mappedResponse -> {
            return new JsonObject().put(geo, new JsonObject()
                    .put(distChannel, new JsonObject().put(
                            TAXONOMY, mappedResponse)));
        })
        .subscribe(
                next -> {
                    response.handle(Future.succeededFuture(next));
                },
                error -> {
                    LOGGER.error(MARKER, "Error obtaining/mapping elasticsearch response");
                    response.handle(Future.failedFuture(error));
                },
                () -> {
                    LOGGER.debug(MARKER, "Finished sending elasticsearch browse request to controller verticle");
                });
    }

    /**
     * This method maps the elasticsearch aggregation response to a taxonomy response that the client expects
     * @param elasticResponse
     * @return
					*/
    public Observable<JsonObject> getBrowsingTaxonomyRx(JsonObject elasticResponse) {
        LOGGER.debug(MARKER, "Mapping elastic aggregations response to API taxonomy");
        Taxonomy taxonomy = new Taxonomy();
        return Observable.just(elasticResponse)
                .filter(elasticResp -> {
                    return elasticResponse.containsKey("aggregations");
                })
                .switchIfEmpty(Observable.error(new RuntimeException("no aggregations found")))
                .flatMap(elasticResp -> {
                    return Observable.from(elasticResp.getJsonObject("aggregations")
                            .getJsonObject("superDepartments")
                            .getJsonArray("buckets").getList().toArray());
                })
                .switchIfEmpty(Observable.error(new RuntimeException("SuperDepartments are empty")))
                .flatMap(superDepartment -> {
                    LOGGER.debug(MARKER, "Mapping superDepartments");
                    JsonObject sd = new JsonObject(Json.encode(superDepartment));
                    return Observable.from(sd.getJsonObject("departments").getJsonArray("buckets").getList().toArray())
                            .switchIfEmpty(Observable.error(new RuntimeException("Departments are empty")))
                            .flatMap(department -> {
                                LOGGER.debug(MARKER, "Mapping departments");
                                JsonObject dep = new JsonObject(Json.encode(department));
                                return Observable.from(dep.getJsonObject("aisles").getJsonArray("buckets").getList().toArray())
                                        .switchIfEmpty(Observable.error(new RuntimeException("Aisles are empty")))
                                        .flatMap(aisle -> {
                                            LOGGER.debug(MARKER, "Mapping aisles");
                                            JsonObject jAisle = new JsonObject(Json.encode(aisle));
                                            return Observable.from(jAisle.getJsonObject("shelves").getJsonArray("buckets").getList().toArray())
                                                    .switchIfEmpty(Observable.error(new RuntimeException("Shelves are empty")))
                                                    .map(shelf -> {
                                                        LOGGER.debug(MARKER, "Mapping shelves");
                                                        JsonObject jShelf = new JsonObject(Json.encode(shelf));
                                                        return new Shelf(jShelf.getString("key"));
                                                    })
                                                    .collect(() -> new ArrayList<Shelf>(),
                                                            (shelves, she) -> shelves.add(she))
                                                    .map(shelves -> {
                                                        return new Aisle(jAisle.getString("key")).addShelves(shelves);
                                                    });
                                        })
                                        .collect(() -> new ArrayList<Aisle>(),
                                                (aisles, aisle) -> aisles.add(aisle))
                                        .map(aisles -> {
                                            return new Department(dep.getString("key")).addAisles(aisles);
                                        });
                            })
                            .collect(() -> new ArrayList<Department>(),
                                    (departments, department) -> departments.add(department))
                            .map(departments -> {
                                return new SuperDepartment(sd.getString("key")).addDepartments(departments);
                            })
                            .collect(() -> new ArrayList<SuperDepartment>(),
                                    (superDeps, superDep) -> superDeps.add(superDep))
                            .map(supeDeps -> {
                                    return taxonomy.addSuperDepartments(supeDeps).toJson();
                            });
                })
                .onErrorResumeNext(Observable.just(taxonomy.toJson()));
    }

    public void unregister() {
        LOGGER.info(MARKER, "Unregistering verticle address: {} from the eventbus", BrowseService.class.getName());
        ProxyHelper.unregisterService(consumer);
    }
}
