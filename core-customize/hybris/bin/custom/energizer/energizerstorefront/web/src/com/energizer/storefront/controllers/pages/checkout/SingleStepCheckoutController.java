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
package com.energizer.storefront.controllers.pages.checkout;

import de.hybris.platform.b2bacceleratorfacades.order.data.B2BCommentData;
import de.hybris.platform.b2bacceleratorfacades.order.data.B2BDaysOfWeekData;
import de.hybris.platform.b2bacceleratorfacades.order.data.B2BPaymentTypeData;
import de.hybris.platform.b2bacceleratorfacades.order.data.B2BReplenishmentRecurrenceEnum;
import de.hybris.platform.b2bacceleratorfacades.order.data.ScheduledCartData;
import de.hybris.platform.b2bacceleratorfacades.order.data.TriggerData;
import de.hybris.platform.b2bacceleratorservices.enums.CheckoutPaymentType;
import de.hybris.platform.b2bcommercefacades.company.data.B2BCostCenterData;
import de.hybris.platform.b2bcommercefacades.company.impl.DefaultB2BCostCenterFacade;
import de.hybris.platform.cms2.exceptions.CMSItemNotFoundException;
import de.hybris.platform.cms2.model.pages.ContentPageModel;
import de.hybris.platform.commercefacades.order.CartFacade;
import de.hybris.platform.commercefacades.order.data.CCPaymentInfoData;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.order.data.DeliveryModeData;
import de.hybris.platform.commercefacades.order.data.OrderData;
import de.hybris.platform.commercefacades.order.data.OrderEntryData;
import de.hybris.platform.commercefacades.order.data.ZoneDeliveryModeData;
import de.hybris.platform.commercefacades.product.ProductFacade;
import de.hybris.platform.commercefacades.product.ProductOption;
import de.hybris.platform.commercefacades.product.data.ProductData;
import de.hybris.platform.commercefacades.storesession.StoreSessionFacade;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commercefacades.user.data.CountryData;
import de.hybris.platform.commercefacades.user.data.TitleData;
import de.hybris.platform.commerceservices.order.CommerceCartModificationException;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.cronjob.enums.DayOfWeek;
import de.hybris.platform.order.CartService;
import de.hybris.platform.order.InvalidCartException;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.session.Session;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.util.Config;
import de.hybris.platform.util.localization.Localization;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.mail.internet.AddressException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.ValidationException;
import javax.xml.bind.JAXBException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.energizer.business.BusinessRuleError;
import com.energizer.core.business.service.EnergizerOrderBusinessRuleValidationService;
import com.energizer.core.constants.EnergizerCoreConstants;
import com.energizer.core.data.EnergizerDeliveryNoteData;
import com.energizer.core.datafeed.facade.impl.DefaultEnergizerAddressFacade;
import com.energizer.core.model.EnergizerB2BEmployeeModel;
import com.energizer.core.model.EnergizerB2BUnitModel;
import com.energizer.core.model.EnergizerCMIRModel;
import com.energizer.facades.flow.impl.DefaultEnergizerB2BCheckoutFlowFacade;
import com.energizer.facades.order.impl.DefaultEnergizerB2BOrderHistoryFacade;
import com.energizer.facades.order.impl.DefaultEnergizerDeliveryNoteFacade;
import com.energizer.storefront.annotations.RequireHardLogIn;
import com.energizer.storefront.breadcrumb.impl.ContentPageBreadcrumbBuilder;
import com.energizer.storefront.constants.WebConstants;
import com.energizer.storefront.controllers.ControllerConstants;
import com.energizer.storefront.controllers.util.GlobalMessages;
import com.energizer.storefront.forms.AddressForm;
import com.energizer.storefront.forms.PaymentDetailsForm;
import com.energizer.storefront.forms.PlaceOrderForm;
import com.energizer.storefront.forms.UpdateQuantityForm;
import com.energizer.storefront.forms.validation.PaymentDetailsValidator;
import com.energizer.storefront.security.B2BUserGroupProvider;
import com.energizer.storefront.util.XSSFilterUtil;


/**
 * SingleStepCheckoutController
 */
@Controller
@Scope("tenant")
@RequestMapping(value = "/checkout/single")
public class SingleStepCheckoutController extends AbstractCheckoutController
{
	protected static final Logger LOG = Logger.getLogger(SingleStepCheckoutController.class);

	private static final String SINGLE_STEP_CHECKOUT_SUMMARY_CMS_PAGE = "singleStepCheckoutSummaryPage";

	private static final String SINGLE_STEP_SIMULATE_CHECKOUT_SUMMARY_CMS_PAGE = "singleStepSimulateCheckoutSummaryPage";

	private static final String PONUMBER_PATTERN = Config.getParameter("ponumber.checking.pattern");

	private static final String AGREE_NEGOTIATE_EDGEWELL_UNIT_PRICE = "agree.negotiate.edgewell.unit.price";

	private static final String PERSONALCARE = "personalCare";

	private static final String PERSONALCARE_EMEA = "personalCareEMEA";

	@Resource(name = "paymentDetailsValidator")
	private PaymentDetailsValidator paymentDetailsValidator;

	@Resource(name = "b2bProductFacade")
	private ProductFacade productFacade;

	@Resource(name = "b2bUserGroupProvider")
	private B2BUserGroupProvider b2bUserGroupProvider;

	@Resource(name = "b2bContentPageBreadcrumbBuilder")
	private ContentPageBreadcrumbBuilder contentPageBreadcrumbBuilder;

	@Resource(name = "energizerB2BCheckoutFlowFacade")
	private DefaultEnergizerB2BCheckoutFlowFacade energizerB2BCheckoutFlowFacade;

	@Resource(name = "defaultEnergizerAddressFacade")
	private DefaultEnergizerAddressFacade defaultEnergizerAddressFacade;

	@Resource(name = "cartFacade")
	private CartFacade cartFacade;

	@Resource(name = "defaultB2BCostCenterFacade")
	private DefaultB2BCostCenterFacade defaultB2BCostCenterFacade;

	@Resource
	private CartService cartService;

	@Resource(name = "defaultEnergizerDeliveryNoteFacade")
	private DefaultEnergizerDeliveryNoteFacade deliveryNoteFacade;


	@Resource(name = "modelService")
	ModelService modelService;

	@Resource
	private EnergizerOrderBusinessRuleValidationService orderBusinessRulesService;

	@Resource(name = "storeSessionFacade")
	private StoreSessionFacade storeSessionFacade;

	@Resource
	private DefaultEnergizerB2BOrderHistoryFacade defaultEnergizerB2BOrderHistoryFacade;

	@Resource(name = "userService")
	UserService userService;

	@Resource
	private SessionService sessionService;

	@ModelAttribute("titles")
	public Collection<TitleData> getTitles()
	{
		return getUserFacade().getTitles();
	}

	@ModelAttribute("countries")
	public Collection<CountryData> getCountries()
	{
		return getCheckoutFlowFacade().getDeliveryCountries();
	}

	@ModelAttribute("billingCountries")
	public Collection<CountryData> getBillingCountries()
	{
		return getCheckoutFlowFacade().getBillingCountries();
	}


	@ModelAttribute("costCenters")
	public List<? extends B2BCostCenterData> getVisibleActiveCostCenters()
	{
		final List<? extends B2BCostCenterData> costCenterData = defaultB2BCostCenterFacade.getActiveCostCenters();
		return costCenterData == null ? Collections.<B2BCostCenterData> emptyList() : costCenterData;
	}


	@ModelAttribute("paymentTypes")
	public Collection<B2BPaymentTypeData> getAllB2BPaymentTypes()
	{
		return getCheckoutFlowFacade().getPaymentTypesForCheckoutSummary();
	}


	@ModelAttribute("daysOfWeek")
	public Collection<B2BDaysOfWeekData> getAllDaysOfWeek()
	{
		return getCheckoutFlowFacade().getDaysOfWeekForReplenishmentCheckoutSummary();
	}

	@InitBinder
	protected void initBinder(final HttpServletRequest request, final ServletRequestDataBinder binder)
	{
		final DateFormat dateFormat = new SimpleDateFormat(
				getMessageSource().getMessage("text.store.dateformat", null, getI18nService().getCurrentLocale()));
		final CustomDateEditor editor = new CustomDateEditor(dateFormat, true);
		binder.registerCustomEditor(Date.class, editor);
	}

