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
package com.energizer.storefront.interceptors.beforecontroller;

import de.hybris.platform.acceleratorservices.urlresolver.SiteBaseUrlResolutionService;
import de.hybris.platform.cms2.servicelayer.services.CMSSiteService;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.util.Config;

import java.io.IOException;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.method.HandlerMethod;

import com.energizer.storefront.interceptors.BeforeControllerHandler;



/**
 * An interceptor to set up the request the detected device information.
 */
public class EnergizerPortalLoginBeforeControllerHandler implements BeforeControllerHandler
{
	private static final Logger LOG = Logger.getLogger(EnergizerPortalLoginBeforeControllerHandler.class);

	@Resource
	private CMSSiteService cmsSiteService;

	@Resource
	private UserService userService;

	@Resource
	private RedirectStrategy redirectStrategy;

	@Resource
	private SiteBaseUrlResolutionService siteBaseUrlResolutionService;

	private static final String LOGIN_URL = "/login";

	private static final String TERMS_CONDITIONS = "termsandconditions.page.url";

	private static final String CONTACTUS_URL = "/ContactUs";

	private static final String FAQ = "faq.page.url";

	private static final String LANGUAGE_URL = "/_s/language";

	private static final String HOME_URL = "/";

	@Override
	public boolean beforeController(final HttpServletRequest request, final HttpServletResponse response,
			final HandlerMethod handler)
	{
		boolean retVal = true;

		final boolean isUserAnonymous = isAnonymousUser(userService.getCurrentUser());


		if (isUserAnonymous && isNotLoginRequest(request))
		{
			redirect(request, response, getRedirectUrl(LOGIN_URL, true));
			retVal = false;
		}



		return retVal;
	}


	protected String getRedirectUrl(final String mapping, final boolean secured)
	{
		return siteBaseUrlResolutionService.getWebsiteUrlForSite(cmsSiteService.getCurrentSite(), secured, mapping);
	}

	private boolean isAnonymousUser(final UserModel user)
	{
		final ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
		final HttpSession session = attr.getRequest().getSession(true);

		boolean isUserAnonymous = user == null || userService.isAnonymousUser(user);

		if ((String) session.getAttribute(RequireHardLoginBeforeControllerHandler.SECURE_GUID_SESSION_KEY) == null)
		{
			isUserAnonymous = true;
		}
		return isUserAnonymous;
	}


	protected void redirect(final HttpServletRequest request, final HttpServletResponse response, final String targetUrl)
	{

		try
		{


			LOG.info(String.format("Redirecting to url '%s'.", targetUrl));


			redirectStrategy.sendRedirect(request, response, targetUrl);

		}
		catch (final IOException ex)
		{
			LOG.error("Unable to redirect.", ex);
		}

	}

	protected boolean isNotLoginRequest(final HttpServletRequest request)
	{
		final boolean loginUrlFlag = !request.getRequestURI().contains(LOGIN_URL);
		final boolean languageUrlFlag = !request.getRequestURI().contains(LANGUAGE_URL);
		final boolean tncFlag = !request.getRequestURI().contains(Config.getParameter(TERMS_CONDITIONS));
		final boolean fagFlag = !request.getRequestURI().contains(Config.getParameter(FAQ));
		final boolean contactUsFlag = !request.getRequestURI().contains(CONTACTUS_URL);
		return loginUrlFlag && languageUrlFlag && tncFlag && fagFlag && contactUsFlag;
	}

}
