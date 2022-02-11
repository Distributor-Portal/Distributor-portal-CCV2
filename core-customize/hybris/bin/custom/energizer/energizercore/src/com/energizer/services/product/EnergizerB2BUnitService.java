/**
 * 
 */
package com.energizer.services.product;

import java.util.List;

import com.energizer.core.model.EnergizerB2BUnitModel;


/**
 * @author Geetika Singh
 * 
 */
public interface EnergizerB2BUnitService
{
	List<EnergizerB2BUnitModel> getB2BUnitForSalesArea(final String salesOrganisation, final String distributionChannel,
			final String division);
}