	@RequestMapping(method =
	{ RequestMethod.GET, RequestMethod.POST })
	public String checkoutSummary(final RedirectAttributes redirectModel, final Model model)
	{
		try
		{
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

			if (PERSONALCARE.equalsIgnoreCase(this.getSiteUid()) && !isSalesRepUserLogin())
			{
				LOG.info("Validating cart entry data for Customer expected price & Agree Edgewell Price ..... ");

				/* Added by Soma for Checkout Button Validation Issue - START */
				final CartData cartData = cartFacade.getSessionCart();

				int agreeOrNegotiateEdgewellUnitPriceCounter = 0;

				final List<OrderEntryData> orderEntryDataErrors = new ArrayList<OrderEntryData>();

				if (null != cartData && null != cartData.getEntries() && !cartData.getEntries().isEmpty())
				{
					for (final OrderEntryData entry : cartData.getEntries())
					{
						LOG.info("ExpectedUnitPrice ::: " + entry.getExpectedUnitPrice() + " , isAgreeEdgewellUnitPrice ::: "
								+ entry.isAgreeEdgewellUnitPrice());
						if ((null == entry.getExpectedUnitPrice() || StringUtils.isEmpty(entry.getExpectedUnitPrice()))
								&& !entry.isAgreeEdgewellUnitPrice())
						{
							LOG.info("Either agree Edgewell Unit Price or enter expected price for this product === "
									+ entry.getProduct().getCode());

							orderEntryDataErrors.add(entry);
							agreeOrNegotiateEdgewellUnitPriceCounter += 1;
						}
					}

					LOG.info("agreeOrNegotiateEdgewellUnitPriceCounter ::: " + agreeOrNegotiateEdgewellUnitPriceCounter);
					LOG.info("Cart Entries errors list size ::: " + orderEntryDataErrors.size());
				}

				if (agreeOrNegotiateEdgewellUnitPriceCounter > 0)
				{

					GlobalMessages.addBusinessRuleMessage(model, AGREE_NEGOTIATE_EDGEWELL_UNIT_PRICE);

					model.addAttribute("orderEntryDataErrors", orderEntryDataErrors);

					return FORWARD_PREFIX + "/cart";
				}
				/* Added by Soma for Checkout Button Validation Issue - END */
			}

			if (hasItemsInCart())
			{
				return REDIRECT_PREFIX + "/checkout/single/summary";
			}
		}
		catch (final Exception e)
		{
			LOG.error("Exception occured while feteching checkout summary ::: " + e);
			GlobalMessages.addErrorMessage(model, "form.single.checkout.summary.error");
			return REDIRECT_PREFIX + "/checkout/single/summary";
		}
		return REDIRECT_PREFIX + "/cart";
	}


	@RequestMapping(value = "/summary", method =
	{ RequestMethod.GET, RequestMethod.POST })
	@RequireHardLogIn
	public String checkoutSummary(final Model model)
	{
		try
		{
			int leadTime = 0;
			if (!b2bUserGroupProvider.isCurrentUserAuthorizedToCheckOut())
			{
				GlobalMessages.addErrorMessage(model, "checkout.error.invalid.accountType");
				return FORWARD_PREFIX + "/cart";
			}

			if (!hasItemsInCart())
			{
				// no items in the cart
				return FORWARD_PREFIX + "/cart";
			}

			getCheckoutFlowFacade().setDeliveryAddressIfAvailable();
			getCheckoutFlowFacade().setDeliveryModeIfAvailable();
			getCheckoutFlowFacade().setPaymentInfoIfAvailable();
			getCheckoutFlowFacade().setDefaultPaymentTypeForCheckout();

			LOG.info("before address");
			if (getDeliveryAddressesForB2Bunit().size() == 1)
			{
				final AddressData addressData = getDeliveryAddressesForB2Bunit().get(0);
				energizerB2BCheckoutFlowFacade.setSingleDeliveryAddress(addressData);

				final String shippingPoint = getShippingPoint();
				final String soldToAddressId = addressData.getErpAddressId();
				int defaultLeadTime = 0;

				/*
				 * if (super.getSiteUid().equalsIgnoreCase(PERSONALCARE_EMEA)) {
				 */
				//Removing Lead Time for LATAM. Estimated delivery date is returned from simulate response.
				// Calculating Lead Time for EMEA
				if (super.getSiteUid().equalsIgnoreCase(PERSONALCARE))
				{
					// Added code changes for WeSell Implementation
					if (isSalesRepUserLogin())
					{
						defaultLeadTime = Integer.parseInt(getConfigValue("defaultLeadTime.wesell"));
					}
					else
					{
						defaultLeadTime = Integer.parseInt(getConfigValue("defaultLeadTime"));
					}
					LOG.info("Setting lead time for LATAM in checkout summary ... ");
				}
				else if (super.getSiteUid().equalsIgnoreCase(PERSONALCARE_EMEA))
				{
					defaultLeadTime = Integer.parseInt(getConfigValue("defaultLeadTimeEMEA"));
					LOG.info("Setting lead time for EMEA in checkout summary ... ");
				}
				if (null != shippingPoint && null != soldToAddressId)
				{
					leadTime = energizerB2BCheckoutFlowFacade.getLeadTimeData(shippingPoint, soldToAddressId);
					if (leadTime > 0)
					{
						energizerB2BCheckoutFlowFacade.setLeadTime(leadTime);
					}
					else
					{
						energizerB2BCheckoutFlowFacade.setLeadTime(defaultLeadTime);
					}
				}
				/* } */
			}
			else
			{
				LOG.info("address : null");
				getCheckoutFlowFacade().setDeliveryAddress(null);
			}
			LOG.info("after address");

			//final CartData cartData = getCheckoutFlowFacade().getCheckoutCart();
			final CartData cartData = energizerB2BCheckoutFlowFacade.getCheckoutCart();

			LOG.info("Cart data retrieved !");

			if (cartData.getEntries() != null && !cartData.getEntries().isEmpty())
			{
				boolean flag = false;
				String productWithCmirInActive = " ";
				for (final OrderEntryData entry : cartData.getEntries())
				{
					final String productCode = entry.getProduct().getCode();
					final ProductData product = productFacade.getProductForCodeAndOptions(productCode,
							Arrays.asList(ProductOption.BASIC, ProductOption.PRICE));
					if (entry.getProduct().isIsActive() == true)
					{
						entry.setProduct(product);
					}
					else
					{
						productWithCmirInActive += entry.getProduct().getErpMaterialID() + "  ";
						flag = true;

					}

				}
				if (flag == true)
				{
					GlobalMessages.addMessage(model, "accErrorMsgs", "cart.cmirinactive", new Object[]
					{ productWithCmirInActive });
					return FORWARD_PREFIX + "/cart";
				}
			}

			// Try to set default delivery address and delivery mode
			//			if (cartData.getPaymentType() == null)
			//			{
			getCheckoutFlowFacade().setPaymentTypeSelectedForCheckout(CheckoutPaymentType.ACCOUNT.getCode());
			//			}

			LOG.info("Setting model attributes ..");

			model.addAttribute("symbol", storeSessionFacade.getDefaultCurrency().getSymbol());
			model.addAttribute("cartData", cartData);
			model.addAttribute("allItems", cartData.getEntries());
			model.addAttribute("deliveryAddress", cartData.getDeliveryAddress());
			model.addAttribute("deliveryMode", cartData.getDeliveryMode());
			model.addAttribute("paymentInfo", cartData.getPaymentInfo());
			model.addAttribute("costCenter", cartData.getCostCenter());
			model.addAttribute("quoteText", new B2BCommentData());
			// TODO:Make configuration hmc driven than hardcoding in controllers
			model.addAttribute("nDays", getNumberRange(1, 30));
			model.addAttribute("nthDayOfMonth", getNumberRange(1, 31));
			model.addAttribute("nthWeek", getNumberRange(1, 12));
			model.addAttribute("poNumberPattern", PONUMBER_PATTERN);

			model.addAttribute(new AddressForm());
			model.addAttribute(new PaymentDetailsForm());
			if (!model.containsAttribute("placeOrderForm"))
			{
				final PlaceOrderForm placeOrderForm = new PlaceOrderForm();
				// TODO: Make setting of default recurrence enum value hmc driven rather hard coding in controller
				placeOrderForm.setReplenishmentRecurrence(B2BReplenishmentRecurrenceEnum.MONTHLY);
				placeOrderForm.setnDays("14");
				final List<DayOfWeek> daysOfWeek = new ArrayList<DayOfWeek>();
				daysOfWeek.add(DayOfWeek.MONDAY);
				placeOrderForm.setnDaysOfWeek(daysOfWeek);
				model.addAttribute("placeOrderForm", placeOrderForm);
			}
			storeCmsPageInModel(model, getContentPageForLabelOrId(SINGLE_STEP_CHECKOUT_SUMMARY_CMS_PAGE));
			setUpMetaDataForContentPage(model, getContentPageForLabelOrId(SINGLE_STEP_CHECKOUT_SUMMARY_CMS_PAGE));
			model.addAttribute("metaRobots", "no-index,no-follow");
		}
		catch (final Exception e)
		{
			LOG.error("Exception occured while feteching checkout summary ::: " + e);
			GlobalMessages.addErrorMessage(model, "form.single.checkout.summary.error");
			return REDIRECT_PREFIX + "/checkout/single/summary";
		}

		return ControllerConstants.Views.Pages.SingleStepCheckout.CheckoutSummaryPage;
	}

	@ResponseBody
	@RequestMapping(value = "/summary/getCheckoutCart.json", method =
	{ RequestMethod.GET, RequestMethod.POST })
	@RequireHardLogIn
	public CartData getCheckoutCart()
	{
		final CartData cartData = energizerB2BCheckoutFlowFacade.getCheckoutCart();
		return cartData;
	}

	@ResponseBody
	@RequestMapping(value = "/summary/getCostCenters.json", method =
	{ RequestMethod.GET, RequestMethod.POST })
	@RequireHardLogIn
	public List<? extends B2BCostCenterData> getCostCenters()
	{
		return getVisibleActiveCostCenters();
	}

