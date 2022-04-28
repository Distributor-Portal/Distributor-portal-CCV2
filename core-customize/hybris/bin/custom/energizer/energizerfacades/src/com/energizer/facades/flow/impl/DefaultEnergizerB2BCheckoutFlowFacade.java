/**
 *
 */
package com.energizer.facades.flow.impl;

import de.hybris.platform.b2b.services.B2BOrderService;
import de.hybris.platform.b2b.services.B2BWorkflowIntegrationService;
import de.hybris.platform.b2bacceleratorfacades.order.data.B2BOrderApprovalData;
import de.hybris.platform.b2bacceleratorfacades.order.data.B2BPaymentTypeData;
import de.hybris.platform.cms2.servicelayer.services.CMSSiteService;
import de.hybris.platform.commercefacades.order.data.AbstractOrderData;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.order.data.OrderData;
import de.hybris.platform.commercefacades.order.data.OrderEntryData;
import de.hybris.platform.commercefacades.product.data.PriceData;
import de.hybris.platform.commercefacades.product.data.ProductData;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commerceservices.service.data.CommerceCheckoutParameter;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.order.CartService;
import de.hybris.platform.order.InvalidCartException;
import de.hybris.platform.order.OrderService;
import de.hybris.platform.product.PriceService;
import de.hybris.platform.product.ProductService;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.workflow.model.WorkflowActionModel;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.mail.internet.AddressException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.energizer.business.BusinessRuleError;
import com.energizer.core.business.service.EnergizerOrderBusinessRuleValidationService;
import com.energizer.core.business.service.EnergizerOrderEntryBusinessRuleValidationService;
import com.energizer.core.data.EnergizerB2BUnitData;
import com.energizer.core.datafeed.service.impl.DefaultEnergizerAddressService;
import com.energizer.core.model.EnergizerB2BCustomerModel;
import com.energizer.core.model.EnergizerB2BEmployeeModel;
import com.energizer.core.model.EnergizerB2BUnitLeadTimeModel;
import com.energizer.core.model.EnergizerB2BUnitModel;
import com.energizer.core.model.EnergizerCMIRModel;
import com.energizer.core.solr.query.EnergizerSolrQueryManipulationService;
import com.energizer.facades.flow.EnergizerB2BCheckoutFlowFacade;
import com.energizer.facades.order.impl.DefaultEnergizerB2BOrderHistoryFacade;
import com.energizer.services.order.EnergizerB2BOrderService;
import com.energizer.services.product.EnergizerProductService;


/**
 * @author M1023097
 *
 */
public class DefaultEnergizerB2BCheckoutFlowFacade extends DefaultB2BCheckoutFlowFacade implements EnergizerB2BCheckoutFlowFacade
{
	protected static final Logger LOG = Logger.getLogger(DefaultEnergizerB2BCheckoutFlowFacade.class);

	private static final String SITE_PERSONALCAREEMEA = "site.personalCareEMEA";

	private static final String SITE_PERSONALCARE = "site.personalCare";

	@Resource(name = "orderEntryBusinessRulesService")
	EnergizerOrderEntryBusinessRuleValidationService orderEntryBusinessRulesService;

	@Resource(name = "orderBusinessRulesService")
	EnergizerOrderBusinessRuleValidationService orderBusinessRulesService;

	@Resource(name = "energizerB2BOrderService")
	EnergizerB2BOrderService energizerB2BOrderService;

	@Resource(name = "energizerSolrQueryManipulationService")
	EnergizerSolrQueryManipulationService energizerSolrQueryManipulationService;

	@Resource(name = "modelService")
	ModelService modelService;

	@Resource(name = "cartService")
	CartService cartService;

	@Resource(name = "orderService")
	OrderService orderService;

	@Resource(name = "productService")
	ProductService productService;

	@Resource(name = "priceService")
	PriceService priceService;

	@Resource(name = "userService")
	UserService userService;

	@Resource
	private SessionService sessionService;

	@Resource
	DefaultEnergizerAddressService defaultEnergizerAddressService;

	@Resource
	private EnergizerProductService energizerProductService;

	@Resource
	private B2BWorkflowIntegrationService b2bWorkflowIntegrationService;

	@Resource
	private B2BOrderService b2bOrderService;

	@Resource
	private DefaultEnergizerB2BOrderHistoryFacade defaultEnergizerB2BOrderHistoryFacade;

	@Resource(name = "configurationService")
	private ConfigurationService configurationService;

	@Resource(name = "cmsSiteService")
	private CMSSiteService cmsSiteService;

	@Resource
	private CommonI18NService commonI18NService;

	private Converter<OrderModel, OrderData> energizerOrderConverter;
	private Converter<AbstractOrderEntryModel, OrderEntryData> orderEntryConverter;
	private Converter<AddressModel, AddressData> energizerAddressConverter;
	private Converter<CartModel, CartData> energizerCartConverter;
	private Converter<EnergizerB2BUnitModel, EnergizerB2BUnitData> energizerB2BUnitConverter;

	EnergizerB2BUnitModel b2bUnitModel;

