/**
 * 
 */
package com.energizer.core.business.impl;

import de.hybris.platform.commercefacades.order.data.AbstractOrderData;
import de.hybris.platform.util.localization.Localization;

import javax.annotation.Resource;

import com.energizer.business.BusinessRuleError;
import com.energizer.core.model.EnergizerB2BUnitModel;
import com.energizer.core.solr.query.EnergizerSolrQueryManipulationService;


/**
 * @author kaushik.ganguly
 * 
 */
public class MinimumOrderValueEnergizerBusinessRuleValidator extends AbstractEnergizerOrderBusinessRulesValidator
{

	@Resource(name = "energizerSolrQueryManipulationService")
	private EnergizerSolrQueryManipulationService energizerSolrQueryManipulationService;

	private static final String ORDER_VALUE_NOT_MET = "b2bunit.ordervalue.error";

	@Override
	public <T extends AbstractOrderData> void validate(final T orderData)
	{
		// YTODO Auto-generated method stub
		if (hasErrors())
		{
			errors.clear();
		}
		final EnergizerB2BUnitModel b2bUnit = energizerSolrQueryManipulationService.getB2BUnitForLoggedInUser();
		if (b2bUnit != null && b2bUnit.getMinimumOrderValue() != null
				&& b2bUnit.getMinimumOrderValue().compareTo(orderData.getTotalPrice().getValue()) != -1)
		{
			final BusinessRuleError error = new BusinessRuleError();
			error.setMessage(Localization.getLocalizedString(ORDER_VALUE_NOT_MET) + b2bUnit.getCurrencyPreference().getSymbol()
					+ b2bUnit.getMinimumOrderValue().toString());
			//error.setType(this.getClass().getName());
			addError(error);
		}


	}
}