	@ResponseBody
	@RequestMapping(value = "/summary/getDeliveryAddresses.json", method =
	{ RequestMethod.GET, RequestMethod.POST })
	@RequireHardLogIn
	public List<? extends AddressData> getDeliveryAddresses() throws Exception
	{

		List<AddressData> energizerDeliveryAddresses = new ArrayList<AddressData>();
		energizerDeliveryAddresses = energizerB2BCheckoutFlowFacade.getEnergizerDeliveryAddresses();
		//final CartData cartData =energizerB2BCheckoutFlowFacade.getCheckoutCart();
		final List<String> soldToAddressIds = energizerB2BCheckoutFlowFacade.getsoldToAddressIds(getShippingPoint());

		final List<AddressData> energizerAddresses = new ArrayList<AddressData>();
		for (final String soldToAddressId : soldToAddressIds)
		{
			for (final AddressData address : energizerDeliveryAddresses)
			{
				if (soldToAddressId.equalsIgnoreCase(address.getErpAddressId()))
				{
					energizerAddresses.add(address);
					break;
				}
			}

		}

		return energizerAddresses;
	}

	@ResponseBody
	@RequestMapping(value = "/summary/setDefaultAddress.json", method =
	{ RequestMethod.GET, RequestMethod.POST })
	@RequireHardLogIn
	public List<? extends AddressData> setDefaultAddress(@RequestParam(value = "addressId")
	final String addressId) throws Exception
	{
		getUserFacade().setDefaultAddress(getUserFacade().getAddressForCode(addressId));
		return getDeliveryAddresses();
	}

	@ResponseBody
	@RequestMapping(value = "/summary/setDeliveryAddress.json", method =
	{ RequestMethod.GET, RequestMethod.POST })
	@RequireHardLogIn
	public CartData setDeliveryAddress(@RequestParam(value = "addressId")
	final String addressId) throws ParseException
	{
		try
		{
			AddressData addressData = null;

			final List<AddressData> deliveryAddresses = energizerB2BCheckoutFlowFacade.getEnergizerDeliveryAddresses();
			for (final AddressData deliveryAddress : deliveryAddresses)
			{
				if (deliveryAddress.getId().equals(addressId))
				{
					addressData = deliveryAddress;
					break;
				}
			}

			// Remove Lead Time Dependency for LATAM
			if (super.getSiteUid().equalsIgnoreCase(PERSONALCARE))
			{
				getCheckoutFlowFacade().setDeliveryAddress(addressData);

				// Added by Soma to add lead time for LATAM. Will remove once the SAP changes are done - START
				int leadTime = 0;

				final String shippingPoint = getShippingPoint();
				final String soldToAddressId = addressData.getErpAddressId();
				int defaultLeadTime = 0;

				// Added code changes for WeSell Implementation
				if (isSalesRepUserLogin())
				{
					defaultLeadTime = Integer.parseInt(getConfigValue("defaultLeadTime.wesell"));
				}
				else
				{
					defaultLeadTime = Integer.parseInt(getConfigValue("defaultLeadTime"));
				}

				if (shippingPoint != null && soldToAddressId != null)
				{
					leadTime = energizerB2BCheckoutFlowFacade.getLeadTimeData(shippingPoint, soldToAddressId);
					if (leadTime > 0)
					{
						energizerB2BCheckoutFlowFacade.setLeadTime(leadTime);
					}
					else
					{
						energizerB2BCheckoutFlowFacade.setLeadTime(defaultLeadTime);
					}
				}
				// Added by Soma to add lead time for LATAM. Will remove once the SAP changes are done - END

			}
			else
			{
				// Calculate Lead Time for EMEA
				int leadTime = 0;
				if (addressData != null && getCheckoutFlowFacade().setDeliveryAddress(addressData))
				{
					//ShippingPoint should fetch from cartdata but its not availble so fetching it by code. //
					//final String shippingPointId = cartFacade.getSessionCart().getShippingPoint();

					final String shippingPoint = getShippingPoint();
					final String soldToAddressId = addressData.getErpAddressId();
					final int defaultLeadTime = Integer.parseInt(getConfigValue("defaultLeadTimeEMEA"));
					if (shippingPoint != null && soldToAddressId != null)
					{
						leadTime = energizerB2BCheckoutFlowFacade.getLeadTimeData(shippingPoint, soldToAddressId);
						if (leadTime > 0)
						{
							energizerB2BCheckoutFlowFacade.setLeadTime(leadTime);
						}
						else
						{
							energizerB2BCheckoutFlowFacade.setLeadTime(defaultLeadTime);
						}
					}
				}
			}
		}
		catch (final Exception e)
		{
			LOG.error("Exception occured while setting delivery adress.....");
			e.printStackTrace();
		}
		return energizerB2BCheckoutFlowFacade.getCheckoutCart();
	}

	@ResponseBody
	@RequestMapping(value = "/summary/getDeliveryModes.json", method =
	{ RequestMethod.GET, RequestMethod.POST })
	@RequireHardLogIn
	public List<? extends DeliveryModeData> getDeliveryModes()
	{
		final List<? extends DeliveryModeData> deliveryModes = getCheckoutFlowFacade().getSupportedDeliveryModes();
		return deliveryModes == null ? Collections.<ZoneDeliveryModeData> emptyList() : deliveryModes;
	}

	@ResponseBody
	@RequestMapping(value = "/summary/setDeliveryMode.json", method =
	{ RequestMethod.GET, RequestMethod.POST })
	@RequireHardLogIn
	public CartData setDeliveryMode(@RequestParam(value = "modeCode")
	final String modeCode)
	{
		if (getCheckoutFlowFacade().setDeliveryMode(modeCode))
		{
			final CartData cartData = energizerB2BCheckoutFlowFacade.getCheckoutCart();

			return cartData;
		}
		else
		{
			return null;
		}
	}

	@RequestMapping(value = "/summary/getDeliveryAddressForm.json", method =
	{ RequestMethod.GET, RequestMethod.POST })
	@RequireHardLogIn
	public String getDeliveryAddressForm(final Model model, @RequestParam(value = "addressId")
	final String addressId, @RequestParam(value = "createUpdateStatus")
	final String createUpdateStatus)
	{
		AddressData addressData = null;
		if (addressId != null && !addressId.isEmpty())
		{
			addressData = getCheckoutFlowFacade().getDeliveryAddressForCode(addressId);
		}

		final AddressForm addressForm = new AddressForm();

		final boolean hasAddressData = addressData != null;
		if (hasAddressData)
		{
			addressForm.setAddressId(addressData.getId());
			addressForm.setTitleCode(addressData.getTitleCode());
			addressForm.setFirstName(addressData.getFirstName());
			addressForm.setLastName(addressData.getLastName());
			addressForm.setLine1(addressData.getLine1());
			addressForm.setLine2(addressData.getLine2());
			addressForm.setTownCity(addressData.getTown());
			addressForm.setPostcode(addressData.getPostalCode());
			addressForm.setCountryIso(addressData.getCountry().getIsocode());
			addressForm.setShippingAddress(Boolean.valueOf(addressData.isShippingAddress()));
			addressForm.setBillingAddress(Boolean.valueOf(addressData.isBillingAddress()));
		}

		model.addAttribute("edit", Boolean.valueOf(hasAddressData));
		model.addAttribute("noAddresses", Boolean.valueOf(getUserFacade().isAddressBookEmpty()));

		model.addAttribute(addressForm);
		model.addAttribute("createUpdateStatus", createUpdateStatus);

		// Work out if the address form should be displayed based on the payment type
		final B2BPaymentTypeData paymentType = getCheckoutFlowFacade().getCheckoutCart().getPaymentType();
		final boolean payOnAccount = paymentType != null && CheckoutPaymentType.ACCOUNT.getCode().equals(paymentType.getCode());
		model.addAttribute("showAddressForm", Boolean.valueOf(!payOnAccount));

		return ControllerConstants.Views.Fragments.SingleStepCheckout.DeliveryAddressFormPopup;
	}

