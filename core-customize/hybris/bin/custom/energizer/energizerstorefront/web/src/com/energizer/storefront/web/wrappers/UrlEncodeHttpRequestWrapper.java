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
package com.energizer.storefront.web.wrappers;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.lang3.StringUtils;


public class UrlEncodeHttpRequestWrapper extends HttpServletRequestWrapper
{
	private final String pattern;

	public UrlEncodeHttpRequestWrapper(final HttpServletRequest request, final String pattern)
	{
		super(request);
		this.pattern = pattern;
	}

	@Override
	public String getContextPath()
	{
		final String originalContextPath = super.getContextPath();
		if (StringUtils.isBlank(originalContextPath))
		{
			return originalContextPath;
		}
		return originalContextPath + "/" + pattern;
	}

	@Override
	public String getRequestURI()
	{
		final String originalContextPath = super.getContextPath();
		if (StringUtils.isBlank(originalContextPath))
		{
			// Storefront deployed at root: strip the encoding pattern from the URI
			// so Spring sees /login instead of /USD/login
			final String originalRequestURI = super.getRequestURI();
			final String patternPrefix = "/" + pattern;
			if (StringUtils.startsWith(originalRequestURI, patternPrefix + "/"))
			{
				return StringUtils.removeStart(originalRequestURI, patternPrefix);
			}
			else if (originalRequestURI.equals(patternPrefix))
			{
				return "/";
			}
			return originalRequestURI;
		}
		// Non-root context path: prepend pattern to context path
		final String contextPath = this.getContextPath();
		final String originalRequestURI = super.getRequestURI();
		final String originalRequestUriMinusAnyContextPath;
		if (StringUtils.startsWith(originalRequestURI, contextPath))
		{
			originalRequestUriMinusAnyContextPath = StringUtils.removeStart(originalRequestURI, contextPath);
		}
		else if (StringUtils.startsWith(originalRequestURI, originalContextPath))
		{
			originalRequestUriMinusAnyContextPath = StringUtils.removeStart(originalRequestURI, originalContextPath);
		}
		else
		{
			originalRequestUriMinusAnyContextPath = originalRequestURI;
		}
		return contextPath + originalRequestUriMinusAnyContextPath;
	}


	@Override
	public String getServletPath()
	{
		final String originalServletPath = super.getServletPath();
		if (("/").equals(originalServletPath) || ("/" + pattern).equals(originalServletPath)
				|| ("/" + pattern + "/").equals(originalServletPath))
		{
			return "";
		}
		else if (urlPatternChecker(originalServletPath, pattern))
		{
			return StringUtils.replace(originalServletPath, "/" + pattern + "/", "/");
		}
		return originalServletPath;
	}

	protected boolean urlPatternChecker(final String urlToBeChecked, final String pattern)
	{
		boolean containsPattern = StringUtils.contains(urlToBeChecked, "/" + pattern + "/");
		if (!containsPattern)
		{
			final String[] splitUrl = urlToBeChecked.split("/");
			final String last = splitUrl[splitUrl.length - 1];
			if (last.equalsIgnoreCase(pattern))
			{
				containsPattern = true;
			}
		}
		return containsPattern;
	}
}
