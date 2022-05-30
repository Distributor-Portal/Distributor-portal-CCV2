/**
 *
 */
package com.energizer.facades.order.impl;

import de.hybris.platform.b2b.services.B2BOrderService;
import de.hybris.platform.commercefacades.order.data.OrderData;
import de.hybris.platform.commercefacades.order.impl.DefaultOrderFacade;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;

import com.energizer.facades.order.EnergizerB2BOrderFacade;


/**
 * @author atmakup
 *
 */
public class DefaultEnergizerB2BOrderFacade extends DefaultOrderFacade implements EnergizerB2BOrderFacade
{
	private B2BOrderService b2bOrderService;

	/**
	 * @return the b2bOrderService
	 */
	protected B2BOrderService getB2bOrderService()
	{
		return b2bOrderService;
	}

	/**
	 * @param b2bOrderService
	 *           the b2bOrderService to set
	 */

	public void setB2bOrderService(final B2BOrderService b2bOrderService)
	{
		this.b2bOrderService = b2bOrderService;
	}

	@Override
	public OrderData getOrderDetailsForCode(final String code)
	{
		// YTODO Auto-generated method stub

		System.out.println("Enter in getOrderDetailsForCode method ");
		System.out.println("code-->" + code);

		final OrderModel orderModel = b2bOrderService.getOrderForCode(code);
		if (orderModel == null)
		{
			throw new UnknownIdentifierException("Order with code " + code + " not found for current user in current BaseStore");
		}
		return getOrderConverter().convert(orderModel);

	}

}
