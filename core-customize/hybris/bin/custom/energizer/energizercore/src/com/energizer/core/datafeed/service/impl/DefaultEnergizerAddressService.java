/**
 *
 */
package com.energizer.core.datafeed.service.impl;

import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;

import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;

import com.energizer.core.datafeed.service.EnergizerAddressService;




/**
 * @author M1023278
 *
 */
public class DefaultEnergizerAddressService implements EnergizerAddressService
{
	protected static final Logger LOG = Logger.getLogger(DefaultEnergizerAddressService.class);

	@Resource
	private FlexibleSearchService flexibleSearchService;

	@Override
	public List<AddressModel> fetchAddress(final String erpAddressId)
	{
		List<AddressModel> addressModelList = null;
		try
		{
			final FlexibleSearchQuery retreiveQuery = new FlexibleSearchQuery(
					"SELECT {pk} FROM {Address} where {" + AddressModel.ERPADDRESSID + "}=?erpAddressId");

			retreiveQuery.addQueryParameter("erpAddressId", erpAddressId);

			final SearchResult<AddressModel> result = flexibleSearchService.search(retreiveQuery);
			addressModelList = result.getResult();
		}
		catch (final Exception e)
		{
			LOG.error("Exception occured while fetching address for erpAddressId ...." + e);
			throw e;
		}
		return addressModelList;

	}

	@Override
	public List<AddressModel> fetchAddressForB2BUnit(final String b2bUnitUId)
	{
		List<AddressModel> addressModelList = null;
		try
		{
			final FlexibleSearchQuery retreiveQuery = new FlexibleSearchQuery(
					"SELECT {pk} FROM {Address as p join energizerb2bunit as b on {p.OWNER}={b.PK}} where {b:uid}=?b2bUnitUId");
			retreiveQuery.addQueryParameter("b2bUnitUId", b2bUnitUId);

			final SearchResult<AddressModel> result = flexibleSearchService.search(retreiveQuery);
			addressModelList = result.getResult();
		}
		catch (final Exception e)
		{
			LOG.error("Exception occured while fetching list of addresses for b2bUnit ...");
			throw e;
		}
		return addressModelList;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.energizer.core.datafeed.service.EnergizerAddressService#fetchAddressForB2BUnitAndErpAddressID(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public List<AddressModel> fetchAddressForB2BUnitAndErpAddressID(final String b2bUnitId, final String erpAddressId)
	{
		List<AddressModel> addressModelList = null;
		try
		{
			final FlexibleSearchQuery retreiveQuery = new FlexibleSearchQuery(
					"SELECT {pk} FROM {Address as p join EnergizerB2BUnit as b on {p.OWNER}={b.PK}} where {b.uid}=?b2bUnitId and {p.erpAddressId}=?erpAddressId");
			retreiveQuery.addQueryParameter("b2bUnitId", b2bUnitId);
			retreiveQuery.addQueryParameter("erpAddressId", erpAddressId);

			final SearchResult<AddressModel> result = flexibleSearchService.search(retreiveQuery);
			addressModelList = result.getResult();
		}
		catch (final Exception e)
		{
			LOG.error("Exception occured while fetching list of addresses for b2bunit and erpAddressID ...");
			throw e;
		}
		return addressModelList;
	}
}
