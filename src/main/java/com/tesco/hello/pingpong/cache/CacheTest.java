package com.tesco.hello.pingpong.cache;

import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.core.shareddata.SharedData;

/**
 * Created by bruno on 09/12/16.
 */
public class CacheTest {
    private SharedData sharedData;
    private JsonObject config;

    public CacheTest(Vertx vertx) {
        sharedData = vertx.sharedData();
    }

    public <T> T writeToCache(T objectToCache, String cacheName) {
        sharedData.getClusterWideMapObservable(cacheName)
                .flatMap(asyncMap -> {
                   asyncMap.putIfAbsentObservable()
                });
    }
}