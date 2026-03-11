/**
 *
 */
package com.energizer.services.b2bemployee;

import de.hybris.platform.commerceservices.search.pagedata.PageableData;
import de.hybris.platform.commerceservices.search.pagedata.SearchPageData;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderModel;

import java.util.Date;
import java.util.List;

import com.energizer.core.model.EnergizerB2BEmployeeModel;
import com.energizer.core.model.EnergizerB2BUnitModel;


/**
 * @author kaki.rajasekhar
 *
 */
public interface EnergizerB2BEmployeeService
{
	public List<EnergizerB2BUnitModel> getEnergizerB2BUnitList(String user);

	public List<EnergizerB2BEmployeeModel> getEnergizerB2BEmployeeList();

	public SearchPageData<OrderModel> getFilterOrdersForB2BUnit(final String unitId, final PageableData pageableData,
			final OrderStatus[] orderStatuses, final Date fromDate, final Date toDate, final boolean fetchAllOrders);
}
