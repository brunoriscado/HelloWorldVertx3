package com.tesco.disco.browse.integration.browse.service;

import com.tesco.disco.browse.integration.AbstractElasticsearchTestVerticle;
import com.tesco.disco.browse.integration.browse.BrowseTest;
import com.tesco.disco.browse.model.enumerations.DistributionChannelsEnum;
import com.tesco.disco.browse.model.enumerations.IndicesEnum;
import com.tesco.disco.browse.model.enumerations.ResponseTypesEnum;
import com.tesco.disco.browse.service.BrowseService;
import com.tesco.disco.browse.service.context.BrowseServiceContext;
import com.tesco.search.commons.context.ContextDelegator;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;

/**
 * Created by bruno on 21/04/16.
 */
@RunWith(VertxUnitRunner.class)
public class BrowseServiceTest extends AbstractElasticsearchTestVerticle implements BrowseTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(BrowseServiceTest.class);
    private static final String TEMPLATE_ID = "default";
    private static final String GEO = "uk";
    static Vertx vertx;
    static BrowseServiceContext context;
    BrowseService browseService;
    JsonObject defaults;

    @BeforeClass
    public static void setup(TestContext testContext) throws IOException {
        Async async = testContext.async();
        vertx = Vertx.vertx();
        vertx.deployVerticle(AbstractElasticsearchTestVerticle.class.getName(), res -> {
            if (res.succeeded()) {
                LOGGER.debug("Deployment id is: {}", res.result());
                async.complete();
            } else {
                LOGGER.debug("Deployment failed!");
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
        defaults = new JsonObject("{\"limit\":\"10\",\"store\":\"3060\",\"offset\":\"0\"," +
                "\"results\":\"true\",\"totals\":\"true\"," +
                "\"suggestions\":\"true\",\"geo\":\"uk\",\"distChannel\":\"ghs\",\"index\":\"ghs.products\"," +
                "\"resType\":\"products\",\"config\":\"default\"}");
        defaults.put("fields", "\"" + "tpnb" + "\"");
    }

    @Test
    public void testGenericBrowse(TestContext testContext) {
        List<String> shelves = new ArrayList<String>();
        Async async = testContext.async();
        browseService.getBrowseResults(IndicesEnum.GHS_TAXONOMY.getIndex(),
                TEMPLATE_ID,
                GEO,
                DistributionChannelsEnum.GHS.getChannelName(),
                ResponseTypesEnum.TAXONOMY.getType(),
                null,
                handle -> {
                    if (handle.succeeded()) {
                        JsonObject response = handle.result();
                        JsonArray superDepartments = response.getJsonObject("uk")
                                .getJsonObject("ghs")
                                .getJsonObject("taxonomy")
                                .getJsonArray("superDepartments");
                        testContext.assertTrue(superDepartments.size() > 1);
                        superDepartments
                                .forEach(superDepartment -> {
                                    JsonObject sd = new JsonObject(Json.encode(superDepartment));
                                    if (sd.getString("name").equals("Health & Beauty")) {
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
                                    }
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
        browseService.getBrowseResults(IndicesEnum.GHS_TAXONOMY.getIndex(),
                TEMPLATE_ID,
                GEO,
                DistributionChannelsEnum.GHS.getChannelName(),
                ResponseTypesEnum.TAXONOMY.getType(),
                new JsonObject().put("superDepartment", "Health & Beauty"),
                handle -> {
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
        browseService.getBrowseResults(IndicesEnum.GHS_TAXONOMY.getIndex(),
                TEMPLATE_ID,
                GEO,
                DistributionChannelsEnum.GHS.getChannelName(),
                ResponseTypesEnum.TAXONOMY.getType(),
                new JsonObject().put("department", "Haircare"),
                handle -> {
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
        browseService.getBrowseResults(IndicesEnum.GHS_TAXONOMY.getIndex(),
                TEMPLATE_ID,
                GEO,
                DistributionChannelsEnum.GHS.getChannelName(),
                ResponseTypesEnum.TAXONOMY.getType(),
                new JsonObject().put("aisle", "Gift Sets"),
                handle -> {
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
        browseService.getBrowseResults(IndicesEnum.GHS_TAXONOMY.getIndex(),
                TEMPLATE_ID,
                GEO,
                DistributionChannelsEnum.GHS.getChannelName(),
                ResponseTypesEnum.TAXONOMY.getType(),
                new JsonObject().put("shelf", "Womens Gift Sets"),
                handle -> {
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
        browseService.getBrowseResults(IndicesEnum.GHS_TAXONOMY.getIndex(),
                TEMPLATE_ID,
                GEO,
                DistributionChannelsEnum.GHS.getChannelName(),
                ResponseTypesEnum.TAXONOMY.getType(),
                new JsonObject().put("superDepartment", "NonExistentSuperDepartment"),
                handle -> {
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
        browseService.getBrowseResults(IndicesEnum.GHS_TAXONOMY.getIndex(),
                TEMPLATE_ID,
                GEO,
                DistributionChannelsEnum.GHS.getChannelName(),
                ResponseTypesEnum.TAXONOMY.getType(),
                new JsonObject().put("nonExistentFiler", "nonExistentFiler"),
                handle -> {
                    JsonObject response = handle.result();
                    response.getJsonObject("uk")
                            .getJsonObject("ghs")
                            .getJsonObject("taxonomy")
                            .getJsonArray("superDepartments")
                            .forEach(superDepartment -> {
                                JsonObject superDep = new JsonObject(Json.encode(superDepartment));
                                if (superDep.getString("name").equals("Health & Beauty")) {
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
                                }
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

    @Test
    public void testBrowseWithProductsResultsDefaultQuery(TestContext testContext) {
        Async async = testContext.async();
        browseService.getBrowseResults(IndicesEnum.GHS_PRODUCTS.getIndex(),
                TEMPLATE_ID,
                GEO,
                DistributionChannelsEnum.GHS.getChannelName(),
                ResponseTypesEnum.PRODUCTS.getType(),
                defaults,
                handle -> {
                    JsonObject response = handle.result();
                    testContext.assertEquals(response.getJsonObject("uk")
                            .getJsonObject("ghs").getJsonObject("products")
                            .getJsonObject("totals")
                            .getInteger("all"), 369);
                    async.complete();
                });
    }

    @Test
    public void testDefaultRequestWithFullResponseSet(TestContext testContext) {
        defaults.put("fields", "\"" + "name" + "\"," + "\"" + "price" + "\"")
                .put("limit", "10")
                .put("results", "true")
                .put("totals", "true")
                .put("suggestions", "true")
                .put("taxonomy", "true");
        Async async = testContext.async();
        browseService.getBrowseResults(IndicesEnum.GHS_PRODUCTS.getIndex(),
                TEMPLATE_ID,
                GEO,
                DistributionChannelsEnum.GHS.getChannelName(),
                ResponseTypesEnum.PRODUCTS.getType(),
                defaults,
                handle -> {
                    JsonObject response = handle.result();
                    testContext.assertEquals(response.getJsonObject("uk")
                            .getJsonObject("ghs").getJsonObject("products")
                            .getJsonObject("totals")
                            .getInteger("all"), 369);
                    testContext.assertEquals(response.getJsonObject("uk")
                                    .getJsonObject("ghs").getJsonObject("products")
                                    .getJsonArray("results").size(), 10);
                    async.complete();
                });
    }

    @Test
    public void testRequestFilteringNonMatchingSuperDepartment(TestContext testContext) {
        defaults.put("fields", "\"" + "name" + "\"," + "\"" + "price" + "\"")
                .put("superDepartment", "Some String")
                .put("limit", "10")
                .put("results", "true")
                .put("totals", "true")
                .put("taxonomy", "true");
        Async async = testContext.async();
        browseService.getBrowseResults(IndicesEnum.GHS_PRODUCTS.getIndex(),
                TEMPLATE_ID,
                GEO,
                DistributionChannelsEnum.GHS.getChannelName(),
                ResponseTypesEnum.PRODUCTS.getType(),
                defaults,
                handle -> {
                    JsonObject response = handle.result();
                    testContext.assertEquals(response.getJsonObject("uk")
                            .getJsonObject("ghs").getJsonObject("products")
                            .getJsonObject("totals")
                            .getInteger("all"), 0);
                    testContext.assertEquals(response.getJsonObject("uk")
                            .getJsonObject("ghs").getJsonObject("products")
                            .getJsonArray("results").size(), 0);
                    async.complete();
                });
    }

    @Test
    public void testRequestFilteringMatchingSuperDepartment(TestContext testContext) {
        defaults.put("fields", "\"" + "name" + "\"," + "\"" + "price" + "\"")
                .put("superDepartment", "Fresh Food")
                .put("limit", "10")
                .put("results", "true")
                .put("totals", "true")
                .put("taxonomy", "true");
        Async async = testContext.async();
        browseService.getBrowseResults(IndicesEnum.GHS_PRODUCTS.getIndex(),
                TEMPLATE_ID,
                GEO,
                DistributionChannelsEnum.GHS.getChannelName(),
                ResponseTypesEnum.PRODUCTS.getType(),
                defaults,
                handle -> {
                    JsonObject response = handle.result();
                    testContext.assertEquals(response.getJsonObject("uk")
                            .getJsonObject("ghs")
                            .getJsonObject("products")
                            .getJsonObject("taxonomy")
                            .getJsonArray("superDepartments").size(), 1);
                    async.complete();
                });
    }

    @Test
    public void testRequestFilteringMatchingShelf(TestContext testContext) {
        defaults.put("fields", "\"" + "name" + "\"," + "\"" + "price" + "\"")
                .put("shelf", "Whole Milk")
                .put("limit", "10")
                .put("results", "true")
                .put("totals", "true")
                .put("taxonomy", "true");
        Async async = testContext.async();
        browseService.getBrowseResults(IndicesEnum.GHS_PRODUCTS.getIndex(),
                TEMPLATE_ID,
                GEO,
                DistributionChannelsEnum.GHS.getChannelName(),
                ResponseTypesEnum.PRODUCTS.getType(),
                defaults,
                handle -> {
                    JsonObject response = handle.result();
                    testContext.assertEquals(response.getJsonObject("uk")
                            .getJsonObject("ghs")
                            .getJsonObject("products")
                            .getJsonObject("totals")
                            .getInteger("all"), 2);
                    testContext.assertEquals(response.getJsonObject("uk")
                            .getJsonObject("ghs")
                            .getJsonObject("products")
                            .getJsonObject("taxonomy")
                            .getJsonArray("superDepartments").getJsonObject(0)
                            .getJsonArray("departments").getJsonObject(0)
                            .getJsonArray("aisles").getJsonObject(0)
                            .getJsonArray("shelves").getJsonObject(0)
                            .getString("name"), "Whole Milk");
                    async.complete();
                });
    }

    @Test
    public void testRequestWithoutTaxonomy(TestContext testContext) {
        defaults.put("fields", "\"" + "name" + "\"," + "\"" + "price" + "\"")
                .put("shelf", "stuff")
                .put("limit", "10")
                .put("results", "true")
                .put("totals", "true");
        Async async = testContext.async();
        browseService.getBrowseResults(IndicesEnum.GHS_PRODUCTS.getIndex(),
                TEMPLATE_ID,
                GEO,
                DistributionChannelsEnum.GHS.getChannelName(),
                ResponseTypesEnum.PRODUCTS.getType(),
                defaults,
                handle -> {
                    JsonObject response = handle.result();
                    testContext.assertEquals(response.getJsonObject("uk")
                            .getJsonObject("ghs")
                            .getJsonObject("products")
                            .getJsonObject("taxonomy"), null);
                    async.complete();
                });
    }

    @Test
    public void testRequestWithoutTotals(TestContext testContext) {
        defaults.put("fields", "\"" + "name" + "\"," + "\"" + "price" + "\"")
                .put("shelf", "stuff")
                .put("limit", "10")
                .put("results", "true")
                .put("taxonomy", "true");
        defaults.remove("totals");
        Async async = testContext.async();
        browseService.getBrowseResults(IndicesEnum.GHS_PRODUCTS.getIndex(),
                TEMPLATE_ID,
                GEO,
                DistributionChannelsEnum.GHS.getChannelName(),
                ResponseTypesEnum.PRODUCTS.getType(),
                defaults,
                handle -> {
                    JsonObject response = handle.result();
                    testContext.assertEquals(response.getJsonObject("uk")
                            .getJsonObject("ghs")
                            .getJsonObject("products")
                            .getJsonObject("totals"), null);
                    async.complete();
                });
    }

    @Test
    public void testRequestBrandFilter(TestContext testContext) {
        defaults.put("fields", "\"" + "name" + "\"," + "\"" + "brand" + "\"")
                .put("brands", "Tesco")
                .put("limit", "10")
                .put("results", "true")
                .put("filters", "true")
                .put("totals", "true");
        Async async = testContext.async();
        browseService.getBrowseResults(IndicesEnum.GHS_PRODUCTS.getIndex(),
                TEMPLATE_ID,
                GEO,
                DistributionChannelsEnum.GHS.getChannelName(),
                ResponseTypesEnum.PRODUCTS.getType(),
                defaults,
                handle -> {
                    JsonObject response = handle.result();
                    testContext.assertEquals(response.getJsonObject("uk")
                            .getJsonObject("ghs")
                            .getJsonObject("products")
                            .getJsonObject("totals")
                            .getInteger("all"), 83);
                    testContext.assertEquals(response.getJsonObject("uk")
                            .getJsonObject("ghs")
                            .getJsonObject("products")
                            .getJsonObject("filters")
                            .getJsonArray("brands").getJsonObject(0)
                            .getString("name"), "Tesco");
                    async.complete();
                });
    }

    @Test
    public void testRequestBrandResponseSet(TestContext testContext) {
        defaults.put("fields", "\"" + "name" + "\"," + "\"" + "brand" + "\"")
                .put("limit", "10")
                .put("results", "true")
                .put("filters", "true")
                .put("totals", "true");
        Async async = testContext.async();
        browseService.getBrowseResults(IndicesEnum.GHS_PRODUCTS.getIndex(),
                TEMPLATE_ID,
                GEO,
                DistributionChannelsEnum.GHS.getChannelName(),
                ResponseTypesEnum.PRODUCTS.getType(),
                defaults,
                handle -> {
                    JsonObject response = handle.result();
                    testContext.assertEquals(response.getJsonObject("uk")
                            .getJsonObject("ghs")
                            .getJsonObject("products")
                            .getJsonObject("totals")
                            .getInteger("all"), 369);
                    testContext.assertEquals(response.getJsonObject("uk")
                            .getJsonObject("ghs")
                            .getJsonObject("products")
                            .getJsonObject("filters")
                            .getJsonArray("brands").getJsonObject(0)
                            .getString("name"), "Tesco");
                    async.complete();
                });
    }

    @AfterClass
    public static void tearDown() {
        shutdownEmbeddedElasticsearchServer();
    }
}
