/**
 *
 */
package com.energizer.core.solr.query.impl;

import de.hybris.platform.b2b.company.B2BCommerceUserService;
import de.hybris.platform.servicelayer.user.UserService;

import javax.annotation.Resource;

import org.apache.log4j.Logger;

import com.energizer.core.model.EnergizerB2BUnitModel;
import com.energizer.core.solr.query.EnergizerSolrQueryManipulationService;
import com.energizer.core.solr.query.EnergizerSolrQueryManipulationServiceTest;



/**
 * This class implements the EnergizerSolrQueryManipulationService to define the logic for manipulating the solr query
 * to enforce the b2bunit specific catalog.
 * 
 * @author kaushik.ganguly
 * @see EnergizerSolrQueryManipulationServiceTest
 */
public class DefaultEnergizerSolrQueryManipulationService implements EnergizerSolrQueryManipulationService
{

	protected static final Logger LOG = Logger.getLogger(DefaultEnergizerSolrQueryManipulationService.class);

	@Resource(name = "b2bCommerceUserService")
	private B2BCommerceUserService b2bUserService;

	@Resource(name = "userService")
	private UserService userService;


	/**
	 * This method retrieves the EnergizerB2BUnitModel of the currently logged in user.
	 * 
	 * @see EnergizerB2BUnitModel
	 * @return EnergizerB2BUnitModel
	 */
	@Override
	public EnergizerB2BUnitModel getB2BUnitForLoggedInUser()
	{
		EnergizerB2BUnitModel b2bUnitModel = null;
		try {
			b2bUnitModel = b2bUserService.getParentUnitForCustomer(userService.getCurrentUser().getUid());
		}
		catch (final Exception e)
		{
			LOG.error("Exception occured while fetching b2bunit for the logged in user....." + e);
			throw e;
		}
		return b2bUnitModel;
	}

	/**
	 * This method solr query part involving the b2bunit
	 * 
	 * @return String
	 */
	@Override
	public String getB2BUnitInSolrQuery()
	{
		final EnergizerB2BUnitModel b2bUnit = getB2BUnitForLoggedInUser();
		if (b2bUnit != null)
		{
			return B2B_UNIT_SEARCHQUERY_PREFIX + QUERY_CONNECTOR + b2bUnit.getUid();
		}
		else
		{
			return EMPTY_STRING;
		}

	}

	/**
	 * This method manipulates the SOLR query for the category listing page.
	 * 
	 * @param sortCode
	 *           This is the sort code selected by the user in the category listing page.
	 * @param existingQuery
	 *           This is the already constructed SOLR query that needs manipulation
	 * @return String
	 */
	@Override
	public String getSolrQueryForCategorySearch(final String sortCode, final String existingQuery)
	{
		try
		{
		final String b2bUnit = getB2BUnitInSolrQuery();
		final EnergizerB2BUnitModel b2bUnitModel = getB2BUnitForLoggedInUser();
		String retVal = EMPTY_STRING;
		if (b2bUnitModel == null)
		{
			return retVal;
		}

		if (existingQuery == null || existingQuery.isEmpty())
		{
			if (sortCode != null)
			{
				retVal = EMPTY_STRING + QUERY_CONNECTOR + sortCode + QUERY_CONNECTOR + b2bUnit;
			}
			else
			{
				retVal = EMPTY_STRING + QUERY_CONNECTOR + B2B_UNIT_EXTRAFILTER_PREFIX + QUERY_CONNECTOR + b2bUnit;
			}

		}
		else
		{

			if (sortCode != null && existingQuery.indexOf(b2bUnitModel.getUid() + QUERY_CONNECTOR + sortCode) == -1)
			{
				final String strippingSortOrderfromQuery = existingQuery.substring(
						existingQuery.indexOf(QUERY_CONNECTOR, existingQuery.indexOf(QUERY_CONNECTOR) + 1) + 1, existingQuery.length());
				retVal = EMPTY_STRING + QUERY_CONNECTOR + sortCode + QUERY_CONNECTOR + strippingSortOrderfromQuery;

				if (retVal.indexOf(QUERY_CONNECTOR + b2bUnit) == -1)
				{
					retVal = retVal + QUERY_CONNECTOR + b2bUnit;
				}

			}
			else if (sortCode != null && existingQuery.indexOf(b2bUnitModel.getUid() + QUERY_CONNECTOR + sortCode) != -1)
			{
				retVal = existingQuery;

				if (retVal.indexOf(QUERY_CONNECTOR + b2bUnit) == -1)
				{
					retVal = retVal + QUERY_CONNECTOR + b2bUnit;
				}
			}
			else if (sortCode == null)
			{
				final String strippingSortOrderfromQuery = existingQuery.substring(
						existingQuery.indexOf(QUERY_CONNECTOR, existingQuery.indexOf(QUERY_CONNECTOR) + 1) + 1, existingQuery.length());
				retVal = EMPTY_STRING + QUERY_CONNECTOR + B2B_UNIT_EXTRAFILTER_PREFIX + QUERY_CONNECTOR + strippingSortOrderfromQuery;

				if (retVal.indexOf(QUERY_CONNECTOR + b2bUnit) == -1)
				{
					retVal = retVal + QUERY_CONNECTOR + b2bUnit;
				}
			}



		}
		return retVal;
		}
		catch (final Exception e)
		{
			// YTODO: handle exception
			LOG.error("Solr Query Exception ", e);
			throw e;
		}
	}


