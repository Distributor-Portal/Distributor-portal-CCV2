/**
 * 
 */
package com.energizer.services.order.dao;

import java.util.List;

import com.energizer.core.model.EnergizerB2BUnitLeadTimeModel;
import com.energizer.core.model.EnergizerB2BUnitModel;


/**
 * @author M1023097
 * 
 */
public interface EnergizerB2BOrderDAO
{
	List<EnergizerB2BUnitLeadTimeModel> getLeadTimeData(EnergizerB2BUnitModel b2bUnitModel, String shippingPointId,
			String soldToAddressId);

	List<String> getsoldToAddressIds(EnergizerB2BUnitModel b2bUnitModel, String shippingPointId);
}
