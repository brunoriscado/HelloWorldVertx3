package com.tesco.disco.browse.model.taxonomy.model;

import io.vertx.core.json.JsonObject;

import java.io.Serializable;

/**
 * Created by bruno on 07/04/16.
 */
public interface SearchSerializable extends Serializable {
    JsonObject toJson();
}
