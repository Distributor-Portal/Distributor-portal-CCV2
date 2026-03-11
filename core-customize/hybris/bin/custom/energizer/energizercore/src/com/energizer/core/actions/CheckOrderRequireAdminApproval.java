/**
 *
 */
package com.energizer.core.actions;

import de.hybris.platform.b2b.process.approval.actions.AbstractSimpleB2BApproveOrderDecisionAction;
import de.hybris.platform.b2b.process.approval.actions.B2BPermissionResultHelperImpl;
import de.hybris.platform.b2b.process.approval.model.B2BApprovalProcessModel;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.processengine.action.AbstractSimpleDecisionAction;
import de.hybris.platform.task.RetryLaterException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.energizer.services.order.EnergizerB2BPermissionService;


/**
 * @author kaushik.ganguly
 *
 */
public class CheckOrderRequireAdminApproval extends AbstractSimpleB2BApproveOrderDecisionAction
{
	private static final Logger LOG = Logger.getLogger(CheckOrderRequireAdminApproval.class);
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

			// Added for WeSell Implementation - START
			if (null != order.getPlacedBySalesRep() && order.getPlacedBySalesRep())
			{
				LOG.info("Order placed by ::: " + order.getSalesRepEmailID() + ", " + order.getSalesRepName());
				LOG.info(
						"Order placed by Sales Rep, so redirecting to 'performMerchantCheck' action to create order in SAP directly and update the order !!");
				return AbstractSimpleDecisionAction.Transition.OK;
			}
			else
			{
				LOG.info(
						"Order placed by a Non-Sales Rep, so redirecting to 'auditStartOfAdminApproval' action to start admin approval workflow !!");
				return AbstractSimpleDecisionAction.Transition.NOK;
			}
			// Added for WeSell Implementation - END
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
