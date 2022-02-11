package com.energizer.core.actions;

import de.hybris.platform.b2b.enums.PermissionStatus;
import de.hybris.platform.b2b.model.B2BPermissionResultModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.security.PrincipalModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.workflow.jobs.AutomatedWorkflowTemplateJob;
import de.hybris.platform.workflow.model.WorkflowActionModel;
import de.hybris.platform.workflow.model.WorkflowDecisionModel;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import org.apache.log4j.Logger;


public abstract class B2BAbstractWorkflowAutomatedAction implements AutomatedWorkflowTemplateJob
{

	private static final Logger LOG = Logger.getLogger(B2BAbstractWorkflowAutomatedAction.class);
	private ModelService modelService;

	public final WorkflowDecisionModel perform(final WorkflowActionModel action)
	{
		performAction(action);
		final Iterator iterator = action.getDecisions().iterator();
		if (iterator.hasNext())
		{
			final WorkflowDecisionModel decision = (WorkflowDecisionModel) iterator.next();
			return decision;
		}
		else
		{
			return null;
		}
	}

	public abstract void performAction(WorkflowActionModel workflowactionmodel);

	protected void updatePermissionResultsStatus(final OrderModel order, final PrincipalModel principalAssigned,
			final PermissionStatus status)
	{
		final Collection<B2BPermissionResultModel> permissionResults = (order.getPermissionResults() == null ? ((Collection) (Collections
				.emptyList())) : order.getPermissionResults());
		for (final B2BPermissionResultModel b2bPermissionResultModel : permissionResults)
		{
			b2bPermissionResultModel.setStatus(status);
		}

		getModelService().saveAll(permissionResults);
	}

	protected ModelService getModelService()
	{
		return modelService;
	}

	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

}