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
package com.energizer.storefront.controllers.misc;

import de.hybris.platform.b2b.company.B2BCommerceUserService;
import de.hybris.platform.b2bacceleratorfacades.api.cart.CartFacade;
import de.hybris.platform.b2bacceleratorfacades.product.data.CartEntryData;
import de.hybris.platform.cms2.servicelayer.services.CMSSiteService;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.order.data.CartModificationData;
import de.hybris.platform.commercefacades.order.data.OrderEntryData;
import de.hybris.platform.commercefacades.product.data.ProductData;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.order.CalculationService;
import de.hybris.platform.order.CartService;
import de.hybris.platform.order.exceptions.CalculationException;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.util.Config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.energizer.business.BusinessRuleError;
import com.energizer.core.business.service.EnergizerOrderEntryBusinessRuleValidationService;
import com.energizer.core.model.EnergizerB2BUnitModel;
import com.energizer.core.model.EnergizerCMIRModel;
import com.energizer.core.model.EnergizerProductModel;
import com.energizer.facades.flow.impl.DefaultEnergizerB2BCartFacade;
import com.energizer.services.order.EnergizerCartService;
import com.energizer.services.product.EnergizerProductService;
import com.energizer.storefront.controllers.AbstractController;
import com.energizer.storefront.controllers.ControllerConstants;
import com.energizer.storefront.controllers.util.GlobalMessages;
import com.energizer.storefront.forms.AddToCartForm;
import com.energizer.storefront.forms.AddToCartOrderForm;
import com.google.common.collect.Lists;


/**
 * Controller for Add to Cart functionality which is not specific to a certain page.
 */
@Controller
@Scope("tenant")
public class AddToCartController extends AbstractController
{
	private static final String TYPE_MISMATCH_ERROR_CODE = "typeMismatch";
	private static final String ERROR_MSG_TYPE = "errorMsg";
	private static final String QUANTITY_INVALID_BINDING_MESSAGE_KEY = "basket.error.quantity.invalid.binding";

	protected static final Logger LOG = Logger.getLogger(AddToCartController.class);
	private static final Long MINIMUM_SINGLE_SKU_ADD_CART = 0L;
	private static final String SHOWN_PRODUCT_COUNT = "storefront.minicart.shownProductCount";
	public static final String SUCCESSFUL_MODIFICATION_CODE = "success";

	private static final String FREIGHT_TRUCK = "Truck";
	private static final String FREIGHT_CONTAINER = "Container";

	@Resource
	CalculationService calculationService;

	@Resource(name = "b2bCartFacade")
	private CartFacade cartFacade;

	@Deprecated
	@Resource(name = "cartFacade")
	private de.hybris.platform.commercefacades.order.CartFacade cartCommerceFacade;

	@Resource
	private CartService cartService;

	@Resource(name = "energizerProductService")
	private EnergizerProductService energizerProductService;

	@Resource
	private UserService userService;

	@Resource
	private B2BCommerceUserService b2bCommerceUserService;

	@Resource
	EnergizerCartService energizerCartService;

	@Resource
	private EnergizerOrderEntryBusinessRuleValidationService orderEntryBusinessRulesService;

	@Resource(name = "configurationService")
	private ConfigurationService configurationService;

	@Resource(name = "cmsSiteService")
	private CMSSiteService cmsSiteService;

	@Resource
	DefaultEnergizerB2BCartFacade energizerB2BCartFacade;

	@Resource
	private ModelService modelService;

	@Resource
	private SessionService sessionService;

	/**
	 * @return the sessionService
	 */
	public SessionService getSessionService()
	{
		return sessionService;
	}

	/**
	 * @param sessionService
	 *                          the sessionService to set
	 */
	public void setSessionService(final SessionService sessionService)
	{
		this.sessionService = sessionService;
	}

	@InitBinder
	public void initBinder(final WebDataBinder binder)
	{
		binder.setAutoGrowCollectionLimit(Integer.MAX_VALUE);
	}

