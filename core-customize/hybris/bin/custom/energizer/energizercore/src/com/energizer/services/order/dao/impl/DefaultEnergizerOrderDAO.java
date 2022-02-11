package com.energizer.services.order.dao.impl;

import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;

import com.energizer.core.constants.EnergizerCoreConstants;
import com.energizer.core.model.EnergizerProductModel;
import com.energizer.services.order.dao.EnergizerOrderDAO;


/**
 * @author ROMIL JAISHWAL M1028411
 */
public class DefaultEnergizerOrderDAO implements EnergizerOrderDAO
{
	@Resource
	protected FlexibleSearchService flexibleSearchService;

	@Override
	public OrderModel findExistingOrder(final String sapOrderNo)
	{
		OrderModel energizerOrderModel = null;
		final String fsq = "SELECT{" + OrderModel.PK + "} FROM {" + OrderModel._TYPECODE + "} WHERE {" + OrderModel.ERPORDERNUMBER
				+ "}=?sapOrderId";
		final FlexibleSearchQuery fsquery = new FlexibleSearchQuery(fsq, Collections.singletonMap("sapOrderId", sapOrderNo));
		final SearchResult<OrderModel> result = flexibleSearchService.search(fsquery);
		final List<OrderModel> energizerOrderModels = result.getResult();
		if (!(energizerOrderModels.isEmpty()))
		{
			energizerOrderModel = energizerOrderModels.get(0);
		}
		return energizerOrderModel;
	}

	@Override
	public OrderModel findExistingOrder(final String sapOrderNo, final String hybrisOrderNo)
	{
		OrderModel energizerOrderModel = null;
		//Retrieve EnergizerOrder
		final String fsq = "SELECT{" + OrderModel.PK + "} from {" + OrderModel._TYPECODE + "} WHERE {" + OrderModel.CODE
				+ "}=?hybrisOrderId AND {" + OrderModel.ERPORDERNUMBER + "} =?sapOrderId";
		final HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("hybrisOrderId", hybrisOrderNo);
		params.put("sapOrderId", sapOrderNo);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(fsq, params);
		final SearchResult<OrderModel> result = flexibleSearchService.search(query);
		final List<OrderModel> energizerOrderModels = result.getResult();
		if (!energizerOrderModels.isEmpty())
		{
			energizerOrderModel = energizerOrderModels.get(0);
		}
		return energizerOrderModel;
	}

	@Override
	public OrderEntryModel findExistingOrderItem(final OrderModel energizerOrderModel,
			final EnergizerProductModel energizerProductModel)
	{
		OrderEntryModel energizerOrderEntryModel = null;
		//Retrieve energizerOrderEntryModel
		final String fsq = "SELECT{" + OrderEntryModel.PK + "} FROM {" + OrderEntryModel._TYPECODE + "} WHERE {"
				+ OrderEntryModel.ORDER + "}=?orderId AND {" + OrderEntryModel.PRODUCT + "} =?productId";
		final HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("orderId", energizerOrderModel);
		params.put("productId", energizerProductModel);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(fsq, params);
		final SearchResult<OrderEntryModel> result = flexibleSearchService.search(query);
		final List<OrderEntryModel> energizerOrderEntryModels = result.getResult();
		if (!energizerOrderEntryModels.isEmpty())
		{
			energizerOrderEntryModel = energizerOrderEntryModels.get(0);
		}
		return energizerOrderEntryModel;
	}

	@Override
	public List<OrderEntryModel> findExistingOrderItems(final OrderModel energizerOrderModel)
	{
		List<OrderEntryModel> energizerOrderEntryModels = null;
		//Retrieve energizerOrderEntryModel
		final String fsq = "SELECT{" + OrderEntryModel.PK + "} FROM {" + OrderEntryModel._TYPECODE + "} WHERE {"
				+ OrderEntryModel.ORDER + "}=?orderId";
		final HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("orderId", energizerOrderModel);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(fsq, params);
		final SearchResult<OrderEntryModel> result = flexibleSearchService.search(query);

		if (!result.getResult().isEmpty())
		{
			energizerOrderEntryModels = result.getResult();
		}
		return energizerOrderEntryModels;
	}

