/**
 *
 */
package com.energizer.facades.b2bemployee.impl;

import java.util.List;

import javax.annotation.Resource;

import com.energizer.core.model.EnergizerB2BEmployeeModel;
import com.energizer.core.model.EnergizerB2BUnitModel;
import com.energizer.facades.b2bemployee.EnergizerB2BEmployeeFacade;
import com.energizer.services.b2bemployee.EnergizerB2BEmployeeService;


/**
 * @author kaki.rajasekhar
 *
 */
public class DefaultEnergizerB2BEmployeeFacade implements EnergizerB2BEmployeeFacade
{

	@Resource(name = "defaultEnergizerB2BEmployeeService")
	private EnergizerB2BEmployeeService defaultEnergizerB2BEmployeeService;

	@Override
	public List<EnergizerB2BUnitModel> getEnergizerB2BUnitList(final String userID)
	{
		return defaultEnergizerB2BEmployeeService.getEnergizerB2BUnitList(userID);
	}

	@Override
	public List<EnergizerB2BEmployeeModel> getEnergizerB2BEmployeeList()
	{
		return defaultEnergizerB2BEmployeeService.getEnergizerB2BEmployeeList();
	}
}
