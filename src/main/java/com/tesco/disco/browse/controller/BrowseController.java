package com.tesco.disco.browse.controller;

import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.http.HttpServerResponse;

/**
 * Created by bruno on 20/04/16.
 */
public interface BrowseController {
    void browse(String geo, String distChannel, JsonObject payload, HttpServerResponse response);
}
