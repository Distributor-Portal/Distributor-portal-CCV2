/**
 *
 */
package com.energizer.services.order.impl;

import de.hybris.platform.b2b.model.B2BUnitModel;
import de.hybris.platform.commerceservices.search.pagedata.PageableData;
import de.hybris.platform.commerceservices.search.pagedata.SearchPageData;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderModel;

import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;

import com.energizer.services.order.EnergizerB2BOrderHistoryService;
import com.energizer.services.order.dao.impl.DefaultEnergizerB2BOrderHistoryDAO;



/**
 * @author M1028886
 *
 */
public class DefaultEnergizerB2BOrderHistoryService implements EnergizerB2BOrderHistoryService
{
	protected static final Logger LOG = Logger.getLogger(DefaultEnergizerB2BOrderHistoryService.class);

	@Resource(name = "defaultEnergizerB2BOrderHistoryDAO")
	private DefaultEnergizerB2BOrderHistoryDAO orderDAO;

	/*
	 * (non-Javadoc)
	 *
	 * @see com.energizer.services.order.EnergizerB2BOrderHistoryService#getOrdersForB2BUnit(de.hybris.platform.b2b.model.
	 * B2BUnitModel)
	 */
	@Override
	public SearchPageData<OrderModel> getOrdersForB2BUnit(final B2BUnitModel unitId, final PageableData pageableData,
			final OrderStatus[] orderStatuses) throws Exception
	{
		SearchPageData<OrderModel> ordersHistoryList = null;
		try
		{
		ordersHistoryList = orderDAO.getOrdersForB2BUnit(unitId, pageableData, orderStatuses);
		}
		catch (final Exception e)
		{
			LOG.info("Exception occured while getting orders :::: " + e);
			throw e;
		}

		return ordersHistoryList;

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.energizer.services.order.EnergizerB2BOrderHistoryService#getOrderHistoryForB2BUnit(java.lang.String,
	 * de.hybris.platform.core.enums.OrderStatus[])
	 */
	@Override
	public List<Object> getOrderHistoryForB2BUnit(final String unitId, final List orderStatusList) throws Exception
	{
		List<Object> orderList = null;
		try
		{
			orderList = orderDAO.getOrderHistoryForB2BUnit(unitId, orderStatusList);
		}
		catch (final Exception e)
		{
			LOG.info("Exception occured while getting order list:::: " + e);
			throw e;
		}


		return orderList;
	}

}
