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

import de.hybris.platform.acceleratorcms.model.components.SearchBoxComponentModel;
import de.hybris.platform.acceleratorservices.controllers.page.PageType;
import de.hybris.platform.acceleratorservices.customer.CustomerLocationService;
import de.hybris.platform.cms2.exceptions.CMSItemNotFoundException;
import de.hybris.platform.cms2.servicelayer.services.CMSComponentService;
import de.hybris.platform.commercefacades.product.ProductOption;
import de.hybris.platform.commercefacades.product.data.ProductData;
import de.hybris.platform.commercefacades.search.data.AutocompleteResultData;
import de.hybris.platform.commercefacades.search.data.AutocompleteSuggestionData;
import de.hybris.platform.commercefacades.search.data.SearchQueryData;
import de.hybris.platform.commercefacades.search.data.SearchStateData;
import de.hybris.platform.commerceservices.search.facetdata.ProductSearchPageData;
import de.hybris.platform.commerceservices.search.pagedata.PageableData;
import de.hybris.platform.commerceservices.search.pagedata.PaginationData;
import de.hybris.platform.commerceservices.search.pagedata.SearchPageData;
import de.hybris.platform.converters.Converters;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.util.Config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.energizer.core.model.EnergizerProductModel;
import com.energizer.core.solr.query.EnergizerSolrQueryManipulationService;
import com.energizer.facades.search.B2BProductSearchFacade;
import com.energizer.services.order.EnergizerCartService;
import com.energizer.services.product.EnergizerProductService;
import com.energizer.storefront.annotations.RequireHardLogIn;
import com.energizer.storefront.breadcrumb.impl.SearchBreadcrumbBuilder;
import com.energizer.storefront.constants.WebConstants;
import com.energizer.storefront.controllers.ControllerConstants;
import com.energizer.storefront.controllers.util.GlobalMessages;
import com.energizer.storefront.forms.AdvancedSearchForm;
import com.energizer.storefront.util.MetaSanitizerUtil;
import com.energizer.storefront.util.XSSFilterUtil;
import com.google.common.collect.Lists;


/**
 * Controller for search page.
 */
@Controller
@Scope("tenant")
@RequestMapping("/search")
public class SearchPageController extends AbstractSearchPageController
{
	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(SearchPageController.class);

	private static final String ADVANCED_SEARCH_PRODUCT_IDS_DELIMITER = "storefront.advancedsearch.delimiter";
	private static final String ADVANCED_SEARCH_PRODUCT_IDS_DELIMITER_DEFAULT = ",";

	private static final String COMPONENT_UID_PATH_VARIABLE_PATTERN = "{componentUid:.*}";

	private static final String ADVANCED_SEARCH_RESULT_TYPE_CATALOG = "catalog";
	private static final String ADVANCED_SEARCH_RESULT_TYPE_ORDER_FORM = "order-form";

	private static final String SEARCH_CMS_PAGE_ID = "search";
	private static final String NO_RESULTS_CMS_PAGE_ID = "searchEmpty";

	private static final String NO_RESULTS_ADVANCED_PAGE_ID = "searchAdvancedEmpty";

	private static final String FUTURE_STOCK_ENABLED = "storefront.products.futurestock.enabled";

	private static final String INFINITE_SCROLL = "infiniteScroll";

	private static final String ACTIVE_B2BUNIT = "activeB2BUnit";

	private static final String SITE_PERSONALCAREEMEA = "site.personalCareEMEA";

	@Resource(name = "b2bSolrProductSearchFacade")
	private B2BProductSearchFacade<ProductData> solrProductSearchFacade;

	@Resource(name = "b2bProductFlexibleSearchFacade")
	private B2BProductSearchFacade<ProductData> flexibleSearchProductSearchFacade;

	@Resource(name = "searchBreadcrumbBuilder")
	private SearchBreadcrumbBuilder searchBreadcrumbBuilder;

	@Resource(name = "customerLocationService")
	private CustomerLocationService customerLocationService;

	@Resource(name = "cmsComponentService")
	private CMSComponentService cmsComponentService;

	@Resource(name = "energizerSolrQueryManipulationService")
	private EnergizerSolrQueryManipulationService energizerSolrQueryManipulationService;

