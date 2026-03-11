/**
 * 
 */
package com.energizer.core.datafeed.facade.impl;

import java.util.List;

import javax.annotation.Resource;

import com.energizer.core.datafeed.facade.EnergizerCustomerLeadTimeFacade;
import com.energizer.core.datafeed.service.impl.DefaultEnergizerCustomerLeadTimeService;
import com.energizer.core.model.EnergizerB2BUnitLeadTimeModel;


/**
 * @author M1023278
 * 
 */
public class DefaultEnergizerCustomerLeadTimeFacade implements EnergizerCustomerLeadTimeFacade
{


	@Resource
	private DefaultEnergizerCustomerLeadTimeService defaultEnergizerCustomerLeadTimeService;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.energizer.core.datafeed.service.EnergizerCustomerLeadTimeService#fecthEnergizerB2BUnitLeadTime(java.lang.String
	 * , java.lang.String, java.lang.String)
	 */
	@Override
	public List<EnergizerB2BUnitLeadTimeModel> fetchEnergizerB2BUnitLeadTime(final String pk_EnergizerB2BUnit,
			final String shippingPointNo, final String shipTo)
	{
		final List<EnergizerB2BUnitLeadTimeModel> energizerB2BUnitLeadTimes = defaultEnergizerCustomerLeadTimeService
				.fecthEnergizerB2BUnitLeadTime(pk_EnergizerB2BUnit, shippingPointNo, shipTo);
		return energizerB2BUnitLeadTimes;
	}

}
