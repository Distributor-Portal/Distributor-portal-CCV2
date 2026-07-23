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

import java.util.Set;
import java.util.function.Predicate;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.PathMatcher;


/**
 * Custom request matcher that returns false for urls that match the urls found in the excludeUrlSet.
 * All other urls will return true.
 */
public class ExcludeUrlRequestMatcher implements RequestMatcher
{
	private Set<String> excludeUrlSet;
	private PathMatcher pathMatcher;

	@Override
	public boolean matches(final HttpServletRequest request)
	{
		// Do not match patterns specified in the excludeUrlSet to the servletPath
		return !this.excludeUrlSet.stream().anyMatch(excludeUrl -> pathMatcher.match((String) excludeUrl, request.getServletPath()));
	}

	protected Set<String> getExcludeUrlSet()
	{
		return excludeUrlSet;
	}

	public void setExcludeUrlSet(final Set<String> excludeUrlSet)
	{
		// Ensure only valid urls are added to the excludeUrlSet
		excludeUrlSet.removeIf(object -> !(object != null) && (object instanceof String) && ((String) object).startsWith("/"));

		this.excludeUrlSet = excludeUrlSet;
	}

	protected PathMatcher getPathMatcher()
	{
		return pathMatcher;
	}

	public void setPathMatcher(final PathMatcher pathMatcher)
	{
		this.pathMatcher = pathMatcher;
	}
}