/**
 * 
 */
package com.energizer.core.actions;

import de.hybris.platform.b2b.enums.PermissionStatus;
import de.hybris.platform.b2b.process.approval.actions.AbstractSimpleB2BApproveOrderDecisionAction;
import de.hybris.platform.b2b.process.approval.actions.B2BPermissionResultHelperImpl;
import de.hybris.platform.b2b.process.approval.model.B2BApprovalProcessModel;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.processengine.action.AbstractSimpleDecisionAction;
import de.hybris.platform.task.RetryLaterException;

import java.util.Collection;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.energizer.core.model.EnergizerB2BCustomerModel;
import com.energizer.services.order.EnergizerB2BPermissionService;


/**
 * @author kaushik.ganguly
 * 
 */
public class FindApprovers extends AbstractSimpleB2BApproveOrderDecisionAction
{
	private static final Logger LOG = Logger.getLogger(FindApprovers.class);
	private EnergizerB2BPermissionService b2bPermissionService;
	private B2BPermissionResultHelperImpl permissionResultHelper;

	@Override
	public AbstractSimpleDecisionAction.Transition executeAction(final B2BApprovalProcessModel approvalProcess)
			throws RetryLaterException
	{
		OrderModel order = null;
		try
		{
			order = approvalProcess.getOrder();

			final Collection openPermissionsForOrder = getPermissionResultHelper().filterResultByPermissionStatus(
					order.getPermissionResults(), PermissionStatus.OPEN);

			final List permissionResults = b2bPermissionService.getApproversForOpenPermission(order,
					(EnergizerB2BCustomerModel) order.getUser(), openPermissionsForOrder);

			if (CollectionUtils.isNotEmpty(permissionResults))
			{
				order.setPermissionResults(permissionResults);
				this.modelService.save(order);

				// Added for WeSell Implementation - START
				if (null != order.getPlacedBySalesRep() && order.getPlacedBySalesRep())
				{
					LOG.info(
							"Order placed by Sales Rep, so redirecting to 'checkOrderAutoApprovalForWeSell' action to decide whether it needs approval or not !!");
					return AbstractSimpleDecisionAction.Transition.NOK;
				}
				// Added for WeSell Implementation - END
				return AbstractSimpleDecisionAction.Transition.OK;
			}
			else if (CollectionUtils.isEmpty(permissionResults))
			{
				// Added for WeSell Implementation - START
				if (null != order.getPlacedBySalesRep() && order.getPlacedBySalesRep())
				{
					LOG.info("Order placed by Sales Rep, so redirecting to check if it needs explicit approval !!");
					return AbstractSimpleDecisionAction.Transition.NOK;
				}
				else
				{

					LOG.info("Order placed by Non-Sales Rep, so redirecting to check if it needs explicit approval !!");
					return AbstractSimpleDecisionAction.Transition.OK;

				}
				// Added for WeSell Implementation - END
			}
			//return AbstractSimpleDecisionAction.Transition.NOK;
			return AbstractSimpleDecisionAction.Transition.OK;
		}
		catch (final Exception e)
		{
			LOG.error(e.getMessage(), e);
			handleError(order, e);
		}
		return AbstractSimpleDecisionAction.Transition.NOK;
	}

	private void handleError(final OrderModel order, final Exception e)
	{
		if (order != null)
		{
			setOrderStatus(order, OrderStatus.B2B_PROCESSING_ERROR);
		}
		LOG.error(e.getMessage(), e);
	}

	public EnergizerB2BPermissionService getB2bPermissionService()
	{
		return this.b2bPermissionService;
	}

	@Required
	public void setB2bPermissionService(final EnergizerB2BPermissionService b2bPermissionService)
	{
		this.b2bPermissionService = b2bPermissionService;
	}

	public B2BPermissionResultHelperImpl getPermissionResultHelper()
	{
		return this.permissionResultHelper;
	}

	@Required
	public void setPermissionResultHelper(final B2BPermissionResultHelperImpl permissionResultHelper)
	{
		this.permissionResultHelper = permissionResultHelper;
	}



}
