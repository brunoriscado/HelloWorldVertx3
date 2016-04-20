package com.tesco.disco.browse.model.taxonomy.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

import java.util.List;

/**
 * Created by bruno on 07/04/16.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Aisle implements SearchSerializable {
    private String name;
    private long total;
    private List<Shelf> shelves;

    public Aisle() {
    }

    public Aisle(String name, long total) {
        this.name = name;
        this.total = total;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public List<Shelf> getShelves() {
        return shelves;
    }

    public void setShelves(List<Shelf> shelves) {
        this.shelves = shelves;
    }

    public Aisle addShelves(List<Shelf> shelves) {
        this.setShelves(shelves);
        return this;
    }

    public JsonObject toJson() {
        return new JsonObject(Json.encode(this));
    }
}
