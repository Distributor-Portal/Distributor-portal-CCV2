/**
 *
 */
package com.energizer.core.business;

import de.hybris.platform.commercefacades.order.data.OrderEntryData;
import de.hybris.platform.servicelayer.exceptions.ModelNotFoundException;

import java.util.List;

import com.energizer.business.BusinessRuleError;




/**
 * @author kaushik.ganguly
 *
 */
public interface EnergizerOrderEntryBusinessRulesValidator
{
	public void validate(OrderEntryData orderEntryData) throws ModelNotFoundException, Exception;

	public Boolean hasErrors();

	public List<BusinessRuleError> getErrors();

	public void addError(BusinessRuleError error);
}
