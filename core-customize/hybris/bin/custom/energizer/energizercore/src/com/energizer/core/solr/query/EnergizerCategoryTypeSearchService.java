/**
 * 
 */
package com.energizer.core.solr.query;

import java.util.List;

import com.energizer.core.model.EnergizerCategoryModel;


/**
 * This interface defines the method signatures for recursively searching the category types for a category
 * 
 * @author kaushik.ganguly
 * 
 */
public interface EnergizerCategoryTypeSearchService
{
	public List<EnergizerCategoryModel> getEnergizerCategoryWithType(final EnergizerCategoryModel model, final String categoryType);

}
