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

import de.hybris.platform.acceleratorcms.services.CMSPageContextService;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.servicelayer.user.UserService;

import java.io.IOException;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.method.HandlerMethod;

import com.energizer.storefront.interceptors.BeforeControllerHandler;

/**
 * Spring MVC interceptor that validates that the spring security user and the hybris session user are in sync.
 * If the spring security user and the hybris session user are not in sync then the session is invalidated and
 * the visitor is redirect to the homepage.
 */
public class SecurityUserCheckBeforeControllerHandler implements BeforeControllerHandler
{
	private static final Logger LOG = Logger.getLogger(SecurityUserCheckBeforeControllerHandler.class);

	@Resource(name = "userService")
	private UserService userService;

	@Resource(name = "cmsPageContextService")
	private CMSPageContextService cmsPageContextService;

	@Resource
	private SessionService sessionService;



	@Override
	public boolean beforeController(final HttpServletRequest request, final HttpServletResponse response, final HandlerMethod handler) throws IOException
	{
		try
		{
		// Skip this security check when run from within the WCMS Cockpit
		if (isPreviewDataModelValid(request))
		{
			return true;
		}

		final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		//final String salesRepLogin = (String) sessionService.getAttribute("salesRepLogin");
		//final boolean isSalesRepUser = (boolean) sessionService.getAttribute("isSalesRepUser");
		if (authentication != null)
		{
			final Object principal = authentication.getPrincipal();
			if (principal instanceof String)
			{
				String springSecurityUserId = (String) principal;

				final String hybrisUserId = userService.getCurrentUser().getUid();
				//Added Code changes for WeSell Implementation - START
				if (null != springSecurityUserId && null != hybrisUserId && !springSecurityUserId.equalsIgnoreCase("anonymous")
						&& !hybrisUserId.equalsIgnoreCase("anonymous") && !springSecurityUserId.equals(hybrisUserId))
				{
					final boolean isSalesRepUser = (boolean) sessionService.getAttribute("isSalesRepUserLoggedIn");
					if (isSalesRepUser)
					{
						springSecurityUserId = hybrisUserId;
					}
				}
				//Added Code changes for WeSell Implementation - END
				if (!springSecurityUserId.equals(hybrisUserId))
				{
					LOG.error("User miss-match springSecurityUserId [" + springSecurityUserId + "] hybris session user ["
							+ hybrisUserId + "]. Invalidating session.");

					// Invalidate session and redirect to the root page
					request.getSession().invalidate();

					final String encodedRedirectUrl = response.encodeRedirectURL(request.getContextPath() + "/");
					response.sendRedirect(encodedRedirectUrl);
					return false;
				}
			}
		}
		return true;
		}
		catch (final Exception e)
		{
			LOG.error("User Credential is not correct ::: ", e);
			throw e;
		}
	}

	/**
	 * Checks whether there is a preview data setup for the current request
	 *
	 * @param httpRequest
	 *           current request
	 * @return true whether is valid otherwise false
	 */
	protected boolean isPreviewDataModelValid(final HttpServletRequest httpRequest)
	{
		return cmsPageContextService.getCmsPageRequestContextData(httpRequest).getPreviewData() != null;
	}
}

