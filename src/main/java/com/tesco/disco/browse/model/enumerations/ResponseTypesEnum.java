package com.tesco.disco.browse.model.enumerations;

import org.elasticsearch.common.lang3.StringUtils;

/**
	* Created by bruno on 04/05/16.
	*/
public enum ResponseTypesEnum {
				PRODUCTS("products", "product"),
				TAXONOMY("taxonomy", "taxonomy");

				private String type;
				private String indexType;

				private ResponseTypesEnum(String type, String indexType) {
								this.type =  type;
								this.indexType = indexType;
				}

    public String getType() {
								return type;
				}

				public String getIndexType() {
								return indexType;
				}

				public static ResponseTypesEnum getByType(String type) {
								for (int i = 0; i < ResponseTypesEnum.values().length; i++) {
												if (StringUtils.isNotBlank(type) && type.equals(ResponseTypesEnum.values()[i].getType())) {
																return ResponseTypesEnum.values()[i];
												}
								}
								return null;
				}
}
