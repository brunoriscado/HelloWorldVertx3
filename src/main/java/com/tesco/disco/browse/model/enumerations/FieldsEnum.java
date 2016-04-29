package com.tesco.disco.browse.model.enumerations;

/**
	* Created by bruno on 29/04/16.
	*/
public enum FieldsEnum {
				TPNB("tpnb"),
				UNIT_PRICE("unitprice"),
				PRICE("price"),
				PRODUCT_NAME("name"),
				DESCRIPTION("description"),
				IS_NEW("IsNew"),
				IS_SPECIAL_OFFER("IsSpecialOffer"),
				IMAGE("image"),
				ID("id"),
				SUPER_DEPARTMENT("superDepartment"),
				DEPARTMENT("department"),
				AISLE("aisle"),
				SHELF("shelf"),
				PROMOTION_DESCRIPTION("PromotionDescription"),
				PROMOTION_ID("PromotionId"),
				PROMOTION_ICON("PromotionIcon"),
				PROMOTION_START("PromotionStart"),
				PROMOTION_END("PromotionEnd"),
				CONTENTS_MEASURE_TYPE("ContentsMeasureType"),
				CONTENTS_QUANTITY("ContentsQuantity"),
				UNIT_QUANTITY("UnitQuantity"),
				AVERAGE_SELLING_UNIT_WEIGHT("AverageSellingUnitWeight"),
				UNIT_OF_SALE("UnitOfSale"),
				AVAILABILITY("availability"),
				NEW("new"),
				OFFER("offer");

				private String name;

				private FieldsEnum(String name) {
								this.name = name;
				}

				public String getName() {
								return name;
				}

				public static FieldsEnum getField(String field) {
								for (int i = 0; i < FieldsEnum.values().length; i++) {
												if (field.equals(FieldsEnum.values()[i])) {
																return FieldsEnum.values()[i];
												}
								}
								return null;
				}
}
