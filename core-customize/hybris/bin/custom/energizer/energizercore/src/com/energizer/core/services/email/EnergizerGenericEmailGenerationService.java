package com.energizer.core.services.email;

import de.hybris.platform.acceleratorservices.email.CMSEmailPageService;
import de.hybris.platform.acceleratorservices.email.EmailService;
import de.hybris.platform.acceleratorservices.model.cms2.pages.EmailPageModel;
import de.hybris.platform.acceleratorservices.model.cms2.pages.EmailPageTemplateModel;
import de.hybris.platform.acceleratorservices.model.email.EmailAddressModel;
import de.hybris.platform.acceleratorservices.model.email.EmailMessageModel;
import de.hybris.platform.acceleratorservices.process.strategies.EmailTemplateTranslationStrategy;
import de.hybris.platform.acceleratorservices.urlresolver.SiteBaseUrlResolutionService;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.catalog.CatalogService;
import de.hybris.platform.catalog.CatalogVersionService;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.cms2.model.contents.ContentCatalogModel;
import de.hybris.platform.cms2.model.contents.components.AbstractCMSComponentModel;
import de.hybris.platform.cms2.model.contents.contentslot.ContentSlotModel;
import de.hybris.platform.cms2.model.site.CMSSiteModel;
import de.hybris.platform.cms2.servicelayer.data.ContentSlotData;
import de.hybris.platform.cms2.servicelayer.services.CMSComponentService;
import de.hybris.platform.cms2.servicelayer.services.CMSPageService;
import de.hybris.platform.commons.model.renderer.RendererTemplateModel;
import de.hybris.platform.commons.renderer.RendererService;
import de.hybris.platform.commons.renderer.daos.RendererTemplateDao;
import de.hybris.platform.core.Registry;
import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.core.model.type.ComposedTypeModel;
import de.hybris.platform.servicelayer.exceptions.AttributeNotSupportedException;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.type.TypeService;
import de.hybris.platform.servicelayer.util.ServicesUtil;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import javax.annotation.Resource;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;

import com.energizer.core.services.email.context.EnergizerGenericEmailContext;


public class EnergizerGenericEmailGenerationService
{
	private static final Logger LOG = Logger.getLogger(EnergizerGenericEmailGenerationService.class);

	@Resource
	private EmailService emailService;

	@Resource
	CatalogVersionService catalogVersionService;

	@Resource
	CatalogService catalogService;

	@Resource
	private RendererService rendererService;

	@Resource
	CMSEmailPageService cmsEmailPageService;

	@Resource
	private EmailTemplateTranslationStrategy emailTemplateTranslationStrategy;

	private Map<String, String> emailContextVariables;

	@Resource
	private CMSPageService cmsPageService;

	@Resource
	private TypeService typeService;

	@Resource
	private CMSComponentService cmsComponentService;

	@Resource
	private RendererTemplateDao rendererTemplateDao;

	@Resource
	private ModelService modelService;

	@Resource
	private SiteBaseUrlResolutionService siteBaseUrlResolutionService;

	private String frontendTemplateName;

	public void generateAndSendEmail(final String frontendTemplateName, final List<EmailAddressModel> toAddress,
			final EmailAddressModel fromAddress, final List<EmailAddressModel> ccAddress, final LanguageModel language,
			final Map<String, Object> contextMap)
	{
		this.frontendTemplateName = frontendTemplateName;
		final CatalogVersionModel contentCatalogVersion = null;
		ContentCatalogModel contentCatalog = null;
		CMSSiteModel baseSite = null;
		if (contextMap != null)
		{
			for (final Map.Entry<String, Object> entry : contextMap.entrySet())
			{
				if (entry.getKey().equalsIgnoreCase("baseSite"))
				{
					baseSite = (CMSSiteModel) entry.getValue();
				}
			}
		}
		if (null != baseSite)
		{
			contentCatalog = baseSite.getContentCatalogs().get(0);
		}
		final EmailMessageModel emailMessageModel = generateEmail(
				catalogService.getCatalogVersion(contentCatalog.getId(), "Online"), toAddress, fromAddress, ccAddress, language,
				contextMap);
		getEmailService().send(emailMessageModel);
	}

