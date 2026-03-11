/**
 *
 */
package com.energizer.facades.accounts;

import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.commercefacades.user.data.CustomerData;
import de.hybris.platform.commerceservices.customer.DuplicateUidException;
import de.hybris.platform.commerceservices.customer.TokenInvalidatedException;
import de.hybris.platform.commerceservices.search.pagedata.SearchPageData;
import de.hybris.platform.servicelayer.exceptions.ModelNotFoundException;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;

import java.util.List;

import com.energizer.core.model.EnergizerB2BCustomerModel;
import com.energizer.core.model.EnergizerB2BUnitModel;


/**
 * @author m9005673
 *
 */
public interface EnergizerCompanyB2BCommerceFacade
{
	/**
	 * Get the Contact Number of profile user
	 *
	 * @param uuid
	 */
	public String getContactNumber(String uuid, CustomerData customerData);

	/**
	 * validates the maximum user count for b2b unit
	 *
	 * @param energizerModel
	 *           YTODO
	 */
	public boolean validateUserCount(EnergizerB2BUnitModel energizerModel);

	/**
	 * This method retrieves the EnergizerB2BUnitModel of the currently logged in user.
	 *
	 * @see EnergizerB2BUnitModel
	 * @return EnergizerB2BUnitModel
	 * @throws Exception
	 * @throws ModelNotFoundException
	 */
	public EnergizerB2BUnitModel getEnergizerB2BUnitModelForLoggedInUser() throws ModelNotFoundException, Exception;

	/**
	 * This method is to update the user profile.
	 *
	 * @see CustomerData
	 * @return void
	 */

	public void updateProfile(CustomerData customerData) throws DuplicateUidException;

	/**
	 * This method is to retrieves the all energizer B2B Unit administor user.
	 *
	 * @return EnergizerB2BCustomerModel
	 *
	 * @see CustomerData
	 */

	public void updateCustomer(final CustomerData customerData) throws DuplicateUidException;


	/**
	 *
	 * This method upadtes the password by validating with the 5 previous passwords
	 *
	 * @param customerModel
	 * @param newPassword
	 * @param token
	 * @throws TokenInvalidatedException
	 */

	public boolean updatingPassword(String newPassword, String token) throws TokenInvalidatedException;


	/**
	 *
	 * This method validates the previous passwords matches with the new password
	 *
	 * @param customerModel
	 * @param newPassword
	 * @return
	 */
	public boolean checkPreviousPasswordMatch(final EnergizerB2BCustomerModel customerModel, final String newPassword);


	/**
	 * This method changes the password by validating with the 5 previous passwords
	 *
	 * @param currentPassword
	 * @param newPassword
	 */

	public boolean changingPassword(String currentPassword, String newPassword);

	/**
	 *
	 *
	 * @param currentPassword
	 * @return
	 */
	public boolean validateCurrentPassword(final String currentPassword);

	/**
	 *
	 *
	 * @param email
	 * @return
	 */
	public EnergizerB2BCustomerModel getExistingUserForUID(final String email) throws UnknownIdentifierException;

	/**
	 * This method is to retrieves the all energizer B2B Unit administor user.
	 *
	 * @return EnergizerB2BCustomerModel
	 *
	 * @see CustomerData
	 */

	public B2BCustomerModel findB2BAdministratorForCustomer(final EnergizerB2BUnitModel b2bModel);

	/**
	 * @return
	 */
	public List getUserGroups();

	/**
	 * @param user
	 * @param customerData
	 */
	public void populateRolesByCustomer(String user, CustomerData customerData);

	/**
	 * @param b2bCustomer
	 */
	void populateUserRoles(SearchPageData<CustomerData> b2bCustomer);

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
