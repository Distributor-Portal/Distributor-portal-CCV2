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
package com.energizer.storefront.controllers.pages;

import de.hybris.platform.acceleratorservices.controllers.page.PageType;
import de.hybris.platform.acceleratorservices.customer.CustomerLocationService;
import de.hybris.platform.b2b.company.B2BCommerceUserService;
import de.hybris.platform.cms2.exceptions.CMSItemNotFoundException;
import de.hybris.platform.cms2.servicelayer.services.CMSPageService;
import de.hybris.platform.commercefacades.order.CartFacade;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.order.data.CartModificationData;
import de.hybris.platform.commercefacades.order.data.CartRestorationData;
import de.hybris.platform.commercefacades.order.data.OrderEntryData;
import de.hybris.platform.commercefacades.product.ProductFacade;
import de.hybris.platform.commercefacades.product.ProductOption;
import de.hybris.platform.commercefacades.product.data.ProductData;
import de.hybris.platform.commercefacades.user.UserFacade;
import de.hybris.platform.commerceservices.order.CommerceCartModificationException;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.order.CartService;
import de.hybris.platform.product.ProductService;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.exceptions.ModelNotFoundException;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.util.Config;
import de.hybris.platform.util.localization.Localization;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.annotation.Resource;
import javax.mail.internet.AddressException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import javax.xml.bind.JAXBException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.energizer.business.BusinessRuleError;
import com.energizer.core.business.service.EnergizerOrderBusinessRuleValidationService;
import com.energizer.core.business.service.EnergizerOrderEntryBusinessRuleValidationService;
import com.energizer.core.constants.EnergizerCoreConstants;
import com.energizer.core.model.EnergizerB2BUnitModel;
import com.energizer.core.model.EnergizerCMIRModel;
import com.energizer.core.model.EnergizerProductModel;
import com.energizer.facades.flow.impl.DefaultEnergizerB2BCartFacade;
import com.energizer.facades.flow.impl.DefaultEnergizerB2BCheckoutFlowFacade;
import com.energizer.facades.flow.impl.SessionOverrideB2BCheckoutFlowFacade;
import com.energizer.services.order.EnergizerCartService;
import com.energizer.services.product.EnergizerProductService;
import com.energizer.storefront.annotations.RequireHardLogIn;
import com.energizer.storefront.breadcrumb.ResourceBreadcrumbBuilder;
import com.energizer.storefront.breadcrumb.impl.SearchBreadcrumbBuilder;
import com.energizer.storefront.constants.WebConstants;
import com.energizer.storefront.controllers.ControllerConstants;
import com.energizer.storefront.controllers.ControllerConstants.Views;
import com.energizer.storefront.controllers.util.GlobalMessages;
import com.energizer.storefront.forms.ContainerUtilizationForm;
import com.energizer.storefront.forms.UpdateExpectedUnitPriceForm;
import com.energizer.storefront.forms.UpdateProfileForm;
import com.energizer.storefront.forms.UpdateQuantityForm;
import com.energizer.storefront.variants.VariantSortStrategy;


/**
 * Controller for cart page
 */
@Controller
@Scope("tenant")
@RequestMapping(value = "/cart")
public class CartPageController extends AbstractPageController
{
	private static final String TYPE_MISMATCH_ERROR_CODE = "typeMismatch";
	private static final String ERROR_MSG_TYPE = "errorMsg";
	private static final String QUANTITY_INVALID_BINDING_MESSAGE_KEY = "basket.error.quantity.invalid.binding";
	private static final String ORDER_EXCEEDED = "container.business.rule.orderExceeded";
	private static final String ORDER_BLOCKED = "container.business.rule.orderblocked";

	//Added for EMEA Truck optimization enhancement - START
	private static final String ORDER_EXCEEDED_EMEA = "truck.business.rule.orderExceeded";
	private static final String ORDER_BLOCKED_EMEA = "truck.business.rule.orderblocked";
	//Added for EMEA Truck optimization enhancement - END

	protected static final Logger LOG = Logger.getLogger(CartPageController.class);

	private static final String CART_CMS_PAGE = "cartPage";
	private static final String REDIRECT_TO_CART_PAGE = REDIRECT_PREFIX + "/cart";

	private static final String CONTINUE_URL = "continueUrl";
	public static final String SUCCESSFUL_MODIFICATION_CODE = "success";
	private static final String REGION_EMEA = "EMEA";
	private static final String FREIGHT_TRUCK = "Truck";
	private static final String FREIGHT_CONTAINER = "Container";

	@Deprecated
	@Resource(name = "cartFacade")
	private CartFacade cartFacade;

	@Resource(name = "userFacade")
	protected UserFacade userFacade;

	@Resource(name = "b2bCartFacade")
	private de.hybris.platform.b2bacceleratorfacades.api.cart.CartFacade b2bCartFacade;

	@Resource(name = "sessionService")
	private SessionService sessionService;

	@Resource(name = "simpleBreadcrumbBuilder")
	private ResourceBreadcrumbBuilder resourceBreadcrumbBuilder;

	@Resource(name = "variantSortStrategy")
	private VariantSortStrategy variantSortStrategy;

	@Resource(name = "productService")
	private ProductService productService;

	@Resource(name = "b2bProductFacade")
	private ProductFacade productFacade;

	@Resource(name = "customerLocationService")
	private CustomerLocationService customerLocationService;

	@Resource
	DefaultEnergizerB2BCartFacade energizerB2BCartFacade;

	@Resource
	EnergizerCartService energizerCartService;

	@Resource
	private EnergizerOrderEntryBusinessRuleValidationService orderEntryBusinessRulesService;

	@Resource
	private EnergizerOrderEntryBusinessRuleValidationService cartEntryBusinessRulesService;

	@Resource
	private EnergizerOrderBusinessRuleValidationService orderBusinessRulesService;


	@Resource(name = "energizerProductService")
	private EnergizerProductService energizerProductService;

	@Resource
	private UserService userService;

	@Resource
	private B2BCommerceUserService b2bCommerceUserService;

