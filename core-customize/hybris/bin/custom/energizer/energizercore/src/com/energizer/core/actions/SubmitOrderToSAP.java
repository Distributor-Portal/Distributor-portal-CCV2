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

import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.b2b.process.approval.actions.AbstractSimpleB2BApproveOrderDecisionAction;
import de.hybris.platform.b2b.process.approval.model.B2BApprovalProcessModel;
import de.hybris.platform.commercefacades.order.data.OrderData;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.task.RetryLaterException;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.Scope;

import com.energizer.services.order.EnergizerB2BOrderService;


/**
 * Sends an order approval email.
 */
@Scope("tenant")
public class SubmitOrderToSAP extends AbstractSimpleB2BApproveOrderDecisionAction
{
	@Resource(name = "energizerB2BOrderService")
	EnergizerB2BOrderService energizerB2BOrderService;


	/**
	 * The Constant LOG.
	 */
	private static final Logger LOG = Logger.getLogger(SubmitOrderToSAP.class);
	private Converter<OrderModel, OrderData> energizerOrderConverter;

	@SuppressWarnings("unused")
	@Override
	public Transition executeAction(final B2BApprovalProcessModel process) throws RetryLaterException
	{
		OrderModel order = null;
		final String OrderCreateResponse = null;
		final String OrderCreatexml = null;
		final OrderData orderData = null;
		int result = 0;
		try
		{
			order = process.getOrder();
			final B2BCustomerModel user = (B2BCustomerModel) order.getUser();

			if (LOG.isDebugEnabled())
			{
				LOG.debug(String.format("Process for accelerator: %s in step %s order: %s user: %s ", process.getCode(), getClass(),
						order.getUnit(), user.getUid()));
			}

			result = energizerB2BOrderService.createOrder(order);

			if (result == 1)
			{
				return Transition.OK;
			}
			else
			{
				LOG.info("Response from simulate was null");
				order.setStatus(OrderStatus.PENDING);
				return Transition.NOK;
			}

		}
		catch (final Exception exception)
		{
			LOG.error(exception.getMessage(), exception);
			this.handleError(order, exception);
			//throw new RuntimeException(exception.getMessage(), exception);
			order.setStatus(OrderStatus.PENDING);
			return Transition.NOK;
		}
	}

	protected void handleError(final OrderModel order, final Exception exception)
	{
		if (order != null)
		{
			this.setOrderStatus(order, OrderStatus.B2B_PROCESSING_ERROR);
		}
		LOG.error(exception.getMessage(), exception);
	}

	/*
	 * public Converter<OrderModel, OrderData> getEnergizerOrderConverter() { return energizerOrderConverter; }
	 * 
	 * @Required public void setEnergizerOrderConverter(final Converter<OrderModel, OrderData> energizerOrderConverter) {
	 * this.energizerOrderConverter = energizerOrderConverter; }
	 */
}
