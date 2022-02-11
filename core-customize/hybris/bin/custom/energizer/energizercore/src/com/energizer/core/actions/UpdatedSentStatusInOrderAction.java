/*
 * [y] hybris Platform
 *
 * Copyright (c) 2000-2014 hybris AG
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of hybris
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with hybris.
 *
 *  
 */
package com.energizer.core.actions;

import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.processengine.action.AbstractProceduralAction;
import de.hybris.platform.servicelayer.model.ModelService;

import org.springframework.beans.factory.annotation.Required;


/**
 * Sends Order Pending Approval Notification event.
 */
public class UpdatedSentStatusInOrderAction extends AbstractProceduralAction<OrderProcessModel>
{
	private ModelService modelService;

	@Override
	public void executeAction(final OrderProcessModel process)
	{
		process.getOrder().setIsPendingApprovalEmailSent(true);
		getModelService().save(process.getOrder());
	}


	@Override
	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	@Override
	public ModelService getModelService()
	{
		return modelService;
	}
}
