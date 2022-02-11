/**
 *
 */
package com.energizer.services.order;

import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.order.data.OrderEntryData;
import de.hybris.platform.commerceservices.order.CommerceCartModification;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;


/**
 * @author Bivash Pandit
 *
 */
public interface EnergizerCartService
{

	public CartData calCartContainerUtilization(CartData cartData, String containerHeight, String packingOption,
			boolean enableButton) throws Exception;

	public CartData calCartTruckUtilization(CartData cartData, String containerHeight, String packingOption, boolean enableButton,
			String palletType) throws Exception;

	public List<String> getMessages();

	public HashMap getProductNotAddedToCart();

	public HashMap getProductsNotDoublestacked();

	public LinkedHashMap getFloorSpaceProductsMap();

	public LinkedHashMap getNonPalletFloorSpaceProductsMap();

	public CommerceCartModification updateExpectedUnitPriceForCartEntry(final OrderEntryData orderEntry);

	public CommerceCartModification updateEachUnitPriceForCartEntry(final OrderEntryData orderEntry);

	public CommerceCartModification updateAgreeEdgewellUnitPriceForCartEntry(final OrderEntryData orderEntry);

	public void updateAgreeEdgewellPriceForAllProducts(final CartData cartData);

	public void updateCartCurrency();
}
