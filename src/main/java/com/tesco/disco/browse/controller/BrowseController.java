package com.tesco.disco.browse.controller;

import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.http.HttpServerResponse;

/**
 * Created by bruno on 20/04/16.
 */
public interface BrowseController {
    void browse(String index, String templateId, String geo, String distChannel, String responseType, JsonObject payload, HttpServerResponse response);
}
