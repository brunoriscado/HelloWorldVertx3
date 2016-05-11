package com.tesco.disco.browse.model.taxonomy;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

import java.util.List;

/**
 * Created by bruno on 07/04/16.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Taxonomy implements SearchSerializable {
    public Taxonomy() {
    }

    private List<SuperDepartment> superDepartments;

    public List<SuperDepartment> getSuperDepartments() {
        return superDepartments;
    }

    public void setSuperDepartments(List<SuperDepartment> superDepartments) {
        this.superDepartments = superDepartments;
    }

    public Taxonomy addSuperDepartments(List<SuperDepartment> superDepartments) {
        this.setSuperDepartments(superDepartments);
        return this;
    }

    public JsonObject toJson() {
        return new JsonObject(Json.encode(this));
    }
}
