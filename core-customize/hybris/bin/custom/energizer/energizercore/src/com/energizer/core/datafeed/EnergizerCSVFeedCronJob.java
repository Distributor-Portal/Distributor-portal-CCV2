/**
 *
 */
package com.energizer.core.datafeed;

import de.hybris.platform.acceleratorservices.email.EmailService;
import de.hybris.platform.acceleratorservices.model.email.EmailAttachmentModel;
import de.hybris.platform.core.Registry;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.CronJobService;
import de.hybris.platform.servicelayer.cronjob.PerformResult;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.energizer.core.constants.EnergizerCoreConstants;
import com.energizer.core.datafeed.processor.product.EnergizerProduct2CategoryRelationCSVProcessor;
import com.energizer.core.model.EnergizerCronJobModel;


/**
 * @author M9005674
 *
 */
public class EnergizerCSVFeedCronJob extends AbstractJobPerformable<EnergizerCronJobModel>
{

	/*
	 * (non-Javadoc)
	 *
	 * @see de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable#perform(de.hybris.platform.cronjob.model.
	 * CronJobModel )
	 */

	private static final Logger LOG = Logger.getLogger(EnergizerCSVFeedCronJob.class);

	private static DecimalFormat df2 = new DecimalFormat("#.##");

	@Resource
	EmailService emailService;

	private CronJobService cronJobService;

	/**
	 * @return the cronJobService
	 */
	public CronJobService getCronJobService()
	{
		return cronJobService;
	}

	/**
	 * @param cronJobService
	 *           the cronJobService to set
	 */
	public void setCronJobService(final CronJobService cronJobService)
	{
		this.cronJobService = cronJobService;
	}

	// Added by Soma - To abort those cronjobs during run time that take more time for processing.
	@Override
	public boolean isAbortable()
	{
		return true;
	}

	@Resource
	private ConfigurationService configurationService;

