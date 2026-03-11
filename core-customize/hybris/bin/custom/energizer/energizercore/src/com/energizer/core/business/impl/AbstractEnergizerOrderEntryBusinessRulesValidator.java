/**
 *
 */
package com.energizer.core.business.impl;

import de.hybris.platform.cms2.model.site.CMSSiteModel;
import de.hybris.platform.cms2.servicelayer.services.CMSSiteService;
import de.hybris.platform.commercefacades.order.data.OrderEntryData;
import de.hybris.platform.servicelayer.exceptions.ModelNotFoundException;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.ModelAttribute;

import com.energizer.business.BusinessRuleError;
import com.energizer.core.business.EnergizerOrderEntryBusinessRulesValidator;



/**
 * @author kaushik.ganguly
 *
 */
public abstract class AbstractEnergizerOrderEntryBusinessRulesValidator implements EnergizerOrderEntryBusinessRulesValidator
{
	List<BusinessRuleError> errors = new ArrayList<BusinessRuleError>();

	@Resource(name = "cmsSiteService")
	private CMSSiteService cmsSiteService;

	/*
	 * (non-Javadoc)
	 *
	 * @see com.energizer.core.business.EnergizerBusinessRulesValidator#validate()
	 */
	@Override
	public void validate(final OrderEntryData orderEntryData) throws ModelNotFoundException, Exception
	{

	}

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
	 * @see com.energizer.core.business.EnergizerBusinessRulesValidator#addError(com.energizer.business.BusinessRuleError)
	 */
	@Override
	public void addError(final BusinessRuleError error)
	{
		// YTODO Auto-generated method stub
		if (error != null)
		{
			//errors = new ArrayList<BusinessRuleError>();
			errors.add(error);
		}
		//errors.add(error);
	}

	@ModelAttribute("siteUid")
	public String getSiteUid()
	{
		final CMSSiteModel site = cmsSiteService.getCurrentSite();
		return site != null ? site.getUid() : "";
	}

}
