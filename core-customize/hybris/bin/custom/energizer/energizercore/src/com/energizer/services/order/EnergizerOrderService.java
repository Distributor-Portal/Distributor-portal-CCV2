package com.energizer.services.order;

import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.energizer.core.model.EnergizerB2BUnitModel;
import com.energizer.core.model.EnergizerProductModel;



/**
 * @author ROMIL JAISHWAL M1028411
 */
public interface EnergizerOrderService
{

	/**
	 * This function will return OrderModel object for a given sapOrderNo if it is not exist then it will return null
	 *
	 * @param sapOrderNo
	 * @return OrderModel
	 */
	public OrderModel getExistingOrder(final String sapOrderNo);

	/**
	 * This function will return OrderModel object for a given sapOrderNo and hybrisOrderNo if it is not exist then it will
	 * return null
	 *
	 * @param sapOrderNo
	 * @param hybrisOrderNo
	 * @return OrderModel
	 */
	public OrderModel getExistingOrder(final String sapOrderNo, final String hybrisOrderNo);

	/**
	 * This function will return order entry if entry is not exist then it will return null
	 *
	 * @param energizerOrderModel
	 * @param energizerProductModel
	 * @return OrderEntryModel
	 */
	public OrderEntryModel getExistingOrderItem(final OrderModel energizerOrderModel,
			final EnergizerProductModel energizerProductModel);

	/**
	 * This function will return OrderStatus object for a given energizer order status code if code is valid otherwise it
	 * will return null.
	 *
	 * @param energizerOrderStatusCode
	 * @return OrderStatus
	 */
	public OrderStatus getEnergizerOrderStatus(final String energizerOrderStatusCode);


	/**
	 * This function will return all associated admin ids for energizerB2BUnitModel if it is having members of admin group
	 * or it will return empty list if energizerB2BUnitModel id is not having admin members of admin group
	 *
	 * @param energizerB2BUnitModel
	 * @return String
	 */
	public List<String> getAdminIdsOfEnergizerB2BUnitModel(final EnergizerB2BUnitModel energizerB2BUnitModel);



	/**
	 * This function will return all associated admin email ids for energizerB2BUnitModel if it is having members of admin
	 * group or it will return empty list if energizerB2BUnitModel id is not having admin members of admin group
	 *
	 * @param energizerB2BUnitModel
	 * @return String
	 */
	public List<String> getAdminEmailIdsOfEnergizerB2BUnitModel(final EnergizerB2BUnitModel energizerB2BUnitModel);

	/**
	 * This function will return EnergizerProductModel object for a given erpMaterialID if erpMaterialId is exist for Online
	 * catelog if productid Is not exist then it returns null
	 *
	 * @param productMaterialId
	 * @return EnergizerProductModel
	 */
	public EnergizerProductModel getEnergizerProduct(final String productMaterialId);


	/**
	 * This function will add adjustedLinePrice of all order entries for a given order and return it. If no order entry is
	 * having adjustedLinePrice then it will return null
	 *
	 * @param energizerOrderModel
	 * @return BigDecimal
	 */
	public BigDecimal getAdjustedTotalPriceForOrder(final OrderModel energizerOrderModel);

	/**
	 * @param sapOrderNo
	 * @return
	 */
	OrderModel getHybrisOrderNoForOfflineOrder(String sapOrderNo);

	public List<Object> getAllStuckOrders(List<String> status, List<Date> dateList, String site, String region);
}
