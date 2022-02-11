/**
 * 
 */
package com.energizer.core.datafeed.facade.impl;

import de.hybris.platform.cms2.model.site.CMSSiteModel;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;

import java.util.HashMap;
import java.util.List;

import javax.annotation.Resource;

import com.energizer.core.datafeed.facade.EnergizerPasswordExpiryFacade;
import com.energizer.core.model.EnergizerB2BCustomerModel;


/**
 * @author M1023278
 * 
 */
public class DefaultEnergizerPasswordExpiryFacade implements EnergizerPasswordExpiryFacade
{

	@Resource
	protected FlexibleSearchService flexibleSearchService;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.energizer.core.datafeed.facade.EnergizerPasswordExpiryFacade#getCMSSiteByName(java.lang.String)
	 */
	@Override
	public List<CMSSiteModel> getCMSSiteByName(final String siteName)
	{
		final String searchQuery = "SELECT{" + CMSSiteModel.PK + "} FROM {" + CMSSiteModel._TYPECODE + "} WHERE {"
				+ CMSSiteModel.UID + "}=?siteName ";
		final HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("siteName", siteName);
		final SearchResult<CMSSiteModel> result = flexibleSearchService.search(searchQuery, params);
		final List<CMSSiteModel> cmsSiteModels = result.getResult();
		return cmsSiteModels;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.energizer.core.datafeed.facade.EnergizerPasswordExpiryFacade#getEnergizerCustomers()
	 */
	@Override
	public List<EnergizerB2BCustomerModel> getEnergizerCustomers()
	{
		final String flexiSearchQuery = "SELECT{" + EnergizerB2BCustomerModel.PK + "} FROM {" + EnergizerB2BCustomerModel._TYPECODE
				+ "}";
		final SearchResult<EnergizerB2BCustomerModel> result = flexibleSearchService.search(flexiSearchQuery);
		final List<EnergizerB2BCustomerModel> energizerB2BCustomerModels = result.getResult();
		return energizerB2BCustomerModels;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.energizer.core.datafeed.facade.EnergizerPasswordExpiryFacade#getCustomerByUID(java.lang.String)
	 */
	@Override
	public EnergizerB2BCustomerModel getCustomerByUID(final String UID)
	{
		EnergizerB2BCustomerModel b2bCustomerModel = null;
		final String flexiSearchQuery = "SELECT{" + EnergizerB2BCustomerModel.PK + "} FROM {" + EnergizerB2BCustomerModel._TYPECODE
				+ "}  WHERE {" + EnergizerB2BCustomerModel.UID + "}=?username";
		final HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("username", UID);
		final SearchResult<EnergizerB2BCustomerModel> result = flexibleSearchService.search(flexiSearchQuery, params);
		final List<EnergizerB2BCustomerModel> energizerB2BCustomerModels = result.getResult();
		if (null != energizerB2BCustomerModels && energizerB2BCustomerModels.size() > 0)
		{
			b2bCustomerModel = energizerB2BCustomerModels.get(0);
		}
		return b2bCustomerModel;
	}


}
