/**
 *
 */
package com.energizer.core.event;

import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.commerceservices.enums.SiteChannel;
import de.hybris.platform.commerceservices.event.AbstractSiteEventListener;
import de.hybris.platform.commerceservices.model.process.EmployeeUnlockAccountProcessModel;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.util.ServicesUtil;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;


/**
 * @author Srivenkata_N
 *
 */
public class B2BEmployeeUnlockAccountEventListener extends AbstractSiteEventListener<B2BEmployeeUnlockAccountEvent>
{
	private static final Logger LOG = Logger.getLogger(B2BEmployeeUnlockAccountEventListener.class);
	private BusinessProcessService businessProcessService;
	private ModelService modelService;

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
	protected void onSiteEvent(final B2BEmployeeUnlockAccountEvent b2bEmployeeUnlockAccountEvent)
	{
		try
		{

			final EmployeeUnlockAccountProcessModel employeeUnlockAccountProcessModel = (EmployeeUnlockAccountProcessModel) getBusinessProcessService()
					.createProcess("b2bEmployeeUnlockAccount" + "-" + b2bEmployeeUnlockAccountEvent.getSalesRepUser().getUid()
							+ "-" + System.currentTimeMillis(), "b2bEmployeeUnlockAccountEmailProcess");
			employeeUnlockAccountProcessModel.setSite(b2bEmployeeUnlockAccountEvent.getSite());
			employeeUnlockAccountProcessModel.setEmployee(b2bEmployeeUnlockAccountEvent.getSalesRepUser());
			employeeUnlockAccountProcessModel.setToken(b2bEmployeeUnlockAccountEvent.getToken());
			employeeUnlockAccountProcessModel.setLanguage(b2bEmployeeUnlockAccountEvent.getLanguage());
			employeeUnlockAccountProcessModel.setCurrency(b2bEmployeeUnlockAccountEvent.getCurrency());
			employeeUnlockAccountProcessModel.setStore(b2bEmployeeUnlockAccountEvent.getBaseStore());
			getModelService().save(employeeUnlockAccountProcessModel);
			getBusinessProcessService().startProcess(employeeUnlockAccountProcessModel);

		}
		catch (final Exception ex)
		{
			LOG.info("Exception occured in Listner:::: " + ex.getMessage());
			ex.printStackTrace();
		}
	}

	@Override
	protected boolean shouldHandleEvent(final B2BEmployeeUnlockAccountEvent event)
	{
		final BaseSiteModel site = event.getSite();
		ServicesUtil.validateParameterNotNullStandardMessage("event.order.site", site);
		return SiteChannel.B2B.equals(site.getChannel());
	}
}
