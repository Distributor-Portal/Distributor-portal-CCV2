/**
 *
 */
package com.energizer.core.services.email.context;

import de.hybris.platform.acceleratorservices.model.cms2.pages.EmailPageModel;
import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import javax.annotation.Resource;


/**
 * @author M1023278
 *
 */
public class EnergizerPasswordExpiryEmailContext extends EnergizerGenericEmailContext
{

	@Resource(name = "configurationService")
	private ConfigurationService configurationService;

	/**
	 * @param emailPageModel
	 * @param language
	 * @param contextMap
	 */
	@Override
	public void init(final EmailPageModel emailPageModel, final LanguageModel language, final Map<String, Object> contextMap)
	{

		super.init(emailPageModel, language, contextMap);

	}

	public String getSecureResetPasswordUrl() throws UnsupportedEncodingException
	{
		return getSiteBaseUrlResolutionService().getWebsiteUrlForSite(getBaseSite(), getUrlEncodingAttributes(), true,
				"/login/pw/request-page");
	}

	public String getDisplaySecureResetPasswordUrl() throws UnsupportedEncodingException
	{
		return getSiteBaseUrlResolutionService().getWebsiteUrlForSite(getBaseSite(), getUrlEncodingAttributes(), true,
				"/login/pw/request-page");
	}

}
