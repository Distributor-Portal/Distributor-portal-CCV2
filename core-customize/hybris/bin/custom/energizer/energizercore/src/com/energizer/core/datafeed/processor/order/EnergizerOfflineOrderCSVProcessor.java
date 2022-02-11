package com.energizer.core.datafeed.processor.order;

import de.hybris.platform.b2b.company.B2BCommerceUnitService;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.catalog.CatalogVersionService;
import de.hybris.platform.cms2.model.site.CMSSiteModel;
import de.hybris.platform.cms2.servicelayer.services.CMSSiteService;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.product.UnitModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.product.ProductService;
import de.hybris.platform.product.UnitService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.util.Config;
import de.hybris.platform.util.DiscountValue;
import de.hybris.platform.util.TaxValue;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;

import com.energizer.core.datafeed.AbstractEnergizerCSVProcessor;
import com.energizer.core.datafeed.EnergizerCSVFeedError;
import com.energizer.core.datafeed.facade.impl.DefaultEnergizerAddressFacade;
import com.energizer.core.model.EnergizerB2BUnitModel;
import com.energizer.core.model.EnergizerCMIRModel;
import com.energizer.core.model.EnergizerCronJobModel;
import com.energizer.core.model.EnergizerProductConversionFactorModel;
import com.energizer.core.model.EnergizerProductModel;
import com.energizer.services.order.EnergizerOrderService;
import com.energizer.services.product.EnergizerProductService;


/**
 *
 * This processors imports the off line order.
 *
 * Sample file will look like
 *
 * SAPOrderNo,CreatedByUser,CreatedDate,ReqDeliveryDate,PONo,TotalValue,TotalShipment,TotalDiscount,TotalTax,B2BAccount,
 * Status,
 * ContainerID,SealNumber,VesselNumber,InvoicePDF,ArchiveID,ERPMaterialID,OrderEntryQty,UOM,UnitPrice,TotalPrice,
 * ItemTotalShipment,ItemTotalDiscount,ItemTax 54321, Jack, 11-5-2014 , 14-5-2014 ,XYZ,1000, 200, 100, 250, 4321,
 * PENDING,XYZ, XYZ, XYZ, XYZ, XYZ, PRD001, 10, 1A, 100, 1000, 50, 50, 50
 *
 * Total column count : 24
 */

public class EnergizerOfflineOrderCSVProcessor extends AbstractEnergizerCSVProcessor
{
	@Resource
	private ModelService modelService;
	@Resource
	private B2BCommerceUnitService b2bCommerceUnitService;
	@Resource
	private UserService userService;
	@Resource
	private CatalogVersionService catalogVersionService;
	@Resource
	private ProductService productService;
	@Resource
	private UnitService unitService;
	@Resource
	private EnergizerOrderService energizerOrderService;
	@Resource
	EnergizerProductService energizerProductService;
	@Resource
	CMSSiteService cmsSiteService;

	@Resource
	private DefaultEnergizerAddressFacade defaultEnergizerAddressFacade;

	//Constant declarations for CSV header names
	private static final String SAP_ORDER_NO = "SAPOrderNo";
	private static final String CREATED_BY_USER = "CreatedByUser";
	private static final String CREATED_DATE = "CreatedDate";
	private static final String CREATED_TIME = "CreatedTime";
	private static final String REQ_DELIVERY_DATE = "ReqDeliveryDate";
	private static final String PO_NO = "PONo";
	private static final String TOTAL_VALUE = "TotalValue";
	private static final String TOTAL_SHIPMENT = "TotalShipment";
	private static final String TOTAL_DISCOUNT = "TotalDiscount";
	private static final String TOTAL_TAX = "TotalTax";
	private static final String B2B_ACCOUNT = "B2BAccount";
	private static final String STATUS = "Status";
	private static final String CONTAINER_ID = "ContainerID";
	private static final String SEAL_NUMBER = "SealNumber";
	private static final String VESSAL_NUMBER = "VesselNumber";
	private static final String INVOICE_PDF = "InvoicePDF";
	private static final String ARCHIVE_ID = "ArchiveID";
	private static final String ERP_MATERIAL_ID = "ERPMaterialID";
	private static final String ORDER_ENTRY_QTY = "OrderEntryQty";
	private static final String UOM = "UOM";
	private static final String UNIT_PRICE = "UnitPrice";
	private static final String TOTAL_PRICE = "TotalPrice";
	private static final String ITEM_TOTAL_SHIPMENT = "ItemTotalShipment";
	private static final String ITEM_TOTAL_DISCOUNT = "ItemTotalDiscount";
	private static final String ITEM_TAX = "ItemTax";


