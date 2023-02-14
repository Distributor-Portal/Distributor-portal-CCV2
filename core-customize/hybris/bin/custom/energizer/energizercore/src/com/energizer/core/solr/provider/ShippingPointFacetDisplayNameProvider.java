package com.energizer.core.solr.provider;

import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.provider.impl.AbstractFacetValueDisplayNameProvider;
import de.hybris.platform.solrfacetsearch.search.SearchQuery;
import org.apache.commons.lang.StringUtils;

public class ShippingPointFacetDisplayNameProvider extends AbstractFacetValueDisplayNameProvider {

    @Override
    public String getDisplayName(final SearchQuery query, final IndexedProperty property, final String facetValue)
    {
        if(facetValue.contains("AccountNo")){
            return StringUtils.substringBefore(facetValue,"AccountNo");
        }else {
            return facetValue;
        }
    }
}
