/**
 *
 */
package com.energizer.core.event;

import com.energizer.core.model.EnergizerB2BEmployeeModel;


/**
 * @author Srivenkata_N
 *
 */
public class RegisterEmployeeEvent extends B2BEmployeeAbstractCommerceUserEvent
{

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public RegisterEmployeeEvent()
	{
		super();
	}

	public RegisterEmployeeEvent(final EnergizerB2BEmployeeModel salesRepUser)
	{
		super();
		super.salesRepUser = salesRepUser;
	}

	/**
	 * @return the salesRepUser
	 */
	@Override
	public EnergizerB2BEmployeeModel getSalesRepUser()
	{
		return salesRepUser;
	}

	/**
	 * @param salesRepUser
	 *           the salesRepUser to set
	 */
	@Override
	public void setSalesRepUser(final EnergizerB2BEmployeeModel salesRepUser)
	{
		this.salesRepUser = salesRepUser;
	}

}
