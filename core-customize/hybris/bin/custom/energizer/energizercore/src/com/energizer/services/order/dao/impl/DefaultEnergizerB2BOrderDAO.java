/**
 *
 */
package com.energizer.services.order.dao.impl;

import de.hybris.platform.commerceservices.strategies.DeliveryAddressesLookupStrategy;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;

import com.energizer.core.model.EnergizerB2BUnitLeadTimeModel;
import com.energizer.core.model.EnergizerB2BUnitModel;
import com.energizer.services.order.dao.EnergizerB2BOrderDAO;



/**
 * @author M1023097
 * 
 */
public class DefaultEnergizerB2BOrderDAO implements EnergizerB2BOrderDAO
{

	protected static final Logger LOG = Logger.getLogger(DefaultEnergizerB2BOrderDAO.class);

	@Resource(name = "flexibleSearchService")
	private FlexibleSearchService flexibleSearchService;

	@Resource(name = "defaultB2BDeliveryAddressesLookupStrategy")
	DeliveryAddressesLookupStrategy defaultB2BDeliveryAddressesLookupStrategy;

	String productQuery;
	FlexibleSearchQuery query;

	/*
	 * (non-Javadoc)
	 *
	 * @see com.energizer.daos.order.EnergizerB2BOrderDAO#getLeadTimeData(java.lang.String)
	 */
	@Override
	public List<EnergizerB2BUnitLeadTimeModel> getLeadTimeData(final EnergizerB2BUnitModel b2bUnitModel,
			final String shippingPointId, final String soldToAddressId)
	{
		List<EnergizerB2BUnitLeadTimeModel> models = new ArrayList<EnergizerB2BUnitLeadTimeModel>();
		try
		{
		productQuery = "SELECT { " + EnergizerB2BUnitLeadTimeModel.PK + " } " + " FROM { "
				+ EnergizerB2BUnitLeadTimeModel._TYPECODE + "}" + "  WHERE {" + EnergizerB2BUnitLeadTimeModel.SHIPPINGPOINTID + "}='"
				+ shippingPointId + "'" + " AND {" + EnergizerB2BUnitLeadTimeModel.SOLDTOADDRESSID + "}='" + soldToAddressId
				+ "' AND {" + EnergizerB2BUnitLeadTimeModel.B2BUNITID + "}=?b2bUnit";
			query = new FlexibleSearchQuery(productQuery);
			query.addQueryParameter("b2bUnit", b2bUnitModel);
		 models =flexibleSearchService.<EnergizerB2BUnitLeadTimeModel> search(query).getResult();
		}
		 catch(final Exception e) {
			LOG.error("Exception Occured while fetching lead time data...." + e);
			throw e;
		 }
		return models;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.energizer.services.order.dao.EnergizerB2BOrderDAO#getDeliveryAddress(java.lang.String)
	 */
	@Override
	public List<String> getsoldToAddressIds(final EnergizerB2BUnitModel b2bUnitModel, final String shippingPointId)
	{
		final List<String> soldToAddressIds = new ArrayList<String>();
		List<String> soldToIds = new ArrayList<String>();

		productQuery = "SELECT { " + EnergizerB2BUnitLeadTimeModel.SOLDTOADDRESSID + " } " + " FROM { "
				+ EnergizerB2BUnitLeadTimeModel._TYPECODE + "}" + "  WHERE { " + EnergizerB2BUnitLeadTimeModel.B2BUNITID
				+ "}=?b2bUnit " + " AND {" + EnergizerB2BUnitLeadTimeModel.SHIPPINGPOINTID + "}=?shippingPointId";

		query = new FlexibleSearchQuery(productQuery);
		query.setResultClassList(Collections.singletonList(String.class));
		query.addQueryParameter("b2bUnit", b2bUnitModel);
		query.addQueryParameter("shippingPointId", shippingPointId);

		soldToIds = flexibleSearchService.<String> search(query).getResult();
		for (final String soldToId : soldToIds)
		{
			soldToAddressIds.add(soldToId);
		}
		return soldToAddressIds;
	}

}
