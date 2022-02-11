/**
 *
 */
package com.energizer.facades.search.populators;

import de.hybris.platform.commercefacades.order.converters.populator.OrderHistoryPopulator;
import de.hybris.platform.commercefacades.order.data.OrderData;
import de.hybris.platform.commercefacades.order.data.OrderHistoryData;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.servicelayer.dto.converter.Converter;


/**
 * @author M1023097
 *
 */
public class EnergizerOrderHistoryPopulator extends OrderHistoryPopulator
{
	private Converter<OrderModel, OrderData> orderConverter;


	@Override
	public void populate(final OrderModel source, final OrderHistoryData target)
	{
		super.populate(source, target);
		target.setB2bOrderData(getOrderConverter().convert(source));
		target.setReferenceNumber(source.getErpOrderNumber());
		target.setPurchaseOrderNumber(source.getPurchaseOrderNumber());
		target.setErpOrderCreator(source.getErpOrderCreator());
		if (null != source.getSalesRepName())
		{
			target.setSalesRepName(source.getSalesRepName());
		}
	}

	/**
	 * @return the orderConverter
	 */
	public Converter<OrderModel, OrderData> getOrderConverter()
	{
		return orderConverter;
	}

	/**
	 * @param orderConverter
	 *           the orderConverter to set
	 */
	public void setOrderConverter(final Converter<OrderModel, OrderData> orderConverter)
	{
		this.orderConverter = orderConverter;
	}
}
