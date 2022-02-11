/**
 *
 */
package com.energizer.core.event;

import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.commerceservices.enums.SiteChannel;
import de.hybris.platform.commerceservices.event.AbstractSiteEventListener;
import de.hybris.platform.commerceservices.model.process.EmployeeForgottenPasswordProcessModel;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.util.ServicesUtil;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;


/**
 * @author Srivenkata_N
 *
 */
public class B2BEmployeeForgottenPwdEventListener extends AbstractSiteEventListener<B2BEmployeeForgottenPwdEvent>
{
	private static final Logger LOG = Logger.getLogger(B2BEmployeeForgottenPwdEventListener.class);
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
	protected void onSiteEvent(final B2BEmployeeForgottenPwdEvent b2bEmployeeForgottenPwdEvent)
	{
		try
		{

			final EmployeeForgottenPasswordProcessModel employeeForgottenPasswordProcessModel = (EmployeeForgottenPasswordProcessModel) getBusinessProcessService()
					.createProcess("b2bEmployeeForgottenPassword" + "-" + b2bEmployeeForgottenPwdEvent.getSalesRepUser().getUid() + "-"
							+ System.currentTimeMillis(), "b2bEmployeeForgottenPasswordEmailProcess");
			employeeForgottenPasswordProcessModel.setSite(b2bEmployeeForgottenPwdEvent.getSite());
			employeeForgottenPasswordProcessModel.setEmployee(b2bEmployeeForgottenPwdEvent.getSalesRepUser());
			employeeForgottenPasswordProcessModel.setToken(b2bEmployeeForgottenPwdEvent.getToken());
			employeeForgottenPasswordProcessModel.setLanguage(b2bEmployeeForgottenPwdEvent.getLanguage());
			employeeForgottenPasswordProcessModel.setCurrency(b2bEmployeeForgottenPwdEvent.getCurrency());
			employeeForgottenPasswordProcessModel.setStore(b2bEmployeeForgottenPwdEvent.getBaseStore());
			getModelService().save(employeeForgottenPasswordProcessModel);
			getBusinessProcessService().startProcess(employeeForgottenPasswordProcessModel);

	}
		catch (final Exception ex)
		{
			LOG.info("Exception occured in Listner:::: " + ex.getMessage());
			ex.printStackTrace();
		}
	}
	@Override
	protected boolean shouldHandleEvent(final B2BEmployeeForgottenPwdEvent event)
	{
		final BaseSiteModel site = event.getSite();
		ServicesUtil.validateParameterNotNullStandardMessage("event.order.site", site);
		return SiteChannel.B2B.equals(site.getChannel());
	}
}
