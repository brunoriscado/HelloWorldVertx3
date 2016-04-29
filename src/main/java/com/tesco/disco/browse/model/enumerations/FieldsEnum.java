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
				PROMOTION_("PromotionId"),
				PROMOTION_("PromotionIcon"),
				PROMOTION_("PromotionStart"),
				PROMOTION_("PromotionEnd"),
				CONTENTS_MEASURE_TYPE("ContentsMeasureType"),
				CONTENTS_QUANTITY("ContentsQuantity"),
				UNIT_QUANTITY("UnitQuantity"),
				("AverageSellingUnitWeight"),
				("UnitOfSale"),
				("availability"),
				NEW("new"),
				OFFER("offer");
				private String name;

				private FieldsEnum(String name) {
								this.name = name;
				}
}
