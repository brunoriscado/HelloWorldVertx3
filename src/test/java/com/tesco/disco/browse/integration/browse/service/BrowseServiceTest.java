package com.tesco.disco.browse.integration.browse.service;

import com.tesco.disco.browse.integration.AbstractElasticsearchTestVerticle;
import com.tesco.disco.browse.integration.browse.BrowseTest;
import com.tesco.disco.browse.service.BrowseService;
import com.tesco.disco.browse.service.context.BrowseServiceContext;
import com.tesco.search.commons.context.ContextDelegator;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.rxjava.core.Vertx;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
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
    static BrowseServiceContext context;
    BrowseService browseService;

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
        context = ContextDelegator.getInstance()
                .getContext(vertx, new JsonObject(serviceVerticleConfig).getJsonObject("options").getJsonObject("config"));
    }

    @Before
    public void startServices() {
        browseService = context.getBrowseService();
    }

    @Test
    public void testGenericBrowse(TestContext testContext) {
        Async async = testContext.async();
        browseService.getBrowseResults(null, handle -> {
            if (handle.succeeded()) {
                JsonObject response = handle.result();
                JsonArray superDepartments = response.getJsonObject("uk")
                        .getJsonObject("ghs")
                        .getJsonObject("taxonomy")
                        .getJsonArray("superDepartments");
                testContext.assertEquals(superDepartments.size(), 2);
                async.complete();
            } else {
                testContext.fail("failed to check response payload");
            }
        });
    }

    @Test
    public void testBrowseWithSuperDeparmentFilter(TestContext testContext) {
        Async async = testContext.async();
        browseService.getBrowseResults(new JsonObject().put("superDepartment", "Health & Beauty"), handle -> {
            if (handle.succeeded()) {
                JsonObject response = handle.result();
                JsonArray superDepartments = response.getJsonObject("uk")
                        .getJsonObject("ghs")
                        .getJsonObject("taxonomy")
                        .getJsonArray("superDepartments");
                testContext.assertEquals(superDepartments.size(), 2);
                async.complete();
            } else {
                testContext.fail("failed to check response payload");
            }
        });
    }

    @Test
    public void testBrowseWithDeparmentFilter(TestContext testContext) {
        Async async = testContext.async();
        browseService.getBrowseResults(null, handle -> {
            JsonObject response = handle.result();
            JsonArray superDepartments = response.getJsonObject("uk")
                    .getJsonObject("ghs")
                    .getJsonObject("taxonomy")
                    .getJsonArray("superDepartments");
            testContext.assertEquals(superDepartments.size(), 2);
            async.complete();
        });
    }

    @Test
    public void testBrowseWithAisleFilter(TestContext testContext) {
        Async async = testContext.async();
        browseService.getBrowseResults(null, handle -> {
            JsonObject response = handle.result();
            JsonArray superDepartments = response.getJsonObject("uk")
                    .getJsonObject("ghs")
                    .getJsonObject("taxonomy")
                    .getJsonArray("superDepartments");
            testContext.assertEquals(superDepartments.size(), 2);
            async.complete();
        });
    }

    @Test
    public void testBrowseWithShelfFilter(TestContext testContext) {
        Async async = testContext.async();
        browseService.getBrowseResults(null, handle -> {
            JsonObject response = handle.result();
            JsonArray superDepartments = response.getJsonObject("uk")
                    .getJsonObject("ghs")
                    .getJsonObject("taxonomy")
                    .getJsonArray("superDepartments");
            testContext.assertEquals(superDepartments.size(), 2);
            async.complete();
        });
    }

    @Test
    public void testIncorrectBrowseEndpoint(TestContext testContext) {
        Async async = testContext.async();
        browseService.getBrowseResults(null, handle -> {
            JsonObject response = handle.result();
            JsonArray superDepartments = response.getJsonObject("uk")
                    .getJsonObject("ghs")
                    .getJsonObject("taxonomy")
                    .getJsonArray("superDepartments");
            testContext.assertEquals(superDepartments.size(), 2);

        });
    }

    @Test
    public void testEmptyTaxonomyResponse(TestContext testContext) {
        Async async = testContext.async();
        browseService.getBrowseResults(null, handle -> {
            JsonObject response = handle.result();
            JsonArray superDepartments = response.getJsonObject("uk")
                    .getJsonObject("ghs")
                    .getJsonObject("taxonomy")
                    .getJsonArray("superDepartments");
            testContext.assertEquals(superDepartments.size(), 2);

        });
    }

    @Test
    public void testNonExistentFilter(TestContext testContext) {
        Async async = testContext.async();
        browseService.getBrowseResults(null, handle -> {
            JsonObject response = handle.result();
            JsonArray superDepartments = response.getJsonObject("uk")
                    .getJsonObject("ghs")
                    .getJsonObject("taxonomy")
                    .getJsonArray("superDepartments");
            testContext.assertEquals(superDepartments.size(), 2);

        });
    }
}
