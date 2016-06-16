package com.tesco.disco.browse.integration.browse.controller;

import com.tesco.disco.browse.controller.BrowseController;
import com.tesco.disco.browse.integration.AbstractElasticsearchTestVerticle;
import com.tesco.disco.browse.integration.browse.BrowseTest;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.core.http.HttpClient;
import org.apache.commons.io.IOUtils;
import org.hamcrest.Matchers;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;

/**
 * Created by bruno on 21/04/16.
 */
@RunWith(VertxUnitRunner.class)
public class BrowseControllerTest extends AbstractElasticsearchTestVerticle implements BrowseTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(BrowseController.class);
    private static Vertx vertx;
    private HttpClient httpClient;

    @BeforeClass
    public static void setup(TestContext testContext) throws IOException {
        Async async = testContext.async();
        vertx = Vertx.vertx();
        vertx.deployVerticle(AbstractElasticsearchTestVerticle.class.getName(), res -> {
            if (res.succeeded()) {
                LOGGER.debug("Deployment id is: " + res.result());
                async.complete();
            } else {
                LOGGER.debug("Deployment failed!");
                async.complete();
            }
        });
        String testConfig = IOUtils.toString(Thread.currentThread().getContextClassLoader().getResource("config/application-test.json"));
        JsonObject serviceVerticleConfig = new JsonObject(Json.encode(new JsonObject(testConfig).getJsonArray("verticles").getList().get(0)));
        JsonObject controllerVerticleConfig = new JsonObject(Json.encode(new JsonObject(testConfig).getJsonArray("verticles").getList().get(1)));

        Async asyncService = testContext.async();

        vertx.deployVerticle(serviceVerticleConfig.getString("main"), new DeploymentOptions(serviceVerticleConfig.getJsonObject("options")), res -> {
            if (res.succeeded()) {
                LOGGER.debug("Deployment id is: " + res.result());
                asyncService.complete();
            } else {
                LOGGER.debug("Deployment failed!");
                asyncService.complete();
            }
        });

        Async asyncController = testContext.async();

        vertx.deployVerticle(controllerVerticleConfig.getString("main"), new DeploymentOptions(controllerVerticleConfig.getJsonObject("options")), res -> {
            if (res.succeeded()) {
                LOGGER.debug("Deployment id is: " + res.result());
                asyncController.complete();
            } else {
                LOGGER.debug("Deployment failed!");
                asyncController.complete();
            }
        });
    }

    @Before
    public void testInit() {
        httpClient = vertx.createHttpClient(new HttpClientOptions()
                .setDefaultHost("localhost")
                .setDefaultPort(9003));
    }

    @Test
    public void testGenericBrowse(TestContext testContext) {
        given().port(9003)
                .when().get("/taxonomy/")
                .then()
                .body("uk.ghs.taxonomy.superDepartments.size()", greaterThan(1))
                .body("uk.ghs.taxonomy.superDepartments[0].departments[0].aisles[0].shelves.name",
                        hasItems("Anti Dandruff Shampoo", "Kids Shampoo", "Professional Shampoo"))
                .body("uk.ghs.taxonomy.superDepartments[0].departments[0].aisles[1].shelves.name",
                        hasItems("Blonde Shampoo & Conditioner"))
                .body("uk.ghs.taxonomy.superDepartments[0].departments[0].aisles[2].shelves.name",
                        hasItems("Colour Conditioner"))
                .body("uk.ghs.taxonomy.superDepartments[0].departments[0].aisles[3].shelves.name",
                        hasItems("Professional Styling"))
                .body("uk.ghs.taxonomy.superDepartments[0].departments[1].aisles[0].shelves.name",
                        hasItems("Womens Gift Sets"))
                .body("uk.ghs.taxonomy.superDepartments[0].departments[1].aisles[1].shelves.name",
                        hasItems("Tesco Shower Gel"))
                .body("uk.ghs.taxonomy.superDepartments[0].departments[1].aisles[2].shelves.name",
                        hasItems("Travel Sizes"));
    }

    @Test
    public void testBrowseWithSuperDeparmentFilter(TestContext testContext) {
        given().port(9003)
                .when().get("/taxonomy/?superDepartment=Health%20%26%20Beauty")
                .then()
                .body("uk.ghs.taxonomy.superDepartments[0].departments[0].aisles[0].shelves.name",
                        hasItems("Anti Dandruff Shampoo", "Kids Shampoo", "Professional Shampoo"))
                .body("uk.ghs.taxonomy.superDepartments[0].departments[0].aisles[1].shelves.name",
                        hasItems("Blonde Shampoo & Conditioner"))
                .body("uk.ghs.taxonomy.superDepartments[0].departments[0].aisles[2].shelves.name",
                        hasItems("Colour Conditioner"))
                .body("uk.ghs.taxonomy.superDepartments[0].departments[0].aisles[3].shelves.name",
                        hasItems("Professional Styling"))
                .body("uk.ghs.taxonomy.superDepartments[0].departments[1].aisles[0].shelves.name",
                        hasItems("Womens Gift Sets"))
                .body("uk.ghs.taxonomy.superDepartments[0].departments[1].aisles[1].shelves.name",
                        hasItems("Tesco Shower Gel"))
                .body("uk.ghs.taxonomy.superDepartments[0].departments[1].aisles[2].shelves.name",
                        hasItems("Travel Sizes"));
    }

    @Test
    public void testBrowseWithDeparmentFilter(TestContext testContext) {
        given().port(9003)
                .when().get("/taxonomy/?department=Haircare")
                .then()
                .body("uk.ghs.taxonomy.superDepartments[0].departments[0].aisles[0].shelves.name",
                        hasItems("Anti Dandruff Shampoo", "Kids Shampoo", "Professional Shampoo"))
                .body("uk.ghs.taxonomy.superDepartments[0].departments[0].aisles[1].shelves.name",
                        hasItems("Blonde Shampoo & Conditioner"))
                .body("uk.ghs.taxonomy.superDepartments[0].departments[0].aisles[2].shelves.name",
                        hasItems("Colour Conditioner"))
                .body("uk.ghs.taxonomy.superDepartments[0].departments[0].aisles[3].shelves.name",
                        hasItems("Professional Styling"));
    }

    @Test
    public void testBrowseWithAisleFilter(TestContext testContext) {
        given().port(9003)
                .when().get("/taxonomy/?aisle=Gift%20Sets")
                .then()
                .body("uk.ghs.taxonomy.superDepartments[0].departments[0].aisles[0].shelves.name",
                        hasItems("Womens Gift Sets"));
    }

    @Test
    public void testBrowseWithShelfFilter(TestContext testContext) {
        given().port(9003)
                .when().get("/taxonomy/?shelf=Womens%20Gift%20Sets")
                .then()
                .body("uk.ghs.taxonomy.superDepartments[0].departments[0].aisles[0].shelves.name",
                        hasItems("Womens Gift Sets"));
    }

    @Test
    public void testIncorrectBrowseEndpoint(TestContext testContext) {
        given().port(9003)
                .when().get("/browsy/?superDepartment=Something")
                .then()
                .statusCode(404);
    }

    @Test
    public void testEmptyTaxonomyResponse(TestContext testContext) {
        given().port(9003)
                .when().get("/taxonomy/?superDepartment=NonExistent")
                .then()
                .body("uk.ghs.taxonomy.keySet()", Matchers.emptyIterable());
    }

    @Test
    public void testNonExistentFilter(TestContext testContext) {
        given().port(9003)
                .when().get("/taxonomy/?nonExistentFilter=something")
                .then()
                .body("uk.ghs.taxonomy.superDepartments[0].departments[0].aisles[0].shelves.name",
                        hasItems("Anti Dandruff Shampoo", "Kids Shampoo", "Professional Shampoo"))
                .body("uk.ghs.taxonomy.superDepartments[0].departments[0].aisles[1].shelves.name",
                        hasItems("Blonde Shampoo & Conditioner"))
                .body("uk.ghs.taxonomy.superDepartments[0].departments[0].aisles[2].shelves.name",
                        hasItems("Colour Conditioner"))
                .body("uk.ghs.taxonomy.superDepartments[0].departments[0].aisles[3].shelves.name",
                        hasItems("Professional Styling"))
                .body("uk.ghs.taxonomy.superDepartments[0].departments[1].aisles[0].shelves.name",
                        hasItems("Womens Gift Sets"))
                .body("uk.ghs.taxonomy.superDepartments[0].departments[1].aisles[1].shelves.name",
                        hasItems("Tesco Shower Gel"))
                .body("uk.ghs.taxonomy.superDepartments[0].departments[1].aisles[2].shelves.name",
                        hasItems("Travel Sizes"));
    }

    @Test
    public void testBrowseWithProductsResultsDefaultQuery() {
        given().port(9003)
                .when().get("/browse/")
                .then()
                .statusCode(200)
                .body("uk.ghs.products.totals.all", is(369))
                .body("uk.ghs.products.results.size()", is(10));
    }

    @Test
    public void testDefaultRequestWithFullResponseSet() {
        given().port(9003)
                .when().get("/browse/?fields=name,price&limit=10&responseSet=results,totals,suggestions,taxonomy")
                .then()
                .statusCode(200)
                .body("uk.ghs.products.totals.all", is(369))
                .body("uk.ghs.products.results.size()", is(10));
    }

    @Test
    public void testRequestFilteringNonMatchingSuperDepartment() {
        given().port(9003)
                .when().get("/browse/?fields=name,price&limit=10&superDepartment=Some+String&responseSet=results,totals,taxonomy")
                .then()
                .statusCode(200)
                .body("uk.ghs.products.totals.all", is(0))
                .body("uk.ghs.products.results.size()", is(0));
    }

    @Test
    public void testRequestFilteringMatchingSuperDepartment() {
        given().port(9003)
                .when().get("/browse/?fields=name,price&limit=10&superDepartment=Fresh+Food&responseSet=results,totals,taxonomy")
                .then()
                .statusCode(200)
                .body("uk.ghs.products.taxonomy.superDepartments.size()", is(1));
    }

    @Test
    public void testRequestFilteringMatchingShelf() {
        given().port(9003)
                .when().get("/browse/?fields=name,price&limit=10&shelf=Whole+Milk&responseSet=results,totals,taxonomy")
                .then()
                .statusCode(200)
                .body("uk.ghs.products.totals.all", is(2))
                .body("uk.ghs.products.taxonomy.superDepartments[0].departments[0].aisles[0].shelves[0].name", is("Whole Milk"));
    }

    @Test
    public void testRequestWithoutTaxonomy() {
        given().port(9003)
                .when().get("/browse/?fields=name,price&limit=10&shelf=stuff&responseSet=results,totals")
                .then()
                .statusCode(200)
                .body("uk.ghs.products.taxonomy", Matchers.nullValue());
    }

    @Test
    public void testRequestWithoutTotals() {
        given().port(9003)
                .when().get("/browse/?fields=name,price&limit=10&shelf=stuff&responseSet=results,taxonomy")
                .then()
                .statusCode(200)
                .body("uk.ghs.products.totals", Matchers.nullValue());
    }

    @Test
    public void testRequestBrandFilter() {
        given().port(9003)
                .when().get("/browse/?fields=name,brand&brand=Tesco&responseSet=results,totals,filters")
                .then()
                .statusCode(200)
                .body("uk.ghs.products.totals.all", is(83))
                .body("uk.ghs.products.filters.brands.name", hasItems("Tesco"));
    }

    @Test
    public void testRequestBrandResponseSet() {
        given().port(9003)
                .when().get("/browse/?fields=name,brand&responseSet=results,totals,filters")
                .then()
                .statusCode(200)
                .body("uk.ghs.products.totals.all", is(369))
                .body("uk.ghs.products.filters.brands[0].name", is("Tesco"));
    }

    @Test
    public void testRequestRDGResponseTest() {
        given().port(9003)
                .when().get("/browse/?superDepartment=Drinks&department=Wine&aisle=Red+Wine&sh\n" +
                "\n" +
                "elf=Other+Red+Wines&fields=name,DeliveryRestrictions&pretty=true&store=2007&responseSet=results&brand=Tesco")
                .then()
                .statusCode(200);
    }

    @Test
    public void testRequestValidateRDGResponseTest(TestContext context) {
        JsonObject body = new JsonObject(given().port(9003)
                .when().get("/browse/?superDepartment=Drinks&department=Wine&aisle=Red+Wine&shelf=Other+Red+Wines&fields=name,DeliveryRestrictions&pretty=true&store=2007&responseSet=results&brand=Tesco")
                .thenReturn().body().print());
        JsonArray results = body.getJsonObject("uk").getJsonObject("ghs").getJsonObject("products").getJsonArray("results");
        results.stream().forEach(product -> {
            JsonObject prod = new JsonObject(Json.encode(product));
            context.assertNotNull(prod.getString("name"));
            context.assertNotNull(prod.getJsonObject("DeliveryRestrictions"));
        });
    }

    @Test
    public void testRequestWithAllFields(TestContext context) {
        JsonObject body = new JsonObject(given().port(9003)
                .when().get("/browse/?fields=tpnb,unitprice,price,name,availability&responseSet=results")
                .thenReturn().body().print());
        JsonArray results = body.getJsonObject("uk").getJsonObject("ghs").getJsonObject("products").getJsonArray("results");
        results.stream().forEach(product -> {
            JsonObject prod = new JsonObject(Json.encode(product));
            context.assertNotNull(prod.getLong("availability"));
            context.assertNotNull(prod.getDouble("unitprice"));
            context.assertNotNull(prod.getDouble("price"));
        });
    }

    @Test
    public void testRequestFieldsWithNonExistentFieldValue(TestContext context) {
        given().port(9003)
                .when().get("/browse/?fields=fasfasfasf")
                .then()
                .statusCode(400)
                .body("error", is("Incorrect Fields specified!"));
    }
    
    @Test
    public void testRequestFieldsWithZeroOffsetValue(TestContext context) {
        given().port(9003)
                .when().get("/browse/?offset=0")
                .then()
                .statusCode(400)
                .body("error", is("Incorrect offset type!"));
    }

    @Test
    public void testRequestFieldsWithInvalidoffsetValue(TestContext context) {
        given().port(9003)
                .when().get("/browse/?offset=1000000")
                .then()
                .statusCode(400)
                .body("error", is("Incorrect offset type!"));
    }
    
    @Test
    public void testRequestFieldsWithValidoffsetValue(TestContext context) {
        given().port(9003)
                .when().get("/browse/?offset=14")
                .then()
                .statusCode(200);               
    }

    
    @Test
    public void testRequestEmptyFields(TestContext context) {
        JsonObject body = new JsonObject(given().port(9003)
                .when().get("/browse/?fields=")
                .thenReturn().body().print());
        JsonArray results = body.getJsonObject("uk").getJsonObject("ghs").getJsonObject("products").getJsonArray("results");
        results.stream().forEach(product -> {
            JsonObject prod = new JsonObject(Json.encode(product));
            context.assertNotNull(prod.getString("tpnb"));
            context.assertNull(prod.getDouble("BulkBuyLimit"));
            context.assertNull(prod.getDouble("popularity"));
        });
    }

    @AfterClass
    public static void tearDown() {
        shutdownEmbeddedElasticsearchServer();
    }
}