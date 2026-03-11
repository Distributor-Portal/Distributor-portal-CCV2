package com.energizer.core.services.email.context;


import de.hybris.platform.acceleratorservices.model.cms2.pages.EmailPageModel;
import de.hybris.platform.acceleratorservices.urlencoder.UrlEncoderService;
import de.hybris.platform.acceleratorservices.urlresolver.SiteBaseUrlResolutionService;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.cms2.servicelayer.services.CMSSiteService;
import de.hybris.platform.commercefacades.storesession.StoreSessionFacade;
import de.hybris.platform.commerceservices.customer.CustomerEmailResolutionService;
import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;

import java.util.Locale;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.springframework.beans.factory.annotation.Required;


/**
 * The email velocity context.
 */
public class EnergizerGenericEmailContext extends VelocityContext
{
	private static final Logger LOG = Logger.getLogger(EnergizerGenericEmailContext.class);

	public static final String BASE_URL = "baseUrl";
	public static final String SECURE_BASE_URL = "secureBaseUrl";
	public static final String MEDIA_BASE_URL = "mediaBaseUrl";
	public static final String MEDIA_SECURE_BASE_URL = "mediaSecureBaseUrl";
	public static final String TITLE = "title";
	public static final String DISPLAY_NAME = "displayName";
	public static final String EMAIL = "email";
	public static final String FROM_EMAIL = "fromEmail";
	public static final String FROM_DISPLAY_NAME = "fromDisplayName";
	public static final String THEME = "theme";
	public static final String BASE_SITE = "baseSite";
	public static final String BASE_THEME_URL = "baseThemeUrl";
	public static final String EMAIL_LANGUAGE = "email_language";


	private Map<String, String> cmsSlotContents;
	private Map<String, Object> messages;
	private String urlEncodingAttributes;

	private SiteBaseUrlResolutionService siteBaseUrlResolutionService;
	private CustomerEmailResolutionService customerEmailResolutionService;
	private ConfigurationService configurationService;
	private UrlEncoderService urlEncoderService;

	@Resource
	CMSSiteService cmsSiteService;

	@Resource(name = "storeSessionFacade")
	private StoreSessionFacade storeSessionFacade;

	protected UrlEncoderService getUrlEncoderService()
	{
		return urlEncoderService;
	}

	@Required
	public void setUrlEncoderService(final UrlEncoderService urlEncoderService)
	{
		this.urlEncoderService = urlEncoderService;
	}

	public String getUrlEncodingAttributes()
	{
		return urlEncodingAttributes;
	}

	public void setUrlEncodingAttributes(final String urlEncodingAttributes)
	{
		this.urlEncodingAttributes = urlEncodingAttributes;
	}

	protected SiteBaseUrlResolutionService getSiteBaseUrlResolutionService()
	{
		return siteBaseUrlResolutionService;
	}

	@Required
	public void setSiteBaseUrlResolutionService(final SiteBaseUrlResolutionService siteBaseUrlResolutionService)
	{
		this.siteBaseUrlResolutionService = siteBaseUrlResolutionService;
	}

	protected CustomerEmailResolutionService getCustomerEmailResolutionService()
	{
		return customerEmailResolutionService;
	}

	@Required
	public void setCustomerEmailResolutionService(final CustomerEmailResolutionService customerEmailResolutionService)
	{
		this.customerEmailResolutionService = customerEmailResolutionService;
	}

	protected ConfigurationService getConfigurationService()
	{
		return configurationService;
	}

	@Required
	public void setConfigurationService(final ConfigurationService configurationService)
	{
		this.configurationService = configurationService;
	}

	public Map<String, String> getCmsSlotContents()
	{
		return cmsSlotContents;
	}

	public void setCmsSlotContents(final Map<String, String> cmsSlotContents)
	{
		this.cmsSlotContents = cmsSlotContents;
	}

	protected CMSSiteService getCmsSiteService()
	{
		return cmsSiteService;
	}

	public void setCmsSiteService(final CMSSiteService cmsSiteService)
	{
		this.cmsSiteService = cmsSiteService;
	}

