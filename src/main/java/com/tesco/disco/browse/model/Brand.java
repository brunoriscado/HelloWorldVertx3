package com.tesco.disco.browse.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.tesco.disco.browse.model.taxonomy.SearchSerializable;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;


@JsonInclude(JsonInclude.Include.NON_NULL)
public class Brand implements SearchSerializable {
				private String name;
				private long total;

				public Brand() {
				}

				public Brand(String name, long total) {
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

				@Override
				public JsonObject toJson() {
								return new JsonObject(Json.encode(this));
				}
}
