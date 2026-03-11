/**
 *
 */
package com.energizer.services.order;

import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.core.model.order.OrderModel;

import java.util.List;

import com.energizer.core.model.EnergizerB2BUnitLeadTimeModel;
import com.energizer.core.model.EnergizerB2BUnitModel;


/**
 * @author M1023097
 *
 */
public interface EnergizerB2BOrderService
{
	/**
	 * @param b2bUnitModel
	 * @param shippingPointId
	 * @param soldToAddressId
	 * @return List<EnergizerB2BUnitLeadTimeModel>
	 */
	List<EnergizerB2BUnitLeadTimeModel> getLeadTimeData(EnergizerB2BUnitModel b2bUnitModel, String shippingPointId,
			String soldToAddressId);

	/**
	 * @param b2bUnitModel
	 * @param shippingPointId
	 * @return List<String>
	 */
	List<String> getsoldToAddressIds(EnergizerB2BUnitModel b2bUnitModel, String shippingPointId);

	/**
	 * @param cartData
	 * @return
	 * @throws Exception
	 */
	CartData simulateOrder(CartData cartData, final String requestSource) throws Exception;

	/**
	 * @param orderModel
	 * @return
	 * @throws Exception
	 */
	int createOrder(OrderModel orderModel) throws Exception;

}