	@RequestMapping(value = "/summary/createUpdateDeliveryAddress.json", method = RequestMethod.POST)
	@RequireHardLogIn
	public String createUpdateDeliveryAddress(final Model model, @Valid
	final AddressForm form, final BindingResult bindingResult)
	{
		final AddressData addressData = new AddressData();
		try
		{
			if (bindingResult.hasErrors())
			{
				model.addAttribute("edit", Boolean.valueOf(StringUtils.isNotBlank(form.getAddressId())));
				// Work out if the address form should be displayed based on the payment type
				final B2BPaymentTypeData paymentType = getCheckoutFlowFacade().getCheckoutCart().getPaymentType();
				final boolean payOnAccount = paymentType != null
						&& CheckoutPaymentType.ACCOUNT.getCode().equals(paymentType.getCode());
				model.addAttribute("showAddressForm", Boolean.valueOf(!payOnAccount));

				return ControllerConstants.Views.Fragments.SingleStepCheckout.DeliveryAddressFormPopup;
			}

			// create delivery address and set it on cart

			addressData.setId(form.getAddressId());
			addressData.setTitleCode(form.getTitleCode());
			addressData.setFirstName(form.getFirstName());
			addressData.setLastName(form.getLastName());
			addressData.setLine1(form.getLine1());
			addressData.setLine2(form.getLine2());
			addressData.setTown(form.getTownCity());
			addressData.setPostalCode(form.getPostcode());
			addressData.setCountry(getI18NFacade().getCountryForIsocode(form.getCountryIso()));
			addressData.setShippingAddress(
					Boolean.TRUE.equals(form.getShippingAddress()) || Boolean.TRUE.equals(form.getSaveInAddressBook()));

			addressData.setVisibleInAddressBook(
					Boolean.TRUE.equals(form.getSaveInAddressBook()) || StringUtils.isNotBlank(form.getAddressId()));
			addressData.setDefaultAddress(Boolean.TRUE.equals(form.getDefaultAddress()));

			if (StringUtils.isBlank(form.getAddressId()))
			{
				getUserFacade().addAddress(addressData);
			}
			else
			{
				getUserFacade().editAddress(addressData);
			}

			getCheckoutFlowFacade().setDeliveryAddress(addressData);

			if (getCheckoutFlowFacade().getCheckoutCart().getDeliveryMode() == null)
			{
				getCheckoutFlowFacade().setDeliveryModeIfAvailable();
			}

			model.addAttribute("createUpdateStatus", "Success");
			model.addAttribute("addressId", addressData.getId());
		}
		catch (final Exception e)
		{
			LOG.error("Exception occured while creating or updating delivery adress....");
			e.printStackTrace();
		}
		return REDIRECT_PREFIX + "/checkout/single/summary/getDeliveryAddressForm.json?addressId=" + addressData.getId()
				+ "&createUpdateStatus=Success";
	}


	@ResponseBody
	@RequestMapping(value = "/summary/setCostCenter.json", method = RequestMethod.POST)
	@RequireHardLogIn
	public CartData setCostCenter(@RequestParam(value = "costCenterId")
	final String costCenterId) throws Exception
	{
		// remove the delivery address;

		//getCheckoutFlowFacade().removeDeliveryAddress();

		if (getDeliveryAddressesForB2Bunit().size() != 1)
		{
			getCheckoutFlowFacade().removeDeliveryAddress();
		}

		getCheckoutFlowFacade().removeDeliveryMode();
		final CartData cartData = getCheckoutFlowFacade().setCostCenterForCart(costCenterId,
				this.getCheckoutFlowFacade().getCheckoutCart().getCode());

		return cartData;
	}


	@ResponseBody
	@RequestMapping(value = "/summary/setDeliveryDate.json", method = RequestMethod.POST)
	@RequireHardLogIn
	public CartData setDeliveryDate(@RequestParam(value = "deliveryDate")
	final String deliveryDate) throws ParseException, Exception
	{
		LOG.info("Delivery Date set as ::: " + deliveryDate);

		final CartData cartData = energizerB2BCheckoutFlowFacade.setDeliveryDate(deliveryDate,
				energizerB2BCheckoutFlowFacade.getCheckoutCart().getCode());
		if (getDeliveryAddressesForB2Bunit().size() == 1)
		{
			final AddressData addressData = getDeliveryAddressesForB2Bunit().get(0);
			cartData.setDeliveryAddress(addressData);
		}
		return cartData;
	}



	@ResponseBody
	@RequestMapping(value = "/summary/updateCostCenter.json", method = RequestMethod.POST)
	@RequireHardLogIn
	public CartData updateCostCenterForCart(@RequestParam(value = "costCenterId")
	final String costCenterId)
	{
		final CartData cartData = getCheckoutFlowFacade().setCostCenterForCart(costCenterId,
				energizerB2BCheckoutFlowFacade.getCheckoutCart().getCode());

		return cartData;
	}

	@ResponseBody
	@RequestMapping(value = "/summary/getSavedCards.json", method =
	{ RequestMethod.GET, RequestMethod.POST })
	@RequireHardLogIn
	public List<CCPaymentInfoData> getSavedCards()
	{
		final List<CCPaymentInfoData> paymentInfos = getUserFacade().getCCPaymentInfos(true);
		return paymentInfos == null ? Collections.<CCPaymentInfoData> emptyList() : paymentInfos;
	}

	@ResponseBody
	@RequestMapping(value = "/summary/setPaymentDetails.json", method = RequestMethod.POST)
	@RequireHardLogIn
	public CartData setPaymentDetails(@RequestParam(value = "paymentId")
	final String paymentId)
	{
		if (StringUtils.isNotBlank(paymentId) && getCheckoutFlowFacade().setPaymentDetails(paymentId))
		{
			final CartData cartData = energizerB2BCheckoutFlowFacade.getCheckoutCart();

			return cartData;
		}

		return null;
	}



	@ResponseBody
	@RequestMapping(value = "/summary/setPaymentType.json", method = RequestMethod.POST)
	@RequireHardLogIn
	public CartData setPaymentType(@RequestParam(value = "paymentType")
	final String paymentType)
	{
		getCheckoutFlowFacade().setPaymentTypeSelectedForCheckout(paymentType);
		getCheckoutFlowFacade().removeDeliveryAddress();
		getCheckoutFlowFacade().removeDeliveryMode();
		getCheckoutFlowFacade().setCostCenterForCart("", getCheckoutFlowFacade().getCheckoutCart().getCode());

		final CartData cartData = energizerB2BCheckoutFlowFacade.getCheckoutCart();

		return cartData;
	}


	@ResponseBody
	@RequestMapping(value = "/summary/setPurchaseOrderNumber.json", method = RequestMethod.POST)
	@RequireHardLogIn
	public CartData setPurchaseOrderNumber(@RequestParam(value = "purchaseOrderNumber")
	final String purchaseOrderNumber) throws Exception
	{
		getCheckoutFlowFacade().setPurchaseOrderNumber(purchaseOrderNumber);
		LOG.info("Purchase order no set in cart " + purchaseOrderNumber);


		if (getDeliveryAddressesForB2Bunit().size() == 1)
		{
			final AddressData addressData = getDeliveryAddressesForB2Bunit().get(0);
			energizerB2BCheckoutFlowFacade.setSingleDeliveryAddress(addressData);

		}

		final CartData cartData = energizerB2BCheckoutFlowFacade.getCheckoutCart();


		return cartData;
	}

	@RequestMapping(value = "/summary/getPaymentDetailsForm.json", method =
	{ RequestMethod.GET, RequestMethod.POST })
	@RequireHardLogIn
	public String getPaymentDetailsForm(final Model model, @RequestParam(value = "paymentId")
	final String paymentId, @RequestParam(value = "createUpdateStatus")
	final String createUpdateStatus)
	{
		CCPaymentInfoData paymentInfoData = null;
		if (StringUtils.isNotBlank(paymentId))
		{
			paymentInfoData = getUserFacade().getCCPaymentInfoForCode(paymentId);
		}

		final PaymentDetailsForm paymentDetailsForm = new PaymentDetailsForm();

		if (paymentInfoData != null)
		{
			paymentDetailsForm.setPaymentId(paymentInfoData.getId());
			paymentDetailsForm.setCardTypeCode(paymentInfoData.getCardType());
			paymentDetailsForm.setNameOnCard(paymentInfoData.getAccountHolderName());
			paymentDetailsForm.setCardNumber(paymentInfoData.getCardNumber());
			paymentDetailsForm.setStartMonth(paymentInfoData.getStartMonth());
			paymentDetailsForm.setStartYear(paymentInfoData.getStartYear());
			paymentDetailsForm.setExpiryMonth(paymentInfoData.getExpiryMonth());
			paymentDetailsForm.setExpiryYear(paymentInfoData.getExpiryYear());
			paymentDetailsForm.setSaveInAccount(Boolean.valueOf(paymentInfoData.isSaved()));
			paymentDetailsForm.setIssueNumber(paymentInfoData.getIssueNumber());

			final AddressForm addressForm = new AddressForm();
			final AddressData addressData = paymentInfoData.getBillingAddress();
			if (addressData != null)
			{
				addressForm.setAddressId(addressData.getId());
				addressForm.setTitleCode(addressData.getTitleCode());
				addressForm.setFirstName(addressData.getFirstName());
				addressForm.setLastName(addressData.getLastName());
				addressForm.setLine1(addressData.getLine1());
				addressForm.setLine2(addressData.getLine2());
				addressForm.setTownCity(addressData.getTown());
				addressForm.setPostcode(addressData.getPostalCode());
				addressForm.setCountryIso(addressData.getCountry().getIsocode());
				addressForm.setShippingAddress(Boolean.valueOf(addressData.isShippingAddress()));
				addressForm.setBillingAddress(Boolean.valueOf(addressData.isBillingAddress()));
			}

			paymentDetailsForm.setBillingAddress(addressForm);
		}

		model.addAttribute("edit", Boolean.valueOf(paymentInfoData != null));
		model.addAttribute("paymentInfoData", getUserFacade().getCCPaymentInfos(true));
		model.addAttribute(paymentDetailsForm);
		model.addAttribute("createUpdateStatus", createUpdateStatus);
		return ControllerConstants.Views.Fragments.SingleStepCheckout.PaymentDetailsFormPopup;
	}

