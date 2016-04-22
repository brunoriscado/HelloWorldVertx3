package com.tesco.disco.browse.integration.browse.service;

import com.tesco.disco.browse.integration.AbstractElasticsearchTestVerticle;
import com.tesco.disco.browse.integration.browse.BrowseTest;
import com.tesco.disco.browse.service.context.BrowseServiceContext;
import com.tesco.disco.browse.service.elasticsearch.ElasticSearchClientFactory;
import com.tesco.search.commons.context.ContextDelegator;
import io.vertx.core.json.Json;
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
public class BrowseServiceTest extends AbstractElasticsearchTestVerticle implements BrowseTest {
    static Vertx vertx;
    static ElasticSearchClientFactory esManager;

    @BeforeClass
    public static void setup(TestContext testContext) throws IOException {
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
        esManager = context.getElasticSearchClientFactory();
    }

    @Test
    public void testGenericBrowse(TestContext testContext) {
        SearchResponse res = esManager.getElasticsearchClient().prepareSearch()
                .setIndices("ghs.taxonomy")
                .get();
        res.toString();
    }

    @Test
    public void testBrowseWithSuperDeparmentFilter(TestContext testContext) {

    }

    @Test
    public void testBrowseWithDeparmentFilter(TestContext testContext) {

    }

    @Test
    public void testBrowseWithAisleFilter(TestContext testContext) {

    }

    @Test
    public void testBrowseWithShelfFilter(TestContext testContext) {

    }

    @Test
    public void testIncorrectBrowseEndpoint(TestContext testContext) {

    }

    @Test
    public void testEmptyTaxonomyResponse(TestContext testContext) {

    }

    @Test
    public void testNonExistentFilter(TestContext testContext) {

    }
}
