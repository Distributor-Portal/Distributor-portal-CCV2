/**
 *
 */
package com.energizer.core.solr.query.impl;

import de.hybris.platform.commerceservices.search.solrfacetsearch.querybuilder.impl.AbstractFreeTextQueryBuilder;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.search.RawQuery;
import de.hybris.platform.solrfacetsearch.search.SearchQuery;
import de.hybris.platform.solrfacetsearch.search.SearchQuery.Operator;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.springframework.beans.factory.annotation.Required;


/**
 * @author kaki.rajasekhar
 *
 */
public class DefaultEnergizerFreeTextQueryBuilder extends AbstractFreeTextQueryBuilder
{
	final Logger LOG = Logger.getLogger(DefaultEnergizerFreeTextQueryBuilder.class);

	@Resource
	private SessionService sessionService;

	@Resource(name = "configurationService")
	private ConfigurationService configurationService;

	private String propertyName;
	private int boost;

	protected String getPropertyName()
	{
		return propertyName;
	}

	@Required
	public void setPropertyName(final String propertyName)
	{
		this.propertyName = propertyName;
	}

	protected int getBoost()
	{
		return boost;
	}

	@Required
	public void setBoost(final int boost)
	{
		this.boost = boost;
	}

	@Override
	public void addFreeTextQuery(final SearchQuery searchQuery, final String fullText, final String[] textWords)
	{
		final IndexedType indexedType = searchQuery.getIndexedType();
		if (indexedType != null)
		{
			final IndexedProperty indexedProperty = indexedType.getIndexedProperties().get(getPropertyName());
			if (indexedProperty != null)
			{
				addFreeTextQuery(searchQuery, indexedProperty, fullText, textWords, getBoost());
			}
		}
	}

	@Override
	@SuppressWarnings("deprecation")
	protected void addFreeTextQuery(final SearchQuery searchQuery, final String field, String value, final String suffixOp,
			final double boost)
	{

		RawQuery rawQuery = null;
		/*
		 * First we are preparing solr text query for code or ean. If search text match with any product code or ean
		 * number , we are displaying results into the storefront. If not match with any product code or ean number it
		 * will go to else condition and again preparing query for remaining property names.
		 */
		/*
		 * We are set the session attribute TRUE when user search any Text, by default it is false we are added session
		 * attribute those class : DefaultEnergizerSolrProductSearchService.java &&
		 * StorefrontAuthenticationSuccessHandler.java
		 */
		if ((boolean) sessionService.getAttribute("solrFreeTextEnable"))
		{
			if (null != field && (field.equalsIgnoreCase("code") || field.equalsIgnoreCase("ean")))
			{
				rawQuery = new RawQuery(field, ClientUtils.escapeQueryChars(value.toUpperCase()) + "^" + boost, Operator.OR);
			}

		}
		else
		{
				if (!value.contains("*"))
				{
					value = "*".concat(value).concat("*");
				}
				if (null != field && (field.equalsIgnoreCase("code") || field.equalsIgnoreCase("ean"))
						&& suffixOp.equalsIgnoreCase("*"))
				{
					rawQuery = new RawQuery(field,
							"*" + ClientUtils.escapeQueryChars(value.replaceAll("[*@#]", "").toUpperCase()) + suffixOp + "^" + boost,
							Operator.OR);
				}
				else
				{
					rawQuery = new RawQuery(field, ClientUtils.escapeQueryChars(value) + suffixOp + "^" + boost, Operator.OR);
				}

		}
		//final RawQuery rawQuery = new RawQuery(field, ClientUtils.escapeQueryChars(value) + suffixOp + "^" + boost, Operator.OR);
		if (rawQuery != null)
		{
		searchQuery.addRawQuery(rawQuery);
		}
	}

}
