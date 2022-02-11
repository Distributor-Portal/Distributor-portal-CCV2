/**
 *
 */
package com.energizer.facades.accounts;

import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;


/**
 * @author m9005673
 *
 */
public interface EnergizerCompanyB2BCommerceService
{

	/**
	 * @param uid
	 */
	public void sendUnlockAccountEmail(final String uid);

	/**
	 * @param email
	 * @throws UnknownIdentifierException
	 */
	public boolean unlockCustomerAccount(final String email) throws UnknownIdentifierException;


}
