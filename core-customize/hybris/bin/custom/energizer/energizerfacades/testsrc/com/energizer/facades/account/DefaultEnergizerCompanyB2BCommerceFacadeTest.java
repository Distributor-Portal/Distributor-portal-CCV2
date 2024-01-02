/**
 * 
 */
package com.energizer.facades.account;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.b2b.constants.B2BConstants;
import de.hybris.platform.b2b.services.B2BUnitService;
import de.hybris.platform.b2bacceleratorfacades.order.populators.B2BCustomerReversePopulator;
import de.hybris.platform.b2bacceleratorservices.company.B2BCommerceUserService;
import de.hybris.platform.b2bacceleratorservices.company.CompanyB2BCommerceService;
import de.hybris.platform.commercefacades.user.data.CustomerData;
import de.hybris.platform.commerceservices.customer.CustomerAccountService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.user.UserService;

import java.util.ArrayList;
import java.util.Collection;

import junit.framework.Assert;

import org.apache.commons.lang.math.NumberUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import com.energizer.core.model.EnergizerB2BCustomerModel;
import com.energizer.core.model.EnergizerB2BUnitModel;
import com.energizer.facades.accounts.impl.DefaultEnergizerCompanyB2BCommerceFacade;


/**
 * @author M1023097
 *
 */
@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultEnergizerCompanyB2BCommerceFacadeTest
{
	@InjectMocks
	DefaultEnergizerCompanyB2BCommerceFacade energizerCompanyB2BCommerceFacade = new DefaultEnergizerCompanyB2BCommerceFacade();
	@Mock
	UserService userService;

	@Mock
	B2BCommerceUserService b2bUserService;

	@Mock
	CustomerAccountService customerAccountService;

	@Mock
	B2BUnitService defaultB2BUnitService;

	@Mock
	B2BCustomerReversePopulator b2BCustomerReversePopulator;

	@Mock
	CompanyB2BCommerceService companyB2BCommerceService;

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
		b2BCustomerReversePopulator = null;
		companyB2BCommerceService = null;
	}

	@Test(expected = Test.None.class /* no exception expected */)
	public void getContactNumberTest(final String uuid)
	{
		final EnergizerB2BCustomerModel model = new EnergizerB2BCustomerModel();
		model.setUid("test@user.com");
		//final String contactNumber = energizerCompanyB2BCommerceFacade.getContactNumber(model.getUid(), new CustomerData());
		//Assert.assertNotNull(contactNumber);
	}


	@Test
	public void getB2BUnitForLoggedInUserTest()
	{
		final EnergizerB2BUnitModel b2BUnitModel;
		final EnergizerB2BCustomerModel customer = new EnergizerB2BCustomerModel();
		b2BUnitModel = b2bUserService.getParentUnitForCustomer(customer.getUid());
		Assert.assertNotNull(b2BUnitModel);
	}

	@Test(expected = Test.None.class /* no exception expected */)
	public void validateUserCountTest()
	{
		final EnergizerB2BUnitModel b2BUnitModel = new EnergizerB2BUnitModel();
		Mockito.when(
				defaultB2BUnitService.getB2BCustomers(b2BUnitModel).size() == NumberUtils.toInt(b2BUnitModel.getMaxUserLimit()))
				.thenReturn(true);
	}

	@Test(expected = Test.None.class /* no exception expected */)
	public void updateProfileTest()
	{
		final EnergizerB2BCustomerModel customer = new EnergizerB2BCustomerModel();
		customer.setUid("user@test.com");
		customer.setContactNumber("33333333");
		customer.setName("test User");
		modelService.save(customer);
	}

	@Test
	public void findB2BAdministratorForCustomerTest()
	{
		Collection<EnergizerB2BCustomerModel> adminList = new ArrayList<EnergizerB2BCustomerModel>();
		final EnergizerB2BUnitModel b2BUnitModel = new EnergizerB2BUnitModel();
		adminList = defaultB2BUnitService.getUsersOfUserGroup(b2BUnitModel, B2BConstants.B2BADMINGROUP, true);
		Assert.assertNotNull(adminList);
	}

	@Test(expected = Test.None.class /* no exception expected */)
	public void updateCustomerTest()
	{
		final EnergizerB2BCustomerModel customer = new EnergizerB2BCustomerModel();
		final CustomerData customerData = new CustomerData();
		customerData.setFirstName("Test");
		customerData.setLastName("User");
		customerData.setEmail("user@test.com");
		customerData.setContactNumber("11111111");
		b2BCustomerReversePopulator.populate(customerData, customer);
		modelService.save(customer);
	}
}