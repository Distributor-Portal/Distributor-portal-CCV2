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
import de.hybris.platform.servicelayer.event.EventService;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.energizer.core.event.SubmitOrderToSAPFailedNotificationEvent;


/**
 * Sends an order approval email.
 */
@Scope("tenant")
@Component("informOfOrderApproval")
public class SendsubmitOrderToSAPFailedNotification extends AbstractProceduralAction<OrderProcessModel>
{
	/**
	 * The Constant LOG.
	 */
	private static final Logger LOG = Logger.getLogger(SendsubmitOrderToSAPFailedNotification.class);
	private EventService eventService;

	@Override
	public void executeAction(final OrderProcessModel process)
	{
		getEventService().publishEvent(new SubmitOrderToSAPFailedNotificationEvent(process));
		Logger.getLogger(getClass()).info("Process: " + process.getCode() + " in step " + getClass());
	}

	@Required
	public void setEventService(final EventService eventService)
	{
		this.eventService = eventService;
	}

	protected EventService getEventService()
	{
		return eventService;
	}
}
