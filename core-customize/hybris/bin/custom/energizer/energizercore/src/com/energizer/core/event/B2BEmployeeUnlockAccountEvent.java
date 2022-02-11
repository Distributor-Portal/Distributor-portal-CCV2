/**
 *
 */
package com.energizer.core.event;

/**
 * @author Srivenkata_N
 *
 */
public class B2BEmployeeUnlockAccountEvent extends B2BEmployeeAbstractCommerceUserEvent
{

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private String token;
	//private EnergizerB2BEmployeeModel salesRepUser;


	public B2BEmployeeUnlockAccountEvent()
	{
		super();
	}

	public B2BEmployeeUnlockAccountEvent(final String token)
	{
		super();

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




}