	@RequestMapping(value = "/summary/createUpdatePaymentDetails.json", method = RequestMethod.POST)
	@RequireHardLogIn
	public String createUpdatePaymentDetails(final Model model, @Valid
	final PaymentDetailsForm form, final BindingResult bindingResult)
	{
		paymentDetailsValidator.validate(form, bindingResult);

		final boolean editMode = StringUtils.isNotBlank(form.getPaymentId());

		if (bindingResult.hasErrors())
		{
			model.addAttribute("edit", Boolean.valueOf(editMode));

			return ControllerConstants.Views.Fragments.SingleStepCheckout.PaymentDetailsFormPopup;
		}

		final CCPaymentInfoData paymentInfoData = new CCPaymentInfoData();
		paymentInfoData.setId(form.getPaymentId());
		paymentInfoData.setCardType(form.getCardTypeCode());
		paymentInfoData.setAccountHolderName(form.getNameOnCard());
		paymentInfoData.setCardNumber(form.getCardNumber());
		paymentInfoData.setStartMonth(form.getStartMonth());
		paymentInfoData.setStartYear(form.getStartYear());
		paymentInfoData.setExpiryMonth(form.getExpiryMonth());
		paymentInfoData.setExpiryYear(form.getExpiryYear());
		paymentInfoData.setSaved(Boolean.TRUE.equals(form.getSaveInAccount()));
		paymentInfoData.setIssueNumber(form.getIssueNumber());

		final AddressData addressData;
		if (!editMode && Boolean.FALSE.equals(form.getNewBillingAddress()))
		{
			addressData = getCheckoutCart().getDeliveryAddress();
			if (addressData == null)
			{
				GlobalMessages.addErrorMessage(model, "checkout.paymentMethod.createSubscription.billingAddress.noneSelected");

				model.addAttribute("edit", Boolean.valueOf(editMode));
				return ControllerConstants.Views.Fragments.SingleStepCheckout.PaymentDetailsFormPopup;
			}

			addressData.setBillingAddress(true); // mark this as billing address
		}
		else
		{
			final AddressForm addressForm = form.getBillingAddress();

			addressData = new AddressData();
			if (addressForm != null)
			{
				addressData.setId(addressForm.getAddressId());
				addressData.setTitleCode(addressForm.getTitleCode());
				addressData.setFirstName(addressForm.getFirstName());
				addressData.setLastName(addressForm.getLastName());
				addressData.setLine1(addressForm.getLine1());
				addressData.setLine2(addressForm.getLine2());
				addressData.setTown(addressForm.getTownCity());
				addressData.setPostalCode(addressForm.getPostcode());
				addressData.setCountry(getI18NFacade().getCountryForIsocode(addressForm.getCountryIso()));
				addressData.setShippingAddress(Boolean.TRUE.equals(addressForm.getShippingAddress()));
				addressData.setBillingAddress(Boolean.TRUE.equals(addressForm.getBillingAddress()));
			}
		}

		paymentInfoData.setBillingAddress(addressData);

		final CCPaymentInfoData newPaymentSubscription = getCheckoutFlowFacade().createPaymentSubscription(paymentInfoData);
		if (newPaymentSubscription != null && StringUtils.isNotBlank(newPaymentSubscription.getSubscriptionId()))
		{
			if (Boolean.TRUE.equals(form.getSaveInAccount()) && getUserFacade().getCCPaymentInfos(true).size() <= 1)
			{
				getUserFacade().setDefaultPaymentInfo(newPaymentSubscription);
			}
			getCheckoutFlowFacade().setPaymentDetails(newPaymentSubscription.getId());
		}
		else
		{
			GlobalMessages.addErrorMessage(model, "checkout.paymentMethod.createSubscription.failed");

			model.addAttribute("edit", Boolean.valueOf(editMode));
			return ControllerConstants.Views.Fragments.SingleStepCheckout.PaymentDetailsFormPopup;
		}

		model.addAttribute("createUpdateStatus", "Success");
		model.addAttribute("paymentId", newPaymentSubscription.getId());

		return REDIRECT_PREFIX + "/checkout/single/summary/getPaymentDetailsForm.json?paymentId=" + paymentInfoData.getId()
				+ "&createUpdateStatus=Success";
	}

	@RequestMapping(value = "/termsAndConditions")
	@RequireHardLogIn
	public String getTermsAndConditions(final Model model) throws CMSItemNotFoundException
	{
		try
		{
			final ContentPageModel pageForRequest = getCmsPageService().getPageForLabel("/termsAndConditions");
			storeCmsPageInModel(model, pageForRequest);
			setUpMetaDataForContentPage(model, pageForRequest);
			model.addAttribute(WebConstants.BREADCRUMBS_KEY, contentPageBreadcrumbBuilder.getBreadcrumbs(pageForRequest));
		}
		catch (final Exception e)
		{
			LOG.error("Exception occured while feteching checkout summary ::: " + e);
			GlobalMessages.addErrorMessage(model, "form.single.checkout.summary.error");
			return REDIRECT_PREFIX + "/checkout/single/summary";
		}
		return ControllerConstants.Views.Fragments.Checkout.TermsAndConditionsPopup;
	}



	@RequestMapping(value = "/placeOrder")
	@RequireHardLogIn
	public String placeOrder(final Model model, @Valid
	final PlaceOrderForm placeOrderForm, final BindingResult bindingResult)
			throws CMSItemNotFoundException, InvalidCartException, ParseException
	{
		try
		{
			// validate the cart
			//final CartData cartData = getCheckoutFlowFacade().getCheckoutCart();
			final CartData cartData = energizerB2BCheckoutFlowFacade.getCheckoutCart();

			final boolean isAccountPaymentType = CheckoutPaymentType.ACCOUNT.getCode().equals(cartData.getPaymentType().getCode());
			final String securityCode = placeOrderForm.getSecurityCode();
			final boolean termsChecked = placeOrderForm.isTermsCheck();


			if (!isSalesRepUserLogin())
			{
				if (!termsChecked)
				{
					GlobalMessages.addErrorMessage(model, "checkout.error.terms.not.accepted");
				}
			}
			if (getDeliveryAddressesForB2Bunit().size() == 1)
			{
				final AddressData addressData = getDeliveryAddressesForB2Bunit().get(0);
				cartData.setDeliveryAddress(addressData);
			}
			if (validateOrderform(placeOrderForm, model, cartData))
			{
				placeOrderForm.setTermsCheck(false);
				model.addAttribute(placeOrderForm);
				return checkoutSummary(model);
			}

			if (!isAccountPaymentType && !getCheckoutFlowFacade().authorizePayment(securityCode))
			{
				return checkoutSummary(model);
			}

			// validate quote negotiation
			if (placeOrderForm.isNegotiateQuote())
			{
				if (StringUtils.isBlank(placeOrderForm.getQuoteRequestDescription()))
				{
					GlobalMessages.addErrorMessage(model, "checkout.error.noQuoteDescription");
					return checkoutSummary(model);
				}
				else
				{
					getCheckoutFlowFacade()
							.setQuoteRequestDescription(XSSFilterUtil.filter(placeOrderForm.getQuoteRequestDescription()));
				}
			}

			if (!isSalesRepUserLogin())
			{
				if (!termsChecked)
				{
					return checkoutSummary(model);
				}
			}

			// validate replenishment
			if (placeOrderForm.isReplenishmentOrder())
			{
				if (placeOrderForm.getReplenishmentStartDate() == null)
				{
					bindingResult.addError(new FieldError(placeOrderForm.getClass().getSimpleName(), "replenishmentStartDate", ""));
					GlobalMessages.addErrorMessage(model, "checkout.error.replenishment.noStartDate");
					return checkoutSummary(model);
				}
				if (B2BReplenishmentRecurrenceEnum.WEEKLY.equals(placeOrderForm.getReplenishmentRecurrence()))
				{
					if (CollectionUtils.isEmpty(placeOrderForm.getnDaysOfWeek()))
					{
						GlobalMessages.addErrorMessage(model, "checkout.error.replenishment.no.Frequency");
						return checkoutSummary(model);
					}
				}
				final TriggerData triggerData = new TriggerData();
				//populateTriggerDataFromPlaceOrderForm(placeOrderForm, triggerData);
				final ScheduledCartData scheduledCartData = getCheckoutFlowFacade().scheduleOrder(triggerData);
				return REDIRECT_PREFIX + "/checkout/replenishmentConfirmation/" + scheduledCartData.getJobCode();
			}
		}
		catch (final Exception e)
		{
			LOG.error("Exception occured while feteching checkout summary ::: " + e);
			GlobalMessages.addErrorMessage(model, "form.single.checkout.summary.error");
			return REDIRECT_PREFIX + "/checkout/single/simulateOrder";
		}

		final OrderData orderData;
		try
		{
			orderData = energizerB2BCheckoutFlowFacade.placeOrder();

			if (orderData == null)
			{

				LOG.info("There is no cart model in session, returing back to cart page");
				GlobalMessages.addErrorMessage(model, "checkout.placeOrder.failed");
				//placeOrderForm.setNegotiateQuote(true);

				model.addAttribute(placeOrderForm);
				return checkoutSummary(model);
			}
		}
		catch (final Exception e)
		{
			e.printStackTrace();
			GlobalMessages.addErrorMessage(model, "checkout.placeOrder.failed");
			placeOrderForm.setNegotiateQuote(true);
			model.addAttribute(placeOrderForm);
			return checkoutSummary(model);
		}


		if (placeOrderForm.isNegotiateQuote())
		{
			return REDIRECT_PREFIX + "/checkout/quoteOrderConfirmation/" + orderData.getCode();
		}
		else
		{
			cartService.removeSessionCart();
			return REDIRECT_PREFIX + "/checkout/orderConfirmation/" + orderData.getCode();

		}
	}

