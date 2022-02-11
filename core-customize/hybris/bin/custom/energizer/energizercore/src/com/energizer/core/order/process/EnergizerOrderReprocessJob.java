package com.energizer.core.order.process;

import de.hybris.platform.acceleratorservices.email.EmailService;
import de.hybris.platform.acceleratorservices.model.email.EmailAddressModel;
import de.hybris.platform.acceleratorservices.model.email.EmailMessageModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import de.hybris.platform.servicelayer.exceptions.ModelSavingException;
import de.hybris.platform.servicelayer.model.ModelService;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.log4j.Logger;

import com.energizer.core.constants.EnergizerCoreConstants;
import com.energizer.core.model.EnergizerCronJobModel;
import com.energizer.services.order.dao.impl.DefaultEnergizerOrderDAO;


public class EnergizerOrderReprocessJob extends AbstractJobPerformable<EnergizerCronJobModel>
{
	protected static final Logger LOG = Logger.getLogger(EnergizerOrderReprocessJob.class);

	@Resource(name = "defaultEnergizerOrderDAO")
	private DefaultEnergizerOrderDAO defaultEnergizerOrderDAO;

	@Resource(name = "businessProcessService")
	private BusinessProcessService businessProcessService;

	@Resource(name = "modelService")
	private ModelService modelService;

	@Resource(name = "configurationService")
	private ConfigurationService configurationService;

	@Resource(name = "emailService")
	private EmailService emailService;

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable#perform(de.hybris.platform.cronjob.model.CronJobModel)
	 */
	@Override
	public PerformResult perform(final EnergizerCronJobModel cronJob)
	{
		LOG.info("Entering order reprocessing cronjob ... ");

		final List<OrderModel> orders = defaultEnergizerOrderDAO.getReprocessedOrders();
		LOG.info("No. of orders to be reprocessed now  ::: " + (null != orders ? orders.size() : 0));

		final Set<String> processedOrders = new HashSet<String>();
		final Set<String> unProcessedOrders = new HashSet<String>();
		if (null != orders)
		{
			try
			{
				for (final OrderModel order : orders)
				{
					try
					{
						for (final OrderProcessModel orderProcess : order.getOrderProcess())
						{
							if (orderProcess.getProcessDefinitionName()
									.equalsIgnoreCase(EnergizerCoreConstants.ORDER_PROCESS_DEFINITION_NAME))
							{
								try
								{
									LOG.info("Processing order : " + order.getCode());
									businessProcessService.restartProcess(orderProcess,
											EnergizerCoreConstants.Approval_Process_StartAction);
									order.setReprocessed(true);
									processedOrders.add(order.getCode());
								}
								catch (final Exception e)
								{
									unProcessedOrders.add(order.getCode());
									LOG.error("Exception occured while reprocessiong the order...." + e);
									e.printStackTrace();
								}
								break;
							}
						}
						modelService.save(order);
						LOG.info("Reprocessed & saved the order : " + order.getCode());
					}
					catch (final ModelSavingException me)
					{
						unProcessedOrders.add(order.getCode());
						LOG.error("Exception occured while saving the order..." + me);
						me.printStackTrace();
					}
					catch (final Exception e)
					{
						unProcessedOrders.add(order.getCode());
						LOG.error("Exception Occured while reprocessiong the order...." + e);
						e.printStackTrace();
					}
				}
			}
			catch (final Exception e)
			{
				LOG.error("Exception Occured while reprocessiong the order...." + e);
				e.printStackTrace();
			}

			// Sending Email Notification to IT support mailbox for further inspection/evaluation
			if(null != processedOrders && processedOrders.size() > 0) {
				sendOrderReprocessUpdatesEmailNotification(processedOrders, "success");
				LOG.info("No of orders processed successfully  ::: " + processedOrders.size());
			}
			if (null != unProcessedOrders && unProcessedOrders.size() > 0)
			{
				sendOrderReprocessUpdatesEmailNotification(unProcessedOrders, "failure");
				LOG.info("No of orders processed but failed again  ::: " + unProcessedOrders.size());
			}
		}
		else
		{
			LOG.info("No orders found for reprocessing ...");
		}
		LOG.info("Exiting order reprocessing cronjob ... ");
		return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
	}

	private void sendOrderReprocessUpdatesEmailNotification(final Set<String> orders, final String orderReprocessedStatus)
	{

		String supportEmail = configurationService.getConfiguration().getString(
				EnergizerCoreConstants.WESELL_IT_MAIL_ERRORS_EMAILID,
				"B2B_IT_Support@Edgewell.com");
		final String displayName = configurationService.getConfiguration()
				.getString(EnergizerCoreConstants.WESELL_IT_MAIL_ERRORS_EMAIL_DISPLAY_NAME,
				"B2B_IT_Support");
		final EmailAddressModel toAddress = emailService.getOrCreateEmailAddressForEmail(supportEmail,
				displayName);

		supportEmail = configurationService.getConfiguration()
				.getString(EnergizerCoreConstants.CRONJOBS_FROM_EMAIL,
						"hybrisdev@edgewell.com");
		final EmailAddressModel fromAddress = emailService.getOrCreateEmailAddressForEmail(supportEmail,
				"Hybris B2B Cronjob System");

		final StringBuilder emailBody = new StringBuilder();
		final StringBuilder emailSubject = new StringBuilder();

		emailSubject.append("Order Reprocessing Updates Email Notification");

		if (orderReprocessedStatus.equalsIgnoreCase("success"))
		{
			emailBody.append(
					"Please note that the following orders are reprocessed successfully by Hybris Order Reprocessing Cronjob. <br/><br/>");
		}
		else
		{
			emailBody.append(
					"Please note that the following orders failed during order reprocessing. Please check the server logs for more details. \n ");
		}

		if(null != orders && !orders.isEmpty()) {
			for (final String code : orders)
			{
				emailBody.append(code + "<br/>");
			}
		}

		emailBody.append("<br/><br/><br/><br/> This is an automatically generated email. Please do not reply.");

		final EmailMessageModel message = emailService.createEmailMessage(Arrays.asList(toAddress), null, null, fromAddress, "",
				emailSubject.toString(), emailBody.toString() + "<br/>", null);

		LOG.info("From email : " + fromAddress.getEmailAddress());
		LOG.info("To email : " + toAddress.getEmailAddress());

		emailService.send(message);
	}
}
