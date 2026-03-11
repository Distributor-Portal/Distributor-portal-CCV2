/**
 *
 */
package com.energizer.services.b2bemployee.impl;

import de.hybris.platform.commerceservices.search.pagedata.PageableData;
import de.hybris.platform.commerceservices.search.pagedata.SearchPageData;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderModel;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;

import com.energizer.core.model.EnergizerB2BCustomerModel;
import com.energizer.core.model.EnergizerB2BEmployeeModel;
import com.energizer.core.model.EnergizerB2BUnitModel;
import com.energizer.services.b2bemployee.EnergizerB2BEmployeeService;
import com.energizer.services.b2bemployee.dao.DefaultEnergizerB2BEmployeeDao;


/**
 * @author kaki.rajasekhar
 *
 */
public class DefaultEnergizerB2BEmployeeService implements EnergizerB2BEmployeeService
{
	protected static final Logger LOG = Logger.getLogger(DefaultEnergizerB2BEmployeeService.class);

	@Resource(name = "defaultEnergizerB2BEmployeeDao")
	private DefaultEnergizerB2BEmployeeDao defaultEnergizerB2BEmployeeDao;

	@Override
	public List<EnergizerB2BUnitModel> getEnergizerB2BUnitList(final String userID)
	{
		return defaultEnergizerB2BEmployeeDao.getEnergizerB2BUnitList(userID);
	}

	//	WeSell Implementation -  Added Code Changes for Forgot Password/Unlock Account for Sales Rep login - by Venkat
	public EnergizerB2BEmployeeModel getB2BEmployeeByToken(final String token)
	{
		return defaultEnergizerB2BEmployeeDao.getB2BEmployeeByToken(token);
	}


	public EnergizerB2BCustomerModel getB2BCustomerByToken(final String token)
	{
		return defaultEnergizerB2BEmployeeDao.getB2BCustomerByToken(token);
	}

	public List<EnergizerB2BEmployeeModel> getEnergizerB2BEmployeeList()
	{
		return defaultEnergizerB2BEmployeeDao.getEnergizerB2BEmployeeList();
	}
	//	WeSell Implementation -  Added Code Changes for Forgot Password/Unlock Account for Sales Rep login - by Venkat

	/*
	 * (non-Javadoc)
	 *
	 * @see com.energizer.services.b2bemployee.EnergizerB2BEmployeeService#getSpecificOrdersForB2BUnit(java.lang.String,
	 * de.hybris.platform.commerceservices.search.pagedata.PageableData, de.hybris.platform.core.enums.OrderStatus[],
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public SearchPageData<OrderModel> getFilterOrdersForB2BUnit(final String unitId, final PageableData pageableData,
			final OrderStatus[] orderStatuses, final Date fromDate, final Date toDate, final boolean fetchAllOrders)
	{
		SearchPageData<OrderModel> ordersHistoryList = null;
		try
		{
			ordersHistoryList = defaultEnergizerB2BEmployeeDao.getFilterOrdersForB2BUnit(unitId, pageableData, orderStatuses,
					fromDate, toDate, fetchAllOrders);

		}
		catch (final NullPointerException ne)
		{
			LOG.info("Null Pointer Exception Occured while filtering the Orders :::" + ne);
			throw ne;
		}
		catch (final Exception e)
		{
			LOG.info("Exception Occured while filtering the Orders :::" + e);
			throw e;
		}
		return ordersHistoryList;
	}
}
