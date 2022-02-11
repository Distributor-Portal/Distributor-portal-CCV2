/**
 *
 * Copyright (c) 2018 on words , Edgewell B2B EMEA
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of hybris
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with hybris.
 */
package com.energizer.storefront.controllers.pages;

import de.hybris.platform.acceleratorservices.controllers.page.PageType;
import de.hybris.platform.b2b.company.B2BCommerceUserService;
import de.hybris.platform.cms2.exceptions.CMSItemNotFoundException;
import de.hybris.platform.commercefacades.order.CartFacade;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.order.data.CartModificationData;
import de.hybris.platform.commercefacades.order.data.CartRestorationData;
import de.hybris.platform.commercefacades.order.data.OrderEntryData;
import de.hybris.platform.commercefacades.product.data.PriceData;
import de.hybris.platform.commercefacades.product.data.ProductData;
import de.hybris.platform.commercefacades.storesession.StoreSessionFacade;
import de.hybris.platform.commerceservices.order.CommerceCartModificationException;
import de.hybris.platform.europe1.model.PriceRowModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.util.Config;
import de.hybris.platform.util.localization.Localization;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.energizer.business.BusinessRuleError;
import com.energizer.core.business.service.EnergizerOrderEntryBusinessRuleValidationService;
import com.energizer.core.constants.EnergizerCoreConstants;
import com.energizer.core.model.EnergizerB2BUnitModel;
import com.energizer.core.model.EnergizerCMIRModel;
import com.energizer.core.model.EnergizerPriceRowModel;
import com.energizer.core.model.EnergizerProductModel;
import com.energizer.core.product.data.EnergizerFileUploadData;
import com.energizer.facades.flow.impl.DefaultEnergizerB2BCartFacade;
import com.energizer.facades.flow.impl.DefaultEnergizerB2BCheckoutFlowFacade;
import com.energizer.facades.order.EnergizerExcelUploadFacade;
import com.energizer.facades.quickorder.EnergizerQuickOrderFacade;
import com.energizer.services.order.EnergizerCartService;
import com.energizer.services.product.EnergizerProductService;
import com.energizer.storefront.annotations.RequireHardLogIn;
import com.energizer.storefront.breadcrumb.ResourceBreadcrumbBuilder;
import com.energizer.storefront.constants.WebConstants;
import com.energizer.storefront.controllers.ControllerConstants;
import com.energizer.storefront.controllers.ControllerConstants.Views;
import com.energizer.storefront.controllers.util.GlobalMessages;
import com.energizer.storefront.forms.ContainerUtilizationForm;
import com.energizer.storefront.forms.ExcelUploadForm;
import com.energizer.storefront.forms.UpdateExpectedUnitPriceForm;
import com.energizer.storefront.forms.UpdateQuantityForm;


/**
 * @author Sakib Hassan
 *
 */

@Controller
@Scope("tenant")
@RequestMapping("/my-cart")
public class ExcelUploadPageController extends AbstractSearchPageController
{

	protected static final Logger LOG = Logger.getLogger(ExcelUploadPageController.class);

	private static final String PRODUCT_ENTRIES_PAGE = "productentries";
	private static final String CART_CMS_PAGE = "cartPage";
	private static final String CONTINUE_URL = "continueUrl";
	private static final String EXCEL_ORDER_AJAX_CALL = "/excelUpload/updateOrderQuantity";
	private static final String CART = "/cart";
	private static final String FREIGHT_TRUCK = "Truck";
	private static final String REGION_EMEA = "EMEA";
	private static final String FREIGHT_CONTAINER = "Container";
	private static final String ORDER_EXCEEDED = "container.business.rule.orderExceeded";
	private static final String ORDER_BLOCKED = "container.business.rule.orderblocked";
	//Added for EMEA Truck optimization enhancement - START
	private static final String ORDER_EXCEEDED_EMEA = "truck.business.rule.orderExceeded";
	private static final String ORDER_BLOCKED_EMEA = "truck.business.rule.orderblocked";
	//Added for EMEA Truck optimization enhancement - END

	private static int ZERO = 0;
	private static String EMPTY = "";

	@Resource(name = "accountBreadcrumbBuilder")
	private ResourceBreadcrumbBuilder accountBreadcrumbBuilder;

	@Resource
	private EnergizerExcelUploadFacade energizerExcelRowtoModelFacade;

	@Resource
	private EnergizerOrderEntryBusinessRuleValidationService shippingPointBusinessRulesService;

	@Resource
	private EnergizerOrderEntryBusinessRuleValidationService cartEntryBusinessRulesService;

	@Deprecated
	@Resource(name = "cartFacade")
	private CartFacade cartFacade;

	@Resource(name = "sessionService")
	private SessionService sessionService;

	@Resource(name = "simpleBreadcrumbBuilder")
	private ResourceBreadcrumbBuilder resourceBreadcrumbBuilder;

	@Resource(name = "quickOrderFacade")
	private EnergizerQuickOrderFacade quickOrderFacade;

	@Resource
	EnergizerCartService energizerCartService;

	@Resource
	private UserService userService;

	@Resource
	private B2BCommerceUserService b2bCommerceUserService;

	@Resource
	EnergizerProductService energizerProductService;

	@Resource(name = "energizerB2BCheckoutFlowFacade")
	private DefaultEnergizerB2BCheckoutFlowFacade energizerB2BCheckoutFlowFacade;

	@Resource
	DefaultEnergizerB2BCartFacade energizerB2BCartFacade;

	@Resource(name = "storeSessionFacade")
	private StoreSessionFacade storeSessionFacade;

	ContainerUtilizationForm contUtilForm = new ContainerUtilizationForm();

	String containerHeight, packingOption;

	boolean enableButton = false;

	boolean enableForB2BUnit = false;

	@Value("${excelFileSize}")
	private String excelFileSize;

	@Resource(name = "configurationService")
	private ConfigurationService configurationService;

	Map<String, List<EnergizerFileUploadData>> shipmentMap = new HashMap<String, List<EnergizerFileUploadData>>();


	Map<String, String> shipmentNameMap = new HashMap<String, String>();

