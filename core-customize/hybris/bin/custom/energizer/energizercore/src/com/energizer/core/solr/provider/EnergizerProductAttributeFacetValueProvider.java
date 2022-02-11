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

import de.hybris.platform.solrfacetsearch.config.IndexConfig;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.config.exceptions.FieldValueProviderException;
import de.hybris.platform.solrfacetsearch.provider.FieldNameProvider;
import de.hybris.platform.solrfacetsearch.provider.FieldValue;
import de.hybris.platform.solrfacetsearch.provider.FieldValueProvider;
import de.hybris.platform.solrfacetsearch.provider.impl.AbstractPropertyFieldValueProvider;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.energizer.core.model.EnergizerProductModel;


/**
 * This class provides attribute value for facet search from the marketing category for the solr indexing
 * 
 * @author kaushik.ganguly
 */
public class EnergizerProductAttributeFacetValueProvider extends AbstractPropertyFieldValueProvider implements FieldValueProvider
{
	private FieldNameProvider fieldNameProvider;
	private static final Logger LOG = Logger.getLogger(EnergizerProductAttributeFacetValueProvider.class);

	@Override
	public Collection<FieldValue> getFieldValues(final IndexConfig indexConfig, final IndexedProperty indexedProperty,
			final Object model) throws FieldValueProviderException
	{

		final Collection<FieldValue> fieldValues = new ArrayList<FieldValue>();

		try
		{
			if (model instanceof EnergizerProductModel)
			{
				final EnergizerProductModel energizerProduct = (EnergizerProductModel) model;
				final Collection<String> fieldNames = fieldNameProvider.getFieldNames(indexedProperty, null);

				for (final String fieldName : fieldNames)
				{
					final String searchAttribute = energizerProduct.getSearchAttribute();
					if (searchAttribute != null && !searchAttribute.isEmpty())
					{
						fieldValues.add(new FieldValue(fieldName, searchAttribute));
						LOG.debug("Field Name :" + fieldName + " Attribute : " + energizerProduct.getSearchAttribute());
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
			LOG.debug("Created the facet attribute");
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
}
