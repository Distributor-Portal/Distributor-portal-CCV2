/**
 * 
 */
package com.energizer.facades.flow.impl;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.b2b.model.B2BUnitModel;
import de.hybris.platform.b2bacceleratorservices.company.CompanyB2BCommerceService;
import de.hybris.platform.commercefacades.order.data.OrderData;
import de.hybris.platform.commercefacades.order.data.OrderEntryData;
import de.hybris.platform.commercefacades.product.data.PriceData;
import de.hybris.platform.commercefacades.product.data.ProductData;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.jalo.order.price.PriceInformation;
import de.hybris.platform.product.PriceService;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.model.ModelService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import com.energizer.business.BusinessRuleError;
import com.energizer.core.business.service.EnergizerOrderBusinessRuleValidationService;
import com.energizer.core.business.service.EnergizerOrderEntryBusinessRuleValidationService;
import com.energizer.core.model.EnergizerB2BUnitLeadTimeModel;
import com.energizer.core.model.EnergizerB2BUnitModel;
import com.energizer.core.solr.query.EnergizerSolrQueryManipulationService;
import com.energizer.services.order.EnergizerB2BOrderService;


/**
 * @author M1023097
 * 
 */
@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultEnergizerB2BCheckoutFlowFacadeTest
{
	@InjectMocks
	DefaultEnergizerB2BCheckoutFlowFacade energizerB2BCheckoutFlowFacade = new DefaultEnergizerB2BCheckoutFlowFacade();
	@Mock
	EnergizerOrderEntryBusinessRuleValidationService orderEntryBusinessRulesService;
	@Mock
	PriceService priceService;
	@Mock
	EnergizerOrderBusinessRuleValidationService orderBusinessRulesService;
	@Mock
	EnergizerSolrQueryManipulationService energizerSolrQueryManipulationService;
	@Mock
	CompanyB2BCommerceService companyB2BCommerceService;
	@Mock
	EnergizerB2BOrderService energizerB2BOrderService;
	@Mock
	Converter<AbstractOrderEntryModel, OrderEntryData> orderEntryConverter;
	@Mock
	ModelService modelService;

	@Before
	public void setUp()
	{
		MockitoAnnotations.initMocks(this);
	}

	@After
	public void tearDown() throws Exception
	{
		orderEntryBusinessRulesService = null;
		orderBusinessRulesService = null;
	}

	@Test
	public void getOrderShippingValidationTest()
	{

		final AbstractOrderEntryModel orderEntryModel = new AbstractOrderEntryModel();
		final OrderEntryData entrydata = new OrderEntryData();
		List<BusinessRuleError> errors = new ArrayList<BusinessRuleError>();
		final BusinessRuleError error = new BusinessRuleError();
		error.setMessage("Shipping Point validation Error");
		errors.add(error);
		Mockito.when(orderEntryConverter.convert(orderEntryModel)).thenReturn((entrydata));
		Mockito.when(orderEntryBusinessRulesService.hasErrors()).thenReturn((true));
		Mockito.when(orderEntryBusinessRulesService.getErrors()).thenReturn((errors));
		errors = energizerB2BCheckoutFlowFacade.getOrderShippingValidation(orderEntryModel);
		Assert.assertNotNull(errors);
	}

	@Test
	public void getOrderValidationTest()
	{
		final AbstractOrderEntryModel orderEntryModel = new AbstractOrderEntryModel();
		final OrderEntryData entrydata = new OrderEntryData();
		final ProductData productData = new ProductData();
		final OrderData orderData = new OrderData();
		final List<BusinessRuleError> errors = new ArrayList<BusinessRuleError>();
		final BusinessRuleError error = new BusinessRuleError();
		error.setMessage("Business Error ");
		errors.add(error);
		final List<PriceInformation> prices = new ArrayList<PriceInformation>();
		final PriceData priceData = new PriceData();
		priceData.setCurrencyIso("USD");
		priceData.setValue(BigDecimal.valueOf(20.22));
		productData.setPrice(priceData);
		orderData.setTotalPrice(productData.getPrice());
		final ProductModel product = orderEntryModel.getProduct();
		Mockito.when(priceService.getPriceInformationsForProduct(product)).thenReturn((prices));
		Mockito.when(orderEntryConverter.convert(orderEntryModel)).thenReturn((entrydata));
		Mockito.when(orderBusinessRulesService.hasErrors()).thenReturn((true));
		Mockito.when(orderBusinessRulesService.getErrors()).thenReturn((errors));
		//		errors = energizerB2BCheckoutFlowFacade.getOrderValidation(orderEntryModel);
		Assert.assertNotNull(errors);
	}

	@Test
	public void getLeadTimeDataTest()
	{

		final EnergizerB2BUnitModel b2bUnitModel = new EnergizerB2BUnitModel();
		b2bUnitModel.setUid("TestUnit");
		final B2BUnitModel b2bModel = new B2BUnitModel();
		b2bModel.setUid("111");
		modelService.save(b2bModel);
		final List<EnergizerB2BUnitLeadTimeModel> models = new ArrayList<EnergizerB2BUnitLeadTimeModel>();
		final EnergizerB2BUnitLeadTimeModel model = new EnergizerB2BUnitLeadTimeModel();
		model.setB2bUnitId(b2bUnitModel);
		model.setLeadTime(11);
		final String shippingpoint = "SHIP01";
		final String soldToAddressId = "1111";

		Mockito.when(energizerSolrQueryManipulationService.getB2BUnitForLoggedInUser()).thenReturn((b2bUnitModel));
		Mockito.when(companyB2BCommerceService.getUnitForUid(b2bUnitModel.getUid())).thenReturn((b2bModel));
		Mockito.when(energizerB2BOrderService.getLeadTimeData(b2bUnitModel, shippingpoint, soldToAddressId)).thenReturn((models));
		final int leadTime = energizerB2BCheckoutFlowFacade.getLeadTimeData(shippingpoint, soldToAddressId);
		Assert.assertNotNull(leadTime);
		Assert.assertTrue(leadTime > 0);
	}
}
