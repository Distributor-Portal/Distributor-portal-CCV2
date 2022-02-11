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
package com.energizer.storefront.filters.cms;

import de.hybris.platform.cms2.misc.CMSFilter;
import de.hybris.platform.cms2.model.site.CMSSiteModel;
import de.hybris.platform.cms2.servicelayer.services.CMSSiteService;
import de.hybris.platform.jalo.JaloObjectNoLongerValidException;
import de.hybris.platform.servicelayer.config.ConfigurationService;

import java.io.IOException;
import java.net.URL;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.web.filter.OncePerRequestFilter;


/**
 * This filter addressess the OOTB issue of having the URL repeated twice and lands on to 404, on the request when you
 * login->go to homepage -> logout -> login
 * 
 * @author kaushik.ganguly
 * 
 */
public class EnergizerURLFilter extends OncePerRequestFilter implements CMSFilter
{
	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(EnergizerURLFilter.class);

	private static final String HOMEPAGE = "/";
	private static final String WEBSITE = "website.";
	private static final String HTTPS = ".https";

	private ConfigurationService configurationService;

	private CMSSiteService cmsSiteService;

	/**
	 * @return the configurationService
	 */
	public ConfigurationService getConfigurationService()
	{
		return configurationService;
	}

	/**
	 * @param configurationService
	 *           the configurationService to set
	 */
	public void setConfigurationService(final ConfigurationService configurationService)
	{
		this.configurationService = configurationService;
	}

	/**
	 * @return the cmsSiteService
	 */
	public CMSSiteService getCmsSiteService()
	{
		return cmsSiteService;
	}

	/**
	 * @param cmsSiteService
	 *           the cmsSiteService to set
	 */
	public void setCmsSiteService(final CMSSiteService cmsSiteService)
	{
		this.cmsSiteService = cmsSiteService;
	}

	@Override
	protected void doFilterInternal(final HttpServletRequest httpRequest, final HttpServletResponse httpResponse,
			final FilterChain filterChain) throws ServletException, IOException
	{
		try
		{
			final String requestURL = httpRequest.getRequestURL().toString();
			final String websiteURL = configurationService.getConfiguration().getString(
					WEBSITE + getCurrentCmsSite().getUid() + HTTPS);
			final URL aURL = new URL(requestURL);
			final URL bURL = new URL(websiteURL);
			if (requestURL.indexOf(bURL.getHost()) != -1 && requestURL.indexOf(aURL.getHost()) != -1
					&& requestURL.indexOf(bURL.getHost()) != requestURL.lastIndexOf(aURL.getHost()))
			{
				httpResponse.sendRedirect(HOMEPAGE);
			}
			else
			{
				filterChain.doFilter(httpRequest, httpResponse);
			}

		}
		catch (final Exception ex)
		{
			filterChain.doFilter(httpRequest, httpResponse);
		}

	}

	protected CMSSiteModel getCurrentCmsSite()
	{
		try
		{
			return getCmsSiteService().getCurrentSite();
		}
		catch (final JaloObjectNoLongerValidException ignore)
		{
			return null;
		}
	}
}
