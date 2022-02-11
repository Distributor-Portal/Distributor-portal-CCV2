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
package com.energizer.storefront.controllers.pages;

import de.hybris.platform.acceleratorservices.controllers.page.PageType;
import de.hybris.platform.acceleratorservices.customer.CustomerLocationService;
import de.hybris.platform.acceleratorservices.data.RequestContextData;
import de.hybris.platform.category.model.CategoryModel;
import de.hybris.platform.cms2.exceptions.CMSItemNotFoundException;
import de.hybris.platform.cms2.model.pages.CategoryPageModel;
import de.hybris.platform.commercefacades.product.data.CategoryData;
import de.hybris.platform.commercefacades.product.data.ProductData;
import de.hybris.platform.commercefacades.search.ProductSearchFacade;
import de.hybris.platform.commercefacades.search.data.SearchQueryData;
import de.hybris.platform.commercefacades.search.data.SearchStateData;
import de.hybris.platform.commerceservices.category.CommerceCategoryService;
import de.hybris.platform.commerceservices.search.facetdata.BreadcrumbData;
import de.hybris.platform.commerceservices.search.facetdata.ProductCategorySearchPageData;
import de.hybris.platform.commerceservices.search.pagedata.PageableData;
import de.hybris.platform.commerceservices.search.pagedata.SearchPageData;
import de.hybris.platform.commerceservices.url.UrlResolver;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;

import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.energizer.core.solr.query.EnergizerSolrQueryManipulationService;
import com.energizer.storefront.breadcrumb.impl.SearchBreadcrumbBuilder;
import com.energizer.storefront.constants.WebConstants;
import com.energizer.storefront.controllers.ControllerConstants;
import com.energizer.storefront.controllers.util.GlobalMessages;
import com.energizer.storefront.util.MetaSanitizerUtil;
import com.energizer.storefront.util.XSSFilterUtil;


/**
 * Controller for a category page.
 */
@Controller
@Scope("tenant")
@RequestMapping(value = "/**/c")
public class CategoryPageController extends AbstractSearchPageController
{
	protected static final Logger LOG = Logger.getLogger(CategoryPageController.class);

	protected static final String PRODUCT_GRID_PAGE = "category/productGridPage";

	private static final String SITE_PERSONALCAREEMEA = "site.personalCareEMEA";

	/**
	 * We use this suffix pattern because of an issue with Spring 3.1 where a Uri value is incorrectly extracted if it
	 * contains on or more '.' characters. Please see https://jira.springsource.org/browse/SPR-6164 for a discussion on
	 * the issue and future resolution.
	 */
	protected static final String CATEGORY_CODE_PATH_VARIABLE_PATTERN = "/{categoryCode:.*}";

	protected static final String INFINITE_SCROLL = "infiniteScroll";

	@Resource(name = "productSearchFacade")
	private ProductSearchFacade<ProductData> productSearchFacade;

	@Resource(name = "commerceCategoryService")
	private CommerceCategoryService commerceCategoryService;

	@Resource(name = "searchBreadcrumbBuilder")
	private SearchBreadcrumbBuilder searchBreadcrumbBuilder;

	@Resource(name = "categoryModelUrlResolver")
	private UrlResolver<CategoryModel> categoryModelUrlResolver;

	@Resource(name = "customerLocationService")
	private CustomerLocationService customerLocationService;


	@Resource(name = "energizerSolrQueryManipulationService")
	private EnergizerSolrQueryManipulationService energizerSolrQueryManipulationService;

	@Resource(name = "configurationService")
	private ConfigurationService configurationService;

	private static final String ACTIVE_B2BUNIT = "activeB2BUnit";
	private static final String NO_RESULTS_CMS_PAGE_ID = "searchEmpty";