	/**
	 * This method manipulates the SOLR query for the search listing page.
	 * 
	 * @param sortCode
	 *           This is the sort code selected by the user in the category listing page.
	 * @param existingQuery
	 *           This is the already constructed SOLR query that needs manipulation
	 * 
	 * @return String
	 */
	@Override
	public String getSolrQueryForTextSearchPage(final String sortCode, final String existingQuery)
	{
		try
		{
		final String b2bUnit = getB2BUnitInSolrQuery();
		final EnergizerB2BUnitModel b2bUnitModel = getB2BUnitForLoggedInUser();
		String retVal = EMPTY_STRING;
		if (b2bUnitModel == null)
		{
			return retVal;
		}

		if (existingQuery == null || existingQuery.isEmpty())
		{
			if (sortCode != null)
			{
				retVal = ((existingQuery == null) ? EMPTY_STRING : existingQuery) + QUERY_CONNECTOR + sortCode + QUERY_CONNECTOR
						+ b2bUnit;
			}
			else
			{
				retVal = ((existingQuery == null) ? EMPTY_STRING : existingQuery) + QUERY_CONNECTOR + B2B_UNIT_EXTRAFILTER_PREFIX
						+ QUERY_CONNECTOR + b2bUnit;

			}

		}
		else
		{
			if (sortCode == null && existingQuery.indexOf(QUERY_CONNECTOR) == -1)
			{
				retVal = existingQuery + QUERY_CONNECTOR + B2B_UNIT_EXTRAFILTER_PREFIX + QUERY_CONNECTOR + b2bUnit;

			}
			if (sortCode != null && existingQuery.indexOf(QUERY_CONNECTOR) == -1)
			{

				retVal = existingQuery + QUERY_CONNECTOR + sortCode + QUERY_CONNECTOR + b2bUnit;
			}
			else if (sortCode != null && existingQuery.indexOf(QUERY_CONNECTOR) != -1)
			{

				retVal = existingQuery;

				if (existingQuery.indexOf(b2bUnit) == -1)
				{
					retVal = retVal + QUERY_CONNECTOR + b2bUnit;
				}

			}
			else if (sortCode == null && existingQuery.indexOf(QUERY_CONNECTOR) != -1)
			{

				retVal = existingQuery;

				if (existingQuery.indexOf(b2bUnit) == -1)
				{
					retVal = retVal + QUERY_CONNECTOR + b2bUnit;
				}

			}


		}
			return retVal;
		}
		catch (final Exception e)
		{
			LOG.error("Solr Query Exception ", e);
			throw e;
		}
	}


}