	@RequestMapping(value = "/excelFileToUpload", method = RequestMethod.POST)
	@RequireHardLogIn
	public String uploadExcelFile(final Model model, @RequestParam("file") final CommonsMultipartFile file)
			throws CMSItemNotFoundException
	{

		storeCmsPageInModel(model, getContentPageForLabelOrId(PRODUCT_ENTRIES_PAGE));
		setUpMetaDataForContentPage(model, getContentPageForLabelOrId(PRODUCT_ENTRIES_PAGE));
		model.addAttribute("breadcrumbs", accountBreadcrumbBuilder.getBreadcrumbs("text.account.excelFileUpload"));
		model.addAttribute("metaRobots", "no-index,no-follow");

		List<EnergizerFileUploadData> energizerExcelUploadModels = new ArrayList<EnergizerFileUploadData>();
		final EnergizerB2BUnitModel b2bUnit = b2bCommerceUserService.getParentUnitForCustomer(this.getUser().getUid());

		if (shipmentMap != null && !shipmentMap.keySet().isEmpty())
		{
			shipmentMap.clear();
		}

		if (file == null || file.isEmpty())
		{
			GlobalMessages.addErrorMessage(model, "text.account.excelUpload.fileUploadEmpty");
			return ControllerConstants.Views.Pages.Account.AccountExcelUpload;
		}
		else if (file.getSize() > Double.valueOf(excelFileSize))
		{
			GlobalMessages.addErrorMessage(model, "text.account.excelUpload.fileUploadSize");
			return ControllerConstants.Views.Pages.Account.AccountExcelUpload;
		}
		else if (!file.isEmpty())
		{
			Iterator<Row> rowIterator = null;

			LOG.info(" Name of the file " + file.getOriginalFilename());

			try
			{
				EnergizerFileUploadData uploadData = null;
				String productsObsoleted = "";
				int productsObsoletedCount = 0;
				final List<EnergizerFileUploadData> energizerFileUploadDatas = new ArrayList<EnergizerFileUploadData>();
				if (file.getOriginalFilename().endsWith(".xls"))
				{
					final HSSFWorkbook hworkbook = new HSSFWorkbook(file.getInputStream());
					final HSSFSheet hsheet = hworkbook.getSheetAt(0);
					rowIterator = hsheet.rowIterator();
				}
				else if (file.getOriginalFilename().endsWith(".xlsx"))
				{
					final XSSFWorkbook xworkbook = new XSSFWorkbook(file.getInputStream());
					final XSSFSheet xsheet = xworkbook.getSheetAt(0);
					rowIterator = xsheet.rowIterator();
				}
				else
				{
					LOG.warn("Format doesn't support, upload only xls and xlsx types");
					GlobalMessages.addErrorMessage(model, "text.account.excelUpload.fileUploadFormat");
					return ControllerConstants.Views.Pages.Account.AccountExcelUpload;
				}

				int materialMissingCount = 0;
				int quantityMissingCount = 0;
				int quantityZeroCount = 0;
				int totalRows = 0;

				while (rowIterator.hasNext())
				{
					totalRows += 1;

					final Row row = rowIterator.next();

					if (!(row.getRowNum() == 0))
					{
						String materialId = validateAndGetString(row.getCell(0));
						String customerMaterailId = validateAndGetString(row.getCell(1));

						if (null != materialId && !StringUtils.isEmpty(materialId))
						{
							materialId = materialId.trim();
						}
						else
						{
							materialId = null;
							LOG.info("Material ID null/empty at row number : " + row.getRowNum());
						}

						if (null != customerMaterailId && !StringUtils.isEmpty(customerMaterailId))
						{
							customerMaterailId = customerMaterailId.trim();
						}
						else
						{
							customerMaterailId = null;
							LOG.info("Customer MaterailId ID null/empty at row number : " + row.getRowNum());
						}

						if (materialId != null || customerMaterailId != null)
						{
							uploadData = new EnergizerFileUploadData();
							uploadData.setCustomerMaterialId(customerMaterailId);
							uploadData.setMaterialId(materialId);

							if (null != row.getCell(3))
							{
								try
								{
									final String val = (StringUtils.isNotEmpty(row.getCell(3).toString())
											? row.getCell(3).toString().trim()
											: "0");
									final Long quantity = new Double(val).longValue();

									if (quantity > 0)
									{
										//uploadData.setCustomerMaterialId(customerMaterailId);
										//uploadData.setMaterialId(materialId);

										/*
										 * Quantity default to '1' for LATAM(since it is not present in catalog sheet), actual quantity for
										 * EMEA & WESELL - START
										 */
										if (this.getSiteUid().equalsIgnoreCase(EnergizerCoreConstants.SITE_PERSONALCARE)
												&& !isSalesRepUserLogin())
										{
											uploadData.setQuantity(Long.parseLong("1"));
										}
										else
										{
											uploadData.setQuantity(quantity);
										}
										/* Quantity default to '1' for LATAM, actual quantity for EMEA & WESELL - END */


										final String salesOrgString = energizerProductService.getProductWithCode(uploadData.getMaterialId())
												.getNonObsoleteSalesOrgsString();
										boolean uploadProduct = true;
										if (null != salesOrgString && StringUtils.isNotEmpty(salesOrgString)
												&& !salesOrgString.contains(b2bUnit.getSalesOrganisation()))
										{
											uploadProduct = false;
											LOG.info("Material is obsoleted. Cannot be added to the order : '" + uploadData.getMaterialId()
													+ "' !! ");
											productsObsoleted += uploadData.getMaterialId() + "  ";
											productsObsoletedCount += 1;
										}
										if (uploadProduct)
										{
											energizerFileUploadDatas.add(uploadData);
										}
									}
									else
									{
										/*-LOG.debug("Quantity is less than zero(0) for materialId : " + materialId + " at row number : "
												+ row.getRowNum() + ". So ignoring that material for upload ...");*/
										quantityZeroCount += 1;
										//uploadData.setQuantity(Long.parseLong("0"));
									}
								}
								catch (final Exception ise)
								{
									LOG.error("cannot convert " + row.getCell(2) + " into number");
									GlobalMessages.addErrorMessage(model, "text.account.excelUpload.badDataForQuantity");
								}
							}
							else
							{
								/*-LOG.info("row.getCell(3)/Quantity is NULL/EMPTY for materialId : '" + materialId + "' at row number : '"
										+ row.getRowNum() + "' !! So ignoring that material for upload ...");*/
								quantityMissingCount += 1;
								//uploadData.setQuantity(Long.parseLong("0"));
							}

							/*-final String salesOrgString = energizerProductService.getProductWithCode(uploadData.getMaterialId())
									.getNonObsoleteSalesOrgsString();
							boolean uploadProduct = true;
							if (null != salesOrgString && StringUtils.isNotEmpty(salesOrgString)
									&& !salesOrgString.contains(b2bUnit.getSalesOrganisation()))
							{
								uploadProduct = false;
								LOG.info(
										"Material is obsoleted. Cannot be added to the order : '" + uploadData.getMaterialId() + "' !! ");
								productsObsoleted += uploadData.getMaterialId() + "  ";
								productsObsoletedCount += 1;
							}
							if (uploadProduct)
							{
								energizerFileUploadDatas.add(uploadData);
							}*/
						}
						else
						{
							LOG.info("MaterialId and CustomerMaterailId is NULL at row number '" + row.getRowNum() + "' !");
							materialMissingCount += 1;
						}
					}
				}
				LOG.info("Total rows in excel sheet : " + totalRows + " , material records : " + (totalRows - 1));
				LOG.info("Missing Materials : '" + materialMissingCount + "' , Missing Quantity : '" + quantityMissingCount
						+ "', Zero Quantity : '" + quantityZeroCount + "'");
				LOG.info("So ignoring '" + (materialMissingCount + quantityMissingCount + quantityZeroCount)
						+ "' products for upload !!");
				LOG.info("Materials uploaded : " + energizerFileUploadDatas.size());

				if (energizerFileUploadDatas.size() == 0)
				{
					GlobalMessages.addErrorMessage(model, "text.account.excelUpload.noRowsFound");
					return ControllerConstants.Views.Pages.Account.AccountExcelUpload;
				}

				if (productsObsoletedCount > 0)
				{
					GlobalMessages.addMessage(model, "accErrorMsgs", "excel.order.products.obsoleted", new Object[]
					{ productsObsoleted });
					//return FORWARD_PREFIX + "/cart";
				}

				energizerExcelUploadModels = energizerExcelRowtoModelFacade.convertExcelRowtoBean(energizerFileUploadDatas);
				for (final EnergizerFileUploadData energizerCMIRModel : energizerExcelUploadModels)
				{
					if (energizerCMIRModel.isHasError())
					{
						GlobalMessages.addMessage(model, "accErrorMsgs", "text.account.excelUpload.productnotfound", new Object[]
						{ energizerCMIRModel.getMessage() });
					}
					else
					{
						final String shipmentPointId = (energizerCMIRModel.getShippingPoint());
						shipmentNameMap.put(shipmentPointId, energizerProductService.getShippingPointName(shipmentPointId));
						if (shipmentMap.containsKey(shipmentPointId))
						{
							final List<EnergizerFileUploadData> tempList = shipmentMap.get(shipmentPointId);
							tempList.add(energizerCMIRModel);
						}
						else
						{
							final List<EnergizerFileUploadData> uploadDataCMIRList = new ArrayList<EnergizerFileUploadData>();
							uploadDataCMIRList.add(energizerCMIRModel);
							shipmentMap.put(shipmentPointId, uploadDataCMIRList);

						}
					}
				}
				model.addAttribute("shipmentData", shipmentMap);
				model.addAttribute("shipmentName", shipmentNameMap);

			}
			catch (final FileNotFoundException fne)
			{
				LOG.error("File Not Found " + fne.getMessage());
				fne.printStackTrace();
				GlobalMessages.addErrorMessage(model, "text.account.excelUpload.fileNotFound");
				return ControllerConstants.Views.Pages.Account.AccountExcelUpload;
			}
			catch (final IOException e)
			{
				LOG.error("Unable to convert the file into stream " + e.getMessage());
				e.printStackTrace();
				GlobalMessages.addErrorMessage(model, "text.account.excelUpload.unableToConvert");
				return ControllerConstants.Views.Pages.Account.AccountExcelUpload;
			}
			catch (final Exception e)
			{
				LOG.error(e.getMessage());
				e.printStackTrace();
				return ControllerConstants.Views.Pages.Account.AccountExcelUpload;
			}
		}

		model.addAttribute("cartData", quickOrderFacade.getCurrentSessionCart());
		return ControllerConstants.Views.Pages.Account.AccountExcelUploadEntries;
	}

