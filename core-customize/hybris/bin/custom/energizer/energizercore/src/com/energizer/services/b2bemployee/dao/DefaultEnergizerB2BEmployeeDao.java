/**
 *
 */
package com.energizer.services.b2bemployee.dao;

import de.hybris.platform.commerceservices.search.flexiblesearch.impl.DefaultPagedFlexibleSearchService;
import de.hybris.platform.commerceservices.search.pagedata.PageableData;
import de.hybris.platform.commerceservices.search.pagedata.SearchPageData;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.servicelayer.exceptions.ModelNotFoundException;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.util.StringUtils;

import com.energizer.core.model.EnergizerB2BCustomerModel;
import com.energizer.core.model.EnergizerB2BEmployeeModel;
import com.energizer.core.model.EnergizerB2BUnitModel;


/**
 * @author kaki.rajasekhar
 *
 */
public class DefaultEnergizerB2BEmployeeDao implements EnergizerB2BEmployeeDao
{

	@Resource
	private FlexibleSearchService flexibleSearchService;

	@Resource(name = "pagedFlexibleSearchService")
	private DefaultPagedFlexibleSearchService pagedFlexibleSearchService;

	/*
	 * (non-Javadoc)
	 *
	 * @see com.energizer.services.b2bemployee.dao.EnergizerB2BEmployeeDao#getEnergizerB2BUnitList(java.lang.String)
	 */
	@Override
	public List<EnergizerB2BUnitModel> getEnergizerB2BUnitList(final String userID)
	{
		final String query = "select {eb.pk} from {EnergizerB2BEmployee as e join EnergizerB2BEmployeeB2BUnitRelation as ebr on {ebr.source}= {e.pk} join EnergizerB2BUnit as eb on {eb.pk}={ebr.target}} where {e.uid}=?userID and {e.isSalesRep}=?isSalesRep and {e.active}=?active";

		final FlexibleSearchQuery flexibleSearchQuery = new FlexibleSearchQuery(query);
		flexibleSearchQuery.addQueryParameter("userID", userID);
		flexibleSearchQuery.addQueryParameter("isSalesRep", true);
		flexibleSearchQuery.addQueryParameter("active", true);

		final SearchResult<EnergizerB2BUnitModel> b2bUnitresults = flexibleSearchService.search(flexibleSearchQuery);
		if (null != b2bUnitresults)
		{
			final List<EnergizerB2BUnitModel> b2bUnitModels = b2bUnitresults.getResult();
			return b2bUnitModels;
		}
		return null;
	}

	//	WeSell Implementation -  Added Code Changes for Forgot Password/Unlock Account for Sales Rep login - by Venkat
	/*
	 * (non-Javadoc)
	 *
	 * @see com.energizer.services.b2bemployee.dao.EnergizerB2BEmployeeDao#getB2BEmployeeByToken(java.lang.String)
	 */
	@Override
	public EnergizerB2BEmployeeModel getB2BEmployeeByToken(final String token)
	{
		final String query = "SELECT {e.pk} FROM {EnergizerB2BEmployee as e} WHERE {e.token}=?token and {e.isSalesRep}=true and {e.active}=true";

		final FlexibleSearchQuery flexibleSearchQuery = new FlexibleSearchQuery(query);
		flexibleSearchQuery.addQueryParameter("token", token);
		try
		{
			return flexibleSearchService.searchUnique(flexibleSearchQuery);
		}
		catch (final ModelNotFoundException exception)
		{
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.energizer.services.b2bemployee.dao.EnergizerB2BEmployeeDao#getB2BCustomerByToken(java.lang.String)
	 */
	@Override
	public EnergizerB2BCustomerModel getB2BCustomerByToken(final String token)
	{
		{
			final String query = "SELECT {c.pk} FROM {EnergizerB2BCustomer as c} WHERE {token}=?token";

			final FlexibleSearchQuery flexibleSearchQuery = new FlexibleSearchQuery(query);
			flexibleSearchQuery.addQueryParameter("token", token);
			try
			{
				return flexibleSearchService.searchUnique(flexibleSearchQuery);
			}
			catch (final ModelNotFoundException exception)
			{
				return null;
			}

		}
	}

	//	WeSell Implementation -  Added Code Changes for Forgot Password/Unlock Account for Sales Rep login - by Venkat
	@Override
	public List<EnergizerB2BEmployeeModel> getEnergizerB2BEmployeeList()
	{
		final String query = "select {e.pk} from {EnergizerB2BEmployee as e} where {e.isSalesRep}=?isSalesRep and {e.active}=?active";

		final FlexibleSearchQuery flexibleSearchQuery = new FlexibleSearchQuery(query);
		flexibleSearchQuery.addQueryParameter("isSalesRep", true);
		flexibleSearchQuery.addQueryParameter("active", true);

		final SearchResult<EnergizerB2BEmployeeModel> activeSalesRepModelResults = flexibleSearchService
				.search(flexibleSearchQuery);
		if (null != activeSalesRepModelResults)
		{
			final List<EnergizerB2BEmployeeModel> activeSalesRepModels = activeSalesRepModelResults.getResult();
			return activeSalesRepModels;
		}
		return null;
	}


	/*
	 * (non-Javadoc)
	 *
	 * @see com.energizer.services.b2bemployee.dao.EnergizerB2BEmployeeDao#getSpecificOrdersForB2BUnit(java.lang.String,
	 * de.hybris.platform.commerceservices.search.pagedata.PageableData, de.hybris.platform.core.enums.OrderStatus[],
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public SearchPageData<OrderModel> getFilterOrdersForB2BUnit(final String unitId, final PageableData pageableData,
			final OrderStatus[] orderStatuses, final Date fromDate, final Date toDate, final boolean fetchAllOrders)
	{
		SearchPageData<OrderModel> filterOrders = null;
		try
		{
			String dateFilterQuery = null;
			final HashMap<String, Object> params = new HashMap<String, Object>();

			if ((null == fromDate || StringUtils.isEmpty(fromDate)) && !(null == toDate || StringUtils.isEmpty(toDate)))
			{
				dateFilterQuery = "AND {o.creationTime} <=?toDate";
				params.put("toDate", toDate);
			}
			else if (!(null == fromDate || StringUtils.isEmpty(fromDate)) && (null == toDate || StringUtils.isEmpty(toDate)))
			{
				dateFilterQuery = "AND {o.creationTime} >=?fromDate";
				params.put("fromDate", fromDate);
			}
			else if (!(null == fromDate || StringUtils.isEmpty(fromDate)) && !(null == toDate || StringUtils.isEmpty(toDate)))
			{
				dateFilterQuery = "AND {o.creationTime} >=?fromDate AND {o.creationTime} <=?toDate";
				params.put("fromDate", fromDate);
				params.put("toDate", toDate);
			}
			else if (fetchAllOrders)
			{
				dateFilterQuery = "";
			}

			final String query = "SELECT {o.pk} FROM { Order AS o JOIN B2BUnit AS u ON {o.Unit}= {u.pk} } WHERE {o:status} IN (?statusList) AND {u.uid}=?uid "
					+ dateFilterQuery + " AND {versionID} IS NULL ORDER BY {o.creationtime} DESC";


			params.put("uid", unitId);
			params.put("statusList", Arrays.asList(orderStatuses));

			filterOrders = pagedFlexibleSearchService.search(query, params, pageableData);
		}
		catch (final ModelNotFoundException me)
		{
			return null;
		}
		return filterOrders;
	}
}