	@RequestMapping(value = "/simulateOrder")
	@RequireHardLogIn
	public String simulateOrder(final Model model, @Valid
	final PlaceOrderForm placeOrderForm, final BindingResult bindingResult)
			throws CMSItemNotFoundException, ParseException, AddressException, ValidationException
	{
		final Long srartTime = System.currentTimeMillis();
		LOG.info("Simulation invocation started " + srartTime);
		try
		{
			//CartData cartData = getCheckoutFlowFacade().getCheckoutCart();
			CartData cartData = energizerB2BCheckoutFlowFacade.getCheckoutCart();

			//no items in the cart, then redirect to "Card" page. also when back button is pressed.
			if (!hasItemsInCart())
			{
				return REDIRECT_PREFIX + "/checkout/single/summary";
			}
			if (getDeliveryAddressesForB2Bunit().size() == 1)
			{
				final AddressData addressData = getDeliveryAddressesForB2Bunit().get(0);
				cartData.setDeliveryAddress(addressData);
				if (addressData.getErpAddressId() != null
						&& defaultEnergizerAddressFacade.fetchAddress(addressData.getErpAddressId()).get(0) != null)
				{
					final CartModel cartModel = cartService.getSessionCart();
					cartModel.setDeliveryAddress(defaultEnergizerAddressFacade.fetchAddress(addressData.getErpAddressId()).get(0));
					modelService.save(cartModel);
				}
			}

			// ONLY for EMEA, NOT for LATAM
			/*
			 * if (super.getSiteUid().equalsIgnoreCase(PERSONALCARE_EMEA)) {
			 */
			LOG.info("Retrieving requested delivery date from checkout flow facade ::: "
					+ energizerB2BCheckoutFlowFacade.getCheckoutCart().getRequestedDeliveryDate());
			cartData.setRequestedDeliveryDate(energizerB2BCheckoutFlowFacade.getCheckoutCart().getRequestedDeliveryDate());
			/* } */

			final boolean termsChecked = placeOrderForm.isTermsCheck();

			// Setting Order Comments to cartData - START
			final String orderComments = placeOrderForm.getOrderComments();
			LOG.info("orderComments ::: " + orderComments);
			cartData.setOrderComments(orderComments);
			sessionService.setAttribute("orderComments", orderComments);
			// Setting Order Comments to cartData - END

			if (!isSalesRepUserLogin())
			{

				if (!termsChecked)
				{
					GlobalMessages.addErrorMessage(model, "checkout.error.terms.not.accepted");
					return checkoutSummary(model);
				}
			}
			for (final OrderEntryData entry : cartData.getEntries())
			{
				final UpdateQuantityForm uqf = new UpdateQuantityForm();
				uqf.setQuantity(entry.getQuantity());
				model.addAttribute("updateQuantityForm" + entry.getEntryNumber(), uqf);
			}
			try
			{
				cartData = energizerB2BCheckoutFlowFacade.simulateOrder(cartData, EnergizerCoreConstants.CHECKOUT);

				// Estimated Delivery Date logic for LATAM, NOT for EMEA
				if (getSiteUid().equalsIgnoreCase(PERSONALCARE))
				{

					LOG.info(
							"Requested Delivery Date from Simulate Order, we are going to ignore this date from SAP response for LATAM ::: "
									+ cartData.getRequestedDeliveryDate());

					//Removing Lead Time for LATAM & EMEA. Estimated delivery date is returned from simulate response.
					LOG.info("Retrieving requested delivery date from checkout flow facade & setting it to cart data again ::: "
							+ energizerB2BCheckoutFlowFacade.getCheckoutCart().getRequestedDeliveryDate());
					cartData.setRequestedDeliveryDate(energizerB2BCheckoutFlowFacade.getCheckoutCart().getRequestedDeliveryDate());

					LOG.info("cartData.getRequestedDeliveryDate() based on lead time ::: " + cartData.getRequestedDeliveryDate());
				}
			}
			catch (final AddressException e)
			{
				LOG.info(e.getMessage());
				e.printStackTrace();
				GlobalMessages.addErrorMessage(model, "checkout.simulateOrder.soldTo.shipTo.failed");
				model.addAttribute(placeOrderForm);
				return checkoutSummary(model);
			}
			catch (final ValidationException e)
			{

				GlobalMessages.addErrorMessage(model, "order.comment.size.exceeded");
				model.addAttribute(placeOrderForm);
				return checkoutSummary(model);
			}
			catch (final Exception e)
			{
				LOG.info(e.getMessage());
				e.printStackTrace();

				final String orderSimulateErrorMessage = sessionService.getAttribute("orderSimulateErrorMessage");
				if (null != orderSimulateErrorMessage && !StringUtils.isEmpty(orderSimulateErrorMessage.toString()))
				{
					//GlobalMessages.addErrorMessage(model, "checkout.simulateOrder.failed.error.message");
					GlobalMessages.addMessage(model, "businessRuleError", "checkout.simulateOrder.failed.simulate.error.message",
							new Object[]
							{ orderSimulateErrorMessage });

				}
				else
				{
					GlobalMessages.addErrorMessage(model, "checkout.simulateOrder.failed");
				}
				//GlobalMessages.addErrorMessage(model, "checkout.simulateOrder.failed");
				model.addAttribute(placeOrderForm);
				return checkoutSummary(model);
			}

			if (validateOrderform(placeOrderForm, model, cartData))
			{
				placeOrderForm.setTermsCheck(false);
				model.addAttribute(placeOrderForm);
				return checkoutSummary(model);
			}

			model.addAttribute(new AddressForm());

			storeCmsPageInModel(model, getContentPageForLabelOrId(SINGLE_STEP_SIMULATE_CHECKOUT_SUMMARY_CMS_PAGE));
			setUpMetaDataForContentPage(model, getContentPageForLabelOrId(SINGLE_STEP_SIMULATE_CHECKOUT_SUMMARY_CMS_PAGE));

			energizerB2BCheckoutFlowFacade.updateSessionCart(cartData);
			//cartData = getCheckoutFlowFacade().getCheckoutCart();
			cartData = energizerB2BCheckoutFlowFacade.getCheckoutCart();
			model.addAttribute("cartData1", cartData);
			if (null != cartData.getSalesRepCurrencyIsoCode()
					&& ((boolean) getSessionService().getAttribute("isSalesRepUserLoggedIn")))
			{
				model.addAttribute("symbol", cartData.getSalesRepCurrencyIsoCode());

			}
			else
			{
				model.addAttribute("symbol", storeSessionFacade.getDefaultCurrency().getSymbol());
			}
			model.addAttribute("showTaxForSalesRep", true);

			final Long endTime = System.currentTimeMillis();
			LOG.info("Simulation invocation ended " + endTime);
			LOG.info("Simulation total time in Sec" + (endTime - srartTime) / 1000);
			return ControllerConstants.Views.Pages.SingleStepCheckout.SimulateCheckoutSummaryPage;
		}
		/*-catch (final HttpClientErrorException clientException)
		{
			GlobalMessages.addErrorMessage(model, "checkout.simulateOrder.failed");
			model.addAttribute(placeOrderForm);
			return checkoutSummary(model);
		}
		catch (final Exception e)
		{
			GlobalMessages.addErrorMessage(model, "checkout.simulateOrder.failed");
			model.addAttribute(placeOrderForm);
			return checkoutSummary(model);
		}*/
		catch (final Exception e)
		{
			if ((boolean) getSessionService().getAttribute("isSalesRepUserLoggedIn"))
			{
				if (e instanceof HttpClientErrorException)
				{
					LOG.error("Order simulation failed in SAP because of bad format of xml ... ");
					GlobalMessages.addErrorMessage(model, "Order simulation failed in SAP because of bad format of xml");
				}
				else if (e instanceof RestClientException)
				{
					LOG.error("Order simulation failed in SAP because of " + e);
					GlobalMessages.addErrorMessage(model, "Order simulation failed in SAP because of connectivity issues");
				}
				else if (e instanceof JAXBException)
				{
					LOG.error("Order simulation failed in SAP because of improper xml/data " + e);
					GlobalMessages.addErrorMessage(model, "Order simulation failed in SAP because of improper xml/data");
					e.printStackTrace();
				}
				else
				{
					LOG.error("Exception occured during simulate order ::: " + e);
				}
			}
			GlobalMessages.addErrorMessage(model, "checkout.simulateOrder.failed");
			model.addAttribute(placeOrderForm);
			return checkoutSummary(model);
		}
	}

