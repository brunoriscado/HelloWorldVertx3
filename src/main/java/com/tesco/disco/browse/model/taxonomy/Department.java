package com.tesco.disco.browse.model.taxonomy;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

import java.util.List;

/**
 * Created by bruno on 07/04/16.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Department implements SearchSerializable {
    private String name;
    private long total;
    private List<Aisle> aisles;

    public Department() {
    }

    public Department(String name, long total) {
        this.name = name;
        this.total =  total;
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

    public List<Aisle> getAisles() {
        return aisles;
    }

    public void setAisles(List<Aisle> aisles) {
        this.aisles = aisles;
    }

    public Department addAisles(List<Aisle> aisles) {
        this.setAisles(aisles);
        return this;
    }

    public JsonObject toJson() {
        return new JsonObject(Json.encode(this));
    }
}
