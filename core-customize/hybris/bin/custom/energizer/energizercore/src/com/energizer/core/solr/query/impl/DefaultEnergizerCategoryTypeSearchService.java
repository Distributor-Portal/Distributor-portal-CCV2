/**
 * 
 */
package com.energizer.core.solr.query.impl;

import de.hybris.platform.category.CategoryService;
import de.hybris.platform.category.model.CategoryModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.energizer.core.model.EnergizerCategoryModel;
import com.energizer.core.solr.query.EnergizerCategoryTypeSearchService;


/**
 * This service class provides with the ability to search the category types for a category in a recursive manner.
 * 
 * @author kaushik.ganguly
 * 
 * 
 */
public class DefaultEnergizerCategoryTypeSearchService implements EnergizerCategoryTypeSearchService
{

	private CategoryService categoryService;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.energizer.core.solr.query.EnergizerCategoryTypeSearchService#getEnergizerCategoryWithType(com.energizer.core
	 * .model.EnergizerCategoryModel, java.lang.String)
	 */
	@Override
	public List<EnergizerCategoryModel> getEnergizerCategoryWithType(final EnergizerCategoryModel model, final String categoryType)
	{
		final List<EnergizerCategoryModel> list = new ArrayList<EnergizerCategoryModel>();
		final Collection<CategoryModel> queriedCategories = categoryService.getAllSupercategoriesForCategory(model);
		if (queriedCategories != null && queriedCategories.size() > 0)
		{
			for (final CategoryModel category : queriedCategories)
			{
				if (category instanceof EnergizerCategoryModel && ((EnergizerCategoryModel) category).getCategoryType() != null
						&& ((EnergizerCategoryModel) category).getCategoryType().equals(categoryType))
				{
					list.add((EnergizerCategoryModel) category);
					if (categoryService.getAllSupercategoriesForCategory(category) != null)
					{
						list.addAll(getEnergizerCategoryWithType((EnergizerCategoryModel) category, categoryType));
					}
				}
			}
		}

		return list;
	}

	/**
	 * @return the categoryService
	 */
	public CategoryService getCategoryService()
	{
		return categoryService;
	}

	/**
	 * @param categoryService
	 *           the categoryService to set
	 */
	public void setCategoryService(final CategoryService categoryService)
	{
		this.categoryService = categoryService;
	}


}