	@Resource
	EnergizerCartService energizerCartService;

	@Resource
	private ModelService modelService;

	@Resource(name = "energizerProductService")
	EnergizerProductService energizerProductService;

	@Resource(name = "productConverter")
	private Converter<ProductModel, ProductData> productConverter;

	@RequestMapping(method = RequestMethod.GET, params = "!q")
	public String textSearch(@RequestParam(value = "text", defaultValue = StringUtils.EMPTY) String searchText,
			final HttpServletRequest request, final Model model) throws CMSItemNotFoundException
	{
		try
		{
			if (StringUtils.isNotBlank(searchText))
			{
				//need to handle the solr query to enforce the b2bunit restriction on the catalog before it proceeds to the OOTB solr search
				searchText = energizerSolrQueryManipulationService.getSolrQueryForTextSearchPage(null, searchText);

				final PageableData pageableData = createPageableData(0, getSearchPageSize(), null, ShowMode.Page);
				final SearchStateData searchState = new SearchStateData();
				final SearchQueryData searchQueryData = new SearchQueryData();
				//searchQueryData.setValue(XSSFilterUtil.filter(searchText));

				final String PERSONALCARE_EMEA = super.getConfigValue(SITE_PERSONALCAREEMEA);

				if (PERSONALCARE_EMEA.equalsIgnoreCase(super.getCmsSiteService().getCurrentSite().getUid()))
				{
					final String filteredSearchQuery = (XSSFilterUtil.filter(searchText)).replaceAll("&#39;", "'");
					searchQueryData.setValue(filteredSearchQuery);
				}
				else
				{
					searchQueryData.setValue(XSSFilterUtil.filter(searchText));
				}

				searchState.setQuery(searchQueryData);

				ProductSearchPageData<SearchStateData, ProductData> searchPageData = null;
				try
				{
					searchPageData = solrProductSearchFacade.textSearch(searchState, pageableData);
				}
				catch (final Exception ex)
				{
					LOG.info("Exception occured ::: " + ex.getMessage());
					ex.printStackTrace();
					throw ex;
				}

				if (searchPageData == null)
				{
					storeCmsPageInModel(model, getContentPageForLabelOrId(NO_RESULTS_CMS_PAGE_ID));
				}
				else if (searchPageData.getKeywordRedirectUrl() != null)
				{
					// if the search engine returns a redirect, just
					return "redirect:" + searchPageData.getKeywordRedirectUrl();
				}
				else if (searchPageData.getPagination().getTotalNumberOfResults() == 0)
				{
					model.addAttribute("searchPageData", searchPageData);
					storeCmsPageInModel(model, getContentPageForLabelOrId(NO_RESULTS_CMS_PAGE_ID));
					updatePageTitle(searchText, model);
				}
				else
				{
					storeContinueUrl(request);
					try
					{
						populateModel(model, searchPageData, ShowMode.Page);
					}
					catch (final Exception ex)
					{
						LOG.info("Exception occured ::: " + ex.getMessage());
						ex.printStackTrace();
						throw ex;
					}
					storeCmsPageInModel(model, getContentPageForLabelOrId(SEARCH_CMS_PAGE_ID));
					updatePageTitle(searchText, model);
				}
				getRequestContextData(request).setSearch(searchPageData);
				if (searchPageData != null)
				{
					model.addAttribute(WebConstants.BREADCRUMBS_KEY, searchBreadcrumbBuilder.getBreadcrumbs(null, searchText,
							CollectionUtils.isEmpty(searchPageData.getBreadcrumbs())));
				}
			}
			else
			{
				storeCmsPageInModel(model, getContentPageForLabelOrId(NO_RESULTS_CMS_PAGE_ID));
			}
			model.addAttribute(ACTIVE_B2BUNIT, energizerSolrQueryManipulationService.getB2BUnitForLoggedInUser());
			addMetaData(model, "search.meta.description.results", searchText, "search.meta.description.on", PageType.PRODUCTSEARCH,
					"no-index,follow");

			return getViewForPage(model);
		}
		catch (final Exception e)
		{
			// YTODO: handle exception
			GlobalMessages.addErrorMessage(model, "search.page.error.message");
			storeCmsPageInModel(model, getContentPageForLabelOrId(NO_RESULTS_CMS_PAGE_ID));
			return getViewForPage(model);
		}

	}

