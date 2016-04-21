package com.tesco.disco.browse.controller;

import io.vertx.rxjava.core.http.HttpServerResponse;

/**
 * Created by bruno on 20/04/16.
 */
public interface BrowseController {
    void browse(HttpServerResponse response);
}
