/**
 * 
 */
package com.energizer.core.business.service.impl;

import de.hybris.platform.commercefacades.order.data.OrderEntryData;

import java.util.ArrayList;
import java.util.List;

import com.energizer.business.BusinessRuleError;
import com.energizer.core.business.EnergizerOrderEntryBusinessRulesValidator;
import com.energizer.core.business.service.EnergizerOrderEntryBusinessRuleValidationService;


/**
 * @author kaushik.ganguly
 * 
 */
public class DefaultEnergizerOrderEntryBusinessRuleValidationService implements EnergizerOrderEntryBusinessRuleValidationService
{

	List<EnergizerOrderEntryBusinessRulesValidator> validators;
	List<BusinessRuleError> errors = new ArrayList<BusinessRuleError>();
	List<BusinessRuleError> tempErrors;


	/*
	 * (non-Javadoc)
	 * 
	 * @see com.energizer.core.business.service.EnergizerOrderEntryBusinessRuleValidationService#hasErrors()
	 */
	@Override
	public Boolean hasErrors()
	{
		// YTODO Auto-generated method stub
		return tempErrors != null && tempErrors.size() > 0;
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
	public List<EnergizerOrderEntryBusinessRulesValidator> getValidators()
	{
		return validators;
	}

	/**
	 * @param validators
	 *           the validators to set
	 */
	public void setValidators(final List<EnergizerOrderEntryBusinessRulesValidator> validators)
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
	public void validateBusinessRules(final OrderEntryData orderEntryData)
	{
		try
		{
		errors.clear();
		if (hasErrors())
		{
			tempErrors.clear();
		}

		if (validators != null && validators.size() > 0)
		{
			for (final EnergizerOrderEntryBusinessRulesValidator rule : validators)
			{
				rule.validate(orderEntryData);

				if (rule.getErrors() != null && rule.getErrors().size() > 0)
				{
					tempErrors = new ArrayList<BusinessRuleError>(rule.getErrors());
					errors.addAll(tempErrors);
					rule.getErrors().clear();
				}
			}
		}
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.energizer.core.business.service.EnergizerOrderEntryBusinessRuleValidationService#getTempErrors()
	 */
	@Override
	public List<BusinessRuleError> getTempErrors()
	{
		// YTODO Auto-generated method stub
		return tempErrors;
	}

	public void clearErrors()
	{
		errors.clear();
	}


}
