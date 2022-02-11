/**
 * 
 */
package com.energizer.core.datafeed.facade;

import de.hybris.platform.cms2.model.site.CMSSiteModel;

import java.util.List;

import com.energizer.core.model.EnergizerB2BCustomerModel;


/**
 * @author M1023278
 * 
 */
public interface EnergizerPasswordExpiryFacade
{
	public List<CMSSiteModel> getCMSSiteByName(String siteName);

	public List<EnergizerB2BCustomerModel> getEnergizerCustomers();

	public EnergizerB2BCustomerModel getCustomerByUID(String UID);
}
