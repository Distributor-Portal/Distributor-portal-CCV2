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
package com.energizer.core.solr.provider;

import de.hybris.platform.category.model.CategoryModel;
import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.solrfacetsearch.config.IndexConfig;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.config.exceptions.FieldValueProviderException;
import de.hybris.platform.solrfacetsearch.provider.FieldNameProvider;
import de.hybris.platform.solrfacetsearch.provider.FieldValue;
import de.hybris.platform.solrfacetsearch.provider.FieldValueProvider;
import de.hybris.platform.solrfacetsearch.provider.impl.AbstractPropertyFieldValueProvider;
import de.hybris.platform.util.Config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.energizer.core.model.EnergizerCategoryModel;
import com.energizer.core.model.EnergizerProductModel;
import com.energizer.core.solr.query.EnergizerCategoryTypeSearchService;


/**
 * This class provides segment category for the product for the solr indexing
 */
public class EnergizerProductSegmentValueProvider extends AbstractPropertyFieldValueProvider implements FieldValueProvider
{
	private FieldNameProvider fieldNameProvider;
	private EnergizerCategoryTypeSearchService energizerCategoryTypeSearchService;
	private ConfigurationService configurationService;


	private static final Logger LOG = Logger.getLogger(EnergizerProductSegmentValueProvider.class);


	@Override
	public Collection<FieldValue> getFieldValues(final IndexConfig indexConfig, final IndexedProperty indexedProperty,
			final Object model) throws FieldValueProviderException
	{
		final String ENERGIZER_SEGMENT = Config.getParameter("marketingcategory.level1.type");
		final Collection<FieldValue> fieldValues = new ArrayList<FieldValue>();

		try
		{
			if (model instanceof EnergizerProductModel)
			{
				final EnergizerProductModel energizerProduct = (EnergizerProductModel) model;
				final Collection<CategoryModel> superCategories = energizerProduct.getSupercategories();

				for (final CategoryModel category : superCategories)
				{
					if (category instanceof EnergizerCategoryModel)
					{
						final List<EnergizerCategoryModel> allCategoriesAsSegments = energizerCategoryTypeSearchService
								.getEnergizerCategoryWithType((EnergizerCategoryModel) category, ENERGIZER_SEGMENT);

						final EnergizerCategoryModel castedCategoryModel = (EnergizerCategoryModel) category;

						if (castedCategoryModel.getCategoryType() != null
								&& castedCategoryModel.getCategoryType().equals(ENERGIZER_SEGMENT))
						{
							allCategoriesAsSegments.add(castedCategoryModel);
						}


						for (final EnergizerCategoryModel segment : allCategoriesAsSegments)
						{

							if (indexedProperty.isLocalized())
							{
								final Collection<LanguageModel> languages = indexConfig.getLanguages();

								for (final LanguageModel language : languages)
								{
									final Collection<String> names = getFieldNameProvider().getFieldNames(indexedProperty,
											(language == null) ? null : language.getIsocode());

									for (final String fieldName : names)
									{
										fieldValues.add(new FieldValue(fieldName, segment.getName(new Locale(language.getIsocode()))));
										LOG.debug("Segment added  :" + segment.getName(new Locale(language.getIsocode())));
									}

								}


							}
						}

					}

				}


			}

		}
		catch (final Exception ex)
		{
			LOG.error("Exception Occured", ex);
		}
		finally
		{
			LOG.debug("Created the Product segment");
		}
		return fieldValues;
	}



	protected FieldNameProvider getFieldNameProvider()
	{
		return fieldNameProvider;
	}

	@Required
	public void setFieldNameProvider(final FieldNameProvider fieldNameProvider)
	{
		this.fieldNameProvider = fieldNameProvider;
	}


	/**
	 * @return the energizerCategoryTypeSearchService
	 */
	public EnergizerCategoryTypeSearchService getEnergizerCategoryTypeSearchService()
	{
		return energizerCategoryTypeSearchService;
	}



	/**
	 * @param energizerCategoryTypeSearchService
	 *           the energizerCategoryTypeSearchService to set
	 */
	public void setEnergizerCategoryTypeSearchService(final EnergizerCategoryTypeSearchService energizerCategoryTypeSearchService)
	{
		this.energizerCategoryTypeSearchService = energizerCategoryTypeSearchService;
	}



	/**
	 * @return the configurationService
	 */
	public ConfigurationService getConfigurationService()
	{
		return configurationService;
	}



	/**
	 * @param configurationService
	 *           the configurationService to set
	 */
	public void setConfigurationService(final ConfigurationService configurationService)
	{
		this.configurationService = configurationService;
	}





}
