/**
 * 
 */
package com.energizer.core.business.service.impl;

import de.hybris.platform.commercefacades.order.data.AbstractOrderData;

import java.util.ArrayList;
import java.util.List;

import com.energizer.business.BusinessRuleError;
import com.energizer.core.business.EnergizerOrderBusinessRulesValidator;
import com.energizer.core.business.service.EnergizerOrderBusinessRuleValidationService;


/**
 * @author kaushik.ganguly
 * 
 */
public class DefaultEnergizerOrderBusinessRuleValidationService implements EnergizerOrderBusinessRuleValidationService
{

	List<EnergizerOrderBusinessRulesValidator> validators;
	List<BusinessRuleError> errors;


	/*
	 * (non-Javadoc)
	 * 
	 * @see com.energizer.core.business.service.EnergizerOrderEntryBusinessRuleValidationService#hasErrors()
	 */
	@Override
	public Boolean hasErrors()
	{
		// YTODO Auto-generated method stub
		return errors != null && errors.size() > 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.energizer.core.business.service.EnergizerOrderEntryBusinessRuleValidationService#getErrors()
	 */
	@Override
	public List<BusinessRuleError> getErrors()
	{
		// YTODO Auto-generated method stub
		return (errors != null) ? errors : null;
	}

	/**
	 * @return the validators
	 */
	public List<EnergizerOrderBusinessRulesValidator> getValidators()
	{
		return validators;
	}

	/**
	 * @param validators
	 *           the validators to set
	 */
	public void setValidators(final List<EnergizerOrderBusinessRulesValidator> validators)
	{
		this.validators = validators;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.energizer.core.business.service.EnergizerOrderEntryBusinessRuleValidationService#validateBusinessRules(de.
	 * hybris.platform.commercefacades.order.data.OrderEntryData)
	 */
	@Override
	public <T extends AbstractOrderData> void validateBusinessRules(final T orderData)
	{
		errors = null;
		// YTODO Auto-generated method stub
		if (validators != null && validators.size() > 0)
		{
			for (final EnergizerOrderBusinessRulesValidator rule : validators)
			{
				rule.validate(orderData);
				if (rule.hasErrors())
				{
					if (errors != null)
					{
						errors.addAll(rule.getErrors());
					}
					else
					{
						errors = new ArrayList<BusinessRuleError>(rule.getErrors());
					}
				}
			}
		}

	}


}
