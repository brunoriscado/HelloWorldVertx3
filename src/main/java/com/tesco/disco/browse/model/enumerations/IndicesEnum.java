package com.tesco.disco.browse.model.enumerations;

import org.elasticsearch.common.lang3.StringUtils;

/**
	* Created by bruno on 04/05/16.
	*/
public enum IndicesEnum {
				GHS_PRODUCTS("ghs.products"),
				GHS_TAXONOMY("ghs.taxonomy");

				private String index;

				private IndicesEnum(String index) {
								this.index =  index;
				}

				public String getIndex() {
								return index;
				}

				public static IndicesEnum getByIndexName(String index) {
								for (int i = 0; i < IndicesEnum.values().length; i++) {
												if (StringUtils.isNotBlank(index) && index.equals(IndicesEnum.values()[i].getIndex())) {
																return IndicesEnum.values()[i];
												}
								}
								return null;
				}
}
