package com.tesco.disco.browse.integration.browse.controller;

import com.tesco.disco.browse.integration.AbstractElasticsearchTestVerticle;
import com.tesco.disco.browse.integration.browse.BrowseTest;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.core.http.HttpClient;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bruno on 21/04/16.
 */
public class BrowseControllerTest extends AbstractElasticsearchTestVerticle implements BrowseTest {
    private static Vertx vertx;
    private HttpClient httpClient;

    @BeforeClass
    public static void setup() {
        vertx = Vertx.vertx();
    }

    @Before
    public void testInit() {
        httpClient = vertx.createHttpClient(new HttpClientOptions()
                .setDefaultHost("localhost")
                .setDefaultPort(9003));
    }

    @Test
    public void testGenericBrowse(TestContext testContext) {
        List<String> shelves = new ArrayList<String>();
        JsonObject response = httpClient.get("/browse/").toObservable()
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
    }

    @Test
    public void testBrowseWithSuperDeparmentFilter(TestContext testContext) {
        List<String> shelves = new ArrayList<String>();
        JsonObject response = httpClient.get("/browse/?superDepartment=Health%20&%20Beauty").toObservable()
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
    }

    @Test
    public void testBrowseWithDeparmentFilter(TestContext testContext) {
        Async async = testContext.async();
        httpClient.get("/browse/?superDepartment=");
    }

    @Test
    public void testBrowseWithAisleFilter(TestContext testContext) {
        Async async = testContext.async();
        httpClient.get("/browse/?superDepartment=");
    }

    @Test
    public void testBrowseWithShelfFilter(TestContext testContext) {
        Async async = testContext.async();
        httpClient.get("/browse/?superDepartment=");
    }

    @Test
    public void testIncorrectBrowseEndpoint(TestContext testContext) {
        Async async = testContext.async();
        httpClient.get("/browsy/?superDepartment=Something");
    }

    @Test
    public void testEmptyTaxonomyResponse(TestContext testContext) {
        Async async = testContext.async();
        httpClient.get("/browse/?superDepartment=");
    }

    @Test
    public void testNonExistentFilter(TestContext testContext) {
        Async async = testContext.async();
        httpClient.get("/browse/?superDepartment=");
    }
}
