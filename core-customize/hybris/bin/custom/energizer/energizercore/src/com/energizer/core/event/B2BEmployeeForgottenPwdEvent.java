/**
 *
 */
package com.energizer.core.event;

import com.energizer.core.model.EnergizerB2BEmployeeModel;


/**
 * @author Srivenkata_N
 *
 */
public class B2BEmployeeForgottenPwdEvent extends B2BEmployeeAbstractCommerceUserEvent
{
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private String token;
	//private EnergizerB2BEmployeeModel salesRepUser;


	public B2BEmployeeForgottenPwdEvent()
	{
		super();
	}

	public B2BEmployeeForgottenPwdEvent(final String token, final EnergizerB2BEmployeeModel salesRepUser)
	{
		super();
		super.salesRepUser = salesRepUser;
		this.token = token;
	}

	/**
	 * @return the token
	 */
	public String getToken()
	{
		return token;
	}

	/**
	 * @param token
	 *           the token to set
	 */
	public void setToken(final String token)
	{
		this.token = token;
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
