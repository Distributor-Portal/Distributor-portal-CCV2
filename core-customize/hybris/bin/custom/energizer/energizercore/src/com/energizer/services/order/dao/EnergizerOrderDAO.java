package com.energizer.services.order.dao;

import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;

import java.util.Date;
import java.util.List;

import com.energizer.core.model.EnergizerProductModel;


/**
 * @author ROMIL JAISHWAL M1028411
 */
public interface EnergizerOrderDAO
{
	/**
	 * This function will return OrderModel object for a given sapOrderNo if it is not exist then it will return null
	 *
	 * @param sapOrderNo
	 * @return OrderModel
	 */
	public OrderModel findExistingOrder(final String sapOrderNo);

	/**
	 * This function will return OrderModel object for a given sapOrderNo and hybrisOrderNo if it is not exist then it will
	 * return null
	 *
	 * @param sapOrderNo
	 * @param hybrisOrderNo
	 * @return OrderModel
	 */
	public OrderModel findExistingOrder(final String sapOrderNo, final String hybrisOrderNo);

	/**
	 * This function will return order entry if entry is not exist then it will return null
	 *
	 * @param energizerOrderModel
	 * @param energizerProductModel
	 * @return OrderEntryModel
	 */
	public OrderEntryModel findExistingOrderItem(final OrderModel energizerOrderModel,
			final EnergizerProductModel energizerProductModel);

	/**
	 * This function will return all order entry if no entry is exist for a given order then it will return null
	 *
	 * @param energizerOrderModel
	 * @return List<OrderEntryModel>
	 */
	public List<OrderEntryModel> findExistingOrderItems(final OrderModel energizerOrderModel);

	/**
	 * @param sapOrderNo
	 * @return
	 */
	public OrderModel findHybrisOrderNoForOfflineOrder(String sapOrderNo);

	public List<Object> getAllStuckOrders(List<String> status, List<Date> dateList, String site, String region);

	public List<OrderModel> getReprocessedOrders();


}
