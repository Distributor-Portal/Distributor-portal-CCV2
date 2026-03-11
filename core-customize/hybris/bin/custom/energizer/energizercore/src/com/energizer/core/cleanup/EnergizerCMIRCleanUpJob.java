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
public class EnergizerCMIRCleanUpJob extends AbstractJobPerformable<EnergizerCronJobModel>
{
	final Logger LOG = Logger.getLogger(EnergizerCMIRCleanUpJob.class);

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
			final Long beforeCMIRCleanUpStartTime = System.currentTimeMillis();
			if (null != region && StringUtils.isNotEmpty(region))
			{
				final boolean cleanUpStatus = energizerProductService.cleanUpInActiveCMIRForRegion(region);
				if (cleanUpStatus)
				{
					LOG.info("CMIR cleanup is successful !!");

					final Long afterCMIRCleanUpEndTime = System.currentTimeMillis();
					LOG.info("Total time taken for CMIR clean up : " + (afterCMIRCleanUpEndTime - beforeCMIRCleanUpStartTime)
							+ " milliseconds, " + (afterCMIRCleanUpEndTime - beforeCMIRCleanUpStartTime) / 1000 + " seconds ...");

					return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
				}
			}
			else
			{
				LOG.info("Region is null/empty. Cannot clean up inactive CMIRs ...");
			}
		}
		catch (final Exception e)
		{
			LOG.info("Exception occurred while cleaning up inactive CMIRs ...");
			e.printStackTrace();
			return new PerformResult(CronJobResult.ERROR, CronJobStatus.FINISHED);
		}
		return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
	}
}