	/*
	 * (non-Javadoc)
	 *
	 * @see com.energizer.facades.flow.EnergizerB2BCheckoutFlowFacade#getEnergizerDeliveryAddresses()
	 */
	@Override
	public List<AddressData> getEnergizerDeliveryAddresses()
	{
		final List<AddressData> deliveryAddresses = new ArrayList<AddressData>();
		List<AddressModel> addressModels = new ArrayList<AddressModel>();
		final CartModel cartModel = getCart();
		if (cartModel != null)
		{
			addressModels = getDeliveryService().getSupportedDeliveryAddressesForOrder(cartModel, true);
			if (addressModels != null)
			{
				LOG.info("supported address size: " + addressModels.size());
			}
			else
			{
				LOG.info("supported address size:null");
			}
		}
		for (final AddressModel model : addressModels)
		{
			final AddressData addressData = getEnergizerAddressConverter().convert(model);
			deliveryAddresses.add(addressData);
		}
		return deliveryAddresses;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.hybris.platform.commercefacades.order.impl.DefaultCheckoutFacade#getCheckoutCart()
	 */
	@Override
	public CartData getCheckoutCart()
	{
		final CartModel cartModel = getCart();
		final CartData cartData = getEnergizerCartConverter().convert(cartModel);

		// Added by Soma - START
		if (null != cartData && null == cartData.getPaymentType())
		{
			final B2BPaymentTypeData paymentType = super.getCheckoutCart().getPaymentType();
			cartData.setPaymentType(paymentType);
		}
		if (configurationService.getConfiguration().getString(SITE_PERSONALCARE)
				.equalsIgnoreCase(cmsSiteService.getCurrentSite().getUid()) && null != cartData
				&& (null == cartData.getAvailableVolume() || null == cartData.getAvailableWeight()))
		{
			if (null != cartData.getContainerVolumeUtilization())
			{
				cartData.setAvailableVolume(
						BigDecimal.valueOf(100).subtract(BigDecimal.valueOf(cartData.getContainerVolumeUtilization()))
								.setScale(2, RoundingMode.HALF_EVEN).doubleValue());
			}
			if (null != cartData.getContainerWeightUtilization())
			{
				cartData.setAvailableWeight(
						BigDecimal.valueOf(100).subtract(BigDecimal.valueOf(cartData.getContainerWeightUtilization()))
								.setScale(2, RoundingMode.HALF_EVEN).doubleValue());
			}
		}
		// Added by Soma - END

		return cartData;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.energizer.facades.flow.EnergizerB2BCheckoutFlowFacade#getsoldToAddressIds()
	 */
	@Override
	public List<String> getsoldToAddressIds(final String shippingPointId)
	{
		b2bUnitModel = energizerSolrQueryManipulationService.getB2BUnitForLoggedInUser();
		final List<String> shippingIds = energizerB2BOrderService.getsoldToAddressIds(b2bUnitModel, shippingPointId);
		return shippingIds;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.energizer.facades.flow.EnergizerB2BCheckoutFlowFacade#getOrderValidation(de.hybris.platform.core.model.order
	 * .AbstractOrderEntryModel)
	 */
	@Override
	public List<BusinessRuleError> getOrderValidation(final AbstractOrderModel orderModel)
	{
		final OrderData orderData = new OrderData();
		/*
		 * final ProductModel product = orderEntryModel.getProduct(); final List<PriceInformation> prices =
		 * priceService.getPriceInformationsForProduct(product); final ProductData productData = new ProductData();
		 * productData.setCode(product.getCode()); productData.setDescription(product.getDescription());
		 * productData.setName(product.getName());
		 *
		 * if (!prices.isEmpty()) { final PriceInformation price = prices.iterator().next(); final PriceData priceData =
		 * new PriceData(); priceData.setCurrencyIso(price.getPriceValue().getCurrencyIso());
		 * priceData.setValue(BigDecimal.valueOf(price.getPriceValue().getValue())); productData.setPrice(priceData); }
		 */

		final PriceData priceData = new PriceData();
		priceData.setCurrencyIso(orderModel.getCurrency().getIsocode());
		priceData.setValue(BigDecimal.valueOf(orderModel.getTotalPrice()));


		orderData.setTotalPrice(priceData);
		final List<BusinessRuleError> OrderDataError = new ArrayList<BusinessRuleError>();

		orderBusinessRulesService.validateBusinessRules(orderData);
		if (orderBusinessRulesService.hasErrors())
		{
			OrderDataError.addAll(orderBusinessRulesService.getErrors());
		}
		return OrderDataError;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.energizer.facades.flow.EnergizerB2BCheckoutFlowFacade#getOrderShippingValidation(de.hybris.platform.core.model
	 * .order.AbstractOrderEntryModel)
	 */
	@Override
	public List<BusinessRuleError> getOrderShippingValidation(final AbstractOrderEntryModel orderEntryModel)
	{
		final OrderEntryData entryData = getOrderEntryConverter().convert(orderEntryModel);
		final String shippingPoint = entryData.getProduct().getShippingPoint();
		final String shippingPointLocation = entryData.getProduct().getShippingPointLocation();
		if (shippingPoint != null)
		{
			entryData.setShippingPoint(shippingPoint);
		}
		if (null != shippingPointLocation)
		{
			entryData.setShippingPointLocation(shippingPointLocation);
		}
		final List<BusinessRuleError> OrderDataError = new ArrayList<BusinessRuleError>();
		orderEntryBusinessRulesService.validateBusinessRules(entryData);
		if (orderEntryBusinessRulesService.hasErrors())
		{
			OrderDataError.addAll(orderEntryBusinessRulesService.getErrors());
		}
		return OrderDataError;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.energizer.facades.flow.EnergizerB2BCheckoutFlowFacade#getLeadTimeData(java.lang.String, java.lang.String)
	 */
	public int getLeadTimeData(final String shippingPointId, final String soldToAddressId)
	{
		int leadTIme = 0;
		final EnergizerB2BUnitModel b2bUnitModel = energizerSolrQueryManipulationService.getB2BUnitForLoggedInUser();
		final List<EnergizerB2BUnitLeadTimeModel> models = energizerB2BOrderService.getLeadTimeData(b2bUnitModel, shippingPointId,
				soldToAddressId);
		if (models.size() > 0)
		{
			for (final EnergizerB2BUnitLeadTimeModel leadTimeModel : models)
			{
				if (leadTimeModel.getB2bUnitId().getUid().equalsIgnoreCase(b2bUnitModel.getUid()))
				{
					if (null != leadTimeModel.getLeadTime())
					{
						leadTIme = leadTimeModel.getLeadTime();
					}
				}
			}
		}
		return leadTIme;
	}

	/**
	 * @param deliveryDate
	 * @param orderCode
	 * @return <T extends AbstractOrderData>
	 */
	public <T extends AbstractOrderData> T setDeliveryDate(final String deliveryDate, final String orderCode)
	{
		final String[] splitString = deliveryDate.split("-");
		String deliveryDateFinal = "";
		deliveryDateFinal = splitString[1] + "/" + splitString[0] + "/" + splitString[2];
		final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		Date date = new Date();
		try
		{
			date = dateFormat.parse(deliveryDateFinal);
		}
		catch (final ParseException e)
		{
			e.printStackTrace();
		}
		final CartModel cartModel = cartService.getSessionCart();
		cartModel.setRequestedDeliveryDate(date);
		modelService.save(cartModel);
		return (T) getCheckoutCart();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.energizer.facades.flow.EnergizerB2BCheckoutFlowFacade#getOrderData()
	 */
	@Override
	public AbstractOrderData getOrderData()
	{
		final CartModel cartModel = getCart();
		final AbstractOrderData orderData = getEnergizerCartConverter().convert((cartModel));

		return orderData;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.energizer.facades.flow.EnergizerB2BCheckoutFlowFacade#simulateOrderMarshel(de.hybris.platform.commercefacades
	 * .order.data.CartData)
	 */
	public synchronized void updateSessionCart(final CartData cartData)
	{
		final CartModel model = getCart();
		//model.setB2bUnit(getB2bUnitModel());
		// Added for order model unit!=b2bUnit issue fix - START
		EnergizerB2BUnitModel b2bUnit = null;
		if (null != model && null != model.getUnit() && model.getUnit() instanceof EnergizerB2BUnitModel)
		{
			b2bUnit = (EnergizerB2BUnitModel) model.getUnit();
		}
		if (null == b2bUnit)
		{
			b2bUnit = energizerSolrQueryManipulationService.getB2BUnitForLoggedInUser();
		}
		model.setB2bUnit(b2bUnit);
		// Added for order model unit!=b2bUnit issue fix - END
		if (null != model.getUnit() && null != model.getB2bUnit())
		{
			LOG.info("Setting unit : " + model.getUnit().getUid() + " , b2bUnit : " + model.getB2bUnit().getUid()
					+ " while updating session cart !! ");
		}
		model.setRequestedDeliveryDate(cartData.getRequestedDeliveryDate());
		LOG.info("Requested Delivery Date set in cart model ::: " + cartData.getRequestedDeliveryDate());
		final Double totalPrice = cartData.getTotalPrice().getValue().doubleValue();
		final Double totalTax = cartData.getTotalTax().getValue().doubleValue();
		final Double discount = cartData.getTotalDiscounts().getValue().doubleValue();
		final boolean isSalesRepUser = (boolean) getSessionService().getAttribute("isSalesRepUserLoggedIn");

		// Added for WeSell - Sub Total is excluding the Tax value, totalPrice is the NET_VALUE from Simulate Response Header and this is the sub total
		if (isSalesRepUser)
		{
			model.setSubtotal(totalPrice);
		}
		else
		{
			// No Tax for LATAM/EMEA applications, so existing logic is retained
			model.setSubtotal(totalPrice - discount - totalTax);
		}
		model.setTotalPrice(totalPrice);
		//model.setSubtotal(totalPrice - discount - totalTax);
		model.setTotalDiscounts(discount);
		model.setTotalTax(totalTax);
		model.setOrderComments(cartData.getOrderComments());
		model.setAgreeEdgewellUnitPriceForAllProducts(cartData.isAgreeEdgewellUnitPriceForAllProducts());
		LOG.info("Order Comments set in cart Model ::: " + model.getOrderComments());
		final List<AbstractOrderEntryModel> modelEntries = model.getEntries();
		final List<OrderEntryData> dataEntries = cartData.getEntries();
		for (final AbstractOrderEntryModel modelEntry : modelEntries)
		{
			updateModelEntry(modelEntry, dataEntries);
			modelEntry.setRejectedStatus("No");
			getModelService().save(modelEntry);
		}
		//getCartService().calculateCart(model);
		//model.setCalculated(true);

		//Added code changes for WeSell Implementation - START
		final boolean isSalesRepLoggedIn = (boolean) getSessionService().getAttribute("isSalesRepUserLoggedIn");
		if (isSalesRepLoggedIn)
		{
			final EnergizerB2BEmployeeModel employee = (EnergizerB2BEmployeeModel) getSessionService()
					.getAttribute("salesRepEmployeeModel");
			if (null != employee)
			{
				model.setSalesRepUid(employee.getUid());
				model.setSalesRepName(employee.getName());
				model.setSalesRepEmailID(employee.getEmail());
				model.setPlacedBySalesRep(Boolean.valueOf(true));
				final String selectedEmployeeUser = (String) getSessionService().getAttribute("selectedEmployee");
				if (null != selectedEmployeeUser)
				{
					final EnergizerB2BEmployeeModel selectB2BEmployee = (EnergizerB2BEmployeeModel) userService
							.getUserForUID(selectedEmployeeUser);

					model.setSelectedEmpUid(selectB2BEmployee.getUid());
					model.setSelectedEmpName(selectB2BEmployee.getName());
					model.setSelectedEmpEmailID(selectB2BEmployee.getEmail());
				}

				LOG.info(
						"Setting Sales Rep Name & Email ID to the cart model, so that we can set them up for order email context !!");
				final String salesRepcurrencyIsoCode = cartData.getSalesRepCurrencyIsoCode();
				if (null != salesRepcurrencyIsoCode)
				{
					LOG.info("sales Rep user currency iso code::::" + salesRepcurrencyIsoCode);
					final CurrencyModel currencyModel = commonI18NService.getCurrency(salesRepcurrencyIsoCode.toString().trim());
					model.setCurrency(currencyModel);
				}
			}
			else
			{
				LOG.info("employee null during order checkout !!");
			}
		}
		else
		{
			model.setPlacedBySalesRep(Boolean.valueOf(false));
			LOG.info("Not a Sales Rep order to be created !!");
		}
		//Added code changes for WeSell Implementation - END


		getModelService().save(model);
		//getCartService().calculateCart(model);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.energizer.facades.flow.EnergizerB2BCheckoutFlowFacade#setLeadTime(int)
	 */
	@Override
	public void setLeadTime(final int leadTime) throws ParseException
	{
		final CartModel cartModel = cartService.getSessionCart();
		cartModel.setLeadTime(leadTime);

		final Date sysCurrentDate = new Date();
		final Calendar cal = Calendar.getInstance();
		cal.setTime(sysCurrentDate);


		if (getConfigValue(SITE_PERSONALCAREEMEA).equalsIgnoreCase(this.cmsSiteService.getCurrentSite().getUid()))
		{


			LOG.info("Current Date ::: " + cal.getTime());
			LOG.info("Lead time ::: " + leadTime);

			// add the working days
			for (int i = 0; i < leadTime; i++)
			{
				do
				{
					cal.add(Calendar.DAY_OF_MONTH, 1);
				}
				while (!isWorkingDay(cal));
			}
			LOG.info("Requested delivery date includings weekends and/or holidays ::: " + cal.getTime());

		}
		else
		{
			cal.add(Calendar.DATE, leadTime); // add LeadTime

		}


		cartModel.setRequestedDeliveryDate(cal.getTime());

		modelService.save(cartModel);
	}


	/*
	 * (non-Javadoc)
	 *
	 * @see com.energizer.facades.flow.EnergizerB2BCheckoutFlowFacade#setLeadTime(int,
	 * de.hybris.platform.commercefacades.order.data.CartData)
	 */
	@Override
	public void setLeadTime(int leadTime, final CartData cartData) throws ParseException
	{
		final CartModel cartModel = cartService.getSessionCart();
		//cartModel.setLeadTime(leadTime);

		//final Date sysCurrentDate = new Date();
		final Date sysCurrentDate = cartData.getRequestedDeliveryDate();
		final Calendar cal = Calendar.getInstance();
		cal.setTime(sysCurrentDate);

		/*
		 * if (getConfigValue(SITE_PERSONALCAREEMEA).equalsIgnoreCase(this.cmsSiteService.getCurrentSite().getUid())) {
		 */

		LOG.info("Actual LeadTime ::: " + leadTime + " days !");

		// ONLY for LATAM
		if (getConfigValue(SITE_PERSONALCARE).equalsIgnoreCase(this.cmsSiteService.getCurrentSite().getUid()))
		{
			// Adding 30 days for time being for LATAM. Remove it once SAP changes are done.
			leadTime += Integer.parseInt(getConfigValue("latam.leadtime.additional.days"));

			if (Integer.valueOf(getConfigValue("latam.leadtime.additional.days")) > 0)
			{
				LOG.info("Added " + getConfigValue("latam.leadtime.additional.days") + " days extra to the lead time for LATAM. This "
						+ getConfigValue("latam.leadtime.additional.days")
						+ " days will be reset to zero(0) once the SAP Changes are done !");
			}
			else
			{
				LOG.info("Additional lead time configured is zero !");
			}
		}

		LOG.info("Current Date is ::: " + cal.getTime() + ", LeadTime ::: " + leadTime + " . So adding leadTime of '" + leadTime
				+ "' days to the current date ...");

		// add the working days
		for (int i = 0; i < leadTime; i++)
		{
			do
			{
				cal.add(Calendar.DAY_OF_MONTH, 1);
			}
			while (!isWorkingDay(cal));
		}
		LOG.info("Requested delivery date excluding weekends and/or holidays ::: " + cal.getTime());
		/*
		 * } else { cal.add(Calendar.DATE, leadTime); // add LeadTime }
		 */

		cartModel.setLeadTime(leadTime);
		cartModel.setRequestedDeliveryDate(cal.getTime());

		modelService.save(cartModel);
	}

	/**
	 * @param cal
	 *           the Calendar
	 * @return boolean the boolean value
	 */
	private boolean isWorkingDay(final Calendar cal) throws ParseException
	{

		boolean isHoliday = false;
		String holidays = StringUtils.EMPTY;

		if (getConfigValue(SITE_PERSONALCAREEMEA).equalsIgnoreCase(this.cmsSiteService.getCurrentSite().getUid()))
		{
			holidays = getConfigValue("holidays.EMEA");
		}
		else if (getConfigValue(SITE_PERSONALCARE).equalsIgnoreCase(this.cmsSiteService.getCurrentSite().getUid()))
		{
			holidays = getConfigValue("holidays.LATAM");
		}
		if (null != holidays && !StringUtils.isEmpty(holidays))
		{
			isHoliday = isHoliday(holidays, cal, isHoliday);
		}
		final int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
		if (dayOfWeek == Calendar.SUNDAY || dayOfWeek == Calendar.SATURDAY || isHoliday)
		{
			return false;
		}
		return true;
	}

	/**
	 * @param holidaysEMEA
	 *           the holidays String
	 * @param cal
	 *           the Calendar
	 * @param isHoliday
	 *           the boolean response
	 * @return isHoliday
	 * @throws ParseException
	 */
	private boolean isHoliday(final String holidaysEMEA, final Calendar cal, boolean isHoliday) throws ParseException
	{

		final String[] holidaysList = (null != holidaysEMEA) ? holidaysEMEA.split(",") : null;
		final List<Calendar> calendarDates = new ArrayList<Calendar>();
		final SimpleDateFormat sdf = new SimpleDateFormat(getConfigValue("dateFormat.EMEA"));

		if (null != holidaysList && holidaysList.length > 0)
		{
			for (final String s : holidaysList)
			{
				if (null != s && StringUtils.isNotBlank(s))
				{
					final Date date = sdf.parse(s);
					calendarDates.add(DateUtils.toCalendar(date));
				}
			}

			if (null != calendarDates && !calendarDates.isEmpty())
			{
				for (final Calendar calendar : calendarDates)
				{
					if ((calendar.get(Calendar.YEAR) == cal.get(Calendar.YEAR))
							&& (calendar.get(Calendar.MONTH) == cal.get(Calendar.MONTH))
							&& (calendar.get(Calendar.DAY_OF_MONTH) == cal.get(Calendar.DAY_OF_MONTH)))
					{
						isHoliday = true;
						break;
					}
				}
			}
		}

		return isHoliday;

	}

	private String getConfigValue(final String key)
	{
		return configurationService.getConfiguration().getString(key);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.energizer.facades.flow.EnergizerB2BCheckoutFlowFacade#simulateOrder(de.hybris.platform.commercefacades.order
	 * .data.CartData)
	 */
	@Override
	public CartData simulateOrder(final CartData cartData, final String requestSource) throws Exception, AddressException
	{
		final EnergizerB2BUnitModel b2bUnitModel = energizerSolrQueryManipulationService.getB2BUnitForLoggedInUser();
		Date requestedDelDate = null;

		try
		{
			if (configurationService.getConfiguration().getString(SITE_PERSONALCARE)
					.equalsIgnoreCase(cmsSiteService.getCurrentSite().getUid()))
			{
				requestedDelDate = new Date(); // Pass the current date instance to the SAP in Simulate Call for LATAM.
				cartData.setRequestedDeliveryDate(requestedDelDate);
			}
			else
			{

				requestedDelDate = getCartService().getSessionCart().getRequestedDeliveryDate();
			}

			final EnergizerB2BUnitData unitdata = getEnergizerB2BUnitConverter().convert(b2bUnitModel);
			cartData.setB2bUnit(unitdata);
			return energizerB2BOrderService.simulateOrder(cartData, requestSource);
		}
		catch (final AddressException e)
		{
			throw e;
		}
		catch (final Exception e)
		{
			LOG.error("Exception occured during simulate order ::: " + e.getMessage());
			throw e;
		}
	}


	private void updateModelEntry(final AbstractOrderEntryModel modelEntry, final List<OrderEntryData> dataEntries)
	{
		for (final OrderEntryData dataEntrie : dataEntries)
		{
			final ProductData prodData = dataEntrie.getProduct();
			final ProductModel prodModel = modelEntry.getProduct();
			if (prodData.getCode().equals(prodModel.getCode()))
			{
				final Long modelQuantity = modelEntry.getQuantity();
				final Long dataQuantity = dataEntrie.getQuantity();
				LOG.info("modelQuantity =" + modelQuantity + " dataQuantity =" + dataQuantity);
				if (!(modelQuantity == dataQuantity))
				{
					modelEntry.setQuantity(dataQuantity);
				}
				final double modelPrice = modelEntry.getBasePrice();
				final double dataPrice = dataEntrie.getBasePrice().getValue().doubleValue();
				LOG.info("modelPrice =" + modelPrice + " dataPrice =" + dataPrice);
				if (!(modelPrice == dataPrice))
				{
					modelEntry.setBasePrice(dataPrice);
				}
				modelEntry.setTotalPrice(dataEntrie.getTotalPrice().getValue().doubleValue());
			}
		}
	}



	/**
	 * @param currentUser
	 * @param string
	 */
	public void setOrderApprover(final EnergizerB2BCustomerModel orderApprover, final B2BOrderApprovalData b2bOrderApprovalData,
			final String rejectionComment)
	{
		final WorkflowActionModel workflowActionModel = getB2bWorkflowIntegrationService()
				.getActionForCode(b2bOrderApprovalData.getWorkflowActionModelCode());
		final OrderModel orderModel = getB2bWorkflowIntegrationService().getOrderFromAction(workflowActionModel);
		//final OrderModel orderModel = b2bOrderService.getOrderForCode(orderCode);
		orderModel.setOrderApprover(orderApprover);
		orderModel.setRejectionComment(rejectionComment);
		modelService.save(orderModel);
	}

	/**
	 * @return the energizerB2BUnitConverter
	 */
	public Converter<EnergizerB2BUnitModel, EnergizerB2BUnitData> getEnergizerB2BUnitConverter()
	{
		return energizerB2BUnitConverter;
	}

	/**
	 * @param energizerB2BUnitConverter
	 *           the energizerB2BUnitConverter to set
	 */
	@Required
	public void setEnergizerB2BUnitConverter(
			final Converter<EnergizerB2BUnitModel, EnergizerB2BUnitData> energizerB2BUnitConverter)
	{
		this.energizerB2BUnitConverter = energizerB2BUnitConverter;
	}

	/**
	 * @param productCode
	 * @return ProductData
	 */
	public ProductData getProduct(final String productCode)
	{
		return null;
	}

	/**
	 * @param productService
	 */
	public void setProductService(final ProductService productService)
	{
		this.productService = productService;
	}

	/**
	 * @param priceService
	 */
	public void setPriceService(final PriceService priceService)
	{
		this.priceService = priceService;
	}

	/**
	 * @return orderEntryConverter
	 */
	public Converter<AbstractOrderEntryModel, OrderEntryData> getOrderEntryConverter()
	{
		return orderEntryConverter;
	}

	/**
	 * @param orderEntryConverter
	 */
	@Required
	public void setOrderEntryConverter(final Converter<AbstractOrderEntryModel, OrderEntryData> orderEntryConverter)
	{
		this.orderEntryConverter = orderEntryConverter;
	}

	/**
	 * @return energizerOrderConverter
	 */
	public Converter<OrderModel, OrderData> getEnergizerOrderConverter()
	{
		return energizerOrderConverter;
	}

	/**
	 * @param energizerOrderConverter
	 */
	public void setEnergizerOrderConverter(final Converter<OrderModel, OrderData> energizerOrderConverter)
	{
		this.energizerOrderConverter = energizerOrderConverter;
	}

	/**
	 * @return energizerAddressConverter
	 */
	public Converter<AddressModel, AddressData> getEnergizerAddressConverter()
	{
		return energizerAddressConverter;
	}

	/**
	 * @param energizerAddressConverter
	 */
	public void setEnergizerAddressConverter(final Converter<AddressModel, AddressData> energizerAddressConverter)
	{
		this.energizerAddressConverter = energizerAddressConverter;
	}

	/**
	 * @return energizerCartConverter
	 */
	public Converter<CartModel, CartData> getEnergizerCartConverter()
	{
		return energizerCartConverter;
	}

	/**
	 * @param energizerCartConverter
	 */
	public void setEnergizerCartConverter(final Converter<CartModel, CartData> energizerCartConverter)
	{
		this.energizerCartConverter = energizerCartConverter;
	}


	/**
	 * @return the b2bUnitModel
	 */
	public EnergizerB2BUnitModel getB2bUnitModel()
	{
		return b2bUnitModel;
	}

	/**
	 * @param b2bUnitModel
	 *           the b2bUnitModel to set
	 */
	public void setB2bUnitModel(final EnergizerB2BUnitModel b2bUnitModel)
	{
		this.b2bUnitModel = b2bUnitModel;
	}

	/**
	 *
	 */
	public CartModel getSessionCart()
	{
		return cartService.getSessionCart();
	}

	/**
	 * @param entryModel
	 */
	public void saveEntry(final AbstractOrderEntryModel entryModel)
	{
		// YTODO Auto-generated method stub
		modelService.save(entryModel);
	}

	/**
	 * @param cartModel
	 */
	public void setCurrentUser(final CartModel cartModel)
	{
		// YTODO Auto-generated method stub
		cartModel.setUser(userService.getCurrentUser());
		modelService.save(cartModel);
	}

	/**
	 * @return the sessionService
	 */
	public SessionService getSessionService()
	{
		return sessionService;
	}

	/**
	 * @param sessionService
	 *           the sessionService to set
	 */
	public void setSessionService(final SessionService sessionService)
	{
		this.sessionService = sessionService;
	}

	@Override
	public OrderData placeOrder() throws InvalidCartException
	{
		final CartModel cartModel = getCart();
		if (cartModel != null)
		{
			final UserModel currentUser = getCurrentUserForCheckout();
			if (cartModel.getUser().equals(currentUser) || getCheckoutCustomerStrategy().isAnonymousCheckout())
			{
				beforePlaceOrder(cartModel);
				cartModel.getTotalPrice();
				final OrderModel orderModel = placeOrder(cartModel);
				LOG.info("OrderCommentsfromcartModel-->" + cartModel.getOrderComments());
				/*
				 * LOG.info("OrderCommentsfromcartModel-->" + cartModel.getOrderComments());
				 * orderModel.setOrderComments(cartModel.getOrderComments()); //modelService.save(orderModel);
				 * LOG.info("OrderCommentsfromorderModel-->" + orderModel.getOrderComments());
				 *
				 * LOG.info("OrderplacedbySalesRep-->" + orderModel.getPlacedBySalesRep()); LOG.info("OrderTax-->" +
				 * cartModel.getTotalTax());
				 */

				afterPlaceOrder(cartModel, orderModel);

				// Convert the order to an order data
				if (orderModel != null)
				{
					orderModel.getAdjustedTotalPrice();
					orderModel.getTotalPrice();
					return getOrderConverter().convert(orderModel);
				}
			}
		}

		return null;
	}

	@Override
	protected synchronized void afterPlaceOrder(final CartModel cartModel, final OrderModel orderModel)
	{
		try
		{
			cartModel.getTotalPrice();

			if (orderModel != null)
			{
				orderModel.getTotalPrice();

				orderModel.setTotalPrice(cartModel.getTotalPrice());
				orderModel.setSubtotal(cartModel.getSubtotal());
				orderModel.setOrderComments(cartModel.getOrderComments());
				orderModel.setPlacedBySalesRep(cartModel.getPlacedBySalesRep());
				//Added Code changes for WeSell Implementation - START
				if (null != orderModel.getPlacedBySalesRep() && orderModel.getPlacedBySalesRep().booleanValue())
				{
					LOG.info("Order placed by Sales Rep !!! ");
					orderModel.setTotalTax(cartModel.getTotalTax());


				}
				else
				{
					LOG.info("Order placed by a Non-Sales Rep !!! ");
				}
				//Added Code changes for WeSell Implementation - END
				orderModel.setAgreeEdgewellUnitPriceForAllProducts(cartModel.getAgreeEdgewellUnitPriceForAllProducts());
				orderModel.setContainerVolumeUtilization(cartModel.getContainerVolumeUtilization());
				orderModel.setContainerWeightUtilization(cartModel.getContainerWeightUtilization());
				LOG.info("Order Comments set in order Model ::: " + orderModel.getOrderComments());

				//Added code changes for Delivery notes feature for LATAM
				orderModel.setDeliveryNoteFiles(cartModel.getDeliveryNoteFiles());
				orderModel.setCartCode(cartModel.getCode());
				// Remove cart
				getCartService().removeSessionCart();


				for (final AbstractOrderEntryModel entry : orderModel.getEntries())
				{
					final EnergizerCMIRModel cmir = energizerProductService.getEnergizerCMIR(entry.getProduct().getCode(),
							orderModel.getB2bUnit().getUid());
					entry.setCustomerMaterialId(cmir.getCustomerMaterialId());
					modelService.save(entry);
				}

				// Added for  order model unit!=b2bUnit issue fix - START
				EnergizerB2BUnitModel b2bUnit = null;
				if (null != orderModel && null != orderModel.getUnit() && orderModel.getUnit() instanceof EnergizerB2BUnitModel)
				{
					b2bUnit = (EnergizerB2BUnitModel) orderModel.getUnit();
				}
				if (null == b2bUnit)
				{
					b2bUnit = energizerSolrQueryManipulationService.getB2BUnitForLoggedInUser();
				}
				orderModel.setB2bUnit(b2bUnit);
				if (null != orderModel.getUnit() && null != orderModel.getB2bUnit())
				{
					LOG.info("Setting unit : " + orderModel.getUnit().getUid() + " , b2bUnit : " + orderModel.getB2bUnit().getUid()
							+ " during after place order !! ");
				}
				// Added for  order model unit!=b2bUnit issue fix - END


				modelService.save(orderModel);
				getModelService().refresh(orderModel);
				// Remove cart
				getCartService().removeSessionCart();
			}
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 *
	 */
	public void removeSessionCart()
	{
		// YTODO Auto-generated method stub
		getCartService().removeSessionCart();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.energizer.facades.flow.EnergizerB2BCheckoutFlowFacade#getActionForCode(java.lang.String)
	 */
	@Override
	public WorkflowActionModel getActionForCode(final String workFlowActionCode)
	{
		return getB2bWorkflowIntegrationService().getActionForCode(workFlowActionCode);
	}

	/**
	 * @return the b2bWorkflowIntegrationService
	 */
	public B2BWorkflowIntegrationService getB2bWorkflowIntegrationService()
	{
		return b2bWorkflowIntegrationService;
	}

	/**
	 * @param b2bWorkflowIntegrationService
	 *           the b2bWorkflowIntegrationService to set
	 */
	public void setB2bWorkflowIntegrationService(final B2BWorkflowIntegrationService b2bWorkflowIntegrationService)
	{
		this.b2bWorkflowIntegrationService = b2bWorkflowIntegrationService;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.energizer.facades.flow.EnergizerB2BCheckoutFlowFacade#setContainerAttributes(de.hybris.platform.
	 * commercefacades .order.data.CartData)
	 */
	@Override
	public void setContainerAttributes(final CartData cartData)
	{
		final CartModel cartModel = cartService.getSessionCart();
		if (null != cartData.getContainerHeight())
		{
			cartModel.setContainerHeight(cartData.getContainerHeight());
		}
		if (null != cartData.getContainerPackingType())
		{
			cartModel.setContainerPackingType(cartData.getContainerPackingType());
		}
		cartModel.setContainerVolumeUtilization(cartData.getTotalProductVolumeInPercent());
		cartModel.setContainerWeightUtilization(cartData.getTotalProductWeightInPercent());
		cartModel.setTotalPalletCount(cartData.getTotalPalletCount());
		cartModel.setVirtualPalletCount(cartData.getVirtualPalletCount());
		cartModel.setPartialPalletCount(cartData.getPartialPalletCount());
		cartModel.setPalStackData(cartData.getPalStackData());
		modelService.save(cartModel);

	}

	public List<AddressData> fetchAddressForB2BUnit(final String b2bUnitUId)
	{
		b2bUnitModel = energizerSolrQueryManipulationService.getB2BUnitForLoggedInUser();
		final List<AddressData> deliveryAddresses = new ArrayList<AddressData>();
		final List<AddressModel> energizerB2BUnitModelList = defaultEnergizerAddressService.fetchAddressForB2BUnit(b2bUnitUId);

		for (final AddressModel model : energizerB2BUnitModelList)
		{
			final AddressData addressData = getEnergizerAddressConverter().convert(model);
			//LOG.info("no exception");
			deliveryAddresses.add(addressData);
		}
		return deliveryAddresses;
	}



	public boolean setSingleDeliveryAddress(final AddressData addressData)
	{
		final CartModel cartModel = getCart();
		if (cartModel != null)
		{
			AddressModel addressModel = null;
			if (addressData != null)
			{
				if (addressData.getId() != null)
				{
					addressModel = getSingleDeliveryAddressModelForCode(addressData.getErpAddressId());
				}
				else
				{
					addressModel = createDeliveryAddressModel(addressData, cartModel);
				}
			}

			final CommerceCheckoutParameter parameter = new CommerceCheckoutParameter();
			parameter.setEnableHooks(true);
			parameter.setCart(cartModel);
			parameter.setAddress(addressModel);
			parameter.setIsDeliveryAddress(false);

			return getCommerceCheckoutService().setDeliveryAddress(parameter);
		}
		return false;
	}


	protected AddressModel getSingleDeliveryAddressModelForCode(final String erpAddressId)
	{

		final String userId = defaultEnergizerB2BOrderHistoryFacade.getCurrentUser();
		final EnergizerB2BUnitModel b2bUnit = defaultEnergizerB2BOrderHistoryFacade.getParentUnitForCustomer(userId);
		final CartModel cartModel = getCart();
		if (cartModel != null)
		{
			final List<AddressModel> addresses = defaultEnergizerAddressService.fetchAddress(erpAddressId);
			for (final AddressModel address : addresses)
			{
				if (address.getOwner().toString().contains(b2bUnit.getPk().toString()))
				{
					LOG.info("address: " + address);
					return address;
				}

			}

		}
		return null;
	}


}