	private static final String FROM_EMAIL_ADDRESS = Config.getParameter("fromEmailAddress.orderEmailSender");
	private static final String FROM_EMAIL_DISPLAY_NAME = Config.getParameter("fromEmailDisplayName.orderEmailSender");
	private static final String SEND_EMAIL_FOR_ORDER_STATUS = Config.getParameter("sendEmailForOrderStatus");
	private static final String ENERGIZER_OFFLINE_ORDER_FEED_MANDATORY_KEY = "feedprocessor.energizerOfflineOrderFeed.mandatory";
	private static final String ENERGIZER_POSSIBLE_ORDER_STATUS_KEY = Config.getParameter("possibleOrderStatus");
	private static final HashMap<String, String> ENERGIZER_POSSIBLE_ORDER_STATUS_MAP = new HashMap<String, String>()
	{
		{
			put("1", "PENDING");
			put("2", "IN_PROCESS");
			put("3", "SHIPPED");
			put("4", "INVOICED");
			put("5", "CANCELLED");
		}
	};

	private static final String ENERGIZER_DATE_FORMAT_KEY = Config.getParameter("dateFormat");
	private static final SimpleDateFormat ORDER_DATE_FORMATTER = new SimpleDateFormat(ENERGIZER_DATE_FORMAT_KEY);
	private static final String ENERGIZER_DELIVERY_DATE_FORMAT_KEY = Config.getParameter("deliveryDateFormat");
	private static final SimpleDateFormat ORDER_DELIVERY_DATE_FORMATTER = new SimpleDateFormat(ENERGIZER_DELIVERY_DATE_FORMAT_KEY);
	private static final String ENERGIZER_SITE = Config.getParameter("offline.energizer.site");
	private static final Logger LOG = Logger.getLogger(EnergizerOfflineOrderCSVProcessor.class);


	//Instance variables declaration for holding CSV record
	private CSVRecord record = null;

	//Instance variables declarations for holding values of CSV records
	//ForOrder
	private String sapOrderNo = null;
	private String createdByUser = null;
	private Date createdDate = null;
	private Date reqDeliveryDate = null;
	private String poNo = null;
	private Double totalValue = null;
	private Double totalShipment = null;
	private Double totalDiscount = null;
	private Double totalTax = null;
	private String b2bAccount = null;
	private String status = null;
	private String containerID = null;
	private String sealNumber = null;
	private String vesselNumber = null;
	private String invoicePDF = null;
	private String archiveID = null;
	//For Order Entry
	private String erpMaterialID = null;
	private Long orderEntryQty = null;
	private String uom = null;
	private Double unitPrice = null;
	private Double totalPrice = null;
	private Double itemTotalShipment = null;
	private Double itemTotalDiscount = null;
	private Double itemTax = null;

