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
package com.energizer.facades.process.email.context;

import de.hybris.platform.acceleratorservices.model.cms2.pages.EmailPageModel;
import de.hybris.platform.commerceservices.model.process.StoreFrontCustomerProcessModel;
import de.hybris.platform.commerceservices.model.process.UnlockAccountProcessModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLEncoder;

import org.springframework.beans.factory.annotation.Autowired;


/**
 * Velocity context for a forgotten password email.
 */
public class UnlockAccountEmailContext extends CustomerEmailContext
{
	/**
	 * set the DEFAULT_TIMEOUT_IN_MINUTES
	 */
	private static final int DEFAULT_TIMEOUT_IN_MINUTES = 30;
	private static final String EXP_IN_MIN = "forgottenPassword.emailContext.expiresInMinutes";
	/**
	 * This is to set the password expires time done through the local.properties
	 **/
	private int expiresInMinutes = 0;


	private String token;
	@Autowired
	private ConfigurationService configurationService;

	@Override
	public void init(final StoreFrontCustomerProcessModel storeFrontCustomerProcessModel, final EmailPageModel emailPageModel)
	{
		super.init(storeFrontCustomerProcessModel, emailPageModel);
		if (storeFrontCustomerProcessModel instanceof UnlockAccountProcessModel)
		{
			setToken(((UnlockAccountProcessModel) storeFrontCustomerProcessModel).getToken());
		}
	}

	public int getExpiresInMinutes()
	{
		expiresInMinutes = configurationService.getConfiguration()
				.getBigInteger(EXP_IN_MIN, BigInteger.valueOf(DEFAULT_TIMEOUT_IN_MINUTES)).intValue();
		return expiresInMinutes;
	}

	public void setExpiresInMinutes(final int expiresInMinutes)
	{
		this.expiresInMinutes = expiresInMinutes;
	}

	public String getToken()
	{
		return token;
	}

	public void setToken(final String token)
	{
		this.token = token;
	}

	public String getURLEncodedToken() throws UnsupportedEncodingException
	{
		return URLEncoder.encode(token, "UTF-8");
	}

	// For unlock account page redirection URL link
	public String getSecureUnlockAccountRequestUrl() throws UnsupportedEncodingException
	{
		return getSiteBaseUrlResolutionService().getWebsiteUrlForSite(getBaseSite(), getUrlEncodingAttributes(), true,
				"/login/pw/confirm-account", "token=" + getURLEncodedToken());
	}

	// For unlock account page display URL link
	public String getDisplaySecureUnlockAccountRequestUrl() throws UnsupportedEncodingException
	{
		return getSiteBaseUrlResolutionService().getWebsiteUrlForSite(getBaseSite(), getUrlEncodingAttributes(), true,
				"/my-account/unlock-account");
	}

}
