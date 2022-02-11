/**
 *
 */
package com.energizer.facades.quickorder;

import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.order.data.OrderEntryData;

import com.energizer.core.model.EnergizerCMIRModel;
import com.energizer.quickorder.QuickOrderData;


/**
 * @author kaushik.ganguly
 *
 */
public interface EnergizerQuickOrderFacade
{
	public static final String QUICK_ORDER_SESSION_ATTRIBUTE = "quickorder";

	public QuickOrderData getQuickOrderFromSession(QuickOrderData quickOrder);

	public void addItemToQuickOrder(QuickOrderData quickOrder, String productCode, String customerProductCode);

	public void removeItemFromQuickOrder(QuickOrderData quickOrder, String productCode);

	public OrderEntryData getOrderEntryDataFromProductCode(String productCode, String b2bunit);

	public boolean productExistsInList(final String productCode, final QuickOrderData quickOrder);

	public EnergizerCMIRModel getCMIRForProductCodeOrCustomerMaterialID(final String productCode, final String customerMaterialId)
			throws Exception;

	public OrderEntryData getProductData(final String productCode, final String customerProductCode, final EnergizerCMIRModel cmir);

	public void updateQtyToExistingProduct(QuickOrderData quickOrder, String productCode, Long qty);

	public CartData getCurrentSessionCart();

	public void getOrderEntryShippingPoints(final OrderEntryData orderEntryData, QuickOrderData quickOrder);
}
