/**
 *
 */
package com.energizer.core.datafeed.facade;

import de.hybris.platform.cms2.model.site.CMSSiteModel;

import java.util.List;

import com.energizer.core.model.EnergizerB2BCustomerModel;


/**
 * @author M1030110
 *
 */
public interface EnergizerPasswordGenerateFacade
{
	public List<CMSSiteModel> getCMSSiteByName(String siteName);

	public List<EnergizerB2BCustomerModel> getEnergizerCustomers();

	public EnergizerB2BCustomerModel getCustomerByUID(String UID);

	public EnergizerB2BCustomerModel getCustomerBySecureToken(final String token);
}
