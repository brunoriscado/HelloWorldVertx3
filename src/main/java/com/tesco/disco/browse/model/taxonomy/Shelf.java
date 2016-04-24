package com.tesco.disco.browse.model.taxonomy;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

/**
 * Created by bruno on 07/04/16.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Shelf implements SearchSerializable {
    private String name;
    private Long total;

    public Shelf() {
    }

    public Shelf(String name) {
        this.name = name;
    }

    public Shelf(String name, Long total) {
        this.name = name;
        this.total = total;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public JsonObject toJson() {
        return new JsonObject(Json.encode(this));
    }
}
