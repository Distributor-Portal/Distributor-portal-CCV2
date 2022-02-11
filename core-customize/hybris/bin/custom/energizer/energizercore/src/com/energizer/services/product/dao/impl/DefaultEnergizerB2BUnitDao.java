/**
 * 
 */
package com.energizer.services.product.dao.impl;

import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;

import java.util.List;

import javax.annotation.Resource;

import com.energizer.core.model.EnergizerB2BUnitModel;
import com.energizer.services.product.dao.EnergizerB2BUnitDao;


/**
 * @author Geetika Singh
 * 
 */
public class DefaultEnergizerB2BUnitDao implements EnergizerB2BUnitDao
{
	@Resource
	private FlexibleSearchService flexibleSearchService;

	public List<EnergizerB2BUnitModel> findB2BUnitForSalesArea(final String salesOrganisation, final String distributionChannel,
			final String division)
	{
		final FlexibleSearchQuery query = new FlexibleSearchQuery("select {" + EnergizerB2BUnitModel.PK + "}" + "from {"
				+ EnergizerB2BUnitModel._TYPECODE + "} WHERE {" + EnergizerB2BUnitModel.SALESORGANISATION
				+ "}=?salesOrganisation AND" + "{" + EnergizerB2BUnitModel.DISTRIBUTIONCHANNEL + "}=?distributionChannel AND" + "{"
				+ EnergizerB2BUnitModel.DIVISION + "}=?division");

		query.addQueryParameter("salesOrganisation", salesOrganisation);
		query.addQueryParameter("distributionChannel", distributionChannel);
		query.addQueryParameter("division", division);


		final SearchResult<EnergizerB2BUnitModel> b2bUnitresults = flexibleSearchService.search(query);
		final List<EnergizerB2BUnitModel> b2bUnitModels = b2bUnitresults.getResult();

		return b2bUnitModels;

	}

}