	@RequestMapping(value = "/addtocart", method = RequestMethod.POST)
	@RequireHardLogIn
	public String addtocart(@ModelAttribute("excelUploadForm") final ExcelUploadForm excelUploadForm, final Model model,
			final HttpSession session) throws CMSItemNotFoundException
	{

		final Map<String, String> shippingLocationMap = new HashMap<String, String>();

		try
		{

			final List<String> shipmentPointList = excelUploadForm.getShippingPoint();
			/*-final List<String> shippingPointLocation = excelUploadForm.getShippingPointLocation();
			
			LOG.error("shippingPointLocation" + shippingPointLocation);*/

			final List<EnergizerFileUploadData> orderEntryList = new ArrayList<EnergizerFileUploadData>();
			final List<BusinessRuleError> orderEntryErrors = new ArrayList<BusinessRuleError>();

			if (shippingPointBusinessRulesService.getErrors() != null && !shippingPointBusinessRulesService.getErrors().isEmpty())
			{
				shippingPointBusinessRulesService.getErrors().clear();
			}
			if (cartEntryBusinessRulesService.getErrors() != null && !cartEntryBusinessRulesService.getErrors().isEmpty())
			{
				cartEntryBusinessRulesService.getErrors().clear();
			}

			for (final String shipmentPoint : shipmentPointList)
			{
				if (getSiteUid().equalsIgnoreCase(EnergizerCoreConstants.SITE_PERSONALCAREEMEA))
				{
					//Key - ShippingPointLocation, Value - ShippingPointId
					shippingLocationMap.put(getShippingPointLocation(shipmentPoint), getShippingPointName(shipmentPoint));
				}
			}

			for (final String shipmentPoint : shipmentPointList)
			{
				if (shipmentMap.containsKey(shipmentPoint))
				{
					final List<EnergizerFileUploadData> productsList = shipmentMap.get(shipmentPoint);
					for (final EnergizerFileUploadData energizerFileUploadData : productsList)
					{
						final EnergizerCMIRModel cmir = quickOrderFacade.getCMIRForProductCodeOrCustomerMaterialID(
								energizerFileUploadData.getMaterialId(), energizerFileUploadData.getCustomerMaterialId());

						final OrderEntryData orderEntry = quickOrderFacade.getProductData(energizerFileUploadData.getMaterialId(),
								energizerFileUploadData.getCustomerMaterialId(), cmir);

						model.addAttribute("shipmentData", shipmentMap);
						model.addAttribute("shipmentName", shipmentNameMap);


						if (orderEntry != null)
						{
							orderEntry.setQuantity(energizerFileUploadData.getQuantity());
							model.addAttribute("cartShippingPoint",
									orderEntry.getReferenceShippingPoint() != null ? orderEntry.getReferenceShippingPoint() : "");

							model.addAttribute("cartShippingPointLocation",
									orderEntry.getReferenceShippingPointLocation() != null ? orderEntry.getReferenceShippingPointLocation()
											: "");

							shippingPointBusinessRulesService.validateBusinessRules(orderEntry);
							cartEntryBusinessRulesService.validateBusinessRules(orderEntry);

							if (!shippingPointBusinessRulesService.hasErrors() && !cartEntryBusinessRulesService.hasErrors())
							{
								orderEntryList.add(energizerFileUploadData);
							}
							if (shippingPointBusinessRulesService.hasErrors())
							{
								if (getSiteUid().equalsIgnoreCase(EnergizerCoreConstants.SITE_PERSONALCAREEMEA)
										&& shippingLocationMap.size() <= 1)
								{
									orderEntryErrors.addAll(shippingPointBusinessRulesService.getErrors());
									shippingPointBusinessRulesService.getTempErrors().clear();
								}
							}
							if (cartEntryBusinessRulesService.hasErrors())
							{
								orderEntryErrors.addAll(cartEntryBusinessRulesService.getErrors());
								cartEntryBusinessRulesService.getTempErrors().clear();
							}
						}
						else
						{
							GlobalMessages.addErrorMessage(model, "quickorder.addtocart.cmir.badData");
						}
					}
				}
			}

			if (getSiteUid().equalsIgnoreCase(EnergizerCoreConstants.SITE_PERSONALCAREEMEA))
			{
				// If the user selects items/plants from multiple shipping point location, throw an error message.
				if (shippingLocationMap.size() > 1)
				{
					LOG.info("Shipping Point Locations ::: " + shippingLocationMap);
					LOG.info("Please choose items from the same shipping point location!!!");
					final BusinessRuleError error = new BusinessRuleError();
					error.setMessage(Localization.getLocalizedString(EnergizerCoreConstants.CHOOSE_ONE_SHIPPING_POINT_LOCATION));
					orderEntryErrors.add(error);
				}
			}

			if (orderEntryErrors != null && orderEntryErrors.size() > 0)
			{
				storeCmsPageInModel(model, getContentPageForLabelOrId(PRODUCT_ENTRIES_PAGE));
				setUpMetaDataForContentPage(model, getContentPageForLabelOrId(PRODUCT_ENTRIES_PAGE));
				model.addAttribute("breadcrumbs", accountBreadcrumbBuilder.getBreadcrumbs("text.account.excelFileUpload"));
				model.addAttribute("metaRobots", "no-index,no-follow");

				for (final BusinessRuleError error : orderEntryErrors)
				{
					LOG.info("The error message is " + error.getMessage());
					GlobalMessages.addBusinessRuleMessage(model, error.getMessage());
				}
				return ControllerConstants.Views.Pages.Account.AccountExcelUploadEntries;
			}
			else
			{
				for (final EnergizerFileUploadData entry : orderEntryList)
				{
					try
					{
						final CartModificationData modification = cartFacade.addToCart(entry.getMaterialId(), entry.getQuantity());

						final OrderEntryData orderEntryData = getOrderEntryDataForEachUnitPrice(entry.getQuantity(),
								entry.getMaterialId(), null);

						// Setting entry number to the order entry data from recently created/saved cart entry model
						orderEntryData.setEntryNumber(modification.getEntry().getEntryNumber());

						// Setting each unit price to the cart/order entry model & saving it to DB
						energizerB2BCartFacade.updateOrderEntryForEachUnitPrice(orderEntryData);

					}
					catch (final CommerceCartModificationException e)
					{
						LOG.error("Problem in adding items to Cart ::: " + e.getMessage());
						e.printStackTrace();
					}
				}
			}

			/*
			 * if (session.getAttribute("containerHeight") != null && session.getAttribute("packingOption") != null) {
			 * contUtilForm.setContainerHeight((String) session.getAttribute("containerHeight"));
			 * contUtilForm.setPackingType((String) session.getAttribute("packingOption")); }
			 */

			final String userId = userService.getCurrentUser().getUid();
			final EnergizerB2BUnitModel b2bUnit = b2bCommerceUserService.getParentUnitForCustomer(userId);

			//final String PERSONALCARE = getConfigValue("site.personalCare");
			final String PERSONALCARE_EMEA = getConfigValue("site.personalCareEMEA");
			final String PERSONALCARE = getConfigValue("site.personalCare");

			if (null != b2bUnit.getFreightType() && !StringUtils.isEmpty(b2bUnit.getFreightType())
					&& getSiteUid().equalsIgnoreCase(PERSONALCARE_EMEA) && (FREIGHT_TRUCK.equalsIgnoreCase(b2bUnit.getFreightType())
							|| FREIGHT_CONTAINER.equalsIgnoreCase(b2bUnit.getFreightType())))
			{
				if (REGION_EMEA.equals(b2bUnit.getRegion()))
				{
					model.addAttribute("isEmeaUser", true);
				}

				final boolean enableTruck = b2bUnit.getEnableContainerOptimization() == null ? false
						: b2bUnit.getEnableContainerOptimization();
				final boolean enableForB2BUnit = b2bUnit.getEnableContainerOptimization() == null ? false
						: b2bUnit.getEnableContainerOptimization();

				prepareDataForPage(model);

				model.addAttribute("enableTruck", enableTruck);
				model.addAttribute("enableForB2BUnit", enableForB2BUnit);
			}
			else if ((null == b2bUnit.getFreightType() || StringUtils.isEmpty(b2bUnit.getFreightType())
					|| FREIGHT_CONTAINER.equalsIgnoreCase(b2bUnit.getFreightType()))
					&& this.getCmsSiteService().getCurrentSite().getUid().equalsIgnoreCase(PERSONALCARE))
			{
				enableButton = b2bUnit.getEnableContainerOptimization() == null ? false : b2bUnit.getEnableContainerOptimization();

				boolean enableForB2BUnit = b2bUnit.getEnableContainerOptimization() == null ? false
						: b2bUnit.getEnableContainerOptimization();
				final CartData cartData = cartFacade.getSessionCart();
				String ShippingPointNo = null;
				//	reverseCartProductsOrder(cartData.getEntries());
				if (cartData.getEntries() != null && !cartData.getEntries().isEmpty())
				{
					for (final OrderEntryData entry : cartData.getEntries())
					{
						ShippingPointNo = entry.getProduct().getShippingPoint();
						if (ShippingPointNo != null)
						{
							break;
						}
					}
				}

				if (ShippingPointNo != null && ShippingPointNo.equals("867"))
				{
					enableButton = false;
					enableForB2BUnit = false;
				}
				//				WeSell Implementation -  Added Code Changes for Container Optimization for Sales Rep login - by Venkat
				if (isSalesRepUserLogin())
				{
					enableButton = false;
				}
				//				WeSell Implementation -  Added Code Changes for Container Optimization for Sales Rep login - by Venkat
				prepareDataForPage(model);
				model.addAttribute("enableButton", enableButton);
				model.addAttribute("enableForB2BUnit", enableForB2BUnit);

			}
			shipmentMap.clear();

		}
		catch (final Exception e)
		{
			LOG.info(e.getMessage());
			e.printStackTrace();
		}

		// Added this session attribute for WeSell Implementation - to disable the checkout button in cart thereby forcing the Sales Rep to get the realtime prices from SAPA
		if ((boolean) sessionService.getAttribute("isSalesRepUserLoggedIn"))
		{
			session.setAttribute("gotPriceFromSAP", false);
		}
		//Added Code changes for WeSell Implementation - END

		return ControllerConstants.Views.Pages.Cart.CartPage;
	}

