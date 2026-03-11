/**
 *
 */
package com.energizer.services.order.dao.impl;

import de.hybris.platform.b2b.model.B2BUnitModel;
import de.hybris.platform.commerceservices.search.flexiblesearch.data.SortQueryData;
import de.hybris.platform.commerceservices.search.flexiblesearch.impl.DefaultPagedFlexibleSearchService;
import de.hybris.platform.commerceservices.search.pagedata.PageableData;
import de.hybris.platform.commerceservices.search.pagedata.SearchPageData;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.servicelayer.session.SessionService;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.log4j.Logger;

import com.energizer.services.order.dao.EnergizerB2BOrderHistoryDAO;


/**
 * @author M1028886
 *
 */
public class DefaultEnergizerB2BOrderHistoryDAO implements EnergizerB2BOrderHistoryDAO
{

	protected static final Logger LOG = Logger.getLogger(DefaultEnergizerB2BOrderHistoryDAO.class);

	@Resource(name = "pagedFlexibleSearchService")
	DefaultPagedFlexibleSearchService pagedFlexibleSearchService;

	@Resource
	protected FlexibleSearchService flexibleSearchService;

	@Resource(name = "sessionService")
	private SessionService sessionService;

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.energizer.services.order.dao.EnergizerB2BOrderHistoryDAO#getOrdersForB2BUnit(de.hybris.platform.b2b.model.
	 * B2BUnitModel)
	 */
	@Override
	public SearchPageData<OrderModel> getOrdersForB2BUnit(final B2BUnitModel unitId, final PageableData pageableData,
			final OrderStatus[] orderStatuses) throws Exception
	{
		SearchPageData<OrderModel> results = null;

		final Map queryParameters = new HashMap();
		final List sortedResults;
		queryParameters.put("name", unitId);
		queryParameters.put("statusList", Arrays.asList(orderStatuses));
		try
		{
		final String query = "SELECT {o.pk}, {o.creationtime}, {o.code} FROM { Order AS o JOIN B2BUnit AS u ON {o.Unit}= {u.pk} } WHERE {o:status} in (?statusList) AND {versionID} IS NULL ORDER BY {o.creationtime} DESC ";
			results = pagedFlexibleSearchService.search(query, queryParameters, pageableData);
		}
		catch (final Exception e)
		{
			LOG.info("Exception Occured since flexible query was not successful in fetching Orders ::: " + e);
			throw e;
		}
		return results;
	}

	protected SortQueryData getSortedResultData(final String sortCode, final String query)
	{
		final SortQueryData result = new SortQueryData();
		result.setSortCode(sortCode);
		result.setQuery(query);
		return result;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.energizer.services.order.dao.EnergizerB2BOrderHistoryDAO#getOrderHistoryForB2BUnit(java.lang.String,
	 * de.hybris.platform.core.enums.OrderStatus[])
	 */
	@Override
	public List<Object> getOrderHistoryForB2BUnit(final String unitId, final List orderStatusList) throws Exception
	{

		String queryString = null;

		final HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("uid", unitId);
		params.put("statusList", orderStatusList);

		FlexibleSearchQuery query = null;
		try
		{
		if ((boolean) sessionService.getAttribute("isSalesRepUserLoggedIn"))
		{
			queryString = "SELECT {o.code},{o.purchaseOrderNumber},{o.erpOrderNumber},{o.salesRepName},{a.erpAddressId},{a.company},{s.code},{o.creationTime},{o.totalPrice},{o.totalPrice} + {o.totalTax},{o.invoicenumber} FROM { Order AS o JOIN B2BUnit AS u ON {o.Unit}= {u.pk} Join OrderStatus as s ON {o.status}={s.pk} join address as a ON {o.deliveryAddress}={a.pk} } WHERE {s.code} NOT IN (?statusList) AND {u.uid}=?uid AND {o.versionID} IS NULL ORDER BY {o.creationtime} DESC ";

			query = new FlexibleSearchQuery(queryString, params);
			query.setResultClassList(Arrays.asList(String.class, String.class, String.class, String.class, String.class,
					String.class, String.class, Date.class, Double.class, Double.class, String.class));
		}
		else
		{
			queryString = "SELECT {o.code},{o.purchaseOrderNumber},{o.erpOrderNumber},{u.name},{s.code},{o.creationTime},{o.totalPrice} FROM { Order AS o JOIN B2BUnit AS u ON {o.Unit}= {u.pk} Join OrderStatus as s ON {o.status}={s.pk} } WHERE {s.code} NOT IN (?statusList) AND {u.uid}=?uid AND {o.versionID} IS NULL ORDER BY {o.creationtime} DESC ";

			query = new FlexibleSearchQuery(queryString, params);
			query.setResultClassList(
					Arrays.asList(String.class, String.class, String.class, String.class, String.class, Date.class, Double.class));
		}

		}
		catch (final Exception e)
		{
			LOG.info("Exception occured while fetching Order history data ::: " + e);
			throw e;
		}
		final SearchResult<Object> result = flexibleSearchService.search(query);
		return result.getResult();

	}
}
