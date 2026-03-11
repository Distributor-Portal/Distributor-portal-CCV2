/**
 *
 */
package com.energizer.services.order;

import de.hybris.platform.b2b.model.B2BUnitModel;
import de.hybris.platform.commerceservices.search.pagedata.PageableData;
import de.hybris.platform.commerceservices.search.pagedata.SearchPageData;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderModel;

import java.util.List;



/**
 * @author M1028886
 *
 */
public interface EnergizerB2BOrderHistoryService
{

	public SearchPageData<OrderModel> getOrdersForB2BUnit(B2BUnitModel unitId, PageableData pageableData,
			OrderStatus[] orderStatuses) throws Exception;

	public List<Object> getOrderHistoryForB2BUnit(String unitId, List orderStatusList) throws Exception;

}