	private EmailMessageModel generateEmail(final CatalogVersionModel contentCatalogVersion,
			final List<EmailAddressModel> toAddreaa, final EmailAddressModel fromAddress, final List<EmailAddressModel> ccAddress,
			final LanguageModel language, final Map<String, Object> contextMap)
	{
		final List<CatalogVersionModel> catalogs = new ArrayList<CatalogVersionModel>();
		EnergizerGenericEmailContext emailContext = null;
		catalogs.add(contentCatalogVersion);
		catalogVersionService.setSessionCatalogVersions(catalogs);

		final EmailPageModel emailPageModel = getCmsEmailPageService().getEmailPageForFrontendTemplate(getFrontendTemplateName(),
				contentCatalogVersion);

		ServicesUtil.validateParameterNotNull(emailPageModel, "EmailPageModel cannot be null");
		Assert.isInstanceOf(EmailPageTemplateModel.class, emailPageModel.getMasterTemplate(),
				"MasterTemplate associated with EmailPageModel should be EmailPageTemplate");

		final EmailPageTemplateModel emailPageTemplateModel = (EmailPageTemplateModel) emailPageModel.getMasterTemplate();
		final RendererTemplateModel bodyRenderTemplate = emailPageTemplateModel.getHtmlTemplate();
		Assert.notNull(bodyRenderTemplate, "HtmlTemplate associated with MasterTemplate of EmailPageModel cannot be null");
		final RendererTemplateModel subjectRenderTemplate = emailPageTemplateModel.getSubject();
		Assert.notNull(subjectRenderTemplate, "Subject associated with MasterTemplate of EmailPageModel cannot be null");

		final EmailMessageModel emailMessageModel;

		emailContext = create(emailPageModel, bodyRenderTemplate, language, contextMap);

		if (emailContext == null)
		{
			LOG.error("Failed to create email context for businessProcess ");
			throw new RuntimeException("Failed to create email context for businessProcess ");
		}
		else
		{
			final StringWriter subject = new StringWriter();
			getRendererService().render(subjectRenderTemplate, emailContext, subject);

			final StringWriter body = new StringWriter();
			getRendererService().render(bodyRenderTemplate, emailContext, body);

			emailMessageModel = getEmailService().createEmailMessage(toAddreaa, new ArrayList<EmailAddressModel>(),
					new ArrayList<EmailAddressModel>(), fromAddress, null, subject.toString(), body.toString(), null);

			if (LOG.isDebugEnabled())
			{
				LOG.debug("Email Subject: " + emailMessageModel.getSubject());
				LOG.debug("Email Body: " + emailMessageModel.getBody());
			}
		}

		return emailMessageModel;

	}



	protected boolean validate(final EnergizerGenericEmailContext emailContext)
	{
		boolean valid = true;
		if (StringUtils.isBlank(emailContext.getToEmail()))
		{
			LOG.error("Missing ToEmail in AbstractEmailContext");
			valid = false;
		}

		if (StringUtils.isBlank(emailContext.getFromEmail()))
		{
			LOG.error("Missing FromEmail in AbstractEmailContext");
			valid = false;
		}
		return valid;
	}

	protected EmailMessageModel createEmailMessage(final String emailSubject, final String emailBody,
			final EnergizerGenericEmailContext emailContext)
	{
		final List<EmailAddressModel> toEmails = new ArrayList<EmailAddressModel>();
		final EmailAddressModel toAddress = getEmailService().getOrCreateEmailAddressForEmail("surendra_na@mindtree.com",
				"surendra");
		toEmails.add(toAddress);
		final EmailAddressModel fromAddress = getEmailService().getOrCreateEmailAddressForEmail(emailContext.getFromEmail(),
				emailContext.getFromDisplayName());
		return getEmailService().createEmailMessage(toEmails, new ArrayList<EmailAddressModel>(),
				new ArrayList<EmailAddressModel>(), fromAddress, emailContext.getFromEmail(), emailSubject, emailBody, null);
	}

	/**
	 * @param emailPageModel
	 * @param renderTemplate
	 * @param language
	 * @param contextMap
	 * @return
	 * @throws RuntimeException
	 */
	public EnergizerGenericEmailContext create(final EmailPageModel emailPageModel, final RendererTemplateModel renderTemplate,
			final LanguageModel language, final Map<String, Object> contextMap) throws RuntimeException
	{
		final EnergizerGenericEmailContext emailContext = resolveEmailContext(renderTemplate);
		emailContext.init(emailPageModel, language, contextMap);
		renderCMSSlotsIntoEmailContext(emailContext, emailPageModel);

		// parse and populate the variable at the end
		parseVariablesIntoEmailContext(emailContext);

		emailContext.setMessages(getEmailTemplateTranslationStrategy().translateMessagesForTemplate(renderTemplate,
				language.getIsocode()));

		return emailContext;
	}

