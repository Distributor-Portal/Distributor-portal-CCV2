/**
 *
 */
package com.energizer.facades.accounts.impl;

import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.commerceservices.event.AbstractCommerceUserEvent;
import de.hybris.platform.commerceservices.security.SecureToken;
import de.hybris.platform.commerceservices.security.SecureTokenService;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.servicelayer.event.EventService;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;
import de.hybris.platform.store.services.BaseStoreService;

import java.util.Date;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.util.Assert;

import com.energizer.core.event.UnlockAccountEvent;
import com.energizer.core.model.EnergizerB2BCustomerModel;
import com.energizer.facades.accounts.EnergizerCompanyB2BCommerceService;
import com.google.common.base.Preconditions;


/**
 * @author m9005673
 *
 */
public class DefaultEnergizerCompanyB2BCommerceService implements EnergizerCompanyB2BCommerceService
{
	private static final Logger LOG = Logger.getLogger(DefaultEnergizerCompanyB2BCommerceService.class);

	@Resource(name = "userService")
	private UserService userService;
	@Resource
	private SecureTokenService secureTokenService;
	@Resource
	private ModelService modelService;
	@Resource
	private EventService eventService;

	private long tokenValiditySeconds;
	private BaseStoreService baseStoreService;
	private BaseSiteService baseSiteService;
	private CommonI18NService commonI18NService;

	/**
	 * @return the userService
	 */
	public UserService getUserService()
	{
		return userService;
	}

	/**
	 * @param userService
	 *           the userService to set
	 */
	public void setUserService(final UserService userService)
	{
		this.userService = userService;
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
	 * @return the modelService
	 */
	public ModelService getModelService()
	{
		return modelService;
	}

	/**
	 * @param modelService
	 *           the modelService to set
	 */
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	/**
	 * @return the eventService
	 */
	public EventService getEventService()
	{
		return eventService;
	}

	/**
	 * @param eventService
	 *           the eventService to set
	 */
	public void setEventService(final EventService eventService)
	{
		this.eventService = eventService;
	}

	/**
	 * @return the tokenValiditySeconds
	 */
	public long getTokenValiditySeconds()
	{
		return tokenValiditySeconds;
	}

	/**
	 * @param tokenValiditySeconds
	 *           the tokenValiditySeconds to set
	 */
	public void setTokenValiditySeconds(final long tokenValiditySeconds)
	{
		this.tokenValiditySeconds = tokenValiditySeconds;
	}

	/**
	 * @return the baseStoreService
	 */
	public BaseStoreService getBaseStoreService()
	{
		return baseStoreService;
	}

	/**
	 * @param baseStoreService
	 *           the baseStoreService to set
	 */
	public void setBaseStoreService(final BaseStoreService baseStoreService)
	{
		this.baseStoreService = baseStoreService;
	}

	/**
	 * @return the baseSiteService
	 */
	public BaseSiteService getBaseSiteService()
	{
		return baseSiteService;
	}

	/**
	 * @param baseSiteService
	 *           the baseSiteService to set
	 */
	public void setBaseSiteService(final BaseSiteService baseSiteService)
	{
		this.baseSiteService = baseSiteService;
	}

	/**
	 * @return the commonI18NService
	 */
	public CommonI18NService getCommonI18NService()
	{
		return commonI18NService;
	}

	/**
	 * @param commonI18NService
	 *           the commonI18NService to set
	 */
	public void setCommonI18NService(final CommonI18NService commonI18NService)
	{
		this.commonI18NService = commonI18NService;
	}

	public void sendUnlockAccountEmail(final String uid)
	{

		Assert.hasText(uid, "The field [uid] cannot be empty");
		final CustomerModel customerModel = getUserService().getUserForUID(uid.toLowerCase(), CustomerModel.class);

		validateParameterNotNullStandardMessage("customerModel", customerModel);
		final long timeStamp = getTokenValiditySeconds() > 0L ? new Date().getTime() : 0L;
		final SecureToken data = new SecureToken(customerModel.getUid(), timeStamp);
		final String token = getSecureTokenService().encryptData(data);
		customerModel.setToken(token);
		getModelService().save(customerModel);
		getEventService().publishEvent(initializeEvent(new UnlockAccountEvent(token), customerModel));
	}

	public boolean unlockCustomerAccount(final String email) throws UnknownIdentifierException
	{
		final EnergizerB2BCustomerModel customerModel = getExistingUserForUID(email);
		customerModel.setActive(true);
		customerModel.setLoginDisabled(Boolean.FALSE);
		modelService.save(customerModel);
		modelService.refresh(customerModel);
		LOG.info("Is the customer account disabled ??? " + customerModel.isLoginDisabled());
		return customerModel.isLoginDisabled();
	}

	public static void validateParameterNotNull(final Object parameter, final String nullMessage)
	{
		Preconditions.checkArgument(parameter != null, nullMessage);
	}

	public static void validateParameterNotNullStandardMessage(final String parameter, final Object parameterValue)
	{
		validateParameterNotNull(parameterValue, "Parameter " + parameter + " can not be null");
	}

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

	protected AbstractCommerceUserEvent<BaseSiteModel> initializeEvent(final AbstractCommerceUserEvent<BaseSiteModel> event,
			final CustomerModel customerModel)
	{
		event.setBaseStore(getBaseStoreService().getCurrentBaseStore());
		event.setSite(getBaseSiteService().getCurrentBaseSite());
		event.setCustomer(customerModel);
		event.setLanguage(getCommonI18NService().getCurrentLanguage());
		event.setCurrency(getCommonI18NService().getCurrentCurrency());
		return event;
	}


}