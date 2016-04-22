package com.tesco.disco.browse.integration.browse.controller;

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
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by bruno on 21/04/16.
 */
@RunWith(VertxUnitRunner.class)
@Ignore
public class BrowseControllerTest extends AbstractElasticsearchTestVerticle implements BrowseTest {
    private static Vertx vertx;
    private HttpClient httpClient;

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

        Async asyncController = testContext.async();

        vertx.deployVerticle(controllerVerticleConfig.getString("main"), new DeploymentOptions(controllerVerticleConfig.getJsonObject("options")), res -> {
            if (res.succeeded()) {
                System.out.println("Deployment id is: " + res.result());
                asyncController.complete();
            } else {
                System.out.println("Deployment failed!");
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
        List<String> shelves = new ArrayList<String>();
        JsonObject response = httpClient.get("/browse/?department=Haircare").toObservable()
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
        testContext.assertEquals(shelves.size(), 6);
        List<String> expected = new ArrayList<String>();
        expected.add("Colour Conditioner");
        expected.add("Professional Shampoo");
        expected.add("Anti Dandruff Shampoo");
        expected.add("Kids Shampoo");
        expected.add("Professional Styling");
        expected.add("Blonde Shampoo & Conditioner");
        testContext.assertTrue(shelves.containsAll(expected));
    }

    @Test
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
        int status = httpClient.get("/browsy/?superDepartment=Something")
                .toObservable()
                .map(response -> {
                    return response.statusCode();
                }).toBlocking().single();
        testContext.assertEquals(404, status);
    }

    @Test
    public void testEmptyTaxonomyResponse(TestContext testContext) {
        JsonObject response = httpClient.get("/browse/?superDepartment=NonExistent")
                .toObservable()
                .flatMap(resp -> {
                    return resp.toObservable();
                })
                .map(respBody -> {
                    return new JsonObject(respBody.toString());
                }).toBlocking().single();
        testContext.assertTrue(response.getJsonObject("uk")
                .getJsonObject("ghs")
                .getJsonObject("taxonomy").isEmpty());
    }

    @Test
    public void testNonExistentFilter(TestContext testContext) {
        List<String> shelves = new ArrayList<String>();
        HttpClientRequest request = httpClient.get("/browse/");
        JsonObject response = request.toObservable()
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
        request.end();
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
    public void testStatus(TestContext testContext) {
        List<String> shelves = new ArrayList<String>();
        HttpClientRequest request = httpClient.get("/_status", handler -> {
            handler.statusCode();
        });

//        JsonObject response = request.toObservable()
//                .flatMap(resp -> {
//                    return resp.toObservable();
//                })
//                .map(respBody -> {
//                    return new JsonObject(respBody.toString());
//                }).toBlocking().single();
        request.end();
    }
}
