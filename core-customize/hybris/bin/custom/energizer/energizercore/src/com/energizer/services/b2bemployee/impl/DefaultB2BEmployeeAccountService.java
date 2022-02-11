/**
 *
 */
package com.energizer.services.b2bemployee.impl;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;

import de.hybris.platform.commerceservices.customer.DuplicateUidException;
import de.hybris.platform.commerceservices.customer.TokenInvalidatedException;
import de.hybris.platform.commerceservices.customer.impl.DefaultCustomerAccountService;
import de.hybris.platform.commerceservices.security.SecureToken;
import de.hybris.platform.servicelayer.event.EventService;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;

import java.util.Date;

import org.apache.log4j.Logger;
import org.springframework.util.Assert;

import com.energizer.core.event.B2BEmployeeAbstractCommerceUserEvent;
import com.energizer.core.event.B2BEmployeeForgottenPwdEvent;
import com.energizer.core.event.B2BEmployeeUnlockAccountEvent;
import com.energizer.core.event.RegisterEmployeeEvent;
import com.energizer.core.model.EnergizerB2BEmployeeModel;


/**
 * @author Srivenkata_N
 *
 */

public class DefaultB2BEmployeeAccountService extends DefaultCustomerAccountService
{
	private static final Logger LOG = Logger.getLogger(DefaultB2BEmployeeAccountService.class);

	private EventService eventService;


	/**
	 * @return the eventService
	 */
	@Override
	public EventService getEventService()
	{
		return eventService;
	}

	/**
	 * @param eventService
	 *           the eventService to set
	 */
	@Override
	public void setEventService(final EventService eventService)
	{
		this.eventService = eventService;
	}

	protected B2BEmployeeAbstractCommerceUserEvent initializeEvent(final B2BEmployeeAbstractCommerceUserEvent event,
			final EnergizerB2BEmployeeModel employeeModel)
	{
		event.setBaseStore(getBaseStoreService().getCurrentBaseStore());
		event.setSite(getBaseSiteService().getCurrentBaseSite());
		event.setSalesRepUser(employeeModel);
		event.setLanguage(getCommonI18NService().getCurrentLanguage());
		event.setCurrency(getCommonI18NService().getCurrentCurrency());
		return event;
	}

	public void forgottenPassword(final EnergizerB2BEmployeeModel employeeModel)
	{
		try
		{
			validateParameterNotNullStandardMessage("employeeModel", employeeModel);
			final long timeStamp = getTokenValiditySeconds() > 0L ? new Date().getTime() : 0L;
			final SecureToken data = new SecureToken(employeeModel.getUid(), timeStamp);
			final String token = getSecureTokenService().encryptData(data);
			employeeModel.setToken(token);
			getModelService().save(employeeModel);
			getEventService().publishEvent(initializeEvent(new B2BEmployeeForgottenPwdEvent(token, employeeModel), employeeModel));
		}
		catch (final Exception ex)
		{
			LOG.info("Exception occured in service:::: " + ex.getMessage());
			ex.printStackTrace();
		}
	}

	/**
	 * @param oldPassword
	 * @param newPassword
	 * @throws TokenInvalidatedException
	 */


	public void changeEmployeePassword(final String token, final String newPassword) throws TokenInvalidatedException
	{
		Assert.hasText(token, "The field [token] cannot be empty");
		Assert.hasText(newPassword, "The field [newPassword] cannot be empty");

		final SecureToken data = getSecureTokenService().decryptData(token);
		if (getTokenValiditySeconds() > 0L)
		{
			final long delta = new Date().getTime() - data.getTimeStamp();
			if (delta / 1000 > getTokenValiditySeconds())
			{
				throw new IllegalArgumentException("token expired");
			}
		}

		final EnergizerB2BEmployeeModel employee = getUserService().getUserForUID(data.getData(), EnergizerB2BEmployeeModel.class);
		if (employee == null)
		{
			throw new IllegalArgumentException("user for token not found");
		}
		if (!token.equals(employee.getToken()))
		{
			throw new TokenInvalidatedException();
		}
		employee.setToken(null);
		employee.setLoginDisabled(false);
		getModelService().save(employee);

		getUserService().setPassword(data.getData(), newPassword, getPasswordEncoding());

	}

	public void sendEmployeeUnlockAccountEmail(final String uid)
	{

		Assert.hasText(uid, "The field [uid] cannot be empty");
		final EnergizerB2BEmployeeModel employeeModel = getUserService().getUserForUID(uid.toLowerCase(),
				EnergizerB2BEmployeeModel.class);

		validateParameterNotNullStandardMessage("employeeModel", employeeModel);
		final long timeStamp = getTokenValiditySeconds() > 0L ? new Date().getTime() : 0L;
		final SecureToken data = new SecureToken(employeeModel.getUid(), timeStamp);
		final String token = getSecureTokenService().encryptData(data);
		employeeModel.setToken(token);
		getModelService().save(employeeModel);
		getEventService().publishEvent(initializeEvent(new B2BEmployeeUnlockAccountEvent(token), employeeModel));
	}

	/**
	 * @param email
	 * @return
	 */
	public boolean unlockEmployeeAccount(final String email)
	{

		final EnergizerB2BEmployeeModel employeeModel = getExistingEmployeeForUID(email);
		employeeModel.setActive(true);
		employeeModel.setLoginDisabled(Boolean.FALSE);
		getModelService().save(employeeModel);
		getModelService().refresh(employeeModel);
		LOG.info("Is the employee account disabled ??? " + employeeModel.isLoginDisabled());
		return employeeModel.isLoginDisabled();
	}

	public EnergizerB2BEmployeeModel getExistingEmployeeForUID(final String email)
	{
		EnergizerB2BEmployeeModel employeeModel = null;
		try
		{
			employeeModel = getUserService().getUserForUID(email.toLowerCase(), EnergizerB2BEmployeeModel.class);
		}
		catch (final UnknownIdentifierException e)
		{
			employeeModel = null;
		}
		return employeeModel;
	}

	public void register(final EnergizerB2BEmployeeModel employeeModel) throws DuplicateUidException
	{
		getModelService().save(employeeModel);
		getEventService().publishEvent(initializeEvent(new RegisterEmployeeEvent(), employeeModel));
	}

	//	WeSell Implementation -  Added Code Changes to change password/update profile details in My-Account for Sales Rep login - by Venkat
	public void updateEmployeeProfile(final EnergizerB2BEmployeeModel employeeModel) throws DuplicateUidException, Exception
	{
		try
		{
		validateParameterNotNullStandardMessage("employeeModel", employeeModel);

		getModelService().save(employeeModel);
		LOG.info("Sales Rep profile updated successfully ...");
		}
		catch (final Exception e)
		{
			LOG.info("Sales Rep profile was not updated successfully ...");
			throw e;
		}
	}
	//	WeSell Implementation -  Added Code Changes to change password/update profile details in My-Account for Sales Rep login - by Venkat

}