	@RequestMapping(value = CATEGORY_CODE_PATH_VARIABLE_PATTERN, method = RequestMethod.GET)
	public String category(@PathVariable("categoryCode") final String categoryCode,
			@RequestParam(value = "q", required = false) String searchQuery,
			@RequestParam(value = "page", defaultValue = "0") final int page,
			@RequestParam(value = "show", defaultValue = "Page") final ShowMode showMode,
			@RequestParam(value = "sort", required = false) final String sortCode, final Model model,
			final HttpServletRequest request, final HttpServletResponse response)
			throws UnsupportedEncodingException, CMSItemNotFoundException
	{
		final CategoryModel category = commerceCategoryService.getCategoryForCode(categoryCode);

		final String redirection = checkRequestUrl(request, response, categoryModelUrlResolver.resolve(category));
		if (StringUtils.isNotEmpty(redirection))
		{
			return redirection;
		}

		//need to handle the solr query to enforce the b2bunit restriction on the catalog before it proceeds to the OOTB solr search
		searchQuery = energizerSolrQueryManipulationService.getSolrQueryForCategorySearch(sortCode, searchQuery);

		CategoryPageModel categoryPage = getCategoryPage(category);
		try
		{
		final ProductCategorySearchPageData<SearchStateData, ProductData, CategoryData> searchPageData;

		final SearchQueryData searchQueryData = new SearchQueryData();

		final String PERSONALCARE_EMEA = getConfigValue(SITE_PERSONALCAREEMEA);

		if (PERSONALCARE_EMEA.equalsIgnoreCase(this.getCmsSiteService().getCurrentSite().getUid()))
		{
			final String filteredSearchQuery = (XSSFilterUtil.filter(searchQuery)).replaceAll("&#39;", "'");
			searchQueryData.setValue(filteredSearchQuery);
		}
		else
		{
			searchQueryData.setValue(XSSFilterUtil.filter(searchQuery));
		}

		if (searchQueryData.getValue() == null)
		{
			// Direct category link without filtering
			searchPageData = productSearchFacade.categorySearch(categoryCode);
		}
		else
		{
			// We have some search filtering
			if (categoryPage == null || Boolean.FALSE.equals(categoryPage.getDefaultPage()))
			{
				// Load the default category page
				categoryPage = getDefaultCategoryPage();
			}

			final SearchStateData searchState = new SearchStateData();
			searchState.setQuery(searchQueryData);

			final PageableData pageableData = createPageableData(page, 0, sortCode, showMode);
			searchPageData = productSearchFacade.categorySearch(categoryCode, searchState, pageableData);
		}

		storeCmsPageInModel(model, categoryPage);
		storeContinueUrl(request);

		final boolean showCategoriesOnly = searchQueryData.getValue() == null && categoryPage != null
				&& Boolean.FALSE.equals(categoryPage.getDefaultPage()) && searchPageData.getSubCategories() != null
				&& !searchPageData.getSubCategories().isEmpty();

		populateModel(model, searchPageData, showMode);
		model.addAttribute(WebConstants.BREADCRUMBS_KEY, searchBreadcrumbBuilder.getBreadcrumbs(categoryCode, searchPageData));
		model.addAttribute("showCategoriesOnly", Boolean.valueOf(showCategoriesOnly));
		model.addAttribute("pageType", PageType.CATEGORY.name());
		model.addAttribute("userLocation", customerLocationService.getUserLocation());
		model.addAttribute(ACTIVE_B2BUNIT, energizerSolrQueryManipulationService.getB2BUnitForLoggedInUser());

		updatePageTitle(category, searchPageData.getBreadcrumbs(), model);
		final RequestContextData requestContextData = getRequestContextData(request);
		requestContextData.setCategory(category);
		requestContextData.setSearch(searchPageData);

		if (searchQueryData.getValue() != null)
		{
			model.addAttribute("metaRobots", "no-index,follow");
		}

		final String metaKeywords = MetaSanitizerUtil.sanitizeKeywords(category.getKeywords());
		final String metaDescription = MetaSanitizerUtil.sanitizeDescription(category.getDescription());
		setUpMetaData(model, metaKeywords, metaDescription);

		}
		catch (final Exception e)
		{
			// YTODO: handle exception
			LOG.error("Selected category or category products are not available:::");
			e.printStackTrace();
			GlobalMessages.addErrorMessage(model, "category.page.error.message");
			storeCmsPageInModel(model, getContentPageForLabelOrId(NO_RESULTS_CMS_PAGE_ID));
			return getViewForPage(model);
		}
		return getViewPage(categoryPage);
	}

