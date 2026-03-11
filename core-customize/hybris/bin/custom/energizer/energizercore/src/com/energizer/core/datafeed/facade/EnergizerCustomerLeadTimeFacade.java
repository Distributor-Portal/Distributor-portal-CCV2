/**
 * 
 */
package com.energizer.core.datafeed.facade;

import java.util.List;

import com.energizer.core.model.EnergizerB2BUnitLeadTimeModel;


/**
 * @author M1023278
 * 
 */
public interface EnergizerCustomerLeadTimeFacade
{
	public List<EnergizerB2BUnitLeadTimeModel> fetchEnergizerB2BUnitLeadTime(final String pk_EnergizerB2BUnit,
			final String shippingPointNo, final String shipTo);
}
