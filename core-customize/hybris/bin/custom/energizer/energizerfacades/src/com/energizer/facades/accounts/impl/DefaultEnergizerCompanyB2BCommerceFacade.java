/**
 *
 */
package com.energizer.facades.accounts.impl;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;

import de.hybris.platform.b2b.company.B2BCommerceUserService;
import de.hybris.platform.b2b.constants.B2BConstants;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.b2b.model.B2BUnitModel;
import de.hybris.platform.b2b.model.B2BUserGroupModel;
import de.hybris.platform.b2b.services.B2BUnitService;
import de.hybris.platform.commercefacades.customer.impl.DefaultCustomerFacade;
import de.hybris.platform.commercefacades.user.data.CustomerData;
import de.hybris.platform.commercefacades.user.exceptions.PasswordMismatchException;
import de.hybris.platform.commerceservices.customer.DuplicateUidException;
import de.hybris.platform.commerceservices.customer.TokenInvalidatedException;
import de.hybris.platform.commerceservices.customer.impl.DefaultCustomerAccountService;
import de.hybris.platform.commerceservices.search.pagedata.SearchPageData;
import de.hybris.platform.commerceservices.security.SecureToken;
import de.hybris.platform.commerceservices.security.SecureTokenService;
import de.hybris.platform.core.model.security.PrincipalGroupModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.jalo.user.User;
import de.hybris.platform.jalo.user.UserManager;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.exceptions.ModelNotFoundException;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.user.UserService;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.PredicateUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.Assert;

import com.energizer.core.model.EnergizerB2BCustomerModel;
import com.energizer.core.model.EnergizerB2BEmployeeModel;
import com.energizer.core.model.EnergizerB2BUnitModel;
import com.energizer.facades.accounts.EnergizerCompanyB2BCommerceFacade;
import com.energizer.facades.accounts.populators.ContactNumberPopulator;
import com.energizer.facades.accounts.populators.EnergizerB2BCustomerReversePopulator;
import com.energizer.facades.employee.data.EnergizerB2BEmployeeData;
import com.energizer.services.b2bemployee.impl.DefaultB2BEmployeeAccountService;


/**
 * @author m9005673
 *
 */
public class DefaultEnergizerCompanyB2BCommerceFacade extends DefaultCustomerFacade implements EnergizerCompanyB2BCommerceFacade
{
	private static final Logger LOG = Logger.getLogger(DefaultEnergizerCompanyB2BCommerceFacade.class);

	@Resource(name = "userService")
	private UserService userService;

	@Resource(name = "b2bCommerceUserService")
	private B2BCommerceUserService b2bUserService;

	@Resource(name = "customerAccountService")
	private DefaultCustomerAccountService customerAccountService;

	@SuppressWarnings("rawtypes")
	@Resource(name = "defaultB2BUnitService")
	private B2BUnitService defaultB2BUnitService;

	@Resource(name = "energizerCustomerReversePopulator")
	private EnergizerB2BCustomerReversePopulator energizerCustomerReversePopulator;


	@Resource(name = "contactNumberPopulator")
	private ContactNumberPopulator contactNumberPopulator;

	@Resource(name = "energizerGroupsLookUpStrategy")
	private DefaultEnergizerGroupsLookUpStrategy energizerGroupsLookUpStrategy;

	@Resource(name = "defaultB2BEmployeeAccountService")
	private DefaultB2BEmployeeAccountService defaultB2BEmployeeAccountService;

	@Resource(name = "configurationService")
	private ConfigurationService configurationService;

	@Resource
	private SecureTokenService secureTokenService;

	@Resource
	private ModelService modelService;

	@Value("${previousPasswordCount}")
	int prevPasswordCount;

	@Value("${passwordDelimiter}")
	String passwordDelimiter;

	@Resource(name = "defaultEnergizerCompanyB2BCommerceService")
	private DefaultEnergizerCompanyB2BCommerceService defaultEnergizerCompanyB2BCommerceService;



