/**
 * 
 */
package com.energizer.core.business;

import de.hybris.platform.commercefacades.order.data.AbstractOrderData;

import java.util.List;

import com.energizer.business.BusinessRuleError;




/**
 * @author kaushik.ganguly
 * 
 */
public interface EnergizerOrderBusinessRulesValidator
{
	public <T extends AbstractOrderData> void validate(T orderData);

	public Boolean hasErrors();

	public List<BusinessRuleError> getErrors();

	public void addError(BusinessRuleError error);
}
