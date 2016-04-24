package com.tesco.disco.browse.integration.browse.controller;

import com.jayway.restassured.specification.RequestSpecification;
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
import io.vertx.rxjava.core.http.HttpClientRequest;
import io.vertx.rxjava.core.http.HttpServerRequest;
import org.apache.commons.io.IOUtils;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.when;
import static org.hamcrest.Matchers.hasItems;

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
            }
        });

        Async asyncController = testContext.async();

        vertx.deployVerticle(controllerVerticleConfig.getString("main"), new DeploymentOptions(controllerVerticleConfig.getJsonObject("options")), res -> {
            if (res.succeeded()) {
                LOGGER.debug("Deployment id is: " + res.result());
                asyncController.complete();
            } else {
                LOGGER.debug("Deployment failed!");
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
                .when().get("/browse")
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
                .when().get("/browse?superDepartment=Health%20&%20Beauty")
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
                .when().get("/browse?department=Haircare")
                .then()
                .body("uk.ghs.taxonomy.superDepartments[0].departments[0].aisles[0].shelves.name",
                        hasItems("Blonde Shampoo & Conditioner"))
                .body("uk.ghs.taxonomy.superDepartments[0].departments[0].aisles[1].shelves.name",
                        hasItems("Colour Conditioner"))
                .body("uk.ghs.taxonomy.superDepartments[0].departments[0].aisles[2].shelves.name",
                        hasItems("Professional Styling"))
                .body("uk.ghs.taxonomy.superDepartments[0].departments[1].aisles[0].shelves.name",
                        hasItems("Womens Gift Sets"))
                .body("uk.ghs.taxonomy.superDepartments[0].departments[1].aisles[1].shelves.name",
                        hasItems("Tesco Shower Gel"))
                .body("uk.ghs.taxonomy.superDepartments[0].departments[1].aisles[2].shelves.name",
                        hasItems("Travel Sizes"));
//
//
//        List<String> shelves = new ArrayList<String>();
//        JsonObject response = httpClient.get("/browse/?department=Haircare").toObservable()
//                .flatMap(resp -> {
//                    return resp.toObservable();
//                })
//                .map(respBody -> {
//                    return new JsonObject(respBody.toString());
//                }).toBlocking().single();
//        response.getJsonObject("uk")
//                .getJsonObject("ghs")
//                .getJsonObject("taxonomy")
//                .getJsonArray("superDepartments")
//                .forEach(superDepartment -> {
//                    JsonObject superDep = new JsonObject(Json.encode(superDepartment));
//                    superDep.getJsonArray("departments")
//                            .forEach(department -> {
//                                JsonObject jsonDep = new JsonObject(Json.encode(department));
//                                jsonDep.getJsonArray("aisles")
//                                        .forEach(aisle -> {
//                                            JsonObject jsonAisle = new JsonObject(Json.encode(aisle));
//                                            jsonAisle.getJsonArray("shelf")
//                                                    .forEach(shelf -> {
//                                                        shelves.add(new JsonObject(Json.encode(shelf)).getString("name"));
//                                                    });
//                                        });
//                            });
//                });
//        testContext.assertEquals(shelves.size(), 6);
//        List<String> expected = new ArrayList<String>();
//        expected.add("Colour Conditioner");
//        expected.add("Professional Shampoo");
//        expected.add("Anti Dandruff Shampoo");
//        expected.add("Kids Shampoo");
//        expected.add("Professional Styling");
//        expected.add("Blonde Shampoo & Conditioner");
//        testContext.assertTrue(shelves.containsAll(expected));
    }

    @Test
    @Ignore
    public void testBrowseWithAisleFilter(TestContext testContext) {
        List<String> shelves = new ArrayList<String>();
        JsonObject response = httpClient.get("/browse/?aisle=Gift%20Sets").toObservable()
                .flatMap(resp -> {
                    return resp.toObservable();
                })
                .map(respBody -> {
                    return new JsonObject(respBody.toString());
                }).toBlocking().single();
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
                                            jsonAisle.getJsonArray("shelf")
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
    }

    @Test
    @Ignore
    public void testBrowseWithShelfFilter(TestContext testContext) {
        List<String> shelves = new ArrayList<String>();
        JsonObject response = httpClient.get("/browse/?shelf==Womens%20Gift%20Sets").toObservable()
                .flatMap(resp -> {
                    return resp.toObservable();
                })
                .map(respBody -> {
                    return new JsonObject(respBody.toString());
                }).toBlocking().single();
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
                                            jsonAisle.getJsonArray("shelf")
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
    }

    @Test
    public void testIncorrectBrowseEndpoint(TestContext testContext) {
        given().port(9003)
                .when().get("/browsy?superDepartment=Something")
                .then()
                .statusCode(404);
    }

    @Test
    public void testEmptyTaxonomyResponse(TestContext testContext) {
        given().port(9003)
                .when().get("/browse?superDepartment=NonExistent")
                .then()
                .body("uk.ghs.taxonomy", Matchers.anEmptyMap());
    }

    @Test
    public void testNonExistentFilter(TestContext testContext) {
        given().port(9003)
                .when().get("/browse?nonExistentFilter=something")
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
}