	@RequestMapping(method = RequestMethod.GET, params = "q")
	public String refineSearch(@RequestParam("q") String searchQuery,
			@RequestParam(value = "page", defaultValue = "0") final int page,
			@RequestParam(value = "show", defaultValue = "Page") final ShowMode showMode,
			@RequestParam(value = "sort", required = false) final String sortCode,
			@RequestParam(value = "text", required = false) final String searchText, final HttpServletRequest request,
			final Model model) throws CMSItemNotFoundException
	{
		try
		{
			//need to handle the solr query to enforce the b2bunit restriction on the catalog before it proceeds to the OOTB solr search
			searchQuery = energizerSolrQueryManipulationService.getSolrQueryForTextSearchPage(sortCode, searchQuery);

			ProductSearchPageData<SearchStateData, ProductData> searchPageData = null;

			try
			{
				searchPageData = performSearch(searchQuery, page, showMode, sortCode, getSearchPageSize(), false);
				populateModel(model, searchPageData, showMode);
			}
			catch (final Exception ex)
			{
				LOG.info("Exception occured ::: " + ex.getMessage());
				ex.printStackTrace();
				throw ex;
			}

			model.addAttribute("userLocation", customerLocationService.getUserLocation());

			if (searchPageData.getPagination().getTotalNumberOfResults() == 0)
			{
				updatePageTitle(searchPageData.getFreeTextSearch(), model);
				storeCmsPageInModel(model, getContentPageForLabelOrId(NO_RESULTS_CMS_PAGE_ID));
			}
			else
			{
				storeContinueUrl(request);
				updatePageTitle(searchPageData.getFreeTextSearch(), model);
				storeCmsPageInModel(model, getContentPageForLabelOrId(SEARCH_CMS_PAGE_ID));
			}
			model.addAttribute(WebConstants.BREADCRUMBS_KEY, searchBreadcrumbBuilder.getBreadcrumbs(null, searchPageData));
			model.addAttribute(ACTIVE_B2BUNIT, energizerSolrQueryManipulationService.getB2BUnitForLoggedInUser());
			addMetaData(model, "search.meta.description.results", searchText, "search.meta.description.on", PageType.PRODUCTSEARCH,
					"no-index,follow");

			return getViewForPage(model);
		}
		catch (final Exception e)
		{
			e.printStackTrace();
			storeCmsPageInModel(model, getContentPageForLabelOrId(NO_RESULTS_CMS_PAGE_ID));
			GlobalMessages.addErrorMessage(model, "search.page.error.message");
			return getViewForPage(model);
		}
	}

	protected ProductSearchPageData<SearchStateData, ProductData> performSearch(final String searchQuery, final int page,
			final ShowMode showMode, final String sortCode, final int pageSize, final boolean populateMatrix)
	{
		try
		{
			final PageableData pageableData = createPageableData(page, pageSize, sortCode, showMode);
			final SearchStateData searchState = new SearchStateData();
			final SearchQueryData searchQueryData = new SearchQueryData();
			//searchQueryData.setValue(XSSFilterUtil.filter(searchQuery));

			final String PERSONALCARE_EMEA = super.getConfigValue(SITE_PERSONALCAREEMEA);

			if (PERSONALCARE_EMEA.equalsIgnoreCase(super.getCmsSiteService().getCurrentSite().getUid()))
			{
				final String filteredSearchQuery = (XSSFilterUtil.filter(searchQuery)).replaceAll("&#39;", "'");
				searchQueryData.setValue(filteredSearchQuery);
			}
			else
			{
				searchQueryData.setValue(XSSFilterUtil.filter(searchQuery));
			}
			searchState.setQuery(searchQueryData);

			final ProductSearchPageData<SearchStateData, ProductData> pageData = solrProductSearchFacade.textSearch(searchState,
					pageableData, populateMatrix);

			return pageData;
		}
		catch (final Exception e)
		{
			LOG.error("Solr text search Exception ::: ", e);
			throw e;
		}
	}

