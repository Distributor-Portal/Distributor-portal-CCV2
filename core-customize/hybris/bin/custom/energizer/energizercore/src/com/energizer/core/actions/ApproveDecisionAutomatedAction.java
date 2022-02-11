package com.energizer.core.actions;

import de.hybris.platform.b2b.enums.PermissionStatus;
import de.hybris.platform.b2b.process.approval.model.B2BApprovalProcessModel;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.link.LinkModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.security.PrincipalModel;
import de.hybris.platform.jalo.JaloBusinessException;
import de.hybris.platform.jalo.JaloInvalidParameterException;
import de.hybris.platform.jalo.link.Link;
import de.hybris.platform.jalo.security.JaloSecurityException;
import de.hybris.platform.servicelayer.event.EventService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.workflow.WorkflowProcessingService;
import de.hybris.platform.workflow.model.WorkflowActionModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.PredicateUtils;
import org.apache.log4j.Logger;
import org.springframework.util.Assert;


// Referenced classes of package de.hybris.platform.b2b.process.approval.actions:
//            B2BAbstractWorkflowAutomatedAction

public class ApproveDecisionAutomatedAction extends B2BAbstractWorkflowAutomatedAction
{

	private static final Logger LOG = Logger.getLogger(ApproveDecisionAutomatedAction.class);
	private EventService eventService;
	@Resource
	private ModelService modelService;
	@Resource
	private FlexibleSearchService flexibleSearchService;
	@Resource
	private WorkflowProcessingService workflowProcessingService;

	@Override
	public void performAction(final WorkflowActionModel action)
	{
		OrderModel order = null;
		try
		{
			final PrincipalModel principalAssigned = action.getPrincipalAssigned();
			final B2BApprovalProcessModel process = (B2BApprovalProcessModel) CollectionUtils.find(action.getAttachmentItems(),
					PredicateUtils.instanceofPredicate(B2BApprovalProcessModel.class));
			Assert.notNull(process, String.format("Process attachment missing for action %s", new Object[]
			{ action.getCode() }));
			final List<WorkflowActionModel> actions = action.getWorkflow().getActions();
			for (final WorkflowActionModel _action : actions)
			{
				LOG.info("Principal Assigned to %s action is %s " + new Object[]
				{ _action.getName(), _action.getPrincipalAssigned().getUid() });
				//activate all the action in the work flow so that they will be moved to next step.
				// When one approver approves the orders, others approvers ' action will be triggered. 
				activateLinks(_action);
			}
			order = process.getOrder();
			updatePermissionResultsStatus(order, principalAssigned, PermissionStatus.APPROVED);
		}
		catch (final Exception e)
		{
			LOG.error(e.getMessage(), e);
			if (order != null)
			{
				order.setStatus(OrderStatus.B2B_PROCESSING_ERROR);
				getModelService().save(order);
			}
		}
	}

	/**
	 * @param action
	 */
	private void activateLinks(final WorkflowActionModel action)
	{
		final Map<String, WorkflowActionModel> params = new HashMap<String, WorkflowActionModel>();
		params.put("act", action);
		final SearchResult<LinkModel> res = flexibleSearchService.search(
				(new StringBuilder("SELECT {pk} from {"))
						.append(de.hybris.platform.workflow.constants.GeneratedWorkflowConstants.Relations.WORKFLOWACTIONLINKRELATION)
						.append("} where {").append("target").append("}=?act").toString(), params);
		final List<LinkModel> links = res.getResult();
		for (final LinkModel link : links)
		{
			final Boolean andconnection = (Boolean) getAttributeForLink(link, "andconnection");
			final Boolean active = (Boolean) getAttributeForLink(link, "active");
			if (andconnection && active == null)
			{
				setAttributeForLink(link, "active", Boolean.TRUE);
			}
		}
	}

	private Object getAttributeForLink(final LinkModel link, final String attribute)
	{
		final Link linkSource = (Link) modelService.getSource(link);
		try
		{
			return linkSource.getAttribute(attribute);
		}
		catch (JaloInvalidParameterException | JaloSecurityException e)
		{
			// YTODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

	}

	private void setAttributeForLink(final LinkModel link, final String attribute, final Boolean value)
	{
		final Link linkSource = (Link) modelService.getSource(link);
		try
		{
			linkSource.setAttribute(attribute, value);
		}
		catch (final JaloInvalidParameterException e)
		{
			LOG.error("Jalo invalid parameter exception", e);
		}
		catch (final JaloSecurityException e)
		{
			LOG.error("Jalo security attribute exception", e);
		}
		catch (final JaloBusinessException e)
		{
			LOG.error("Jalo business attribute exception", e);
		}
	}

	protected EventService getEventService()
	{
		return eventService;
	}

	public void setEventService(final EventService eventService)
	{
		this.eventService = eventService;
	}
}