	@RequestMapping(value = "/cart/add", method = RequestMethod.POST, produces = "application/json")
	public String addToCart(@RequestParam("productCodePost") final String code, final Model model, @Valid final AddToCartForm form,
			final BindingResult bindingErrors, final HttpSession session) throws Exception
	{
		try
		{

			final String PERSONALCARE = getConfigValue("site.personalCare");
			final String PERSONALCARE_EMEA = getConfigValue("site.personalCareEMEA");

			final String userId = userService.getCurrentUser().getUid();
			final EnergizerB2BUnitModel b2bUnit = b2bCommerceUserService.getParentUnitForCustomer(userId);
			if (cmsSiteService.getCurrentSite().getUid().equalsIgnoreCase(PERSONALCARE_EMEA))
			{
				// Setting up currency preference to the cart model ONLY for EMEA - Added for multiple currencies implementation
				energizerCartService.updateCartCurrency();
			}

			final EnergizerCMIRModel energizerCMIR = energizerProductService.getEnergizerCMIR(code, b2bUnit.getUid());
			OrderEntryData orderEntryData = null;
			if (cmsSiteService.getCurrentSite().getUid().equalsIgnoreCase(PERSONALCARE))
			{
				energizerCMIR.setUom(form.getUOM());
				modelService.save(energizerCMIR);
			}
			orderEntryData = getOrderEntryData(form.getQty(), code, null);
			getOrderEntryShippingPoints(orderEntryData);
			orderEntryBusinessRulesService.clearErrors();

			orderEntryBusinessRulesService.validateBusinessRules(orderEntryData);
			if (orderEntryBusinessRulesService.hasErrors())
			{
				final List<BusinessRuleError> errors = orderEntryBusinessRulesService.getErrors();
				if (null != errors && !errors.isEmpty())
				{
					for (final BusinessRuleError error : errors)
					{
						LOG.info("The error message is " + error.getMessage());
						GlobalMessages.addBusinessRuleMessage(model, error.getMessage());
					}
				}
				return ControllerConstants.Views.Fragments.Product.ProductLister;
			}

			if (bindingErrors.hasErrors())
			{
				return getViewWithBindingErrorMessages(model, bindingErrors);
			}


			final CartModificationData modification = cartFacade.addOrderEntry(orderEntryData);

			orderEntryData = energizerB2BCartFacade.getOrderEntryDataForEachUnitPrice(orderEntryData);
			// Setting entry number to the order entry data from recently added cart entry model
			orderEntryData.setEntryNumber(modification.getEntry().getEntryNumber());

			// Setting each unit price/division to the cart/order entry model
			energizerB2BCartFacade.updateOrderEntryForEachUnitPrice(orderEntryData);

			CartData cartModificationData = null;
			String height = (String) session.getAttribute("containerHeight");
			final Boolean enableButton = b2bUnit.getEnableContainerOptimization() == null ? false
					: b2bUnit.getEnableContainerOptimization();


			LOG.info("Freight Type: " + b2bUnit.getFreightType());
			LOG.info("Pallet Type: " + b2bUnit.getPalletType());


			if (((null == b2bUnit.getFreightType() || StringUtils.isEmpty(b2bUnit.getFreightType())
					|| FREIGHT_CONTAINER.equalsIgnoreCase(b2bUnit.getFreightType()))
					&& cmsSiteService.getCurrentSite().getUid().equalsIgnoreCase(PERSONALCARE)))
			{
				//	WeSell Implementation -  Added Code Changes for Container Optimization for Sales Rep login - by Venkat
				if (!(boolean) getSessionService().getAttribute("isSalesRepUserLoggedIn"))
				{

					if (height == null)
					{
						height = "40FT";
					}
					if (height != null && height.contains("40"))
					{
						LOG.info("for 40 ft");
						cartModificationData = energizerCartService.calCartContainerUtilization(cartCommerceFacade.getSessionCart(),
								"40FT", "1 SLIP SHEET AND 1 WOODEN BASE", enableButton);

					}
					else if (height != null && height.contains("20"))
					{
						LOG.info("for 20 ft");
						cartModificationData = energizerCartService.calCartContainerUtilization(cartCommerceFacade.getSessionCart(),
								"20FT", "1 SLIP SHEET AND 1 WOODEN BASE", enableButton);

					}
					if (cartModificationData == null)
					{
						model.addAttribute("FullPallet", null);
						model.addAttribute("MixedPallet", null);
					}

					else
					{
						if (cartModificationData != null && cartModificationData.getTotalPalletCount() == null)
						{
							model.addAttribute("FullPallet", null);
						}
						else
						{
							model.addAttribute("FullPallet", cartModificationData.getTotalPalletCount());
						}
						if (cartModificationData != null && cartModificationData.getPartialPalletCount() == null)
						{
							model.addAttribute("MixedPallet", null);
						}
						else
						{
							model.addAttribute("MixedPallet", cartModificationData.getPartialPalletCount());
						}
					}
					//	WeSell Implementation -  Added Code Changes for Container Optimization for Sales Rep login - by Venkat
				}

			}

			if (null != b2bUnit.getFreightType() && !StringUtils.isEmpty(b2bUnit.getFreightType())
					&& cmsSiteService.getCurrentSite().getUid().equalsIgnoreCase(PERSONALCARE_EMEA)
					&& (FREIGHT_TRUCK.equalsIgnoreCase(b2bUnit.getFreightType())
							|| FREIGHT_CONTAINER.equalsIgnoreCase(b2bUnit.getFreightType())))
			{

				cartModificationData = energizerCartService.calCartTruckUtilization(cartCommerceFacade.getSessionCart(),
						b2bUnit.getFreightType(), "1 WOODEN BASE", enableButton, b2bUnit.getPalletType());

				final List<String> packingOptionsList = new ArrayList<String>();

				packingOptionsList.add("Wooden Base");
				model.addAttribute("packingOptionList", packingOptionsList);
				if (cartModificationData == null)
				{
					model.addAttribute("FullPallet", null);
					model.addAttribute("MixedPallet", null);
				}

				else
				{
					if (cartModificationData != null && cartModificationData.getTotalPalletCount() == null)
					{
						model.addAttribute("FullPallet", null);
					}
					else
					{
						model.addAttribute("FullPallet", cartModificationData.getTotalPalletCount());
					}
					if (cartModificationData != null && cartModificationData.getPartialPalletCount() == null)
					{
						model.addAttribute("MixedPallet", null);
					}
					else
					{
						model.addAttribute("MixedPallet", cartModificationData.getPartialPalletCount());
					}
				}

			}

			model.addAttribute("numberShowing", Config.getInt(SHOWN_PRODUCT_COUNT, 3));
			model.addAttribute("modifications", (modification != null ? Lists.newArrayList(modification) : Collections.emptyList()));

			addStatusMessages(model, modification);
			//Added Model Attribute for WeSell Implementation--START
			model.addAttribute("isSalesRepUserLogin", (boolean) getSessionService().getAttribute("isSalesRepUserLoggedIn"));
			if ((boolean) sessionService.getAttribute("isSalesRepUserLoggedIn"))
			{
				session.setAttribute("gotPriceFromSAP", false);
			}
			//Added Model Attribute for WeSell Implementation--END
		}
		catch (final Exception e)
		{
			LOG.error("Exception occured while add product to the cart::: ");
			e.printStackTrace();
			return null;
		}

		return ControllerConstants.Views.Fragments.Cart.AddToCartPopup;
	}