	protected void prepareDataForPage(final Model model) throws CMSItemNotFoundException
	{
		try
		{
			final String continueUrl = (String) getSessionService().getAttribute(WebConstants.CONTINUE_URL);
			model.addAttribute(CONTINUE_URL, (continueUrl != null && !continueUrl.isEmpty()) ? continueUrl : ROOT);

			if (sessionService.getAttribute(WebConstants.CART_RESTORATION) instanceof CartRestorationData)
			{
				final CartRestorationData restorationData = (CartRestorationData) sessionService
						.getAttribute(WebConstants.CART_RESTORATION);
				model.addAttribute("restorationData", restorationData);
			}
			createProductList(model);
		}
		catch (final Exception e)
		{
			LOG.info("Exception occured ::: " + e.getMessage());
			e.printStackTrace();
		}

		model.addAttribute(WebConstants.BREADCRUMBS_KEY, resourceBreadcrumbBuilder.getBreadcrumbs("breadcrumb.cart"));
		model.addAttribute("pageType", PageType.CART.name());
	}

	protected void createProductList(final Model model) throws Exception
	{
		CartData cartData = cartFacade.getSessionCart();
		final List<String> businessRuleErrors = new ArrayList<String>();
		boolean errorMessages = false;
		boolean cartEntriesSizeExceed = false;
		final String CartEntriesMaxLimit = configurationService.getConfiguration().getString("wesell.cart.entries.maximum.size");
		int limit = 1;
		if (null != CartEntriesMaxLimit)
		{
			limit = limit + Integer.parseInt(CartEntriesMaxLimit);
		}
		else
		{
			limit = 101;
		}
		if (isSalesRepUserLogin() && null != cartData && cartData.getEntries() != null && !cartData.getEntries().isEmpty()
				&& cartData.getEntries().size() >= limit)
		{
			cartEntriesSizeExceed = true;
			GlobalMessages.addErrorMessage(model, "cart.entries.size.exceed");
		}
		model.addAttribute("cartEntriesSizeExceed", cartEntriesSizeExceed);
		//	reverseCartProductsOrder(cartData.getEntries());
		if (cartData.getEntries() != null && !cartData.getEntries().isEmpty())
		{
			boolean flag = false;
			String productWithCmirInActive = "";
			int agreeEdgewellUnitPriceForAllProducts = 0;
			for (OrderEntryData entry : cartData.getEntries())
			{
				final UpdateQuantityForm uqf = new UpdateQuantityForm();
				uqf.setQuantity(entry.getQuantity());
				model.addAttribute("updateQuantityForm" + entry.getEntryNumber(), uqf);

				final UpdateExpectedUnitPriceForm updateExpectedUnitPriceForm = new UpdateExpectedUnitPriceForm();
				if (entry.getExpectedUnitPrice() != null)
				{
					updateExpectedUnitPriceForm.setExpectedUnitPrice(String.valueOf(entry.getExpectedUnitPrice()));
				}
				else
				{
					updateExpectedUnitPriceForm.setExpectedUnitPrice(StringUtils.EMPTY);
				}
				updateExpectedUnitPriceForm.setAgreeEdgewellUnitPrice(entry.isAgreeEdgewellUnitPrice());

				// Update the each unit price for the already existing cart with null each unit price. This avoids error during cart page updates.
				if (null == entry.getEachUnitPrice() || null == entry.getEachUnitPrice().getValue())
				{
					entry = energizerB2BCartFacade.getOrderEntryDataForEachUnitPrice(entry);
					// Setting each unit price to the cart/order entry model
					energizerB2BCartFacade.updateOrderEntryForEachUnitPrice(entry);
				}

				model.addAttribute("UpdateExpectedUnitPriceForm" + entry.getEntryNumber(), updateExpectedUnitPriceForm);

				if (entry.getProduct().isIsActive() == false)
				{
					productWithCmirInActive += entry.getProduct().getErpMaterialID() + "  ";
					flag = true;

				}

				// Check if customer disagrees edgewell price for at least one product
				if (!entry.isAgreeEdgewellUnitPrice())
				{
					agreeEdgewellUnitPriceForAllProducts += 1;
				}

			}
			if (flag == true)
			{
				GlobalMessages.addMessage(model, "accErrorMsgs", "cart.cmirinactive", new Object[]
				{ productWithCmirInActive });
				//return FORWARD_PREFIX + "/cart";
			}

			// If customer disagrees edgewell price for atleast one product, then the header level agree flag should be false. Else, it will be true.
			if (agreeEdgewellUnitPriceForAllProducts > 0)
			{
				cartData.setAgreeEdgewellUnitPriceForAllProducts(Boolean.valueOf(false));
			}
			else if (agreeEdgewellUnitPriceForAllProducts == 0)
			{
				cartData.setAgreeEdgewellUnitPriceForAllProducts(Boolean.valueOf(true));
			}

			final UpdateExpectedUnitPriceForm updateExpectedUnitPriceForm = new UpdateExpectedUnitPriceForm();
			updateExpectedUnitPriceForm.setAgreeEdgewellUnitPriceForAllProducts(cartData.isAgreeEdgewellUnitPriceForAllProducts());
			model.addAttribute("UpdateExpectedUnitPriceForm", updateExpectedUnitPriceForm);

		}

		/** Energizer Container Utilization service */

		final String userId = userService.getCurrentUser().getUid();
		final EnergizerB2BUnitModel b2bUnit = b2bCommerceUserService.getParentUnitForCustomer(userId);

		LOG.info(" Freight Type: " + b2bUnit.getFreightType());

		final String PERSONALCARE = getConfigValue("site.personalCare");
		final String PERSONALCARE_EMEA = getConfigValue("site.personalCareEMEA");

		// Calculation logic for Container which is for LATAM
		if (((null == b2bUnit.getFreightType() || StringUtils.isEmpty(b2bUnit.getFreightType())
				|| FREIGHT_CONTAINER.equalsIgnoreCase(b2bUnit.getFreightType()))
				&& this.getCmsSiteService().getCurrentSite().getUid().equalsIgnoreCase(PERSONALCARE)))
		{
			//			WeSell Implementation -  Added Code Changes for Container Optimization for Sales Rep login - by Venkat
			if (!isSalesRepUserLogin())
			{
				//				WeSell Implementation -  Added Code Changes for Container Optimization for Sales Rep login - by Venkat
				LOG.info(" Enable Container Optimization value: " + enableButton);

				if (contUtilForm.getContainerHeight() != null && contUtilForm.getPackingType() != null)
				{
					containerHeight = contUtilForm.getContainerHeight();
					packingOption = contUtilForm.getPackingType();
				}
				else
				{
					containerHeight = Config.getParameter("energizer.default.containerHeight");
					packingOption = Config.getParameter("energizer.default.packingOption");
				}

				cartData = energizerCartService.calCartContainerUtilization(cartData, containerHeight, packingOption, enableButton);

				if (cartData.isIsFloorSpaceFull() && cartData.getContainerPackingType().equalsIgnoreCase("2 SLIP SHEETS")
						&& enableButton)
				{
					GlobalMessages.addErrorMessage(model, "errorMessages.enable.2slipsheet");
				}

				if (cartData.isIsOrderBlocked())
				{
					businessRuleErrors.add(Localization.getLocalizedString(ORDER_BLOCKED));
				}

				final List<String> message = energizerCartService.getMessages();
				if (message != null && message.size() > 0)
				{
					for (final String messages : message)
					{
						if (messages.contains("20"))
						{
							GlobalMessages.addMessage(model, "accErrorMsgs", "errormessage.greaterthan.totalpalletcount", new Object[]
							{ "20" });
						}
						else if (messages.contains("40"))
						{
							GlobalMessages.addMessage(model, "accErrorMsgs", "errormessage.greaterthan.totalpalletcount", new Object[]
							{ "40" });
						}
						else if (message.contains("2 wooden base packing material"))
						{
							GlobalMessages.addErrorMessage(model, "errormessage.partialpallet");
						}
						else
						{
							GlobalMessages.addErrorMessage(model, messages);
						}
						businessRuleErrors.add(messages);
					}
					errorMessages = true;
				}

				cartData.setBusinesRuleErrors(businessRuleErrors);

				final List<String> containerHeightList = Arrays
						.asList(Config.getParameter("possibleContainerHeights").split(new Character(',').toString()));

				final List<String> packingOptionsList;
				if (containerHeight.equals("20FT"))
				{
					packingOptionsList = Arrays
							.asList(Config.getParameter("possiblePackingOptions.20FT").split(new Character(',').toString()));
				}
				else
				{

					packingOptionsList = Arrays
							.asList(Config.getParameter("possiblePackingOptions").split(new Character(',').toString()));
				}

				contUtilForm.setContainerHeight(containerHeight);
				contUtilForm.setPackingType(packingOption);
				cartData.setFloorSpaceProductsMap(energizerCartService.getFloorSpaceProductsMap());
				cartData.setNonPalletFloorSpaceProductsMap(energizerCartService.getNonPalletFloorSpaceProductsMap());
				cartData.setProductsNotAddedToCart(energizerCartService.getProductNotAddedToCart());
				cartData.setProductsNotDoubleStacked(energizerCartService.getProductsNotDoublestacked());

				energizerB2BCheckoutFlowFacade.setContainerAttributes(cartData);

				model.addAttribute("containerHeightList", containerHeightList);
				model.addAttribute("packingOptionList", packingOptionsList);
				model.addAttribute("errorMessages", errorMessages);


				//model.addAttribute("productList", productList);
				//model.addAttribute("productsNotDoubleStacked", productsNotDoubleStacked);
				model.addAttribute("containerUtilizationForm", contUtilForm);
				model.addAttribute("enableButton", enableButton);
				model.addAttribute("freightType", b2bUnit.getFreightType());
				model.addAttribute("palletType", b2bUnit.getPalletType());
				//			WeSell Implementation -  Added Code Changes for Container Optimization for Sales Rep login - by Venkat
			}
			//			WeSell Implementation -  Added Code Changes for Container Optimization for Sales Rep login - by Venkat
		}

		// Add code for Truck which is for EMEA
		if (null != b2bUnit.getFreightType() && !StringUtils.isEmpty(b2bUnit.getFreightType())
				&& getSiteUid().equalsIgnoreCase(PERSONALCARE_EMEA) && (FREIGHT_TRUCK.equalsIgnoreCase(b2bUnit.getFreightType())
						|| FREIGHT_CONTAINER.equalsIgnoreCase(b2bUnit.getFreightType())))
		{

			LOG.info("Started Truck/Container Optimization logic for EMEA !!!");
			LOG.info("FreightType ::: " + b2bUnit.getFreightType() + " , PalletType ::: " + b2bUnit.getPalletType()
					+ " , containerHeight ::: " + cartData.getContainerHeight());

			final List<String> packingOptionsList = new ArrayList<String>();

			enableButton = b2bUnit.getEnableContainerOptimization() == null ? false : b2bUnit.getEnableContainerOptimization();

			LOG.info(" EnableButton: " + enableButton);

			String containerHeight = getConfigValue("container.default.height");

			// Setting up container height
			if (FREIGHT_CONTAINER.equalsIgnoreCase(b2bUnit.getFreightType()))
			{
				if (contUtilForm.getContainerHeight() != null)
				{
					containerHeight = contUtilForm.getContainerHeight();
				}
				cartData.setContainerHeight(containerHeight);
				LOG.info("Container Height set for Container as ::: " + cartData.getContainerHeight());
			}
			else
			{
				LOG.info("DO NOT set container height for Truck !");
			}

			cartData = energizerCartService.calCartTruckUtilization(cartData, b2bUnit.getFreightType(), "1 WOODEN BASE",
					enableButton, b2bUnit.getPalletType());

			packingOptionsList.add("Wooden Base");
			model.addAttribute("packingOptionList", packingOptionsList);

			if (cartData.getTotalPalletCount() == null && cartData.getPartialPalletCount() == null)
			{
				model.addAttribute("FullPallet", null);
				model.addAttribute("MixedPallet", null);
			}
			else
			{
				model.addAttribute("FullPallet", cartData.getTotalPalletCount());
				model.addAttribute("MixedPallet", cartData.getPartialPalletCount());
			}

			if (cartData.isIsContainerFull())
			{
				businessRuleErrors.add(Localization.getLocalizedString(ORDER_EXCEEDED_EMEA));
			}
			if (cartData.isIsOrderBlocked())
			{
				businessRuleErrors.add(Localization.getLocalizedString(ORDER_BLOCKED_EMEA));
			}

			final List<String> containerHeightList = Arrays
					.asList(getConfigValue("possibleContainerHeights").split(new Character(',').toString()));

			contUtilForm.setContainerHeight(containerHeight);
			packingOptionsList.add("Wooden Base");

			energizerB2BCheckoutFlowFacade.setContainerAttributes(cartData);

			model.addAttribute("packingOptionList", packingOptionsList);
			model.addAttribute("containerHeightList", containerHeightList);
			model.addAttribute("containerUtilizationForm", contUtilForm);
			model.addAttribute("enableButton", enableButton);
			model.addAttribute("freightType", b2bUnit.getFreightType());
			model.addAttribute("palletType", b2bUnit.getPalletType());
		}

		// Set cart placed by Sales Rep
		if (isSalesRepUserLogin())
		{
			cartData.setPlacedBySalesRep(true);
		}
		else
		{
			cartData.setPlacedBySalesRep(false);
		}

		model.addAttribute("cartData", cartData);
		model.addAttribute("currencySymbol", this.getStoreSessionFacade().getCurrentCurrency().getSymbol());
		storeCmsPageInModel(model, getContentPageForLabelOrId(CART_CMS_PAGE));
		setUpMetaDataForContentPage(model, getContentPageForLabelOrId(CART_CMS_PAGE));

	}

