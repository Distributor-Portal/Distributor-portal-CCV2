/**
 *
 */
package com.energizer.core.actions;

import de.hybris.platform.b2b.enums.PermissionStatus;
import de.hybris.platform.b2b.enums.WorkflowTemplateType;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.b2b.model.B2BPermissionResultModel;
import de.hybris.platform.b2b.process.approval.actions.AbstractProceduralB2BOrderAproveAction;
import de.hybris.platform.b2b.process.approval.actions.B2BPermissionResultHelperImpl;
import de.hybris.platform.b2b.process.approval.model.B2BApprovalProcessModel;
import de.hybris.platform.b2b.services.B2BWorkflowIntegrationService;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.workflow.WorkflowProcessingService;
import de.hybris.platform.workflow.WorkflowService;
import de.hybris.platform.workflow.model.WorkflowModel;
import de.hybris.platform.workflow.model.WorkflowTemplateModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;


/**
 * @author M1030110
 *
 */
public class StartWorkFlowForApprovers extends AbstractProceduralB2BOrderAproveAction
{

	private static final Logger LOG = Logger.getLogger(StartWorkFlowForApprovers.class);

	private B2BWorkflowIntegrationService b2bWorkflowIntegrationService;
	private WorkflowProcessingService workflowProcessingService;
	private WorkflowService workflowService;
	private B2BPermissionResultHelperImpl permissionResultHelper;

	@Override
	public void executeAction(final B2BApprovalProcessModel aprovalProcess)
	{
		try
		{
			final OrderModel order = aprovalProcess.getOrder();
			LOG.info("Order Number : " + order.getCode());
			final Collection<B2BPermissionResultModel> b2bPermissionResults = order.getPermissionResults();
			final List<B2BCustomerModel> approvers = permissionResultHelper.getApproversWithPermissionStatus(b2bPermissionResults,
					PermissionStatus.PENDING_APPROVAL);

			if (LOG.isDebugEnabled())
			{
				final List<String> approverUids = new ArrayList<String>();
				B2BCustomerModel b2bCustomerModel;
				for (final Iterator iterator = approvers.iterator(); iterator.hasNext(); approverUids.add(b2bCustomerModel.getUid()))
				{
					b2bCustomerModel = (B2BCustomerModel) iterator.next();
				}

				LOG.debug(String.format("Creating a worflow for order %s and approvers %s", new Object[]
				{ order.getCode(), approverUids }));
			}
			final String workflowTemplateCode = generateWorkflowTemplateCode("B2B_APPROVAL_WORKFLOW", approvers);
			final WorkflowTemplateModel workflowTemplate = b2bWorkflowIntegrationService.createWorkflowTemplate(approvers,
					workflowTemplateCode, "Generated B2B Order Approval Workflow", WorkflowTemplateType.ORDER_APPROVAL);
			final WorkflowModel workflow = getWorkflowService().createWorkflow(workflowTemplate.getName(), workflowTemplate,
					Collections.<ItemModel> singletonList(aprovalProcess), workflowTemplate.getOwner());
			workflowProcessingService.startWorkflow(workflow);
			modelService.saveAll();
			order.setWorkflow(workflow);
			order.setStatus(OrderStatus.PENDING_APPROVAL);
			order.setExhaustedApprovers(new HashSet(approvers));
			modelService.save(order);


		}
		catch (final Exception e)
		{
			LOG.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public void setB2bWorkflowIntegrationService(final B2BWorkflowIntegrationService b2bWorkflowIntegrationService)
	{
		this.b2bWorkflowIntegrationService = b2bWorkflowIntegrationService;
	}



	public void setPermissionResultHelper(final B2BPermissionResultHelperImpl permissionResultHelper)
	{
		this.permissionResultHelper = permissionResultHelper;
	}

	public void setWorkflowProcessingService(final WorkflowProcessingService workflowProcessingService)
	{
		this.workflowProcessingService = workflowProcessingService;
	}

	public void setWorkflowService(final WorkflowService workflowService)
	{
		this.workflowService = workflowService;
	}

	protected WorkflowService getWorkflowService()
	{
		return workflowService;
	}

	public B2BWorkflowIntegrationService getB2bWorkflowIntegrationService()
	{
		return b2bWorkflowIntegrationService;
	}

	public WorkflowProcessingService getWorkflowProcessingService()
	{
		return workflowProcessingService;
	}

	public B2BPermissionResultHelperImpl getPermissionResultHelper()
	{
		return permissionResultHelper;
	}

	public String generateWorkflowTemplateCode(final String prefix, final List<B2BCustomerModel> users)
	{
		final StringBuffer id = new StringBuffer(prefix);
		String userUID = null;
		String userPK = null;
		final Collection<String> uidCollection = new ArrayList<String>();

		for (final B2BCustomerModel user : users)
		{
			userUID = user.getUid();
			LOG.info("Approver Id: " + userUID);
			userPK = user.getPk().toString();
			LOG.info("Approver Id: " + userPK);
			uidCollection.add(userUID + "_" + userPK);
		}
		Collections.sort((List<String>) uidCollection);
		id.append(StringUtils.join(Arrays.asList(new Collection[]
		{ uidCollection }), "_"));
		LOG.info("Template code: " + id.toString());
		return id.toString();
	}


}
