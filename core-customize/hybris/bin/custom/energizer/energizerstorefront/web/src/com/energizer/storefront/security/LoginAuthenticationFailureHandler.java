/*
 * [y] hybris Platform
 *
 * Copyright (c) 2000-2014 hybris AG
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of hybris
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with hybris.
 *
 *  
 */
package com.energizer.storefront.security;




import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.servicelayer.user.UserService;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;


/**
 * Handler for login authentication, see {@link SimpleUrlAuthenticationFailureHandler}.
 */
public class LoginAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler
{
	private static final Logger LOG = Logger.getLogger(LoginAuthenticationFailureHandler.class);
	private BruteForceAttackCounter bruteForceAttackCounter;
	private SessionService sessionService;
	private UserService userService;
	private ModelService modelService;

	private final static String FAILED_MAX_ATTEMPTS_TO_LOGIN = "FAILED_MAX_ATTEMPTS_TO_LOGIN";

	/**
	 * @return the sessionService
	 */
	public SessionService getSessionService()
	{
		return sessionService;
	}


	/**
	 * @param sessionService
	 *           the sessionService to set
	 */
	public void setSessionService(final SessionService sessionService)
	{
		this.sessionService = sessionService;
	}


	@Override
	public void onAuthenticationFailure(final HttpServletRequest request, final HttpServletResponse response,
			final AuthenticationException exception) throws IOException, ServletException
	{
		// Register brute attacks
		bruteForceAttackCounter.registerLoginFailure(request.getParameter("j_username"));
		final String username = request.getParameter("j_username");
		UserModel userModel = null;
		try
		{
			userModel = getUserService().getUserForUID(StringUtils.lowerCase(username));
		}
		catch (final Exception e)
		{
			LOG.info("User does not exist.. " + username);
			e.printStackTrace();
		}

		if(userModel == null){
			LOG.info("User does not exist since userModel is null " + username);
		}
		else{
		if (userModel.isLoginDisabled()
				&& getBruteForceAttackCounter().getUserFailedLogins(StringUtils.lowerCase(username)) > getBruteForceAttackCounter()
						.getMaxLoginAttempts())
		{

			bruteForceAttackCounter.registerLoginFailure(userModel.getUid(), getBruteForceAttackCounter().getMaxLoginAttempts());

		}
		else if (!userModel.isLoginDisabled()
				&& getBruteForceAttackCounter().getUserFailedLogins(StringUtils.lowerCase(username)) >= getBruteForceAttackCounter()
						.getMaxLoginAttempts())
		{
			try
			{
				userModel.setLoginDisabled(true);
				getModelService().save(userModel);
				//let's not reset the counter to keep showing the attempt exhaustion message.
				//bruteForceAttackCounter.resetUserCounter(userModel.getUid());
			}
			catch (final UnknownIdentifierException e)
			{
				LOG.warn("Brute force attack attempt for non existing user name " + username);
			}
			
		}
	  }


		if (bruteForceAttackCounter.isAttack(request.getParameter("j_username")))
		{
			sessionService.setAttribute(FAILED_MAX_ATTEMPTS_TO_LOGIN, FAILED_MAX_ATTEMPTS_TO_LOGIN);
		}

		// Store the j_username in the session
		request.getSession().setAttribute("SPRING_SECURITY_LAST_USERNAME", request.getParameter("j_username"));

		super.onAuthenticationFailure(request, response, exception);
	}


	protected BruteForceAttackCounter getBruteForceAttackCounter()
	{
		return bruteForceAttackCounter;
	}

	@Required
	public void setBruteForceAttackCounter(final BruteForceAttackCounter bruteForceAttackCounter)
	{
		this.bruteForceAttackCounter = bruteForceAttackCounter;
	}

	protected UserService getUserService()
	{
		return userService;
	}

	@Required
	public void setUserService(final UserService userService)
	{
		this.userService = userService;
	}

	protected ModelService getModelService()
	{
		return modelService;
	}

	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

}