	/**
	 * @param emailContext
	 */
	protected void parseVariablesIntoEmailContext(final EnergizerGenericEmailContext emailContext)
	{
		final Map<String, String> variables = getEmailContextVariables();
		if (variables != null)
		{
			for (final Map.Entry<String, String> entry : variables.entrySet())
			{
				final StringBuilder buffer = new StringBuilder();

				final StringTokenizer tokenizer = new StringTokenizer(entry.getValue(), "{}");
				while (tokenizer.hasMoreElements())
				{
					final String token = tokenizer.nextToken();
					if (emailContext.containsKey(token))
					{
						final Object tokenValue = emailContext.get(token);
						if (tokenValue != null)
						{
							buffer.append(tokenValue.toString());
						}
					}
					else
					{
						buffer.append(token);
					}
				}

				emailContext.put(entry.getKey(), buffer.toString());
			}
		}
	}

	/**
	 * @param emailContext
	 * @param emailPageModel
	 */
	protected void renderCMSSlotsIntoEmailContext(final EnergizerGenericEmailContext emailContext,
			final EmailPageModel emailPageModel)
	{
		final Map<String, String> cmsSlotContents = new HashMap<String, String>();

		for (final ContentSlotData contentSlotData : getCmsPageService().getContentSlotsForPage(emailPageModel))
		{
			if (LOG.isDebugEnabled())
			{
				LOG.debug("Starting to prodess Content Slot: " + contentSlotData.getName() + "...");
			}

			final String contentPosition = contentSlotData.getPosition();
			final String renderedComponent = renderComponents(contentSlotData.getContentSlot(), emailContext);
			cmsSlotContents.put(contentPosition, renderedComponent);

			if (LOG.isDebugEnabled())
			{
				LOG.debug("Content Slot Position: " + contentPosition);
				LOG.debug("Renedered Component: " + renderedComponent);

				LOG.debug("Finished Processing Content Slot: " + contentSlotData.getName());
			}
		}
		emailContext.setCmsSlotContents(cmsSlotContents);
	}

	protected String renderComponents(final ContentSlotModel contentSlotModel, final EnergizerGenericEmailContext emailContext)
	{
		final StringWriter text = new StringWriter();
		for (final AbstractCMSComponentModel component : contentSlotModel.getCmsComponents())
		{
			final ComposedTypeModel componentType = getTypeService().getComposedTypeForClass(component.getClass());
			if (Boolean.TRUE.equals(component.getVisible())
					&& !getCmsComponentService().isComponentContainer(componentType.getCode()))
			{
				final BaseSiteModel baseSite = emailContext.getBaseSite();
				final String renderTemplateCode = resolveRendererTemplateForComponent(component, baseSite);
				final List<RendererTemplateModel> results = getRendererTemplateDao().findRendererTemplatesByCode(renderTemplateCode);
				final RendererTemplateModel renderTemplate = results.isEmpty() ? null : results.get(0);


				if (renderTemplate != null)
				{
					if (LOG.isDebugEnabled())
					{
						LOG.debug("Using Render Template Code: " + renderTemplateCode);
					}

					final Map<String, Object> componentContext = new HashMap<String, Object>();
					componentContext.put("parentContext", emailContext);
					for (final String property : getCmsComponentService().getEditorProperties(component))
					{
						try
						{
							final Object value = modelService.getAttributeValue(component, property);
							componentContext.put(property, value);
						}
						catch (final AttributeNotSupportedException ignore)
						{
							// ignore
						}
					}
					// insert services for usage at jsp/vm page
					componentContext.put("urlResolutionService", getSiteBaseUrlResolutionService());
					// insert cms site
					componentContext.put("site", baseSite);

					if (LOG.isDebugEnabled())
					{

						for (final Entry<String, Object> entry : componentContext.entrySet())
						{
							LOG.debug("Render template Context Data: " + entry.getKey() + "=" + entry.getValue());
						}
						final Object[] keys = emailContext.getKeys();
						if (keys != null)
						{
							for (final Object key : keys)
							{
								LOG.debug("Parent render template Context Data: " + key + "=" + emailContext.get(String.valueOf(key)));
							}
						}
					}

					getRendererService().render(renderTemplate, componentContext, text);
				}
				else
				{
					// Component won't get rendered in the emails.
					final String siteName = baseSite == null ? null : baseSite.getUid();

					LOG.error("Couldn't find render template for component [" + component.getUid() + "] of type ["
							+ componentType.getCode() + "] in slot [" + contentSlotModel.getUid() + "] for site [" + siteName
							+ "] during process Tried code [" + renderTemplateCode + "]");
				}
			}
		}
		return text.toString();
	}

