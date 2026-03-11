/**
 *
 */
package com.energizer.services.b2bemployee.dao;

import de.hybris.platform.commerceservices.search.pagedata.PageableData;
import de.hybris.platform.commerceservices.search.pagedata.SearchPageData;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderModel;

import java.util.Date;
import java.util.List;

import com.energizer.core.model.EnergizerB2BCustomerModel;
import com.energizer.core.model.EnergizerB2BEmployeeModel;
import com.energizer.core.model.EnergizerB2BUnitModel;


/**
 * @author kaki.rajasekhar
 *
 */
public interface EnergizerB2BEmployeeDao
{
	public List<EnergizerB2BUnitModel> getEnergizerB2BUnitList(String userID);

	//	WeSell Implementation -  Added Code Changes for Forgot Password/Unlock Account for Sales Rep login - by Venkat
	public EnergizerB2BEmployeeModel getB2BEmployeeByToken(final String token);

	/**
	 * @param token
	 * @return
	 */
	public EnergizerB2BCustomerModel getB2BCustomerByToken(String token);
	//	WeSell Implementation -  Added Code Changes for Forgot Password/Unlock Account for Sales Rep login - by Venkat

	/**
	 * @return
	 */
	public List<EnergizerB2BEmployeeModel> getEnergizerB2BEmployeeList();

	public SearchPageData<OrderModel> getFilterOrdersForB2BUnit(final String unitId, final PageableData pageableData,
			final OrderStatus[] orderStatuses, final Date fromDate, final Date toDate, final boolean fetchAllOrders);


}
