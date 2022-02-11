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

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.web.filter.OncePerRequestFilter;


/**
 * This filter addressess the security concerns as follows: cross site reference leakage, cache control, clickjacking
 * 
 * @author kaushik.ganguly
 * 
 */
public class EnergizerSecurityFilter extends OncePerRequestFilter implements CMSFilter
{
	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(EnergizerSecurityFilter.class);

	private static final String X_FRAME_OPTIONS = "X-Frame-Options";

	private static final String X_FRAME_OPTIONS_VALUE = "SAMEORIGIN";

	private static final String CACHE_CONTROL = "Cache-control";

	private static final String CACHE_CONTROL_VALUE = "no-store";

	private static final String PRAGMA = "Pragma";

	private static final String PRAGMA_VALUE = "no-cache";




	@Override
	protected void doFilterInternal(final HttpServletRequest httpRequest, final HttpServletResponse httpResponse,
			final FilterChain filterChain) throws ServletException, IOException
	{

		if (httpResponse.getHeader(X_FRAME_OPTIONS) == null)
		{
			httpResponse.setHeader(X_FRAME_OPTIONS, X_FRAME_OPTIONS_VALUE);
		}

		if (httpResponse.getHeader(CACHE_CONTROL) == null)
		{
			httpResponse.setHeader(CACHE_CONTROL, CACHE_CONTROL_VALUE);
		}

		if (httpResponse.getHeader(PRAGMA) == null)
		{
			httpResponse.setHeader(PRAGMA, PRAGMA_VALUE);
		}

		filterChain.doFilter(httpRequest, httpResponse);

	}
}
