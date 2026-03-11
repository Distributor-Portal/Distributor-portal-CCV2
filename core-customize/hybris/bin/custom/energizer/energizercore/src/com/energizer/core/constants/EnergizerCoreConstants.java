/*
 * [y] hybris Platform
 *
 * Copyright (c) 2000-2014 hybris AG
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of hybris
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with hybris.
 *
 *
 */
package com.energizer.core.constants;

/**
 * Global class for all B2BAcceleratorCore constants. You can add global constants for your extension into this class.
 */
@SuppressWarnings("PMD")
public class EnergizerCoreConstants extends GeneratedEnergizerCoreConstants
{
	public static final String EXTENSIONNAME = "energizercore";

	// Constants for ProductCSVProcessor
	public static final String ERPMATERIAL_ID = "ERPMaterialID";
	public static final String IMAGEREFERENCE_ID = "Image Ref Mat";
	public static final String PRODUCT_GROUP = "Product Group";
	public static final String LIST_PRICE = "ListPrice";
	public static final String LIST_PRICE_CURRENCY = "ListPriceCurrency";
	public static final String OBSOLETE_STATUS = "ObsoleteStatus";
	public static final String LANGUAGE = "Language";
	public static final String PRODUCT_DESCRIPTION = "ProductDesription";
	public static final String EAN = "ean";
	public static final String IS_PBG = "isPBG";
	public static final String WESELL = "WESELL";
	public static final String LATAM = "LATAM";
	public static final String EMEA = "EMEA";
	public static final String ALL = "ALL";
	public static final String WESELL_DEFAULT_UOM = "WesellDefaultUOM";
	public static final String PERSONALCARE_PRODUCTCATALOG = "personalCareProductCatalog";
	public static final String UPC_STARTS_WITH_0245 = "0245";
	public static final String UPC_STARTS_WITH_049 = "049";
	public static final String PBG_UPC0245_BRAND_NAME = "pbg.upc0245.brand.name";
	public static final String PBG_UPC049_BRAND_NAME = "pbg.upc049.brand.name";
	public static final String PBG_SKIP_CSV_LEVEL23_HEADERS = "pbg.skip.csv.level23.headers";
	public static final String LATAM_DEFAULT_SALES_ORG = "latam.default.sales.org";
	public static final String ORDER_SIMULATE_CHARSET_NAME = "order.simulate.charset.name";

	// Constants for Delivery attachments feature
	public static final String MEDIA_ROOT_DIRECTORY = "media.read.dir";
	public static final String SYS_MASTER = "sys_master";
	public static final String DELIVERY_NOTE_FILE_DIRECTORY = "delivery_notes";
	public static final String DELIVERY_NOTE_FILE_EMPTY = "text.delivery.notes.file.empty";
	public static final String PERSONALCARE_CONTENTCATALOG = "personalCareContentCatalog";
	public static final String PERSONALCAREEMEA_CONTENTCATALOG = "personalCareEMEAContentCatalog";


	// Constants for EnergizerMediaCSVProcessor
	public static final String THUMBNAIIL_PATH = "ThumnailPath";
	public static final String DISPLAY_IMAGE_PATH = "DisplayImagePath";

	// Constants for  EnergizerCMIRCSVProcessor
	public static final String ENERGIZER_ACCOUNT_ID = "EnergizerAccountID";
	public static final String CUSTOMER_MATERIAL_ID = "CustomerMaterialID";
	public static final String CUSTOMER_MATERIAL_DESCRIPTION = "CustomerMaterialDescription";
	public static final String SHIPMENT_POINT_NO = "ShipmentPointNumber";
	public static final String CUSTOMER_LIST_PRICE_CURRENCY = "CustomerListpricecurrency";
	public static final String CUSTOMER_LIST_PRICE = "CustomerListPrice";
	public static final String MATERIAL_LIST_PRICE_CURRENCY = "MaterialListpricecurrency";
	public static final String MATERIAL_LIST_PRICE = "MaterialListprice";
	public static final String MINIMUM_ORDERING_QUANTITY = "MinimumOrderingQuantity";

	// Constants for  EnergizerProductConversionCSVProcessor
	public static final String ALTERNATE_UOM = "AlternateUOM";
	public static final String BASE_UOM_MULTIPLIER = "BaseUOMMultiplier";
	public static final String VOLUME_IN_UOM = "VolumeInUOM";
	public static final String VOLUME_UOM = "VolumeUOM";
	public static final String WEIGHT_IN_UOM = "WeightInUOM";
	public static final String WEIGHT_UOM = "WeightUOM";