	@Resource
	private ModelService modelService;
	//@Resource(name = "energizerCompanyB2BCommerceFacade")
	//protected EnergizerCompanyB2BCommerceFacade energizerCompanyB2BCommerceFacade;

	@Resource
	private CartService cartService;

	@Resource(name = "cmsPageService")
	private CMSPageService cmsPageService;

	ContainerUtilizationForm contUtilForm = new ContainerUtilizationForm();

	String containerHeight, packingOption;

	@Resource(name = "productConverter")
	private Converter<ProductModel, ProductData> productConverter;

	@Resource(name = "searchBreadcrumbBuilder")
	private SearchBreadcrumbBuilder searchBreadcrumbBuilder;

	boolean enableForB2BUnit = false;

	@Resource(name = "energizerB2BCheckoutFlowFacade")
	private DefaultEnergizerB2BCheckoutFlowFacade energizerB2BCheckoutFlowFacade;

	@Resource(name = "configurationService")
	private ConfigurationService configurationService;

	boolean enableButton = false;

	@RequestMapping(method = RequestMethod.GET)
	public String showCart(final Model model, final HttpSession session)
			throws CMSItemNotFoundException, NullPointerException, Exception
	{
		try
		{
			setUpCartPageBasicData(model);

			// Added this session attribute for WeSell Implementation - to disable the checkout button in cart thereby forcing the Sale sRep to get the realtime prices from SAPA
			if ((boolean) sessionService.getAttribute("isSalesRepUserLoggedIn"))
			{
				session.setAttribute("gotPriceFromSAP", false);
			}
			//Added Code changes for WeSell Implementation - END
		}
		catch (final NullPointerException ne)
		{
			LOG.error("NullPointerException occured ::: " + ne);
			return Views.Pages.Cart.CartPage;
		}
		catch (final Exception e)
		{
			LOG.error("Exception occured ::: " + e);
			return Views.Pages.Cart.CartPage;
		}

		return Views.Pages.Cart.CartPage;
	}

	@RequestMapping(method = RequestMethod.POST)
	@RequireHardLogIn
	public String updateContainerUtil(@Valid
	final ContainerUtilizationForm containerUtilizationForm, final Model model, final BindingResult bindingErrors,
			final RedirectAttributes redirectAttributes, final HttpServletRequest request, final HttpSession session)
			throws CMSItemNotFoundException
	{
		try
		{
			final String userId = userService.getCurrentUser().getUid();
			final EnergizerB2BUnitModel b2bUnit = b2bCommerceUserService.getParentUnitForCustomer(userId);
			final CartData cartData = cartFacade.getSessionCart();
			String ShippingPointNo = null;
			//reverseCartProductsOrder(cartData.getEntries());
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
			boolean enableForB2BUnit = b2bUnit.getEnableContainerOptimization() == null ? false
					: b2bUnit.getEnableContainerOptimization();


			if (bindingErrors.hasErrors())
			{
				getViewWithBindingErrorMessages(model, bindingErrors);
			}

			enableButton = b2bUnit.getEnableContainerOptimization() == null ? false : b2bUnit.getEnableContainerOptimization();

			if (ShippingPointNo != null && ShippingPointNo.equals("867"))
			{
				enableButton = false;
				enableForB2BUnit = false;
			}
			cartEntryBusinessRulesService.clearErrors();
			contUtilForm.setContainerHeight(containerUtilizationForm.getContainerHeight());
			contUtilForm.setPackingType(containerUtilizationForm.getPackingType());
			session.setAttribute("containerHeight", containerUtilizationForm.getContainerHeight());
			session.setAttribute("packingType", containerUtilizationForm.getPackingType());
			LOG.info("session: " + session.getAttribute("containerHeight"));
			session.setAttribute("enableButton", enableButton);
			prepareDataForPage(model);
			model.addAttribute("enableButton", enableButton);
			model.addAttribute("enableForB2BUnit", enableForB2BUnit);
		}
		catch (final NullPointerException ne)
		{
			LOG.error("NullPointerException occured ::: " + ne);
		}
		catch (final Exception e)
		{
			LOG.error("Exception occured ::: " + e);
		}

		return Views.Pages.Cart.CartPage;
	}


	@RequestMapping(value = "/clearCart", method = RequestMethod.GET)
	@RequireHardLogIn
	public String clearCart(final Model model, final RedirectAttributes redirectModel) throws CMSItemNotFoundException
	{

		final List<OrderEntryData> cartItems = new ArrayList<OrderEntryData>();

		final CartData cartData = cartFacade.getSessionCart();

		final List<OrderEntryData> cartItemsList = cartData.getEntries();

		cartData.setEntries(cartItems);
		cartFacade.removeSessionCart();

		model.addAttribute("cartData", cartData);
		storeCmsPageInModel(model, getContentPageForLabelOrId(CART_CMS_PAGE));
		setUpMetaDataForContentPage(model, getContentPageForLabelOrId(CART_CMS_PAGE));
		model.addAttribute(WebConstants.BREADCRUMBS_KEY, resourceBreadcrumbBuilder.getBreadcrumbs("breadcrumb.cart"));
		model.addAttribute("pageType", PageType.CART.name());


		//return REDIRECT_PREFIX + "/cart";
		return Views.Pages.Cart.CartPage;
	}


