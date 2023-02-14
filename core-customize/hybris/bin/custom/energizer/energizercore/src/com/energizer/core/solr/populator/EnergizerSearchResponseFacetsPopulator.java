package com.energizer.core.solr.populator;

import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.b2b.model.B2BUnitModel;
import de.hybris.platform.b2b.services.B2BCustomerService;
import de.hybris.platform.commerceservices.search.facetdata.FacetData;
import de.hybris.platform.commerceservices.search.facetdata.FacetSearchPageData;
import de.hybris.platform.commerceservices.search.facetdata.FacetValueData;
import de.hybris.platform.commerceservices.search.solrfacetsearch.data.SolrSearchQueryData;
import de.hybris.platform.commerceservices.search.solrfacetsearch.data.SolrSearchResponse;
import de.hybris.platform.commerceservices.search.solrfacetsearch.populators.SearchResponseFacetsPopulator;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.search.Facet;
import de.hybris.platform.solrfacetsearch.search.FacetValue;
import de.hybris.platform.solrfacetsearch.search.SearchQuery;
import de.hybris.platform.solrfacetsearch.search.SearchResult;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class EnergizerSearchResponseFacetsPopulator<FACET_SEARCH_CONFIG_TYPE, INDEXED_TYPE_TYPE, INDEXED_PROPERTY_TYPE, INDEXED_TYPE_SORT_TYPE, ITEM> extends
        SearchResponseFacetsPopulator<FACET_SEARCH_CONFIG_TYPE, INDEXED_TYPE_TYPE, INDEXED_PROPERTY_TYPE, INDEXED_TYPE_SORT_TYPE, ITEM>{

    public B2BCustomerService<B2BCustomerModel, B2BUnitModel> getB2BCustomerService() {
        return b2BCustomerService;
    }

    public void setB2BCustomerService(B2BCustomerService<B2BCustomerModel, B2BUnitModel> b2BCustomerService) {
        this.b2BCustomerService = b2BCustomerService;
    }

    private B2BCustomerService<B2BCustomerModel, B2BUnitModel> b2BCustomerService;

    @Override
    public void populate(
            final SolrSearchResponse<FACET_SEARCH_CONFIG_TYPE, INDEXED_TYPE_TYPE, INDEXED_PROPERTY_TYPE, SearchQuery, INDEXED_TYPE_SORT_TYPE, SearchResult> source,
            final FacetSearchPageData<SolrSearchQueryData, ITEM> target)
    {
        super.populate(source, target);
    }


    @Override
    protected void buildFacetValues(final FacetData<SolrSearchQueryData> facetData, final Facet facet,
                                    final IndexedProperty indexedProperty, final SearchResult solrSearchResult, final SolrSearchQueryData searchQueryData)
    {
        final List<FacetValue> facetValues = facet.getFacetValues();
        if (facetValues != null && !facetValues.isEmpty())
        {
            final List<FacetValueData<SolrSearchQueryData>> allFacetValues = new ArrayList<>(facetValues.size());

            Iterator<FacetValue> facetValueIteratator = facetValues.iterator();

            while(facetValueIteratator.hasNext()){
                FacetValue f = facetValueIteratator.next();
                if (facet.getName().equalsIgnoreCase("shippingPoint")) {
                    String b2bunit = getB2BCustomerService().getCurrentB2BCustomer().getDefaultB2BUnit().getUid();
                    if (!(f.getName().contains(b2bunit))) {
                        facetValues.remove(f);
                    }
                }
            }


            for (final FacetValue facetValue : facetValues)
            {
                final FacetValueData<SolrSearchQueryData> facetValueData = buildFacetValue(facetData, facet, facetValue,
                        solrSearchResult, searchQueryData);
                if (facetValueData != null)
                {
                    allFacetValues.add(facetValueData);
                }
            }

            facetData.setValues(allFacetValues);

            if (!CollectionUtils.isEmpty(facet.getTopFacetValues()))
            {
                final List<FacetValueData<SolrSearchQueryData>> topFacetValuesData = new ArrayList<>();
                for (final FacetValue facetValue : facet.getTopFacetValues())
                {
                    final FacetValueData<SolrSearchQueryData> topFacetValueData = buildFacetValue(facetData, facet, facetValue,
                            solrSearchResult, searchQueryData);
                    if (topFacetValueData != null)
                    {
                        topFacetValuesData.add(topFacetValueData);
                    }
                }
                facetData.setTopValues(topFacetValuesData);
            }
        }
    }

}
