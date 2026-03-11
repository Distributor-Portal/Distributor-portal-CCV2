/**
 *
 */
package com.energizer.core.datafeed.processor.product;

import de.hybris.platform.acceleratorservices.email.EmailService;
import de.hybris.platform.acceleratorservices.model.email.EmailAddressModel;
import de.hybris.platform.acceleratorservices.model.email.EmailMessageModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import de.hybris.platform.site.BaseSiteService;
import de.hybris.platform.util.Config;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;

import com.energizer.core.model.EnergizerCronJobModel;
import com.energizer.core.model.EnergizerProductModel;
import com.energizer.services.product.EnergizerProductService;


/**
 *
 * This processors imports the orphan product.
 *
 */
public class EnergizerOrphanedProductProcessor extends AbstractJobPerformable<EnergizerCronJobModel>
{

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable#perform(de.hybris.platform.cronjob.model.CronJobModel
	 * )
	 */
	@Resource
	private EnergizerProductService energizerProductService;

	@Resource
	private EmailService emailService;

	@Resource
	private ConfigurationService configurationService;

	@Resource
	private BaseSiteService baseSiteService;

	private static final Logger LOG = Logger.getLogger(EnergizerOrphanedProductProcessor.class);

	@Override
	public PerformResult perform(final EnergizerCronJobModel arg0)
	{
		try
		{
			//get all products where category is null

			final List<EnergizerProductModel> orphProductList = energizerProductService.getEnergizerOrphanedProductList();
			if (orphProductList != null && !orphProductList.isEmpty())
			{
				final StringBuilder sbProducts = new StringBuilder();
				for (final EnergizerProductModel product : orphProductList)
				{
					LOG.info("PRODUCT WITH NO CATEGORY : " + product.getCode());
					sbProducts.append(product.getCode()).append(", ");
				}
				sendEmail(sbProducts.toString(), arg0.getEmailAddress());
			}
		}
		catch (final Exception e)
		{
			LOG.error(" Error while processing orphaned product list ", e);
		}
		return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
	}

	private void sendEmail(final String strProducts, final String toEmail)
	{
		try
		{
			final BaseSiteModel currentBaseSite = this.baseSiteService.getCurrentBaseSite();

			final EmailAddressModel toAddress = emailService.getOrCreateEmailAddressForEmail(
					configurationService.getConfiguration().getString("energizer.customer.support.to.email.offline"),
					"Order Portal Team");

			final EmailAddressModel fromEmailAddress = emailService.getOrCreateEmailAddressForEmail(configurationService
					.getConfiguration().getString("cronjobs.from.email", Config.getParameter("fromEmailAddress.orderEmailSender")),
					"Order Portal Team");

			final EmailMessageModel message = emailService.createEmailMessage(Arrays.asList(toAddress), null, null, fromEmailAddress,
					"", "Orphaned Products", "(" + configurationService.getConfiguration().getString("mail.enviorment") + ")"
							+ "List of products orphaned : \n" + strProducts + "\n",
					null);
			emailService.send(message);
		}
		catch (final Exception e)
		{
			LOG.info("Printing error in ");
			e.printStackTrace();
		}

	}
}
