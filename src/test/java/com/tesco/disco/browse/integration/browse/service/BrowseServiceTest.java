package com.tesco.disco.browse.integration.browse.service;

import com.tesco.disco.browse.integration.AbstractElasticsearchTestVerticle;
import com.tesco.disco.browse.integration.browse.BrowseTest;
import com.tesco.disco.browse.service.BrowseService;
import com.tesco.disco.browse.service.context.BrowseServiceContext;
import com.tesco.search.commons.context.ContextDelegator;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.rxjava.core.Vertx;
import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by bruno on 21/04/16.
 */
@RunWith(VertxUnitRunner.class)
public class BrowseServiceTest extends AbstractElasticsearchTestVerticle implements BrowseTest {
    private static final String INDEX = "ghs.taxonomy";
    private static final String TEMPLATE_ID = "ghs.taxonomy.default";
    private static final String GEO = "uk";
    private static final String DIST_CHANNEL = "ghs";
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
        JsonObject serviceVerticleConfig = new JsonObject(Json.encode(new JsonObject(testConfig).getJsonArray("verticles").getList().get(0)));
        JsonObject controllerVerticleConfig = new JsonObject(Json.encode(new JsonObject(testConfig).getJsonArray("verticles").getList().get(1)));

        Async asyncService = testContext.async();

        vertx.deployVerticle(serviceVerticleConfig.getString("main"), new DeploymentOptions(serviceVerticleConfig.getJsonObject("options")), res -> {
            if (res.succeeded()) {
                System.out.println("Deployment id is: " + res.result());
                asyncService.complete();
            } else {
                System.out.println("Deployment failed!");
            }
        });

