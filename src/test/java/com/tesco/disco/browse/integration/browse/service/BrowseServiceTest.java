package com.tesco.disco.browse.integration.browse.service;

import com.tesco.disco.browse.integration.AbstractElasticsearchTestVerticle;
import com.tesco.disco.browse.service.context.BrowseServiceContext;
import com.tesco.disco.browse.service.elasticsearch.ElasticSearchManager;
import com.tesco.disco.browse.utis.ConfigurationUtils;
import com.tesco.search.commons.context.ContextDelegator;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.rxjava.core.Vertx;
import org.apache.commons.io.IOUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

/**
 * Created by bruno on 21/04/16.
 */
@RunWith(VertxUnitRunner.class)
public class BrowseServiceTest extends AbstractElasticsearchTestVerticle {
    static Vertx vertx;
    static ElasticSearchManager esManager;


    @BeforeClass
    public static void test(TestContext testContext) throws IOException {
        Async async = testContext.async();
        vertx = Vertx.vertx();
        vertx.deployVerticle(AbstractElasticsearchTestVerticle.class.getName(), res -> {
            if (res.succeeded()) {
                System.out.println("Deployment id is: " + res.result());
                async.complete();
            } else {
                System.out.println("Deployment failed!");
            }
        });
        String testConfig = IOUtils.toString(Thread.currentThread().getContextClassLoader().getResource("config/application-test.json"));
        String serviceVerticleConfig = Json.encode(new JsonObject(testConfig).getJsonArray("verticles").getList().get(0));
        BrowseServiceContext context = ContextDelegator.getInstance()
                .getContext(vertx, new JsonObject(serviceVerticleConfig).getJsonObject("options").getJsonObject("config"));
        esManager = context.getElasticSearchManager();
    }

    @Test
    public void testBrowse() {
        SearchResponse res = getClient().prepareSearch()
                .setIndices("ghs.taxonomy")
                .get();
        res.toString();
    }
}
