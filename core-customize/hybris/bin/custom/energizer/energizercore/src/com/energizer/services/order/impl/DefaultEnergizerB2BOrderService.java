/**
 *
 */
package com.energizer.services.order.impl;

import de.hybris.platform.acceleratorservices.email.EmailService;
import de.hybris.platform.acceleratorservices.model.email.EmailAddressModel;
import de.hybris.platform.acceleratorservices.model.email.EmailAttachmentModel;
import de.hybris.platform.acceleratorservices.model.email.EmailMessageModel;
import de.hybris.platform.b2b.company.B2BCommerceUnitService;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.cms2.servicelayer.services.CMSSiteService;
import de.hybris.platform.commercefacades.order.data.AbstractOrderData;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.order.data.OrderEntryData;
import de.hybris.platform.commercefacades.product.PriceDataFactory;
import de.hybris.platform.commercefacades.product.data.PriceData;
import de.hybris.platform.commercefacades.product.data.PriceDataType;
import de.hybris.platform.commercefacades.product.data.ProductData;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.order.CartService;
import de.hybris.platform.product.ProductService;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;
import de.hybris.platform.util.Config;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;
import javax.mail.internet.AddressException;
import javax.validation.ValidationException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.energizer.core.constants.EnergizerCoreConstants;
import com.energizer.core.createorder.jaxb.xsd.objects.DTB2BSALESORDERCREATEREQUEST;
import com.energizer.core.createorder.jaxb.xsd.objects.DTB2BSALESORDERCREATEREQUEST.ISOHEAD;
import com.energizer.core.createorder.jaxb.xsd.objects.DTB2BSALESORDERCREATERESPONSE;
import com.energizer.core.data.EnergizerB2BUnitData;
import com.energizer.core.jaxb.xsd.objects.DTB2BSALESORDERSIMULATEREQUEST;
import com.energizer.core.jaxb.xsd.objects.DTB2BSALESORDERSIMULATEREQUEST.MESSAGETABLE;
import com.energizer.core.jaxb.xsd.objects.DTB2BSALESORDERSIMULATEREQUEST.ORDERINCOMPLETE;
import com.energizer.core.jaxb.xsd.objects.DTB2BSALESORDERSIMULATEREQUEST.TSOITEM;
import com.energizer.core.jaxb.xsd.objects.DTB2BSALESORDERSIMULATEREQUEST.TSOITEM.ZSDTSOITEM;
import com.energizer.core.jaxb.xsd.objects.DTB2BSALESORDERSIMULATEREQUEST.TSOPARTNER;
import com.energizer.core.jaxb.xsd.objects.DTB2BSALESORDERSIMULATEREQUEST.TSOPARTNER.ZSDTSOPART;
import com.energizer.core.jaxb.xsd.objects.DTB2BSALESORDERSIMULATEREQUEST.TTEXTS;
import com.energizer.core.jaxb.xsd.objects.DTB2BSALESORDERSIMULATEREQUEST.TTEXTS.BAPISDTEXT;
import com.energizer.core.jaxb.xsd.objects.DTB2BSALESORDERSIMULATEREQUEST.TTSOCONDITIONS;
import com.energizer.core.jaxb.xsd.objects.DTB2BSALESORDERSIMULATERESPONSE;
import com.energizer.core.jaxb.xsd.objects.DTB2BSALESORDERSIMULATERESPONSE.ESOHEAD;
import com.energizer.core.jaxb.xsd.objects.DTB2BSALESORDERSIMULATERESPONSE.MESSAGETABLE.BAPIRET2;
import com.energizer.core.jaxb.xsd.objects.DTB2BSALESORDERSIMULATERESPONSE.TTSOCONDITIONS.ZSDTSOCONDITIONS;
import com.energizer.core.jaxb.xsd.objects.ObjectFactory;
import com.energizer.core.model.EnergizerB2BUnitLeadTimeModel;
import com.energizer.core.model.EnergizerB2BUnitModel;
import com.energizer.core.model.EnergizerCMIRModel;
import com.energizer.core.model.EnergizerPriceRowModel;
import com.energizer.core.model.EnergizerProductConversionFactorModel;
import com.energizer.core.model.EnergizerProductModel;
import com.energizer.services.order.EnergizerB2BOrderService;
import com.energizer.services.order.dao.EnergizerB2BOrderDAO;
import com.energizer.services.product.EnergizerProductService;


/**
 * @author M1023097
 *
 */
public class DefaultEnergizerB2BOrderService implements EnergizerB2BOrderService
{

	protected static final Logger LOG = Logger.getLogger(DefaultEnergizerB2BOrderService.class);

	@Resource(name = "energizerB2BOrderDAO")
	EnergizerB2BOrderDAO energizerB2BOrderDAO;

	@Resource(name = "modelService")
	ModelService modelService;

	@Resource(name = "configurationService")
	private ConfigurationService configurationService;
	@Resource(name = "productService")
	ProductService productService;
	@Resource(name = "baseSiteService")
	BaseSiteService baseSiteService;

	@Resource(name = "cmsSiteService")
	private CMSSiteService cmsSiteService;

	@Resource
	private SessionService sessionService;

	@Resource(name = "userService")
	private UserService userService;

	@Resource
	private EmailService emailService;

	private PriceDataFactory priceDataFactory;

	private CommonI18NService commonI18NService;

	@Resource
	private CartService cartService;


	@Resource(name = "energizerCartService")
	DefaultEnergizerCartService energizerCartService;

	@Resource(name = "b2bCommerceUnitService")
	private B2BCommerceUnitService b2bCommerceUnitService;

	final String MIME_TYPE = "application/vnd.xml";

	/**
	 * @return the priceDataFactory
	 */
	public PriceDataFactory getPriceDataFactory()
	{
		return priceDataFactory;
	}

	/**
	 * @param priceDataFactory
	 *           the priceDataFactory to set
	 */
	public void setPriceDataFactory(final PriceDataFactory priceDataFactory)
	{
		this.priceDataFactory = priceDataFactory;
	}

	/**
	 * @return the commonI18NService
	 */
	public CommonI18NService getCommonI18NService()
	{
		return commonI18NService;
	}

	/**
	 * @param commonI18NService
	 *           the commonI18NService to set
	 */
	public void setCommonI18NService(final CommonI18NService commonI18NService)
	{
		this.commonI18NService = commonI18NService;
	}

	/**
	 * @return the cartService
	 */
	public CartService getCartService()
	{
		return cartService;
	}

	/**
	 * @param cartService
	 *           the cartService to set
	 */
	public void setCartService(final CartService cartService)
	{
		this.cartService = cartService;
	}

	String siteName = null;

	final String PERSONALCARE = "personalCare";

	final String PERSONALCAREEMEA = "personalCareEMEA";
	final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	@Resource
	private EnergizerProductService energizerProductService;

	private static final int SUCCESS = 1;
	private static final int FAILURE = 0;
	private static final String DOC_NUMBER = "order.comments.doc.number";
	private static final String ITM_NUMBER = "order.comments.itm.number";
	private static final String TEXT_ID_PERSONALCARE = "order.comments.text.id.personalCare";
	private static final String TEXT_ID_PERSONALCARE_EMEA = "order.comments.text.id.personalCareEMEA";
	private static final String LANGU = "order.comments.langu";
	private static final String FORMAT_COL = "order.comments.format.col";
	private static final String SITE_PERSONALCARE = "site.personalCare";
	private static final String SITE_PERSONALCARE_EMEA = "site.personalCareEMEA";
	private static final String ORDER_SIMULATE_CONTEXT_PATH = "com.energizer.core.jaxb.xsd.objects";
	private static final String ORDER_CREATE_CONTEXT_PATH = "com.energizer.core.createorder.jaxb.xsd.objects";
	public static final String SIMULATE_URL = Config.getParameter("simulateURL");
	public static final String ORDER_SUBMIT_URL = Config.getParameter("orderSubmitURL");

