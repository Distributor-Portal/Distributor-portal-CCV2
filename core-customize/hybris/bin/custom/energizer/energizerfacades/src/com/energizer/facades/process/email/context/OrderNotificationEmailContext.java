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
package com.energizer.facades.process.email.context;

import de.hybris.platform.acceleratorservices.model.cms2.pages.EmailPageModel;
import de.hybris.platform.acceleratorservices.process.email.context.AbstractEmailContext;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.commercefacades.order.data.OrderData;
import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.user.UserService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.energizer.core.constants.EnergizerCoreConstants;
import com.energizer.core.data.EnergizerDeliveryNoteData;


/**
 * Context (velocity) for email order notification.
 */
public class OrderNotificationEmailContext extends AbstractEmailContext<OrderProcessModel>
{

	protected static final Logger LOG = Logger.getLogger(OrderNotificationEmailContext.class);

	private String salesRepName;

	private String salesRepEmailID;

	private boolean isSalesRep;

	private Converter<OrderModel, OrderData> orderConverter;
	private OrderData orderData;
	@Resource
	private UserService userService;

	protected ConfigurationService configurationService;

	@Override
	public void init(final OrderProcessModel orderProcessModel, final EmailPageModel emailPageModel)
	{

		super.init(orderProcessModel, emailPageModel);
		userService.setCurrentUser(orderProcessModel.getOrder().getUser());
		orderData = getOrderConverter().convert(orderProcessModel.getOrder());
		orderData.setOrderComments(orderProcessModel.getOrder().getOrderComments());
		setDeliveryNoteFiles(orderProcessModel.getOrder(), orderData);
		final Date date = orderData.getRequestedDeliveryDate();
		final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
		final String formattedDate = dateFormat.format(date);
		orderData.setRequestedDeliveryDateDisplay(formattedDate);



		if (BASE_SITE != null)
		{
			super.put("env", configurationService.getConfiguration().getString("mail.enviorment"));
		}

		//Added code changes for WeSell Implementation - START
		if (null != orderData.getSalesRepName() && null != orderData.getSalesRepEmailID())
		{
			this.salesRepName = orderData.getSalesRepName();
			this.salesRepEmailID = orderData.getSalesRepEmailID();
			this.isSalesRep = orderData.isPlacedBySalesRep();

			if (null != emailPageModel
					&& emailPageModel.getUid().equalsIgnoreCase(EnergizerCoreConstants.WESELL_SEND_SAP_FAILED_EMAIL_PAGE_UID))
			{
				LOG.info(
						"Error occured while submitting the order to SAP, so sending the email to the IT support DL mailbox for analysis.....");
				put(EMAIL, configurationService.getConfiguration().getString(EnergizerCoreConstants.WESELL_IT_MAIL_ERRORS_EMAILID));
				put(DISPLAY_NAME, configurationService.getConfiguration()
						.getString(EnergizerCoreConstants.WESELL_IT_MAIL_ERRORS_EMAIL_DISPLAY_NAME));
			}
			else
			{
				put(EMAIL, orderData.getSalesRepEmailID());
				put(DISPLAY_NAME, orderData.getSalesRepName());
			}

			//Set URL Encoding Attributes for Sales Rep orders other than 'USD'.
			if (null != orderProcessModel.getOrder() && null != orderProcessModel.getOrder().getStore()
					&& !orderProcessModel.getOrder().getCurrency().getIsocode().equalsIgnoreCase("USD"))
			{
				put(BASE_URL, getSiteBaseUrlResolutionService().getWebsiteUrlForSite(getBaseSite(),
						"/" + orderProcessModel.getOrder().getStore().getDefaultCurrency().getIsocode(), true, ""));
			}
			else
			{
				put(BASE_URL,
						getSiteBaseUrlResolutionService().getWebsiteUrlForSite(getBaseSite(), getUrlEncodingAttributes(), true, ""));
			}

			LOG.info("Sales Rep Name ::: " + orderData.getSalesRepName() + ", Sales Rep Email ID ::: "
					+ orderData.getSalesRepEmailID());
		}
		//Added code changes for WeSell Implementation - END
	}

	@Override
	protected BaseSiteModel getSite(final OrderProcessModel orderProcessModel)
	{
		return orderProcessModel.getOrder().getSite();
	}

	@Override
	protected CustomerModel getCustomer(final OrderProcessModel orderProcessModel)
	{
		return (CustomerModel) orderProcessModel.getOrder().getUser();
	}

	protected Converter<OrderModel, OrderData> getOrderConverter()
	{
		return orderConverter;
	}

	@Required
	public void setOrderConverter(final Converter<OrderModel, OrderData> orderConverter)
	{
		this.orderConverter = orderConverter;
	}

	public OrderData getOrder()
	{
		return orderData;
	}

	@Override
	protected LanguageModel getEmailLanguage(final OrderProcessModel orderProcessModel)
	{
		return orderProcessModel.getOrder().getLanguage();
	}

	@Override
	protected ConfigurationService getConfigurationService()
	{
		return configurationService;
	}

	@Override
	@Required
	public void setConfigurationService(final ConfigurationService configurationService)
	{
		this.configurationService = configurationService;
	}

	protected void setDeliveryNoteFiles(final OrderModel orderEntry, final OrderData orderData)
	{

		if (null != orderEntry.getDeliveryNoteFiles() && !orderEntry.getDeliveryNoteFiles().getMedias().isEmpty())
		{

			final List<EnergizerDeliveryNoteData> deliveryNoteFilesList = new ArrayList<EnergizerDeliveryNoteData>();
			final Collection<MediaModel> mediaModels = orderEntry.getDeliveryNoteFiles().getMedias();

			EnergizerDeliveryNoteData deliveryNoteFileData = null;
			for (final MediaModel media : mediaModels)
			{
				deliveryNoteFileData = new EnergizerDeliveryNoteData();
				if (null != media)
				{
					deliveryNoteFileData.setFileName(media.getAltText());
					deliveryNoteFileData.setFileSize(media.getSize());
					deliveryNoteFileData.setUrl(media.getURL());
					deliveryNoteFileData.setMediaCode(media.getCode());
					deliveryNoteFileData.setMimeType(media.getMime());
				}
				deliveryNoteFilesList.add(deliveryNoteFileData);
			}
			orderData.setDeliveryNoteFiles(deliveryNoteFilesList);
			LOG.info(
					"Number of files uploaded for this order '" + orderEntry.getCode() + "' is ::: " + deliveryNoteFilesList.size());
		}
	}
}
