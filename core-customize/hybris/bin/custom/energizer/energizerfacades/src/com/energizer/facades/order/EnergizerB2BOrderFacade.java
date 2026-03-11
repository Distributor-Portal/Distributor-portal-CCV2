/**
 *
 */
package com.energizer.facades.order;

import de.hybris.platform.commercefacades.order.data.OrderData;


/**
 * @author atmakup
 *
 */
public interface EnergizerB2BOrderFacade
{
	public OrderData getOrderDetailsForCode(String code);

}
