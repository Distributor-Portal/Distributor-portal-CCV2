/**
 * 
 */
package com.energizer.core.business.service;

import de.hybris.platform.commercefacades.order.data.AbstractOrderData;

import java.util.List;

import com.energizer.business.BusinessRuleError;


/**
 * @author kaushik.ganguly
 * 
 */
public interface EnergizerOrderBusinessRuleValidationService
{
	public <T extends AbstractOrderData> void validateBusinessRules(T orderData);

	public Boolean hasErrors();

	public List<BusinessRuleError> getErrors();

}