	@RequestMapping(value = "/results", method = RequestMethod.GET)
	public String productListerSearchResults(@RequestParam("q") final String searchQuery,
			@RequestParam(value = "page", defaultValue = "0") final int page,
			@RequestParam(value = "show", defaultValue = "Page") final ShowMode showMode,
			@RequestParam(value = "sort", required = false) final String sortCode,
			@RequestParam(value = "searchResultType", required = false) final String searchResultType,
			@RequestParam(value = "skuIndex", required = false, defaultValue = "0") final int skuIndex,
			@RequestParam(value = "isOrderForm", required = false, defaultValue = "false") final boolean isOrderForm,
			@RequestParam(value = "isOnlyProductIds", required = false, defaultValue = "false") final boolean isOnlyProductIds,
			@RequestParam(value = "isCreateOrderForm", required = false, defaultValue = "false") final boolean isCreateOrderForm,
			final Model model) throws CMSItemNotFoundException
	{
		try
		{
		//add b2bunit
		//addB2BUnitToExistingSearchQuery(searchQuery);

		ProductSearchPageData<SearchStateData, ProductData> searchPageData = null;

		try
		{
			searchPageData = performAdvancedSearch(searchQuery,
				isOnlyProductIds, isCreateOrderForm, page, showMode, sortCode, searchResultType, model);
		}
		catch (final Exception ex)
		{
			LOG.info("Exception occured ::: " + ex.getMessage());
			ex.printStackTrace();
				throw ex;
		}

		final SearchResultsData<ProductData> searchResultsData = new SearchResultsData<ProductData>();

		searchResultsData.setResults(searchPageData.getResults());
		searchResultsData.setPagination(searchPageData.getPagination());

		model.addAttribute("searchResultsData", searchResultsData);
		model.addAttribute("skuIndex", Integer.valueOf(skuIndex));
		model.addAttribute("isOrderForm", Boolean.valueOf(isOrderForm));
		model.addAttribute("isCreateOrderForm", Boolean.valueOf(isCreateOrderForm));


		if (isCreateOrderForm)
		{
			model.addAttribute("searchResultType", ADVANCED_SEARCH_RESULT_TYPE_ORDER_FORM);
			final List<String> filterSkus = splitSkusAsList(searchQuery);
			model.addAttribute("filterSkus", filterSkus);
		}
		else
		{
			model.addAttribute("searchResultType", searchResultType);
		}
		model.addAttribute(ACTIVE_B2BUNIT, energizerSolrQueryManipulationService.getB2BUnitForLoggedInUser());
		return ControllerConstants.Views.Fragments.Product.ProductLister;
		}
		catch (final Exception e)
		{
			LOG.error("Exception Occured during search result ::: ", e);
			GlobalMessages.addErrorMessage(model, "search.page.error.message");
			storeCmsPageInModel(model, getContentPageForLabelOrId(NO_RESULTS_CMS_PAGE_ID));
			return ControllerConstants.Views.Fragments.Product.ProductLister;
		}
	}

