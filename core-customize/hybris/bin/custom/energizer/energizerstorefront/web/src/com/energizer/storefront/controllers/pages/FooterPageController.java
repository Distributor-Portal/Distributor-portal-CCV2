/**
 *
 */
package com.energizer.storefront.controllers.pages;

import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.cms2.exceptions.CMSItemNotFoundException;
import de.hybris.platform.cms2.model.contents.components.CMSLinkComponentModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.energizer.storefront.breadcrumb.impl.DefaultResourceBreadcrumbBuilder;
import com.energizer.storefront.controllers.ControllerConstants;


/**
 * @author Somasundaram Jayaraman
 *
 *         This file is introduced for footer content pages. Based on the url/footer link,the following footer page will
 *         be displayed with all the content.
 *
 */
@Controller
@Scope("tenant")
@RequestMapping("/content")
public class FooterPageController extends AbstractPageController
{

	// CMS Page
	private static final String FOOTER_CMS_PAGE = "footerHomePage";
	private static final Logger LOG = Logger.getLogger(FooterPageController.class);

	// Breadcrumb
	@Resource(name = "footerContentPageBreadcrumbBuilder")
	private DefaultResourceBreadcrumbBuilder footerContentPageBreadcrumbBuilder;

	@Resource(name = "configurationService")
	private ConfigurationService configService;

	@Resource(name = "flexibleSearchService")
	private FlexibleSearchService flexibleSearchService;

	/**
	 * @return the footerContentPageBreadcrumbBuilder
	 */
	public DefaultResourceBreadcrumbBuilder getFooterContentPageBreadcrumbBuilder()
	{
		return footerContentPageBreadcrumbBuilder;
	}

	/**
	 * @param footerContentPageBreadcrumbBuilder
	 *           the footerContentPageBreadcrumbBuilder to set
	 */
	public void setFooterContentPageBreadcrumbBuilder(final DefaultResourceBreadcrumbBuilder footerContentPageBreadcrumbBuilder)
	{
		this.footerContentPageBreadcrumbBuilder = footerContentPageBreadcrumbBuilder;
	}

	/**
	 * @return the configService
	 */
	public ConfigurationService getConfigService()
	{
		return configService;
	}

	/**
	 * @param configService
	 *           the configService to set
	 */
	public void setConfigService(final ConfigurationService configService)
	{
		this.configService = configService;
	}

	@RequestMapping(value = "/{pageId}", method = RequestMethod.GET)
	public String footerContentPage(final Model model, @PathVariable
	final String pageId) throws CMSItemNotFoundException
	{
		LOG.info("**********  Entering footerContentPage method  ***********");

		storeCmsPageInModel(model, getContentPageForLabelOrId(FOOTER_CMS_PAGE));
		setUpMetaDataForContentPage(model, getContentPageForLabelOrId(FOOTER_CMS_PAGE));
		try
		{
			if (null != pageId)
			{
				model.addAttribute("currentFooterPage", pageId);
				footerContentPageBreadcrumbBuilder.setParentBreadcrumbLinkPath("/content/" + pageId);
				footerContentPageBreadcrumbBuilder.setParentBreadcrumbResourceKey(getConfigValue("header.link." + pageId));

			}
			else
			{
				throw new Exception("Invalid URL. Please try the correct one.");
			}

			final String footerLinksFromConfig = getConfigValue("footer.content.page.links");
			final List<String> footerLinksListFromConfig = Arrays.asList(footerLinksFromConfig.split(","));
			final String aboutUsURL = getConfigValue("footer.aboutus.url");
			final String legalURL = getConfigValue("footer.legal.url");
			//final String faqURL = getConfigValue("footer.faq.url");

			final List<String> visibleFooterLinks = new ArrayList<String>();

			/*
			 * if (null != footerLinksListFromConfig && !footerLinksListFromConfig.isEmpty()) { List<CMSLinkComponentModel>
			 * footerCMSLinkComponents = new ArrayList<CMSLinkComponentModel>();
			 *
			 * footerCMSLinkComponents = getFooterCMSLinkComponents(footerCMSLinkComponents, footerLinksListFromConfig);
			 *
			 * for (final CMSLinkComponentModel linkComp : footerCMSLinkComponents) { LOG.info("URL for " + linkComp +
			 * " is ==> " + linkComp.getUrl()); LOG.info("link ====>  /content/" + linkComp.getUrl().split("/")[2]);
			 * visibleFooterLinks.add(linkComp.getUrl().split("/")[2]); }
			 *
			 * LOG.info("*********  There are " + visibleFooterLinks.size() + " footer link pages *********  "); }
			 */

			model.addAttribute("breadcrumbs", footerContentPageBreadcrumbBuilder.getBreadcrumbs(null));
			model.addAttribute("metaRobots", "no-index,no-follow");
			model.addAttribute("footerLinksList", footerLinksListFromConfig);
			model.addAttribute("aboutUsURL", aboutUsURL);
			model.addAttribute("legalURL", legalURL);
			//model.addAttribute("faqURL", faqURL);

			LOG.info("*******  Redirecting to " + getConfigValue("header.link." + pageId) + " page  ********  ");
			LOG.info("**********  Exiting footerContentPage method  ***********");
		}
		catch (final Exception ex)
		{
			LOG.error("Error occured : " + ex.getMessage());
		}
		return ControllerConstants.Views.Pages.footer.FooterHomePage;
	}