	@RequestMapping(value = "/checkout", method = RequestMethod.GET)
	@RequireHardLogIn
	public String cartCheck(final Model model, final RedirectAttributes redirectModel) throws Exception
	{
		SessionOverrideB2BCheckoutFlowFacade.resetSessionOverrides();
		if (!cartFacade.hasSessionCart() || cartFacade.getSessionCart().getEntries().isEmpty())
		{
			LOG.info("Missing or empty cart");

			// No session cart or empty session cart. Bounce back to the cart page.
			return REDIRECT_PREFIX + "/cart";
		}

		orderBusinessRulesService.validateBusinessRules(cartFacade.getSessionCart());
		if (orderBusinessRulesService.hasErrors())
		{
			final List<BusinessRuleError> errors = orderBusinessRulesService.getErrors();
			for (final BusinessRuleError error : errors)
			{
				LOG.info("The error message is " + error.getMessage());
				redirectModel.addFlashAttribute("businessErrors", error.getMessage());
			}
			return REDIRECT_PREFIX + "/cart";
			//return Views.Pages.Cart.CartPage;
		}

		if (validateCart(redirectModel))
		{
			return REDIRECT_PREFIX + "/cart";
		}

		final String userId = userService.getCurrentUser().getUid();
		final EnergizerB2BUnitModel b2bUnit = b2bCommerceUserService.getParentUnitForCustomer(userId);
		final String productCode = cartService.getSessionCart().getEntries().get(0).getProduct().getCode();
		final EnergizerCMIRModel energizerCMIR = energizerProductService.getEnergizerCMIR(productCode, b2bUnit.getUid());
		cartFacade.getSessionCart().setShippingPoint(energizerCMIR.getShippingPoint());

		// Redirect to the start of the checkout flow to begin the checkout process
		// We just redirect to the generic '/checkout' page which will actually select the checkout flow
		// to use. The customer is not necessarily logged in on this request, but will be forced to login
		// when they arrive on the '/checkout' page.
		return REDIRECT_PREFIX + "/checkout";

	}

	@RequestMapping(value = "/getProductVariantMatrix", method = RequestMethod.GET)
	@RequireHardLogIn
	public String getProductVariantMatrix(@RequestParam("productCode")
	final String productCode, final Model model)
	{
		final ProductModel productModel = productService.getProductForCode(productCode);

		final ProductData productData = productFacade.getProductForOptions(productModel,
				Arrays.asList(ProductOption.BASIC, ProductOption.CATEGORIES, ProductOption.VARIANT_MATRIX_BASE,
						ProductOption.VARIANT_MATRIX_PRICE, ProductOption.VARIANT_MATRIX_MEDIA, ProductOption.VARIANT_MATRIX_STOCK));

		model.addAttribute("product", productData);

		return ControllerConstants.Views.Fragments.Cart.ExpandGridInCart;
	}

	protected boolean validateCart(final RedirectAttributes redirectModel) throws CommerceCartModificationException
	{
		// Validate the cart
		final List<CartModificationData> modifications = cartFacade.validateCartData();
		if (!modifications.isEmpty())
		{
			redirectModel.addFlashAttribute("validationData", modifications);

			// Invalid cart. Bounce back to the cart page.
			return true;
		}
		return false;
	}

	@ResponseBody
	@RequestMapping(value = "/update/expectedUnitPrice", method = RequestMethod.POST)
	@RequireHardLogIn
	public String updateExpectedUnitPrice(@RequestParam("entryNumber")
	final Integer entryNumber, @RequestParam("productCode")
	final String productCode, final Model model, @Valid
	final UpdateExpectedUnitPriceForm form, final BindingResult bindingErrors, final HttpSession session)
			throws ModelNotFoundException, Exception
	{
		CartModificationData cartModification = null;
		if (bindingErrors.hasErrors())
		{
			getViewWithBindingErrorMessages(model, bindingErrors);
		}


		cartModification = energizerB2BCartFacade
				.updateOrderEntry(getOrderEntryDataForExpectedUnitPrice(form.getExpectedUnitPrice(), productCode, entryNumber));


		if (cartModification.getStatusCode().equals(SUCCESSFUL_MODIFICATION_CODE))
		{
			GlobalMessages.addMessage(model, GlobalMessages.CONF_MESSAGES_HOLDER, cartModification.getStatusMessage(), null);
		}
		else if (!model.containsAttribute(ERROR_MSG_TYPE))
		{
			GlobalMessages.addMessage(model, GlobalMessages.ERROR_MESSAGES_HOLDER, cartModification.getStatusMessage(), null);
		}

		return REDIRECT_PREFIX + "/cart";
	}

	@ResponseBody
	@RequestMapping(value = "/update/agreeEdgewellUnitPrice", method = RequestMethod.POST)
	@RequireHardLogIn
	public String updateAgreeEdgewellUnitPrice(@RequestParam("entryNumber")
	final Integer entryNumber, @RequestParam("productCode")
	final String productCode, @RequestParam("expectedUnitPrice")
	final String expectedUnitPrice, @RequestParam("agreeEdgewellPriceForAllProducts")
	final String agreeEdgewellPriceForAllProducts, final Model model, @Valid
	final UpdateExpectedUnitPriceForm form, final BindingResult bindingErrors, final HttpSession session)
			throws CMSItemNotFoundException
	{
		try
		{
			CartModificationData cartModification = null;
			if (bindingErrors.hasErrors())
			{
				getViewWithBindingErrorMessages(model, bindingErrors);
			}

			LOG.info("ExpectedUnitPrice ::: " + expectedUnitPrice + " , ProductCode :::  " + productCode + " , Entry Number ::: "
					+ entryNumber + " , agreeEdgewellUnitPrice ::: " + form.getAgreeEdgewellUnitPrice()
					+ " , agreeEdgewellPriceForAllProducts ::: " + agreeEdgewellPriceForAllProducts);

			if (Boolean.valueOf(agreeEdgewellPriceForAllProducts))
			{
				energizerB2BCartFacade.updateAgreeEdgewellPriceForAllProducts(
						getCartDataForAgreeEdgewellPriceForAllProducts(Boolean.valueOf(agreeEdgewellPriceForAllProducts)));
			}
			else
			{
				cartModification = energizerB2BCartFacade
						.updateOrderEntryForAgreeEdgewellPrice(getOrderEntryDataForAgreeEdgewellPrice(expectedUnitPrice, productCode,
								entryNumber, Boolean.valueOf(form.getAgreeEdgewellUnitPrice())));

				if (cartModification.getStatusCode().equals(SUCCESSFUL_MODIFICATION_CODE))
				{
					GlobalMessages.addMessage(model, GlobalMessages.CONF_MESSAGES_HOLDER, cartModification.getStatusMessage(), null);
				}
				else if (!model.containsAttribute(ERROR_MSG_TYPE))
				{
					GlobalMessages.addMessage(model, GlobalMessages.ERROR_MESSAGES_HOLDER, cartModification.getStatusMessage(), null);
				}
			}

		}
		catch (final Exception e)
		{
			LOG.info("Exception occured ::: " + e.getMessage());
			e.printStackTrace();
		}

		return REDIRECT_PREFIX + "/cart";
	}