	/*
	 * (non-Javadoc)
	 *
	 * @see de.hybris.platform.b2bacceleratorfacades.company.CompanyB2BCommerceFacade#getContactNumber(java.lang.String)
	 */
	@Override
	public String getContactNumber(final String uuid, final CustomerData customerData)
	{
		final EnergizerB2BCustomerModel model = userService.getUserForUID(uuid, EnergizerB2BCustomerModel.class);
		contactNumberPopulator.populate(model, customerData);
		return customerData.getContactNumber();
	}

	/**
	 * This method retrieves the EnergizerB2BUnitModel of the currently logged in user.
	 *
	 * @see EnergizerB2BUnitModel
	 * @return EnergizerB2BUnitModel
	 */
	private EnergizerB2BUnitModel getB2BUnitForLoggedInUser()
	{
		EnergizerB2BUnitModel energizerB2BUnitModel = null;
		try
		{
		energizerB2BUnitModel = b2bUserService.getParentUnitForCustomer(userService.getCurrentUser().getUid());
		}
		catch (final ModelNotFoundException me)
		{
			LOG.info("No B2B unit model found for the logged In User :::" + me);
			throw me;
		}
		catch (final Exception e)
		{
			LOG.info("Exception occured while fetching B2B unit model for Logged In User ::: " + e);
			throw e;
		}
		return energizerB2BUnitModel;
	}


