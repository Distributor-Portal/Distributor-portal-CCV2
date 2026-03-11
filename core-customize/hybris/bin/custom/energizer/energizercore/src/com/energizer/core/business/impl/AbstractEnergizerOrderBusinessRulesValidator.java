/**
 * 
 */
package com.energizer.core.business.impl;

import de.hybris.platform.commercefacades.order.data.AbstractOrderData;

import java.util.ArrayList;
import java.util.List;

import com.energizer.business.BusinessRuleError;
import com.energizer.core.business.EnergizerOrderBusinessRulesValidator;



/**
 * @author kaushik.ganguly
 * 
 */
public abstract class AbstractEnergizerOrderBusinessRulesValidator implements EnergizerOrderBusinessRulesValidator
{
	List<BusinessRuleError> errors;


	/*
	 * (non-Javadoc)
	 * 
	 * @see com.energizer.core.business.EnergizerBusinessRulesValidator#hasErrors()
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
	 * @see com.energizer.core.business.EnergizerBusinessRulesValidator#getErrors()
	 */
	@Override
	public List<BusinessRuleError> getErrors()
	{
		// YTODO Auto-generated method stub
		return (errors != null) ? errors : null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.energizer.core.business.EnergizerBusinessRulesValidator#addError(com.energizer.business.BusinessRuleError)
	 */
	@Override
	public void addError(final BusinessRuleError error)
	{
		// YTODO Auto-generated method stub
		if (errors == null)
		{
			errors = new ArrayList<BusinessRuleError>();
		}
		errors.add(error);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.energizer.core.business.EnergizerOrderBusinessRulesValidator#validate(de.hybris.platform.commercefacades.order
	 * .data.AbstractOrderData)
	 */
	@Override
	public <T extends AbstractOrderData> void validate(final T orderData)
	{
		// YTODO Auto-generated method stub

	}






}
