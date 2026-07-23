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
package com.energizer.storefront.interceptors.beforeview;

import de.hybris.platform.acceleratorservices.config.SiteConfigService;
import com.energizer.storefront.interceptors.BeforeViewHandler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;

/**
 */
public class ConfigGranuleBeforeViewHandler implements BeforeViewHandler
{
	private SiteConfigService siteConfigService;

	protected SiteConfigService getSiteConfigService()
	{
		return siteConfigService;
	}

	public void setSiteConfigService(final SiteConfigService siteConfigService)
	{
		this.siteConfigService = siteConfigService;
	}

	@Override
	public void beforeView(final HttpServletRequest request, final HttpServletResponse response, final ModelAndView modelAndView) throws Exception
	{
		modelAndView.addObject("granuleEnabled", Boolean.valueOf(getSiteConfigService().getBoolean("storefront.granule.enabled", false)));
	}
}
