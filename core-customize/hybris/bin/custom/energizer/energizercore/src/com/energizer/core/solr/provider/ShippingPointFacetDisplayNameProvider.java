package com.energizer.core.solr.provider;

import com.energizer.services.product.EnergizerProductService;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.provider.impl.AbstractFacetValueDisplayNameProvider;
import de.hybris.platform.solrfacetsearch.search.SearchQuery;
import org.apache.commons.lang.StringUtils;

public class ShippingPointFacetDisplayNameProvider extends AbstractFacetValueDisplayNameProvider {

    private EnergizerProductService energizerProductService;

    public EnergizerProductService getEnergizerProductService() {
        return energizerProductService;
    }

    public void setEnergizerProductService(EnergizerProductService energizerProductService) {
        this.energizerProductService = energizerProductService;
    }

    @Override
    public String getDisplayName(final SearchQuery query, final IndexedProperty property, final String facetValue)
    {
        if(facetValue.contains("_")){
            String shippingPoint = StringUtils.substringBefore(facetValue,"_").trim();
            String shippingPointName = energizerProductService.getShippingPointName(shippingPoint);
            if(null != shippingPointName ){
                return shippingPointName;
            }else {
               return  shippingPoint;
            }
        }else {
            return facetValue;
        }
    }
}
