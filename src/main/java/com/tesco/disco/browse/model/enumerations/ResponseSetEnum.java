package com.tesco.disco.browse.model.enumerations;

import org.elasticsearch.common.lang3.StringUtils;

/**
	* Created by bruno on 04/05/16.
	*/
public enum ResponseSetEnum {
				RESULTS("results"),
				TOTALS("totals"),
				// SUGGESTIONS are not required for browse, since it doesn't perform a specific query
  		//	SUGGESTIONS("suggestions"),
				BRAND("brands"),
				TAXONOMY("taxonomy"),
				FILTERS("filters");

				private String setName;

				private ResponseSetEnum(String setName) {
								this.setName =  setName;
				}

				public String getSetName() {
								return setName;
				}

				public static ResponseSetEnum getBySetName(String setName) {
								for (int i = 0; i < ResponseSetEnum.values().length; i++) {
												if (StringUtils.isNotBlank(setName) && setName.equals(ResponseSetEnum.values()[i].getSetName())) {
																return ResponseSetEnum.values()[i];
												}
								}
								return null;
				}
}
