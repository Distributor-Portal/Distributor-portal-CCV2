/**
 * 
 */
package com.energizer.services.product.dao.impl;

import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;

import java.util.List;

import javax.annotation.Resource;

import com.energizer.core.model.EnergizerSalesAreaUOMModel;
import com.energizer.services.product.dao.EnergizerSalesUOMDao;


/**
 * @author Geetika Singh
 * 
 */
public class DefaultEnergizerSalesUOMDao implements EnergizerSalesUOMDao
{
	@Resource
	private FlexibleSearchService flexibleSearchService;

	public List<EnergizerSalesAreaUOMModel> findSalesAreaUOM(final String familyId)
	{
		final FlexibleSearchQuery retreiveQuery = new FlexibleSearchQuery(
				"SELECT {pk} FROM {EnergizerSalesAreaUOM} where {familyID}=?familyID");
		retreiveQuery.addQueryParameter("familyID", familyId);
		final SearchResult<EnergizerSalesAreaUOMModel> result = flexibleSearchService.search(retreiveQuery);

		final List<EnergizerSalesAreaUOMModel> salesUOMs = result.getResult();

		return salesUOMs;
	}


}
