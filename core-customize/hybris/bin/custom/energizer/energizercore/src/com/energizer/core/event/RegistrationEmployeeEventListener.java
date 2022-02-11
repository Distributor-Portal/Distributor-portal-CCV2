/**
 *
 */
package com.energizer.core.event;

import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.commerceservices.enums.SiteChannel;
import de.hybris.platform.commerceservices.event.AbstractSiteEventListener;
import de.hybris.platform.commerceservices.model.process.EmployeeRegistrationProcessModel;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.util.ServicesUtil;
import de.hybris.platform.site.BaseSiteService;

import java.util.Collection;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Required;


/**
 * @author Srivenkata_N
 *
 */
public class RegistrationEmployeeEventListener extends AbstractSiteEventListener<RegisterEmployeeEvent>
{

	private ModelService modelService;
	private BusinessProcessService businessProcessService;
	@Resource(name = "baseSiteService")
	private BaseSiteService baseSiteService;

	protected BusinessProcessService getBusinessProcessService()
	{
		return businessProcessService;
	}

	@Required
	public void setBusinessProcessService(final BusinessProcessService businessProcessService)
	{
		this.businessProcessService = businessProcessService;
	}

	protected ModelService getModelService()
	{
		return modelService;
	}

	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	@Override
	protected void onSiteEvent(final RegisterEmployeeEvent registerEmployeeEvent)
	{
		final EmployeeRegistrationProcessModel employeeRegistrationProcess = (EmployeeRegistrationProcessModel) getBusinessProcessService()
				.createProcess("b2bEmployeeRegistration" + "-" + registerEmployeeEvent.getSalesRepUser().getUid() + "-"
						+ System.currentTimeMillis(), "b2bEmployeeRegistrationEmailProcess");
		employeeRegistrationProcess.setSite(registerEmployeeEvent.getSite());
		employeeRegistrationProcess.setEmployee(registerEmployeeEvent.getSalesRepUser());
		employeeRegistrationProcess.setLanguage(registerEmployeeEvent.getLanguage());
		employeeRegistrationProcess.setCurrency(registerEmployeeEvent.getCurrency());
		employeeRegistrationProcess.setStore(registerEmployeeEvent.getBaseStore());
		getModelService().save(employeeRegistrationProcess);
		getBusinessProcessService().startProcess(employeeRegistrationProcess);
	}

	@Override
	protected boolean shouldHandleEvent(final RegisterEmployeeEvent event)
	{
		/*
		 * final BaseSiteModel site = baseSiteService.getBaseSiteForUID("personalCare"); site.setChannel(SiteChannel.B2B);
		 * event.setSite(site); ServicesUtil.validateParameterNotNullStandardMessage("event.order.site", site); return
		 * SiteChannel.B2B.equals(site.getChannel());
		 */


		//		final BaseSiteModel site = baseSiteService.getBaseSiteForUID("personalCare");
		//		final BaseSiteModel site = baseSiteService.getCurrentBaseSite();
		boolean siteFlag = false;
		final Collection<BaseSiteModel> allSite = baseSiteService.getAllBaseSites();
		for (final BaseSiteModel baseSiteModel : allSite)
		{
			baseSiteModel.setChannel(SiteChannel.B2B);
			event.setSite(baseSiteModel);
			ServicesUtil.validateParameterNotNullStandardMessage("event.order.site", baseSiteModel);
			siteFlag = SiteChannel.B2B.equals(baseSiteModel.getChannel());
		}
		return siteFlag;
	}

}