	@Override
	public List<EnergizerCSVFeedError> process(final Iterable<CSVRecord> records, final String catalogName,
			final EnergizerCronJobModel cronjob)
	{
		final List<EnergizerCSVFeedError> errors = new ArrayList<EnergizerCSVFeedError>();
		try
		{
			LOG.info("EnergizerOfflineOrderCSVProcessor:process:Start");
			long succeedRecord = getRecordSucceeded();
			for (final CSVRecord tempRecord : records)
			{
				record = tempRecord;
				validate(record);
				if (!getTechnicalFeedErrors().isEmpty())
				{
					csvFeedErrorRecords.addAll(getTechnicalFeedErrors());
					getBusinessFeedErrors().addAll(getTechnicalFeedErrors());
					getTechnicalFeedErrors().clear();
					continue;
				}
				if (!readCSVRecord(record))
				{
					continue;
				}

				//Validate status code
				if (energizerOrderService.getEnergizerOrderStatus(status) == null)
				{
					LOG.info("In Record Number " + record.getRecordNumber() + " " + STATUS + " " + status + " is not valid");
					LOG.info("Valid status codes are " + ENERGIZER_POSSIBLE_ORDER_STATUS_KEY);
					continue;
				}

				OrderModel existEnergizerOrder = energizerOrderService.getExistingOrder(sapOrderNo);


				//If existEnergizerOrder is null then we will create order and if it is not null then we will update the order
				//If its going to update then at first we need to check for offline order if it is not offline order then don't proceed to update

				if ((existEnergizerOrder != null) && (StringUtils.isEmpty(existEnergizerOrder.getErpOrderCreator())))
				{
					LOG.info("In Record Number " + record.getRecordNumber() + " " + SAP_ORDER_NO + " " + sapOrderNo
							+ " is not offline order");
					continue;
				}

				existEnergizerOrder = createOrUpdateOfflineOrder(existEnergizerOrder);

				final OrderModel currentEnergizerOrder = existEnergizerOrder;

				if (existEnergizerOrder != null)
				{
					try
					{
						modelService.save(existEnergizerOrder);
						final OrderEntryModel energizerOrderEntryModel = createOrUpdateOfflineOrderItem(existEnergizerOrder);
						if (energizerOrderEntryModel != null)
						{
							modelService.save(energizerOrderEntryModel);
							LOG.info(" Energizer Offline order and associated items are saved successfully ");
							existEnergizerOrder
									.setAdjustedTotalPrice(energizerOrderService.getAdjustedTotalPriceForOrder(existEnergizerOrder));
							modelService.save(existEnergizerOrder);
							prepareEmail();
						}
						else
						{
							//Roll back EnergizerOrder
							if (currentEnergizerOrder == null)
							{
								modelService.remove(existEnergizerOrder);
							}
							else
							{
								modelService.save(currentEnergizerOrder);
							}
						}
					}

					catch (final Exception exc)
					{
						LOG.error("Caught" + exc.getMessage());
					}
				}
				succeedRecord++;
				setRecordSucceeded(succeedRecord);
			} //end of for loop
			LOG.info("EnergizerOfflineOrderCSVProcessor:process:End");
		} //end of try block
		catch (final Exception e)
		{

			LOG.error("EnergizerOfflineOrderCSVProcessor ", e);
		}
		getTechnicalFeedErrors().addAll(getBusinessFeedErrors());
		getBusinessFeedErrors().clear();
		return getCsvFeedErrorRecords();
	}

	private void prepareEmail()
	{
		final List<String> possibleOrderStatusValues = Arrays
				.asList(SEND_EMAIL_FOR_ORDER_STATUS.split(new Character(',').toString()));
		final String orderStatusCode = status.toUpperCase();
		final boolean result = possibleOrderStatusValues.contains(orderStatusCode);

		if (result)
		{
			final String emailSubject = createdByUser + " Order is " + status;
			final String emailBody = createdByUser + " Order is " + status + "\n";
			List<String> listOfToAddress = null;

			final EnergizerB2BUnitModel energizerB2BUnitModel = (EnergizerB2BUnitModel) b2bCommerceUnitService
					.getUnitForUid(b2bAccount);
			listOfToAddress = energizerOrderService.getAdminEmailIdsOfEnergizerB2BUnitModel(energizerB2BUnitModel);

			sendEmail(listOfToAddress, null, null, FROM_EMAIL_ADDRESS, FROM_EMAIL_DISPLAY_NAME, null, emailSubject, emailBody);
		}
	}

