package com.tesco.disco.browse.integration.probe;

import com.tesco.disco.browse.controller.BrowseController;
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
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
	* Created by bruno on 26/04/16.
	*/
@RunWith(VertxUnitRunner.class)
public class ProbeTest {
				private static final Logger LOGGER = LoggerFactory.getLogger(BrowseController.class);
				private static Vertx vertx;
				private HttpClient httpClient;

				@BeforeClass
				public static void setup(TestContext testContext) throws IOException {
								vertx = Vertx.vertx();
								String testConfig = IOUtils.toString(Thread.currentThread().getContextClassLoader().getResource("config/application-test.json"));
								JsonObject probeVerticleConfig = new JsonObject(Json.encode(new JsonObject(testConfig).getJsonArray("verticles").getList().get(2)));

								Async asyncProbe = testContext.async();

								vertx.deployVerticle(probeVerticleConfig.getString("main"), new DeploymentOptions(probeVerticleConfig.getJsonObject("options")), res -> {
												if (res.succeeded()) {
																LOGGER.debug("Deployment id is: " + res.result());
																asyncProbe.complete();
												} else {
																LOGGER.debug("Deployment failed!");
												}
								});
				}

				@Before
				public void testInit() {
								httpClient = vertx.createHttpClient(new HttpClientOptions()
																.setDefaultHost("localhost")
																.setDefaultPort(8091));
				}

				@Test
				public void shouldReturnProbeStatusDisabled(TestContext context) {
								Async async = context.async();
								httpClient.get("/", response -> {
												context.assertEquals(400, response.statusCode());
												response.bodyHandler(body -> {
																String actual = body.toString();
																String expected = "This server probe status is false";
																context.assertEquals(expected, actual, "Probe server should start disabled");
																async.complete();
												});
								})
								.end();
				}

				@Test
				@Ignore
				public void shouldReturnProbeStatusEnabled(TestContext context) {
//								Async async = context.async();
//								HttpClientRequest request = httpClient.get("/", response -> {
//												context.assertEquals(400, response.statusCode());
//												ObservableHandler<Buffer> handleBody = RxHelper.observableHandler();
//												response.bodyHandler(handleBody.toHandler());
//            handleBody
//																				.flatMap()
//
//
//																				body -> {
//																String actual = body.toString();
//																String expected = "This server probe status is false";
//																context.assertEquals(expected, actual, "Probe server should start disabled");
//
//												});
//								});
//								HttpClientRequest request = vertx.createHttpClient().setPort(8090).exceptionHandler(handler).get("/", new Handler<HttpClientResponse>() {
//
//												@Override
//												public void handle(HttpClientResponse response) {
//
//																assertEquals(400, response.statusCode());
//
//																response.bodyHandler(new Handler<Buffer>() {
//																				public void handle(Buffer body) {
//																								String actual = body.toString();
//																								String expected = "This server probe status is false";
//																								assertEquals("Probe server should start disabled", expected, actual);
//																								try {
//																												InetAddress IP = InetAddress.getLocalHost();
//																												localAddress = IP.getHostAddress();
//																								} catch (java.net.UnknownHostException unhostException) {
//																												unhostException.printStackTrace();
//																								}
//																								HttpClientRequest request = vertx.createHttpClient().setHost(localAddress).setPort(8090).exceptionHandler(handler).get("/?probe=start", new Handler<HttpClientResponse>() {
//
//																												@Override
//																												public void handle(HttpClientResponse response) {
//
//																																assertEquals(200, response.statusCode());
//
//																																response.bodyHandler(new Handler<Buffer>() {
//																																				public void handle(Buffer body) {
//																																								String actual = body.toString();
//																																								String expected = "This server probe status is true";
//																																								assertEquals("Probe server should start after /?probe=start request", expected, actual);
//
//																																								HttpClientRequest request = vertx.createHttpClient().setPort(8090).exceptionHandler(handler).get("/", new Handler<HttpClientResponse>() {
//
//																																												@Override
//																																												public void handle(HttpClientResponse response) {
//																																																assertEquals(200, response.statusCode());
//
//																																																response.bodyHandler(new Handler<Buffer>() {
//																																																				public void handle(Buffer body) {
//																																																								String actual = body.toString();
//																																																								String expected = "This server probe status is true";
//																																																								assertEquals("Probe server should return enabled after starting", expected, actual);
//
//																																																								testComplete();
//																																																				}
//																																																});
//																																												}
//																																								});
//																																								request.end();
//																																				}
//																																});
//																												}
//																								});
//																								request.end();
//																				}
//																});
//												}
//								});
//								request.end();
				}

				@Test
				@Ignore
				public void shouldReturnProbeStatusDisabledAfterEnablingAndDisabling(TestContext context) {

//								HttpClientRequest request = vertx.createHttpClient().setPort(8090).exceptionHandler(handler).get("/", new Handler<HttpClientResponse>() {
//
//												@Override
//												public void handle(HttpClientResponse response) {
//
//																assertEquals(400, response.statusCode());
//
//																response.bodyHandler(new Handler<Buffer>() {
//																				public void handle(Buffer body) {
//																								String actual = body.toString();
//																								String expected = "This server probe status is false";
//																								assertEquals("Probe server should start disabled", expected, actual);
//																								try {
//																												InetAddress IP = InetAddress.getLocalHost();
//																												localAddress = IP.getHostAddress();
//																								} catch (java.net.UnknownHostException unhostException) {
//																												unhostException.printStackTrace();
//																								}
//																								HttpClientRequest request = vertx.createHttpClient().setHost(localAddress).setPort(8090).exceptionHandler(handler).get("/?probe=start", new Handler<HttpClientResponse>() {
//
//																												@Override
//																												public void handle(HttpClientResponse response) {
//
//																																assertEquals(200, response.statusCode());
//
//																																response.bodyHandler(new Handler<Buffer>() {
//																																				public void handle(Buffer body) {
//																																								String actual = body.toString();
//																																								String expected = "This server probe status is true";
//																																								assertEquals("Probe server should start after /?probe=start request", expected, actual);
//
//																																								HttpClientRequest request = vertx.createHttpClient().setHost(localAddress).setPort(8090).exceptionHandler(handler).get("/?probe=stop", new Handler<HttpClientResponse>() {
//
//																																												@Override
//																																												public void handle(HttpClientResponse response) {
//																																																assertEquals(200, response.statusCode());
//
//																																																response.bodyHandler(new Handler<Buffer>() {
//																																																				public void handle(Buffer body) {
//																																																								String actual = body.toString();
//																																																								String expected = "This server probe status is false";
//																																																								assertEquals("Probe server stop request should return disabled after disabling", expected, actual);
//
//																																																								HttpClientRequest request = vertx.createHttpClient().setHost(localAddress).setPort(8090).exceptionHandler(handler).get("/", new Handler<HttpClientResponse>() {
//
//																																																												@Override
//																																																												public void handle(HttpClientResponse response) {
//																																																																assertEquals(200, response.statusCode());
//
//																																																																response.bodyHandler(new Handler<Buffer>() {
//																																																																				public void handle(Buffer body) {
//																																																																								String actual = body.toString();
//																																																																								String expected = "This server probe status is false";
//																																																																								assertEquals("Probe server status should return disabled after disabling", expected, actual);
//
//																																																																								testComplete();
//																																																																				}
//																																																																});
//																																																												}
//																																																								});
//																																																								request.end();
//																																																				}
//																																																});
//																																												}
//																																								});
//																																								request.end();
//																																				}
//																																});
//																												}
//																								});
//																								request.end();
//																				}
//																});
//												}
//								});
//								request.end();
				}
}
