/**
 * 
 */
package com.energizer.services.order.dao.impl;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;

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

import com.energizer.core.model.EnergizerB2BUnitLeadTimeModel;
import com.energizer.core.model.EnergizerB2BUnitModel;


/**
 * @author M1023097
 * 
 */
@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultEnergizerB2BOrderDAOTest
{
	@InjectMocks
	DefaultEnergizerB2BOrderDAO energizerB2BOrderDAO = new DefaultEnergizerB2BOrderDAO();
	@Mock
	FlexibleSearchService flexibleSearchService;

	@Before
	public void setUp()
	{
		MockitoAnnotations.initMocks(this);
	}

	@After
	public void tearDown() throws Exception
	{
		flexibleSearchService = null;
		energizerB2BOrderDAO = null;
	}

	@Test
	public void getLeadTimeDataTest()
	{
		List<EnergizerB2BUnitLeadTimeModel> models = new ArrayList<EnergizerB2BUnitLeadTimeModel>();
		final String productQuery = "Query to get data from DB";
		final String modelData = "TestUnit";
		final String shippingpoint = "SHIP01";
		final String soldToAddressId = "1111";
		final EnergizerB2BUnitModel b2bUnitModel = new EnergizerB2BUnitModel();
		final FlexibleSearchQuery query = new FlexibleSearchQuery(productQuery);
		Mockito.when(flexibleSearchService.<EnergizerB2BUnitLeadTimeModel> search(query).getResult()).thenReturn((models));
		models = energizerB2BOrderDAO.getLeadTimeData(b2bUnitModel, shippingpoint, soldToAddressId);
		Assert.assertNotNull(models);
	}
}
