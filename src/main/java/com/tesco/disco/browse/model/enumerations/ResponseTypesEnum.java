package com.tesco.disco.browse.model.enumerations;

import org.elasticsearch.common.lang3.StringUtils;

/**
	* Created by bruno on 04/05/16.
	*/
public enum ResponseTypesEnum {
				PRODUCTS("products"),
				TAXONOMY("taxonomy");

				private String type;

				private ResponseTypesEnum(String type) {
								this.type =  type;
				}

    public String getType() {
								return type;
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