	/*
	 * (non-Javadoc)
	 *
	 * @see com.energizer.services.order.EnergizerB2BOrderService#getLeadTimeData()
	 */
	@Override
	public List<EnergizerB2BUnitLeadTimeModel> getLeadTimeData(final EnergizerB2BUnitModel b2bUnitModel,
			final String shippingPointId, final String soldToAddressId)
	{
		List<EnergizerB2BUnitLeadTimeModel> b2bUnitLeadTimeModel = null;
		try
		{
			b2bUnitLeadTimeModel = energizerB2BOrderDAO.getLeadTimeData(b2bUnitModel, shippingPointId, soldToAddressId);
		}
		catch (final Exception e)
		{
			LOG.error("Exception occured while fetching lead time data.... " + e);
			throw e;
		}
		return b2bUnitLeadTimeModel;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.energizer.services.order.EnergizerB2BOrderService#getDeliveryAddress(java.lang.String)
	 */
	@Override
	public List<String> getsoldToAddressIds(final EnergizerB2BUnitModel b2bUnitModel, final String shippingPointId)
	{
		return energizerB2BOrderDAO.getsoldToAddressIds(b2bUnitModel, shippingPointId);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.energizer.services.order.EnergizerB2BOrderService#simulateOrder(de.hybris.platform.commercefacades.order.data
	 * .CartData)
	 */
	@Override
	public CartData simulateOrder(final CartData cartData, final String requestSource)
			throws Exception, AddressException, ValidationException
	{
		try
		{
			final Long startTime = System.currentTimeMillis();
			LOG.info("Before marshall " + startTime);
			final String orderSimulateXML = simulateOrderMarshall(cartData, requestSource);
			if (orderSimulateXML.equalsIgnoreCase("UOM Empty"))
			{
				/*
				 * LOG.info(
				 * "UOM Empty for atleast one product in the cart data during request XML creation, so Simulate Request CANNOT be processed !"
				 * );
				 */
				throw new Exception(
						"UOM Empty for atleast one product in the cart data during request XML creation, so Simulate Request CANNOT be processed !");
			}
			else
			{
				final Long marshallTime = System.currentTimeMillis();
				LOG.info("Marshall took " + (marshallTime - startTime) + " milliseconds");
				final String restCallResponse = invokeRESTCall(orderSimulateXML, "simulate", new OrderModel(), cartData);
				final Long unmarshallTime = System.currentTimeMillis();
				LOG.info("REST Call took " + (unmarshallTime - marshallTime) + " milliseconds");
				final AbstractOrderData orderData = simulateOrderUnMarshall(restCallResponse, cartData, orderSimulateXML,
						restCallResponse, "simulate");


				LOG.info("UnMarshall took " + (System.currentTimeMillis() - unmarshallTime) + " milliseconds");

				return (CartData) orderData;
				//return cartData;

			}
		}
		catch (final AddressException e)
		{
			throw e;
		}
		catch (final ValidationException e)
		{
			throw e;
		}

		catch (final Exception e)
		{
			throw e;
		}
	}


	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.energizer.services.order.EnergizerB2BOrderService#createOrder(de.hybris.platform.core.model.order.OrderModel)
	 */
	@Override
	public int createOrder(final OrderModel orderModel) throws Exception
	{
		String OrderCreationXML = "";
		final Long startTime = System.currentTimeMillis();
		LOG.info("Before create order marshall " + startTime);
		try
		{
			OrderCreationXML = createOrderMarshall(orderModel);
			if (OrderCreationXML.equalsIgnoreCase("UOM Empty"))
			{
				/*
				 * LOG.info(
				 * "UOM Empty for atleast one product in the cart data during request XML creation, so Create Request CANNOT be processed !"
				 * );
				 */
				throw new Exception(
						"UOM Empty for atleast one product in the cart data during request XML creation, so Create Request CANNOT be processed !");
			}
		}
		catch (final Exception e)
		{
			throw e;
		}

		final Long marshallTime = System.currentTimeMillis();
		LOG.info("Create order Marshall took " + (marshallTime - startTime) + " milliseconds");
		final String restCallResponse = invokeRESTCall(OrderCreationXML, "createOrder", orderModel, new AbstractOrderData());

		final Long unmarshallTime = System.currentTimeMillis();
		LOG.info("Create order REST Call took " + (unmarshallTime - marshallTime) + " milliseconds");
		try
		{
			simulateOrderforIDUnMarshall(restCallResponse, orderModel, OrderCreationXML, restCallResponse, "createOrder");
			LOG.info("Create order UnMarshall took " + (System.currentTimeMillis() - unmarshallTime) + " milliseconds");
			return SUCCESS;
		}
		catch (final Exception e)
		{
			LOG.info("Caught Exception during UnMarshall: " + e.getMessage());
			return FAILURE;
		}
	}

	private String simulateOrderMarshall(final CartData orderData, final String requestSource)
			throws JAXBException, Exception, AddressException, ValidationException
	{
		LOG.info("Request source ::: " + requestSource);

		final ObjectFactory objectFactory = new ObjectFactory();
		StringWriter stringWriter = new StringWriter();
		String parsedXML = null;
		JAXBContext context;
		final Set<String> emptyUomProducts = new HashSet<String>();
		try
		{
			context = JAXBContext.newInstance(ORDER_SIMULATE_CONTEXT_PATH);
			final DTB2BSALESORDERSIMULATEREQUEST xmlRoot = objectFactory.createDTB2BSALESORDERSIMULATEREQUEST();
			final EnergizerB2BUnitData b2bUnitData = orderData.getB2bUnit();
			final com.energizer.core.jaxb.xsd.objects.DTB2BSALESORDERSIMULATEREQUEST.ISOHEAD xmlHead = objectFactory
					.createDTB2BSALESORDERSIMULATEREQUESTISOHEAD();
			xmlHead.setPURCHNO(null != orderData.getPurchaseOrderNumber() ? orderData.getPurchaseOrderNumber().trim() : "");
			xmlHead.setPURCHNOS(orderData.getCode());
			xmlHead.setSALESORG(b2bUnitData.getSalesOrganisation());
			xmlHead.setDIVISION(b2bUnitData.getDivision());
			xmlHead.setDISTRCHAN(b2bUnitData.getDistributionChannel());
			xmlHead.setDOCTYPE(b2bUnitData.getErpOrderingType());
			// SAP dont need net value to be sent
			xmlHead.setNETVALUE("");
			String currencyIsocode = null;

			//if (null != requestSource && requestSource.equalsIgnoreCase(EnergizerCoreConstants.CART))
			if ((boolean) sessionService.getAttribute("isSalesRepUserLoggedIn"))
			{
				currencyIsocode = b2bUnitData.getCurrencyPreference().getIsocode();
				LOG.info("Simulate request for Sales Rep order !!, sales org = " + b2bUnitData.getSalesOrganisation()
						+ " , currencyIsocode = " + currencyIsocode);
			}
			//else if (null != requestSource && requestSource.equalsIgnoreCase(EnergizerCoreConstants.CHECKOUT))
			else
			{
				currencyIsocode = orderData.getSubTotal().getCurrencyIso();
				LOG.info("Simulate request for Non-Sales Rep order !!, sales org = " + b2bUnitData.getSalesOrganisation()
						+ " , currencyIsocode = " + currencyIsocode);
			}

			xmlHead.setCURRENCY(currencyIsocode);

			final GregorianCalendar c = new GregorianCalendar();
			c.setTime(orderData.getRequestedDeliveryDate());
			XMLGregorianCalendar date2 = null;
			try
			{
				date2 = DatatypeFactory.newInstance().newXMLGregorianCalendar(sdf.format(c.getTime()));
			}
			catch (final DatatypeConfigurationException e)
			{
				LOG.error(e.getMessage());
				e.printStackTrace();
			}
			catch (final NullPointerException ne)
			{
				LOG.error("Null pointer Exception occured ::: " + ne.getMessage());
				ne.printStackTrace();
			}
			catch (final Exception e)
			{
				LOG.error("Exception occured during simulate order marshall ::: " + e.getMessage());
				e.printStackTrace();
			}

			// Added by Soma for Estimated Delivery Date for LATAM Only - START
			if (getSiteUid(new OrderModel()).equalsIgnoreCase(configurationService.getConfiguration().getString(SITE_PERSONALCARE))
					&& !(boolean) sessionService.getAttribute("isSalesRepUserLoggedIn"))
			{

				if ((null != orderData.getContainerVolumeUtilization() && orderData.getContainerVolumeUtilization() >= 100.0)
						|| (null != orderData.getContainerWeightUtilization() && orderData.getContainerWeightUtilization() >= 100.0))
				{
					xmlHead.setSHIPCOND("CL");
				}
				else
				{
					xmlHead.setSHIPCOND("LC");
				}
			}
			// Added by Soma for Estimated Delivery Date for LATAM Only - END

			// Added by Rajasekhar for WeSell - START
			if ((boolean) sessionService.getAttribute("isSalesRepUserLoggedIn"))
			{
				xmlHead.setCREDITLIMIT("");
				xmlHead.setCUSTOMERBALANCE("");
				xmlHead.setCREDITAVAILABLE("");
			}
			// Added by Rajasekhar for WeSell - END
			//LOG.info("simulate -> date2 : " + date2);
			xmlHead.setREQDATEH(date2);

			//LOG.info("simulate -> retrieve date2 : " + xmlHead.getREQDATEH());
			final String siteUid = cmsSiteService.getCurrentSite().getUid();
			final String PERSONALCARE_EMEA = configurationService.getConfiguration().getString("site.personalCareEMEA");
			final String PERSONALCARE = configurationService.getConfiguration().getString("site.personalCare");

			final TSOITEM itemObj = objectFactory.createDTB2BSALESORDERSIMULATEREQUESTTSOITEM();

			if (null != orderData)
			{
				for (final OrderEntryData entry : orderData.getEntries())
				{
					LOG.info("Division for NON PBG products "+ b2bUnitData.getDivision());
					final ProductData productData = entry.getProduct();
					LOG.info(" Is PBG---" +productData.isIsPBG() +"SalesOrganisation  " + b2bUnitData.getSalesOrganisation() +"siteUid" + siteUid +"SalesArea()"+b2bUnitData.getSalesArea());
					LOG.info("IS PBG BOOLEAN "+BooleanUtils.isTrue(productData.isIsPBG()));
					if(BooleanUtils.isTrue(productData.isIsPBG())  && b2bUnitData.getSalesOrganisation() != null && b2bUnitData.getSalesOrganisation().equals("1000") && null != siteUid && null != b2bUnitData.getSalesArea() && siteUid.equalsIgnoreCase(PERSONALCARE) && b2bUnitData.getSalesArea().equalsIgnoreCase("LATAM") ){
                     xmlHead.setDIVISION("40");
					 LOG.info("Division for PBG products - 40");


					}
					final String material = productData.getErpMaterialID();
					final String prodCode = productData.getCode();
					final String plant = productData.getShippingPoint();
					Long quantity = entry.getQuantity();
					final String expectedUnitPrice = entry.getExpectedUnitPrice();
					final ZSDTSOITEM orderEntry = objectFactory.createDTB2BSALESORDERSIMULATEREQUESTTSOITEMZSDTSOITEM();
					// SAP need item number to be sent in multiple of 10's
					orderEntry.setITMNUMBER((entry.getEntryNumber() + 1) * 10);
					orderEntry.setMATERIAL(prodCode);
                    if(null != productData.getPriceUOM()) {
                     orderEntry.setCONDUNIT(productData.getPriceUOM());
                    }
					//LOG.info("expectedUnitPrice ::: " + expectedUnitPrice);

					// Hide Expected unit price for EMEA - START
					if (null != siteUid && !siteUid.equalsIgnoreCase(PERSONALCARE_EMEA))
					{
						if (null != expectedUnitPrice && !StringUtils.isEmpty(expectedUnitPrice))
						{
							//LOG.info("new bigDecimal ::: " + new BigDecimal(expectedUnitPrice));
							orderEntry.setCUSTPRICE(new BigDecimal(expectedUnitPrice));
						}
						else
						{
							//LOG.info("new bigDecimal ::: " + new BigDecimal("0.00"));
							orderEntry.setCUSTPRICE(new BigDecimal("0.00"));
						}
					}
					// Hide Expected unit price for EMEA - START

					String uom = productData.getUom();

					// Checking product uom validation -Start
					if (null == uom || StringUtils.isEmpty(uom))
					{
						emptyUomProducts.add(prodCode);
					}
					//Added Code changes for WeSell Implementation - START

					if (null != uom && !StringUtils.isEmpty(uom) && null != userService.getCurrentUser()
							&& !userService.getCurrentUser().getUid().equalsIgnoreCase("anonymous")
							&& ((boolean) sessionService.getAttribute("isSalesRepUserLoggedIn")))
					{
						orderEntry.setTARGETQTY(quantity);
						orderEntry.setTARGETQU(uom);
					}
					//Added Code changes for WeSell Implementation - END
					else
					{
						// Checking product uom validation-End
						if (null != uom && !StringUtils.isEmpty(uom) && uom.equalsIgnoreCase("EA"))
						{

							LOG.error("We can not simulate the Order for UOM in EA");
							throw new Exception("We can not simulate the Order for UOM in EA");
						}
						if (null != uom && !StringUtils.isEmpty(uom) && !uom.equalsIgnoreCase("CS") && !uom.equalsIgnoreCase("IP"))
						{
							final List<EnergizerProductConversionFactorModel> conversionList = getConversionModelList(prodCode);
							for (final EnergizerProductConversionFactorModel enrProdConversion : conversionList)
							{
								if (uom.equalsIgnoreCase("PAL"))
								{
									final Integer conversionMultiplier = getAlernateConversionMultiplierForUOM(conversionList, "PAL");
									final Integer conversionMultiplierForCase = getAlernateConversionMultiplierForUOM(conversionList,
											"CS");
									Integer quantityInInt = 0;
									if (conversionMultiplier != null)
									{
										quantityInInt = conversionMultiplier / conversionMultiplierForCase;
									}
									else
									{
										sendEmail(orderData, material, prodCode, "PAL");
									}
									quantity = quantityInInt.longValue() * quantity;
									uom = "CS";
								}
								if (uom.equalsIgnoreCase("LAY"))
								{
									final Integer conversionMultiplier = getAlernateConversionMultiplierForUOM(conversionList, "LAY");
									final Integer conversionMultiplierForCase = getAlernateConversionMultiplierForUOM(conversionList,
											"CS");
									Integer quantityinInt = 0;
									//quantityinInt = conversionMultiplier / conversionMultiplierForCase;
									if (conversionMultiplier != null)
									{
										quantityinInt = conversionMultiplier / conversionMultiplierForCase;
									}
									else
									{
										sendEmail(orderData, material, prodCode, "LAY");
									}
									quantity = quantityinInt.longValue() * quantity;
									uom = "CS";
								}
							}
						}
						orderEntry.setTARGETQTY(quantity);
						orderEntry.setTARGETQU(null != uom ? uom : "");
					}
					orderEntry.setPLANT(null != plant ? plant : "");
					// SAP dont need net value to be sent
					orderEntry.setNETVALUE("");

					// Added by Rajasekhar for WeSell - START
					if ((boolean) sessionService.getAttribute("isSalesRepUserLoggedIn"))
					{
						orderEntry.setINVENTORYAVAILABLE(0L);
						orderEntry.setDISCOUNTAMT("");
						orderEntry.setDISCOUNTPERCENT("");
					}
					// Added by Rajasekhar for WeSell - END

					itemObj.getZSDTSOITEM().add(orderEntry);
				}
			}
			LOG.info("Division for NON PBG products "+ b2bUnitData.getDivision());

            if ( b2bUnitData.getSalesOrganisation() != null && b2bUnitData.getSalesOrganisation().equals("1000") && null != b2bUnitData.getSalesArea() && null != b2bUnitData.getSalesOrganisation()  && b2bUnitData.getSalesOrganisation().equalsIgnoreCase("1000") && b2bUnitData.getSalesArea().equalsIgnoreCase("LATAM")){
             xmlHead.setDIVISION("40");
				LOG.info("Division for PBG products "+ b2bUnitData.getDivision());
			}
			final TSOPARTNER partnerObj = objectFactory.createDTB2BSALESORDERSIMULATEREQUESTTSOPARTNER();
			ZSDTSOPART partner = null;

			// If SoldToAddressID == ShipToAddressId, then add only 'SP' in the request. Else, add both 'SP'(SoldToAddressId) & 'SH'(ErpAddressId/ShipToId)
			if (null != siteUid && siteUid.equalsIgnoreCase(PERSONALCARE))
			{
				LOG.info("Setting PART_ROLE & PART_NUMBER for LATAM !");
				String erpAddressId = StringUtils.EMPTY;
				String soldToAddressId = StringUtils.EMPTY;
				if (null != requestSource && requestSource.equalsIgnoreCase(EnergizerCoreConstants.CHECKOUT))
				{
					LOG.info("Simulate request from '" + requestSource + "' page ...");

					if (null != orderData.getDeliveryAddress())
					{
						erpAddressId = orderData.getDeliveryAddress().getErpAddressId();
						soldToAddressId = orderData.getDeliveryAddress().getSoldToAddressId();
						LOG.info("erpAddressId ::: '" + erpAddressId + "' , soldToAddressId ::: '" + soldToAddressId + "' ");
						if (null != erpAddressId && null != soldToAddressId && erpAddressId.equalsIgnoreCase(soldToAddressId))
						{
							LOG.info("erpAddressId && soldToAddressId are same, so adding only 'SP' in the PART ...");
							// Adding SoldToAddressID in the Request XML - START
							partner = objectFactory.createDTB2BSALESORDERSIMULATEREQUESTTSOPARTNERZSDTSOPART();
							partner.setPARTNROLE("SP");
							if (orderData.getDeliveryAddress() != null)
							{
								partner.setPARTNNUMB(orderData.getDeliveryAddress().getSoldToAddressId());
							}
							partnerObj.getZSDTSOPART().add(partner);
							// Adding SoldToAddressID in the Request XML - END
						}
						else if (null != erpAddressId && null != soldToAddressId && !erpAddressId.equalsIgnoreCase(soldToAddressId))
						{
							LOG.info("erpAddressId & soldToAddressId are not same, so adding both 'SP' and 'SH' in the PART ...");
							// Adding SoldToAddressID in the Request XML - START
							partner = objectFactory.createDTB2BSALESORDERSIMULATEREQUESTTSOPARTNERZSDTSOPART();
							partner.setPARTNROLE("SP");
							if (orderData.getDeliveryAddress() != null)
							{
								partner.setPARTNNUMB(orderData.getDeliveryAddress().getSoldToAddressId());
							}
							partnerObj.getZSDTSOPART().add(partner);
							// Adding SoldToAddressID in the Request XML - END

							// Adding ShipToId(ErpAddressId/ShipToId) in the Request XML - START
							partner = objectFactory.createDTB2BSALESORDERSIMULATEREQUESTTSOPARTNERZSDTSOPART();
							partner.setPARTNROLE("SH");
							partner.setPARTNNUMB(orderData.getDeliveryAddress().getErpAddressId());
							partnerObj.getZSDTSOPART().add(partner);
							// Adding ShipToId(ErpAddressId/ShipToId) in the Request XML - END
						}
						else
						{
							LOG.info("Either erpAddressId or soldToAddressId is null ...");
						}
					}
					else
					{
						LOG.info("This order doesn't have delivery address attached, delivery address is null !");
					}
				}
				else if (null != requestSource && requestSource.equalsIgnoreCase(EnergizerCoreConstants.CART))
				{
					LOG.info("Simulate request from '" + requestSource + "' page ...");

					final List<AddressData> b2bUnitAddresses = orderData.getB2bUnit().getAddresses();
					if (null != b2bUnitAddresses && !b2bUnitAddresses.isEmpty())
					{
						if (null != b2bUnitAddresses.get(0))
						{
							soldToAddressId = b2bUnitAddresses.get(0).getSoldToAddressId();
						}
					}

					LOG.info(
							"Adding ONLY the 'SP' as a partner number for the cart page request to get realtime prices from SAP ... ");

					LOG.info("soldToAddressId ::: " + soldToAddressId);
					// Adding SoldToAddressID in the Request XML - START
					partner = objectFactory.createDTB2BSALESORDERSIMULATEREQUESTTSOPARTNERZSDTSOPART();
					partner.setPARTNROLE("SP");
					partner.setPARTNNUMB(null != soldToAddressId ? soldToAddressId : "");
					partnerObj.getZSDTSOPART().add(partner);
					// Adding SoldToAddressID in the Request XML - END

					// Adding Payer(B2B Unit ID) for WeSell - START
					partner = objectFactory.createDTB2BSALESORDERSIMULATEREQUESTTSOPARTNERZSDTSOPART();
					partner.setPARTNROLE("PY");
					partner.setPARTNNUMB(null != orderData.getB2bUnit() ? orderData.getB2bUnit().getUid() : "");
					partnerObj.getZSDTSOPART().add(partner);
					// Adding Payer(B2B Unit ID) for WeSell - END
				}
				else
				{
					LOG.info("No source ...");
				}
				if ((boolean) sessionService.getAttribute("isSalesRepUserLoggedIn"))
				{
					if (null == soldToAddressId || soldToAddressId.isEmpty())
					{

						throw new AddressException("Sold To/Ship To number is not available for this Customer - "
								+ (null != orderData.getB2bUnit() ? orderData.getB2bUnit().getUid() : ""));
					}
				}

			}
			else if (null != siteUid && siteUid.equalsIgnoreCase(PERSONALCARE_EMEA))
			{
				LOG.info("Setting PART_ROLE & PART_NUMBER for EMEA !");
				/*
				 * LOG.info(
				 * "EMEA has only single Ship To and so SoldTo & ShiptTo are same as of now, so adding only SoldTo in the 'SP' !"
				 * );
				 */
				LOG.info("ErpAddressId ::: '" + orderData.getDeliveryAddress().getErpAddressId() + "' , SoldToAddressId ::: '"
						+ orderData.getDeliveryAddress().getSoldToAddressId() + "' !");

				String erpAddressId = StringUtils.EMPTY;
				String soldToAddressId = StringUtils.EMPTY;

				if (null != orderData.getDeliveryAddress())
				{
					erpAddressId = orderData.getDeliveryAddress().getErpAddressId();
					soldToAddressId = orderData.getDeliveryAddress().getSoldToAddressId();

					if (null != erpAddressId && null != soldToAddressId && (erpAddressId.equalsIgnoreCase(soldToAddressId)))
					{
						// Adding SoldToAddressID in the Request XML - START
						partner = objectFactory.createDTB2BSALESORDERSIMULATEREQUESTTSOPARTNERZSDTSOPART();
						partner.setPARTNROLE("SP");
						if (orderData.getDeliveryAddress() != null)
						{
							partner.setPARTNNUMB(soldToAddressId);
						}
						partnerObj.getZSDTSOPART().add(partner);
						// Adding only SoldToAddressID in the Request XML - END

					}
					else if (null != erpAddressId && null != soldToAddressId && !(erpAddressId.equalsIgnoreCase(soldToAddressId)))
					{

						// Adding SoldToAddressID in the Request XML - START
						partner = objectFactory.createDTB2BSALESORDERSIMULATEREQUESTTSOPARTNERZSDTSOPART();
						partner.setPARTNROLE("SP");
						if (orderData.getDeliveryAddress() != null)
						{
							partner.setPARTNNUMB(soldToAddressId);
						}
						partnerObj.getZSDTSOPART().add(partner);
						// Adding only SoldToAddressID in the Request XML - END

						// Added by pavani for setting ShipTo in request XML - START
						partner = objectFactory.createDTB2BSALESORDERSIMULATEREQUESTTSOPARTNERZSDTSOPART();
						partner.setPARTNROLE("SH");
						partner.setPARTNNUMB(erpAddressId);
						partnerObj.getZSDTSOPART().add(partner);
						// Added by pavani for setting ShipTo in request XML - END
						// Set SoldTo and ShipTo in the request XML END//
					}
					else
					{
						LOG.info("Either erpAddressId or soldToAddressId is null ...");
					}
				}
				else
				{
					LOG.info("This order doesn't have delivery address attached, delivery address is null !");
				}
			}
			LOG.info("Partner Array size in Simulate ::: " + partnerObj.getZSDTSOPART().size());

			final TTSOCONDITIONS conditionsObj = objectFactory.createDTB2BSALESORDERSIMULATEREQUESTTTSOCONDITIONS();
			//final ZSDTSOCONDITIONS conditions = objectFactory.createDTB2BSALESORDERSIMULATEREQUESTTTSOCONDITIONSZSDTSOCONDITIONS();
			//conditionsObj.getZSDTSOCONDITIONS().add(conditions);

			final MESSAGETABLE messageObj = objectFactory.createDTB2BSALESORDERSIMULATEREQUESTMESSAGETABLE();
			//final BAPIRET2 messages = objectFactory.createDTB2BSALESORDERSIMULATEREQUESTMESSAGETABLEBAPIRET2();
			//messageObj.getBAPIRET2().add(messages);

			final ORDERINCOMPLETE orderIncompleteObj = objectFactory.createDTB2BSALESORDERSIMULATEREQUESTORDERINCOMPLETE();
			//final BAPIINCOMP orderIncomp = objectFactory.createDTB2BSALESORDERSIMULATEREQUESTORDERINCOMPLETEBAPIINCOMP();
			//orderIncompleteObj.getBAPIINCOMP().add(orderIncomp);

			// Added by Soma for Order Comments - START
			final int orderCommentsMaxLength = configurationService.getConfiguration().getInt("order.commments.max.length", 264);
			final int textLineMaxLength = configurationService.getConfiguration().getInt("text.line.max.length", 132);

			final TTEXTS textObj = objectFactory.createDTB2BSALESORDERSIMULATEREQUESTTTEXTS();
			BAPISDTEXT text = null;
			String textLine = null;

			String orderComments = orderData.getOrderComments();

			final String doc_No = configurationService.getConfiguration().getString(DOC_NUMBER, StringUtils.EMPTY);
			final Integer itm_No = configurationService.getConfiguration().getInteger(ITM_NUMBER, 00000);
			final String text_Id = getSiteSpecificTextId(new OrderModel());
			final String lang_U = configurationService.getConfiguration().getString(LANGU, "E");
			final String format_Col = configurationService.getConfiguration().getString(FORMAT_COL, "*");

			if (null != orderComments && !orderComments.isEmpty() && (orderComments.length() > orderCommentsMaxLength))
			{
				throw new ValidationException("Order comments cannot exceed 264 characters.");
			}
			if (null != orderComments && !orderComments.isEmpty())
			{
				// Convert normal String to UTF-8
				orderComments = convertStringToUTF8(orderComments);

				System.out.println("Order Comments length ::: " + orderComments.length() + " , " + orderComments);
				if (orderComments.length() > (textLineMaxLength))
				{
					//substring(0, 132)
					final String text1 = orderComments.substring(0, textLineMaxLength);
					LOG.info("text1 length ::: " + text1.length() + " , " + text1);

					//substring(132, 264)
					final String text2 = orderComments.substring(textLineMaxLength, orderCommentsMaxLength);
					LOG.info("text2 length ::: " + text2.length() + " , " + text2);

					//substring(264)
					final String text3 = orderComments.substring(orderCommentsMaxLength);
					LOG.info("text3 length ::: " + text3.length() + " , " + text3);

					for (int i = 1; i <= 3; i++)
					{
						text = objectFactory.createDTB2BSALESORDERSIMULATEREQUESTTTEXTSBAPISDTEXT();
						textLine = null;

						if (i == 1 && null != text1 && StringUtils.isNotEmpty(text1) && text1.length() > 0)
						{
							textLine = text1;
						}
						else if (i == 2 && null != text2 && StringUtils.isNotEmpty(text2) && text2.length() > 0)
						{
							textLine = text2;
						}
						else if (i == 3 && null != text3 && StringUtils.isNotEmpty(text3))
						{
							if (text3.length() > 0)
							{
								textLine = text3;
							}
							else
							{
								break;
							}
						}

						if (null != textLine && StringUtils.isNotEmpty(textLine))
						{
							LOG.info("textLine " + i + " : " + textLine);

							text.setDOCNUMBER(doc_No);
							text.setITMNUMBER(itm_No);
							text.setTEXTID(text_Id);
							text.setLANGU(lang_U);
							text.setFORMATCOL(format_Col);
							text.setTEXTLINE(textLine);

							textObj.getBAPISDTEXT().add(text);
						}
					}
				}
				else
				{
					text = objectFactory.createDTB2BSALESORDERSIMULATEREQUESTTTEXTSBAPISDTEXT();

					textLine = orderComments;

					if (null != textLine && StringUtils.isNotEmpty(textLine.toString()))
					{
						text.setDOCNUMBER(doc_No);
						text.setITMNUMBER(itm_No);
						text.setTEXTID(text_Id);
						text.setLANGU(lang_U);
						text.setFORMATCOL(format_Col);
						text.setTEXTLINE(textLine);

						textObj.getBAPISDTEXT().add(text);
					}
				}
			}
			// Added by Soma for Order Comments - END

			xmlRoot.setISOHEAD(xmlHead);
			xmlRoot.setMESSAGETABLE(messageObj);
			xmlRoot.setORDERINCOMPLETE(orderIncompleteObj);
			xmlRoot.setTSOITEM(itemObj);
			xmlRoot.setTSOPARTNER(partnerObj);
			xmlRoot.setTTSOCONDITIONS(conditionsObj);
			xmlRoot.setTTEXTS(textObj);

			final JAXBElement<DTB2BSALESORDERSIMULATEREQUEST> xmlRootJAXBElement = objectFactory
					.createMTB2BSALESORDERSIMULATEREQUEST(xmlRoot);

			final Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			marshaller.setProperty(Marshaller.JAXB_ENCODING,
					configurationService.getConfiguration().getString(EnergizerCoreConstants.ORDER_SIMULATE_CHARSET_NAME, "UTF-8"));

			stringWriter = new StringWriter();
			marshaller.marshal(xmlRootJAXBElement, stringWriter);
			parsedXML = stringWriter.toString();
			LOG.info("Simulate order request xml is as below ");
			LOG.info(parsedXML);
		}
		catch (final JAXBException jaxbException)
		{
			LOG.error(jaxbException.getMessage());
			throw jaxbException;
		}
		catch (final Exception e)
		{
			LOG.error("Exception occured during simulate order marshall .... " + e);
			throw e;
		}
		if (null != emptyUomProducts && emptyUomProducts.size() > 0)
		{

			LOG.info("UOM empty for " + emptyUomProducts.size() + " products ==> " + emptyUomProducts
					+ ". So, UOM is not added to the simulate request XML !");
			return "UOM Empty";
		}
		return parsedXML;
	}

	private void sendEmail(final AbstractOrderData orderData, final String material, final String prodCode, final String uom)
			throws Exception
	{
		{
			LOG.info("Could not find the conversion factor in cases(CS)" + material);
			//final BaseSiteModel currentBaseSite = baseSiteService.getCurrentBaseSite();

			final String siteUid = getSiteUid(new OrderModel());

			String supportEmail = configurationService.getConfiguration().getString("energizer.customer.support.to.email." + siteUid,
					"test@test.com");
			final String mailEnvironment = configurationService.getConfiguration().getString("mail.environment.stuck.orders");

			final EmailAddressModel toAddress = emailService.getOrCreateEmailAddressForEmail(supportEmail, "Hybris Support Mail");
			supportEmail = configurationService.getConfiguration().getString("energizer.customer.support.from.email." + siteUid,
					"test@test.com");
			final EmailAddressModel fromAddress = emailService.getOrCreateEmailAddressForEmail(supportEmail, "Hybris Support Mail");
			final StringBuilder emailBody = new StringBuilder();
			final StringBuilder emailSubject = new StringBuilder();
			emailSubject.append("Simulation Failed in " + mailEnvironment.toUpperCase() + " Environment !");
			emailBody.append("Hi <br/>");
			emailBody.append("While creating data for simulating order in hybris " + "<br/>");
			emailBody.append(
					"we could not find the conversions for the sales UOM " + uom + " for the material id " + prodCode + "<br/>");
			emailBody.append("This is an automatically generated email. Please do not reply to this mail");
			final EmailMessageModel message = emailService.createEmailMessage(Arrays.asList(toAddress), null, null, fromAddress, "",
					emailSubject.toString(), emailBody.toString(), null);
			LOG.error("Failed to simulate order \n ");
			emailService.send(message);
			throw new Exception("No conversion found in Cases for material " + material);
		}
	}

	/**
	 * @param code
	 * @return
	 */
	private List<EnergizerProductConversionFactorModel> getConversionModelList(final String prodCode)
	{
		final EnergizerProductModel enrProdModel = (EnergizerProductModel) productService.getProductForCode(prodCode);
		final List<EnergizerProductConversionFactorModel> conversionList = enrProdModel.getProductConversionFactors();
		return conversionList;
	}

	@SuppressWarnings("unused")
	private String createOrderMarshall(final OrderModel order) throws Exception
	{
		if (null != order.getPlacedBySalesRep() && order.getPlacedBySalesRep())
		{
			LOG.info("Create order request for Sales Rep order !!, sales org = " + order.getB2bUnit().getSalesOrganisation()
					+ " , currencyIsocode from b2b unit = " + order.getB2bUnit().getCurrencyPreference().getIsocode()
					+ " , currency from order model ::: " + order.getCurrency().getIsocode());
		}
		else if (null != order.getPlacedBySalesRep() && !order.getPlacedBySalesRep())
		{
			LOG.info("Create order request for a Non-Sales Rep order !!, sales org = " + order.getB2bUnit().getSalesOrganisation()
					+ " , currencyIsocode from b2b unit = " + order.getB2bUnit().getCurrencyPreference().getIsocode()
					+ " , currency from order model ::: " + order.getCurrency().getIsocode());
		}

		final com.energizer.core.createorder.jaxb.xsd.objects.ObjectFactory objectFactory = new com.energizer.core.createorder.jaxb.xsd.objects.ObjectFactory();
		StringWriter stringWriter = new StringWriter();
		String parsedXML = null;
		JAXBContext context;
		final Set<String> emptyUomProducts = new HashSet<String>();
		try
		{
			context = JAXBContext.newInstance(ORDER_CREATE_CONTEXT_PATH);
			final DTB2BSALESORDERCREATEREQUEST xmlRoot = objectFactory.createDTB2BSALESORDERCREATEREQUEST();
			final EnergizerB2BUnitModel b2bUnitData = order.getB2bUnit();
			final ISOHEAD xmlHead = objectFactory.createDTB2BSALESORDERCREATEREQUESTISOHEAD();
			// SAP dont need net value to be sent
			final GregorianCalendar c = new GregorianCalendar();

			final Calendar ca = Calendar.getInstance();
			ca.setTime(new Date());
			ca.add(Calendar.DATE, order.getLeadTime());
			Date reqDate = order.getRequestedDeliveryDate();
			if (reqDate.compareTo(ca.getTime()) < 0)
			{
				reqDate = ca.getTime();
			}
			c.setTime(reqDate);
			XMLGregorianCalendar date2 = null;
			try
			{
				date2 = DatatypeFactory.newInstance().newXMLGregorianCalendar(sdf.format(c.getTime()));
			}
			catch (final DatatypeConfigurationException e)
			{
				LOG.error(e.getMessage());
			}

			// Added by Soma for Estimated Delivery Date for LATAM Only - START
			if (getSiteUid(order).equalsIgnoreCase(configurationService.getConfiguration().getString(SITE_PERSONALCARE))
					&& (null == order.getPlacedBySalesRep() || (null != order.getPlacedBySalesRep() && !order.getPlacedBySalesRep())))
			{
				if ((null != order.getContainerVolumeUtilization() && order.getContainerVolumeUtilization() >= 100.0)
						|| (null != order.getContainerWeightUtilization() && order.getContainerWeightUtilization() >= 100.0))
				{
					xmlHead.setSHIPCOND("CL");
				}
				else
				{
					xmlHead.setSHIPCOND("LC");
				}
			}
			// Added by Soma for Estimated Delivery Date for LATAM Only - END

			xmlHead.setDOCTYPE(b2bUnitData.getErpOrderingType());
			xmlHead.setSALESORG(b2bUnitData.getSalesOrganisation());
			xmlHead.setDISTRCHAN(b2bUnitData.getDistributionChannel());
			xmlHead.setDIVISION(b2bUnitData.getDivision());
			xmlHead.setPURCHNO(order.getPurchaseOrderNumber());
			xmlHead.setPURCHNOS(order.getCode());
			xmlHead.setNETVALUE("");
			//LOG.info("create -> date2 : " + date2);
			xmlHead.setREQDATEH(date2);

			//LOG.info("create -> retrieve date2 : " + xmlHead.getREQDATEH());
			xmlHead.setCURRENCY(order.getCurrency().getIsocode());

			final com.energizer.core.createorder.jaxb.xsd.objects.DTB2BSALESORDERCREATEREQUEST.TSOITEM itemObj = objectFactory
					.createDTB2BSALESORDERCREATEREQUESTTSOITEM();

			final String siteUid = order.getSite().getUid();
			final String PERSONALCARE_EMEA = configurationService.getConfiguration().getString("site.personalCareEMEA");
			final String PERSONALCARE = configurationService.getConfiguration().getString("site.personalCare");

			for (final AbstractOrderEntryModel orderEntry : order.getEntries())
			{
				final EnergizerProductModel productData = (EnergizerProductModel) orderEntry.getProduct();
				final String material = productData.getCode();
				LOG.info("DIVISION For NON PBG Products"+b2bUnitData.getDivision());

				LOG.info("get Is PBG---" +productData.getIsPBG()+"SalesOrganisation  " + b2bUnitData.getSalesOrganisation() +"siteUid" + siteUid +"SalesArea()"+b2bUnitData.getSalesArea());
                LOG.info("IS PBG BOOLEAN "+BooleanUtils.isTrue(productData.getIsPBG()));
				if(BooleanUtils.isTrue(productData.getIsPBG())  &&  b2bUnitData.getSalesOrganisation() != null && b2bUnitData.getSalesOrganisation().equals("1000") && null != siteUid && null != b2bUnitData.getSalesArea() && siteUid.equalsIgnoreCase(PERSONALCARE) && b2bUnitData.getSalesArea().equalsIgnoreCase("LATAM") ){
                xmlHead.setDIVISION("40");
				LOG.info("Division for PBG Product -- 40");

                }
				final String code = productData.getCode();

				//final String plant = productData.getProductCMIR().get(0).getShippingPoint();
				final EnergizerCMIRModel energizerCMIRModel = energizerProductService.getEnergizerCMIR(material,
						order.getB2bUnit().getUid());
				final String plant = energizerCMIRModel.getShippingPoint();

				Long quantity = orderEntry.getQuantity();

				BigDecimal expectedUnitPrice = new BigDecimal("0.00");

				//LOG.info("initial expectedUnitPrice ::: " + expectedUnitPrice);

				if (null != orderEntry.getExpectedUnitPrice())
				{
					/*
					 * LOG.info("orderEntry.getExpectedUnitPrice() ::: for this product " + material + " === " +
					 * orderEntry.getExpectedUnitPrice());
					 */

					final String eupFromDB = new BigDecimal(orderEntry.getExpectedUnitPrice()).toPlainString();

					//LOG.info("eupFromDB ::: " + eupFromDB);

					/*
					 * LOG.info("eupFromDB new scale plain ::: " + new
					 * BigDecimal(orderEntry.getExpectedUnitPrice()).setScale(2, RoundingMode.CEILING).toPlainString());
					 */

					expectedUnitPrice = new BigDecimal(eupFromDB);

					//LOG.info("expectedUnitPrice new ::: " + expectedUnitPrice);
				}

				final com.energizer.core.createorder.jaxb.xsd.objects.DTB2BSALESORDERCREATEREQUEST.TSOITEM.ZSDTSOITEM orderEntries = objectFactory
						.createDTB2BSALESORDERCREATEREQUESTTSOITEMZSDTSOITEM();
				// SAP need item number to be sent in multiple of 10's
				orderEntries.setITMNUMBER((orderEntry.getEntryNumber() + 1) * 10);
				orderEntries.setMATERIAL(code);
				
				


				//final List<EnergizerCMIRModel> CMIRModelList = productData.getProductCMIR();
				//String uom = "";
				//for (final EnergizerCMIRModel CMIRModel : CMIRModelList)
				//{
				//if (CMIRModel.getB2bUnit().getUid().equalsIgnoreCase(b2bUnitData.getUid()))
				//{
				//uom = CMIRModel.getUom();
				//}
				//}
				String uom = energizerCMIRModel.getUom();
				// Checking product uom validation -Start
				if (null == uom || StringUtils.isEmpty(uom))
				{
					emptyUomProducts.add(code);
				}
				// Checking product uom validation -End

				//Added Code changes for WeSell Implementation - START
				if (null != uom && !StringUtils.isEmpty(uom) && order.getPlacedBySalesRep())
				{
					orderEntries.setTARGETQTY(quantity);
					orderEntries.setTARGETQU(uom);
				}
				//Added Code changes for WeSell Implementation - END
				else
				{
					if (null != uom && !StringUtils.isEmpty(uom) && uom.equalsIgnoreCase("EA"))
					{
						LOG.error("We can not simulate the Order for UOM in EA");
						throw new Exception("We can not simulate the Order for UOM in EA");
					}
					if (null != uom && !StringUtils.isEmpty(uom) && !uom.equalsIgnoreCase("CS") && !uom.equalsIgnoreCase("IP"))
					{
						final List<EnergizerProductConversionFactorModel> conversionList = productData.getProductConversionFactors();
						for (final EnergizerProductConversionFactorModel enrProdConversion : conversionList)
						{
							if (uom.equalsIgnoreCase("PAL"))
							{
								final Integer conversionMultiplier = getAlernateConversionMultiplierForUOM(conversionList, "PAL");
								final Integer conversionMultiplierForCase = getAlernateConversionMultiplierForUOM(conversionList, "CS");
								Integer quantityInInt = 0;
								if (conversionMultiplierForCase != null)
								{
									quantityInInt = conversionMultiplier / conversionMultiplierForCase;
								}
								else
								{
									LOG.info("Could not find the conversion factor in cases(CS)" + material);
									throw new Exception("No converion found in Cases for material " + material);
								}
								quantity = quantityInInt.longValue() * quantity;
								uom = "CS";
							}
							if (uom.equalsIgnoreCase("LAY"))
							{
								final Integer conversionMultiplier = getAlernateConversionMultiplierForUOM(conversionList, "LAY");
								final Integer conversionMultiplierForCase = getAlernateConversionMultiplierForUOM(conversionList, "CS");
								Integer quantityinInt = conversionMultiplier / conversionMultiplierForCase;
								if (conversionMultiplierForCase != null)
								{
									quantityinInt = conversionMultiplier / conversionMultiplierForCase;
								}
								else
								{
									LOG.info("Could not find the conversion factor in cases(CS)" + material);
									throw new Exception("No converion found in Cases for material " + material);
								}
								quantity = quantityinInt.longValue() * quantity;
								uom = "CS";
							}
						}
					}
					orderEntries.setTARGETQTY(quantity);
					orderEntries.setTARGETQU(uom);
				}
				orderEntries.setPLANT(plant);

				// Hide Expected unit price for EMEA - START

				if (null != siteUid && !siteUid.equalsIgnoreCase(PERSONALCARE_EMEA))
				{
					if (null != orderEntry.getExpectedUnitPrice())
					{
						orderEntries.setCUSTPRICE(new BigDecimal(orderEntry.getExpectedUnitPrice()).setScale(2, RoundingMode.CEILING));
					}
					else
					{
						orderEntries.setCUSTPRICE(new BigDecimal(0).setScale(2, RoundingMode.CEILING));
					}
				}
				// Hide Expected unit price for EMEA - END
				// SAP dont need net value to be sent
				orderEntries.setNETVALUE("");

				itemObj.getZSDTSOITEM().add(orderEntries);
			}
			LOG.info("Division for NON PBG products"+ b2bUnitData.getDivision());
			if (b2bUnitData.getSalesOrganisation() != null && b2bUnitData.getSalesOrganisation().equals("1000") && null != b2bUnitData.getSalesArea() && null != b2bUnitData.getSalesOrganisation()  && b2bUnitData.getSalesOrganisation().equalsIgnoreCase("1000") && b2bUnitData.getSalesArea().equalsIgnoreCase("LATAM")){
             xmlHead.setDIVISION("40");
			 LOG.info("Division for PBG products"+ b2bUnitData.getDivision());

			}
			final com.energizer.core.createorder.jaxb.xsd.objects.DTB2BSALESORDERCREATEREQUEST.TSOPARTNER partnerObj = objectFactory
					.createDTB2BSALESORDERCREATEREQUESTTSOPARTNER();
			com.energizer.core.createorder.jaxb.xsd.objects.DTB2BSALESORDERCREATEREQUEST.TSOPARTNER.ZSDTSOPART partner = null;

			// If SoldToAddressID == ShipToAddressId, then add only 'SP' in the request. Else, add both 'SP'(SoldToAddressId) & 'SH'(ErpAddressId/ShipToId)
			if (null != siteUid && siteUid.equalsIgnoreCase(PERSONALCARE))
			{
				LOG.info("Setting PART_ROLE & PART_NUMBER for LATAM !");
				String erpAddressId = StringUtils.EMPTY;
				String soldToAddressId = StringUtils.EMPTY;

				if (null != order.getDeliveryAddress())
				{
					erpAddressId = order.getDeliveryAddress().getErpAddressId();
					soldToAddressId = order.getDeliveryAddress().getSoldToAddressId();
					LOG.info("erpAddressId ::: '" + erpAddressId + "' , soldToAddressId ::: '" + soldToAddressId + "' ");
					if (null != erpAddressId && null != soldToAddressId && erpAddressId.equalsIgnoreCase(soldToAddressId))
					{
						LOG.info("erpAddressId && soldToAddressId are same, so adding only 'SP' in the PART ...");
						// Adding SoldToAddressID in the Request XML - START
						partner = objectFactory.createDTB2BSALESORDERCREATEREQUESTTSOPARTNERZSDTSOPART();
						partner.setPARTNROLE("SP");
						if (order.getDeliveryAddress() != null)
						{
							partner.setPARTNNUMB(order.getDeliveryAddress().getSoldToAddressId());
						}
						partnerObj.getZSDTSOPART().add(partner);
						// Adding SoldToAddressID in the Request XML - END
					}
					else if (null != erpAddressId && null != soldToAddressId && !erpAddressId.equalsIgnoreCase(soldToAddressId))
					{
						LOG.info("erpAddressId & soldToAddressId are not same, so adding both 'SP' and 'SH' in the PART ...");
						// Adding SoldToAddressID in the Request XML - START
						partner = objectFactory.createDTB2BSALESORDERCREATEREQUESTTSOPARTNERZSDTSOPART();
						partner.setPARTNROLE("SP");
						if (order.getDeliveryAddress() != null)
						{
							partner.setPARTNNUMB(order.getDeliveryAddress().getSoldToAddressId());
						}
						partnerObj.getZSDTSOPART().add(partner);
						// Adding SoldToAddressID in the Request XML - END

						// Adding ShipToId(ErpAddressId/ShipToId) in the Request XML - START
						partner = objectFactory.createDTB2BSALESORDERCREATEREQUESTTSOPARTNERZSDTSOPART();
						partner.setPARTNROLE("SH");
						partner.setPARTNNUMB(order.getDeliveryAddress().getErpAddressId());
						partnerObj.getZSDTSOPART().add(partner);
						// Adding ShipToId(ErpAddressId/ShipToId) in the Request XML - END
					}
					else
					{
						LOG.info("Either erpAddressId or soldToAddressId is null ...");
					}
				}
				else
				{
					LOG.info("This order doesn't have delivery address attached, delivery address is null !");
				}
			}
			else if (null != siteUid && siteUid.equalsIgnoreCase(PERSONALCARE_EMEA))
			{
				LOG.info("Setting PART_ROLE & PART_NUMBER for EMEA !");
				/*-LOG.info(
						"EMEA has only single Ship To and so SoldTo & ShiptTo are same as of now, so adding only SoldTo in the 'SP' !");*/
				String erpAddressId = StringUtils.EMPTY;
				String soldToAddressId = StringUtils.EMPTY;

				if (null != order.getDeliveryAddress())
				{
					erpAddressId = order.getDeliveryAddress().getErpAddressId();
					soldToAddressId = order.getDeliveryAddress().getSoldToAddressId();
					LOG.info("erpAddressId ::: '" + erpAddressId + "' , soldToAddressId ::: '" + soldToAddressId + "' ");
					if (null != erpAddressId && null != soldToAddressId && erpAddressId.equalsIgnoreCase(soldToAddressId))
					{
						LOG.info("erpAddressId && soldToAddressId are same, so adding only 'SP' in the PART ...");
						// Adding SoldToAddressID in the Request XML - START
						partner = objectFactory.createDTB2BSALESORDERCREATEREQUESTTSOPARTNERZSDTSOPART();
						partner.setPARTNROLE("SP");
						if (order.getDeliveryAddress() != null)
						{
							partner.setPARTNNUMB(order.getDeliveryAddress().getSoldToAddressId());
						}
						partnerObj.getZSDTSOPART().add(partner);
						// Adding SoldToAddressID in the Request XML - END
					}
					else if (null != erpAddressId && null != soldToAddressId && !erpAddressId.equalsIgnoreCase(soldToAddressId))
					{
						LOG.info("erpAddressId & soldToAddressId are not same, so adding both 'SP' and 'SH' in the PART ...");
						// Adding SoldToAddressID in the Request XML - START
						partner = objectFactory.createDTB2BSALESORDERCREATEREQUESTTSOPARTNERZSDTSOPART();
						partner.setPARTNROLE("SP");
						if (order.getDeliveryAddress() != null)
						{
							partner.setPARTNNUMB(order.getDeliveryAddress().getSoldToAddressId());
						}
						partnerObj.getZSDTSOPART().add(partner);
						// Adding SoldToAddressID in the Request XML - END

						// Adding ShipToId(ErpAddressId/ShipToId) in the Request XML - START
						partner = objectFactory.createDTB2BSALESORDERCREATEREQUESTTSOPARTNERZSDTSOPART();
						partner.setPARTNROLE("SH");
						partner.setPARTNNUMB(order.getDeliveryAddress().getErpAddressId());
						partnerObj.getZSDTSOPART().add(partner);
						// Adding ShipToId(ErpAddressId/ShipToId) in the Request XML - END
					}
					else
					{
						LOG.info("Either erpAddressId or soldToAddressId is null ...");
					}
				}
				else
				{
					LOG.info("This order doesn't have delivery address attached, delivery address is null !");
				}
			}
			LOG.info("Partner Array size in Create ::: " + partnerObj.getZSDTSOPART().size());

			//partner = objectFactory.createZSDTSOPARTD0B6B6();
			//	partner.setPARTN_ROLE(objectFactory.createZSD_TSOPART_Fa2309PARTN_ROLE("PY"));
			// partner.setPARTN_NUMB(objectFactory.createZSD_TSOPART_Fa2309PARTN_NUMB(orderData.getDeliveryAddress().getErpAddressId()));
			// prtnerArray.getZSD_TSOPART().add(partner);

			final com.energizer.core.createorder.jaxb.xsd.objects.DTB2BSALESORDERCREATEREQUEST.TTSOCONDITIONS conditionsObj = objectFactory
					.createDTB2BSALESORDERCREATEREQUESTTTSOCONDITIONS();
			/*-final com.energizer.core.createorder.jaxb.xsd.objects.DTB2BSALESORDERCREATEREQUEST.TTSOCONDITIONS.ZSDTSOCONDITIONS conditions = objectFactory
					.createDTB2BSALESORDERCREATEREQUESTTTSOCONDITIONSZSDTSOCONDITIONS();
			conditionsObj.getZSDTSOCONDITIONS().add(conditions);*/

			final com.energizer.core.createorder.jaxb.xsd.objects.DTB2BSALESORDERCREATEREQUEST.MESSAGETABLE messageObj = objectFactory
					.createDTB2BSALESORDERCREATEREQUESTMESSAGETABLE();
			/*-final com.energizer.core.createorder.jaxb.xsd.objects.DTB2BSALESORDERCREATEREQUEST.MESSAGETABLE.BAPIRET2 messages = objectFactory
					.createDTB2BSALESORDERCREATEREQUESTMESSAGETABLEBAPIRET2();
			messageObj.getBAPIRET2().add(messages);*/

			final com.energizer.core.createorder.jaxb.xsd.objects.DTB2BSALESORDERCREATEREQUEST.ORDERINCOMPLETE orderIncompleteObj = objectFactory
					.createDTB2BSALESORDERCREATEREQUESTORDERINCOMPLETE();

			/*-final BAPIINCOMP orderIncomp = objectFactory.createDTB2BSALESORDERCREATEREQUESTORDERINCOMPLETEBAPIINCOMP();
			orderIncompleteObj.getBAPIINCOMP().add(orderIncomp);*/

			// Added by Soma for Order Comments - START
			final int orderCommentsMaxLength = configurationService.getConfiguration().getInt("order.commments.max.length", 264);
			final int textLineMaxLength = configurationService.getConfiguration().getInt("text.line.max.length", 132);

			final com.energizer.core.createorder.jaxb.xsd.objects.DTB2BSALESORDERCREATEREQUEST.TTEXTS textObj = objectFactory
					.createDTB2BSALESORDERCREATEREQUESTTTEXTS();

			String orderComments = order.getOrderComments();

			final String doc_No = configurationService.getConfiguration().getString(DOC_NUMBER, StringUtils.EMPTY);
			final Integer itm_No = configurationService.getConfiguration().getInteger(ITM_NUMBER, 00000);
			final String text_Id = getSiteSpecificTextId(order);
			final String lang_U = configurationService.getConfiguration().getString(LANGU, "E");
			final String format_Col = configurationService.getConfiguration().getString(FORMAT_COL, "*");

			com.energizer.core.createorder.jaxb.xsd.objects.DTB2BSALESORDERCREATEREQUEST.TTEXTS.BAPISDTEXT text = null;
			String textLine = null;

			if (null != orderComments && !orderComments.isEmpty())
			{
				// Convert normal String to UTF-8
				orderComments = convertStringToUTF8(orderComments);

				System.out.println("Order Comments length ::: " + orderComments.length() + " , " + orderComments);

				if (orderComments.length() > textLineMaxLength)
				{
					//substring(0, 132)
					final String text1 = orderComments.substring(0, textLineMaxLength);
					LOG.info("text1 length ::: " + text1.length() + " , " + text1);

					//substring(132, 264)
					final String text2 = orderComments.substring(textLineMaxLength, orderCommentsMaxLength);
					LOG.info("text2 length ::: " + text2.length() + " , " + text2);

					//substring(264)
					final String text3 = orderComments.substring(orderCommentsMaxLength);
					LOG.info("text3 length ::: " + text3.length() + " , " + text3);

					for (int i = 1; i <= 3; i++)
					{
						text = objectFactory.createDTB2BSALESORDERCREATEREQUESTTTEXTSBAPISDTEXT();
						textLine = null;

						if (i == 1 && null != text1 && StringUtils.isNotEmpty(text1) && text1.length() > 0)
						{
							textLine = text1;
						}
						else if (i == 2 && null != text2 && StringUtils.isNotEmpty(text2) && text2.length() > 0)
						{
							textLine = text2;
						}
						else if (i == 3 && null != text3 && StringUtils.isNotEmpty(text3))
						{
							if (text3.length() > 0)
							{
								textLine = text3;
							}
							else
							{
								break;
							}
						}

						if (null != textLine && StringUtils.isNotEmpty(textLine))
						{
							text.setDOCNUMBER(doc_No);
							text.setITMNUMBER(itm_No);
							text.setTEXTID(text_Id);
							text.setLANGU(lang_U);
							text.setFORMATCOL(format_Col);
							text.setTEXTLINE(textLine);

							textObj.getBAPISDTEXT().add(text);
						}
					}
				}
				else
				{
					text = objectFactory.createDTB2BSALESORDERCREATEREQUESTTTEXTSBAPISDTEXT();

					textLine = orderComments;

					if (null != textLine && StringUtils.isNotEmpty(textLine))
					{
						text.setDOCNUMBER(doc_No);
						text.setITMNUMBER(itm_No);
						text.setTEXTID(text_Id);
						text.setLANGU(lang_U);
						text.setFORMATCOL(format_Col);
						text.setTEXTLINE(textLine);

						textObj.getBAPISDTEXT().add(text);
					}
				}
			}
			// Added by Soma for Order Comments - END

			xmlRoot.setISOHEAD(xmlHead);
			xmlRoot.setMESSAGETABLE(messageObj);
			xmlRoot.setORDERINCOMPLETE(orderIncompleteObj);
			xmlRoot.setTSOITEM(itemObj);
			xmlRoot.setTSOPARTNER(partnerObj);
			xmlRoot.setTTSOCONDITIONS(conditionsObj);
			xmlRoot.setTTEXTS(textObj);

			final JAXBElement<DTB2BSALESORDERCREATEREQUEST> xmlRootJAXBElement = objectFactory
					.createMTB2BSALESORDERCREATEREQUEST(xmlRoot);

			final Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			marshaller.setProperty(Marshaller.JAXB_ENCODING,
					configurationService.getConfiguration().getString(EnergizerCoreConstants.ORDER_SIMULATE_CHARSET_NAME, "UTF-8"));
			stringWriter = new StringWriter();
			marshaller.marshal(xmlRootJAXBElement, stringWriter);
			parsedXML = stringWriter.toString();
			LOG.info("Order create Request xml is as below ");
			LOG.info(parsedXML);
		}
		catch (final JAXBException jaxbException)
		{
			LOG.error(jaxbException.getMessage());
		}

		if (null != emptyUomProducts && emptyUomProducts.size() > 0)
		{

			LOG.info("UOM empty for " + emptyUomProducts.size() + " products ==> " + emptyUomProducts
					+ ". So, UOM is not added to the create request XML !");
			return "UOM Empty";
		}
		return parsedXML;
	}

	private String invokeRESTCall(final String requestXML, final String option, final OrderModel orderModel,
			final AbstractOrderData orderData) throws HttpClientErrorException, RestClientException
	{
		BaseSiteModel currentBaseSite = null;
		try
		{
			currentBaseSite = baseSiteService.getCurrentBaseSite();
			final RestTemplate restTemplate = new RestTemplate();
			final HttpHeaders headers = new HttpHeaders();
			headers.add("Content-Type", MediaType.APPLICATION_XML.toString());
			headers.add("Accept", MediaType.APPLICATION_XML.toString());
			final HttpEntity formEntity = new HttpEntity<>(requestXML, headers);
			final String simulateTimeOutinSeconds = configurationService.getConfiguration().getString("simulateTimeOutinSeconds",
					"30");
			final int simulateTimeOutSeconds = Integer.parseInt(simulateTimeOutinSeconds);
			((SimpleClientHttpRequestFactory) restTemplate.getRequestFactory()).setReadTimeout(1000 * simulateTimeOutSeconds);

			final String SIMULATE_URL_old = configurationService.getConfiguration().getString("simulateURL");
			final String ORDER_SUBMIT_URL_old = configurationService.getConfiguration().getString("orderSubmitURL");

			if (option.equalsIgnoreCase("simulate"))
			{
				LOG.info("SIMULATE_URL : " + SIMULATE_URL);
				LOG.info("SIMULATE_URL_old : " + SIMULATE_URL_old);
				return getResponse(restTemplate, SIMULATE_URL, formEntity);
			}
			else
			{
				LOG.info("ORDER_SUBMIT_URL : " + ORDER_SUBMIT_URL);
				LOG.info("SIMULATE_URL_old : " + SIMULATE_URL_old);
				return getResponse(restTemplate, ORDER_SUBMIT_URL, formEntity);
			}
		}
		catch (final HttpClientErrorException clientException)
		{

			final String siteUid = getSiteUid(orderModel);

			String supportEmail = configurationService.getConfiguration().getString("energizer.customer.support.to.email." + siteUid,
					"test@test.com");
			final InputStream inputStream = new ByteArrayInputStream(requestXML.getBytes(Charset.forName(
					configurationService.getConfiguration().getString(EnergizerCoreConstants.ORDER_SIMULATE_CHARSET_NAME, "UTF-8"))));
			final DataInputStream dis = new DataInputStream(inputStream);
			final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HHmmss");
			final Date date = new Date();
			final String strDate = dateFormat.format(date);
			String request = requestXML;
			LOG.info("Request XML::::::" + request);
			final String newLine = System.getProperty("line.separator");
			final String user = supportEmail;
			final EmailAddressModel toAddress = emailService.getOrCreateEmailAddressForEmail(supportEmail, "Hybris Test Mail");
			supportEmail = configurationService.getConfiguration().getString("energizer.customer.support.from.email." + siteUid,
					"test@test.com");
			final String mailEnvironment = configurationService.getConfiguration().getString("mail.environment.stuck.orders");
			final EmailAddressModel fromAddress = emailService.getOrCreateEmailAddressForEmail(supportEmail, "Hybris Test Mail");
			final StringBuilder emailBody = new StringBuilder();
			final StringBuilder emailSubject = new StringBuilder();
			//final List<EmailAttachmentModel> attachments = getEmailAttachmentModels(requestXML, null, option, orderModel.getCode());
			List<EmailAttachmentModel> attachments = null;

			if (option.equalsIgnoreCase("simulate"))
			{
				attachments = getEmailAttachmentModels(requestXML, null, option, orderData.getCode());

				emailSubject.append("ERROR: Order simulation failed in SAP");
				emailBody.append("Hi,");
				emailBody.append("<br/>");
				emailBody.append("<br/>");
				emailBody.append("Order simulation in SAP failed because of bad format of xml  " + mailEnvironment.toUpperCase()
						+ " environment, the reason could be either with PI validation issues (or) the SAP response. Please check with PI & SAP team for further action.Request XMLs attached for reference. Details provided below.");
				emailBody.append("<br/>");
				emailBody.append("<br/>");
				emailBody.append("<br/>");
				emailBody.append("Hybris Ref No : " + orderModel.getCode());
				emailBody.append("<br/>");
				emailBody.append(newLine);
				emailBody.append("User ID : " + user);
				emailBody.append("<br/>");
				emailBody.append("Date : " + new Date().toString());
				emailBody.append("<br/>");
				emailBody.append(newLine);
				emailBody.append("<br/>");
				emailBody.append("Exception: HttpClientErrorException <br/>");
				request = orderData.getCode().concat("_simulate_request_".concat(strDate).concat(".xml"));
			}
			else if (option.equalsIgnoreCase("createOrder"))
			{
				attachments = getEmailAttachmentModels(requestXML, null, option, orderData.getCode());

				emailSubject.append("ERROR: Creating order in SAP failed");
				emailBody.append("Hi,");
				emailBody.append("<br/>");
				emailBody.append("<br/>");
				emailBody.append("Creating order in SAP failed because of bad format of xml  " + mailEnvironment.toUpperCase()
						+ " environment, the reason could be either with PI validation issues (or) the SAP response. Please check with PI & SAP team for further action.Request XMLs attached for reference. Details provided below.");
				emailBody.append("<br/>");
				emailBody.append("<br/>");
				emailBody.append("<br/>");
				emailBody.append("Hybris Ref No : " + orderModel.getCode());
				emailBody.append("<br/>");
				emailBody.append(newLine);
				emailBody.append("User ID : " + user);
				emailBody.append("<br/>");
				emailBody.append("Date : " + new Date().toString());
				emailBody.append("<br/>");
				emailBody.append(newLine);
				emailBody.append("<br/>");
				emailBody.append("Exception: HttpClientErrorException <br/>");
				request = orderModel.getCode().concat("_create_request_".concat(strDate).concat(".xml"));
			}
			emailBody.append("<br/>");
			emailBody.append("<br/> Thanks,");
			emailBody.append("<br/> Edgewell Personal Care Portal Team");
			emailBody.append("<br/> Note: This is an automatically generated email. Please do not reply to this mail.");
			if (request != null)
			{

				try
				{
					final EmailAttachmentModel EmailAttachmentModel = emailService.createEmailAttachment(dis, request, MIME_TYPE);


					attachments.add(EmailAttachmentModel);

				}
				catch (final Exception e)
				{
					// YTODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			final EmailMessageModel message = emailService.createEmailMessage(Arrays.asList(toAddress), null, null, fromAddress, "",
					emailSubject.toString(), emailBody.toString() + "<br/>", attachments);
			LOG.error("Failed to simulate order \n " + requestXML);
			emailService.send(message);
			throw clientException;
		}
		catch (final RestClientException restException)
		{
			final String siteUid = getSiteUid(orderModel);

			LOG.error("Failed to simulate order" + restException.getMessage());
			String supportEmail = configurationService.getConfiguration().getString("energizer.customer.support.to.email." + siteUid,
					"test@test.com");
			final EmailAddressModel toAddress = emailService.getOrCreateEmailAddressForEmail(supportEmail, "Hybris Test Mail");
			supportEmail = configurationService.getConfiguration().getString("energizer.customer.support.from.email." + siteUid,
					"test@test.com");
			final String mailEnvironment = configurationService.getConfiguration().getString("mail.environment.stuck.orders");
			final EmailAddressModel fromAddress = emailService.getOrCreateEmailAddressForEmail(supportEmail, "Hybris Test Mail");
			final StringBuilder emailBody = new StringBuilder();
			final StringBuilder emailSubject = new StringBuilder();
			//final List<EmailAttachmentModel> attachments = getEmailAttachmentModels(requestXML, null, option, orderModel.getCode());
			final InputStream inputStream = new ByteArrayInputStream(requestXML.getBytes(Charset.forName(
					configurationService.getConfiguration().getString(EnergizerCoreConstants.ORDER_SIMULATE_CHARSET_NAME, "UTF-8"))));
			final DataInputStream dis = new DataInputStream(inputStream);
			final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HHmmss");
			final Date date = new Date();
			final String strDate = dateFormat.format(date);
			String request = requestXML;
			LOG.info("Request XML::::::" + request);
			final String newLine = System.getProperty("line.separator");
			final String user = supportEmail;
			List<EmailAttachmentModel> attachments = null;
			if (option.equalsIgnoreCase("simulate"))
			{
				attachments = getEmailAttachmentModels(requestXML, null, option, orderData.getCode());

				emailSubject.append("ERROR: Order simulation failed in SAP");
				emailBody.append("Hi,");
				emailBody.append("<br/>");
				emailBody.append("<br/>");
				emailBody.append("Order simulation in SAP failed because of bad format of xml  " + mailEnvironment.toUpperCase()
						+ " environment, the reason could be either with PI validation issues (or) the SAP response. Please check with PI & SAP team for further action.Request XMLs attached for reference. Details provided below.");
				emailBody.append("<br/>");
				emailBody.append("<br/>");
				emailBody.append("<br/>");
				emailBody.append("Hybris Ref No : " + orderModel.getCode());
				emailBody.append("<br/>");
				emailBody.append(newLine);
				emailBody.append("User ID : " + user);
				emailBody.append("<br/>");
				emailBody.append("Date : " + new Date().toString());
				emailBody.append("<br/>");
				emailBody.append(newLine);
				emailBody.append("<br/>");
				emailBody.append("Exception: RestClientException <br/>");
				request = orderData.getCode().concat("_simulate_request_".concat(strDate).concat(".xml"));
			}
			else if (option.equalsIgnoreCase("createOrder"))
			{
				attachments = getEmailAttachmentModels(requestXML, null, option, orderModel.getCode());

				emailSubject.append("ERROR: Creating order in SAP failed");
				emailBody.append("Hi,");
				emailBody.append("<br/>");
				emailBody.append("<br/>");
				emailBody.append("Creating order in SAP failed because of bad format of xml  " + mailEnvironment.toUpperCase()
						+ " environment, the reason could be either with PI validation issues (or) the SAP response. Please check with PI & SAP team for further action.Request XMLs attached for reference. Details provided below.");
				emailBody.append("<br/>");
				emailBody.append("<br/>");
				emailBody.append("<br/>");
				emailBody.append("Hybris Ref No : " + orderModel.getCode());
				emailBody.append("<br/>");
				emailBody.append(newLine);
				emailBody.append("User ID : " + user);
				emailBody.append("<br/>");
				emailBody.append("Date : " + new Date().toString());
				emailBody.append("<br/>");
				emailBody.append(newLine);
				emailBody.append("<br/>");
				emailBody.append("Exception: RestClientException <br/>");
				request = orderModel.getCode().concat("_create_request_".concat(strDate).concat(".xml"));
			}
			//emailBody.append(requestXML);
			emailBody.append("<br/>");


			emailBody.append("<br/> Thanks,");
			emailBody.append("<br/> Edgewell Personal Care Portal Team");
			emailBody.append("<br/> Note: This is an automatically generated email. Please do not reply to this mail.");
			if (request != null)
			{

				try
				{
					final EmailAttachmentModel EmailAttachmentModel = emailService.createEmailAttachment(dis, request, MIME_TYPE);


					attachments.add(EmailAttachmentModel);

				}
				catch (final Exception e)
				{
					// YTODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			final EmailMessageModel message = emailService.createEmailMessage(Arrays.asList(toAddress), null, null, fromAddress, "",
					emailSubject.toString(), emailBody.toString() + "<br/>", attachments);
			LOG.error("Failed to simulate order \n " + requestXML);
			emailService.send(message);
			throw restException;
		}
	}

	@SuppressWarnings("unchecked")
	private AbstractOrderData simulateOrderUnMarshall(final String responce, final CartData orderData, final String requestXML,
			final String responseXML, final String option) throws JAXBException, Exception
	{
		LOG.info("Unmarshalling for ORDER SIMULATE ...");
		// YTODO Auto-generated method stub
		final JAXBContext jaxbContext;
		final OrderModel orderModel = null;
		final DTB2BSALESORDERCREATERESPONSE unmarshalledOrdCreationObject = null;
		try
		{
			jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
			final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			//final InputStream stream = new ByteArrayInputStream(responce.getBytes(StandardCharsets.UTF_8));
			final InputStream stream = new ByteArrayInputStream(responce.getBytes(Charset.forName(
					configurationService.getConfiguration().getString(EnergizerCoreConstants.ORDER_SIMULATE_CHARSET_NAME, "UTF-8"))));

			final JAXBElement<DTB2BSALESORDERSIMULATERESPONSE> unmarshalledSimulateObject = (JAXBElement<DTB2BSALESORDERSIMULATERESPONSE>) unmarshaller
					.unmarshal(stream);
			final ESOHEAD head = unmarshalledSimulateObject.getValue().getESOHEAD();
			final String status = head.getSTATUS();
			final String currency = head.getCURRENCY().toString().trim();
			LOG.info("currency from simulate response ::: " + currency);

			if ("E".equalsIgnoreCase(status))
			{
				sendEmailonError(unmarshalledSimulateObject.getValue(), unmarshalledOrdCreationObject, orderModel, orderData,
						requestXML, responseXML, option);
				throw new Exception("Simulation returned Status is E");
			}
			else if (Double.parseDouble(head.getNETVALUE()) == 0)
			{
				LOG.info("head.getNETVALUE() is EMPTY ...");
				sendEmailonError(unmarshalledSimulateObject.getValue(), unmarshalledOrdCreationObject, orderModel, orderData,
						requestXML, responseXML, option);
			}
			final List<OrderEntryData> orderDataEntries = orderData.getEntries();
			final com.energizer.core.jaxb.xsd.objects.DTB2BSALESORDERSIMULATERESPONSE.TSOITEM T_SOITEM = unmarshalledSimulateObject
					.getValue().getTSOITEM();
			final List<com.energizer.core.jaxb.xsd.objects.DTB2BSALESORDERSIMULATERESPONSE.TSOITEM.ZSDTSOITEM> xmlEntries = T_SOITEM
					.getZSDTSOITEM();
			final XMLGregorianCalendar reqDeliveryDate = head.getREQDATEH();
			orderData.setRequestedDeliveryDate(reqDeliveryDate.toGregorianCalendar().getTime());
			PriceData priceData = new PriceData();
			final String value = head.getNETVALUE();
			LOG.info("net value ::::::::::::" + value);
			priceData.setValue(new BigDecimal(head.getNETVALUE().trim()));
			orderData.setTotalPrice(priceData);
			priceData = new PriceData();
			if (null != head.getTAXTOTAL())
			{
				priceData.setValue(new BigDecimal(head.getTAXTOTAL().trim()));
				orderData.setTotalTax(priceData);
			}
			final com.energizer.core.jaxb.xsd.objects.DTB2BSALESORDERSIMULATERESPONSE.TTSOCONDITIONS conditionArray = unmarshalledSimulateObject
					.getValue().getTTSOCONDITIONS();


			boolean isWeSellProductCart = false;
			final CurrencyModel currencyModel = getCommonI18NService().getCurrency(head.getCURRENCY().toString().trim());
			// Added by Rajasekhar for WeSell - START
			if ((boolean) sessionService.getAttribute("isSalesRepUserLoggedIn"))
			{
				final String creditLimit = (null != head.getCREDITLIMIT() ? head.getCREDITLIMIT().toString() : "0.00");
				final String customerBalance = (null != head.getCUSTOMERBALANCE() ? head.getCUSTOMERBALANCE().toString() : "0.00");
				final String creditAvailable = (null != head.getCREDITAVAILABLE() ? head.getCREDITAVAILABLE().toString() : "0.00");

				final EnergizerB2BUnitModel b2bUnitModel = (EnergizerB2BUnitModel) b2bCommerceUnitService
						.getUnitForUid(orderData.getB2bUnit().getUid());

				// Set values to model
				b2bUnitModel.setEnergizerCreditLimit(creditLimit);
				b2bUnitModel.setCustomerBalance(customerBalance);
				b2bUnitModel.setCreditAvailable(creditAvailable);
				modelService.save(b2bUnitModel);

				// Set values to b2bUnitData of cartData
				orderData.getB2bUnit()
						.setCreditLimit(createPrice(currencyModel, Double.parseDouble(creditLimit)).getFormattedValue());
				orderData.getB2bUnit()
						.setCustomerBalance(createPrice(currencyModel, Double.parseDouble(customerBalance)).getFormattedValue());
				orderData.getB2bUnit()
						.setCreditAvailable(createPrice(currencyModel, Double.parseDouble(creditAvailable)).getFormattedValue());
				orderData.setPlacedBySalesRep(true);
			}
			// Added by Rajasekhar for WeSell - END

			String pricingCondTypes = "";
			String[] condTypes = new String[] {};
			final Set<String> pricingNotUpdatedMaterials = new HashSet<String>();
			final Set<String> pricingUpdatedMaterials = new HashSet<String>();
			if (configurationService.getConfiguration().getString(SITE_PERSONALCARE)
					.equalsIgnoreCase(cmsSiteService.getCurrentSite().getUid())
					&& orderData.getB2bUnit().getSalesArea().equalsIgnoreCase(EnergizerCoreConstants.LATAM))
			{
				LOG.info("Site: LATAM");
				pricingCondTypes = configurationService.getConfiguration().getString("latam.pricing.cond.types");
			}
			else if (configurationService.getConfiguration().getString(SITE_PERSONALCARE_EMEA)
					.equalsIgnoreCase(cmsSiteService.getCurrentSite().getUid()))
			{
				LOG.info("Site: EMEA");
				pricingCondTypes = configurationService.getConfiguration().getString("emea.pricing.cond.types");
			}

			if (orderData.getB2bUnit().getSalesArea().equalsIgnoreCase(EnergizerCoreConstants.LATAM)
					|| orderData.getB2bUnit().getSalesArea().equalsIgnoreCase(EnergizerCoreConstants.EMEA))
			{
				if (null != pricingCondTypes && !pricingCondTypes.isEmpty())
				{
					condTypes = pricingCondTypes.split(",");
					LOG.info("No. of Pricing Cond Types : " + condTypes.length + ", Cond Types: " + Arrays.toString(condTypes));
				}
				else
				{
					LOG.info("PricingCondTypes is EMPTY !!");
				}
			}
			LOG.info("Total items in the order : " + orderDataEntries.size());

			for (final OrderEntryData OrdEntry : orderDataEntries)
			{
				isWeSellProductCart = OrdEntry.getProduct().isIsWeSellProduct();
				updateDataEntries(OrdEntry, xmlEntries, conditionArray.getZSDTSOCONDITIONS(), currency,
						orderData.getB2bUnit().getUid(), condTypes, pricingNotUpdatedMaterials, pricingUpdatedMaterials);
			}

			if (orderData.getB2bUnit().getSalesArea().equalsIgnoreCase(EnergizerCoreConstants.LATAM)
					|| orderData.getB2bUnit().getSalesArea().equalsIgnoreCase(EnergizerCoreConstants.EMEA))
			{
				if (pricingNotUpdatedMaterials.size() != 0)
				{
					LOG.info("Pricing NOT updated for '" + pricingNotUpdatedMaterials.size() + "' material(s): "
							+ Arrays.toString(pricingNotUpdatedMaterials.toArray()));
				}
				if (pricingUpdatedMaterials.size() != 0)
				{
					LOG.info("Pricing updated for '" + pricingUpdatedMaterials.size() + "' material(s): "
							+ Arrays.toString(pricingUpdatedMaterials.toArray()));
				}
			}

			// Added for WeSell Implementation - START
			if (isWeSellProductCart)
			{
				// Set subTotal to order Data
				priceData = new PriceData();
				priceData.setValue(new BigDecimal(head.getNETVALUE().toString()));
				priceData.setCurrencyIso(currency);
				priceData.setFormattedValue(createPrice(currencyModel, priceData.getValue().doubleValue()).getFormattedValue());
				priceData.setPriceType(PriceDataType.BUY);
				orderData.setSubTotal(priceData);

				// Set totalPrice to order Data
				priceData = orderData.getTotalPrice();
				priceData.setCurrencyIso(currency);
				priceData.setFormattedValue(
						createPrice(currencyModel, orderData.getTotalPrice().getValue().doubleValue()).getFormattedValue());
				priceData.setPriceType(PriceDataType.BUY);
				orderData.setTotalPrice(priceData);

				// Set totalTax to order Data
				priceData = orderData.getTotalTax();
				if (null != orderData.getTotalTax() && null != orderData.getTotalTax().getValue())
				{
					priceData.setCurrencyIso(currency);
					priceData.setFormattedValue(
							createPrice(currencyModel, orderData.getTotalTax().getValue().doubleValue()).getFormattedValue());
					priceData.setPriceType(PriceDataType.BUY);
					orderData.setTotalTax(priceData);
				}

				final CartModel cartModel = cartService.getSessionCart();
				cartModel.setSubtotal(new BigDecimal(head.getNETVALUE().toString()).doubleValue());
				cartModel.setTotalPrice(new BigDecimal(head.getNETVALUE().toString()).doubleValue());
				cartModel.setCurrency(getCommonI18NService().getCurrency(head.getCURRENCY().trim()));
				cartModel.setPlacedBySalesRep(orderData.isPlacedBySalesRep());
				modelService.save(cartModel);
				modelService.refresh(cartModel);
				LOG.info("******** Updated values from cartModel in simulateOrderUnmarshall *********");
				LOG.info("Sub total: " + cartModel.getSubtotal().doubleValue() + " , Total Price : "
						+ cartModel.getTotalPrice().doubleValue() + " , Cart Currency : " + cartModel.getCurrency().getIsocode());
				// cartModel is saved, but the cartService.getSessionCart(); will still return the cart from the saved session attribute. So saving the cartModel to the session.
				cartService.setSessionCart(cartModel);
				LOG.info("cartModel saved to session cart in simulateOrderUnmarshall() method ...");
			}
			// Added for WeSell Implementation - END

			String orderComments = StringUtils.EMPTY;
			final List<com.energizer.core.jaxb.xsd.objects.DTB2BSALESORDERSIMULATERESPONSE.TTEXTS.BAPISDTEXT> textArray = unmarshalledSimulateObject
					.getValue().getTTEXTS().getBAPISDTEXT();
			if (null != textArray && !textArray.isEmpty())
			{
				for (final com.energizer.core.jaxb.xsd.objects.DTB2BSALESORDERSIMULATERESPONSE.TTEXTS.BAPISDTEXT textObj : textArray)
				{
					final String jaxbText = textObj.getTEXTLINE();
					orderComments = orderComments.concat(jaxbText);
				}
				LOG.info("Order Comments from Response ::: " + orderComments);
			}
			orderData.setOrderComments(orderComments);
			if (null != currency)
			{
				orderData.setSalesRepCurrencyIsoCode(currency);
			}
		}
		catch (final JAXBException exception)
		{
			LOG.error(" Failed in getting Orderdata during OrderPlacing " + exception.getMessage(), exception);
			throw exception;
		}
		catch (final Exception e)
		{
			LOG.error("Exception occured during simulate order unmarshall .... " + e);
			throw e;
		}
		return orderData;
	}


	@SuppressWarnings("unchecked")
	private void simulateOrderforIDUnMarshall(final String response, final OrderModel orderModel, final String requestXML,
			final String responseXML, final String option) throws Exception
	{
		LOG.info("Unmarshalling for ORDER CREATE ...");
		final JAXBContext jaxbContext;
		final DTB2BSALESORDERSIMULATERESPONSE unmarshalledSimulateObject = null;
		final CartData orderData = null;
		try
		{
			jaxbContext = JAXBContext.newInstance(com.energizer.core.createorder.jaxb.xsd.objects.ObjectFactory.class);
			final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			//final InputStream stream = new ByteArrayInputStream(response.getBytes(StandardCharsets.UTF_8));
			final InputStream stream = new ByteArrayInputStream(response.getBytes(Charset.forName(
					configurationService.getConfiguration().getString(EnergizerCoreConstants.ORDER_SIMULATE_CHARSET_NAME, "UTF-8"))));

			final JAXBElement<DTB2BSALESORDERCREATERESPONSE> unmarshalledOrdCreationObject = (JAXBElement<DTB2BSALESORDERCREATERESPONSE>) unmarshaller
					.unmarshal(stream);
			final com.energizer.core.createorder.jaxb.xsd.objects.DTB2BSALESORDERCREATERESPONSE.ESOHEAD head = unmarshalledOrdCreationObject
					.getValue().getESOHEAD();

			final String status = head.getSTATUS();

			if ("E".equalsIgnoreCase(status))
			{
				sendEmailonError(unmarshalledSimulateObject, unmarshalledOrdCreationObject.getValue(), orderModel, orderData,
						requestXML, responseXML, option);
				throw new Exception("Create order in SAP returned Status is E");
			}
			else if (Double.parseDouble(head.getNETVALUE()) == 0)
			{
				LOG.info("head.getNETVALUE() is EMPTY ...");
				sendEmailonError(unmarshalledSimulateObject, unmarshalledOrdCreationObject.getValue(), orderModel, orderData,
						requestXML, responseXML, option);
			}
			orderModel.setErpOrderNumber(head.getDOCNUMBER());
			orderModel.setTotalTax(Double.parseDouble(head.getTAXTOTAL()));
			if (null != orderModel.getPlacedBySalesRep() && orderModel.getPlacedBySalesRep())
			{
				orderModel.setSubtotal(Double.parseDouble(head.getNETVALUE()));
			}
			orderModel.setTotalPrice(Double.parseDouble(head.getNETVALUE()));
			final XMLGregorianCalendar reqDeliveryDate = head.getREQDATEH();
			orderModel.setRequestedDeliveryDate(reqDeliveryDate.toGregorianCalendar().getTime());
			final List<AbstractOrderEntryModel> orderModelEntries = orderModel.getEntries();

			final com.energizer.core.createorder.jaxb.xsd.objects.DTB2BSALESORDERCREATERESPONSE.TTSOCONDITIONS conditionArray = unmarshalledOrdCreationObject
					.getValue().getTTSOCONDITIONS();
			final com.energizer.core.createorder.jaxb.xsd.objects.DTB2BSALESORDERCREATERESPONSE.TSOITEM TSOITEM = unmarshalledOrdCreationObject
					.getValue().getTSOITEM();
			final List<com.energizer.core.createorder.jaxb.xsd.objects.DTB2BSALESORDERCREATERESPONSE.TSOITEM.ZSDTSOITEM> xmlEntries = TSOITEM
					.getZSDTSOITEM();
			CurrencyModel currencyModel = null;
			if (null != head.getCURRENCY())
			{
				currencyModel = getCommonI18NService().getCurrency(head.getCURRENCY().trim());
				LOG.info("Currency from create response ::: " + head.getCURRENCY().trim());
			}
			orderModel.setCurrency(currencyModel);

			String pricingCondTypes = "";
			String[] condTypes = new String[] {};
			final Set<String> pricingNotUpdatedMaterials = new HashSet<String>();
			final Set<String> pricingUpdatedMaterials = new HashSet<String>();
			if (configurationService.getConfiguration().getString(SITE_PERSONALCARE).equalsIgnoreCase(getSiteUid(orderModel))
					&& !orderModel.getPlacedBySalesRep())
			//orderModel.getB2bUnit().getSalesArea().equalsIgnoreCase(EnergizerCoreConstants.LATAM)
			{
				LOG.info("Site: LATAM");
				pricingCondTypes = configurationService.getConfiguration().getString("latam.pricing.cond.types");
			}
			else if (configurationService.getConfiguration().getString(SITE_PERSONALCARE_EMEA)
					.equalsIgnoreCase(getSiteUid(orderModel)))
			{
				LOG.info("Site: EMEA");
				pricingCondTypes = configurationService.getConfiguration().getString("emea.pricing.cond.types");
			}
			if (!orderModel.getPlacedBySalesRep())
			{
				if (null != pricingCondTypes && !pricingCondTypes.isEmpty())
				{
					condTypes = pricingCondTypes.split(",");
					LOG.info("No. of Pricing Cond Types : " + condTypes.length + ", Cond Types: " + Arrays.toString(condTypes));
				}
				else
				{
					LOG.info("PricingCondTypes is EMPTY !!");
				}
			}
			LOG.info("Total items in the order : " + orderModelEntries.size());
			for (final AbstractOrderEntryModel orderEntryModel : orderModelEntries)
			{

				final String modelProdCode = orderEntryModel.getProduct().getCode();
				final Double condTypeBaseUomPrice = Double.parseDouble("0.00");
				final Double condTypeBaseUomQuantity = Double.parseDouble("0.00");
				final Double condTypeEntryTotal = Double.parseDouble("0.00");
				boolean isCondTypePriceAvailable = false;

				//Added Code changes for WeSell Implementation - START
				if (null != orderModel.getPlacedBySalesRep() && orderModel.getPlacedBySalesRep())
				{
					for (final com.energizer.core.createorder.jaxb.xsd.objects.DTB2BSALESORDERCREATERESPONSE.TSOITEM.ZSDTSOITEM item : xmlEntries)
					{
						if (orderEntryModel.getProduct().getCode().equalsIgnoreCase(item.getMATERIAL().toString().trim()))
						{
							orderEntryModel.setBasePrice(new BigDecimal(item.getCUSTPRICE().doubleValue()).doubleValue());
							orderEntryModel.setTotalPrice(new BigDecimal(item.getNETVALUE()).doubleValue());
							orderEntryModel.setRejectedStatus("No");
							modelService.save(orderEntryModel);
							modelService.refresh(orderEntryModel);
							break;
						}
					}
				}
				//Added Code changes for WeSell Implementation - END
				// FOR LATAM
				else if (configurationService.getConfiguration().getString(SITE_PERSONALCARE).equalsIgnoreCase(getSiteUid(orderModel))
						&& !orderModel.getPlacedBySalesRep())
				{
					isCondTypePriceAvailable = getCondTypesPricingDataForOrderCreate(conditionArray, condTypes, modelProdCode,
							condTypeBaseUomPrice, condTypeBaseUomQuantity, condTypeEntryTotal, isCondTypePriceAvailable,
							orderEntryModel);
				}
				// FOR EMEA
				else if (configurationService.getConfiguration().getString(SITE_PERSONALCARE_EMEA)
						.equalsIgnoreCase(getSiteUid(orderModel)))
				{
					isCondTypePriceAvailable = getCondTypesPricingDataForOrderCreate(conditionArray, condTypes, modelProdCode,
							condTypeBaseUomPrice, condTypeBaseUomQuantity, condTypeEntryTotal, isCondTypePriceAvailable,
							orderEntryModel);
				}
				if (!orderModel.getPlacedBySalesRep())
				{
					if (!isCondTypePriceAvailable)
					{
						pricingNotUpdatedMaterials.add(modelProdCode);
					}
					else
					{
						pricingUpdatedMaterials.add(modelProdCode);
					}
				}
			}
			if (!orderModel.getPlacedBySalesRep())
			{
				if (pricingNotUpdatedMaterials.size() != 0)
				{
					LOG.info("Pricing NOT updated for '" + pricingNotUpdatedMaterials.size() + "' material(s): "
							+ Arrays.toString(pricingNotUpdatedMaterials.toArray()));
				}
				if (pricingUpdatedMaterials.size() != 0)
				{
					LOG.info("Pricing updated for '" + pricingUpdatedMaterials.size() + "' material(s): "
							+ Arrays.toString(pricingUpdatedMaterials.toArray()));
				}
			}

			String orderComments = StringUtils.EMPTY;
			final List<com.energizer.core.createorder.jaxb.xsd.objects.DTB2BSALESORDERCREATERESPONSE.TTEXTS.BAPISDTEXT> textArray = unmarshalledOrdCreationObject
					.getValue().getTTEXTS().getBAPISDTEXT();
			if (null != textArray && !textArray.isEmpty())
			{
				for (final com.energizer.core.createorder.jaxb.xsd.objects.DTB2BSALESORDERCREATERESPONSE.TTEXTS.BAPISDTEXT textObj : textArray)
				{
					final String jaxbText = textObj.getTEXTLINE();
					orderComments = orderComments.concat(jaxbText);
				}
				LOG.info("Order Comments from Response ::: " + orderComments);
			}
			orderModel.setOrderComments(orderComments);
			orderModel.setStatus(OrderStatus.PENDING);
			modelService.save(orderModel);
		}
		catch (final Exception exception)
		{
			LOG.error("Failed in Order Placing Process " + exception.getMessage(), exception);
			throw new Exception(exception.getMessage());
		}
	}

	private StringBuilder getOrderCreationEmailBody(final StringBuilder emailBody,
			final DTB2BSALESORDERCREATERESPONSE unmarshalledOrdCreationObject)
	{
		final List<com.energizer.core.createorder.jaxb.xsd.objects.DTB2BSALESORDERCREATERESPONSE.MESSAGETABLE.BAPIRET2> messageList = unmarshalledOrdCreationObject
				.getMESSAGETABLE().getBAPIRET2();
		if (null != messageList && !messageList.isEmpty())
		{
			for (final com.energizer.core.createorder.jaxb.xsd.objects.DTB2BSALESORDERCREATERESPONSE.MESSAGETABLE.BAPIRET2 bapi : messageList)
			{
				if (bapi.getTYPE().equalsIgnoreCase("E"))
				{
					//emailBody.append("Message : " + bapi.getMESSAGE().getValue().toString());
					emailBody.append("Message : <p style='color: red'>" + bapi.getMESSAGE().toString() + "</p>");
					emailBody.append("<br />");
				}
			}
		}

		return emailBody;
	}


	private StringBuilder getSimulationEmailBody(final StringBuilder emailBody,
			final DTB2BSALESORDERSIMULATERESPONSE unmarshalledSimulateObject)
	{
		final List<BAPIRET2> messageList = unmarshalledSimulateObject.getMESSAGETABLE().getBAPIRET2();
		if (null != messageList && !messageList.isEmpty())
		{
			for (final BAPIRET2 bapi : messageList)
			{
				if (bapi.getTYPE().equalsIgnoreCase("E"))
				{
					//emailBody.append("Message : " + bapi.getMESSAGE().getValue().toString());
					emailBody.append("Message : <p style='color: red'>" + bapi.getMESSAGE().toString() + "</p>");
					emailBody.append("<br />");
				}
			}
		}

		return emailBody;
	}

	private String getOrderCreationErrorMessage(final DTB2BSALESORDERCREATERESPONSE unmarshalledOrdCreationObject)
	{
		String errorMessage = new String();
		final List<com.energizer.core.createorder.jaxb.xsd.objects.DTB2BSALESORDERCREATERESPONSE.MESSAGETABLE.BAPIRET2> messageList = unmarshalledOrdCreationObject
				.getMESSAGETABLE().getBAPIRET2();
		LOG.info("messageList" + messageList.toArray().toString());
		if (null != messageList && !messageList.isEmpty())
		{
			for (final com.energizer.core.createorder.jaxb.xsd.objects.DTB2BSALESORDERCREATERESPONSE.MESSAGETABLE.BAPIRET2 bapi : messageList)
			{
				if (bapi.getTYPE().equalsIgnoreCase("E") && null != bapi.getMESSAGE() && !StringUtils.isEmpty(bapi.getMESSAGE()))
				{
					errorMessage = bapi.getMESSAGE().toString();
					LOG.info("errorMessage1:" + errorMessage);
				}
			}
		}

		return errorMessage;
	}


	private String getSimulationErrorMessage(final DTB2BSALESORDERSIMULATERESPONSE unmarshalledSimulateObject)
	{
		String errorMessage = new String();
		final List<BAPIRET2> messageList = unmarshalledSimulateObject.getMESSAGETABLE().getBAPIRET2();
		LOG.info("messageList" + messageList.toArray().toString());
		if (null != messageList && !messageList.isEmpty())
		{
			for (final BAPIRET2 bapi : messageList)
			{
				if (bapi.getTYPE().equalsIgnoreCase("E") && null != bapi.getMESSAGE() && !StringUtils.isEmpty(bapi.getMESSAGE()))
				{
					errorMessage = bapi.getMESSAGE().toString();
					LOG.info("errorMessage2:" + errorMessage);
				}
			}
		}

		return errorMessage;
	}

	@SuppressWarnings(
	{ "unused" })
	private void sendEmailonError(final DTB2BSALESORDERSIMULATERESPONSE unmarshalledSimulateObject,
			final DTB2BSALESORDERCREATERESPONSE unmarshalledOrdCreationObject, final OrderModel orderModel, final CartData orderData,
			final String requestXML, final String responseXML, final String option)
	{
		final BaseSiteModel currentBaseSite = baseSiteService.getCurrentBaseSite();
		// todo -- handle messagetable and order-incomplete data coming from SAP

		final String siteUid = getSiteUid(orderModel);
		final ESOHEAD simulateResponseHead = null != unmarshalledSimulateObject ? unmarshalledSimulateObject.getESOHEAD() : null;
		final com.energizer.core.createorder.jaxb.xsd.objects.DTB2BSALESORDERCREATERESPONSE.ESOHEAD createResponseHead = null != unmarshalledOrdCreationObject
				? unmarshalledOrdCreationObject.getESOHEAD()
				: null;

		String supportEmail = configurationService.getConfiguration().getString("energizer.customer.support.to.email." + siteUid,
				"test@test.com");
		LOG.info("supportEmail :::  " + supportEmail);

		final String mailEnvironment = configurationService.getConfiguration().getString("mail.environment.stuck.orders");

		final EmailAddressModel toAddress = emailService.getOrCreateEmailAddressForEmail(supportEmail, "Hybris Support Mail");
		supportEmail = configurationService.getConfiguration().getString("energizer.customer.support.from.email." + siteUid,
				"test@test.com");
		final EmailAddressModel fromAddress = emailService.getOrCreateEmailAddressForEmail(supportEmail, "Hybris Support Mail");
		final List<EmailAttachmentModel> attachments = new ArrayList<EmailAttachmentModel>();

		final InputStream inputStream = new ByteArrayInputStream(requestXML.getBytes(Charset.forName("UTF-8")));
		final InputStream inputStreams = new ByteArrayInputStream(responseXML.getBytes(Charset.forName("UTF-8")));
		final DataInputStream dis = new DataInputStream(inputStream);
		final DataInputStream res = new DataInputStream(inputStreams);

		final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HHmmss");
		final Date date = new Date();
		final String strDate = dateFormat.format(date);
		String request = null;
		String response = null;
		if (option.equalsIgnoreCase("simulate"))
		{
			request = orderData.getCode().concat("_Simulate_request_".concat(strDate).concat(".xml"));
			response = orderData.getCode().concat("_Simulate_response_".concat(strDate).concat(".xml"));
		}
		else
		{

			request = orderModel.getCode().concat("_Create_request_".concat(strDate).concat(".xml"));
			response = orderModel.getCode().concat("_Create_response_".concat(strDate).concat(".xml"));
		}

		if (request != null && response != null)
		{
			try
			{
				final EmailAttachmentModel EmailAttachmentModel = emailService.createEmailAttachment(dis, request, MIME_TYPE);
				final EmailAttachmentModel EmailAttachmentModels = emailService.createEmailAttachment(res, response, MIME_TYPE);

				attachments.add(EmailAttachmentModel);
				attachments.add(EmailAttachmentModels);
			}
			catch (final Exception e)
			{
				// YTODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		//b2bunit,enduser, time, cartno
		StringBuilder emailBody = new StringBuilder();
		final String newLine = System.getProperty("line.separator");
		final String site = ((null != orderData && null != orderData.getSite()) ? orderData.getSite()
				: orderModel.getSite().getUid());

		String user = supportEmail;

		if (!option.equalsIgnoreCase("simulate"))
		{
			//user = ((null != orderModel && null != orderModel.getPlacedBy()) ? orderModel.getPlacedBy().getUid() : supportEmail);
			user = ((null != orderModel && null != orderModel.getUser()) ? orderModel.getUser().getUid() : supportEmail);
			emailBody.append("Hi,");
			emailBody.append("<br/>");
			emailBody.append("Order simulation failed in SAP/PI in " + mailEnvironment.toUpperCase()
					+ " environment, the reason could be either with PI validation issues (or) the SAP response. Please check with PI & SAP team for further action.Request and response XMLs attached for reference. Details provided below.");
			emailBody.append("<br/>");
			emailBody.append("<br/>");
			emailBody.append("Hybris Ref No : " + orderModel.getCode());
			if (Double.parseDouble(createResponseHead.getNETVALUE()) == 0)
			{
				emailBody.append(
						"<p style='color: red'>Order Total Price : " + Double.parseDouble(createResponseHead.getNETVALUE()) + "</p>");
			}
			else
			{
				emailBody.append("Order Total Price : " + Double.parseDouble(createResponseHead.getNETVALUE()));
			}
			emailBody.append("<br/>");
			emailBody.append(newLine);
		}
		else
		{
			user = ((null != orderData && null != orderData.getUser()) ? orderData.getUser().getUid() : supportEmail);
			emailBody.append("Hi,");
			emailBody.append("<br/>");
			emailBody.append("Order simulation failed in SAP/PI in " + mailEnvironment.toUpperCase()
					+ " environment, the reason could be either with PI validation issues (or) the SAP response. Please check with PI & SAP team for further action.Request and response XMLs attached for reference. Details provided below.");
			emailBody.append("<br/>");
			emailBody.append("<br/>");
			emailBody.append("<br/>");
			emailBody.append("Hybris Ref No : " + orderData.getCode());
			if (Double.parseDouble(simulateResponseHead.getNETVALUE()) == 0)
			{
				emailBody.append(
						"<p style='color: red'>Order Total Price : " + Double.parseDouble(simulateResponseHead.getNETVALUE()) + "</p>");
			}
			else
			{
				emailBody.append("Order Total Price : " + Double.parseDouble(simulateResponseHead.getNETVALUE()));
			}
			emailBody.append("<br/>");
			emailBody.append(newLine);

		}
		emailBody.append(newLine);
		emailBody.append("User ID : " + user);
		emailBody.append("<br/>");
		emailBody.append("Date : " + new Date().toString());
		emailBody.append("<br/>");
		emailBody.append('\n');
		emailBody.append(newLine);
		emailBody.append("Error messages from SAP as follows. ");
		emailBody.append("<br/>");
		emailBody.append(newLine);

		if (unmarshalledSimulateObject != null)
		{
			emailBody = getSimulationEmailBody(emailBody, unmarshalledSimulateObject);
			sessionService.removeAttribute("orderSimulateErrorMessage");
			sessionService.setAttribute("orderSimulateErrorMessage", getSimulationErrorMessage(unmarshalledSimulateObject));
		}

		if (unmarshalledOrdCreationObject != null)
		{
			emailBody = getOrderCreationEmailBody(emailBody, unmarshalledOrdCreationObject);
		}

		if (site.equalsIgnoreCase(PERSONALCARE))
		{

			siteName = EnergizerCoreConstants.LATAM;
		}
		if (site.equalsIgnoreCase(PERSONALCAREEMEA))
		{

			siteName = EnergizerCoreConstants.EMEA;
		}

		emailBody.append("<br/>");
		emailBody.append("<br/> Thanks,");
		emailBody.append("<br/> Edgewell Personal Care Portal Team");
		emailBody.append("<br/> Note: This is an automatically generated email. Please do not reply to this mail.");

		final EmailMessageModel message = emailService.createEmailMessage(Arrays.asList(toAddress), null, null, fromAddress, "",
				"ERROR: Order simulation failed in SAP in " + mailEnvironment.toUpperCase() + " Environment -" + siteName,
				emailBody.toString(), attachments);

		if (unmarshalledSimulateObject != null)
		{
			final List<BAPIRET2> messageList = unmarshalledSimulateObject.getMESSAGETABLE().getBAPIRET2();
			if (null != messageList && !messageList.isEmpty())
			{
				for (final BAPIRET2 bapi : messageList)
				{
					if (bapi.getTYPE().equalsIgnoreCase("E"))
					{
						LOG.error("Failed to simulate order : " + bapi.getMESSAGE().toString());
					}
				}
			}
		}

		if (unmarshalledOrdCreationObject != null)
		{
			final List<com.energizer.core.createorder.jaxb.xsd.objects.DTB2BSALESORDERCREATERESPONSE.MESSAGETABLE.BAPIRET2> messageList = unmarshalledOrdCreationObject
					.getMESSAGETABLE().getBAPIRET2();
			if (null != messageList && !messageList.isEmpty())
			{
				for (final com.energizer.core.createorder.jaxb.xsd.objects.DTB2BSALESORDERCREATERESPONSE.MESSAGETABLE.BAPIRET2 bapi : messageList)
				{
					if (bapi.getTYPE().equalsIgnoreCase("E"))
					{
						LOG.error("Failed to create order : " + bapi.getMESSAGE().toString());
					}
				}
			}
		}

		emailService.send(message);
	}

	/**
	 * @param dataEntry
	 * @param xmlEntries
	 *
	 */
	private void updateDataEntries(final OrderEntryData orderEntry,
			final List<com.energizer.core.jaxb.xsd.objects.DTB2BSALESORDERSIMULATERESPONSE.TSOITEM.ZSDTSOITEM> xmlEntries,
			final List<DTB2BSALESORDERSIMULATERESPONSE.TTSOCONDITIONS.ZSDTSOCONDITIONS> conditionList, final String currencyIsoCode,
			final String b2bUnitID, final String[] condTypes, final Set<String> pricingNotUpdatedMaterials,
			final Set<String> pricingUpdatedMaterials)
	{
		try
		{
			final ProductData productData = orderEntry.getProduct();
			final String prodCode = productData.getCode();
			final PriceData condTypePriceData = new PriceData();
			final PriceData condTypeTotalPriceData = new PriceData();
			boolean isCondTypePriceAvailable = false;
			PriceData totalPriceData = null;
			PriceData basePriceData = null;
			double uomPrice = 0.0;
			Long inventoryAvailable = 0L;
			double discountAmount = 0.00;
			double discountPercent = 0.00;

			// Added for WeSell Implementation - START
			final CurrencyModel currency = getCommonI18NService().getCurrency(currencyIsoCode);
			if (orderEntry.getProduct().isIsWeSellProduct())
			{
				uomPrice = 0.01; // Setting the default 0.01 CMIR price to avoid errors/exceptions in Populators/other calculation strategy APIs

				for (final com.energizer.core.jaxb.xsd.objects.DTB2BSALESORDERSIMULATERESPONSE.TSOITEM.ZSDTSOITEM tsoItem : xmlEntries)
				{
					if (orderEntry.getProduct().getCode().equalsIgnoreCase(tsoItem.getMATERIAL().toString()))
					{
						totalPriceData = new PriceData();
						totalPriceData.setValue(new BigDecimal(tsoItem.getNETVALUE().toString()));
						totalPriceData.setCurrencyIso(currencyIsoCode);
						totalPriceData.setPriceType(PriceDataType.BUY);
						totalPriceData
								.setFormattedValue(createPrice(currency, totalPriceData.getValue().doubleValue()).getFormattedValue());

						uomPrice = tsoItem.getCUSTPRICE().doubleValue();
						basePriceData = new PriceData();
						basePriceData.setValue(new BigDecimal(uomPrice));
						basePriceData.setCurrencyIso(currencyIsoCode);
						basePriceData.setPriceType(PriceDataType.BUY);
						basePriceData
								.setFormattedValue(createPrice(currency, basePriceData.getValue().doubleValue()).getFormattedValue());
						if (null != tsoItem.getINVENTORYAVAILABLE() && null != tsoItem.getINVENTORYAVAILABLE())
						{
							inventoryAvailable = tsoItem.getINVENTORYAVAILABLE().longValue();
						}
						if (null != tsoItem.getDISCOUNTAMT() && null != tsoItem.getDISCOUNTAMT())
						{
							discountAmount = Double.parseDouble(tsoItem.getDISCOUNTAMT());
						}
						if (null != tsoItem.getDISCOUNTPERCENT() && null != tsoItem.getDISCOUNTPERCENT())
						{
							discountPercent = Double.parseDouble(tsoItem.getDISCOUNTPERCENT());
						}
						break;
					}
					/*-else
					{
						LOG.info("Product in order entry & material code in response XML does NOT match at item number : "
								+ tsoItem.getITMNUMBER().getValue() + " !!");
						LOG.info("Order entry product : " + orderEntry.getProduct().getCode() + ", response XML material : "
								+ tsoItem.getMATERIAL().getValue().toString());
					}*/
					if (tsoItem.getITMNUMBER() % 10 != 0)
					{
						LOG.info("The material code " + tsoItem.getMATERIAL().toString() + " from the response XML for item number : "
								+ tsoItem.getITMNUMBER() + " is the adjusted material added to the SAP response. Please check with SAP.");
					}
				}
				if ((boolean) sessionService.getAttribute("isSalesRepUserLoggedIn"))
				{
					if (null != inventoryAvailable)
					{
						orderEntry.setInventoryAvailable(NumberFormat.getIntegerInstance().format(inventoryAvailable.intValue()));
					}
					orderEntry.setDiscountAmount(String.format("%.2f", discountAmount));
					//orderEntry.setDiscountPercent(String.format("%.2f", discountPercent));
					orderEntry.setDiscountPercent(String.valueOf(discountPercent));
				}
				orderEntry.setEachUnitPrice(null);
				orderEntry.setBasePrice(basePriceData);
				orderEntry.setTotalPrice(totalPriceData);

				// Saving the Base Price as null & Total Price to the Cart Entry
				final CartModel cartModel = cartService.getSessionCart();

				final AbstractOrderEntryModel abstractOrderEntryModel = energizerCartService.getEntryForNumber(cartModel,
						orderEntry.getEntryNumber());

				//Set each unit price to the abstractOrderEntryModel to display it in the cart page table
				abstractOrderEntryModel.setEachUnitPrice(null);
				//abstractOrderEntryModel.setBasePrice(uomPrice);
				abstractOrderEntryModel.setBasePrice(
						(null != basePriceData && null != basePriceData.getValue()) ? basePriceData.getValue().doubleValue() : 0.0);
				abstractOrderEntryModel.setTotalPrice(
						(null != totalPriceData && null != totalPriceData.getValue()) ? totalPriceData.getValue().doubleValue() : 0.0);
				abstractOrderEntryModel.setIsWeSellProduct(orderEntry.getProduct().isIsWeSellProduct());
				if ((boolean) sessionService.getAttribute("isSalesRepUserLoggedIn"))
				{
					abstractOrderEntryModel.setInventoryAvailable(inventoryAvailable);
					abstractOrderEntryModel.setDiscountAmount(discountAmount);
					abstractOrderEntryModel.setDiscountPercent(discountPercent);
					cartModel.setPlacedBySalesRep(true);
				}
				modelService.save(abstractOrderEntryModel);
				modelService.refresh(abstractOrderEntryModel);
				modelService.refresh(cartModel);

				// cartModel is saved, but the cartService.getSessionCart(); will still return the cart from the saved session attribute. So saving the cartModel to the session.
				cartService.setSessionCart(cartModel);
				LOG.info("cartModel saved to session cart in updateDataEntries() method ...");
				//final EnergizerCMIRModel energizerCMIR = energizerProductService.getEnergizerCMIR(prodCode, b2bUnitID);
				final EnergizerPriceRowModel cmirPriceRow = energizerProductService.getEnergizerPriceRowForB2BUnit(prodCode,
						b2bUnitID);
				/*-final EnergizerProductConversionFactorModel productConversion = energizerProductService
						.getEnergizerProductConversion(prodCode, b2bUnitID);
				EnergizerProductConversionFactorModel productConversionOfPriceUOM = null;
				if (null != energizerCMIR)
				{
					productConversionOfPriceUOM = energizerProductService
						.getEnergizerProductConversionByUOM(prodCode, energizerCMIR.getUom());
				}
				else
				{
					productConversionOfPriceUOM = energizerProductService.getEnergizerProductConversionByUOM(prodCode,
							cmirPriceRow.getPriceUOM());
				}*/
				if (null != cmirPriceRow)
				{
					BigDecimal newPrice = new BigDecimal(uomPrice);
					newPrice = newPrice.setScale(2, BigDecimal.ROUND_HALF_EVEN);
					cmirPriceRow.setPrice(newPrice.doubleValue());
					cmirPriceRow.setCurrency(currency);
					modelService.save(cmirPriceRow);
				}
				else
				{
					LOG.info(" cmir pricerow not available for this product: " + prodCode + " , b2bunit id: " + b2bUnitID);
				}
				/*-if (null != cmirPriceRow && null == productConversion)
				{
					BigDecimal newPrice = new BigDecimal(uomPrice);
					newPrice = newPrice.setScale(2, BigDecimal.ROUND_HALF_EVEN);
					cmirPriceRow.setPrice(newPrice.doubleValue());
					cmirPriceRow.setCurrency(currency);
					modelService.save(cmirPriceRow);
				}*/
			} // Added for WeSell Implementation - end
			else if (configurationService.getConfiguration().getString(SITE_PERSONALCARE)
					.equalsIgnoreCase(cmsSiteService.getCurrentSite().getUid()) && !orderEntry.getProduct().isIsWeSellProduct())
			{
				isCondTypePriceAvailable = getCondTypesPricingDataForOrderSimulate(conditionList, prodCode, condTypes,
						condTypePriceData, condTypeTotalPriceData, isCondTypePriceAvailable, orderEntry);
			}
			else if (configurationService.getConfiguration().getString(SITE_PERSONALCARE_EMEA)
					.equalsIgnoreCase(cmsSiteService.getCurrentSite().getUid()))
			{
				isCondTypePriceAvailable = getCondTypesPricingDataForOrderSimulate(conditionList, prodCode, condTypes,
						condTypePriceData, condTypeTotalPriceData, isCondTypePriceAvailable, orderEntry);
			}
			if (!isCondTypePriceAvailable)
			{
				pricingNotUpdatedMaterials.add(prodCode);
			}
			else
			{
				pricingUpdatedMaterials.add(prodCode);
			}
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}

	}

	private String getResponse(final RestTemplate restTemplate, final String url, final HttpEntity formEntity)
	{
		final ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, formEntity, String.class);
		LOG.info("simulate responce!! = " + response.getBody());
		return response.getBody();
	}

	private Integer getAlernateConversionMultiplierForUOM(final List<EnergizerProductConversionFactorModel> conversionList,
			final String uom)
	{
		// YTODO Auto-generated method stub
		for (final EnergizerProductConversionFactorModel enrProdConversion : conversionList)
		{
			if (enrProdConversion.getAlternateUOM().equalsIgnoreCase(uom))
			{
				return enrProdConversion.getConversionMultiplier();
			}
		}

		return null;

	}

	private String getSiteSpecificTextId(final OrderModel order)
	{

		final String PERSONALCARE = configurationService.getConfiguration().getString(SITE_PERSONALCARE);
		final String PERSONALCARE_EMEA = configurationService.getConfiguration().getString(SITE_PERSONALCARE_EMEA);
		String text_Id = StringUtils.EMPTY;
		final String siteUid = getSiteUid(order);

		if (null != siteUid)
		{
			if (siteUid.equalsIgnoreCase(PERSONALCARE))
			{
				text_Id = configurationService.getConfiguration().getString(TEXT_ID_PERSONALCARE);
				LOG.info("Fetching text_Id " + text_Id + " for : " + PERSONALCARE);
			}
			else if (siteUid.equalsIgnoreCase(PERSONALCARE_EMEA))
			{
				text_Id = configurationService.getConfiguration().getString(TEXT_ID_PERSONALCARE_EMEA);
				LOG.info("Fetching text_Id " + text_Id + " for : " + PERSONALCARE_EMEA);
			}
			else
			{
				LOG.info("text_Id NOT found for : " + siteUid);
			}
		}
		return text_Id;
	}

	private String getSiteUid(final OrderModel order)
	{

		String siteUid = StringUtils.EMPTY;

		if (null == order || null == order.getSite() || null == order.getSite().getUid() || order.getSite().getUid().isEmpty())
		{
			siteUid = this.cmsSiteService.getCurrentSite().getUid();
			LOG.debug("Order Model is null. So, fetching siteUid from cmsSiteService ... ");
		}
		else if (null != order.getSite().getUid() && !order.getSite().getUid().isEmpty())
		{
			siteUid = order.getSite().getUid();
			LOG.debug("Order Model is NOT null. So, fetching siteUid from order model ... ");
		}
		else
		{
			LOG.info("Site ID not found !");
		}

		return siteUid;
	}

	protected PriceData createPrice(final CurrencyModel currency, final Double val)
	{
		return getPriceDataFactory().create(PriceDataType.BUY, BigDecimal.valueOf(val.doubleValue()), currency);
	}

	private List<EmailAttachmentModel> getEmailAttachmentModels(final String requestXML, final String responseXML,
			final String option, final String orderCode)
	{

		final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HHmmss");
		final Date date = new Date();
		final String strDate = dateFormat.format(date);
		String request = null;
		String response = null;
		final List<EmailAttachmentModel> attachments = new ArrayList<EmailAttachmentModel>();
		DataInputStream dis_req = null;
		DataInputStream dis_res = null;
		try
		{
			if (null != responseXML)
			{
				final InputStream inputStream_req = new ByteArrayInputStream(requestXML.getBytes(Charset.forName("UTF-8")));
				dis_req = new DataInputStream(inputStream_req);
				if (option.equalsIgnoreCase("simulate"))
				{
					request = orderCode.concat("_Simulate_request_".concat(strDate).concat(".xml"));
				}
				else
				{
					request = orderCode.concat("_Create_request_".concat(strDate).concat(".xml"));
				}
				final EmailAttachmentModel emailAttachmentModel_req = emailService.createEmailAttachment(dis_req, request, MIME_TYPE);
				attachments.add(emailAttachmentModel_req);
			}
			if (null != responseXML)
			{
				final InputStream inputStream_res = new ByteArrayInputStream(responseXML.getBytes(Charset.forName("UTF-8")));
				dis_res = new DataInputStream(inputStream_res);
				if (option.equalsIgnoreCase("simulate"))
				{
					response = orderCode.concat("_Simulate_response_".concat(strDate).concat(".xml"));
				}
				else
				{
					response = orderCode.concat("_Create_response_".concat(strDate).concat(".xml"));
				}
				final EmailAttachmentModel emailAttachmentModel_res = emailService.createEmailAttachment(dis_res, response,
						MIME_TYPE);
				attachments.add(emailAttachmentModel_res);
			}
		}
		catch (final Exception e)
		{
			LOG.error("Exception occured while sending email ::: " + e.getMessage());
			//e.printStackTrace();
		}
		return attachments;
	}

	public String convertStringToUTF8(final String orderComments) throws UnsupportedEncodingException
	{
		final String utf8string = new String(orderComments.getBytes("UTF-8"), "ISO-8859-1");

		System.out.println("utf8string = " + utf8string);
		System.out.println("length :: " + utf8string.length());
		return utf8string;
	}

	private boolean getCondTypesPricingDataForOrderSimulate(
			final List<DTB2BSALESORDERSIMULATERESPONSE.TTSOCONDITIONS.ZSDTSOCONDITIONS> conditionList, final String prodCode,
			final String[] condTypes, final PriceData condTypePriceData, final PriceData condTypeTotalPriceData,
			boolean isCondTypePriceAvailable, final OrderEntryData orderEntry)
	{
		if (condTypes.length > 0)
		{
			for (int i = 0; i < condTypes.length; i++)
			{
				//LOG.info("condTypes[" + i + "]: " + condTypes[i]);
				for (final ZSDTSOCONDITIONS condition : conditionList)
				{
					if (prodCode.equalsIgnoreCase(condition.getMATERIAL()) && condition.getCONDTYPE().equalsIgnoreCase(condTypes[i]))
					{
						//LOG.info("condition.getCONDTYPE(): " + condition.getCONDTYPE() + ", condTypes[" + i + "]: " + condTypes[i]);
						// COND_VALUE is base UOM price
						final Double eachUnitValue = Double.parseDouble(condition.getCONDVALUE().toString());
						final Double quantityAtBaseUOM = Double.parseDouble(condition.getCONBASEVAL().toString());
						final Double totalPriceValue = (eachUnitValue * quantityAtBaseUOM);
						condTypePriceData.setValue(new BigDecimal(totalPriceValue / orderEntry.getQuantity()));
						condTypePriceData.setCurrencyIso(condition.getCURRENCY());
						condTypeTotalPriceData.setValue(new BigDecimal(totalPriceValue));
						condTypeTotalPriceData.setCurrencyIso(condition.getCURRENCY());
						isCondTypePriceAvailable = true;
						LOG.info("Pricing Data available for Cond Type: " + condition.getCONDTYPE() + ", material: " + prodCode);
						break;
					}
				}
				if (isCondTypePriceAvailable)
				{
					break;
				}
			}
			if (isCondTypePriceAvailable)
			{
				orderEntry.setBasePrice(condTypePriceData);
				orderEntry.setTotalPrice(condTypeTotalPriceData);
				//LOG.info("Pricing updated for material: " + prodCode);
			}
		}
		return isCondTypePriceAvailable;
	}

	private boolean getCondTypesPricingDataForOrderCreate(
			final com.energizer.core.createorder.jaxb.xsd.objects.DTB2BSALESORDERCREATERESPONSE.TTSOCONDITIONS conditionArray,
			final String[] condTypes, final String modelProdCode, Double condTypeBaseUomPrice, Double condTypeBaseUomQuantity,
			Double condTypeEntryTotal, boolean isCondTypePriceAvailable, final AbstractOrderEntryModel orderEntryModel)
	{
		if (condTypes.length > 0)
		{
			for (int i = 0; i < condTypes.length; i++)
			{
				//LOG.info("condTypes[" + i + "]: " + condTypes[i]);
				for (final com.energizer.core.createorder.jaxb.xsd.objects.DTB2BSALESORDERCREATERESPONSE.TTSOCONDITIONS.ZSDTSOCONDITIONS condition : conditionArray
						.getZSDTSOCONDITIONS())
				{
					if (condition.getMATERIAL().equals(modelProdCode) && condition.getCONDTYPE().equalsIgnoreCase(condTypes[i]))
					{
						//LOG.info("condition.getCONDTYPE(): " + condition.getCONDTYPE() + ", condTypes[" + i + "]: " + condTypes[i]);
						condTypeBaseUomPrice = Double.parseDouble(condition.getCONDVALUE());
						condTypeBaseUomQuantity = Double.parseDouble(condition.getCONBASEVAL());
						condTypeEntryTotal = condTypeBaseUomPrice * condTypeBaseUomQuantity;
						isCondTypePriceAvailable = true;
						LOG.info("Pricing Data available for Cond Type: " + condition.getCONDTYPE() + ", material: " + modelProdCode);
						break;
					}
				}
				if (isCondTypePriceAvailable)
				{
					break;
				}
			}
			if (isCondTypePriceAvailable)
			{
				orderEntryModel.setBasePrice(condTypeEntryTotal / orderEntryModel.getQuantity());
				orderEntryModel.setTotalPrice(condTypeEntryTotal);
				orderEntryModel.setRejectedStatus("No");
				modelService.save(orderEntryModel);
				//LOG.info("Pricing saved for material: " + modelProdCode);
			}
		}
		return isCondTypePriceAvailable;
	}

}