	public Map<String, Object> getMessages()
	{
		return messages;
	}

	public void setMessages(final Map<String, Object> messages)
	{
		this.messages = messages;
	}

	public String getBaseUrl()
	{
		return (String) get(BASE_URL);
	}

	public String getBaseThemeUrl()
	{
		return (String) get(BASE_THEME_URL);
	}

	public String getSecureBaseUrl()
	{
		return (String) get(SECURE_BASE_URL);
	}

	public String getMediaBaseUrl()
	{
		return (String) get(MEDIA_BASE_URL);
	}

	public String getMediaSecureBaseUrl()
	{
		return (String) get(MEDIA_SECURE_BASE_URL);
	}

	public String getTheme()
	{
		return (String) get(THEME);
	}

	public String getTitle()
	{
		return (String) get(TITLE);
	}

	public String getDisplayName()
	{
		return (String) get(DISPLAY_NAME);
	}

	public String getEmail()
	{
		return (String) get(EMAIL);
	}

	public String getToEmail()
	{
		return getEmail();
	}

	public String getToDisplayName()
	{
		return getDisplayName();
	}

	public String getFromEmail()
	{
		return (String) get(FROM_EMAIL);
	}

	public String getFromDisplayName()
	{
		return (String) get(FROM_DISPLAY_NAME);
	}

	public BaseSiteModel getBaseSite()
	{
		return (BaseSiteModel) get(BASE_SITE);
	}

	/**
	 * @return the storeSessionFacade
	 */
	public StoreSessionFacade getStoreSessionFacade()
	{
		return storeSessionFacade;
	}

	/**
	 * @param storeSessionFacade
	 *           the storeSessionFacade to set
	 */
	public void setStoreSessionFacade(final StoreSessionFacade storeSessionFacade)
	{
		this.storeSessionFacade = storeSessionFacade;
	}

	public void init(final EmailPageModel emailPageModel, final LanguageModel language, final Map<String, Object> contextMap)
	{
		BaseSiteModel baseSite = null;
		if (contextMap != null)
		{
			for (final Map.Entry<String, Object> entry : contextMap.entrySet())
			{
				if (entry.getKey().equalsIgnoreCase("baseSite"))
				{
					baseSite = (BaseSiteModel) entry.getValue();
				}
				else
				{
					put(entry.getKey(), entry.getValue());
				}
			}

		}
		if (baseSite == null)
		{
			LOG.error("Failed to lookup Site for BusinessProcess ");
		}
		else
		{
			put(BASE_SITE, baseSite);
			setUrlEncodingAttributes(baseSite.getUid());
			final SiteBaseUrlResolutionService siteBaseUrlResolutionService = getSiteBaseUrlResolutionService();
			// Lookup the site specific URLs
			put(BASE_URL, siteBaseUrlResolutionService.getWebsiteUrlForSite(baseSite, "", false, ""));
			put(BASE_THEME_URL, siteBaseUrlResolutionService.getWebsiteUrlForSite(baseSite, false, ""));
			put(SECURE_BASE_URL, siteBaseUrlResolutionService.getWebsiteUrlForSite(baseSite, getUrlEncodingAttributes(), true, ""));
			put(MEDIA_BASE_URL, siteBaseUrlResolutionService.getMediaUrlForSite(baseSite, false));
			put(MEDIA_SECURE_BASE_URL, siteBaseUrlResolutionService.getMediaUrlForSite(baseSite, true));

			put(THEME, baseSite.getTheme() != null ? baseSite.getTheme().getCode() : null);
			put("env", configurationService.getConfiguration().getString("mail.enviorment"));
		}
		put(FROM_EMAIL, emailPageModel.getFromEmail());

		if (language != null)
		{
			put(EMAIL_LANGUAGE, language);
			String fromName = emailPageModel.getFromName(new Locale(language.getIsocode()));
			if (fromName == null)
			{
				fromName = emailPageModel.getFromName();
			}
			put(FROM_DISPLAY_NAME, fromName);
		}
		else
		{
			put(FROM_DISPLAY_NAME, emailPageModel.getFromName());
		}

	}

}