	protected void reverseCartProductsOrder(final List<OrderEntryData> entries)
	{
		if (entries != null)
		{
			Collections.reverse(entries);
		}
	}

	@RequestMapping(value = EXCEL_ORDER_AJAX_CALL, method = RequestMethod.GET)
	@RequireHardLogIn
	public @ResponseBody Map<String, List<EnergizerFileUploadData>> excelUploadQuantityUpdate(final Model model,
			@RequestParam("quantity") final Long quantity, @RequestParam("erpMaterialCode") final String erpMaterialCode)
			throws CMSItemNotFoundException
	{
		List<EnergizerFileUploadData> excelDataList = null;

		for (final Entry<String, List<EnergizerFileUploadData>> entry : shipmentMap.entrySet())
		{
			excelDataList = new ArrayList<EnergizerFileUploadData>();
			excelDataList = entry.getValue();
			for (final EnergizerFileUploadData excelDataRow : excelDataList)
			{
				if (excelDataRow.getMaterialId().equals(erpMaterialCode))
				{
					excelDataRow.setQuantity(quantity);
					break;
				}
			}
		}
		return shipmentMap;
	}

	private String validateAndGetString(final Cell cell)
	{
		final DataFormatter formatter = new DataFormatter(Locale.US);
		if (cell != null && !(StringUtils.isBlank(formatter.formatCellValue(cell))))
		{
			final org.apache.poi.ss.util.CellReference ref = new org.apache.poi.ss.util.CellReference(cell);

			LOG.info("The value of " + ref.formatAsString() + " is " + formatter.formatCellValue(cell));

			return cell == null ? null
					: StringUtils.isBlank(formatter.formatCellValue(cell)) ? null : formatter.formatCellValue(cell);
		}

		return null;

	}