	// Constants for  EnergizerSalesUOMCSVProcessor
	public static final String CUSTOMER_ID = "customerId";
	public static final String SALES_ORG = "salesOrganisation";
	public static final String DISTRIBUTION_CHANNEL = "distributionChannel";
	public static final String DIVISION = "division";
	//public static final String SALES_AREA_ID = "salesAreaId";
	public static final String SEGMENT_ID = "segmentId";
	public static final String FAMILY_ID = "familyId";
	public static final String UOM = "unitOfMeasure";
	public static final String MOQ = "minimumOrderQuantity";

	// Constants for  EnergizerProductCategoryCSVProcessor
	public static final String SEGMENT_NAME = "SegmentName";
	public static final String SEGMENT_DESCRIPTION = "SegmentDescription";
	public static final String FAMILY_NAME = "FamilyName";
	public static final String FAMILY_DESCRIPTION = "FamilyDescription";
	public static final String GROUP_NAME = "GroupName";
	public static final String GROUP_DESCRIPTION = "GroupDescription";
	public static final String SUBGROUP_NAME = "SubGroupName";
	public static final String SUBGROUP_DESCRIPTION = "SubGroupDescription";

	// Constants for  EnergizerCategoryCSVProcessor
	public static final String ERP_CATEGORY_CODE = "ERPCategoryCode";
	public static final String MARKETING_CATEGORY_CODE = "MarketingCategoryCode";
	public static final String MARKETING_CATEGORY_NAME = "MarketingCategoryName";

	// uom constants
	public static final String EA = "EA";
	public static final String INTERPACK = "IP";
	public static final String CASE = "CS";
	public static final String LAYER = "LAY";
	public static final String PALLET = "PAL";
	public static final String CU = "CU";

	// Constants for Package Dimensions including length , breadth and height

	public static final String LENGTH = "LengthInUOM";
	public static final String WIDTH = "WidthInUOM";
	public static final String HEIGHT = "HeightInUOM";
	public static final String UNIT = "DimensionUOM";
	public static final String ALTERNATIVEUNIT = "AlternativeUnit";

	//Constants for WeSell
	public static final String WESELL_USERID_PREFIX = "wesell.userId.prefix";
	public static final String WESELL_USERID_SUFFIX = "wesell.userId.suffix";
	public static final String WESELL_CONTACT_NUMBER = "wesell.contactNumber";
	public static final String WESELL_B2BCUSTOMER_DEFAULT_USER_GROUPS = "wesell.b2bcustomer.default.user.groups";
	public static final String CART = "cart";
	public static final String CHECKOUT = "checkout";
	public static final String WESELL_SALESREP_DEFAULT_USER_GROUPS = "wesell.salesrep.default.user.groups";
	public static final String EMPLOYEE = "employee";
	public static final String CUSTOMER = "customer";
	public static final String WESELL_IT_MAIL_ERRORS_EMAILID = "wesell.it.mail.errors.emailID";
	public static final String WESELL_IT_MAIL_ERRORS_EMAIL_DISPLAY_NAME = "wesell.it.mail.errors.emailID.display.name";
	public static final String WESELL_SEND_SAP_FAILED_EMAIL_PAGE_UID = "QuoteSendSubmitOrderToSAPFailedEmail";
	public static final String CRONJOBS_FROM_EMAIL = "cronjobs.from.email";

	// Generic Site Constants
	public static final String SITE_PERSONALCARE = "personalCare";
	public static final String SITE_PERSONALCAREEMEA = "personalCareEMEA";
	public static final String ORDER_PROCESS_DEFINITION_NAME = "accApproval";
	public static final String Approval_Process_StartAction = "approvalProcessStartAction";
	public static final String ERROR = "ERROR";
	public static final String ACC_APPROVAL = "accApproval";
	public static final String B2B_PROCESSING_ERROR = "B2B_PROCESSING_ERROR";
	//public static final String PRICE_UOM = "PriceUOM";
	public static final String LIST_PRICE_UOM = "ListPriceUOM";
	public static final String CUST_PRICE_UOM = "CustPriceUOM";

	// Shipping Point Business Rule Validator
	public static final String SHIPPING_POINT_MISMATCH = "shippingpoint.business.rule.shipfromnotvalid";
	public static final String SHIPPING_POINT_NOT_FOUND = "shippingpoint.business.rule.shipfromnotfound";
	public static final String SHIPPING_LOCATION_MISMATCH = "shippingpoint.business.rule.shipfromlocationnotsame";
	public static final String SHIPPING_POINT_LOCATION_NOT_FOUND = "shippingpoint.business.rule.shippingLocationnotfound";
	public static final String CHOOSE_ONE_SHIPPING_POINT_LOCATION = "shippingpoint.business.rule.shipping.location.choose.one";


	private EnergizerCoreConstants()
	{
		super();
		assert false;
	}


}