	@RequestMapping(value = "/advanced", method = RequestMethod.GET)
	public String advanceSearchResults(
			@RequestParam(value = "keywords", required = false, defaultValue = StringUtils.EMPTY) String keywords,
			@RequestParam(value = "searchResultType", required = false, defaultValue = ADVANCED_SEARCH_RESULT_TYPE_CATALOG) final String searchResultType,
			@RequestParam(value = "inStockOnly", required = false, defaultValue = "false") final boolean inStockOnly,
			@RequestParam(value = "onlyProductIds", required = false, defaultValue = "false") final boolean onlyProductIds,
			@RequestParam(value = "isCreateOrderForm", required = false, defaultValue = "false") final boolean isCreateOrderForm,
			@RequestParam(value = "q", defaultValue = StringUtils.EMPTY) String searchQuery,
			@RequestParam(value = "page", defaultValue = "0") final int page,
			@RequestParam(value = "show", defaultValue = "Page") final ShowMode showMode,
			@RequestParam(value = "sort", required = false) final String sortCode, final Model model)
			throws CMSItemNotFoundException
	{
		try
		{
		if (StringUtils.isNotBlank(keywords))
		{
			searchQuery = keywords;
		}
		else
		{
			if (StringUtils.isNotBlank(searchQuery))
			{
				keywords = StringUtils.split(searchQuery, ":")[0];
			}
		}

		performAdvancedSearch(searchQuery, onlyProductIds, isCreateOrderForm, page, showMode, sortCode, searchResultType, model);

		String metaInfoText = null;
		if (StringUtils.isEmpty(keywords))
		{
			metaInfoText = MetaSanitizerUtil.sanitizeDescription(getMessageSource().getMessage(
					"search.advanced.meta.description.title", null, getCurrentLocale()));
		}
		else
		{
			metaInfoText = MetaSanitizerUtil.sanitizeDescription(keywords);
		}

		model.addAttribute(WebConstants.BREADCRUMBS_KEY, searchBreadcrumbBuilder.getBreadcrumbs(null, metaInfoText, false));

		final AdvancedSearchForm form = new AdvancedSearchForm();
		form.setOnlyProductIds(Boolean.valueOf(onlyProductIds));
		form.setInStockOnly(Boolean.valueOf(inStockOnly));
		form.setKeywords(keywords);
		form.setCreateOrderForm(isCreateOrderForm);


		if (isCreateOrderForm)
		{
			form.setSearchResultType(ADVANCED_SEARCH_RESULT_TYPE_ORDER_FORM);
			final List<String> filterSkus = splitSkusAsList(keywords);
			form.setFilterSkus(filterSkus);
			form.setCreateOrderForm(Boolean.valueOf(false));
			form.setOnlyProductIds(Boolean.valueOf(true));
		}
		else
		{
			form.setSearchResultType(searchResultType);
		}

		model.addAttribute("advancedSearchForm", form);
		model.addAttribute("futureStockEnabled", Boolean.valueOf(Config.getBoolean(FUTURE_STOCK_ENABLED, false)));

		storeCmsPageInModel(model, getContentPageForLabelOrId(NO_RESULTS_ADVANCED_PAGE_ID));

		addMetaData(model, "search.meta.description.results", metaInfoText, "search.meta.description.on", PageType.PRODUCTSEARCH,
				"no-index,follow");

		return getViewForPage(model);
		}
		catch (final Exception e)
		{
			storeCmsPageInModel(model, getContentPageForLabelOrId(NO_RESULTS_CMS_PAGE_ID));
			e.printStackTrace();
			GlobalMessages.addErrorMessage(model, "search.page.error.message");
			return getViewForPage(model);
		}
	}

	protected ProductSearchPageData<SearchStateData, ProductData> performAdvancedSearch(final String keywords,
			final boolean onlyProductIds, final boolean isCreateOrderForm, final int page, final ShowMode showMode,
			final String sortCode, final String searchResultType, final Model model)
	{
		try
		{
		ProductSearchPageData<SearchStateData, ProductData> searchPageData = createEmptySearchPageData();

		// check if it is order form (either order form was selected or "Create Order Form"
		final boolean populateMatrix = (searchResultType != null && StringUtils.equals(searchResultType,
				ADVANCED_SEARCH_RESULT_TYPE_ORDER_FORM)) || isCreateOrderForm;

		if (StringUtils.isNotBlank(keywords))
		{
			if (onlyProductIds || isCreateOrderForm)
			{
				// search using flexible search
				final List<String> productIdsList = splitSkusAsList(keywords);
				final PageableData pageableData = createPageableData(page, 10, sortCode, showMode);
				searchPageData = flexibleSearchProductSearchFacade.searchForSkus(productIdsList, pageableData,
						Arrays.asList(ProductOption.URL, ProductOption.IMAGES), true);
			}
			else
			{
				// search using solr.
				searchPageData = performSearch(keywords, page, showMode, sortCode, getSearchPageSize(), populateMatrix);
			}

		}
		populateModel(model, searchPageData, showMode);

		return searchPageData;
		}
		catch (final Exception e)
		{
			throw e;
		}
	}

	private ProductSearchPageData<SearchStateData, ProductData> createEmptySearchPageData()
	{
		final ProductSearchPageData productSearchPageData = new ProductSearchPageData();

		productSearchPageData.setResults(Lists.newArrayList());
		final PaginationData pagination = new PaginationData();
		pagination.setTotalNumberOfResults(0);
		productSearchPageData.setPagination(pagination);
		productSearchPageData.setSorts(Lists.newArrayList());

		return productSearchPageData;
	}

