package com.tesco.disco.browse;

import io.vertx.rxjava.core.Vertx;

/**
 * Created by bruno on 21/04/16.
 */
public class Main {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(Starter.class.getName());
    }
}