	/**
	 * This function will create or update order and return created or updated order object if creation or updation is
	 * failed then it will return null
	 */
	private OrderModel createOrUpdateOfflineOrder(OrderModel energizerOrderModel)
	{
		//Flag variable to hold record creating or updating status if record is neither created nor updated then this flag will hold null
		Boolean isOrderCreatedOrUpdated = true;

		// EnergizerB2BUnitModel energizerB2BUnitModel = (EnergizerB2BUnitModel) companyB2BCommerceService.getUnitForUid(b2bAccount);
		EnergizerB2BUnitModel energizerB2BUnitModel = null;
		final List<AddressModel> addresses = defaultEnergizerAddressFacade.fetchAddress(b2bAccount);
		AddressModel addrs = null;
		//only one sold to party will be assigned to one address
		for (final AddressModel address : addresses)
		{
			if (address.getOwner() instanceof EnergizerB2BUnitModel)
			{
				energizerB2BUnitModel = (EnergizerB2BUnitModel) address.getOwner();
				addrs = address;
			}
		}

		// adding extra parameter - Start
		if (null != energizerOrderModel)
		{
			final List<CMSSiteModel> sites = (List<CMSSiteModel>) cmsSiteService.getSites();
			BaseSiteModel baseSite = null;
			for (final CMSSiteModel site : sites)
			{
				if (site.getUid().equalsIgnoreCase(ENERGIZER_SITE))
				{
					baseSite = site;
					energizerOrderModel.setSite(baseSite);
				}
			}
			// only one store per site maintained
			energizerOrderModel.setStore(baseSite.getStores().get(0));

			if (energizerB2BUnitModel != null)
			{
				energizerOrderModel.setUnit(energizerB2BUnitModel);
			}
			if (energizerOrderModel.getUser().getSessionLanguage() != null)
			{
				energizerOrderModel.setLanguage(energizerOrderModel.getUser().getSessionLanguage());
			}
		}
		// adding extra parameter - end

		if (energizerB2BUnitModel == null)
		{
			LOG.info("In Record Number " + record.getRecordNumber() + " " + B2B_ACCOUNT + "does not exist");
			isOrderCreatedOrUpdated = false;
		}
		else
		{
			//Get admin id of B2BAccount
			final List<String> adminIdsOfEnergizerB2BUnitModel = energizerOrderService
					.getAdminIdsOfEnergizerB2BUnitModel(energizerB2BUnitModel);
			if (!adminIdsOfEnergizerB2BUnitModel.isEmpty())
			{
				//If execution reaches here it means B2BAccount is exist and it has admin so we can create or update order
				if (energizerOrderModel == null)
				{
					energizerOrderModel = modelService.create(OrderModel.class);
					final String hybrisOrderId = "" + System.currentTimeMillis();
					energizerOrderModel.setCode(hybrisOrderId);
					energizerOrderModel.setErpOrderNumber(sapOrderNo);
				}

				final CurrencyModel currency = energizerB2BUnitModel.getCurrencyPreference();

				if (currency != null)
				{
					energizerOrderModel.setErpOrderCreator(createdByUser);
					energizerOrderModel.setDate(createdDate);
					energizerOrderModel.setRequestedDeliveryDate(reqDeliveryDate);
					energizerOrderModel.setPurchaseOrderNumber(poNo);
					energizerOrderModel.setTotalPrice(totalValue);
					energizerOrderModel.setSubtotal(totalValue - (totalShipment + totalDiscount + totalTax));
					energizerOrderModel.setDeliveryCost(totalShipment);
					energizerOrderModel.setTotalDiscounts(totalDiscount);
					energizerOrderModel.setTotalTax(totalTax);
					energizerOrderModel.setStatus(energizerOrderService.getEnergizerOrderStatus(status));
					energizerOrderModel.setContainerId(containerID);
					energizerOrderModel.setCurrency(currency);
					final UserModel userModel = userService.getUserForUID(adminIdsOfEnergizerB2BUnitModel.get(0));
					energizerOrderModel.setB2bUnit(energizerB2BUnitModel);
					energizerOrderModel.setUnit(energizerB2BUnitModel);
					energizerOrderModel.setUser(userModel);
					energizerOrderModel.setSealNumber(sealNumber);
					energizerOrderModel.setVesselNumber(vesselNumber);
					energizerOrderModel.setInvoicePDF(invoicePDF);


					isOrderCreatedOrUpdated = true;
				}
				else
				{
					LOG.info("In Record Number " + record.getRecordNumber() + " " + B2B_ACCOUNT + " " + energizerB2BUnitModel.getUid()
							+ " is not having currency preference");
					isOrderCreatedOrUpdated = false;
				}
			}
			else
			{
				LOG.info("In Record Number " + record.getRecordNumber() + " " + B2B_ACCOUNT + " " + energizerB2BUnitModel.getUid()
						+ " is not having any members of admin group");
				isOrderCreatedOrUpdated = false;
			}
			energizerOrderModel.setDeliveryAddress(addrs);
		}
		if (isOrderCreatedOrUpdated == false)
		{
			energizerOrderModel = null;
		}
		return energizerOrderModel;
	}

