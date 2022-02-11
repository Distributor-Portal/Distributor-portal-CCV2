/**
 *
 */
package com.energizer.core.wesell.virtualid.creation;

import com.energizer.core.model.EnergizerB2BCustomerModel;
import com.energizer.core.model.EnergizerB2BEmployeeModel;
import com.energizer.core.model.EnergizerB2BUnitModel;


/**
 * @author kaki.rajasekhar
 *
 */
public interface EnergizerWesellVirtualID
{

	EnergizerB2BCustomerModel createVirtualID(final EnergizerB2BUnitModel b2bUnit,
			final EnergizerB2BEmployeeModel loginB2BEmployee);
}
