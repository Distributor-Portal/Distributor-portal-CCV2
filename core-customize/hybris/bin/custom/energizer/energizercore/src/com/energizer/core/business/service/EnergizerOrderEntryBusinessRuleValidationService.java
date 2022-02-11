/**
 * 
 */
package com.energizer.core.business.service;

import de.hybris.platform.commercefacades.order.data.OrderEntryData;

import java.util.List;

import com.energizer.business.BusinessRuleError;


/**
 * @author kaushik.ganguly
 * 
 */
public interface EnergizerOrderEntryBusinessRuleValidationService
{
	public void validateBusinessRules(OrderEntryData orderEntryData);

	public Boolean hasErrors();

	public List<BusinessRuleError> getErrors();

	public List<BusinessRuleError> getTempErrors();

	public void clearErrors();

}