	@ResponseBody
	@RequestMapping(value = "/update/agreeEdgewellUnitPriceForAllProducts", method = RequestMethod.POST)
	@RequireHardLogIn
	public String updateAgreeEdgewellUnitPriceForAllProducts(@RequestParam("agreeEdgewellPriceForAllProducts")
	final String agreeEdgewellPriceForAllProducts, final Model model, @Valid
	final UpdateExpectedUnitPriceForm form, final BindingResult bindingErrors, final HttpSession session)
			throws CMSItemNotFoundException
	{
		try
		{
			if (bindingErrors.hasErrors())
			{
				getViewWithBindingErrorMessages(model, bindingErrors);
			}

			LOG.info(" agreeEdgewellPriceForAllProducts ::: " + agreeEdgewellPriceForAllProducts);

			energizerB2BCartFacade.updateAgreeEdgewellPriceForAllProducts(
					getCartDataForAgreeEdgewellPriceForAllProducts(Boolean.valueOf(agreeEdgewellPriceForAllProducts)));
		}
		catch (final Exception e)
		{
			LOG.info("Exception occured ::: " + e.getMessage());
			e.printStackTrace();
		}

		return REDIRECT_PREFIX + "/cart";
	}

	@SuppressWarnings("unchecked")
	@ResponseBody
	@RequestMapping(value = "/update", method = RequestMethod.POST)
	@RequireHardLogIn
	public CartData updateCartQuantities(@RequestParam("entryNumber")
	final Integer entryNumber, @RequestParam("productCode")
	final String productCode, final Model model, @Valid
	final UpdateQuantityForm form, final BindingResult bindingErrors, final HttpSession session)
			throws CMSItemNotFoundException, Exception
	{
		LOG.info("********* Update Cart Quantities *********");
		CartData cartData = null;
		try
		{
			if (bindingErrors.hasErrors())
			{
				getViewWithBindingErrorMessages(model, bindingErrors);
			}

			cartEntryBusinessRulesService.clearErrors();
			final List<String> businessRuleErrors = new ArrayList<String>();
			if (null != form && null != form.getQuantity() && form.getQuantity() > 0)
			{
				LOG.info("Product Code : " + productCode + " , new Quantity : " + form.getQuantity());
				cartEntryBusinessRulesService.validateBusinessRules(getOrderEntryData(form.getQuantity(), productCode, entryNumber));
			}

			if (cartEntryBusinessRulesService.hasErrors())
			{
				final List<BusinessRuleError> businessValidationRuleErrors = cartEntryBusinessRulesService.getErrors();

				BusinessRuleError validationErrors[] = new BusinessRuleError[businessValidationRuleErrors.size()];
				validationErrors = businessValidationRuleErrors.toArray(validationErrors);
				for (int errorCount = 0; errorCount < validationErrors.length; errorCount++)
				{
					LOG.info("The error message is " + validationErrors[errorCount]);
					businessRuleErrors.add(validationErrors[errorCount].getMessage());
				}
				//remove duplicate error
				final HashSet<String> set = new HashSet<String>(businessRuleErrors);
				businessRuleErrors.clear();
				businessRuleErrors.addAll(set);
			}
			else
			{
				final CartModificationData cartModification = b2bCartFacade
						.updateOrderEntry(getOrderEntryData(form.getQuantity(), productCode, entryNumber));

				if (cartModification.getStatusCode().equals(SUCCESSFUL_MODIFICATION_CODE))
				{
					GlobalMessages.addMessage(model, GlobalMessages.CONF_MESSAGES_HOLDER, cartModification.getStatusMessage(), null);
				}
				else if (!model.containsAttribute(ERROR_MSG_TYPE))
				{
					GlobalMessages.addMessage(model, GlobalMessages.ERROR_MESSAGES_HOLDER, cartModification.getStatusMessage(), null);
				}
			}
			if (bindingErrors.hasErrors())
			{
				getViewWithBindingErrorMessages(model, bindingErrors);
			}


			cartData = cartFacade.getSessionCart();
			final String userId = userService.getCurrentUser().getUid();
			final EnergizerB2BUnitModel b2bUnit = b2bCommerceUserService.getParentUnitForCustomer(userId);
			LOG.info(" Freight Type: " + b2bUnit.getFreightType());

			final String PERSONALCARE = getConfigValue("site.personalCare");
			final String PERSONALCARE_EMEA = getConfigValue("site.personalCareEMEA");

			/** Energizer Container Utilization service */
			if ((null == b2bUnit.getFreightType() || StringUtils.isEmpty(b2bUnit.getFreightType())
					|| FREIGHT_CONTAINER.equalsIgnoreCase(b2bUnit.getFreightType()))
					&& this.getCmsSiteService().getCurrentSite().getUid().equalsIgnoreCase(PERSONALCARE))
			{
				if (!isSalesRepUserLogin())

				{

					LOG.info("Started container optimization logic for LATAM !!!");

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

					LOG.info(" Container Height: " + containerHeight);
					LOG.info(" Packing Type: " + packingOption);
					LOG.info(" Enable/Disable " + enableButton);
					cartData = energizerCartService.calCartContainerUtilization(cartFacade.getSessionCart(), containerHeight,
							packingOption, enableButton);

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
					}

					if (cartData.isIsFloorSpaceFull() && cartData.getContainerPackingType().equalsIgnoreCase("2 SLIP SHEETS")
							&& enableButton)
					{
						GlobalMessages.addErrorMessage(model, "errorMessages.enable.2slipsheet");
						businessRuleErrors.add(Localization.getLocalizedString("errorMessages.enable.2slipsheet"));
					}

					if (cartData.isIsContainerFull())
					{
						businessRuleErrors.add(Localization.getLocalizedString(ORDER_EXCEEDED));
					}

					if (cartData.isIsOrderBlocked())
					{
						businessRuleErrors.add(Localization.getLocalizedString(ORDER_BLOCKED));
					}
				}
				//cartData = cartFacade.getSessionCart();

			}
			else if (null != b2bUnit.getFreightType() && !StringUtils.isEmpty(b2bUnit.getFreightType())
					&& getSiteUid().equalsIgnoreCase(PERSONALCARE_EMEA) && (FREIGHT_TRUCK.equalsIgnoreCase(b2bUnit.getFreightType())
							|| FREIGHT_CONTAINER.equalsIgnoreCase(b2bUnit.getFreightType())))
			{

				LOG.info("Started truck/container optimization logic for EMEA !!!");

				final List<String> packingOptionsList = new ArrayList<String>();

				enableButton = b2bUnit.getEnableContainerOptimization() == null ? false : b2bUnit.getEnableContainerOptimization();

				LOG.info(" EnableButton: " + enableButton);

				cartData = energizerCartService.calCartTruckUtilization(cartFacade.getSessionCart(), b2bUnit.getFreightType(),
						"1 WOODEN BASE", enableButton, b2bUnit.getPalletType());

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
				packingOptionsList.add("Wooden Base");
				model.addAttribute("packingOptionList", packingOptionsList);
			}

			if (null == b2bUnit.getFreightType() && getSiteUid().equalsIgnoreCase(PERSONALCARE_EMEA))
			{
				LOG.info("FreightType is null not set up ::: ");
			}

			cartData.setBusinesRuleErrors(businessRuleErrors);
			cartData.setFloorSpaceProductsMap(energizerCartService.getFloorSpaceProductsMap());
			cartData.setNonPalletFloorSpaceProductsMap(energizerCartService.getNonPalletFloorSpaceProductsMap());
			cartData.setProductsNotAddedToCart(energizerCartService.getProductNotAddedToCart());
			cartData.setProductsNotDoubleStacked(energizerCartService.getProductsNotDoublestacked());
			energizerB2BCheckoutFlowFacade.setContainerAttributes(cartData);

			//Added for WeSell - Disable Checkout button when the quantity is updated.
			if (isSalesRepUserLogin())
			{
				cartData.setPlacedBySalesRep(true);
				session.setAttribute("gotPriceFromSAP", false);
			}
			else
			{
				cartData.setPlacedBySalesRep(false);
			}
		}
		catch (final NullPointerException ne)
		{
			LOG.error("NullPointerException occured ::: " + ne);
			ne.printStackTrace();
			sessionService.setAttribute("isUpdateQtyError", true);
			return null;
		}
		catch (final Exception e)
		{
			LOG.error("Exception occured during update quantity ::: " + e);
			e.printStackTrace();
			sessionService.setAttribute("isUpdateQtyError", true);
			return null;
		}