	/*
	 * Redirect to /my-cart/cart page ,once action is performed on /my-cart/addtoCart Page...
	 */

	@RequestMapping(value = CART, method = RequestMethod.POST)
	@RequireHardLogIn
	public String updateContainerUtil(@Valid final ContainerUtilizationForm containerUtilizationForm, final Model model,
			final BindingResult bindingErrors, final RedirectAttributes redirectAttributes, final HttpServletRequest request,
			final HttpSession session) throws CMSItemNotFoundException
	{
		final String userId = userService.getCurrentUser().getUid();
		final EnergizerB2BUnitModel b2bUnit = b2bCommerceUserService.getParentUnitForCustomer(userId);
		boolean enableForB2BUnit = b2bUnit.getEnableContainerOptimization() == null ? false
				: b2bUnit.getEnableContainerOptimization();
		final String str = request.getParameter("choice");
		if (str != null && str.equals("Yes"))
		{
			LOG.info("Enable radio button :");
			enableButton = true;
		}

		if (str != null && str.equals("No"))
		{
			LOG.info("radio button value:");
			enableButton = false;
		}

		contUtilForm.setContainerHeight(containerUtilizationForm.getContainerHeight());
		contUtilForm.setPackingType(containerUtilizationForm.getPackingType());

		/*
		 * if (b2bUnit.getEnableContainerOptimization() == false) { enableButton = b2bUnit.getEnableContainerOptimization(); }
		 */
		enableButton = b2bUnit.getEnableContainerOptimization() == null ? false : b2bUnit.getEnableContainerOptimization();



		final String PERSONALCARE = getConfigValue("site.personalCare");
		final String PERSONALCARE_EMEA = getConfigValue("site.personalCareEMEA");

		if (null != b2bUnit.getFreightType() && !StringUtils.isEmpty(b2bUnit.getFreightType())
				&& getSiteUid().equalsIgnoreCase(PERSONALCARE_EMEA) && (FREIGHT_TRUCK.equalsIgnoreCase(b2bUnit.getFreightType())
						|| FREIGHT_CONTAINER.equalsIgnoreCase(b2bUnit.getFreightType())))
		{
			if (REGION_EMEA.equals(b2bUnit.getRegion()))
			{
				model.addAttribute("isEmeaUser", true);
			}

			final boolean enableTruck = b2bUnit.getEnableContainerOptimization() == null ? false
					: b2bUnit.getEnableContainerOptimization();

			prepareDataForPage(model);

			model.addAttribute("enableTruck", enableTruck);
			model.addAttribute("enableForB2BUnit", enableForB2BUnit);
		}
		else if ((null == b2bUnit.getFreightType() || StringUtils.isEmpty(b2bUnit.getFreightType())
				|| FREIGHT_CONTAINER.equalsIgnoreCase(b2bUnit.getFreightType()))
				&& this.getCmsSiteService().getCurrentSite().getUid().equalsIgnoreCase(PERSONALCARE))
		{
			enableButton = b2bUnit.getEnableContainerOptimization() == null ? false : b2bUnit.getEnableContainerOptimization();

			final CartData cartData = cartFacade.getSessionCart();
			String ShippingPointNo = null;
			//	reverseCartProductsOrder(cartData.getEntries());
			if (cartData.getEntries() != null && !cartData.getEntries().isEmpty())
			{
				for (final OrderEntryData entry : cartData.getEntries())
				{
					ShippingPointNo = entry.getProduct().getShippingPoint();
					if (ShippingPointNo != null)
					{
						break;
					}
				}
			}

			if (ShippingPointNo != null && ShippingPointNo.equals("867"))
			{
				enableButton = false;
				enableForB2BUnit = false;
			}
			prepareDataForPage(model);
			model.addAttribute("enableButton", enableButton);
			model.addAttribute("enableForB2BUnit", enableForB2BUnit);
			session.setAttribute("enableButton", enableButton);
		}
		cartEntryBusinessRulesService.clearErrors();
		//contUtilForm.setContainerHeight(containerUtilizationForm.getContainerHeight());
		//contUtilForm.setPackingType(containerUtilizationForm.getPackingType());

		return Views.Pages.Cart.CartPage;
	}