	/*
	 * (non-Javadoc)
	 *
	 * @see de.hybris.platform.b2bacceleratorfacades.company.CompanyB2BCommerceFacade#validateUserCount()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean validateUserCount(final EnergizerB2BUnitModel energizerModel)
	{
		boolean validateFlag = false;
		if (null != energizerModel.getMaxUserLimit())
		{
			final int b2bUnitLimit = NumberUtils.toInt(energizerModel.getMaxUserLimit());
			LOG.info("B2B Unit Count is " + b2bUnitLimit);
			final int totalCustomers = defaultB2BUnitService.getB2BCustomers(energizerModel).size();
			LOG.info("B2B Unit Size is " + totalCustomers);
			final int totalCustomer = defaultB2BUnitService.getB2BCustomers(energizerModel).size() - 1;
			LOG.info("B2B Unit size after is " + totalCustomer);
			validateFlag = (totalCustomers >= b2bUnitLimit) ? true : false;
		}
		return validateFlag;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.energizer.facades.accounts.EnergizerCompanyB2BCommerceFacade#getEnergizerB2BUnitModelForLoggedInUser()
	 */
	@Override
	public EnergizerB2BUnitModel getEnergizerB2BUnitModelForLoggedInUser() throws ModelNotFoundException, Exception
	{
		EnergizerB2BUnitModel energizerB2BUnitModel = null;
		try
		{
		energizerB2BUnitModel = getB2BUnitForLoggedInUser();
		}
		catch (final ModelNotFoundException me)
		{
			LOG.info("No B2B unit model found for the logged In User :::" + me);
			throw me;
		}
		catch (final Exception e)
		{
			LOG.info("Exception occured while fetching B2B unit model for Logged In User ::: " + e);
			throw e;
		}
		return energizerB2BUnitModel;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.energizer.facades.accounts.EnergizerCompanyB2BCommerceFacade#updateProfile(de.hybris.platform.commercefacades
	 * .user.data.CustomerData)
	 */
	@SuppressWarnings("deprecation")
	@Override
	public void updateProfile(final CustomerData customerData)
	{
		try
		{
			validateDataBeforeUpdate(customerData);
			final String name = getCustomerNameStrategy().getName(customerData.getFirstName(), customerData.getLastName());
			final EnergizerB2BCustomerModel customer = (EnergizerB2BCustomerModel) getCurrentSessionCustomer();
			customer.setOriginalUid(customerData.getDisplayUid());
			customer.setContactNumber(customerData.getContactNumber());
			customer.setPasswordQuestion(customerData.getPasswordQuestion());
			customer.setPasswordAnswer(customerData.getPasswordAnswer());
			customer.setIsPasswordQuestionSet(true);
			customerAccountService.updateProfile(customer, customerData.getTitleCode(), name, customerData.getUid());
			getModelService().save(customer);
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
	}


	/*
	 * (non-Javadoc)
	 *
	 * @see com.energizer.facades.accounts.EnergizerCompanyB2BCommerceFacade#updatePassword(com.energizer.core.model.
	 * EnergizerB2BCustomerModel)
	 */
	@Override
	public boolean updatingPassword(final String newPassword, final String token) throws TokenInvalidatedException
	{

		boolean flag = false, checkedMismatch = false;
		final int prevPasswordsLength = prevPasswordCount;
		final String delimiter = passwordDelimiter;

		String newEncodedPassword = null;
		int i = 0;

		final SecureToken data = getSecureTokenService().decryptData(token);
		final EnergizerB2BCustomerModel customerModel = (EnergizerB2BCustomerModel) getUserService().getUserForUID(data.getData(),
				CustomerModel.class);
		//final EnergizerB2BCustomerModel customerModel = (EnergizerB2BCustomerModel) getCurrentSessionCustomer();

		newEncodedPassword = getPasswordEncoderService().encode(customerModel, newPassword, customerModel.getPasswordEncoding());

		if (customerModel.getPreviousPasswords() != null)
		{
			checkedMismatch = checkPreviousPasswordMatch(customerModel, newPassword);

			if (checkedMismatch == false)
			{
				return flag;
			}
			else
			{

				if (customerModel.getPreviousPasswords().contains(delimiter))
				{
					LOG.info("With delimiter");
					final StringTokenizer tempStringArray = new StringTokenizer(customerModel.getPreviousPasswords(), delimiter);
					LOG.info(" String Tokenizer's size: " + tempStringArray.countTokens());

					final String[] prevPasswords = new String[tempStringArray.countTokens()];

					while (tempStringArray.hasMoreElements())
					{
						prevPasswords[i] = tempStringArray.nextElement().toString();
						i++;
					}

					LOG.info("Previous Passwords length: " + prevPasswords.length);
					if (prevPasswords.length == prevPasswordsLength)
					{

						customerModel.setPreviousPasswords(prevPasswords[1] + delimiter + prevPasswords[2] + delimiter
								+ prevPasswords[3] + delimiter + prevPasswords[4] + delimiter + newEncodedPassword);
					}
					else if (prevPasswords.length < prevPasswordsLength)
					{
						customerModel.setPreviousPasswords(customerModel.getPreviousPasswords() + delimiter + newEncodedPassword);
					}

					customerModel.setPasswordModifiedTime(new Date());
					getModelService().save(customerModel);
					super.updatePassword(token, newPassword);
					flag = true;
					return flag;
				}
				else
				{
					LOG.info("Without delimiter");
					customerModel.setPreviousPasswords(customerModel.getPreviousPasswords() + delimiter + newEncodedPassword);
					customerModel.setPasswordModifiedTime(new Date());
					getModelService().save(customerModel);
					super.updatePassword(token, newPassword);
					flag = true;
					return flag;
				}
			}
		}
		else
		{
			customerModel.setPreviousPasswords(newEncodedPassword);
			customerModel.setPasswordModifiedTime(new Date());
			getModelService().save(customerModel);
			super.updatePassword(token, newPassword);
			flag = true;
			return flag;

		}
	}

	/**
	*
	*
	*/
	@Override
	public EnergizerB2BCustomerModel getExistingUserForUID(final String email)
	{
		EnergizerB2BCustomerModel customerModel = null;
		try
		{
			customerModel = (EnergizerB2BCustomerModel) userService.getUserForUID(email.toLowerCase());
		}
		catch (final UnknownIdentifierException e)
		{
			customerModel = null;
		}
		return customerModel;
	}


	/**
	 *
	 *
	 *
	 */
	public boolean checkPreviousPasswordMatch(final EnergizerB2BCustomerModel customerModel, final String newPassword)
	{
		String newEncodedPassword = null;
		final String delimiter = "|";
		boolean flag = false;

		LOG.info("Previuos Passwords: " + customerModel.getPreviousPasswords());
		final StringTokenizer passwordString = new StringTokenizer(customerModel.getPreviousPasswords(), delimiter);

		while (passwordString.hasMoreElements())
		{
			final String previousPassword = passwordString.nextElement().toString();
			LOG.info(" Passwords: " + previousPassword);
			newEncodedPassword = getPasswordEncoderService().encode(customerModel, newPassword, customerModel.getPasswordEncoding());
			LOG.info("Encoded Password: " + newEncodedPassword);
			if (previousPassword.equals(newEncodedPassword))
			{
				LOG.info("New Password matches with the previous 5 passwords");
				flag = false;
				return flag;
			}
			else
			{
				flag = true;
			}
		}

		return flag;
	}

	public boolean checkPreviousPasswordMatch(final EnergizerB2BEmployeeModel employeeModel, final String newPassword)
	{
		String newEncodedPassword = null;
		final String delimiter = "|";
		boolean flag = false;

		LOG.info("Previuos Passwords: " + employeeModel.getPreviousPasswords());
		final StringTokenizer passwordString = new StringTokenizer(employeeModel.getPreviousPasswords(), delimiter);

		while (passwordString.hasMoreElements())
		{
			final String previousPassword = passwordString.nextElement().toString();
			LOG.info(" Passwords: " + previousPassword);
			newEncodedPassword = getPasswordEncoderService().encode(employeeModel, newPassword, employeeModel.getPasswordEncoding());
			LOG.info("Encoded Password: " + newEncodedPassword);
			if (previousPassword.equals(newEncodedPassword))
			{
				LOG.info("New Password matches with the previous 5 passwords");
				flag = false;
				return flag;
			}
			else
			{
				flag = true;
			}
		}

		return flag;
	}

	/**
	 *
	 *
	 */
	@Override
	public boolean validateCurrentPassword(final String currentPassword)
	{
		boolean valid = false;

		final EnergizerB2BCustomerModel customerModel = (EnergizerB2BCustomerModel) getCurrentSessionCustomer();
		customerModel.getEncodedPassword();
		final String encodedCurrentPassword = getPasswordEncoderService().encode(customerModel, currentPassword,
				customerModel.getPasswordEncoding());
		if (encodedCurrentPassword.equals(customerModel.getEncodedPassword()))
		{
			valid = true;
		}
		else
		{
			valid = false;
		}

		return valid;

	}

	//	WeSell Implementation -  Added Code Changes to change password/update profile details in My-Account for Sales Rep login - by Venkat

	public boolean validateEmployeeCurrentPassword(final String currentPassword)
	{

		//final PBKDF2WithHmacSHA1SaltedPasswordEncoder password = new PBKDF2WithHmacSHA1SaltedPasswordEncoder();
		boolean valid = false;
		try
		{
			final UserModel salesRepEmployeeModel = (UserModel) getSessionService().getAttribute("salesRepEmployeeModel");

			LOG.info("Employee ID " + salesRepEmployeeModel.getUid());
			LOG.info(salesRepEmployeeModel.getPasswordEncoding() + "Sales rep model encoding");
			final User user = UserManager.getInstance().getUserByLogin(salesRepEmployeeModel.getUid());

			LOG.info(user.getPasswordEncoding() + "User model emcoding");

			final String encodedPassword = user.getEncodedPassword();

			LOG.info(encodedPassword + "Existing encoded");
			/*
			 * final String employeePswrd = password.encode(salesRepEmployeeModel.getUid(), currentPassword);
			 *
			 * LOG.info(employeePswrd + "Employee pdkdf password encode");
			 */




			final String encodedCurrentPassword = getPasswordEncoderService().encode(salesRepEmployeeModel, currentPassword,
					salesRepEmployeeModel.getPasswordEncoding());

			LOG.info(encodedCurrentPassword + "new encoded");

			if (encodedCurrentPassword.equals(encodedPassword))
			{
				valid = true;
			}
			else
			{
				valid = false;
			}
			return valid;
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}

		return valid;


	}

	//	WeSell Implementation -  Added Code Changes to change password/update profile details in My-Account for Sales Rep login - by Venkat


	/**
	 *
	 *
	 *
	 */
	@Override
	public boolean changingPassword(final String currentPassword, final String newPassword)
	{
		boolean flag = false;
		try
		{
		boolean checkedMismatch = false;
		final int prevPasswordsLength = prevPasswordCount;
		final String delimiter = passwordDelimiter;
		String newEncodedPassword = null;
		int i = 0;

		final EnergizerB2BCustomerModel customerModel = (EnergizerB2BCustomerModel) getCurrentSessionCustomer();
		newEncodedPassword = getPasswordEncoderService().encode(customerModel, newPassword, customerModel.getPasswordEncoding());
		if (customerModel.getPreviousPasswords() != null)
		{
			checkedMismatch = checkPreviousPasswordMatch(customerModel, newPassword);

			if (checkedMismatch == false)
			{
				return flag;
			}
			else
			{

				if (customerModel.getPreviousPasswords().contains(delimiter))
				{
					LOG.info("With delimiter");
					final StringTokenizer tempStringArray = new StringTokenizer(customerModel.getPreviousPasswords(), delimiter);
					LOG.info(" String Tokenizer's size: " + tempStringArray.countTokens());

					final String[] prevPasswords = new String[tempStringArray.countTokens()];

					while (tempStringArray.hasMoreElements())
					{
						prevPasswords[i] = tempStringArray.nextElement().toString();
						i++;
					}

					LOG.info("Previous Passwords length: " + prevPasswords.length);
					if (prevPasswords.length == prevPasswordsLength)
					{

						customerModel.setPreviousPasswords(prevPasswords[1] + delimiter + prevPasswords[2] + delimiter
								+ prevPasswords[3] + delimiter + prevPasswords[4] + delimiter + newEncodedPassword);
					}
					else if (prevPasswords.length < prevPasswordsLength)
					{
						customerModel.setPreviousPasswords(customerModel.getPreviousPasswords() + delimiter + newEncodedPassword);
					}

					customerModel.setPasswordModifiedTime(new Date());
					getModelService().save(customerModel);
					try
					{
						super.changePassword(currentPassword, newPassword);
					}
					catch (final PasswordMismatchException localException)
					{

					}
					flag = true;
					return flag;
				}
				else
				{
					LOG.info("Without delimiter");
					customerModel.setPreviousPasswords(customerModel.getPreviousPasswords() + delimiter + newEncodedPassword);
					customerModel.setPasswordModifiedTime(new Date());
					getModelService().save(customerModel);
					super.changePassword(currentPassword, newPassword);
					flag = true;
					return flag;
				}
			}
		}
		else
		{
			customerModel.setPreviousPasswords(newEncodedPassword);
			customerModel.setPasswordModifiedTime(new Date());
			getModelService().save(customerModel);
			super.changePassword(currentPassword, newPassword);
			flag = true;
			return flag;

		}
		}
		catch (final Exception e)
		{
			LOG.info("Exception Occured in changing customer password :::" + e);
			throw e;
		}
	}

	//	WeSell Implementation -  Added Code Changes to change password/update profile details in My-Account for Sales Rep login - by Venkat
	public boolean changingEmployeePassword(final String currentPassword, final String newPassword)
	{
		boolean flag = false;
		try
		{
		boolean checkedMismatch = false;
		final int prevPasswordsLength = prevPasswordCount;
		final String delimiter = passwordDelimiter;
		String newEncodedPassword = null;
		int i = 0;

		final EnergizerB2BEmployeeModel salesRepEmployeeModel = (EnergizerB2BEmployeeModel) getSessionService()
				.getAttribute("salesRepEmployeeModel");

		newEncodedPassword = getPasswordEncoderService().encode(salesRepEmployeeModel, newPassword,
				salesRepEmployeeModel.getPasswordEncoding());

		if (salesRepEmployeeModel.getPreviousPasswords() != null)
		{
			checkedMismatch = checkPreviousPasswordMatch(salesRepEmployeeModel, newPassword);

			if (checkedMismatch == false)
			{
				return flag;
			}
			else
			{

				if (salesRepEmployeeModel.getPreviousPasswords().contains(delimiter))
				{
					LOG.info("With delimiter");
					final StringTokenizer tempStringArray = new StringTokenizer(salesRepEmployeeModel.getPreviousPasswords(),
							delimiter);
					LOG.info(" String Tokenizer's size: " + tempStringArray.countTokens());

					final String[] prevPasswords = new String[tempStringArray.countTokens()];

					while (tempStringArray.hasMoreElements())
					{
						prevPasswords[i] = tempStringArray.nextElement().toString();
						i++;
					}

					LOG.info("Previous Passwords length: " + prevPasswords.length);
					if (prevPasswords.length == prevPasswordsLength)
					{

						salesRepEmployeeModel.setPreviousPasswords(prevPasswords[1] + delimiter + prevPasswords[2] + delimiter
								+ prevPasswords[3] + delimiter + prevPasswords[4] + delimiter + newEncodedPassword);
					}
					else if (prevPasswords.length < prevPasswordsLength)
					{
						salesRepEmployeeModel
								.setPreviousPasswords(salesRepEmployeeModel.getPreviousPasswords() + delimiter + newEncodedPassword);
					}

					salesRepEmployeeModel.setPasswordModifiedTime(new Date());
					getModelService().save(salesRepEmployeeModel);
					try
					{
						changeEmployeePassword(currentPassword, newPassword);
					}
					catch (final PasswordMismatchException localException)
					{

					}
					flag = true;
					return flag;
				}
				else
				{
					LOG.info("Without delimiter");
					salesRepEmployeeModel
							.setPreviousPasswords(salesRepEmployeeModel.getPreviousPasswords() + delimiter + newEncodedPassword);
					salesRepEmployeeModel.setPasswordModifiedTime(new Date());
					getModelService().save(salesRepEmployeeModel);
					changeEmployeePassword(currentPassword, newPassword);
					flag = true;
					return flag;
				}
			}
		}
		else
		{
			salesRepEmployeeModel.setPreviousPasswords(newEncodedPassword);

			salesRepEmployeeModel.setPasswordModifiedTime(new Date());
			getModelService().save(salesRepEmployeeModel);
			super.changePassword(currentPassword, newPassword);
			flag = true;
			return flag;
		}
		}
		catch (final Exception e)
		{
			LOG.info("Exception Occured in changing Password :::" + e);
			throw e;
		}
	}

	//	WeSell Implementation -  Added Code Changes to change password/update profile details in My-Account for Sales Rep login - by Venkat

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.energizer.facades.accounts.EnergizerCompanyB2BCommerceFacade#findB2BAdministratorForCustomer(com.energizer
	 * .core.model.EnergizerB2BUnitModel.EnergizerB2BUnitModel)
	 */

