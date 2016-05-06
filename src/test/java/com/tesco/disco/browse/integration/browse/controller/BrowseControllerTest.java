package com.tesco.disco.browse.integration.browse.controller;

import com.tesco.disco.browse.controller.BrowseController;
import com.tesco.disco.browse.integration.AbstractElasticsearchTestVerticle;
import com.tesco.disco.browse.integration.browse.BrowseTest;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.core.http.HttpClient;
import org.apache.commons.io.IOUtils;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static com.jayway.restassured.RestAssured.given;
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
                .when().get("/browse/")
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
    public void testBrowseWithSuperDeparmentFilter(TestContext testContext) {
        given().port(9003)
                .when().get("/browse/?superDepartment=Health%20%26%20Beauty")
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
                .when().get("/browse/?department=Haircare")
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
                .when().get("/browse/?aisle=Gift%20Sets")
                .then()
                .body("uk.ghs.taxonomy.superDepartments[0].departments[0].aisles[0].shelves.name",
                        hasItems("Womens Gift Sets"));
    }

    @Test
    public void testBrowseWithShelfFilter(TestContext testContext) {
        given().port(9003)
                .when().get("/browse/?shelf=Womens%20Gift%20Sets")
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
                .when().get("/browse?superDepartment=NonExistent")
                .then()
                .body("uk.ghs.taxonomy.keySet()", Matchers.emptyIterable());
    }

    @Test
    public void testNonExistentFilter(TestContext testContext) {
        given().port(9003)
                .when().get("/browse/?nonExistentFilter=something")
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
    public void testBrowseWithProductsResultsDefaultQuery(TestContext testContext) {
        String body = given().port(9003)
                .when().get("/browse/products/?fields=name&responseSet=results,taxonomy").andReturn().body().prettyPrint();
        testContext.assertTrue(!new JsonObject(body)
                .getJsonObject("uk")
                .getJsonObject("ghs")
                .getJsonObject("products").getJsonArray("results").isEmpty());
    }

    @Test
    public void testDefaultRequestWithFullResponseSet() {
        given().port(9003)
                .when().get("/browse/products/?fields=name,price&limit=10&responseSet=results,totals,suggestions,taxonomy")
                .then()
                .statusCode(200)
                .body("uk.ghs.products.totals.all", is(369))
                .body("uk.ghs.products.results.size()", is(10));
    }

    @Test
    public void testRequestFilteringNonMatchingSuperDepartment() {
        given().port(9003)
                .when().get("/browse/products/?fields=name,price&limit=10&superDepartment=Some+String&responseSet=results,totals,taxonomy")
                .then()
                .statusCode(200)
                .body("uk.ghs.products.totals.all", is(0))
                .body("uk.ghs.products.results.size()", is(0));
    }

    @Test
    public void testRequestFilteringMatchingSuperDepartment() {
        given().port(9003)
                .when().get("/browse/products/?fields=name,price&limit=10&superDepartment=Fresh+Food&responseSet=results,totals,taxonomy")
                .then()
                .statusCode(200)
                .body("uk.ghs.products.taxonomy.superDepartments.size()", is(1));
    }

//    @Test
//    public void testRequestFilteringMatchingShelf() {
//        String uri = "/search/?query=milk&fields=name,price&limit=10&shelf=Whole+Milk&responseSet=results,totals,suggestions,taxonomy";
//
//        HttpClientRequest req = vertx.createHttpClient().setPort(PORT).get(uri, (response) -> {
//            response.exceptionHandler(throwable -> {
//                fail("Error handling request! - " + throwable.getMessage());
//            });
//
//            assertEquals(200, response.statusCode());
//
//            response.bodyHandler((buffer) -> {
//                try {
//                    JsonObject responseBody = new JsonObject(buffer.toString());
//                    if (responseBody.getObject("uk").getObject("ghs").getObject("products").containsField("taxonomy")) {
//                        if (responseBody.getObject("uk").getObject("ghs").getObject("products").getObject("taxonomy").getArray("superDepartments").size() > 0) {
//                            JsonArray sds = responseBody.getObject("uk").getObject("ghs").getObject("products").getObject("taxonomy").getArray("superDepartments");
//                            for (Iterator it = sds.iterator(); it.hasNext();) {
//                                SuperDepartment sd = Json.decodeValue(((JsonObject)it.next()).encode(), SuperDepartment.class);
//                                sd.getDepartments().forEach(dep -> {
//                                    dep.getAisles().forEach(aisle -> {
//                                        aisle.getShelves().forEach(shelf -> {
//                                            if (shelf.getName().equals("Whole Milk")) {
//                                                testComplete();
//                                            }
//                                        });
//                                    });
//                                });
//                            }
//                            fail("Taxonomy doesn't contain the expected results!");
//                        } else {
//                            fail("Taxonomy doesn't contain the expected results!");
//                        }
//                    } else {
//                        fail("No taxonomy returned from the search!");
//                    }
//                } catch (Exception e) {
//                    fail("Error handling request! - " + e.getMessage());
//                }
//            });
//        });
//        req.headers().set("content-type", "application/json");
//        req.end();
//    }
//
//    //TODO - uncomment this test for multiple filters being passed in, as well as the TODO on Parameter.class -> convertMultiStrings
//    @Test
//    @Ignore
//    public void testRequestFilteringMatchingTwoSuperDepartment() {
//        String uri = "/search/?query=milk&fields=name,price&limit=10&superDepartment=Fresh+Food,Baby&responseSet=results,totals,suggestions,taxonomy";
//
//        HttpClientRequest req = vertx.createHttpClient().setPort(PORT).get(uri, (response) -> {
//            response.exceptionHandler(throwable -> {
//                fail("Error handling request! - " + throwable.getMessage());
//            });
//
//            assertEquals(200, response.statusCode());
//
//            response.bodyHandler((buffer) -> {
//                try {
//                    JsonObject responseBody = new JsonObject(buffer.toString());
//                    if (responseBody.getObject("uk").getObject("ghs").getObject("products").containsField("taxonomy")) {
//                        if (responseBody.getObject("uk").getObject("ghs").getObject("products").getObject("taxonomy").getArray("superDepartments").size() == 2) {
//                            testComplete();
//                        } else {
//                            fail("Taxonomy doesn't contain the expected results!");
//                        }
//                    } else {
//                        fail("No taxonomy returned from the search!");
//                    }
//                } catch (Exception e) {
//                    fail("Error handling request! - " + e.getMessage());
//                }
//            });
//        });
//        req.headers().set("content-type", "application/json");
//        req.end();
//    }
//
//    @Test
//    public void testRequestWithoutTaxonomy() {
//        String uri = "/search/?query=milk&fields=name,price&limit=10&shelf=stuff&responseSet=results,totals,suggestions";
//
//        HttpClientRequest req = vertx.createHttpClient().setPort(PORT).get(uri, (response) -> {
//            response.exceptionHandler(throwable -> {
//                fail("Error handling request! - " + throwable.getMessage());
//            });
//
//            assertEquals(200, response.statusCode());
//            response.bodyHandler((buffer) -> {
//                JsonObject responseBody =  new JsonObject(buffer.toString());
//                if (responseBody.containsField("taxonomy")) {
//                    fail("There shouldn't be any taxonomy in the result set!");
//                } else {
//                    testComplete();
//                }
//            });
//        });
//        req.headers().set("content-type", "application/json");
//        req.end();
//    }
//
//    @Test
//    public void testRequestWithoutResults() {
//        String uri = "/search/?query=milk&fields=name,price&limit=10&shelf=stuff&responseSet=taxonomy,totals,suggestions";
//
//        HttpClientRequest req = vertx.createHttpClient().setPort(PORT).get(uri, (response) -> {
//            response.exceptionHandler(throwable -> {
//                fail("Error handling request! - " + throwable.getMessage());
//            });
//
//            assertEquals(200, response.statusCode());
//            response.bodyHandler((buffer) -> {
//                JsonObject responseBody =  new JsonObject(buffer.toString());
//                if (responseBody.containsField("results")) {
//                    fail("There shouldn't be any taxonomy in the result set!");
//                } else {
//                    testComplete();
//                }
//            });
//        });
//        req.headers().set("content-type", "application/json");
//        req.end();
//    }
//
//    @Test
//    public void testRequestWithoutTotals() {
//        String uri = "/search/?query=milk&fields=name,price&limit=10&shelf=stuff&responseSet=results,taxonomy,suggestions";
//
//        HttpClientRequest req = vertx.createHttpClient().setPort(PORT).get(uri, (response) -> {
//            response.exceptionHandler(throwable -> {
//                fail("Error handling request! - " + throwable.getMessage());
//            });
//
//            assertEquals(200, response.statusCode());
//            response.bodyHandler((buffer) -> {
//                JsonObject responseBody =  new JsonObject(buffer.toString());
//                if (responseBody.containsField("totals")) {
//                    fail("There shouldn't be any taxonomy in the result set!");
//                } else {
//                    testComplete();
//                }
//            });
//        });
//        req.headers().set("content-type", "application/json");
//        req.end();
//    }
//
//    @Test
//    public void testRequestWithoutSuggestions() {
//        String uri = "/search/?query=milk&fields=name,price&limit=10&shelf=stuff&responseSet=results,totals,taxonomy";
//
//        HttpClientRequest req = vertx.createHttpClient().setPort(PORT).get(uri, (response) -> {
//            response.exceptionHandler(throwable -> {
//                fail("Error handling request! - " + throwable.getMessage());
//            });
//
//            assertEquals(200, response.statusCode());
//            response.bodyHandler((buffer) -> {
//                JsonObject responseBody =  new JsonObject(buffer.toString());
//                if (responseBody.containsField("suggestions")) {
//                    fail("There shouldn't be any taxonomy in the result set!");
//                } else {
//                    testComplete();
//                }
//            });
//        });
//        req.headers().set("content-type", "application/json");
//        req.end();
//    }
//
//    @Test
//    public void testRequestNoQueryNoFilter() {
//        String uri = "/search/";
//
//        HttpClientRequest req = vertx.createHttpClient().setPort(PORT).get(uri, (response) -> {
//            response.exceptionHandler(throwable -> {
//                fail("Error handling request! - " + throwable.getMessage());
//            });
//
//            assertEquals(400, response.statusCode());
//
//            response.bodyHandler((buffer) -> {
//                try {
//                    JsonObject responseBody = new JsonObject(buffer.toString());
//                    Assert.assertEquals("error", responseBody.getString("status"));
//                    testComplete();
//                } catch (Exception e) {
//                    fail("Error handling request! - " + e.getMessage());
//                }
//            });
//        });
//        req.headers().set("content-type", "application/json");
//        req.end();
//    }
//
//    @Test
//    public void testRequestNoQueryWithFilter() {
//        String uri = "/search/?fields=name,price&limit=10&superDepartment=Fresh+Food&responseSet=results,totals,suggestions,taxonomy";
//
//        HttpClientRequest req = vertx.createHttpClient().setPort(PORT).get(uri, (response) -> {
//            response.exceptionHandler(throwable -> {
//                fail("Error handling request! - " + throwable.getMessage());
//            });
//
//            assertEquals(200, response.statusCode());
//
//            response.bodyHandler((buffer) -> {
//                try {
//                    JsonObject responseBody = new JsonObject(buffer.toString());
//                    if (responseBody.getObject("uk").getObject("ghs").getObject("products").containsField("taxonomy")) {
//                        if (responseBody.getObject("uk").getObject("ghs").getObject("products").getObject("taxonomy").size() > 0) {
//                            testComplete();
//                        } else {
//                            fail("Taxonomy doesn't contain the expected results!");
//                        }
//                    } else {
//                        fail("No taxonomy returned from the search!");
//                    }
//                } catch (Exception e) {
//                    fail("Error handling request! - " + e.getMessage());
//                }
//            });
//        });
//        req.headers().set("content-type", "application/json");
//        req.end();
//    }
}