	protected boolean validateOrderform(final PlaceOrderForm placeOrderForm, final Model model, final CartData cartData)
	{
		boolean invalid = false;
		try
		{
			final boolean accountPaymentType = CheckoutPaymentType.ACCOUNT.getCode().equals(cartData.getPaymentType().getCode());
			//LOG.info("Order simulation : " + cartData.getPaymentType().getCode());
			final String securityCode = placeOrderForm.getSecurityCode();
			if (cartData.getDeliveryAddress() == null)
			{
				GlobalMessages.addErrorMessage(model, "checkout.deliveryAddress.notSelected");
				invalid = true;
			}
			if (!accountPaymentType && cartData.getPaymentInfo() == null)
			{
				GlobalMessages.addErrorMessage(model, "checkout.paymentMethod.notSelected");
				invalid = true;
			}
			else if (!accountPaymentType && StringUtils.isBlank(securityCode))
			{
				GlobalMessages.addErrorMessage(model, "checkout.paymentMethod.noSecurityCode");
				invalid = true;
			}
			if (cartData.getPurchaseOrderNumber() == null && StringUtils.isEmpty(cartData.getPurchaseOrderNumber()))
			{
				GlobalMessages.addErrorMessage(model, "checkout.purchaseNo.notempty");
				invalid = true;
			}
		}
		catch (final Exception e)
		{
			LOG.error("Exception occured while validating place order form...." + e);
			throw e;
		}
		return invalid;

	}



	@RequestMapping(value = "/summary/reorder", method =
	{ RequestMethod.PUT, RequestMethod.POST })
	@RequireHardLogIn
	public String reorder(@RequestParam(value = "orderCode")
	final String orderCode, final RedirectAttributes redirectModel, final Model model)
			throws CMSItemNotFoundException, InvalidCartException, ParseException, CommerceCartModificationException
	{
		try
		{
			// create a cart from the order and set it as session cart.
			getCheckoutFlowFacade().createCartFromOrder(orderCode);

			// validate for stock and availability
			//final List<? extends CommerceCartModification> cartModifications = getCheckoutFlowFacade().validateSessionCart();
			final CartModel cartModel = energizerB2BCheckoutFlowFacade.getSessionCart();

			// Added for Delivery Notes feature for LATAM - Remove existing files while reorder
			cartModel.setDeliveryNoteFiles(null);
			cartModel.setCartCode(null);
			modelService.save(cartModel);
			modelService.refresh(cartModel);

			//Added code changes for WeSell Implementation - START
			final boolean isSalesRepLoggedIn = (boolean) getSessionService().getAttribute("isSalesRepUserLoggedIn");
			if (isSalesRepLoggedIn)
			{
				final EnergizerB2BEmployeeModel employee = (EnergizerB2BEmployeeModel) getSessionService()
						.getAttribute("salesRepEmployeeModel");
				if (null != employee)
				{
					cartModel.setSalesRepUid(employee.getUid());
					cartModel.setSalesRepName(employee.getName());
					cartModel.setSalesRepEmailID(employee.getEmail());
					cartModel.setPlacedBySalesRep(Boolean.valueOf(true));
					final String selectedEmployeeUser = (String) getSessionService().getAttribute("selectedEmployee");
					if (null != selectedEmployeeUser)
					{
						final EnergizerB2BEmployeeModel selectB2BEmployee = (EnergizerB2BEmployeeModel) userService
								.getUserForUID(selectedEmployeeUser);

						cartModel.setSelectedEmpUid(selectB2BEmployee.getUid());
						cartModel.setSelectedEmpName(selectB2BEmployee.getName());
						cartModel.setSelectedEmpEmailID(selectB2BEmployee.getEmail());
					}

				}

			}
			else
			{
				cartModel.setPlacedBySalesRep(Boolean.valueOf(false));
				LOG.info("Not a Sales Rep order to be created !!");
			}
			//Added code changes for WeSell Implementation - END

			energizerB2BCheckoutFlowFacade.setCurrentUser(cartModel);
			List<BusinessRuleError> OrderValidationErros = new ArrayList<BusinessRuleError>();
			List<BusinessRuleError> ShippingValidationErros = new ArrayList<BusinessRuleError>();

			for (final AbstractOrderEntryModel entryModel : cartModel.getEntries())
			{
				entryModel.setAdjustedItemPrice(new BigDecimal("0.00"));
				entryModel.setAdjustedLinePrice(new BigDecimal("0.00"));
				entryModel.setAdjustedQty(0);
				entryModel.setRejectedStatus("No");
				entryModel.setIsNewEntry("N");
				energizerB2BCheckoutFlowFacade.saveEntry(entryModel);
				ShippingValidationErros = energizerB2BCheckoutFlowFacade.getOrderShippingValidation(entryModel);

				if (ShippingValidationErros.size() > 0)
				{
					for (final BusinessRuleError orderErr : ShippingValidationErros)
					{
						GlobalMessages.addFlashMessage(redirectModel, GlobalMessages.ERROR_MESSAGES_HOLDER,
								Localization.getLocalizedString(orderErr.getMessage()));
					}
				}

				/*
				 * else if (CommerceCartModificationStatus.NO_STOCK.equals(cartModification.getStatusCode())) {
				 * GlobalMessages.addFlashMessage(redirectModel, GlobalMessages.ERROR_MESSAGES_HOLDER,
				 * "basket.page.message.update.reducedNumberOfItemsAdded.noStock", new Object[] {
				 * cartModification.getEntry().getProduct().getName() }); break; } else if (cartModification.getQuantity()
				 * != cartModification.getQuantityAdded()) { // item has been modified to match available stock levels
				 * GlobalMessages.addFlashMessage(redirectModel, GlobalMessages.ERROR_MESSAGES_HOLDER,
				 * "basket.information.quantity.adjusted"); break; }
				 */
				// TODO: handle more specific messaging, i.e. out of stock, product not available
			}

			OrderValidationErros = energizerB2BCheckoutFlowFacade.getOrderValidation(cartModel);
			if (OrderValidationErros.size() > 0)
			{
				for (final BusinessRuleError orderErr : OrderValidationErros)
				{
					GlobalMessages.addFlashMessage(redirectModel, GlobalMessages.ERROR_MESSAGES_HOLDER,
							Localization.getLocalizedString(orderErr.getMessage()));
				}

			}

			if (OrderValidationErros.size() > 0 || ShippingValidationErros.size() > 0)
			{
				energizerB2BCheckoutFlowFacade.removeSessionCart();
				return REDIRECT_PREFIX + "/my-account/order/" + orderCode;
			}
			/*-else if ((boolean) getSessionService().getAttribute("isSalesRepUserLoggedIn"))
			{*/
			return REDIRECT_PREFIX + "/cart"; //Redirecting to the cart page in case sales rep does reorder
			/*-}
			else
			{
				return REDIRECT_PREFIX + "/checkout/single/summary";//checkoutSummary(model);
			}*/
		}
		catch (final Exception e)
		{
			LOG.error("Exception Occured while placing re order....." + e);
			GlobalMessages.addFlashMessage(redirectModel, GlobalMessages.ERROR_MESSAGES_HOLDER,
					Localization.getLocalizedString("form.reorder.error"));
			return REDIRECT_PREFIX + "/my-account/order/" + orderCode;
		}
	}

	@RequestMapping(value = "/uploadDeliveryNotesFile", method = RequestMethod.POST)
	@RequireHardLogIn
	@ResponseBody
	public String uploadDeliveryNotesFile(final Model model, @RequestParam("file")
	final CommonsMultipartFile file) throws CMSItemNotFoundException
	{
		LOG.info("Inside /uploadDeliveryNotesFile method !!!");

		final CartData cartData = cartFacade.getSessionCart();

		// Removing the request attribute for emptyDeliveryNoteFile, if any.
		final Session session = this.getSessionService().getCurrentSession();
		session.removeAttribute("emptyDeliveryNoteFileName");

		if (null != file && !file.isEmpty())
		{
			LOG.info("Cart ID : " + cartData.getCode() + " , File uploaded : "
					+ (null != file ? file.getOriginalFilename().toString() : null));
			// Creating the directory to store delivery note file
			final byte[] bytes = file.getBytes();
			try
			{
				final String rootPath = getConfigValue(EnergizerCoreConstants.MEDIA_ROOT_DIRECTORY) + File.separator
						+ EnergizerCoreConstants.SYS_MASTER + File.separator + EnergizerCoreConstants.DELIVERY_NOTE_FILE_DIRECTORY
						+ File.separator + EnergizerCoreConstants.LATAM + File.separator + cartData.getCode();
				final File dir = new File(rootPath);
				if (!dir.exists())
				{
					dir.mkdirs();
				}
				// Create the file on server
				final File serverFile = new File(dir.getAbsolutePath() + File.separator + file.getOriginalFilename());
				LOG.info("Server File Location = " + serverFile.getAbsolutePath());

				final BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(serverFile, false));
				stream.write(bytes);
				stream.close();

				LOG.info("File saved to the media '" + EnergizerCoreConstants.SYS_MASTER
						+ "' directory, but yet to create/update media model !");

				final boolean newMediaFile = deliveryNoteFacade.createUploadCartMedia(serverFile, file.getOriginalFilename(),
						file.getOriginalFilename() + "_" + cartData.getCode(), EnergizerCoreConstants.PERSONALCARE_CONTENTCATALOG,
						cartData.getCode());

				LOG.info("Successfully created/updated the media model for this file :  " + file.getOriginalFilename());

				if (newMediaFile)
				{
					LOG.info("NEW FILE UPLOAD SUCCESS ...");
					return "NEW FILE UPLOAD SUCCESS";
				}
				else
				{
					LOG.info("EXISTING FILE UPDATE SUCCESS ...");
					return "EXISTING FILE UPDATE SUCCESS";
				}
			}
			catch (final Exception e)
			{
				LOG.info("Failed to upload " + file.getOriginalFilename() + " => " + e.getMessage());
				return "UPLOAD FAILURE";
			}
		}
		else
		{
			LOG.info("Failed to upload " + file.getOriginalFilename() + " because the file is EMPTY ...");
			session.setAttribute("emptyDeliveryNoteFileName", file.getOriginalFilename());
			return "FILE EMPTY";
		}

