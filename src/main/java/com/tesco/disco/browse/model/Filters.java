package com.tesco.disco.browse.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.tesco.disco.browse.model.taxonomy.SearchSerializable;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Filters implements SearchSerializable {
				public Filters() {
				}

				private List<Brand> brands;

				public List<Brand> getBrands() {
								return brands;
				}

				public void setBrands(List<Brand> brands) {
								this.brands = brands;
				}

				@Override
				public JsonObject toJson() {
								return new JsonObject(Json.encode(this));
				}
}