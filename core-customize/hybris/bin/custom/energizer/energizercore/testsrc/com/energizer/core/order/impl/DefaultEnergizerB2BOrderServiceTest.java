/**
 * 
 */
package com.energizer.core.order.impl;

import de.hybris.bootstrap.annotations.UnitTest;

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
import com.energizer.services.order.dao.EnergizerB2BOrderDAO;
import com.energizer.services.order.impl.DefaultEnergizerB2BOrderService;


/**
 * @author M1023097
 * 
 */
@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultEnergizerB2BOrderServiceTest
{

	@InjectMocks
	DefaultEnergizerB2BOrderService energizerB2BOrderService = new DefaultEnergizerB2BOrderService();

	@Mock
	EnergizerB2BOrderDAO energizerB2BOrderDAO;

	@Before
	public void setUp()
	{
		MockitoAnnotations.initMocks(this);
	}

	@After
	public void tearDown() throws Exception
	{
		energizerB2BOrderService = null;
		energizerB2BOrderDAO = null;
	}

	@Test
	public void getLeadTimeDataTest()
	{
		List<EnergizerB2BUnitLeadTimeModel> models = new ArrayList<EnergizerB2BUnitLeadTimeModel>();
		final EnergizerB2BUnitLeadTimeModel model = new EnergizerB2BUnitLeadTimeModel();
		model.setLeadTime(11);
		models.add(model);
		final String modelData = "TestUnit";
		final EnergizerB2BUnitModel b2bUnitModel = new EnergizerB2BUnitModel();
		final String shippingpoint = "SHIP01";
		final String soldToAddressId = "1111";
		Mockito.when(energizerB2BOrderDAO.getLeadTimeData(b2bUnitModel, shippingpoint, soldToAddressId)).thenReturn((models));
		models = energizerB2BOrderService.getLeadTimeData(b2bUnitModel, shippingpoint, soldToAddressId);
		Assert.assertNotNull(models);
		Assert.assertTrue(models.size() > 0 && models.get(0).getLeadTime() > 0);
	}
}
