/**
 *
 */
package com.energizer.core.cleanup;

import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.PerformResult;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.energizer.core.model.EnergizerCronJobModel;
import com.energizer.services.product.EnergizerProductService;


/**
 * @author Pavanip
 *
 */
public class EnergizerPriceRowCleanUpJob extends AbstractJobPerformable<EnergizerCronJobModel>
{
	final Logger LOG = Logger.getLogger(EnergizerPriceRowCleanUpJob.class);

	@Autowired
	private EnergizerProductService energizerProductService;

	/*
	 * (non-Javadoc)
	 *
	 * @see de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable#perform(de.hybris.platform.cronjob.model.
	 * CronJobModel)
	 */
	@Override
	public PerformResult perform(final EnergizerCronJobModel energizerCronJobModel)
	{
		final String region = energizerCronJobModel.getRegion();
		try
		{
			final Long beforePriceRowCleanUpStartTime = System.currentTimeMillis();
			if (null != region && StringUtils.isNotEmpty(region))
			{
				final boolean cleanUpStatus = energizerProductService.cleanUpInActivePriceRowForRegion(region);
				if (cleanUpStatus)
				{
					LOG.info("Price row cleanup is successful !!");

					final Long afterPriceRowCleanUpEndTime = System.currentTimeMillis();
					LOG.info("Total time taken for Price Row clean up : "
							+ (afterPriceRowCleanUpEndTime - beforePriceRowCleanUpStartTime) + " milliseconds, "
							+ (afterPriceRowCleanUpEndTime - beforePriceRowCleanUpStartTime) / 1000 + " seconds ...");

					return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
				}
			}
			else
			{
				LOG.info("Region is null/empty. Cannot clean up inactive Price rows ...");
			}
		}
		catch (final Exception e)
		{
			LOG.info("Exception occurred while cleaning up inactive rice rows ...");
			e.printStackTrace();
			return new PerformResult(CronJobResult.ERROR, CronJobStatus.FINISHED);
		}
		return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
	}

}