	protected OrderEntryData getOrderEntryDataForExpectedUnitPrice(final String expectedUnitPrice, final String productCode,
			final Integer entryNumber) throws Exception
	{

		final OrderEntryData orderEntry = new OrderEntryData();
		try
		{
			orderEntry.setExpectedUnitPrice(expectedUnitPrice);
			orderEntry.setProduct(new ProductData());
			orderEntry.getProduct().setCode(productCode);
			orderEntry.setEntryNumber(entryNumber);

			final String userId = userService.getCurrentUser().getUid();
			final EnergizerB2BUnitModel b2bUnit = b2bCommerceUserService.getParentUnitForCustomer(userId);
			final EnergizerCMIRModel energizerCMIR = energizerProductService.getEnergizerCMIR(productCode, b2bUnit.getUid());
			final EnergizerProductModel energizerProductModel = energizerProductService.getProductWithCode(productCode);
			orderEntry.getProduct().setUom(energizerCMIR.getUom());

			// Setting each unit price to the order entry
			orderEntry.setEachUnitPrice(getEachUnitPrice(energizerProductModel, b2bUnit));
		}
		catch (final Exception e)
		{
			LOG.info("Exception occured in Excel Upload Page controller:::" + e);
			throw e;
		}
		return orderEntry;

	}