	@Override
	public synchronized PerformResult perform(final EnergizerCronJobModel cronjob)
	{
		LOG.info("************************ PROCESSING START FOR THIS CRONJOB  ***************************");

		final Long jobStartTime = System.currentTimeMillis();
		LOG.info("Before processing this cronjob : " + jobStartTime + " milliseconds !!");

		List<EnergizerCSVFeedError> errors = new ArrayList<EnergizerCSVFeedError>();
		List<EnergizerCSVFeedError> techfeedErrors = new ArrayList<EnergizerCSVFeedError>();
		List<EnergizerCSVFeedError> busfeedErrors = new ArrayList<EnergizerCSVFeedError>();
		PerformResult performResult = null;
		final List<String> emailAddress = new ArrayList<String>();
		final String type = cronjob.getType();
		if (type == null)
		{
			LOG.info("There is no Type defined for the job " + cronjob.getCode());
			LOG.info(
					"*********************************** NOTHING TO PROCESS FOR THIS CRONJOB  *****************************************");
			return new PerformResult(CronJobResult.ERROR, CronJobStatus.ABORTED);
		}
		final AbstractEnergizerCSVProcessor energizerCSVProcessor = (AbstractEnergizerCSVProcessor) Registry.getApplicationContext()
				.getBean(type);
		/* Added for EMEA cronjob import */
		energizerCSVProcessor.setCronjob(cronjob);
		/* EMEA End */
		final List<File> files = energizerCSVProcessor.getFilesForFeedType(type);
		if (null == files || files.size() == 0)
		{
			LOG.info("Found " + files.size() + " CSV files to process.");
		}
		else
		{
			LOG.info("Found " + files.size() + " CSV files to process. The files are listed below.");
		}

		final int wesellCSVFilesCount = Integer
				.parseInt(configurationService.getConfiguration().getString("wesell.cmir.csv.files.count"));

		int count = 0;
		if (null != files && !files.isEmpty() && files.size() > 0)
		{
			for (final File file : files)
			{
				count++;
				if ((file.length() / 1024) >= 1024)
				{
					LOG.info(count + ". File name  == '" + file.getName() + "' and it's size is == '" + getFileSizeKiloBytes(file)
							+ "' or '" + getFileSizeMegaBytes(file) + "'");
				}
				else
				{
					LOG.info(
							count + ". File name == '" + file.getName() + "' and it's size is == '" + getFileSizeKiloBytes(file) + "'");
				}
			}
		}

		// If the file is not found to process, then the cronjob result is returned as such.
		if (null == files || files.size() == 0)
		{
			LOG.info("************************* NO FILES FOUND, NOTHING TO PROCESS FOR THIS CRONJOB  ****************************");
			return new PerformResult(CronJobResult.FILE_NOT_FOUND, CronJobStatus.FINISHED);
		}
		else if (null != files && files.size() > 0
				&& (cronjob.getRegion().equalsIgnoreCase(EnergizerCoreConstants.WESELL) && files.size() > wesellCSVFilesCount))
		{
			LOG.info("Total files : " + files.size());
			LOG.info("MORE THAN '" + wesellCSVFilesCount + "' FILES FOUND TO PROCESS FOR WESELL, SO IGNORING THIS CRONJOB ...");
			return new PerformResult(CronJobResult.FAILURE, CronJobStatus.FINISHED);
		}

		Boolean exceptionOccured = false;
		if (null != cronjob.getEmailAddress())
		{
			emailAddress.add(cronjob.getEmailAddress());
		}
		energizerCSVProcessor.flush(); /* This is to flush the buffer of existing errorList and message as well */
		for (final File file : files)
		{
			final Long fileProcessingStartTime = System.currentTimeMillis();

			Iterable<CSVRecord> csvRecords;
			try
			{
				csvRecords = energizerCSVProcessor.parse(file);
				LOG.info("************** PROCESSING START FOR THIS FILE  '" + file.getName() + "' ***************");
				LOG.info("Before processing this file : " + fileProcessingStartTime + " milliseconds !!");
				errors = energizerCSVProcessor.process(csvRecords, cronjob.getCatalogName(), cronjob);
				exceptionOccured = (errors.size() != 0) ? true : false;
				energizerCSVProcessor.setMasterDataStream(new DataInputStream(new FileInputStream(file)));
				final List<EmailAttachmentModel> emailAttachmentList = new ArrayList<EmailAttachmentModel>();
				final EmailAttachmentModel attachmentModel = emailService.createEmailAttachment(
						energizerCSVProcessor.getMasterDataStream(),
						StringUtils.replace(file.getName().toLowerCase(), ".csv",
								"_" + new Date().getTime() + "." + de.hybris.platform.impex.constants.ImpExConstants.File.EXTENSION_CSV)
								.toLowerCase(),
						de.hybris.platform.impex.constants.ImpExConstants.File.MIME_TYPE_CSV);
				emailAttachmentList.add(attachmentModel);
				if (cronjob.getTechnicalEmailAddress().isEmpty())
				{
					cronjob.setTechnicalEmailAddress(emailAddress);
				}
				if (cronjob.getBusinessEmailAddress().isEmpty())
				{
					cronjob.setBusinessEmailAddress(emailAddress);
				}
				techfeedErrors = energizerCSVProcessor.getTechnicalFeedErrors();
				if (!techfeedErrors.isEmpty())
				{
					energizerCSVProcessor.setRecordFailed(energizerCSVProcessor.getTechRecordError());
					energizerCSVProcessor.mailErrors(cronjob, techfeedErrors, cronjob.getTechnicalEmailAddress(), emailAttachmentList);
				}
				busfeedErrors = energizerCSVProcessor.getBusinessFeedErrors();
				if (!busfeedErrors.isEmpty())
				{
					energizerCSVProcessor.setRecordFailed(energizerCSVProcessor.getBusRecordError());
					energizerCSVProcessor.mailErrors(cronjob, busfeedErrors, cronjob.getBusinessEmailAddress(), emailAttachmentList);
				}
				energizerCSVProcessor.setTotalRecords(0);
				energizerCSVProcessor.setRecordFailed(0);
				energizerCSVProcessor.setRecordSucceeded(0);
				energizerCSVProcessor.setBusRecordError(0);
				energizerCSVProcessor.setTechRecordError(0);
				emailAttachmentList.clear();
				if ((techfeedErrors != null && techfeedErrors.size() > 0) || (busfeedErrors != null && busfeedErrors.size() > 0))
				{
					if (!(energizerCSVProcessor instanceof EnergizerProduct2CategoryRelationCSVProcessor))
					{
						energizerCSVProcessor.cleanup(type, file, cronjob, true);
					}
					energizerCSVProcessor.flush();
				}
				else
				{
					if (!(energizerCSVProcessor instanceof EnergizerProduct2CategoryRelationCSVProcessor))
					{
						energizerCSVProcessor.cleanup(type, file, cronjob, false);
					}
					energizerCSVProcessor.flush();
				}

				// If the cronjob abort is requested, then perform clean up and return the PerformResult
				this.modelService.refresh(cronjob);
				if (clearAbortRequestedIfNeeded(cronjob))
				{
					LOG.info(cronjob.getRegion() + " : CRONJOB IS ABORTED WHILE PERFORMING ...");

					/* This is to flush the buffer of existing errorList and message as well */
					energizerCSVProcessor.flush();
					//abort the job
					cronJobService.requestAbortCronJob(cronjob);

					return new PerformResult(CronJobResult.ERROR, CronJobStatus.ABORTED);
				}
			}
			catch (final FileNotFoundException e)
			{
				LOG.error("File Not found", e);
				energizerCSVProcessor.flush();
				//exceptionOccured = true;
				// If the file is not found to process, then the cronjob result is returned as such.
				LOG.info("NO FILES FOUND TO PROCESS, IGNORING THIS CRONJOB ...");
				return new PerformResult(CronJobResult.FILE_NOT_FOUND, CronJobStatus.FINISHED);
			}
			if (exceptionOccured)
			{
				performResult = new PerformResult(CronJobResult.ERROR, CronJobStatus.FINISHED);
			}
			else
			{
				performResult = new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
			}
			energizerCSVProcessor.flush();

			final Long fileProcessingEndTime = System.currentTimeMillis();
			LOG.info("After processing this file : " + fileProcessingEndTime + " milliseconds !!");

			LOG.info("Cronjob file processing time taken in milliseconds == " + (fileProcessingEndTime - fileProcessingStartTime)
					+ " , seconds == " + (fileProcessingEndTime - fileProcessingStartTime) / 1000);

			LOG.info("************** PROCESSING END FOR THIS FILE  '" + file.getName() + "' ***************");
		}

		final Long jobEndTime = System.currentTimeMillis();

		LOG.info("Cronjob total processing time taken in milliseconds == " + (jobEndTime - jobStartTime) + ", seconds ==  "
				+ (jobEndTime - jobStartTime) / 1000);

		LOG.info("************************ PROCESSING END FOR THIS CRONJOB  ***************************");

		return performResult;
	}

	private String getFileSizeMegaBytes(final File file)
	{
		return df2.format((double) file.length() / (1024 * 1024)) + " mb";
	}

	private String getFileSizeKiloBytes(final File file)
	{
		return df2.format((double) file.length() / 1024) + "  kb";
	}

}