		return cartData;

	}

	@SuppressWarnings("unchecked")
	protected CartData createProductList(final Model model) throws Exception
	{
		CartData cartData = cartFacade.getSessionCart();
		final String PERSONALCARE = getConfigValue("site.personalCare");
		final String PERSONALCARE_EMEA = getConfigValue("site.personalCareEMEA");
		int limit = 1;

		boolean cartEntriesSizeExceed = false;
		try
		{
			final String CartEntriesMaxLimit = configurationService.getConfiguration().getString("wesell.cart.entries.maximum.size");
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
			final List<String> businessRuleErrors = new ArrayList<String>();
			//	reverseCartProductsOrder(cartData.getEntries());
			if (cartData.getEntries() != null && !cartData.getEntries().isEmpty())
			{
				final List<String> priceNotUpdatedProducts = new ArrayList<String>();
				boolean flag = false;
				String productWithCmirInActive = "";
				int agreeEdgewellUnitPriceForAllProducts = 0;
				for (OrderEntryData entry : cartData.getEntries())
				{
					if (this.getCmsSiteService().getCurrentSite().getUid().equalsIgnoreCase(PERSONALCARE) && !isSalesRepUserLogin())
					{
						if (null != entry.getEachUnitPrice() && null != entry.getEachUnitPrice().getValue()
								&& entry.getEachUnitPrice().getValue().doubleValue() == 0.01)
						{
							priceNotUpdatedProducts.add(entry.getProduct().getCode());
						}
					}
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

					/*
					 * energizerB2BCartFacade.updateOrderEntry(getOrderEntryDataForExpectedUnitPrice(
					 * updateExpectedUnitPriceForm.getExpectedUnitPrice(), entry.getProduct().getCode(),
					 * entry.getEntryNumber()));
					 */

					model.addAttribute("UpdateExpectedUnitPriceForm" + entry.getEntryNumber(), updateExpectedUnitPriceForm);

					if (entry.getProduct().isIsActive() == false || entry.getProduct().isObsolete())
					{
						productWithCmirInActive += entry.getProduct().getErpMaterialID() + "  ";
						flag = true;
					}

					// Check if customer disagrees Edgewell price for at least one product
					if (!entry.isAgreeEdgewellUnitPrice())
					{
						agreeEdgewellUnitPriceForAllProducts += 1;
					}
				}

				if (CollectionUtils.isNotEmpty(priceNotUpdatedProducts))
				{

					String productCode = "";
					int count = 1;
					for (final String code : priceNotUpdatedProducts)
					{

						if (count < priceNotUpdatedProducts.size())
						{
							productCode = productCode.concat(code).concat(" ");
						}
						else
						{
							productCode = productCode.concat(code);
						}
						count++;
					}
					GlobalMessages.addMessage(model, "accErrorMsgs", "cart.price.incorrect", new Object[]
					{ productCode });


					model.addAttribute("productPriceNotUpdated", true);
				}
				else
				{
					model.addAttribute("productPriceNotUpdated", false);
				}
				if (flag == true)
				{
					GlobalMessages.addMessage(model, "accErrorMsgs", "cart.cmirinactive", new Object[]
					{ productWithCmirInActive });
					//return FORWARD_PREFIX + "/cart";
				}
				model.addAttribute("cmirNotAvailable", flag);
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
				updateExpectedUnitPriceForm
						.setAgreeEdgewellUnitPriceForAllProducts(cartData.isAgreeEdgewellUnitPriceForAllProducts());
				model.addAttribute("UpdateExpectedUnitPriceForm", updateExpectedUnitPriceForm);

			}

			/** Energizer Container Utilization service */

			final String userId = userService.getCurrentUser().getUid();
			final EnergizerB2BUnitModel b2bUnit = b2bCommerceUserService.getParentUnitForCustomer(userId);

			LOG.info(" Freight Type: " + b2bUnit.getFreightType());

			if (((null == b2bUnit.getFreightType() || StringUtils.isEmpty(b2bUnit.getFreightType())
					|| FREIGHT_CONTAINER.equalsIgnoreCase(b2bUnit.getFreightType()))
					&& this.getCmsSiteService().getCurrentSite().getUid().equalsIgnoreCase(PERSONALCARE)))
			{
				//				WeSell Implementation -  Added Code Changes for Container Optimization for Sales Rep login - by Venkat
				if (!isSalesRepUserLogin())
				{
					//				WeSell Implementation -  Added Code Changes for Container Optimization for Sales Rep login - by Venkat
					LOG.info("Started container optimization logic for LATAM !!!");
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

					cartData = energizerCartService.calCartContainerUtilization(cartData, containerHeight, packingOption,
							enableButton);

					if (cartData.isIsFloorSpaceFull() && cartData.getContainerPackingType().equalsIgnoreCase("2 SLIP SHEETS")
							&& enableButton)
					{
						GlobalMessages.addErrorMessage(model, "errorMessages.enable.2slipsheet");
						businessRuleErrors.add(Localization.getLocalizedString("errorMessages.enable.2slipsheet"));
					}

					/***** Added by Soma for blocking the cart & disabling the checkout button ******/
					if (cartData.isIsContainerFull())
					{
						businessRuleErrors.add(Localization.getLocalizedString(ORDER_EXCEEDED));
					}
					/***** Added by Soma for blocking the cart & disabling the checkout button ******/

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

					}
					cartData.setBusinesRuleErrors(businessRuleErrors);

					final HashMap productsNotDoubleStacked = energizerCartService.getProductsNotDoublestacked();

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

					cartData.setFloorSpaceProductsMap(energizerCartService.getFloorSpaceProductsMap());
					cartData.setNonPalletFloorSpaceProductsMap(energizerCartService.getNonPalletFloorSpaceProductsMap());
					cartData.setProductsNotAddedToCart(energizerCartService.getProductNotAddedToCart());
					cartData.setProductsNotDoubleStacked(energizerCartService.getProductsNotDoublestacked());

					energizerB2BCheckoutFlowFacade.setContainerAttributes(cartData);

					model.addAttribute("containerHeightList", containerHeightList);
					model.addAttribute("packingOptionList", packingOptionsList);
					model.addAttribute("containerUtilizationForm", contUtilForm);
					model.addAttribute("freightType", b2bUnit.getFreightType());
					model.addAttribute("palletType", b2bUnit.getPalletType());
					//				WeSell Implementation -  Added Code Changes for Container Optimization for Sales Rep login - by Venkat
				}
				//			WeSell Implementation -  Added Code Changes for Container Optimization for Sales Rep login - by Venkat
			}
			/* } */
			// Added code changes for Truck/Container Optimization for EMEA
			if (null != b2bUnit.getFreightType() && !StringUtils.isEmpty(b2bUnit.getFreightType())
					&& getSiteUid().equalsIgnoreCase(PERSONALCARE_EMEA) && (FREIGHT_TRUCK.equalsIgnoreCase(b2bUnit.getFreightType())
							|| FREIGHT_CONTAINER.equalsIgnoreCase(b2bUnit.getFreightType())))
			{
				LOG.info("Started Truck/Container Optimization logic for EMEA !!!");
				LOG.info("FreightType ::: " + b2bUnit.getFreightType() + " , PalletType ::: " + b2bUnit.getPalletType()
						+ " , containerHeight ::: " + cartData.getContainerHeight());

				enableButton = b2bUnit.getEnableContainerOptimization() == null ? false : b2bUnit.getEnableContainerOptimization();

				final List<String> packingOptionsList = new ArrayList<String>();

				LOG.info(" EnableButton: " + enableButton);

				containerHeight = getConfigValue("container.default.height");

				// Setting up container height
				/*
				 * if (contUtilForm.getContainerHeight() != null ||
				 * FREIGHT_CONTAINER.equalsIgnoreCase(b2bUnit.getFreightType())) { containerHeight =
				 * contUtilForm.getContainerHeight(); cartData.setContainerHeight(containerHeight);
				 * LOG.info("Container Height set as ::: " + cartData.getContainerHeight()); }
				 */

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


				packingOptionsList.add("Wooden Base");

				energizerB2BCheckoutFlowFacade.setContainerAttributes(cartData);

				model.addAttribute("containerHeightList", containerHeightList);
				model.addAttribute("packingOptionList", packingOptionsList);
				model.addAttribute("enableButton", enableButton);
				model.addAttribute("containerUtilizationForm", contUtilForm);
				model.addAttribute("freightType", b2bUnit.getFreightType());
				model.addAttribute("palletType", b2bUnit.getPalletType());
			}

			//Added Code changes for WeSell Implementation - END
			if ((boolean) sessionService.getAttribute("isUpdateQtyError"))
			{
				model.addAttribute("isUpdateQtyError", (boolean) sessionService.getAttribute("isUpdateQtyError"));
			}
			else
			{
				model.addAttribute("isUpdateQtyError", false);
			}
			sessionService.setAttribute("isUpdateQtyError", false);

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
			model.addAttribute(WebConstants.BREADCRUMBS_KEY, resourceBreadcrumbBuilder.getBreadcrumbs("breadcrumb.cart"));
			model.addAttribute("pageType", PageType.CART.name());
			return cartData;
		}
		catch (final Exception e)
		{
			GlobalMessages.addErrorMessage(model, "cart.page.error.message");
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
			model.addAttribute(WebConstants.BREADCRUMBS_KEY, resourceBreadcrumbBuilder.getBreadcrumbs("breadcrumb.cart"));
			model.addAttribute("pageType", PageType.CART.name());
			throw e;
		}
	}

	@RequestMapping(value = "/updateprofile", method = RequestMethod.POST)
	@RequireHardLogIn
	public void updateProfile(@Valid
	final UpdateProfileForm updateProfileForm, final BindingResult bindingResult, final Model model,
			final RedirectAttributes redirectAttributes) throws CMSItemNotFoundException
	{
		LOG.info("for container utilization");
	}

	protected void reverseCartProductsOrder(final List<OrderEntryData> entries)
	{
		if (entries != null)
		{
			Collections.reverse(entries);
		}
	}

	protected CartData prepareDataForPage(final Model model) throws CMSItemNotFoundException, Exception
	{
		CartData cartData = null;
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
			cartData = createProductList(model);
		}
		catch (final Exception e)
		{
			LOG.info("Exception occured ::: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}
		model.addAttribute(WebConstants.BREADCRUMBS_KEY, resourceBreadcrumbBuilder.getBreadcrumbs("breadcrumb.cart"));
		model.addAttribute("pageType", PageType.CART.name());
		return cartData;
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
		return Views.Fragments.Cart.AddToCartPopup;
	}


	protected OrderEntryData getOrderEntryData(final long quantity, final String productCode, final Integer entryNumber)
			throws ModelNotFoundException, Exception
	{
		final OrderEntryData orderEntry = new OrderEntryData();
		orderEntry.setQuantity(quantity);
		orderEntry.setProduct(new ProductData());
		orderEntry.getProduct().setCode(productCode);
		orderEntry.setEntryNumber(entryNumber);

		final String userId = userService.getCurrentUser().getUid();
		final EnergizerB2BUnitModel b2bUnit = b2bCommerceUserService.getParentUnitForCustomer(userId);
		final EnergizerCMIRModel energizerCMIR = energizerProductService.getEnergizerCMIR(productCode, b2bUnit.getUid());
		orderEntry.getProduct().setUom(null != energizerCMIR ? energizerCMIR.getUom() : null);
		return orderEntry;

	}


	protected OrderEntryData getOrderEntryDataForExpectedUnitPrice(final String expectedUnitPrice, final String productCode,
			final Integer entryNumber) throws ModelNotFoundException, Exception
	{
		final OrderEntryData orderEntry = new OrderEntryData();
		orderEntry.setExpectedUnitPrice(expectedUnitPrice);
		orderEntry.setProduct(new ProductData());
		orderEntry.getProduct().setCode(productCode);
		orderEntry.setEntryNumber(entryNumber);

		final String userId = userService.getCurrentUser().getUid();
		final EnergizerB2BUnitModel b2bUnit = b2bCommerceUserService.getParentUnitForCustomer(userId);
		final EnergizerCMIRModel energizerCMIR = energizerProductService.getEnergizerCMIR(productCode, b2bUnit.getUid());
		orderEntry.getProduct().setUom(energizerCMIR.getUom());

		return orderEntry;

	}

	protected OrderEntryData getOrderEntryDataForAgreeEdgewellPrice(final String expectedUnitPrice, final String productCode,
			final Integer entryNumber, final boolean agreeEdgewellUnitPrice) throws ModelNotFoundException, Exception
	{
		final OrderEntryData orderEntry = new OrderEntryData();
		orderEntry.setProduct(new ProductData());
		orderEntry.getProduct().setCode(productCode);
		orderEntry.setEntryNumber(entryNumber);
		orderEntry.setAgreeEdgewellUnitPrice(agreeEdgewellUnitPrice);

		final String userId = userService.getCurrentUser().getUid();
		final EnergizerB2BUnitModel b2bUnit = b2bCommerceUserService.getParentUnitForCustomer(userId);
		final EnergizerCMIRModel energizerCMIR = energizerProductService.getEnergizerCMIR(productCode, b2bUnit.getUid());
		final EnergizerProductModel energizerProductModel = energizerProductService.getProductWithCode(productCode);
		orderEntry.getProduct().setUom(energizerCMIR.getUom());

		// Setting each unit price to the order entry
		orderEntry.setEachUnitPrice(energizerB2BCartFacade.getEachUnitPrice(energizerProductModel, b2bUnit));

		// If user agrees to edgewell unit price, then the expected unit price will be same as edgewell product unit EACH price
		if (agreeEdgewellUnitPrice)
		{
			orderEntry.setExpectedUnitPrice(orderEntry.getEachUnitPrice().getValue().toString());
		}
		else
		{
			//orderEntry.setExpectedUnitPrice(expectedUnitPrice);
			orderEntry.setExpectedUnitPrice(null);
		}

		return orderEntry;

	}

	protected CartData getCartDataForAgreeEdgewellPriceForAllProducts(final boolean agreeEdgewellPriceForAllProducts)
	{
		final CartData cartData = cartFacade.getSessionCart();
		// If the customer agrees for edgewell EACH unit price for all the products, then update the expected unit price to the EACH unit price
		if (agreeEdgewellPriceForAllProducts)
		{
			for (final OrderEntryData entry : cartData.getEntries())
			{
				entry.setExpectedUnitPrice(null != entry.getEachUnitPrice() ? entry.getEachUnitPrice().getValue().toString() : null);
				entry.setAgreeEdgewellUnitPrice(agreeEdgewellPriceForAllProducts);
			}
			LOG.info(
					"agreeEdgewellPriceForAllProducts is 'true'. So , updating agreeEdgewellUnitPrice as 'true' for all the products !");
		}
		else
		{
			for (final OrderEntryData entry : cartData.getEntries())
			{
				entry.setExpectedUnitPrice(null);
				entry.setAgreeEdgewellUnitPrice(agreeEdgewellPriceForAllProducts);
			}
			LOG.info(
					"agreeEdgewellPriceForAllProducts is 'false'. So , updating agreeEdgewellUnitPrice as 'false' for all the products !");
		}
		cartData.setAgreeEdgewellUnitPriceForAllProducts(agreeEdgewellPriceForAllProducts);

		return cartData;

	}

	private CartData setUpCartPageBasicData(final Model model) throws CMSItemNotFoundException, Exception
	{
		CartData cartData = null;
		try
		{
			final String userId = userService.getCurrentUser().getUid();
			final EnergizerB2BUnitModel b2bUnit = b2bCommerceUserService.getParentUnitForCustomer(userId);

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
				final boolean enableForB2BUnit = b2bUnit.getEnableContainerOptimization() == null ? false
						: b2bUnit.getEnableContainerOptimization();

				cartData = prepareDataForPage(model);

				model.addAttribute("enableButton", enableButton);
				model.addAttribute("enableTruck", enableTruck);
				model.addAttribute("enableForB2BUnit", enableForB2BUnit);
			}
			else
			{
				enableButton = b2bUnit.getEnableContainerOptimization() == null ? false : b2bUnit.getEnableContainerOptimization();

				boolean enableForB2BUnit = b2bUnit.getEnableContainerOptimization() == null ? false
						: b2bUnit.getEnableContainerOptimization();
				cartData = cartFacade.getSessionCart();
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
				cartData = prepareDataForPage(model);
				//			WeSell Implementation -  Added Code Changes for Container Optimization for Sales Rep login - by Venkat
				if (isSalesRepUserLogin())
				{
					enableButton = false;
				}
				//			WeSell Implementation -  Added Code Changes for Container Optimization for Sales Rep login - by Venkat
				model.addAttribute("enableButton", enableButton);
				model.addAttribute("enableForB2BUnit", enableForB2BUnit);

			}
			if (null == b2bUnit.getFreightType() && getSiteUid().equalsIgnoreCase(PERSONALCARE_EMEA))
			{
				LOG.info("FreightType is null, not set up ::: ");
			}
			if (null == b2bUnit.getPalletType() && getSiteUid().equalsIgnoreCase(PERSONALCARE_EMEA))
			{
				LOG.info("PalletType is null, not set up ::: ");
			}
		}
		catch (final Exception e)
		{
			throw e;
		}

		//Added for WeSell - Disable Checkout button when the quantity is updated.
		if (isSalesRepUserLogin())
		{
			cartData.setPlacedBySalesRep(true);
		}
		else
		{
			cartData.setPlacedBySalesRep(false);
		}
		return cartData;
	}

	//Added Code changes for WeSell Implementation - START
	@RequestMapping(value = "/getPrice", method = RequestMethod.GET)
	@RequireHardLogIn
	public String getPrice(final Model model, final HttpSession session, final RedirectAttributes redirectModel) throws Exception
	{
		LOG.info("Entering /getPrice method !!");
		try
		{
			CartData cartData = setUpCartPageBasicData(model);
			cartData = energizerB2BCheckoutFlowFacade.simulateOrder(cartData, EnergizerCoreConstants.CART);
			/*-for (final OrderEntryData entry : cartData.getEntries())
			{
				if (null == entry.getBasePrice() || null == entry.getBasePrice().getValue()
						|| (null != entry.getBasePrice().getValue() && entry.getBasePrice().getValue().doubleValue() <= 0.01))
				{
					isPriceGotFromSAP = false;
					break;
				}
			}
			if (!isPriceGotFromSAP)
			{
				LOG.info("Exception occured when getting price from SAP::: ");
				GlobalMessages.addErrorMessage(model, "Unable to get price from SAP,Please try After some time");
				session.setAttribute("gotPriceFromSAP", false);
				return Views.Pages.Cart.CartPage;
			}*/

			cartData.setPlacedBySalesRep(true);

			model.addAttribute("cartData", cartData);
			session.setAttribute("gotPriceFromSAP", true);
			String InvalidPriceProducts = "";
			boolean flag = false;
			for (final OrderEntryData entry : cartData.getEntries())
			{
				if (entry.getBasePrice().getValue().doubleValue() <= 0.01)
				{
					model.addAttribute("cartData", cartData);
					model.addAttribute("entryPriceIsZero", true);
					InvalidPriceProducts += entry.getProduct().getCode() + "  ";
					flag = true;
				}
			}
			if (flag)
			{
				GlobalMessages.addMessage(model, "accErrorMsgs", "cart.price.incorrect.message", new Object[]
				{ InvalidPriceProducts });
			}
		}
		catch (final Exception e)
		{
			if (e instanceof AddressException)
			{
				LOG.info(e.getMessage());
				GlobalMessages.addErrorMessage(model, "checkout.simulateOrder.soldTo.shipTo.failed");
			}
			if (e instanceof HttpClientErrorException)
			{
				LOG.error("Order simulation failed in SAP because of bad format of xml ... ");
				GlobalMessages.addErrorMessage(model, "Order simulation failed in SAP because of bad format of xml");
			}
			else if (e instanceof RestClientException)
			{
				LOG.error("Order simulation failed in SAP because of " + e);
				GlobalMessages.addErrorMessage(model,
						"Unable to fetch price(s) from SAP due to connectivity issues. Please try after sometime.");
			}
			else if (e instanceof JAXBException)
			{
				LOG.error("Order simulation failed in SAP because of improper xml/data" + e);
				GlobalMessages.addErrorMessage(model, "Unable to fetch price(s) from SAP because of improper request xml/data");
			}
			else
			{
				LOG.error("Exception occured during simulate order ::: " + e);
			}
			e.printStackTrace();
			GlobalMessages.addErrorMessage(model, "cart.simulate.order.failed");
			session.setAttribute("gotPriceFromSAP", false);
			return Views.Pages.Cart.CartPage;
		}
		finally
		{
			final String orderSimulateErrorMessage = sessionService.getAttribute("orderSimulateErrorMessage");

			if (null != orderSimulateErrorMessage && !StringUtils.isEmpty(orderSimulateErrorMessage.toString()))
			{
				//GlobalMessages.addErrorMessage(model, "checkout.simulateOrder.failed.error.message");
				GlobalMessages.addMessage(model, "businessRuleError", "cart.simulateOrder.failed.simulate.error.message", new Object[]
				{ orderSimulateErrorMessage });

			}
		}
		LOG.info("Exiting /getPrice method !!");
		return Views.Pages.Cart.CartPage;
	}

	//Added Code changes for WeSell Implementation - END
	@Override
	public String getConfigValue(final String key)
	{
		return configurationService.getConfiguration().getString(key);
	}
}
