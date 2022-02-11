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
package com.energizer.core.event;

import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.servicelayer.event.events.AbstractEvent;


public class SubmitOrderToSAPFailedNotificationEvent extends AbstractEvent
{
	private final OrderProcessModel process;

	public SubmitOrderToSAPFailedNotificationEvent(final OrderProcessModel process)
	{
		this.process = process;
	}

	public OrderProcessModel getProcess()
	{
		return process;
	}
}