	/**
	 * This function will create or update OrderEntry for a given Order
	 *
	 * @param energizerOrderModel
	 * @return OrderEntryModel
	 */
	private OrderEntryModel createOrUpdateOfflineOrderItem(final OrderModel energizerOrderModel)
	{/*
	  * OrderEntryModel energizerOrderEntry = null;
	  *
	  * final EnergizerProductModel existEnergizerProduct = (EnergizerProductModel)
	  * productService.getProductForCode(erpMaterialID);
	  *
	  * UnitModel existUnit; try { existUnit = unitService.getUnitForCode(uom); } catch (final Exception e) { existUnit =
	  * null; }
	  *
	  * if (existEnergizerProduct == null || existUnit == null) { LOG.info("In Record Number " + record.getRecordNumber() +
	  * " " + ERP_MATERIAL_ID + " " + erpMaterialID + " or " + UOM + " " + uom + " is not valid"); } else { final
	  * EnergizerCMIRModel energizerCMIRModel = energizerProductService.getEnergizerCMIR(erpMaterialID,
	  * energizerOrderModel.getB2bUnit().getUid());
	  *
	  * if (energizerCMIRModel == null) { LOG.info("No CMIR exist for Record Number " + record.getRecordNumber() + " " +
	  * ERP_MATERIAL_ID + " " + erpMaterialID + " and B2B unit " + energizerOrderModel.getB2bUnit().getUid()); } else {
	  * energizerOrderEntry = energizerOrderService.getExistingOrderItem(energizerOrderModel, existEnergizerProduct); if
	  * (energizerOrderEntry == null) { energizerOrderEntry = modelService.create(OrderEntryModel.class);
	  * energizerOrderEntry.setOrder(energizerOrderModel); energizerOrderEntry.setProduct(existEnergizerProduct);
	  * energizerOrderEntry.setQuantity(orderEntryQty); energizerOrderEntry.setTotalPrice(totalPrice); } else {
	  * energizerOrderEntry.setAdjustedQty((energizerOrderEntry.getAdjustedQty() == null) ? orderEntryQty.intValue() :
	  * orderEntryQty.intValue() + energizerOrderEntry.getAdjustedQty());
	  * energizerOrderEntry.setAdjustedLinePrice((energizerOrderEntry.getAdjustedLinePrice() == null) ? BigDecimal
	  * .valueOf(totalPrice) : energizerOrderEntry.getAdjustedLinePrice().add(BigDecimal.valueOf(totalPrice))); }
	  * energizerOrderEntry.setUnit(existUnit); energizerOrderEntry.setBasePrice(unitPrice);
	  * energizerOrderEntry.setItemTotalShipment(itemTotalShipment);
	  *
	  * if (unitPrice != null && itemTotalDiscount != null) { //Create Discount value list and attach with order entry final
	  * DiscountValue discountValue1 = new DiscountValue("SAP Discount", unitPrice, true, itemTotalDiscount,
	  * energizerOrderModel.getCurrency().getIsocode()); final List<DiscountValue> discountValuesList = new
	  * ArrayList<DiscountValue>(); discountValuesList.add(discountValue1);
	  * energizerOrderEntry.setDiscountValues(discountValuesList); }
	  *
	  * if (unitPrice != null && itemTax != null) { //Create Tax value list and attach with order entry final TaxValue
	  * taxValue1 = new TaxValue("SAP TAX", unitPrice, true, itemTax, energizerOrderModel.getCurrency() .getIsocode()); final
	  * List<TaxValue> taxValuesList = new ArrayList<TaxValue>(); taxValuesList.add(taxValue1);
	  * energizerOrderEntry.setTaxValues(taxValuesList); } } } return energizerOrderEntry;
	  */

		OrderEntryModel energizerOrderEntry = null;

		EnergizerProductModel existEnergizerProduct = null;
		EnergizerCMIRModel energizerCMIRModel = null;

		UnitModel existUnit;
		try
		{
			existEnergizerProduct = (EnergizerProductModel) productService.getProductForCode(erpMaterialID);
			energizerCMIRModel = energizerProductService.getEnergizerCMIR(erpMaterialID, energizerOrderModel.getB2bUnit().getUid());
			existUnit = unitService.getUnitForCode("EA");
		}
		catch (final Exception e)
		{
			existUnit = null;
		}
		final EnergizerCSVFeedError errors = new EnergizerCSVFeedError();
		if (existEnergizerProduct == null || energizerCMIRModel == null)
		{
			if (existEnergizerProduct == null)
			{
				LOG.info("In Record Number " + record.getRecordNumber() + " " + ERP_MATERIAL_ID + " " + erpMaterialID + "not found");
				errors.setErrorMessage(
						"In Record Number " + record.getRecordNumber() + " " + ERP_MATERIAL_ID + " " + erpMaterialID + "not found");
				techFeedErrorRecords.add(errors);
			}
			if (energizerCMIRModel == null)
			{
				LOG.info(
						"In Record Number " + record.getRecordNumber() + " " + ERP_MATERIAL_ID + " " + erpMaterialID + "no CMIR found");
				errors.setErrorMessage(
						"In Record Number " + record.getRecordNumber() + " " + ERP_MATERIAL_ID + " " + erpMaterialID + "no CMIR found");
				techFeedErrorRecords.add(errors);
			}
		}
		else
		{
			final String customerUOM = energizerCMIRModel.getUom();
			Integer customerUOMMultiplier = 0;
			Integer salesUOMMultiplier = 0;
			Integer finalConversionFactor = 0;
			/*
			 * if (energizerCMIRModel == null) { LOG.info("No CMIR exist for Record Number " + record.getRecordNumber() + " " +
			 * ERP_MATERIAL_ID + " " + erpMaterialID + " and B2B unit " + energizerOrderModel.getB2bUnit().getUid()); } else {
			 */

			energizerOrderEntry = energizerOrderService.getExistingOrderItem(energizerOrderModel, existEnergizerProduct);
			// e.g customerUOM = Pallet, uom from SAP order update = cases
			for (final EnergizerProductConversionFactorModel convertion : existEnergizerProduct.getProductConversionFactors())
			{
				if (customerUOM.equalsIgnoreCase(convertion.getAlternateUOM()))
				{
					customerUOMMultiplier = convertion.getConversionMultiplier();
				}
				if (uom.equalsIgnoreCase(convertion.getAlternateUOM()))
				{
					salesUOMMultiplier = convertion.getConversionMultiplier();
				}
			}
			finalConversionFactor = customerUOMMultiplier / salesUOMMultiplier;
			LOG.info("customerUOM " + customerUOM + " incoming UOM " + uom + " customerUOMMultiplier " + customerUOMMultiplier
					+ " salesUOMMultiplier " + salesUOMMultiplier + " finalConversionFactor " + finalConversionFactor);

			if (energizerOrderEntry == null)
			{
				energizerOrderEntry = modelService.create(OrderEntryModel.class);
				energizerOrderEntry.setOrder(energizerOrderModel);
				energizerOrderEntry.setProduct(existEnergizerProduct);
				energizerOrderEntry.setTotalPrice(totalPrice);
				if (!customerUOM.equalsIgnoreCase(uom))
				{
					energizerOrderEntry.setQuantity((orderEntryQty / finalConversionFactor));
					energizerOrderEntry.setBasePrice(unitPrice * customerUOMMultiplier);
				}
				else
				{
					energizerOrderEntry.setQuantity((orderEntryQty));
					energizerOrderEntry.setBasePrice(unitPrice * customerUOMMultiplier);
				}
			}
			else
			{
				if (!customerUOM.equalsIgnoreCase(uom))
				{
					if ((orderEntryQty / finalConversionFactor) != energizerOrderEntry.getQuantity())
					{
						// for pilot project we are only suppose to get final conversions for PAL and LAY UOM's
						// and they are always bigger than incoming UOM(sales uom)
						energizerOrderEntry.setAdjustedQty(new Integer(0));
						energizerOrderEntry.setAdjustedLinePrice(new BigDecimal("0.00"));
						// update the value with incoming value
						energizerOrderEntry.setAdjustedQty(orderEntryQty.intValue() / finalConversionFactor);
						//since the price UOM is always maintained at each (as per confirmation from Hitesh Wadhwa )
						energizerOrderEntry.setAdjustedLinePrice(BigDecimal.valueOf(totalPrice));
						energizerOrderEntry.setAdjustedItemPrice(new BigDecimal(unitPrice * customerUOMMultiplier));
					}
					else
					{
						LOG.info("orderEntryQty from SAP has not changed " + orderEntryQty / finalConversionFactor);
					}
				}
				else
				{
					if (orderEntryQty != energizerOrderEntry.getQuantity())
					{
						//clear the previous value before updating
						energizerOrderEntry.setAdjustedQty(new Integer(0));
						energizerOrderEntry.setAdjustedLinePrice(new BigDecimal("0.00"));
						// update the value with incoming value
						energizerOrderEntry.setAdjustedQty(orderEntryQty.intValue());
						energizerOrderEntry.setAdjustedItemPrice(new BigDecimal(unitPrice * customerUOMMultiplier));
						energizerOrderEntry.setAdjustedLinePrice(BigDecimal.valueOf(totalPrice));
					}
					else
					{
						LOG.info("orderEntryQty from SAP has not changed " + orderEntryQty);
					}
				}
			}
			energizerOrderEntry.setUnit(existUnit);
			energizerOrderEntry.setCustomerMaterialId(energizerCMIRModel.getCustomerMaterialId());
			energizerOrderEntry.setItemTotalShipment(itemTotalShipment);

			if (unitPrice != null && itemTotalDiscount != null)
			{
				//Create Discount value list and attach with order entry
				final DiscountValue discountValue1 = new DiscountValue("SAP Discount", unitPrice, true, itemTotalDiscount,
						energizerOrderModel.getCurrency().getIsocode());
				final List<DiscountValue> discountValuesList = new ArrayList<DiscountValue>();
				discountValuesList.add(discountValue1);
				energizerOrderEntry.setDiscountValues(discountValuesList);

			}
			if (unitPrice != null && itemTax != null)
			{
				//Create Tax value list and attach with order entry
				final TaxValue taxValue1 = new TaxValue("SAP TAX", unitPrice, true, itemTax,
						energizerOrderModel.getCurrency().getIsocode());
				final List<TaxValue> taxValuesList = new ArrayList<TaxValue>();
				taxValuesList.add(taxValue1);
				energizerOrderEntry.setTaxValues(taxValuesList);
			}
			//}
		}
		return energizerOrderEntry;

	}

