/**
 * 
 */
package com.energizer.core.solr.query;

import static org.junit.Assert.assertEquals;

import de.hybris.bootstrap.annotations.IntegrationTest;
import de.hybris.platform.b2bacceleratorservices.company.B2BCommerceB2BUserGroupService;
import de.hybris.platform.b2bacceleratorservices.company.CompanyB2BCommerceService;
import de.hybris.platform.commerceservices.setup.SetupImpexService;
import de.hybris.platform.core.Registry;
import de.hybris.platform.core.model.security.PrincipalGroupModel;
import de.hybris.platform.core.model.user.UserGroupModel;
import de.hybris.platform.jalo.JaloSession;
import de.hybris.platform.jalo.user.UserManager;
import de.hybris.platform.servicelayer.ServicelayerTransactionalTest;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.user.UserService;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.energizer.core.model.EnergizerB2BCustomerModel;
import com.energizer.core.model.EnergizerB2BUnitModel;


/**
 * 
 * 
 * @author kaushik.ganguly
 * 
 */

@IntegrationTest
public class EnergizerSolrQueryManipulationServiceTest extends ServicelayerTransactionalTest
{

	private static final Logger LOG = Logger.getLogger(EnergizerSolrQueryManipulationServiceTest.class);

	private static final String ADMIN_ID = "admin";
	private static final String TEST_EMAIL_ID = "admin@wallmart.com";
	private static final String TEST_CUSTOMER_NAME = "Wallmart admin";
	private static final String TEST_B2BUNIT = "distributor";

	@Resource
	private ModelService modelService;

	@Resource
	private EnergizerSolrQueryManipulationService energizerSolrQueryManipulationService;

	@Resource
	private UserService userService;

	@Resource
	CompanyB2BCommerceService companyB2BCommerceService;

	@Resource
	B2BCommerceB2BUserGroupService b2bCommerceB2BUserGroupService;

	@Resource
	private SetupImpexService setupImpexService;

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
		setupImpexService.importImpexFile("/energizercore/test/solrQueryManipulationTestData.impex", true);

		JaloSession.getCurrentSession().setUser(UserManager.getInstance().getCustomerByLogin(TEST_EMAIL_ID));
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

	@Test
	public void testGetSolrQueryForCategorySearchWithSearchTerm()
	{
		LOG.info("testGetSolrQueryForCategorySearch");
		final String solrQuery = energizerSolrQueryManipulationService.getSolrQueryForCategorySearch(null, "drill");
		assertEquals(solrQuery.indexOf("b2bunit:distributor") != -1, true);

	}

	@Test
	public void testGetSolrQueryForCategorySearchWithoutSortCode()
	{
		LOG.info("testGetSolrQueryForCategorySearch");
		final String solrQuery = energizerSolrQueryManipulationService.getSolrQueryForCategorySearch(null, "");
		assertEquals(solrQuery.indexOf("b2bunit:distributor") != -1, true);

	}

	@Test
	public void testGetSolrQueryForCategorySearchWithoutSortCodeAndTerm()
	{
		LOG.info("testGetSolrQueryForCategorySearch");
		final String solrQuery = energizerSolrQueryManipulationService.getSolrQueryForCategorySearch(null, null);
		assertEquals(solrQuery.indexOf("b2bunit:distributor") != -1, true);

	}

	@Test
	public void testGetSolrQueryForCategorySearchUsingDifferentSortCode()
	{
		LOG.info("testGetSolrQueryForCategorySearch");
		final String solrQuery = energizerSolrQueryManipulationService.getSolrQueryForCategorySearch("topRated", null);
		assertEquals(solrQuery.indexOf("b2bunit:distributor") != -1 && solrQuery.indexOf("topRated") != -1, true);

	}


	@Test
	public void testGetSolrQueryForCategorySearchWithPricingInTheQuery()
	{
		LOG.info("testGetSolrQueryForCategorySearch");
		final String solrQuery = energizerSolrQueryManipulationService.getSolrQueryForCategorySearch("relevance",
				":relevance:b2bunit:wallmart:price:$0-$49.99");
		assertEquals(solrQuery.indexOf("b2bunit:distributor") != -1, true);

	}

	@Test
	public void testGetSolrQueryForCategorySearchWithPriceWithoutB2BUnit()
	{
		LOG.info("testGetSolrQueryForCategorySearch");
		final String solrQuery = energizerSolrQueryManipulationService.getSolrQueryForCategorySearch("topRated",
				":relevance:price:$0-$49.99");
		assertEquals(solrQuery.indexOf("b2bunit:distributor") != -1, true);

	}


	@Test
	public void testGetB2BUnitForLoggedInUser()
	{
		LOG.info("testGetB2BUnitForLoggedInUser");
		final String b2bUnitUID = energizerSolrQueryManipulationService.getB2BUnitForLoggedInUser().getUid();
		assertEquals(b2bUnitUID, "distributor");

	}

	@Test
	public void testGetB2BUnitInSolrQuery()
	{
		LOG.info("testGetB2BUnitInSolrQuery");
		final String solrQuery = energizerSolrQueryManipulationService.getB2BUnitInSolrQuery();
		assertEquals(solrQuery, "b2bunit:distributor");

	}

}