        context = ContextDelegator.getInstance()
                .getContext(vertx, serviceVerticleConfig.getJsonObject("options").getJsonObject("config"));
    }

    @Before
    public void startServices() {
        browseService = context.getBrowseService();
    }

    @Test
    public void testGenericBrowse(TestContext testContext) {
        List<String> shelves = new ArrayList<String>();
        Async async = testContext.async();
        browseService.getBrowseResults(INDEX, TEMPLATE_ID, GEO, DIST_CHANNEL, null, handle -> {
            if (handle.succeeded()) {
                JsonObject response = handle.result();
                response.getJsonObject("uk")
                        .getJsonObject("ghs")
                        .getJsonObject("taxonomy")
                        .getJsonArray("superDepartments")
                        .forEach(superDepartment -> {
                            JsonObject superDep = new JsonObject(Json.encode(superDepartment));
                            superDep.getJsonArray("departments")
                                    .forEach(department -> {
                                        JsonObject jsonDep = new JsonObject(Json.encode(department));
                                        jsonDep.getJsonArray("aisles")
                                                .forEach(aisle -> {
                                                    JsonObject jsonAisle = new JsonObject(Json.encode(aisle));
                                                    jsonAisle.getJsonArray("shelves")
                                                            .forEach(shelf -> {
                                                                shelves.add(new JsonObject(Json.encode(shelf)).getString("name"));
                                                            });
                                                });
                                    });
                        });
                testContext.assertEquals(shelves.size(), 9);
                List<String> expected = new ArrayList<String>();
                expected.add("Tesco Shower Gel");
                expected.add("Travel Sizes");
                expected.add("Womens Gift Sets");
                expected.add("Colour Conditioner");
                expected.add("Professional Shampoo");
                expected.add("Anti Dandruff Shampoo");
                expected.add("Kids Shampoo");
                expected.add("Professional Styling");
                expected.add("Blonde Shampoo & Conditioner");
                testContext.assertTrue(shelves.containsAll(expected));
                async.complete();
            } else {
                testContext.fail("failed to check response payload");
            }
        });
    }

    @Test
    public void testBrowseWithSuperDeparmentFilter(TestContext testContext) {
        List<String> shelves = new ArrayList<String>();
        Async async = testContext.async();
        browseService.getBrowseResults(INDEX, TEMPLATE_ID, GEO, DIST_CHANNEL, new JsonObject().put("superDepartment", "Health & Beauty"), handle -> {
            if (handle.succeeded()) {
                JsonObject response = handle.result();
                response.getJsonObject("uk")
                        .getJsonObject("ghs")
                        .getJsonObject("taxonomy")
                        .getJsonArray("superDepartments")
                        .forEach(superDepartment -> {
                            JsonObject superDep = new JsonObject(Json.encode(superDepartment));
                            superDep.getJsonArray("departments")
                                    .forEach(department -> {
                                        JsonObject jsonDep = new JsonObject(Json.encode(department));
                                        jsonDep.getJsonArray("aisles")
                                                .forEach(aisle -> {
                                                    JsonObject jsonAisle = new JsonObject(Json.encode(aisle));
                                                    jsonAisle.getJsonArray("shelves")
                                                            .forEach(shelf -> {
                                                                shelves.add(new JsonObject(Json.encode(shelf)).getString("name"));
                                                            });
                                                });
                                    });
                        });
                testContext.assertEquals(shelves.size(), 9);
                List<String> expected = new ArrayList<String>();
                expected.add("Tesco Shower Gel");
                expected.add("Travel Sizes");
                expected.add("Womens Gift Sets");
                expected.add("Colour Conditioner");
                expected.add("Professional Shampoo");
                expected.add("Anti Dandruff Shampoo");
                expected.add("Kids Shampoo");
                expected.add("Professional Styling");
                expected.add("Blonde Shampoo & Conditioner");
                testContext.assertTrue(shelves.containsAll(expected));
                async.complete();
            } else {
                testContext.fail("failed to check response payload");
            }
        });
    }

    @Test
    public void testBrowseWithDeparmentFilter(TestContext testContext) {
        List<String> shelves = new ArrayList<String>();
        Async async = testContext.async();
        browseService.getBrowseResults(INDEX, TEMPLATE_ID, GEO, DIST_CHANNEL, new JsonObject().put("department", "Haircare"), handle -> {
            JsonObject response = handle.result();
            response.getJsonObject("uk")
                    .getJsonObject("ghs")
                    .getJsonObject("taxonomy")
                    .getJsonArray("superDepartments")
                    .forEach(superDepartment -> {
                        JsonObject superDep = new JsonObject(Json.encode(superDepartment));
                        superDep.getJsonArray("departments")
                                .forEach(department -> {
                                    JsonObject jsonDep = new JsonObject(Json.encode(department));
                                    jsonDep.getJsonArray("aisles")
                                            .forEach(aisle -> {
                                                JsonObject jsonAisle = new JsonObject(Json.encode(aisle));
                                                jsonAisle.getJsonArray("shelves")
                                                        .forEach(shelf -> {
                                                            shelves.add(new JsonObject(Json.encode(shelf)).getString("name"));
                                                        });
                                            });
                                });
                    });
            testContext.assertEquals(shelves.size(), 6);
            List<String> expected = new ArrayList<String>();
            expected.add("Colour Conditioner");
            expected.add("Professional Shampoo");
            expected.add("Anti Dandruff Shampoo");
            expected.add("Kids Shampoo");
            expected.add("Professional Styling");
            expected.add("Blonde Shampoo & Conditioner");
            testContext.assertTrue(shelves.containsAll(expected));
            async.complete();
        });
    }

    @Test
    public void testBrowseWithAisleFilter(TestContext testContext) {
        List<String> shelves = new ArrayList<String>();
        Async async = testContext.async();
        browseService.getBrowseResults(INDEX, TEMPLATE_ID, GEO, DIST_CHANNEL, new JsonObject().put("aisle", "Gift Sets"), handle -> {
            JsonObject response = handle.result();
            response.getJsonObject("uk")
                    .getJsonObject("ghs")
                    .getJsonObject("taxonomy")
                    .getJsonArray("superDepartments")
                    .forEach(superDepartment -> {
                        JsonObject superDep = new JsonObject(Json.encode(superDepartment));
                        superDep.getJsonArray("departments")
                                .forEach(department -> {
                                    JsonObject jsonDep = new JsonObject(Json.encode(department));
                                    jsonDep.getJsonArray("aisles")
                                            .forEach(aisle -> {
                                                JsonObject jsonAisle = new JsonObject(Json.encode(aisle));
                                                jsonAisle.getJsonArray("shelves")
                                                        .forEach(shelf -> {
                                                            shelves.add(new JsonObject(Json.encode(shelf)).getString("name"));
                                                        });
                                            });
                                });
                    });
            testContext.assertEquals(shelves.size(), 1);
            List<String> expected = new ArrayList<String>();
            expected.add("Womens Gift Sets");
            testContext.assertTrue(shelves.containsAll(expected));
            async.complete();
        });
    }

    @Test
    public void testBrowseWithShelfFilter(TestContext testContext) {
        List<String> shelves = new ArrayList<String>();
        Async async = testContext.async();
        browseService.getBrowseResults(INDEX, TEMPLATE_ID, GEO, DIST_CHANNEL, new JsonObject().put("shelf", "Womens Gift Sets"), handle -> {
            JsonObject response = handle.result();
            response.getJsonObject("uk")
                    .getJsonObject("ghs")
                    .getJsonObject("taxonomy")
                    .getJsonArray("superDepartments")
                    .forEach(superDepartment -> {
                        JsonObject superDep = new JsonObject(Json.encode(superDepartment));
                        superDep.getJsonArray("departments")
                                .forEach(department -> {
                                    JsonObject jsonDep = new JsonObject(Json.encode(department));
                                    jsonDep.getJsonArray("aisles")
                                            .forEach(aisle -> {
                                                JsonObject jsonAisle = new JsonObject(Json.encode(aisle));
                                                jsonAisle.getJsonArray("shelves")
                                                        .forEach(shelf -> {
                                                            shelves.add(new JsonObject(Json.encode(shelf)).getString("name"));
                                                        });
                                            });
                                });
                    });
            testContext.assertEquals(1, shelves.size());
            List<String> expected = new ArrayList<String>();
            expected.add("Womens Gift Sets");
            testContext.assertTrue(shelves.containsAll(expected));
            async.complete();
        });
    }

    @Test
    public void testEmptyTaxonomyResponse(TestContext testContext) {
        List<String> shelves = new ArrayList<String>();
        Async async = testContext.async();
        browseService.getBrowseResults(INDEX, TEMPLATE_ID, GEO, DIST_CHANNEL, new JsonObject().put("superDepartment", "NonExistentSuperDepartment"), handle -> {
            JsonObject response = handle.result();
            testContext.assertTrue(response.getJsonObject("uk")
                            .getJsonObject("ghs")
                            .getJsonObject("taxonomy").isEmpty());
            async.complete();
        });
    }

    @Test
    public void testNonExistentFilter(TestContext testContext) {
        List<String> shelves = new ArrayList<String>();
        Async async = testContext.async();
        browseService.getBrowseResults(INDEX, TEMPLATE_ID, GEO, DIST_CHANNEL, new JsonObject().put("nonExistentFiler", "nonExistentFiler"), handle -> {
            JsonObject response = handle.result();
            response.getJsonObject("uk")
                    .getJsonObject("ghs")
                    .getJsonObject("taxonomy")
                    .getJsonArray("superDepartments")
                    .forEach(superDepartment -> {
                        JsonObject superDep = new JsonObject(Json.encode(superDepartment));
                        superDep.getJsonArray("departments")
                                .forEach(department -> {
                                    JsonObject jsonDep = new JsonObject(Json.encode(department));
                                    jsonDep.getJsonArray("aisles")
                                            .forEach(aisle -> {
                                                JsonObject jsonAisle = new JsonObject(Json.encode(aisle));
                                                jsonAisle.getJsonArray("shelves")
                                                        .forEach(shelf -> {
                                                            shelves.add(new JsonObject(Json.encode(shelf)).getString("name"));
                                                        });
                                            });
                                });
                    });
            testContext.assertEquals(shelves.size(), 9);
            List<String> expected = new ArrayList<String>();
            expected.add("Tesco Shower Gel");
            expected.add("Travel Sizes");
            expected.add("Womens Gift Sets");
            expected.add("Colour Conditioner");
            expected.add("Professional Shampoo");
            expected.add("Anti Dandruff Shampoo");
            expected.add("Kids Shampoo");
            expected.add("Professional Styling");
            expected.add("Blonde Shampoo & Conditioner");
            testContext.assertTrue(shelves.containsAll(expected));
            async.complete();
        });
    }

    @AfterClass
    public static void tearDown() {
//        shutdownEmbeddedElasticsearchServer();
    }
}
