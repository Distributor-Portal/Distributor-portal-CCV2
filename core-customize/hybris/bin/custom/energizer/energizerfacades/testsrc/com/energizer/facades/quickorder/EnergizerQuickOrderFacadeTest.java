/**
 * 
 */
package com.energizer.facades.quickorder;

import static org.junit.Assert.assertEquals;

import de.hybris.bootstrap.annotations.IntegrationTest;
import de.hybris.platform.b2bacceleratorservices.company.B2BCommerceB2BUserGroupService;
import de.hybris.platform.b2bacceleratorservices.company.CompanyB2BCommerceService;
import de.hybris.platform.commercefacades.order.data.OrderEntryData;
import de.hybris.platform.commercefacades.product.data.ProductData;
import de.hybris.platform.commerceservices.setup.SetupImpexService;
import de.hybris.platform.core.Registry;
import de.hybris.platform.core.model.security.PrincipalGroupModel;
import de.hybris.platform.core.model.user.UserGroupModel;
import de.hybris.platform.jalo.JaloSession;
import de.hybris.platform.jalo.user.UserManager;
import de.hybris.platform.servicelayer.ServicelayerTransactionalTest;
import de.hybris.platform.servicelayer.model.ModelService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.energizer.core.model.EnergizerB2BCustomerModel;
import com.energizer.core.model.EnergizerB2BUnitModel;
import com.energizer.quickorder.QuickOrderData;


/**
 * /energizerfacades/test/quickOrderTestData.impex - run this impex file before running this test
 * 
 * @author kaushik.ganguly
 * 
 */
@IntegrationTest
public class EnergizerQuickOrderFacadeTest extends ServicelayerTransactionalTest
{

	private static final Logger LOG = Logger.getLogger(EnergizerQuickOrderFacadeTest.class);

	private static final String ADMIN_ID = "admin";
	private static final String TEST_EMAIL_ID = "admin@wallmart.com";
	private static final String TEST_B2BUNIT = "distributor";
	private static final String TEST_CUSTOMER_NAME = "Wallmart admin";

	@Resource
	private ModelService modelService;

	@Resource(name = "quickOrderFacade")
	private EnergizerQuickOrderFacade quickOrderFacade;

	@Resource
	private SetupImpexService setupImpexService;

	private QuickOrderData quickOrder;

	@Resource
	CompanyB2BCommerceService companyB2BCommerceService;

	@Resource
	B2BCommerceB2BUserGroupService b2bCommerceB2BUserGroupService;


	@BeforeClass
	public static void tenantStuff()
	{
		Registry.setCurrentTenantByID("junit");
	}

	@Before
	public void setUp() throws Exception
	{
		LOG.info("Creating test data ..");

		JaloSession.getCurrentSession().setUser(UserManager.getInstance().getUserByLogin(ADMIN_ID));


		setupTestB2BUnit();
		setupTestUser();
		JaloSession.getCurrentSession().setUser(UserManager.getInstance().getCustomerByLogin(TEST_EMAIL_ID));
		setupImpexService.importImpexFile("/energizerfacades/test/quickOrderTestData.impex", true);
		createSampleQuickOrder();

	}

	private void setupTestUser()
	{
		final EnergizerB2BCustomerModel customer = modelService.create(EnergizerB2BCustomerModel.class);
		customer.setUid(TEST_EMAIL_ID);
		customer.setEmail(TEST_EMAIL_ID);
		customer.setName(TEST_CUSTOMER_NAME);
		final Set<PrincipalGroupModel> groups = new HashSet<PrincipalGroupModel>();
		groups.add(companyB2BCommerceService.getUnitForUid(TEST_B2BUNIT));
		groups.add(b2bCommerceB2BUserGroupService.getUserGroupForUID("b2bgroup", UserGroupModel.class));
		groups.add(b2bCommerceB2BUserGroupService.getUserGroupForUID("b2bcustomergroup", UserGroupModel.class));
		groups.add(b2bCommerceB2BUserGroupService.getUserGroupForUID("b2badmingroup", UserGroupModel.class));

		customer.setGroups(groups);
		modelService.save(customer);

	}

	private void setupTestB2BUnit()
	{
		final EnergizerB2BUnitModel b2bUnit = modelService.create(EnergizerB2BUnitModel.class);
		b2bUnit.setUid(TEST_B2BUNIT);
		modelService.save(b2bUnit);
	}


	/**
	 * 
	 */
	private void createSampleQuickOrder()
	{
		quickOrder = new QuickOrderData();
		quickOrder.setShippingPointId("sample-shipping-point");
		final List<OrderEntryData> orderEntries = new ArrayList<OrderEntryData>();
		orderEntries.add(getSampleOrderEntry("PRD001", "abcd001", "PALLET", 2, 2));
		orderEntries.add(getSampleOrderEntry("PRD002", "abcd002", "PALLET", 2, 2));
		quickOrder.setLineItems(orderEntries);

	}


	/**
	 * @param string
	 * @param string2
	 * @param string3
	 * @param i
	 * @return
	 */
	private OrderEntryData getSampleOrderEntry(final String string, final String string2, final String string3, final int moq,
			final int i)
	{
		final OrderEntryData orderEntry = new OrderEntryData();
		final ProductData product = new ProductData();
		product.setName(string);
		product.setCode(string);
		product.setCustomerMaterialId(string2);
		product.setUom(string3);
		product.setMoq(moq);
		orderEntry.setProduct(product);
		orderEntry.setQuantity(new Long(i));
		return orderEntry;

	}

	@Test
	public void testGetQuickOrderFromSession()
	{
		LOG.info("testGetQuickOrderFromSession");
		final QuickOrderData quickOrder = quickOrderFacade.getQuickOrderFromSession(null);
		assertEquals(quickOrder != null, true);

	}


	@Test
	public void testAddItemToQuickOrder()
	{
		LOG.info("testAddItemToQuickOrder");
		quickOrderFacade.addItemToQuickOrder(quickOrder, "PRD001", null);
		assertEquals(quickOrder.getLineItems().size() == 2, true);

	}

	@Test
	public void testRemoveItemFromQuickOrder()
	{
		LOG.info("testRemoveItemFromQuickOrder");
		quickOrderFacade.removeItemFromQuickOrder(quickOrder, "PRD001");
		assertEquals(quickOrder.getLineItems().size() == 1, true);

	}

	@Test
	public void testAddInvalidProductToQuickOrder()
	{
		LOG.info("testAddItemToQuickOrder");
		quickOrderFacade.addItemToQuickOrder(quickOrder, "PRD00101", null);
		assertEquals(quickOrder.getLineItems().size() == 2, true);

	}



}