	protected List<String> splitSkusAsList(final String skus)
	{
		return Arrays.asList(StringUtils.split(skus,
				Config.getString(ADVANCED_SEARCH_PRODUCT_IDS_DELIMITER, ADVANCED_SEARCH_PRODUCT_IDS_DELIMITER_DEFAULT)));
	}

	private Locale getCurrentLocale()
	{
		return getI18nService().getCurrentLocale();
	}

	@ResponseBody
	@RequestMapping(value = "/autocomplete", method =
	{ RequestMethod.GET, RequestMethod.POST })
	public List<String> getAutocompleteSuggestions(@RequestParam("term") final String term)
	{
		final List<String> terms = new ArrayList<String>();
		for (final AutocompleteSuggestionData termData : solrProductSearchFacade.getAutocompleteSuggestions(term))
		{
			terms.add(termData.getTerm());
		}
		return terms;
	}


	@ResponseBody
	@RequestMapping(value = "/autocomplete/" + COMPONENT_UID_PATH_VARIABLE_PATTERN, method =
	{ RequestMethod.GET, RequestMethod.POST })
	public AutocompleteResultData getAutocompleteSuggestions(@PathVariable final String componentUid,
			@RequestParam("term") String term) throws CMSItemNotFoundException
	{
		final AutocompleteResultData resultData = new AutocompleteResultData();

		final SearchBoxComponentModel component = (SearchBoxComponentModel) cmsComponentService.getSimpleCMSComponent(componentUid);

		/**
		 * commenting out the OOTB functionality to show products only for the suggestions as per the business rules
		 * provided in the requirement
		 */
		/*
		 * if (component.isDisplaySuggestions()) {
		 * resultData.setSuggestions(subList(solrProductSearchFacade.getAutocompleteSuggestions(term),
		 * component.getMaxSuggestions())); }
		 */

		/*
		 * if (component.isDisplayProducts()) {
		 */
		//final ProductSearchPageData<SearchStateData, ProductData> pageData = solrProductSearchFacade.textSearch(term);

		/**
		 * Added the functionality to filter out products associated to the current parent B2Bunit
		 */
		term = energizerSolrQueryManipulationService.getSolrQueryForTextSearchPage(null, term);

		final PageableData pageableData = createPageableData(0, getSearchPageSize(), null, ShowMode.Page);
		final SearchStateData searchState = new SearchStateData();
		final SearchQueryData searchQueryData = new SearchQueryData();
		//searchQueryData.setValue(XSSFilterUtil.filter(term));

		final String PERSONALCARE_EMEA = super.getConfigValue(SITE_PERSONALCAREEMEA);

		if (PERSONALCARE_EMEA.equalsIgnoreCase(super.getCmsSiteService().getCurrentSite().getUid()))
		{
			final String filteredSearchQuery = (XSSFilterUtil.filter(term)).replaceAll("&#39;", "'");
			searchQueryData.setValue(filteredSearchQuery);
		}
		else
		{
			searchQueryData.setValue(XSSFilterUtil.filter(term));
		}

		searchState.setQuery(searchQueryData);

		final ProductSearchPageData<SearchStateData, ProductData> pageData = solrProductSearchFacade.textSearch(searchState,
				pageableData);
		resultData.setProducts(subList(pageData.getResults(), component.getMaxProducts()));


		//}

		return resultData;
	}

	protected <E> List<E> subList(final List<E> list, final int maxElements)
	{
		if (CollectionUtils.isEmpty(list))
		{
			return Collections.emptyList();
		}

		if (list.size() > maxElements)
		{
			return list.subList(0, maxElements);
		}

		return list;
	}

	@ResponseBody
	@RequestMapping(value = "/autocompleteSecure", method =
	{ RequestMethod.GET, RequestMethod.POST })
	public List<String> getAutocompleteSuggestionsSecure(@RequestParam("term") final String term)
	{
		return getAutocompleteSuggestions(term);
	}

