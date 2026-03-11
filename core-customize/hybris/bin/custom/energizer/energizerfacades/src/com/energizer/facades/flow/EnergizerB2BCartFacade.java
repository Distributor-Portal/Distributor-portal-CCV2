/**
 *
 */
package com.energizer.facades.flow;

import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.order.data.CartModificationData;
import de.hybris.platform.commercefacades.order.data.OrderEntryData;
import de.hybris.platform.commercefacades.product.data.PriceData;

import com.energizer.core.model.EnergizerB2BUnitModel;
import com.energizer.core.model.EnergizerProductModel;


/**
 * @author KA289322
 *
 */
public interface EnergizerB2BCartFacade
{
	public CartModificationData updateOrderEntry(final OrderEntryData orderEntry);

	public CartModificationData updateOrderEntryForEachUnitPrice(final OrderEntryData orderEntry);

	public CartModificationData updateOrderEntryForAgreeEdgewellPrice(final OrderEntryData orderEntry);

	public void updateAgreeEdgewellPriceForAllProducts(final CartData cartData);

	public PriceData getEachUnitPrice(EnergizerProductModel energizerProductModel, EnergizerB2BUnitModel energizerB2BUnit)
			throws Exception;

	public OrderEntryData getOrderEntryDataForEachUnitPrice(final OrderEntryData orderEntry) throws Exception;
}
