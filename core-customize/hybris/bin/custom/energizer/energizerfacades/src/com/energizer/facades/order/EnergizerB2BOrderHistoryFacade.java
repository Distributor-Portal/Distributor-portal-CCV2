/**
 *
 */
package com.energizer.facades.order;

import de.hybris.platform.b2b.model.B2BUnitModel;
import de.hybris.platform.commercefacades.order.data.OrderHistoryData;
import de.hybris.platform.commerceservices.search.pagedata.PageableData;
import de.hybris.platform.commerceservices.search.pagedata.SearchPageData;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.servicelayer.exceptions.ModelNotFoundException;

import java.util.Date;
import java.util.List;

import com.energizer.core.model.EnergizerB2BUnitModel;
import com.energizer.core.model.EnergizerCMIRModel;


/**
 * @author M1028886
 *
 */
public interface EnergizerB2BOrderHistoryFacade
{
	public SearchPageData<OrderHistoryData> getOrdersForB2BUnit(B2BUnitModel unitId, PageableData pageableData,
			OrderStatus[] orderStatuses) throws Exception;

	public EnergizerCMIRModel getEnergizerCMIR(String erpMaterialId, String b2bUnitId) throws ModelNotFoundException, Exception;

	public String getProductCodeForCustomer();

	public EnergizerB2BUnitModel getParentUnitForCustomer(String userId);

	public String getCurrentUser();

	public List<Object> getOrderHistoryForB2BUnit(String unitId, List orderStatusList) throws Exception;

	public SearchPageData<OrderHistoryData> getFilterOrdersForB2BUnit(final String unitId, final PageableData pageableData,
			final OrderStatus[] orderStatuses, final Date fromDate, final Date toDate, final boolean fetchAllOrders);
}