	protected void updatePageTitle(final String searchText, final Model model)
	{
		storeContentPageTitleInModel(
				model,
				getPageTitleResolver().resolveContentPageTitle(
						getMessageSource().getMessage("search.meta.title", null, getCurrentLocale()) + " " + searchText));
	}

	protected void addMetaData(final Model model, final String metaPrefixKey, final String searchText,
			final String metaPostfixKey, final PageType pageType, final String robotsBehaviour)
	{
		final String metaDescription = MetaSanitizerUtil.sanitizeDescription(getMessageSource().getMessage(metaPrefixKey, null,
				getCurrentLocale())
				+ " "
				+ searchText
				+ " "
				+ getMessageSource().getMessage(metaPostfixKey, null, getCurrentLocale())
				+ " "
				+ getSiteName());
		final String metaKeywords = MetaSanitizerUtil.sanitizeKeywords(searchText);
		setUpMetaData(model, metaKeywords, metaDescription);

		model.addAttribute("pageType", pageType.name());
		model.addAttribute("metaRobots", robotsBehaviour);
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

	@RequestMapping(value = "/getDoubleStackProducts", method = RequestMethod.GET)
	@RequireHardLogIn
	public String getDoubleStackProducts(@RequestParam(value = "page", defaultValue = "0") final int page,
			@RequestParam(value = "show", defaultValue = "Page") final ShowMode showMode,
			@RequestParam(value = "sort", required = false) final String sortCode,
			@RequestParam(value = "text", required = false) final String searchText, final HttpServletRequest request,
			final Model model, @RequestParam("productID") final List productID) throws CMSItemNotFoundException

	{

		try
		{
			LOG.info("for doubleStacking Products");
			//final HashMap productsNotDoubleStacked = energizerCartService.productsNotDoublestacked123();

			final List<EnergizerProductModel> productModelList = new ArrayList<EnergizerProductModel>();



			for (final Iterator iterator = productID.iterator(); iterator.hasNext();)
			{
				final String productCode = ((String) iterator.next()).replaceAll("\\[", "").replaceAll("\\]", "");


				LOG.info("In loop, productCode " + productCode);
				if (!productCode.equals("0"))
				{

					final EnergizerProductModel product = energizerProductService.getProductWithCode(productCode);

					productModelList.add(product);
				}
			}
			LOG.info(" Products list size: " + productModelList.size());

			final ProductSearchPageData<SearchStateData, ProductData> searchPageData = new ProductSearchPageData<SearchStateData, ProductData>();
			final PageableData pageableData = createPageableData(page, getSearchPageSize(), sortCode, showMode);
			final PaginationData paginationData = new PaginationData();
			paginationData.setPageSize(getSearchPageSize());
			paginationData.setNumberOfPages(page);
			paginationData.setSort(sortCode);
			paginationData.setTotalNumberOfResults(productModelList.size());

			searchPageData.setPagination(paginationData);
			searchPageData.setResults(Converters.convertAll(productModelList, productConverter));

			LOG.info(" Search Results: " + searchPageData.getResults().size());



			LOG.info("Value 1: " + searchPageData.getPagination().getTotalNumberOfResults());
			LOG.info(" Value 2 " + searchPageData.getPagination().getPageSize());
			LOG.info(" value 3 " + isShowAllAllowed(searchPageData));

			populateModel(model, searchPageData, showMode);
			model.addAttribute("userLocation", customerLocationService.getUserLocation());


			storeCmsPageInModel(model, getContentPageForLabelOrId(SEARCH_CMS_PAGE_ID));
			//model.addAttribute("breadcrumbs", searchBreadcrumbBuilder.getBreadcrumbs(null, searchPageData));
			model.addAttribute("metaRobots", "no-index,no-follow");
			model.addAttribute(ACTIVE_B2BUNIT, energizerSolrQueryManipulationService.getB2BUnitForLoggedInUser());


			return getViewForPage(model);

		}
		catch (final Exception e)
		{
			storeCmsPageInModel(model, getContentPageForLabelOrId(NO_RESULTS_CMS_PAGE_ID));
			e.printStackTrace();
			GlobalMessages.addErrorMessage(model, "search.page.error.message");
			return getViewForPage(model);
		}
	}

}
