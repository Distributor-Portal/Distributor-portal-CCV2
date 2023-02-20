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

import de.hybris.platform.europe1.model.PriceRowModel;
import de.hybris.platform.solrfacetsearch.config.IndexConfig;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.config.exceptions.FieldValueProviderException;
import de.hybris.platform.solrfacetsearch.provider.FieldNameProvider;
import de.hybris.platform.solrfacetsearch.provider.FieldValue;
import de.hybris.platform.solrfacetsearch.provider.FieldValueProvider;
import de.hybris.platform.solrfacetsearch.provider.impl.AbstractPropertyFieldValueProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.energizer.core.model.EnergizerCMIRModel;
import com.energizer.core.model.EnergizerPriceRowModel;
import com.energizer.core.model.EnergizerProductModel;
import com.energizer.services.product.EnergizerProductService;


/**
 * This class provides shipping point value provider for the solr indexing
 *
 * @author kaushik.ganguly
 */
public class EnergizerProductShippingPointValueProvider extends AbstractPropertyFieldValueProvider implements FieldValueProvider
{
	private FieldNameProvider fieldNameProvider;
	private EnergizerProductService energizerProductService;
	private static final Logger LOG = Logger.getLogger(EnergizerProductShippingPointValueProvider.class);

	@Override
	public Collection<FieldValue> getFieldValues(final IndexConfig indexConfig, final IndexedProperty indexedProperty,
			final Object model) throws FieldValueProviderException
	{

		final Collection<FieldValue> fieldValues = new ArrayList<FieldValue>();
		String fieldName = StringUtils.EMPTY;
		final Set<String> shippingPointNameSet = new HashSet<String>();

		try
		{
			if (model instanceof EnergizerProductModel)
			{
				final EnergizerProductModel energizerProduct = (EnergizerProductModel) model;
				for (final PriceRowModel unit : energizerProduct.getEurope1Prices())
				{
					if (unit instanceof EnergizerPriceRowModel && ((EnergizerPriceRowModel) unit).getB2bUnit() != null)
					{

						final Collection<String> fieldNames = fieldNameProvider.getFieldNames(indexedProperty, null);

						//LOG.info("fieldNames size :: " + fieldNames.size() + " , fieldNames :: " + fieldNames);
						for (final String field : fieldNames)
						{
							fieldName = field;

							//LOG.info("field :: " + field);

							final List<EnergizerCMIRModel> cmirList = energizerProductService
									.getEnergizerCMIRList(energizerProduct.getCode());

							for (final EnergizerCMIRModel cmir : cmirList)
							{
								String shippingPoint = cmir.getShippingPoint();
								String b2bunit = null;
								if(cmir.getB2bUnit()!=null) {
									b2bunit = cmir.getB2bUnit().getUid();
								}
								if(b2bunit !=null && shippingPoint != null) {
									String b2bunitSippingPoint = shippingPoint.concat("_").concat(b2bunit);
									shippingPointNameSet.add(b2bunitSippingPoint);
								}
							}

							/*-for (final String shippingPointName : shippingPointNameSet)
							{
								fieldValues.add(new FieldValue(fieldName, shippingPointName));
								LOG.debug("Field Name :" + fieldName + " Shipping Point Name : " + shippingPointName);
							}*/

						}
					}
				}

				for (final String shippingPointName : shippingPointNameSet)
				{
					fieldValues.add(new FieldValue(fieldName, shippingPointName));
					LOG.debug("Field Name :" + fieldName + " Shipping Point Name : " + shippingPointName);
				}

			}
		}
		catch (final Exception ex)
		{
			LOG.error("Exception Occured " + ex.getMessage() + ex.getStackTrace(), ex);
		}
		finally
		{
			LOG.debug("Created the B2Bunit");
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
	 * @return the energizerProductService
	 */
	public EnergizerProductService getEnergizerProductService()
	{
		return energizerProductService;
	}

	/**
	 * @param energizerProductService
	 *                                   the energizerProductService to set
	 */
	@Required
	public void setEnergizerProductService(final EnergizerProductService energizerProductService)
	{
		this.energizerProductService = energizerProductService;
	}


}