	@Override
	public String getConfigValue(final String key)
	{
		return configService.getConfiguration().getString(key);
	}

	public List<CMSLinkComponentModel> getFooterCMSLinkComponents(List<CMSLinkComponentModel> footerCMSLinkComponents,
			final List<String> footerLinksListFromConfig)
	{
		final CatalogVersionModel catalogVersion = super.getCmsSiteService().getCurrentCatalogVersion();

		String urlParameters = "";

		for (int i = 0; i < footerLinksListFromConfig.size(); i++)
		{
			if (i + 1 == footerLinksListFromConfig.size())
			{
				urlParameters = urlParameters + "\'/content/" + footerLinksListFromConfig.get(i) + "\' ";
			}
			else
			{
				urlParameters = urlParameters + "\'/content/" + footerLinksListFromConfig.get(i) + "\' , ";
			}
		}
		LOG.info("urlParameters ::: " + urlParameters);

		LOG.info("Current Catalog Version :::  " + catalogVersion + " and it is  \'" + catalogVersion.getActive() + "\'");

		/*
		 * final String queryString = "SELECT {" + CMSLinkComponentModel.PK + "} FROM {" + CMSLinkComponentModel._TYPECODE
		 * + "!} WHERE {" + CMSLinkComponentModel.URL + "} IN (" + urlParameters + ") AND {" +
		 * CMSLinkComponentModel.CATALOGVERSION + "}=?catalogVersion AND {" + CMSLinkComponentModel.VISIBLE +
		 * "}=?isVisible";
		 */

		/*
		 * final String queryString = "SELECT {" + CMSLinkComponentModel.PK + "} FROM {" + CMSLinkComponentModel._TYPECODE
		 * + "! INNER JOIN " + CatalogVersionModel._TYPECODE + " ON {" + CMSLinkComponentModel.CATALOGVERSION + "} = {" +
		 * CatalogVersionModel.PK + "}} WHERE {" + CMSLinkComponentModel.URL + "} LIKE '%content%' AND {" +
		 * CMSLinkComponentModel.VISIBLE + "}=?isVisible AND {" + CatalogVersionModel.VERSION + "}=?catalogVersion";
		 */


		final String queryString = "SELECT * FROM { CMSLinkComponent! INNER JOIN CatalogVersion ON {CMSLinkComponent.catalogVersion} = {CatalogVersion.pk} } WHERE {CMSLinkComponent.url} LIKE '%content%' AND {CMSLinkComponent.visible}=1 AND {CatalogVersion.version}='Online'";

		LOG.info("queryString ::: " + queryString);

		final FlexibleSearchQuery flexibleSearchQuery = new FlexibleSearchQuery(queryString);


		flexibleSearchQuery.addQueryParameter("catalogVersion", getConfigValue("version"));
		flexibleSearchQuery.addQueryParameter("isVisible", Boolean.TRUE);

		LOG.info(flexibleSearchQuery);

		footerCMSLinkComponents = flexibleSearchService.<CMSLinkComponentModel> search(flexibleSearchQuery).getResult();

		if (null != footerCMSLinkComponents && !footerCMSLinkComponents.isEmpty())
		{
			LOG.info("footerCMSLinkComponents.size () ::: " + footerCMSLinkComponents.size());
		}

		Collections.unmodifiableList(footerCMSLinkComponents);

		return footerCMSLinkComponents;
	}
}