	@Override
	public OrderModel findHybrisOrderNoForOfflineOrder(final String sapOrderNo)
	{
		OrderModel energizerOrderModel = null;
		//Retrieve EnergizerOrder
		final String fsq = "SELECT{" + OrderModel.PK + "} from {" + OrderModel._TYPECODE + "} WHERE {" + OrderModel.ERPORDERNUMBER
				+ "} =?sapOrderId";
		final HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("sapOrderId", sapOrderNo);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(fsq, params);
		final SearchResult<OrderModel> result = flexibleSearchService.search(query);
		final List<OrderModel> energizerOrderModels = result.getResult();
		if (!energizerOrderModels.isEmpty())
		{
			energizerOrderModel = energizerOrderModels.get(0);
		}
		return energizerOrderModel;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.energizer.services.order.dao.EnergizerOrderDAO#getAllStuckOrders(de.hybris.platform.core.enums.OrderStatus,
	 * java.util.Date)
	 */
	@Override
	public List<Object> getAllStuckOrders(final List<String> status, final List<Date> dateList, final String site,
			final String region)
	{
		String queryString = null;
		boolean isWesellOrder = false;
		if (null != region && StringUtils.isNotEmpty(region) && region.equalsIgnoreCase("WESELL"))
		{
			isWesellOrder = true;
			queryString = "SELECT {o.code} AS OrderNumber,{o.versionId} AS VersionID,{ebc.uid} AS PlacedBy,{o.salesRepName} AS SalesRep,{b.uid} AS Customer,{b.name} AS CustomerName,{o.creationTime} AS CreationTime,{o.modifiedTime} AS ModifiedTime,"
					+ "{o.erpOrderNumber} AS ERPOrderNumber ,{os.code} AS Status,{o.totalPrice} AS TotalPrice,{o.totalTax} AS Totaltax,{s.name} AS Site FROM {Order AS o JOIN OrderStatus AS os ON {o.status}={os.pk} JOIN EnergizerB2BCustomer AS ebc "
					+ " ON {o.user}={ebc.pk} JOIN CMSSite as s ON {o.site}={s.pk} JOIN EnergizerB2BUnit as b ON {o.unit}={b:pk}} WHERE {os.code} IN ('ASSIGNED_TO_ADMIN','B2B_PROCESSING_ERROR') AND {o.versionId} IS NULL AND "
					+ "{o.erpOrderNumber} IS NULL AND {o.modifiedtime} >=?startDate and {o.modifiedtime} <=?endDate and {s.uid}=?siteId and {o.placedBySalesRep}=?placedBySalesRep";
		}
		else
		{
			isWesellOrder = false;
			queryString = "SELECT {o.code} AS OrderNumber,{o.versionId} AS VersionID,{ebc.uid} AS PlacedBy,{b.name} AS CustomerName,{o.creationTime} AS CreationTime,{o.modifiedTime} AS ModifiedTime,"
					+ "{o.erpOrderNumber} AS ERPOrderNumber ,{os.code} AS Status,{o.totalPrice} AS TotalPrice,{s.name} AS Site FROM {Order AS o JOIN OrderStatus AS os ON {o.status}={os.pk} JOIN EnergizerB2BCustomer AS ebc "
					+ " ON {o.user}={ebc.pk} JOIN CMSSite as s ON {o.site}={s.pk} JOIN EnergizerB2BUnit as b ON {o.unit}={b:pk}} WHERE {os.code} IN ('ASSIGNED_TO_ADMIN','B2B_PROCESSING_ERROR') AND {o.versionId} IS NULL AND "
					+ "{o.erpOrderNumber} IS NULL AND {o.modifiedtime} >=?startDate and {o.modifiedtime} <=?endDate and {s.uid}=?siteId and {o.placedBySalesRep}=?placedBySalesRep";
		}


		final HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("status", status);
		params.put("endDate", dateList.get(1));
		params.put("startDate", dateList.get(0));
		params.put("siteId", site);
		if (isWesellOrder)
		{
			params.put("placedBySalesRep", true);
		}
		else
		{
			params.put("placedBySalesRep", false);
		}
		final FlexibleSearchQuery query = new FlexibleSearchQuery(queryString, params);
		if (isWesellOrder)
		{
			query.setResultClassList(Arrays.asList(String.class, String.class, String.class, String.class, String.class,
					String.class, Date.class, Date.class,
					String.class, String.class, Double.class, Double.class, String.class));
		}
		else
		{
			query.setResultClassList(Arrays.asList(String.class, String.class, String.class, String.class, Date.class, Date.class,
					String.class, String.class, Double.class, String.class));
		}
		final SearchResult<Object> result = flexibleSearchService.search(query);

		return result.getResult();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.energizer.services.order.dao.EnergizerOrderDAO#getReprocessedOrders(java.lang.String)
	 */
	@Override
	public List<OrderModel> getReprocessedOrders()
	{
		List<OrderModel> energizerOrderModels = null;

		final String queryString = "select {o.pk} from {OrderProcess as op join order as o on {op.order}={o.pk}  join ProcessState as ps ON {op.state}={ps.pk} join orderstatus as os ON {o.status}={os.pk}} where {ps.code}=?processCode  and {op.processDefinitionName}=?processDefinitionName and {os.code}=?orderStatus and {o.readyForReprocessing} =?readyForReprocessing and {o.erpOrderNumber} IS NULL";


		final HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("processCode", EnergizerCoreConstants.ERROR);
		params.put("processDefinitionName", EnergizerCoreConstants.ACC_APPROVAL);
		params.put("orderStatus", EnergizerCoreConstants.B2B_PROCESSING_ERROR);
		params.put("readyForReprocessing", true);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(queryString, params);

		final SearchResult<OrderModel> result = flexibleSearchService.search(query);

		if (!result.getResult().isEmpty())
		{
			energizerOrderModels = result.getResult();
			return energizerOrderModels;
		}
		return null;
	}

}