	public PriceData getEachUnitPrice(final EnergizerProductModel energizerProductModel,
			final EnergizerB2BUnitModel loggedInUserB2bUnit)
	{
		final PriceData eachUnitPrice = new PriceData();

		final Collection<PriceRowModel> rowPrices = energizerProductModel.getEurope1Prices();
		boolean foundCmirPrice = false;
		for (final Iterator iterator = rowPrices.iterator(); iterator.hasNext();)
		{
			final PriceRowModel priceRowModel = (PriceRowModel) iterator.next();
			if (priceRowModel instanceof EnergizerPriceRowModel)
			{
				final EnergizerPriceRowModel energizerPriceRowModel = (EnergizerPriceRowModel) priceRowModel;
				if (null != energizerPriceRowModel.getB2bUnit() && null != loggedInUserB2bUnit
						&& energizerPriceRowModel.getB2bUnit().getUid().equalsIgnoreCase(loggedInUserB2bUnit.getUid()))
				{
					if (energizerPriceRowModel.getPrice() == null || energizerPriceRowModel.getPrice().doubleValue() == 0.0)
					{
						foundCmirPrice = false;
					}
					else
					{
						foundCmirPrice = true;
						LOG.info("Ifffffffff");

						if (energizerPriceRowModel.getIsActive())
						{
							LOG.info("eachUnitPrice for " + energizerProductModel.getCode() + " is === "
									+ BigDecimal.valueOf(energizerPriceRowModel.getPrice()));
							eachUnitPrice.setValue(energizerPriceRowModel.getPrice() == null ? BigDecimal.valueOf(ZERO)
									: BigDecimal.valueOf(energizerPriceRowModel.getPrice()).setScale(2, RoundingMode.CEILING));
							eachUnitPrice.setCurrencyIso(energizerPriceRowModel.getCurrency().getIsocode() == null ? EMPTY
									: energizerPriceRowModel.getCurrency().getIsocode());
							eachUnitPrice.setFormattedValue(
									storeSessionFacade.getCurrentCurrency().getSymbol().concat(eachUnitPrice.getValue().toString()));

							break;
						}
						else
						{
							/*
							 * LOG.info("energizerPriceRowModel is inactive for this product ::: " +
							 * energizerPriceRowModel.getProduct().getCode() + " with price ::: " + energizerPriceRowModel.getPrice());
							 */
						}
					}

				}
			}
		}
		if (!foundCmirPrice)
		{
			for (final Iterator iterator = rowPrices.iterator(); iterator.hasNext();)
			{
				final PriceRowModel priceRowModel = (PriceRowModel) iterator.next();
				if (priceRowModel instanceof EnergizerPriceRowModel)
				{
					continue;
				}
				LOG.info("Elseeeeeeeeeee");
				LOG.info("eachUnitPrice for " + energizerProductModel.getCode() + " is === "
						+ BigDecimal.valueOf(priceRowModel.getPrice()));
				eachUnitPrice.setValue(priceRowModel.getPrice() == null ? BigDecimal.valueOf(ZERO)
						: BigDecimal.valueOf(priceRowModel.getPrice()).setScale(2, RoundingMode.CEILING));
				eachUnitPrice.setCurrencyIso(
						priceRowModel.getCurrency().getSymbol() == null ? EMPTY : priceRowModel.getCurrency().getIsocode());
				eachUnitPrice.setFormattedValue(
						storeSessionFacade.getCurrentCurrency().getSymbol().concat(eachUnitPrice.getValue().toString()));
			}
		}

		return eachUnitPrice;
	}

	protected OrderEntryData getOrderEntryDataForEachUnitPrice(final long quantity, final String productCode,
			final Integer entryNumber) throws Exception
	{
		final OrderEntryData orderEntry = new OrderEntryData();
		try
		{
			orderEntry.setQuantity(quantity);
			orderEntry.setProduct(new ProductData());
			orderEntry.getProduct().setCode(productCode);
			orderEntry.setEntryNumber(entryNumber);

			final String userId = userService.getCurrentUser().getUid();
			final EnergizerB2BUnitModel b2bUnit = b2bCommerceUserService.getParentUnitForCustomer(userId);
			final EnergizerProductModel energizerProductModel = energizerProductService
					.getProductWithCode(orderEntry.getProduct().getCode());

			// Setting each unit price to the order entry
			orderEntry.setEachUnitPrice(energizerB2BCartFacade.getEachUnitPrice(energizerProductModel, b2bUnit));
		}
		catch (final Exception e)
		{
			LOG.info("Exception Occured while getting Order Entry Data:::" + e);
			throw e;
		}
		return orderEntry;

	}

}