	/**
	 * This function will read CSV Record and update instance variable related to CSV Processor this function will true if
	 * record is successfully read and if record is having some errors then it will return false
	 *
	 * @param record
	 * @return boolean
	 */
	private boolean readCSVRecord(final CSVRecord record)
	{
		boolean isValid = true;
		final StringBuffer errBuffer = new StringBuffer();
		errBuffer.append("Record Number " + record.getRecordNumber() + " has invalid field");
		final Map<String, String> csvValuesMap = record.toMap();

		//NumberUtils.isNumber function will check for null, empty and number
		//StringUtils.isEmpty function will check null and empty
		//Read all CSV feeds

		sapOrderNo = StringUtils.isNotBlank(csvValuesMap.get(SAP_ORDER_NO)) ? csvValuesMap.get(SAP_ORDER_NO) : null;

		createdByUser = StringUtils.isNotBlank(csvValuesMap.get(CREATED_BY_USER)) ? csvValuesMap.get(CREATED_BY_USER) : null;

		b2bAccount = StringUtils.isNotBlank(csvValuesMap.get(B2B_ACCOUNT)) ? csvValuesMap.get(B2B_ACCOUNT) : null;

		status = StringUtils.isNotBlank(csvValuesMap.get(STATUS))
				? ENERGIZER_POSSIBLE_ORDER_STATUS_MAP.get(csvValuesMap.get(STATUS))
				: null;

		containerID = StringUtils.isNotBlank(csvValuesMap.get(CONTAINER_ID)) ? csvValuesMap.get(CONTAINER_ID) : null;

		sealNumber = StringUtils.isNotBlank(csvValuesMap.get(SEAL_NUMBER)) ? csvValuesMap.get(SEAL_NUMBER) : null;

		vesselNumber = StringUtils.isNotBlank(csvValuesMap.get(VESSAL_NUMBER)) ? csvValuesMap.get(VESSAL_NUMBER) : null;

		invoicePDF = StringUtils.isNotBlank(csvValuesMap.get(INVOICE_PDF)) ? csvValuesMap.get(INVOICE_PDF) : null;

		archiveID = StringUtils.isNotBlank(csvValuesMap.get(ARCHIVE_ID)) ? csvValuesMap.get(ARCHIVE_ID) : null;

		erpMaterialID = StringUtils.isNotBlank(csvValuesMap.get(ERP_MATERIAL_ID)) ? csvValuesMap.get(ERP_MATERIAL_ID) : null;

		uom = StringUtils.isNotBlank(csvValuesMap.get(UOM)) ? csvValuesMap.get(UOM) : null;

		poNo = StringUtils.isNotBlank(csvValuesMap.get(PO_NO)) ? csvValuesMap.get(PO_NO) : null;

		totalValue = calculateNumeric(csvValuesMap.get(TOTAL_VALUE), isValid, errBuffer, TOTAL_VALUE);

		totalShipment = calculateNumeric(csvValuesMap.get(TOTAL_SHIPMENT), isValid, errBuffer, TOTAL_SHIPMENT);

		totalDiscount = calculateNumeric(csvValuesMap.get(TOTAL_DISCOUNT), isValid, errBuffer, TOTAL_DISCOUNT);

		totalTax = calculateNumeric(csvValuesMap.get(TOTAL_TAX), isValid, errBuffer, TOTAL_TAX);

		unitPrice = calculateNumeric(csvValuesMap.get(UNIT_PRICE), isValid, errBuffer, UNIT_PRICE);

		totalPrice = calculateNumeric(csvValuesMap.get(TOTAL_PRICE), isValid, errBuffer, TOTAL_PRICE);

		itemTotalShipment = calculateNumeric(csvValuesMap.get(ITEM_TOTAL_SHIPMENT), isValid, errBuffer, ITEM_TOTAL_SHIPMENT);

		itemTotalDiscount = calculateNumeric(csvValuesMap.get(ITEM_TOTAL_DISCOUNT), isValid, errBuffer, ITEM_TOTAL_DISCOUNT);

		itemTax = calculateNumeric(csvValuesMap.get(ITEM_TAX), isValid, errBuffer, ITEM_TAX);


		if (StringUtils.isNotBlank(csvValuesMap.get(CREATED_DATE)))
		{
			try
			{
				createdDate = ORDER_DATE_FORMATTER.parse(csvValuesMap.get(CREATED_DATE) + csvValuesMap.get(CREATED_TIME));
			}
			catch (final ParseException e)
			{
				isValid = false;
				errBuffer.append(
						" || " + CREATED_DATE + " = " + csvValuesMap.get(CREATED_DATE) + " has invalid format: " + e.getMessage());
			}
		}
		else
		{
			createdDate = null;
		}

		if (StringUtils.isNotBlank(csvValuesMap.get(REQ_DELIVERY_DATE)))
		{
			try
			{
				reqDeliveryDate = ORDER_DELIVERY_DATE_FORMATTER.parse(csvValuesMap.get(REQ_DELIVERY_DATE));
			}
			catch (final ParseException e)
			{
				isValid = false;
				errBuffer.append(" || " + REQ_DELIVERY_DATE + " = " + csvValuesMap.get(REQ_DELIVERY_DATE) + " has invalid format: "
						+ e.getMessage());
			}
		}
		else
		{
			reqDeliveryDate = null;
		}

		if (!StringUtils.isEmpty(csvValuesMap.get(ORDER_ENTRY_QTY)))
		{
			if (NumberUtils.isNumber(csvValuesMap.get(ORDER_ENTRY_QTY)))
			{
				try
				{
					orderEntryQty = NumberUtils.createLong(csvValuesMap.get(ORDER_ENTRY_QTY));
				}
				catch (final NumberFormatException e)
				{
					isValid = false;
					errBuffer.append(" || " + ORDER_ENTRY_QTY + " is not a integer " + e.getMessage());
				}
			}
		}
		else
		{
			orderEntryQty = null;
		}

		//Generate Log if record is not readable
		if (!isValid)
		{
			LOG.info(errBuffer);
		}

		return isValid;
	}

