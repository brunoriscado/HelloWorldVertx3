package com.tesco.disco.browse.model.enumerations;

import org.elasticsearch.common.lang3.StringUtils;

/**
	* Created by bruno on 16/05/16.
	*/
public enum PipedFilters {
				IS_NEW("IsNew"),
				IS_SPECIAL_OFFER("IsSpecialOffer"),
				TPNB("tpnb");

				private String filterName;

				private String getFilterName() {
								return filterName;
				}

				private PipedFilters(String filterName) {
								this.filterName = filterName;
				}

				public static PipedFilters getByFilterName(String filterName) {
								for (int i = 0; i < PipedFilters.values().length; i++) {
												if (StringUtils.isNotBlank(filterName) && filterName.equals(PipedFilters.values()[i].getFilterName())) {
																return PipedFilters.values()[i];
												}
								}
								return null;
				}
}
