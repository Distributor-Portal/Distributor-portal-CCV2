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

import de.hybris.platform.cms2.servicelayer.services.CMSSiteService;
import de.hybris.platform.commerceservices.i18n.CommerceCommonI18NService;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.servicelayer.i18n.I18NService;
import de.hybris.platform.servicelayer.session.SessionService;

import java.io.IOException;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;


/**
 * Default implementation of {@link AuthenticationSuccessHandler}.
 */
public class GUIDAuthenticationSuccessHandler implements AuthenticationSuccessHandler
{
	private GUIDCookieStrategy guidCookieStrategy;
	private AuthenticationSuccessHandler authenticationSuccessHandler;
	@Resource
	private CommerceCommonI18NService commerceCommonI18NService;

	@Resource(name = "cmsSiteService")
	private CMSSiteService cmsSiteService;

	@Resource
	private CommonI18NService commonI18NService;

	@Resource
	private I18NService i18NService;

	@Resource(name = "configurationService")
	private ConfigurationService configurationService;

	@Resource(name = "sessionService")
	private SessionService sessionService;

	private static final String SITE_PERSONALCARE = "site.personalCare";

	@Override
	public void onAuthenticationSuccess(final HttpServletRequest request, final HttpServletResponse response,
			final Authentication authentication) throws IOException, ServletException
	{
		try
		{
		getGuidCookieStrategy().setCookie(request, response);
		getAuthenticationSuccessHandler().onAuthenticationSuccess(request, response, authentication);
		//Added Code changes for WeSell Implementation - START
		if (configurationService.getConfiguration().getString(SITE_PERSONALCARE)
				.equalsIgnoreCase(cmsSiteService.getCurrentSite().getUid()))
		{
			final CurrencyModel currencyModel = commonI18NService.getCurrency("USD");
			commerceCommonI18NService.setCurrentCurrency(currencyModel);
		}
		// Added this session attribute for WeSell Implementation - to disable the checkout button in cart thereby forcing the Sale sRep to get the realtime prices from SAPA
		if ((boolean) sessionService.getAttribute("isSalesRepUserLoggedIn"))
		{
			request.getSession().setAttribute("gotPriceFromSAP", false);
		}
		//Added Code changes for WeSell Implementation - END
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
	}

	protected GUIDCookieStrategy getGuidCookieStrategy()
	{
		return guidCookieStrategy;
	}

	/**
	 * @param guidCookieStrategy
	 *           the guidCookieStrategy to set
	 */
	@Required
	public void setGuidCookieStrategy(final GUIDCookieStrategy guidCookieStrategy)
	{
		this.guidCookieStrategy = guidCookieStrategy;
	}

	protected AuthenticationSuccessHandler getAuthenticationSuccessHandler()
	{
		return authenticationSuccessHandler;
	}

	/**
	 * @param authenticationSuccessHandler
	 *           the authenticationSuccessHandler to set
	 */
	@Required
	public void setAuthenticationSuccessHandler(final AuthenticationSuccessHandler authenticationSuccessHandler)
	{
		this.authenticationSuccessHandler = authenticationSuccessHandler;
	}
}
