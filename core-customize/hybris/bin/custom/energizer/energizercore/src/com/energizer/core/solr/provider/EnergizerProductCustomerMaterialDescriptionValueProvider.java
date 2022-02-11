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

import de.hybris.platform.core.model.c2l.LanguageModel;
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
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.energizer.core.model.EnergizerCMIRModel;
import com.energizer.core.model.EnergizerPriceRowModel;
import com.energizer.core.model.EnergizerProductModel;
import com.energizer.services.product.EnergizerProductService;


/**
 * This class provides customer material description for the solr indexing
 *
 * @author kaushik.ganguly
 */
public class EnergizerProductCustomerMaterialDescriptionValueProvider extends AbstractPropertyFieldValueProvider
		implements FieldValueProvider
{
	private FieldNameProvider fieldNameProvider;
	private EnergizerProductService energizerProductService;
	private static final Logger LOG = Logger.getLogger(EnergizerProductCustomerMaterialDescriptionValueProvider.class);

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
				boolean skipPriceRowLoop = false;
				for (final PriceRowModel unit : energizerProduct.getEurope1Prices())
				{
					if (unit instanceof EnergizerPriceRowModel && ((EnergizerPriceRowModel) unit).getB2bUnit() != null)
					{


						final EnergizerPriceRowModel enrPriceRowModel = (EnergizerPriceRowModel) unit;
						LOG.debug(energizerProduct.getCode() + "enrPriceRowModel.getIsActive() = " + enrPriceRowModel.getIsActive());

						if (!enrPriceRowModel.getIsActive())
						{
							LOG.debug(energizerProduct.getCode() + "PRICE ROW NOT ACTIVE...");
							continue;
						}
						final List<EnergizerCMIRModel> cmirList = energizerProductService
								.getEnergizerCMIRList(energizerProduct.getCode());
						for (final EnergizerCMIRModel cmir : cmirList)
						{
							if (!cmir.getIsActive())
							{
								LOG.debug(energizerProduct.getCode() + "CMIR NOT ACTIVE..");
								continue;
							}

							if (indexedProperty.isLocalized())
							{
								final Collection<LanguageModel> languages = indexConfig.getLanguages();

								for (final LanguageModel language : languages)
								{
									final Collection<String> names = getFieldNameProvider().getFieldNames(indexedProperty,
											(language == null) ? null : language.getIsocode());

									for (final String fieldN : names)
									{
										if (cmir.getCustomerMaterialDescription(new Locale(language.getIsocode())) != null)
										{
											fieldValues.add(new FieldValue(fieldN,
													cmir.getCustomerMaterialDescription(new Locale(language.getIsocode())).toLowerCase()));
										}
										if (cmir.getCustomerMaterialId() != null)
										{
											fieldValues.add(new FieldValue(fieldN, cmir.getCustomerMaterialId().toLowerCase()));
										}
										if (energizerProduct.getCode() != null)
										{
											fieldValues.add(new FieldValue(fieldN, energizerProduct.getCode()));
										}
										if (energizerProduct.getCode() != null)
										{
											fieldValues.add(new FieldValue(fieldN, energizerProduct.getCode().toLowerCase()));
										}
										if (energizerProduct.getName(new Locale(language.getIsocode())) != null)
										{
											fieldValues.add(new FieldValue(fieldN,
													energizerProduct.getName(new Locale(language.getIsocode())).toLowerCase()));
										}

										LOG.debug(
												"Family added  :" + cmir.getCustomerMaterialDescription(new Locale(language.getIsocode())));
									}

								}
							}

							if (null != cmir.getCustomerMaterialId() && StringUtils.isNotEmpty(cmir.getCustomerMaterialId())
									&& null != cmir.getCustomerMaterialDescription()
									&& StringUtils.isNotEmpty(cmir.getCustomerMaterialDescription()))
							{
								skipPriceRowLoop = true;
								break;
							}
						}
					}
					if (skipPriceRowLoop)
					{
						break;
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
	 *           the energizerProductService to set
	 */
	@Required
	public void setEnergizerProductService(final EnergizerProductService energizerProductService)
	{
		this.energizerProductService = energizerProductService;
	}


}
