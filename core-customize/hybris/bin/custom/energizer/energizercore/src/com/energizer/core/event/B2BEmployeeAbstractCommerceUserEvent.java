/**
 *
 */

package com.energizer.core.event;

import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.commerceservices.event.AbstractCommerceUserEvent;

import com.energizer.core.model.EnergizerB2BEmployeeModel;


/**
 * @author Srivenkata_N
 *
 */
public abstract class B2BEmployeeAbstractCommerceUserEvent extends AbstractCommerceUserEvent<BaseSiteModel>
{
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public EnergizerB2BEmployeeModel salesRepUser;

	/**
	 * @return the salesRepUser
	 */
	public EnergizerB2BEmployeeModel getSalesRepUser()
	{
		return salesRepUser;
	}

	/**
	 * @param salesRepUser
	 *           the salesRepUser to set
	 */
	public void setSalesRepUser(final EnergizerB2BEmployeeModel salesRepUser)
	{
		this.salesRepUser = salesRepUser;
	}
}