	@RequestMapping(value = "/{categoryCode}/results", method = RequestMethod.GET)
	public String searchResults(@PathVariable("categoryCode") final String categoryCode,
			@RequestParam("q") final String searchQuery, @RequestParam(value = "page", defaultValue = "0") final int page,
			@RequestParam(value = "show", defaultValue = "Page") final ShowMode showMode,
			@RequestParam(value = "sort", required = false) final String sortCode, final Model model)
			throws CMSItemNotFoundException
	{
		try
		{
		final ProductCategorySearchPageData<SearchStateData, ProductData, CategoryData> searchPageData = performSearch(
				categoryCode, searchQuery, page, showMode, sortCode, getSearchPageSize());
		final SearchResultsData<ProductData> searchResultsData = new SearchResultsData<ProductData>();
		searchResultsData.setResults(searchPageData.getResults());
		searchResultsData.setPagination(searchPageData.getPagination());

		model.addAttribute("searchResultsData", searchResultsData);
		}
		catch (final Exception e)
		{
			LOG.error("Category search result data exception:::");
			e.printStackTrace();
			GlobalMessages.addErrorMessage(model, "category.page.error.message");
			storeCmsPageInModel(model, getContentPageForLabelOrId(NO_RESULTS_CMS_PAGE_ID));
			return getViewForPage(model);
		}

		return ControllerConstants.Views.Fragments.Product.ProductLister;
	}

	protected ProductCategorySearchPageData<SearchStateData, ProductData, CategoryData> performSearch(final String categoryCode,
			final String searchQuery, final int page, final ShowMode showMode, final String sortCode, final int pageSize)
	{
		try
		{
		final PageableData pageableData = createPageableData(page, pageSize, sortCode, showMode);
		final SearchStateData searchState = new SearchStateData();
		final SearchQueryData searchQueryData = new SearchQueryData();
		searchQueryData.setValue(searchQuery);
		searchState.setQuery(searchQueryData);

		return productSearchFacade.categorySearch(categoryCode, searchState, pageableData);
		}
		catch (final Exception e)
		{
			LOG.error("We are unable to seach category products:::");
			throw e;
		}
	}

	protected CategoryPageModel getCategoryPage(final CategoryModel category)
	{
		try
		{
			return getCmsPageService().getPageForCategory(category);
		}
		catch (final CMSItemNotFoundException ignore)
		{
			// Ignore
		}
		return null;
	}

	protected CategoryPageModel getDefaultCategoryPage()
	{
		try
		{
			return getCmsPageService().getPageForCategory(null);
		}
		catch (final CMSItemNotFoundException ignore)
		{
			// Ignore
		}
		return null;
	}

	protected <QUERY> void updatePageTitle(final CategoryModel category, final List<BreadcrumbData<QUERY>> appliedFacets,
			final Model model)
	{
		storeContentPageTitleInModel(model, getPageTitleResolver().resolveCategoryPageTitle(category, appliedFacets));
	}

	protected String getViewPage(final CategoryPageModel categoryPage)
	{
		if (categoryPage != null)
		{
			final String targetPage = getViewForPage(categoryPage);
			if (targetPage != null && !targetPage.isEmpty())
			{
				return targetPage;
			}
		}
		return PAGE_ROOT + PRODUCT_GRID_PAGE;
	}

	@ExceptionHandler(UnknownIdentifierException.class)
	public String handleUnknownIdentifierException(final UnknownIdentifierException exception, final HttpServletRequest request)
	{
		request.setAttribute("message", exception.getMessage());
		return FORWARD_PREFIX + "/404";
	}

	@Override
	protected void populateModel(final Model model, final SearchPageData<?> searchPageData, final ShowMode showMode)
	{
		super.populateModel(model, searchPageData, showMode);

		if (StringUtils.equalsIgnoreCase(getSiteConfigService().getString(PAGINATION_TYPE, PAGINATION), INFINITE_SCROLL))
		{
			model.addAttribute(IS_SHOW_ALLOWED, false);
		}
	}

	public String getConfigValue(final String key)
	{
		return configurationService.getConfiguration().getString(key);
	}
}