	public B2BCustomerModel findB2BAdministratorForCustomer(final EnergizerB2BUnitModel b2bModel)
	{
		@SuppressWarnings("unchecked")
		final List<B2BCustomerModel> b2bAdminGroupUsers = new ArrayList<B2BCustomerModel>(
				defaultB2BUnitService.getUsersOfUserGroup(b2bModel, B2BConstants.B2BADMINGROUP, true));
		return (CollectionUtils.isNotEmpty(b2bAdminGroupUsers) ? b2bAdminGroupUsers.get(0) : null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.energizer.facades.accounts.EnergizerCompanyB2BCommerceFacade#updateCustomer(de.hybris.platform.commercefacades
	 * .user.data.CustomerData)
	 */

	@Override
	public void updateCustomer(final CustomerData customerData) throws DuplicateUidException
	{
		validateParameterNotNullStandardMessage("customerData", customerData);
		final EnergizerB2BCustomerModel energizerB2BCustomerModel;
		if (StringUtils.isEmpty(customerData.getUid()))
		{
			energizerB2BCustomerModel = this.getModelService().create(EnergizerB2BCustomerModel.class);
			energizerB2BCustomerModel.setRegistrationEmailFlag(Boolean.TRUE);
		}
		else
		{
			energizerB2BCustomerModel = (EnergizerB2BCustomerModel) userService.getUserForUID(customerData.getUid());
		}
		if (null != customerData && null != energizerB2BCustomerModel)
		{
			energizerCustomerReversePopulator.populate(customerData, energizerB2BCustomerModel);
			modelService.save(energizerB2BCustomerModel);
		}
	}

	@Override
	public List getUserGroups()
	{
		return energizerGroupsLookUpStrategy.getGroups();
	}

	/**
	 * As per enhancement added new group b2bviewergroup
	 */
	public void populateRolesByCustomer(final String uuid, final CustomerData target)
	{
		final List<String> roles = new ArrayList<String>();
		final EnergizerB2BCustomerModel model = userService.getUserForUID(uuid, EnergizerB2BCustomerModel.class);
		final Set<PrincipalGroupModel> roleModels = new HashSet<PrincipalGroupModel>(model.getGroups());
		CollectionUtils.filter(roleModels, PredicateUtils.notPredicate(PredicateUtils.instanceofPredicate(B2BUnitModel.class)));
		CollectionUtils.filter(roleModels,
				PredicateUtils.notPredicate(PredicateUtils.instanceofPredicate(B2BUserGroupModel.class)));
		for (final PrincipalGroupModel role : roleModels)
		{
			// only display allowed usergroups
			if (energizerGroupsLookUpStrategy.getUserGroups().contains(role.getUid()))
			{
				roles.add(role.getUid());
			}
		}
		target.setRoles(roles);
	}

	/**
	 * As per enhancement added new group b2bviewergroup
	 */
	@Override
	public void populateUserRoles(final SearchPageData<CustomerData> b2bCustomer)
	{
		List<String> roles = null;
		for (int i = 0; i < b2bCustomer.getResults().size(); i++)
		{
			roles = new ArrayList<String>();
			final EnergizerB2BCustomerModel customerModel = userService.getUserForUID(b2bCustomer.getResults().get(i).getUid(),
					EnergizerB2BCustomerModel.class);
			final Set<PrincipalGroupModel> roleModels = new HashSet<PrincipalGroupModel>(customerModel.getGroups());
			for (final PrincipalGroupModel role : roleModels)
			{
				// only display allowed usergroups
				if (energizerGroupsLookUpStrategy.getUserGroups().contains(role.getUid()))
				{
					roles.add(role.getUid());
				}
			}
			b2bCustomer.getResults().get(i).setRoles(roles);
		}
	}

	/**
	 * @return the secureTokenService
	 */
	public SecureTokenService getSecureTokenService()
	{
		return secureTokenService;
	}

	/**
	 * @param secureTokenService
	 *           the secureTokenService to set
	 */
	public void setSecureTokenService(final SecureTokenService secureTokenService)
	{
		this.secureTokenService = secureTokenService;
	}


	/**
	 * @return the energizerCompanyB2BCommerceService
	 */
	public DefaultEnergizerCompanyB2BCommerceService getEnergizerCompanyB2BCommerceService()
	{
		return defaultEnergizerCompanyB2BCommerceService;
	}

	/**
	 * @param defaultEnergizerCompanyB2BCommerceService
	 *           the energizerCompanyB2BCommerceService to set
	 */
	public void setDefaultEnergizerCompanyB2BCommerceService(
			final DefaultEnergizerCompanyB2BCommerceService defaultEnergizerCompanyB2BCommerceService)
	{
		this.defaultEnergizerCompanyB2BCommerceService = defaultEnergizerCompanyB2BCommerceService;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.energizer.facades.accounts.EnergizerCompanyB2BCommerceFacade#sendUnlockAccountEmail(java.lang.String)
	 */
	@Override
	public void sendUnlockAccountEmail(final String uid)
	{
		defaultEnergizerCompanyB2BCommerceService.sendUnlockAccountEmail(uid);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.energizer.facades.accounts.EnergizerCompanyB2BCommerceFacade#unlockCustomerAccount(java.lang.String)
	 */
	public boolean unlockCustomerAccount(final String email) throws UnknownIdentifierException
	{
		return defaultEnergizerCompanyB2BCommerceService.unlockCustomerAccount(email);
	}

	public void updateEmployeeProfile(final EnergizerB2BEmployeeData employeeData) throws Exception
	{
		try
		{
			validateEmployeeDataBeforeUpdate(employeeData);

			final String name = getCustomerNameStrategy().getName(employeeData.getFirstName(), employeeData.getLastName());
			final EnergizerB2BEmployeeModel salesRepEmployeeModel = (EnergizerB2BEmployeeModel) getSessionService()
					.getAttribute("salesRepEmployeeModel");

			salesRepEmployeeModel.setName(name);
			salesRepEmployeeModel.setFirstName(employeeData.getFirstName());
			salesRepEmployeeModel.setLastName(employeeData.getLastName());
			salesRepEmployeeModel.setContactNumber(employeeData.getContactNumber());
			salesRepEmployeeModel.setPasswordQuestion(employeeData.getPasswordQuestion());
			salesRepEmployeeModel.setPasswordAnswer(employeeData.getPasswordAnswer());
			salesRepEmployeeModel.setIsPasswordQuestionSet(true);
			salesRepEmployeeModel.setTitle(getUserService().getTitleForCode(employeeData.getTitleCode()));

			defaultB2BEmployeeAccountService.updateEmployeeProfile(salesRepEmployeeModel);
		}
		catch (final Exception e)
		{
			LOG.error("Exception occured while updating sales rep profile ::: " + e);
			throw e;
		}
	}

	protected void validateEmployeeDataBeforeUpdate(final EnergizerB2BEmployeeData employeeData)
			throws NullPointerException, Exception
	{
		try
		{
		validateParameterNotNullStandardMessage("employeeData", employeeData);
		Assert.hasText(employeeData.getTitleCode(), "The field [TitleCode] cannot be empty");
		Assert.hasText(employeeData.getFirstName(), "The field [FirstName] cannot be empty");
		Assert.hasText(employeeData.getLastName(), "The field [LastName] cannot be empty");
		Assert.hasText(employeeData.getContactNumber(), "The field [contactNumber] cannot be empty");
		Assert.hasText(employeeData.getPasswordQuestion(), "The field [passwordQuestion] cannot be empty");
		Assert.hasText(employeeData.getPasswordAnswer(), "The field [passwordAnswer] cannot be empty");
		}
		catch (final NullPointerException ne)
		{
			LOG.info("Null Pointer Exception Occured while validating Employee Data:::" + ne);
			ne.printStackTrace();
		}
		catch (final Exception e)
		{
			LOG.info("Exception Occured in validating Employee Data:::" + e);
			e.printStackTrace();
		}
	}

	//	WeSell Implementation -  Added Code Changes to change password/update profile details in My-Account for Sales Rep login - by Venkat
	public void changeEmployeePassword(final String oldPassword, final String newPassword) throws PasswordMismatchException
	{
		final EnergizerB2BEmployeeModel salesRepEmployeeModel = (EnergizerB2BEmployeeModel) getSessionService()
				.getAttribute("salesRepEmployeeModel");

		try
		{
			getCustomerAccountService().changePassword(salesRepEmployeeModel, oldPassword, newPassword);
		}
		catch (final de.hybris.platform.commerceservices.customer.PasswordMismatchException e)
		{
			LOG.info("Exception Occured since passwords doesn't match :::" + e);
			throw new PasswordMismatchException(e);
		}
		catch (final Exception e)
		{
			LOG.info("Exception occured while changing password ::::" + e);
			e.printStackTrace();
		}
	}
	//	WeSell Implementation -  Added Code Changes to change password/update profile details in My-Account for Sales Rep login - by Venkat
}