	protected void addStatusMessages(final Model model, final CartModificationData modification)
	{
		try
		{
			final boolean hasMessage = StringUtils.isNotEmpty(modification.getStatusMessage());
			if (hasMessage)
			{
				if (SUCCESSFUL_MODIFICATION_CODE.equals(modification.getStatusCode()))
				{
					GlobalMessages.addMessage(model, GlobalMessages.CONF_MESSAGES_HOLDER, modification.getStatusMessage(), null);
				}
				else if (!model.containsAttribute(ERROR_MSG_TYPE))
				{
					GlobalMessages.addMessage(model, GlobalMessages.ERROR_MESSAGES_HOLDER, modification.getStatusMessage(), null);
				}
			}
		}
		catch (final Exception e)
		{
			// YTODO: handle exception
			LOG.error("Exception occured while cart update ::::");
			e.printStackTrace();
			throw e;
		}
	}


	@RequestMapping(value = "/cart/addGrid", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public final String addGridToCart(@RequestBody final AddToCartOrderForm form, final Model model) throws Exception
	{
		try
		{
			final List<OrderEntryData> orderEntries = getOrderEntryData(form.getCartEntries());
			final List<CartModificationData> modifications = cartFacade.addOrderEntryList(orderEntries);

			model.addAttribute("modifications", modifications);
			model.addAttribute("numberShowing", Config.getInt(SHOWN_PRODUCT_COUNT, 3));

			for (final CartModificationData modification : modifications)
			{
				addStatusMessages(model, modification);
			}
		}
		catch (final Exception e)
		{
			LOG.info(e.getMessage());
			throw e;

		}
		return ControllerConstants.Views.Fragments.Cart.AddToCartPopup;
	}

	protected String getViewWithBindingErrorMessages(final Model model, final BindingResult bindingErrors)
	{
		for (final ObjectError error : bindingErrors.getAllErrors())
		{
			if (error.getCode().equals(TYPE_MISMATCH_ERROR_CODE))
			{
				model.addAttribute(ERROR_MSG_TYPE, QUANTITY_INVALID_BINDING_MESSAGE_KEY);
			}
			else
			{
				model.addAttribute(ERROR_MSG_TYPE, error.getDefaultMessage());
			}
		}
		return ControllerConstants.Views.Fragments.Cart.AddToCartPopup;
	}

	protected OrderEntryData getOrderEntryData(final long quantity, final String productCode, final Integer entryNumber)
			throws Exception
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
			final EnergizerCMIRModel energizerCMIR = energizerProductService.getEnergizerCMIR(productCode, b2bUnit.getUid());
			final EnergizerProductModel productModel = energizerProductService.getProductWithCode(productCode);

			orderEntry.getProduct().setUom(energizerCMIR.getUom());
			LOG.info("ProductModel isPBG"+ productModel.getIsPBG());
			LOG.info("EnergizerCmir IsWesellProduct"+ energizerCMIR.getIsWeSellProduct());

			orderEntry.getProduct().setIsPBG(null != productModel.getIsPBG() ? productModel.getIsPBG() : false);
			orderEntry.getProduct()
					.setIsWeSellProduct(null != energizerCMIR.getIsWeSellProduct() ? energizerCMIR.getIsWeSellProduct() : false);

			LOG.info("OrderEntry isPBG"+ orderEntry.getProduct().isIsPBG());
			LOG.info("OrderEntry IsWesellProduct "+ orderEntry.getProduct().isIsWeSellProduct());

		}
		catch (final Exception e)
		{
			LOG.info("Exception occured in getting Order Entry Data:::" + e);
			throw e;
		}
		return orderEntry;
	}

	protected List<OrderEntryData> getOrderEntryData(final List<CartEntryData> cartEntries) throws Exception
	{
		final List<OrderEntryData> orderEntries = new ArrayList<>();
		try
		{
			for (final CartEntryData entry : cartEntries)
			{
				final Integer entryNumber = entry.getEntryNumber() != null ? entry.getEntryNumber().intValue() : null;
				orderEntries.add(getOrderEntryData(entry.getQuantity(), entry.getSku(), entryNumber));
			}
		}
		catch (final Exception e)
		{
			LOG.info("Exception Occured in getting Order Entry Data:::" + e);
			throw e;
		}
		return orderEntries;
	}


	/**
	 *
	 * @param orderEntryData
	 */
	protected void getOrderEntryShippingPoints(final OrderEntryData orderEntryData) throws Exception
	{
		try
		{
			final String productCode = orderEntryData.getProduct().getCode();
			final String userId = userService.getCurrentUser().getUid();
			final EnergizerB2BUnitModel b2bUnit = b2bCommerceUserService.getParentUnitForCustomer(userId);
			EnergizerCMIRModel energizerCMIR = energizerProductService.getEnergizerCMIR(productCode, b2bUnit.getUid());
			final String shippingPointNo = energizerCMIR.getShippingPoint();

			orderEntryData.setShippingPoint(shippingPointNo);
			orderEntryData
					.setShippingPointLocation(energizerProductService.getShippingPointLocation(energizerCMIR.getShippingPoint()));

			/*
			 * if (cartService.hasSessionCart()) {
			 */
			final CartModel cartModel = cartService.getSessionCart();
			if (cartModel.getEntries() != null && !cartModel.getEntries().isEmpty())
			{
				final AbstractOrderEntryModel orderEntry = cartModel.getEntries().get(0);

				final String cartProductCode = orderEntry.getProduct().getCode();

				final EnergizerCMIRModel cartenergizerCMIR = energizerProductService.getEnergizerCMIR(cartProductCode,
						b2bUnit.getUid());

				final String cartshippingPoint = cartenergizerCMIR.getShippingPoint();
				LOG.info("The shippingPointNo of product in the cart : " + cartshippingPoint);
				orderEntryData.setReferenceShippingPoint(cartshippingPoint);

				final String cartShippingPointLocation = energizerProductService.getShippingPointLocation(cartshippingPoint);
				LOG.info("The shippingPointLocation of product in the cart : " + cartShippingPointLocation);
				orderEntryData.setReferenceShippingPointLocation(cartShippingPointLocation);
			}
			else
			{
				energizerCMIR = energizerProductService.getEnergizerCMIR(orderEntryData.getProduct().getCode(), b2bUnit.getUid());
				final String shippingPoint = energizerCMIR.getShippingPoint();
				orderEntryData.setShippingPoint(shippingPoint);
				orderEntryData.setReferenceShippingPoint(shippingPoint);

				final String shippingPointLocation = energizerProductService.getShippingPointLocation(shippingPoint);
				orderEntryData.setShippingPointLocation(shippingPointLocation);
				orderEntryData.setReferenceShippingPointLocation(shippingPointLocation);
			}
			//}
		}
		catch (final Exception e)
		{
			LOG.info("Exception Occured in getting Order Entry Shipping Point:::" + e);
			throw e;
		}
	}

	public String getConfigValue(final String key)
	{
		return configurationService.getConfiguration().getString(key);
	}

	@RequestMapping(value = "/validateUOM", method = RequestMethod.POST)
	@ResponseBody
	public String validateUOM(@RequestParam("productCode") final String code, @RequestParam("uom") final String uom)
	{

		final List<OrderEntryData> cartItems = new ArrayList<OrderEntryData>();
		String str = "Success";
		final CartData cartData = cartCommerceFacade.getSessionCart();
		if (null == cartData || null == cartData.getEntries())
		{
			return str;
		}
		final List<OrderEntryData> cartItemsList = cartData.getEntries();

		for (final OrderEntryData item : cartItemsList)
		{
			final ProductData product = item.getProduct();
			if (code.equalsIgnoreCase(product.getCode()) && !uom.equalsIgnoreCase(product.getUom()))
			{
				str = "Error";
				break;
			}
			//return REDIRECT_PREFIX + "/cart";
		}
		return str;
	}

	/*
	 * @RequestMapping(value = "/validateUOM", method = RequestMethod.POST)
	 *
	 * @ResponseBody public String validateUOMPost(@RequestParam("productCode") final String code, @RequestParam("uom")
	 * final String uom) { return validateUOM(code, uom); }
	 */

	@RequestMapping(value = "/updateUOM", method = RequestMethod.GET)
	public String updateUOM(@RequestParam("productCode") final String code, @RequestParam("uom") final String uom,
			@RequestParam("uomQty") final String uomQty) throws Exception
	{
		final String PERSONALCARE = getConfigValue("site.personalCare");
		final String userId = userService.getCurrentUser().getUid();
		final EnergizerB2BUnitModel b2bUnit = b2bCommerceUserService.getParentUnitForCustomer(userId);
		final EnergizerCMIRModel energizerCMIR = energizerProductService.getEnergizerCMIR(code, b2bUnit.getUid());
		if (cmsSiteService.getCurrentSite().getUid().equalsIgnoreCase(PERSONALCARE))
		{
			energizerCMIR.setUom(uom);
			modelService.save(energizerCMIR);
		}
		try
		{
			calculationService.recalculate(cartService.getSessionCart());
		}
		catch (final CalculationException ex)
		{
			LOG.info("Cart calculation error during UOM :::" + uom + " change for product ::: " + code + "  Error :::"
					+ ex.getMessage());
		}
		return REDIRECT_PREFIX + "/cart";
	}

	@RequestMapping(value = "/updateUOM", method = RequestMethod.POST)
	public String updateUOM_POST(@RequestParam("productCode") final String code, @RequestParam("uom") final String uom,
			@RequestParam("uomQty") final String uomQty) throws Exception
	{
		return updateUOM(code, uom, uomQty);
	}

	@RequestMapping(value = "/validatePBG", method = RequestMethod.POST)
	@ResponseBody
	public String validatePBG(@RequestParam("productCode") final String productCode, @RequestParam("isPBG") final String isPBG)
	{
		String response = "Success";
		final boolean isSalesRepUserLogin = (boolean) getSessionService().getAttribute("isSalesRepUserLoggedIn");
		final CartData cartData = cartCommerceFacade.getSessionCart();
		if (null == cartData || null == cartData.getEntries())
		{
			return response;
		}
		if (!isSalesRepUserLogin)
		{
			final List<OrderEntryData> cartItemsList = cartData.getEntries();

			for (final OrderEntryData item : cartItemsList)
			{
				if (Boolean.valueOf(isPBG).booleanValue() != item.getProduct().isIsPBG())
				{
					response = "Error";
					break;
				}
			}
		}
		return response;
	}

}