	/**
	 * @param record
	 * @return EnergizerCSVFeedError
	 */
	private EnergizerCSVFeedError validate(final CSVRecord record)
	{
		EnergizerCSVFeedError error = null;

		if (!hasMandatoryFields(record, getHeadersForFeed(ENERGIZER_OFFLINE_ORDER_FEED_MANDATORY_KEY)))
		{
			final List<String> mandatoryFields = Arrays.asList(
					Config.getParameter(ENERGIZER_OFFLINE_ORDER_FEED_MANDATORY_KEY).split(new Character(DELIMETER).toString()));
			error = new EnergizerCSVFeedError();
			final List<String> columnNames = new ArrayList<String>();
			final List<Integer> columnNumbers = new ArrayList<Integer>();
			final Map<String, String> map = record.toMap();
			Integer columnNumber = 0;
			long recordFailed = getRecordFailed();
			for (final String columnHeader : map.keySet())
			{
				columnNumber++;
				if (mandatoryFields.contains(columnHeader))
				{

					final String value = map.get(columnHeader);
					if (StringUtils.isEmpty(value))
					{
						columnNames.add(columnHeader);
						columnNumbers.add(columnNumber);
					}
				}
			}
			error.setLineNumber(record.getRecordNumber());
			error.setColumnName(columnNames);
			error.setColumnNumber(columnNumbers);
			error.setMessage(" column should not be empty");
			error.setUserType(TECHNICAL_USER);
			getTechnicalFeedErrors().add(error);
			setTechRecordError(getTechnicalFeedErrors().size());
			recordFailed++;
			setRecordFailed(recordFailed);
		}
		return error;
	}



}
