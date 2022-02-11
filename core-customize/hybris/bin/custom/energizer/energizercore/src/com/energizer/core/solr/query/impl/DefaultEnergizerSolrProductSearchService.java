/**
 *
 */
package com.energizer.core.solr.query.impl;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

import de.hybris.platform.category.model.CategoryModel;
import de.hybris.platform.commerceservices.search.facetdata.ProductCategorySearchPageData;
import de.hybris.platform.commerceservices.search.pagedata.PageableData;
import de.hybris.platform.commerceservices.search.resultdata.SearchResultValueData;
import de.hybris.platform.commerceservices.search.solrfacetsearch.data.SearchQueryPageableData;
import de.hybris.platform.commerceservices.search.solrfacetsearch.data.SolrSearchQueryData;
import de.hybris.platform.commerceservices.search.solrfacetsearch.data.SolrSearchRequest;
import de.hybris.platform.commerceservices.search.solrfacetsearch.data.SolrSearchResponse;
import de.hybris.platform.commerceservices.search.solrfacetsearch.impl.DefaultSolrProductSearchService;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.solrfacetsearch.search.impl.SolrSearchResult;

import javax.annotation.Resource;

import org.apache.log4j.Logger;


/**
 * @author kaki.rajasekhar
 *
 */
public class DefaultEnergizerSolrProductSearchService extends DefaultSolrProductSearchService<SearchResultValueData>
{

	@Resource
	private SessionService sessionService;

	final Logger LOG = Logger.getLogger(DefaultEnergizerSolrProductSearchService.class);

	@Override
	protected ProductCategorySearchPageData<SolrSearchQueryData, SearchResultValueData, CategoryModel> doSearch(
			final SolrSearchQueryData searchQueryData, final PageableData pageableData)
	{
		LOG.info("DefaultEnergizerSolrProductSearchService calling ::::::::::::::");
		validateParameterNotNull(searchQueryData, "SearchQueryData cannot be null");

		if (!searchQueryData.getFreeTextSearch().isEmpty())
		{
			sessionService.setAttribute("solrFreeTextEnable", Boolean.valueOf(true));
		}

		// Create the SearchQueryPageableData that contains our parameters
		final SearchQueryPageableData<SolrSearchQueryData> searchQueryPageableData = buildSearchQueryPageableData(searchQueryData,
				pageableData);

		/* Build up the search text request query exact match with code or ean -- START */

		// Build up the search request
		final SolrSearchRequest solrSearchRequest = getSearchQueryPageableConverter().convert(searchQueryPageableData);

		// Execute the search
		final SolrSearchResponse solrSearchResponse = getSearchRequestConverter().convert(solrSearchRequest);
		final SolrSearchResult searchResult = (SolrSearchResult) solrSearchResponse.getSearchResult();
		/* search response exact match with code or ean -- END */

		/* Search result is Zero . We are again Build up the search request with patterns */
		if (!solrSearchRequest.getSearchQueryData().getFreeTextSearch().isEmpty() && (int) searchResult.getNumberOfResults() == 0)
		{
			sessionService.setAttribute("solrFreeTextEnable", Boolean.valueOf(false));
			// Build up the search request
			final SolrSearchRequest solrSearchRequest1 = getSearchQueryPageableConverter().convert(searchQueryPageableData);

			// Execute the search
			final SolrSearchResponse solrSearchResponse1 = getSearchRequestConverter().convert(solrSearchRequest1);
			// Convert the response
			return getSearchResponseConverter().convert(solrSearchResponse1);
		}
		sessionService.setAttribute("solrFreeTextEnable", Boolean.valueOf(false));
		// Convert the response
		return getSearchResponseConverter().convert(solrSearchResponse);
	}
}
