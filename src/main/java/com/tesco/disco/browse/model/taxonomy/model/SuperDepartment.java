package com.tesco.disco.browse.model.taxonomy.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

import java.util.List;

/**
 * Created by bruno on 07/04/16.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SuperDepartment implements SearchSerializable {
    private String name;
    private long total;
    private List<Department> departments;

    public SuperDepartment() {
    }

    public SuperDepartment(String name, long total) {
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

    public List<Department> getDepartments() {
        return departments;
    }

    public void setDepartments(List<Department> departments) {
        this.departments = departments;
    }

    public SuperDepartment addDepartments(List<Department> departments) {
        this.setDepartments(departments);
        return this;
    }

    public JsonObject toJson() {
        return new JsonObject(Json.encode(this));
    }
}
