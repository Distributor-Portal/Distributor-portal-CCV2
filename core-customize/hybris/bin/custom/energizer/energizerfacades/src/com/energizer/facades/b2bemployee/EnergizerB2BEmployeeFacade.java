/**
 *
 */
package com.energizer.facades.b2bemployee;

import java.util.List;

import com.energizer.core.model.EnergizerB2BEmployeeModel;
import com.energizer.core.model.EnergizerB2BUnitModel;

/**
 * @author kaki.rajasekhar
 *
 */
public interface EnergizerB2BEmployeeFacade
{

	public List<EnergizerB2BUnitModel> getEnergizerB2BUnitList(String userID);

	/**
	 * @return
	 */
	public List<EnergizerB2BEmployeeModel> getEnergizerB2BEmployeeList();
}
