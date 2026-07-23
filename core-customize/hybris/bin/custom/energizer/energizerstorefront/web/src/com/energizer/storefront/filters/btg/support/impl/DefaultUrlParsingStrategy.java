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
package com.energizer.storefront.filters.btg.support.impl;

import com.energizer.storefront.filters.btg.support.UrlParsingStrategy;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.web.util.UrlPathHelper;


/**
 * Default implementation of {@link UrlParsingStrategy}
 */
public class DefaultUrlParsingStrategy implements UrlParsingStrategy, InitializingBean
{
	private String regex;
	private Pattern pattern;
	private UrlPathHelper urlPathHelper;

	/**
	 * @param regex
	 *           the regex to set
	 */
	public void setRegex(final String regex)
	{
		Assert.hasLength(regex, "must have length; it must not be null or empty");
		this.regex = regex;
	}

	/**
	 * @param urlPathHelper
	 *           the urlPathHelper to set
	 */
	public void setUrlPathHelper(final UrlPathHelper urlPathHelper)
	{
		this.urlPathHelper = urlPathHelper;
	}

	@Override
	public void afterPropertiesSet()
	{
		if (urlPathHelper == null)
		{
			urlPathHelper = new UrlPathHelper();
		}
		Assert.hasLength(regex, "must have length; it must not be null or empty");
		pattern = Pattern.compile(regex);
	}

	@Override
	public String parse(final HttpServletRequest request)
	{
		Assert.notNull(request, "must not be null");
		String result = null;
		final String path = urlPathHelper.getPathWithinApplication(request);
		if (!StringUtils.isBlank(path))
		{
			final Matcher matcher = pattern.matcher(path);
			if (matcher.find())
			{
				result = matcher.group(1);
			}
		}
		return result;
	}
}
