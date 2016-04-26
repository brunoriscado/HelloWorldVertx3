package com.tesco.disco.browse.controller.vertx;

import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.impl.MimeMapping;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.http.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.net.InetAddress;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
	* Created by bruno on 26/04/16.
	*/
public class ProbeVerticle extends AbstractVerticle {
				private static final Logger LOGGER = LoggerFactory.getLogger(ProbeVerticle.class);
				private static final Marker MARKER = MarkerFactory.getMarker("VERTICLE");
				public static final String PROBE_DISABLE = "stop";
				public static final String PROBE_ENABLE = "start";
				public static boolean serverEnabled = false;
				private HttpServer httpServer;
				private static final String PROBE_STRING = "This server probe status is ";

				public void start(io.vertx.core.Future<Void> startFuture) {
								JsonObject config = vertx.getOrCreateContext().config();
								vertx.createHttpServer(new HttpServerOptions()
																.setHost(config.getString("host"))
																.setPort(config.getInteger("port")))
																.requestHandler(request -> {
																				LOGGER.debug("Inside the Handle method");
																				String localAddress = null;
																				try {
																								InetAddress IP = InetAddress.getLocalHost();
																								localAddress = IP.getHostAddress();
																								LOGGER.info("The local IP address of the System is := " + IP.getHostAddress());
																				} catch (java.net.UnknownHostException unhostException) {
																								unhostException.printStackTrace();
																				}
																				String clientIpAddress = request.remoteAddress().host();
																				LOGGER.debug("The client IP address is" + clientIpAddress);
																				LOGGER.debug("The local  IP address is" + localAddress);
																				if (clientIpAddress.equals(localAddress)) {
																								LOGGER.info("Inside the jenkinAddress+ local address block");
																								if (request.params() != null) {
																												LOGGER.info("Request Map" + request.params());
																												String probeStatus = request.params().get("probe");
																												if (probeStatus != null) {
																																LOGGER.info("probeStatus is" + probeStatus);
																																if (PROBE_DISABLE.equals(probeStatus)) {
																																				serverEnabled = false;
																																				LOGGER.info("probeStatus is " + serverEnabled);
																																} else if (PROBE_ENABLE.equals(probeStatus)) {
																																				serverEnabled = true;
																																				LOGGER.info("probeStatus is " + serverEnabled);
																																}
																												}
																								}
																								request.response().setStatusCode(200);
																				} else if (serverEnabled) {
																								LOGGER.debug("Inside the load balancer block");
																								request.response().setStatusCode(200);
																				} else {
																								LOGGER.debug("Inside the else block Block ");
																								request.response().setStatusCode(400);
																				}
																				LOGGER.debug("The response sent to client is " + serverEnabled);
																				request.response().putHeader(HttpHeaders.CONTENT_TYPE.toString(), MimeMapping.getMimeTypeForExtension("txt"));
																				request.response().end(PROBE_STRING + serverEnabled);
																})
																.listenObservable()
																.subscribe(server -> {
																												LOGGER.info("The Probing port is  " + config.getInteger("port"));
																												LOGGER.info("The  Jenkin configured IP Address is " + config.getString("jenkins.address"));
																												httpServer = server;
																								},
																								error -> {
																												LOGGER.error(MARKER, "Server unable to start: {}", error.getMessage());
																												startFuture.fail("Server unable to start");
																								},
																								() -> {
																												LOGGER.info(MARKER, "HttpServer Probe Verticle started at: {}",
																																				ZonedDateTime.now().format(DateTimeFormatter.BASIC_ISO_DATE.ISO_INSTANT));
																												startFuture.complete();
																								});
				}

				@Override
				public void stop() {
								LOGGER.info(MARKER, "Stopping the probe http server verticle");
								httpServer.close();
				}
}