package com.tesco.disco.browse.model.enumerations;

import org.elasticsearch.common.lang3.StringUtils;

/**
	* Created by bruno on 29/04/16.
	*/
public enum FieldsEnum {
				TPNB("tpnb", "tpnb"),
				UNIT_PRICE("unitprice", "store_unitprice"),
				PRICE("price", "store_price"),
				PRODUCT_NAME("name", "name"),
				DESCRIPTION("description", "description"),
				IS_NEW("IsNew", "IsNew"),
				IS_SPECIAL_OFFER("IsSpecialOffer", "IsSpecialOffer"),
				IMAGE("image", "image"),
				ID("id", "id"),
				SUPER_DEPARTMENT("superDepartment", "superDepartment"),
				DEPARTMENT("department", "department"),
				AISLE("aisle", "aisle"),
				SHELF("shelf", "shelf"),
				PROMOTION_DESCRIPTION("PromotionDescription", "PromotionDescription"),
				PROMOTION_ID("PromotionId", "PromotionId"),
				PROMOTION_ICON("PromotionIcon", "PromotionIcon"),
				PROMOTION_START("PromotionStart", "PromotionStart"),
				PROMOTION_END("PromotionEnd", "PromotionEnd"),
				CONTENTS_MEASURE_TYPE("ContentsMeasureType", "ContentsMeasureType"),
				CONTENTS_QUANTITY("ContentsQuantity", "ContentsQuantity"),
				UNIT_QUANTITY("UnitQuantity", "UnitQuantity"),
				AVERAGE_SELLING_UNIT_WEIGHT("AverageSellingUnitWeight", "AverageSellingUnitWeight"),
				UNIT_OF_SALE("UnitOfSale", "UnitOfSale"),
				AVAILABILITY("availability", "store_availability"),
				NEW("new", "new"),
				OFFER("offer", "offer"),
				BRAND("brand", "brand");

				private String name;
				private String remapName;

				private FieldsEnum(String name, String remapName) {
								this.name = name;
								this.remapName = remapName;
				}

				public String getName() {
								return name;
				}

				public String getRemapName() {
								return remapName;
				}

				public static FieldsEnum getByName(String name, String remapName) {
								if (StringUtils.isNotBlank(name) || StringUtils.isNotBlank(remapName)) {
												for (int i = 0; i < FieldsEnum.values().length; i++) {
																if (StringUtils.isNotBlank(name) && name.equals(FieldsEnum.values()[i].getName())) {
																				return FieldsEnum.values()[i];
																} else if (StringUtils.isNotBlank(remapName) && remapName.equals(FieldsEnum.values()[i].getRemapName())) {
																				return FieldsEnum.values()[i];
																}
												}
								}
								return null;
				}
}