		//return "UPLOAD SUCCESSFUL";

	}

	@RequestMapping(value = "/downloadDeliveryNoteFile", method = RequestMethod.GET)
	public void downloadDeliveryNoteFile(@RequestParam("cartID")
	final String cartID, @RequestParam("fileName")
	final String fileName, @RequestParam(value = "inline", required = true)
	final Boolean inline, final Model model, final HttpServletRequest request, final HttpServletResponse response)
			throws CMSItemNotFoundException, IOException
	{

		LOG.info("Inside /downloadDeliveryNoteFile method !");
		//OutputStream responseOutputStream = null;
		try (OutputStream responseOutputStream = response.getOutputStream())
		{
			final EnergizerDeliveryNoteData deliveryNoteFileData = deliveryNoteFacade.getDeliveryNoteFileDataForCart(cartID.trim(),
					fileName.trim());

			if (null != deliveryNoteFileData.getUrl())
			{
				if (inline == null)
				{
					response.addHeader("Content-Disposition", "attachment;filename=" + fileName.trim());
				}
				if (inline != null && inline.booleanValue())
				{
					response.addHeader("Content-Disposition", "inline;filename=" + fileName.trim());
				}
				else if (inline != null && !inline.booleanValue())
				{
					response.addHeader("Content-Disposition", "attachment;filename=" + fileName.trim());
				}
				LOG.info("Downloading the file : " + fileName.trim() + " !!!");
				//responseOutputStream = response.getOutputStream();
				response.setContentType(deliveryNoteFileData.getMimeType());
				response.setContentLength(deliveryNoteFileData.getFileSize().intValue());
				responseOutputStream.write(deliveryNoteFacade.getDeliveryNoteFileAsBytes(deliveryNoteFileData.getMediaCode()));
				responseOutputStream.flush();
				responseOutputStream.close();
			}
			else
			{
				response.setContentType(deliveryNoteFileData.getMimeType());
				final PrintWriter pw = response.getWriter();
				final String docType = "<!doctype html public \"-//w3c//dtd html 4.0 " + "transitional//en\">\n";
				pw.println(docType + "<html>");
				pw.println("<head><title>Error</title>");
				pw.println("<body>");
				pw.println("<h1>Failed to download delivery note file " + fileName.trim() + "</h1>");
				pw.println("</body></html>");
			}
		}
		catch (final Exception e)
		{
			LOG.info("Exception occured while downloading the delivery note file, message ::: " + e.getMessage());
			e.printStackTrace();
		}
		/*
		 * finally { responseOutputStream.flush(); responseOutputStream.close(); responseOutputStream = null;
		 * //IOUtils.closeQuietly(responseOutputStream); LOG.info("Output stream is flushed & closed gracefully ..."); }
		 */
	}

	@RequestMapping(value = "/removeDeliveryNoteFile", method = RequestMethod.POST)
	@ResponseBody
	public String removeDeliveryNoteFile(final Model model, @RequestParam("cartID")
	final String cartID, @RequestParam("fileName")
	final String fileName) throws CMSItemNotFoundException, IOException
	{
		LOG.info("Inside /removeDeliveryNoteFile method !!!");

		String response = "SUCCESS";

		try
		{
			if (null != cartID && !StringUtils.isEmpty(cartID) && null != fileName && !StringUtils.isEmpty(fileName))
			{
				// Finding the directory path of this delivery note file
				final String rootPath = getConfigValue(EnergizerCoreConstants.MEDIA_ROOT_DIRECTORY) + File.separator
						+ EnergizerCoreConstants.SYS_MASTER + File.separator + EnergizerCoreConstants.DELIVERY_NOTE_FILE_DIRECTORY
						+ File.separator + EnergizerCoreConstants.LATAM + File.separator + cartID;
				final File dir = new File(rootPath);

				// File on the server
				final File serverFile = new File(dir.getAbsolutePath() + File.separator + fileName);
				final String mediaCode = serverFile.getName() + "_" + cartID;

				// Prepare file for delete operation
				serverFile.setWritable(true);
				final boolean mediaRemovable = deliveryNoteFacade.setMediaRemovableForCart(mediaCode,
						EnergizerCoreConstants.PERSONALCARE_CONTENTCATALOG, cartID);

				boolean mediaDeletedFlag = false;
				// Delete the file from Media Container & Media
				if (this.getSiteUid().equalsIgnoreCase(EnergizerCoreConstants.SITE_PERSONALCARE) && mediaRemovable)
				{
					mediaDeletedFlag = deliveryNoteFacade.deleteDeliveryNoteFileMediaForCart(mediaCode,
							EnergizerCoreConstants.PERSONALCARE_CONTENTCATALOG, cartID);
				}
				/*
				 * else if (this.getSiteUid().equalsIgnoreCase(EnergizerCoreConstants.SITE_PERSONALCAREEMEA)) {
				 * deliveryNoteFacade.deleteDeliveryNoteFileMedia(mediaCode,
				 * EnergizerCoreConstants.PERSONALCAREEMEA_CONTENTCATALOG, cartID); }
				 */

				// Delete the file from physical server location
				if (mediaDeletedFlag)
				{
					response = "SUCCESS";
					serverFile.delete();
					LOG.info("File '" + fileName + "' IS DELETED !!!");
					final File[] listFiles = dir.listFiles();
					if (null != listFiles && listFiles.length == 0)
					{
						//now directory is empty, so we can delete it
						System.out.println("Deleting Empty Directory. Success : " + dir.delete());
					}
				}
				else
				{
					LOG.info("File '" + fileName + "' NOT DELETED due to some error (or) media model may NOT BE DELETED !!");
					response = "ERROR";
				}
			}
		}
		catch (final Exception ex)
		{
			response = "ERROR";
			LOG.error("Exception occurred while deleting the file/directory ::: " + ex.getStackTrace());
			ex.printStackTrace();
			return response;
		}
		return response;
	}

	/**
	 * Need to move out of controller utility method for Replenishment
	 *
	 */
	protected List<String> getNumberRange(final int startNumber, final int endNumber)
	{
		final List<String> numbers = new ArrayList<String>();
		try
		{

			for (int number = startNumber; number <= endNumber; number++)
			{
				numbers.add(String.valueOf(number));
			}
		}
		catch (final Exception e)
		{
			LOG.error("Exception ocuured while fetching number range...");
			e.printStackTrace();
			throw e;
		}
		return numbers;
	}

	private String getShippingPoint() throws Exception
	{
		EnergizerCMIRModel energizerCMIR = null;
		try
		{
			final String userId = defaultEnergizerB2BOrderHistoryFacade.getCurrentUser();
			final EnergizerB2BUnitModel b2bUnit = defaultEnergizerB2BOrderHistoryFacade.getParentUnitForCustomer(userId);
			final String productCode = defaultEnergizerB2BOrderHistoryFacade.getProductCodeForCustomer();
			energizerCMIR = defaultEnergizerB2BOrderHistoryFacade.getEnergizerCMIR(productCode, b2bUnit.getUid());
		}
		catch (final Exception e)
		{
			LOG.error("Exception occured while fetching shipping point....." + e);
			throw e;
		}
		return energizerCMIR.getShippingPoint();
	}

	public List<? extends AddressData> getDeliveryAddressesForB2Bunit() throws Exception
	{
		final List<AddressData> energizerAddresses = new ArrayList<AddressData>();
		try
		{
			final String userId = defaultEnergizerB2BOrderHistoryFacade.getCurrentUser();
			final EnergizerB2BUnitModel b2bUnit = defaultEnergizerB2BOrderHistoryFacade.getParentUnitForCustomer(userId);
			List<AddressData> energizerDeliveryAddresses = new ArrayList<AddressData>();
			energizerDeliveryAddresses = energizerB2BCheckoutFlowFacade.fetchAddressForB2BUnit(b2bUnit.getUid());
			final List<String> soldToAddressIds = energizerB2BCheckoutFlowFacade.getsoldToAddressIds(getShippingPoint());
			for (final String soldToAddressId : soldToAddressIds)
			{
				for (final AddressData address : energizerDeliveryAddresses)
				{
					if (soldToAddressId.equalsIgnoreCase(address.getErpAddressId()))
					{
						energizerAddresses.add(address);
						break;
					}
				}
			}
		}
		catch (final Exception e)
		{
			LOG.error("Exception occured while fetching delivery address for b2bunit...." + e);
			throw e;
		}
		return energizerAddresses;
	}


}