	protected String resolveRendererTemplateForComponent(final AbstractCMSComponentModel component, final BaseSiteModel Site)
	{
		final BaseSiteModel baseSite = Site;
		final ComposedTypeModel componentType = getTypeService().getComposedTypeForClass(component.getClass());
		return (baseSite != null ? baseSite.getUid() : "") + "-" + componentType.getCode() + "-template";
	}

	public <T extends EnergizerGenericEmailContext> T resolveEmailContext(final RendererTemplateModel renderTemplate)
			throws RuntimeException
	{
		try
		{
			@SuppressWarnings("unchecked")
			final Class<T> contextClass = (Class<T>) Class.forName(renderTemplate.getContextClass());
			final Map<String, T> emailContexts = getApplicationContext().getBeansOfType(contextClass);
			if (MapUtils.isNotEmpty(emailContexts))
			{
				return emailContexts.entrySet().iterator().next().getValue();
			}

			else
			{
				throw new RuntimeException("Cannot find bean in application context for context class [" + contextClass + "]");
			}


		}
		catch (final ClassNotFoundException e)
		{
			LOG.error("failed to create email context", e);
			throw new RuntimeException("Cannot find email context class", e);

		}
	}

	protected RendererService getRendererService()
	{
		return rendererService;
	}

	public void setRendererService(final RendererService rendererService)
	{
		this.rendererService = rendererService;
	}

	protected ApplicationContext getApplicationContext()
	{
		return Registry.getApplicationContext();
	}

	protected EmailService getEmailService()
	{
		return emailService;
	}

	public void setEmailService(final EmailService emailService)
	{
		this.emailService = emailService;
	}

	public CatalogVersionService getCatalogVersionService()
	{
		return catalogVersionService;
	}

	public void setCatalogVersionService(final CatalogVersionService catalogVersionService)
	{
		this.catalogVersionService = catalogVersionService;
	}

	public CMSEmailPageService getCmsEmailPageService()
	{
		return cmsEmailPageService;
	}

	public void setCmsEmailPageService(final CMSEmailPageService cmsEmailPageService)
	{
		this.cmsEmailPageService = cmsEmailPageService;
	}

	protected EmailTemplateTranslationStrategy getEmailTemplateTranslationStrategy()
	{
		return emailTemplateTranslationStrategy;
	}

	public void setEmailTemplateTranslationStrategy(final EmailTemplateTranslationStrategy emailTemplateTranslationStrategy)
	{
		this.emailTemplateTranslationStrategy = emailTemplateTranslationStrategy;
	}

	protected Map<String, String> getEmailContextVariables()
	{
		return emailContextVariables;
	}

	public void setEmailContextVariables(final Map<String, String> emailContextVariables)
	{
		this.emailContextVariables = emailContextVariables;
	}

	protected CMSPageService getCmsPageService()
	{
		return cmsPageService;
	}

	protected TypeService getTypeService()
	{
		return typeService;
	}

	public void setTypeService(final TypeService typeService)
	{
		this.typeService = typeService;
	}

	protected CMSComponentService getCmsComponentService()
	{
		return cmsComponentService;
	}

	public void setCmsComponentService(final CMSComponentService cmsComponentService)
	{
		this.cmsComponentService = cmsComponentService;
	}

	protected SiteBaseUrlResolutionService getSiteBaseUrlResolutionService()
	{
		return siteBaseUrlResolutionService;
	}

	public void setSiteBaseUrlResolutionService(final SiteBaseUrlResolutionService siteBaseUrlResolutionService)
	{
		this.siteBaseUrlResolutionService = siteBaseUrlResolutionService;
	}

	public String getFrontendTemplateName()
	{
		return frontendTemplateName;
	}

	public void setFrontendTemplateName(final String frontendTemplateName)
	{
		this.frontendTemplateName = frontendTemplateName;
	}

	/**
	 * @return the rendererTemplateDao
	 */
	public RendererTemplateDao getRendererTemplateDao()
	{
		return rendererTemplateDao;
	}

	/**
	 * @param rendererTemplateDao
	 *           the rendererTemplateDao to set
	 */
	public void setRendererTemplateDao(final RendererTemplateDao rendererTemplateDao)
	{
		this.rendererTemplateDao = rendererTemplateDao;
	}

}
